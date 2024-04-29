import { saveBookMark } from './bookMark.js';
import { fetchLike } from './giveLike.js';
import { renderVideoContent } from './renderVideo.js';
import { renderPhotos } from './renderPhoto.js';
import { renderInteractionIcons } from './renderIcon.js';

let currentPost = null;
let stompClient = null;

document.addEventListener('DOMContentLoaded', () => {
    fetchSinglePost();
    setupEventListeners();
});
function setupEventListeners() {
    const container = document.querySelector('.tweets-container');
    container.addEventListener('click', handlePostInteractions);

    const commentsContainer = document.querySelector('.comment-section');
    commentsContainer.addEventListener('click', handleCommentInteractions);
}
function handlePostInteractions(event) {
    const target = event.target;
    const postId = target.id.split('-')[1];
    if (target.matches('[id^="likeIcon-"]')) {
        fetchLike(postId, target);
    } else if (target.matches('[id^="bookMarkIcon-"]')) {
        saveBookMark(postId, target);
    }
}

function handleCommentInteractions(event) {
    const target = event.target;

    if (target.classList.contains('reply')) {
        const parentId = target.closest('.comment').id.split('-')[1];
        const parentCommentElement = document.getElementById(`comment-${parentId}`);
        let inputContainer = parentCommentElement.querySelector('.input-container');

        if (inputContainer.style.display === 'none' || !inputContainer.style.display) {
            inputContainer.style.display = 'block';
        } else {
            inputContainer.style.display = 'none';
        }

    } else if (target.classList.contains('loadComment')) {
        const parentCommentElement = target.closest('.comment');
        const parentId = parentCommentElement.id.split('-')[1];
        const repliesContainer = parentCommentElement.querySelector('.replies-container');

        if (!repliesContainer) {
            fetchCommentByParentId(parentId);
        } else {
            repliesContainer.style.display = repliesContainer.style.display === 'none' ? 'block' : 'none';
        }
    } else if (target.classList.contains('send-button')) {
        sendReplyComment(target);
    }
}
function sendReplyComment(target) {
    const inputField = target.closest('.input-container').querySelector('.input-field');
    const messageContent = inputField.value.trim();
    const commentId = target.closest('.comment').id.split('-')[1];
    if (messageContent && stompClient) {
        let chatMessage = {
            parentCommentId: commentId,
            postId: currentPost,
            text: messageContent,
        };
        stompClient.send(`/app/comment/${currentPost}`, {}, JSON.stringify(chatMessage));
        inputField.value = '';
        target.closest('.input-container').style.display = 'none'; // Hide input container after sending message
    }
}
function sendMessage() {
    let messageContent = document.getElementById('messageInput').value.trim();
    if (messageContent && stompClient) {
        let chatMessage = {
            postId : currentPost,
            text: messageContent,
        };
        stompClient.send(`/app/comment/${currentPost}`, {}, JSON.stringify(chatMessage));
        document.getElementById('messageInput').value = '';
    }
}

const currentURL = window.location.href;
const postId = currentURL.match(/\/(\d+)$/)[1];
let URL = '/post/'+ postId;
let container = document.querySelector('.tweets-container');

const fetchSinglePost = async () => {
    const response = await fetch(URL);
    const data = await response.json();
    console.log(data);
    openWebSocket(postId);
    const videoContent = data.content.videos.length > 0 ? renderVideoContent(data) : '';
    const photoContent = data.content.images.length > 0 ? renderPhotos(data.content.images) : '';

    let divContainer = document.createElement('div');
    divContainer.classList.add('tweets','row');

    divContainer.innerHTML = `
              <div class="col-auto" style="padding-right: 10px">
                <div class="user-pics"><img src="${data.photo}" alt="user3"></div>
              </div>
              <div class="user-content-box col">
                <!-- User profile and date -->
                <div class="user-names" style="height: 35px">
                  <h1 class="full-name" style="margin-top: 5px">${data.user.name}.</h1>
                  <p class="time me-3 ">${data.time}</p>
                </div>
                <!-- User profile and date End-->

                <!-- User Content image and text -->
                <div class="user-content">
                  <p>${data.content.text}</p>
                 
                  ${videoContent || photoContent}
                 
                </div>
                <!-- User Content image and text End -->
                ${renderInteractionIcons(data)}
              </div>
    `
    container.appendChild(divContainer);
    await fetchAllComment(postId)
}

const openWebSocket = (postId) => {
    currentPost = postId;
    connectCommentSession(postId);
}

const connectCommentSession = (postId) => {

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        stompClient.subscribe('/comment/messages/' + postId, function(messageOutput) {
            showMessageOutput(JSON.parse(messageOutput.body));
        });
    });
}

function showMessageOutput(messageOutput) {
    let commentContainer = document.querySelector('.comment-section');
    let firstChild = commentContainer.firstChild;

    let comment = createCommentElement(messageOutput, Boolean(messageOutput.parentCommentId)); // Utilize your existing function

    if (messageOutput.parentCommentId) {
        let parentComment = document.getElementById(`comment-${messageOutput.parentCommentId}`);

        if (messageOutput.rootCommentId) {
            let topLevelParent = document.getElementById(`comment-${messageOutput.rootCommentId}`);
            let repliesContainer = topLevelParent.querySelector('.replies-container');
            if (!repliesContainer) {
                repliesContainer = document.createElement('div');
                repliesContainer.className = 'replies-container';
                topLevelParent.appendChild(repliesContainer);
            }
            comment.querySelector('.comment-text').textContent = `@${parentComment.querySelector('.comment-author').textContent} ${messageOutput.text}`;
            repliesContainer.appendChild(comment);
        } else {
            let repliesContainer = parentComment.querySelector('.replies-container');
            if (!repliesContainer) {
                repliesContainer = document.createElement('div');
                repliesContainer.className = 'replies-container';
                parentComment.appendChild(repliesContainer);
            }
            repliesContainer.appendChild(comment);
        }
        repliesContainer.style.display = 'block';
    } else {
        commentContainer.insertBefore(comment, firstChild);
    }
}

const fetchAllComment = async (postId) => {
    try {
        const response = await fetch(`/post/comment?postId=${postId}`);
        const data = await response.json();
        console.log('Data received:', data);

        let commentContainer = document.querySelector('.comment-section');

            data.forEach(comment => {
                const commentDiv = createCommentElement(comment,false);
                commentContainer.appendChild(commentDiv);
            });
    } catch (error) {
        console.error('Failed to fetch comments:', error);
    }
}

const fetchCommentByParentId = async (parentId) => {
    try {
        const response = await fetch(`/post/reply?parentId=${parentId}`);
        const replies = await response.json();

        const parentCommentElement = document.getElementById(`comment-${parentId}`);
        let repliesContainer = parentCommentElement.querySelector('.replies-container');
        if (!repliesContainer) {
            repliesContainer = document.createElement('div');
            repliesContainer.className = 'replies-container';
            parentCommentElement.appendChild(repliesContainer);
            repliesContainer.style.display = 'none'; // Initially hidden
        }
        repliesContainer.innerHTML = '';

        replies.forEach(reply => {
            const replyElement = createCommentElement(reply, true);
            repliesContainer.appendChild(replyElement);
        });

        repliesContainer.style.display = (repliesContainer.style.display === 'none' ? 'block' : 'none');

    } catch (error) {
        console.error('Failed to fetch replies:', error);
    }
}

function createCommentElement(commentData, isReply) {
    const commentElement = document.createElement('div');
    commentElement.id = `comment-${commentData.id}`;
    commentElement.classList.add('comment');
    if (isReply) {
        commentElement.classList.add('nested-comment');
    }
    let displayText = commentData.text;
    if (commentData.rootCommentId && commentData.parentCommentId) {
        displayText = `@${commentData.userName} ${commentData.text}`;
    }

    commentElement.innerHTML = `
        <img src="${commentData.userImage}" alt="User Avatar" class="user-avatar">
        <div class="comment-content">
            <div class="user-display">
            
                <div class="comment-author" style="margin-right: 5px">${commentData.userName}</div>
                <div>
                    ${commentData.edited === true ?' <small style="margin-right: 15px">(edited)</small>' : ''}
                </div>
                <div class="comment-metadata" style="margin: 5px">${commentData.time}</div>
            </div>
            <div class="comment-text">${displayText}</div>
            <div class="comment-interactions d-flex justify-content-between">
                <div>
                    <i class="fa-regular fa-heart" style="cursor: pointer"></i>
                    <i class="fa-solid fa-reply reply" style="cursor: pointer; color: black"></i>
                     ${commentData.owner ?
                        `<div style="display: inline-block;">
                         <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" style="color: black" role="button" data-bs-toggle="dropdown" aria-expanded="false"></i>
                            <ul class="dropdown-menu">
                            <div class="d-flex justify-content-between" style="padding: 5px">
                                 <li class="commentBtn" id="commentEdit">Edit</li>
                                 <li class="commentBtn" id="commentDelete">Delete</li>
                            </div>
                            </ul>
                        </div>` : ''}
                </div>
                 <div>
                    ${!commentData.parentCommentId && commentData.childComment > 0 ? '<small class="loadComment" style="cursor: pointer;">See More ' + commentData.childComment + '</small>' : ''}
                </div>
             
            </div>
            <div class="input-container editInput" style="display: none;">
                <input type="text" id="messageInputs" class="input-field" placeholder="Type a message here">
                <button id="buttonEvent" tabindex="1" class="send-button">Send</button>
            </div>
        </div>
    `;
    if (commentData.owner) {
        const editButton = commentElement.querySelector('#commentEdit');
        editButton.addEventListener('click', () => editComment(commentData.id, displayText, commentData.edited));

        const deleteButton = commentElement.querySelector('#commentDelete');
        deleteButton.addEventListener('click', () => deleteComment(commentData.id));
    }

    return commentElement;
}
const editComment = (commentId,text,status) => {
    const commentElement = document.querySelector(`#comment-${commentId}`);
    const commentTextElement = document.querySelector(`#comment-${commentId} .comment-text`);
    const interactionsDiv = document.querySelector(`#comment-${commentId} .comment-interactions`);

    const interactionsDivParent = interactionsDiv.parentNode;
    const interactionsDivNextSibling = interactionsDiv.nextSibling;

    interactionsDivParent.removeChild(interactionsDiv);

    const inputField = document.createElement('input');
    inputField.setAttribute('type','text');
    inputField.classList.add('editInput');
    inputField.value = text;

    commentTextElement.parentNode.replaceChild(inputField,commentTextElement);

    const container = document.createElement('div');
    container.classList.add('btn-container');
    const saveButton = document.createElement('button');
    saveButton.textContent = 'Save';
    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Cancel';

    container.appendChild(saveButton);
    container.appendChild(cancelButton);

    saveButton.addEventListener('click',async () => {

        const newText = inputField.value;
        await editCommentBk(commentId,newText);
        commentTextElement.textContent = newText;
        inputField.parentNode.replaceChild(commentTextElement, inputField);
        container.parentNode.removeChild(container);
        interactionsDivParent.insertBefore(interactionsDiv, interactionsDivNextSibling);
        if(!status){
            const display = document.querySelector(`#comment-${commentId} .user-display`);
            const metadata = display.querySelector( '.comment-metadata');
            const editDiv = document.createElement('div');
            editDiv.innerHTML = `
            <small>(edited)</small>
        `;
            display.insertBefore(editDiv,metadata);
        }

    });
    cancelButton.addEventListener('click', () => {
        inputField.parentNode.replaceChild(commentTextElement, inputField);
        container.parentNode.removeChild(container);
        interactionsDivParent.insertBefore(interactionsDiv, interactionsDivNextSibling);
    });

    commentElement.appendChild(container);
}
const deleteComment = async (commentId) => {
    const commentContainer = document.querySelector('.comment-section');
    const commentElement = document.querySelector(`#comment-${commentId}`);
    await deleteCommentBK(commentId);

    commentContainer.removeChild(commentElement);

}

const editCommentBk = async (commentId,text) => {

    try{
        const response = await fetch(`/comment/edit?commentId=${commentId}`,{
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "id" : commentId,
                "text" : text
            })
        });

        if(!response.ok){
            throw new Error('Failed to update the comment');
        }
    }catch (error){
        console.log('Error editing comment:'.error);
    }
}

const deleteCommentBK = async (commentId) => {

    try {
        const response = await fetch(`/comment/delete?commentId=${commentId}`, {
            method: 'DELETE',
        });

        if (!response.ok) {
            throw new Error('Failed to delete comment');
        }
    } catch (error) {
        console.error('Error deleting comment:', error);
    }
}
document.getElementById('buttonEvent').addEventListener('click',sendMessage);

