
import { fetchLike } from './giveLike.js';
import { renderVideoContent,renderContent } from './renderVideo.js';
import { renderPhotos } from './renderPhoto.js';
import { renderInteractionIcons } from './renderIcon.js';
import { smoothScrollTo } from './utils.js';

let currentPost = null;
let stompClient = null;
let mentionUserName = null;
let mentionUserList =  [];

document.addEventListener('DOMContentLoaded', async () => {
    await fetchSinglePost();
    setupEventListeners();
    const mainCommentInput = document.getElementById('messageInput');
    userMention(mainCommentInput,false);
    // userMentionForEditComment();
    const commentId = localStorage.getItem('commentId');
    const rootCommentId = localStorage.getItem('rootCommentId');
    if (commentId) {
        await scrollToComment(commentId, rootCommentId);
        localStorage.removeItem('commentId');
        localStorage.removeItem('rootCommentId');
    }
});

function setupEventListeners() {
    const container = document.querySelector('.tweets-container');
    container.addEventListener('click', handlePostInteractions);

    const commentsContainer = document.querySelector('.comment-section');
    commentsContainer.addEventListener('click', handleCommentInteractions);
}
const scrollToComment = async (commentId, rootCommentId = null) => {
    let page = 0;
    let found = false;

    while (!found) {
        const response = await fetch(`/post/comments?postId=${postId}&page=${page}&size=${pageSize}`);
        const data = await response.json();

        if (data && data.content) {
            data.content.forEach(comment => {
                if (comment.id === parseInt(rootCommentId || commentId)) {
                    const commentElement = document.getElementById(`comment-${comment.id}`);
                    if (commentElement) {
                        smoothScrollTo(commentElement, 1000); // Smooth scroll to the comment over 1 second
                        setTimeout(() => {
                            commentElement.classList.add('highlight-comment');
                            setTimeout(() => {
                                commentElement.classList.remove('highlight-comment');
                            }, 1500); // Remove highlight after 1.5 seconds
                        }, 1100); // Apply animation class after scroll completes

                        if (rootCommentId) {
                            expandChildComments(comment.id, commentId);
                        }

                        found = true;
                    }
                }
            });
            page++;
        } else {
            break;
        }
    }
};
const expandChildComments = async (rootCommentId, childCommentId) => {
    const rootCommentElement = document.getElementById(`comment-${rootCommentId}`);
    if (rootCommentElement) {
        const loadCommentsButton = rootCommentElement.querySelector('.loadComment');
        if (loadCommentsButton) {
            loadCommentsButton.click(); // Simulate clicking "See More" to load child comments
            setTimeout(() => {
                const childCommentElement = document.getElementById(`comment-${childCommentId}`);
                if (childCommentElement) {
                    childCommentElement.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });
                    childCommentElement.classList.add('highlight-comment');
                    setTimeout(() => {
                        childCommentElement.classList.remove('highlight-comment');
                    }, 1500); // Remove highlight after 1.5 seconds
                }
            }, 1000); // Wait for the child comments to load
        }
    }
};
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
    const commentId = target.id.split('-')[1];

    if (target.matches('[id^="commentLike-"]')){
        likeComment(commentId,target);
    }

    if (target.classList.contains('reply')) {
        const parentId = target.closest('.comment').id.split('-')[1];
        const parentCommentElement = document.getElementById(`comment-${parentId}`);
        let inputContainer = parentCommentElement.querySelector('.input-container');

        if (inputContainer.style.display === 'none' || !inputContainer.style.display) {
            inputContainer.style.display = 'block';
            const inputField = inputContainer.querySelector('.input-field');
            userMention(inputField,false);
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
function sendMessage() {
    let messageInput = document.getElementById('messageInput');
    let messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {

        if (messageInput.classList.contains('mention-selected')) {
            console.log(mentionUserList);
        }

        let chatMessage = {
            postId: currentPost,
            text: messageContent,
            mention: mentionUserList
        };
        stompClient.send(`/app/comment/${currentPost}`, {}, JSON.stringify(chatMessage));

        messageInput.value = '';
        messageInput.classList.remove('mention-selected');
        mentionUserList = [];
    }
}

function sendReplyComment(target) {
    const inputField = target.closest('.input-container').querySelector('.input-field');
    const messageContent = inputField.value.trim();
    const commentId = target.closest('.comment').id.split('-')[1];
    if (messageContent && stompClient) {

        if (inputField.classList.contains('mention-selected')) {
            console.log(mentionUserList);
        }
        let chatMessage = {
            parentCommentId: commentId,
            postId: currentPost,
            text: messageContent,
            mention: mentionUserList
        };
        stompClient.send(`/app/comment/${currentPost}`, {}, JSON.stringify(chatMessage));

        inputField.value = '';
        inputField.classList.remove('mention-selected');
        target.closest('.input-container').style.display = 'none';
        mentionUserList = [];
    }
}

function extractMention(message) {
    const mentionRegex = /@(\w+)/g;
    const mentions = message.match(mentionRegex);
    return mentions ? mentions[0].substring(1) : null;
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
    const videoContent = data.content.videos.length > 0 ? renderContent(data) : '';
    const photoContent = data.content.images.length > 0 ? renderPhotos(data.content.images,data.id) : '';

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

    requestAnimationFrame(() => {
        divContainer.classList.add('fade-in');
        attachEventListenersToPost(divContainer, postId);
    });
}
const saveBookMark = async (postId,element) => {
    try {
        const response = await fetch('/bookmark/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                postId: postId
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        const isBookmarked = data;

        if (isBookmarked) {
            element.classList.replace('fa-regular', 'fa-solid');
            element.setAttribute('data-liked', 'true');
        } else {
            element.classList.replace('fa-solid', 'fa-regular');
            element.setAttribute('data-liked', 'false');
        }

    } catch (error) {
        console.error('Error occurred while saving bookmark:', error);
    }
}
const attachEventListenersToPost = (postElement,postId) => {
    const images = postElement.querySelectorAll('.img-fluid');
    images.forEach((image, index) => {
        image.addEventListener('click', () => {
            openLightbox(postId, index);
        });
    });

    document.addEventListener('click', function (event) {
        if (event.target && event.target.classList.contains('close-lightbox')) {
            const postId = event.target.getAttribute('data-post-id');
            closeLightbox(postId);
        }
    });
};
const openLightbox = (postId, index) => {
    const lightbox = document.getElementById(`lightbox-${postId}`);
    if (lightbox) {
        lightbox.style.display = 'flex';
        const carousel = new bootstrap.Carousel(lightbox, {
            interval: false,
        });
        carousel.to(index);
    } else {
        console.error(`Lightbox with ID lightbox-${postId} not found`);
    }
};

const closeLightbox = (postId) => {
    const lightbox = document.getElementById(`lightbox-${postId}`);
    if (lightbox) {
        lightbox.style.display = 'none';
    } else {
        console.error(`Lightbox with ID lightbox-${postId} not found`);
    }
};
const openWebSocket = (postId) => {
    currentPost = postId;
    connectCommentSession(postId);
}

const connectCommentSession = (postId) => {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        subscribeToComments(postId);
        subscribeToNotifications();
    },function(error) {
        console.log('WebSocket Connection Error:', error);
        // Optionally, implement a retry mechanism
    });
}
const subscribeToComments = (postId) => {
    stompClient.subscribe('/comment/messages/' + postId, function(messageOutput) {
        processCommentMessage(JSON.parse(messageOutput.body));
    }, function(error) {
        console.log('Subscription error: ', error);
        fetchAllComment(postId);
    });
}
const subscribeToNotifications = () => {
    if (stompClient && stompClient.connected) {
        stompClient.subscribe('/user/queue/notifications', function(notification) {
            console.log("Notification received:", notification);
            displayToastNotification(notification);
        });
    }
}

function processCommentMessage(message) {
    if (message) {
        showMessageOutput(message);
    } else {
        console.log('No message received, fetching comments as fallback');
        fetchAllComment(currentPost);
    }
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
            comment.querySelector('.comment-text').textContent = `${messageOutput.text}`;
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

        // Increment the total comment count by 1
        const commentCountElement = document.querySelector('.fa-comment');
        const currentValue = parseInt(commentCountElement.textContent);
        if (!isNaN(currentValue)) {
            commentCountElement.textContent = currentValue + 1; // Increment by 1
        }
    } else {
        if (!firstChild) {
            commentContainer.appendChild(comment);
            console.log('no comment');
        } else {
            console.log('comment here')
            commentContainer.insertBefore(comment, firstChild);
        }

        // Increment the total comment count by 1
        const commentCountElement = document.querySelector('.fa-comment');
        const currentValue = parseInt(commentCountElement.textContent);
        if (!isNaN(currentValue)) {
            commentCountElement.textContent = currentValue + 1; // Increment by 1
        }
    }
}


let currentPage = 0;
const pageSize = 10;

const fetchAllComment = async (postId, page = 0, size = 10) => {
    try {
        const response = await fetch(`/post/comments?postId=${postId}&page=${page}&size=${size}`);
        const data = await response.json();
        console.log('Data received:', data);

        if (data && data.content) {
            let commentContainer = document.querySelector('.comment-section');

            data.content.forEach(comment => {
                const commentDiv = createCommentElement(comment, false);
                commentContainer.appendChild(commentDiv);
            });
            // Handle the visibility of the "See More" button
            const seeMoreButton = document.querySelector('#seeMoreButton');
            const showLessButton = document.querySelector('#showLessButton');

            if (data.number < data.totalPages - 1) { // If there are more pages
                seeMoreButton.style.display = 'block';
            } else {
                seeMoreButton.style.display = 'none';
            }
            if (page > 0) {
                showLessButton.style.display = 'block';
            } else {
                showLessButton.style.display = 'none';
            }
        }
    } catch (error) {
        console.error('Failed to fetch comments:', error);
    }
};
const showLessComments = () => {
    const commentContainer = document.querySelector('.comment-section');
    commentContainer.innerHTML = '';
    currentPage = 0;
    fetchAllComment(postId, currentPage, pageSize);

    document.querySelector('#showLessButton').style.display = 'none';
    document.querySelector('#seeMoreButton').style.display = 'block';
    const commentsContainer = document.getElementById('commentsContainer');
    commentsContainer.scrollIntoView({ behavior: 'smooth' });
};
const seeMoreComment = document.getElementById('seeMoreButton');
seeMoreComment.addEventListener('click',() => loadMoreComments())

const seeLessComment = document.getElementById('showLessButton');
seeLessComment.addEventListener('click',() => showLessComments())

const loadMoreComments = () => {
    currentPage += 1;
    fetchAllComment(postId, currentPage, pageSize);

    document.querySelector('#showLessButton').style.display = 'block';
};

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
    const heartClass = commentData.commentLiked ? 'fa-solid' : 'fa-regular';
    const heartColor = commentData.commentLiked ? 'color: #c91818;' : '';
    const dataLiked = commentData.commentLiked ? 'true' : 'false';
    const likeCount = commentData.totalLike || '';

    const commentElement = document.createElement('div');
    commentElement.id = `comment-${commentData.id}`;
    commentElement.classList.add('comment');
    if (isReply) {
        commentElement.classList.add('nested-comment');
    }

    let displayText = '';
    if (commentData.mentionUserList && commentData.mentionUserList.length > 0) {
        commentData.mentionUserList.forEach((mention, index) => {
            const mentionTag = `<a class="link-profile" href="/userprofile/${mention.userId}"><span class="mention">@${mention.userName}</span></a>`;
            displayText += mentionTag;

            if (index !== commentData.mentionUserList.length - 1) {
                displayText += ', ';
            }
        });
    }
    const textEditor = `<span id="commentText">${commentData.text}</span>`;
    displayText += ' ' + textEditor;

    let replyUser;
    if ((commentData.rootCommentId && commentData.parentCommentId) || (commentData.rootCommentId === commentData.parentCommentId && commentData.rootCommentId !== null)) {
        if(!commentData.replyToIsOwnComment){
            replyUser = `Reply to @${commentData.parentUserName}`;
        }
    }

    commentElement.innerHTML = `
        <img src="${commentData.userImage}" alt="User Avatar" class="user-avatar">
        <div class="comment-content">
            <div class="user-display">
                <div class="comment-author" style="margin-right: 5px;">
                    ${commentData.userName}
                    ${commentData.edited ? '<small style="margin-right: 15px">(edited)</small>' : ''}
                    ${replyUser ? `<small style="font-size: 11px; font-weight: 700; margin-left: 5px;">${replyUser}</small>` : ''}
                </div>
                <div class="comment-metadata" style="margin: 5px">${commentData.time}</div>
            </div>
            <div class="comment-text">${displayText}</div>
            <div class="comment-interactions d-flex justify-content-between">
                <div>
                    <i id="commentLike-${commentData.id}" class="${heartClass} fa-heart" style="cursor: pointer; ${heartColor};" data-liked="${dataLiked}">${likeCount}</i>
                    <i class="fa-solid fa-reply reply" style="cursor: pointer; color: black"></i>
                    ${commentData.owner ? `
                        <div style="display: inline-block;">
                            <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" style="color: black" role="button" data-bs-toggle="dropdown" aria-expanded="false"></i>
                            <ul class="dropdown-menu">
                                <div class="d-flex justify-content-between" style="padding: 5px">
                                    <li class="commentBtn" id="commentEdit">Edit</li>
                                    <li class="commentBtn" id="commentDelete">Delete</li>
                                </div>
                            </ul>
                        </div>` : ''}
                </div>
                <div id="seeMoreContainer">
                    ${!commentData.parentCommentId && commentData.childComment > 0 ? `<small class="loadComment" style="cursor: pointer;">See More ${commentData.childComment}</small>` : ''}
                </div>
            </div>
            <div class="input-container" style="display: none;">
                <input type="text" id="messageInputs" class="input-field editInput" placeholder="Type a message here">
                <button tabindex="1" class="send-button messageBtn">Send</button>
            </div>
            <div class="userMention" style="display: block;">
                <div id="suggestions" style="display: none;"></div>
                <img id="mentionProfile" style="width: 50px; height: 50px; display: none;">
            </div>
        </div>
    `;

    if (commentData.owner) {
        const editButton = commentElement.querySelector('#commentEdit');
        editButton.addEventListener('click', () => editComment(commentData.id, commentData.text, commentData.edited, commentData.mentionUserList));

        const deleteButton = commentElement.querySelector('#commentDelete');
        deleteButton.addEventListener('click', () => deleteComment(commentData.id));
    }

    return commentElement;
}
let userStaffList = [];

const editComment = (commentId, text, status, mentionList) => {
    const commentElement = document.querySelector(`#comment-${commentId}`);
    const commentTextElement = document.querySelector(`#comment-${commentId} .comment-text`);
    const interactionsDiv = document.querySelector(`#comment-${commentId} .comment-interactions`);

    const interactionsDivParent = interactionsDiv.parentNode;
    const interactionsDivNextSibling = interactionsDiv.nextSibling;

    interactionsDivParent.removeChild(interactionsDiv);

    userStaffList = mentionList.map(mention => mention.userStaffId);

    const inputField = document.createElement('input');
    inputField.setAttribute('type', 'text');
    inputField.classList.add('editInput');
    inputField.value = text;

    const editContainer = document.createElement('div');
    editContainer.classList.add('edit-container');

    editContainer.appendChild(inputField);


    mentionList.forEach(mention => {
        const mentionSpan = document.createElement('span');
        mentionSpan.classList.add('mention-item');
        mentionSpan.id = `${mention.userStaffId}`;

        const mentionName = document.createElement('span');
        mentionName.classList.add('mention-name');
        mentionName.textContent = `@${mention.userName}`;

        const removeButton = document.createElement('button');
        removeButton.textContent = 'x';
        removeButton.classList.add('remove-mention');
        removeButton.addEventListener('click', () => {
            editContainer.removeChild(mentionSpan);
            mentionList = mentionList.filter(user => user.userName !== mention.userName);
            userStaffList = mentionList.map(mention => mention.userStaffId);
            console.log(userStaffList);
        });

        mentionSpan.appendChild(mentionName);
        mentionSpan.appendChild(removeButton);
        editContainer.appendChild(mentionSpan);
    });

    commentTextElement.parentNode.replaceChild(editContainer, commentTextElement);

    const btnContainer = document.createElement('div');
    btnContainer.classList.add('btn-container');
    const saveButton = document.createElement('button');
    saveButton.textContent = 'Save';
    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Cancel';

    btnContainer.appendChild(saveButton);
    btnContainer.appendChild(cancelButton);

    saveButton.addEventListener('click', async () => {
        const newText = inputField.value;

        try {
            const response = await editCommentBk(commentId, newText, userStaffList);

            if (response.ok) {
                const data = await response.json();
                const updatedText = data.text;
                // const updatedMentions = data.mentions.map(m => `@${m.userName}`).join(' ');

                // Update the comment element
                const updatedCommentData = {
                    ...data,
                    text: updatedText,
                    mentionUserList: data.mentionUserList
                };

                const newCommentElement = createCommentElement(updatedCommentData, false);
                commentElement.parentNode.replaceChild(newCommentElement, commentElement);
            } else {
                throw new Error('Failed to update the comment');
            }
        } catch (error) {
            console.error('Error editing comment:', error);
        }
    });

    cancelButton.addEventListener('click', () => {
        editContainer.parentNode.replaceChild(commentTextElement, editContainer);
        btnContainer.parentNode.removeChild(btnContainer);
        interactionsDivParent.insertBefore(interactionsDiv, interactionsDivNextSibling);
    });

    commentElement.appendChild(btnContainer);
    userMention(inputField,true)
};
const editCommentBk = async (commentId,text,userList) => {

    try{
        const response = await fetch(`/comment/edit?commentId=${commentId}`,{
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                "id" : commentId,
                "text" : text,
                "mention" : userList
            })
        });

        if(!response.ok){
            throw new Error('Failed to update the comment');
        }
        return response;
    }catch (error){
        console.log('Error editing comment:'.error);
    }
}
const deleteComment = async (commentId) => {
    const commentContainer = document.querySelector('.comment-section');
    const commentElement = document.querySelector(`#comment-${commentId}`);
    await deleteCommentBK(commentId);

    commentContainer.removeChild(commentElement);

    const commentCountElement = document.querySelector('.fa-comment');
    const currentValue = parseInt(commentCountElement.textContent);
    if (!isNaN(currentValue) && currentValue > 0) {
        commentCountElement.textContent = currentValue - 1;
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
        userStaffList = [];
    } catch (error) {
        console.error('Error deleting comment:', error);
    }
}
document.getElementById('buttonEvent').addEventListener('click',sendMessage);
const likeComment = async (commentId,element) => {

    try {
        const response = await fetch('/comment/like',{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                commentId : commentId,
            })
        });
        if (!response.ok) {
            throw new Error('Failed to like the post');
        }
        const data = await response.json();
        let likeCount = parseInt(element.textContent) || 0;

        if (data.liked) {
            element.classList.replace('fa-regular', 'fa-solid');
            element.style.color = '#c91818';
            element.setAttribute('data-liked', 'true');
            likeCount++;
        } else {
            element.classList.replace('fa-solid', 'fa-regular');
            element.style.color = '';
            element.setAttribute('data-liked', 'false');
            likeCount--;
        }
        element.textContent = likeCount.toString();
    } catch (error) {
        console.error('Error liking the post:', error);
    }
}
const userMention = (inputElement,status) => {
    const suggestions = document.getElementById('suggestions');
    const mentionProfile = document.getElementById('mentionProfile');
    // const closeButton = suggestions.querySelector('.close-button');
    //
    // closeButton.addEventListener('click', () => {
    //     suggestions.style.display = 'none';
    // });

    if (inputElement) {
        inputElement.addEventListener('input', (e) => {
            const inputVal = e.target.value;
            const lastAtPos = inputVal.lastIndexOf('@');
            const search = inputVal.substring(lastAtPos + 1);

            if (lastAtPos === -1 || search.length === 0) {
                suggestions.innerHTML = '';
                suggestions.style.display = 'none';
                mentionProfile.style.display = 'none';
                return;
            }

            if (lastAtPos !== -1 && search.length > 1) {
                fetch(`/user/search?query=${search}`)
                    .then(response => response.json())
                    .then(data => {
                        suggestions.innerHTML = '';

                        if (data.length > 0) {
                            data.forEach(user => {
                                const div = document.createElement('div');
                                div.style.display = 'flex';
                                div.style.alignItems = 'center';
                                div.style.cursor = 'pointer';
                                div.style.marginBottom = '10px';
                                div.id = user.staffId;
                                const userImage = document.createElement('img');
                                userImage.src = user.photo;
                                userImage.alt = user.name;
                                userImage.style.width = '30px';
                                userImage.style.height = '30px';
                                userImage.style.marginRight = '10px';
                                userImage.style.borderRadius = '50%';

                                const userName = document.createTextNode(user.name);

                                div.appendChild(userImage);
                                div.appendChild(userName);
                                div.onclick = () => {
                                    if(status){
                                        const editContainer = document.querySelector('.edit-container');

                                        const mentionSpan = document.createElement('span');
                                        mentionSpan.classList.add('mention-item');
                                        mentionSpan.id = `${user.staffId}`;

                                        const mentionName = document.createElement('span');
                                        mentionName.classList.add('mention-name');
                                        mentionName.textContent = `@${user.name}`;

                                        const removeButton = document.createElement('button');
                                        removeButton.textContent = 'x';
                                        removeButton.classList.add('remove-mention');
                                        removeButton.addEventListener('click', () => {
                                            editContainer.removeChild(mentionSpan);
                                            userStaffList = userStaffList.filter(id => id !== user.staffId);
                                            console.log(userStaffList);
                                        });
                                        userStaffList.push(user.staffId);
                                        mentionSpan.appendChild(mentionName);
                                        mentionSpan.appendChild(removeButton);
                                        editContainer.appendChild(mentionSpan);
                                        suggestions.style.display = 'none';
                                        mentionProfile.style.display = 'none';

                                    }else{
                                        inputElement.value = inputVal.substring(0, lastAtPos) + `@${user.name} `;
                                        inputElement.classList.add('mention-selected');
                                        mentionUserName = user.name;
                                        mentionUserList.push(user.staffId);
                                        suggestions.innerHTML = '';
                                        suggestions.style.display = 'none';
                                        mentionProfile.style.display = 'none';
                                        inputElement.focus();
                                    }
                                };
                                suggestions.appendChild(div);
                            });
                            suggestions.style.display = 'block';
                        } else {
                            suggestions.style.display = 'none';
                            mentionProfile.style.display = 'none';
                        }
                    })
                    .catch(error => {
                        console.error('Fetch error:', error);
                        suggestions.innerHTML = '';
                        suggestions.style.display = 'none';
                        mentionProfile.style.display = 'none';
                    });
            } else {
                suggestions.innerHTML = '';
                suggestions.style.display = 'none';
                mentionProfile.style.display = 'none';
            }
        });
    } else {
        console.error('Input element was not found.');
    }
};

const displayToastNotification = (notification) => {

    let toastBody = document.querySelector('#liveToast .toast-body');
    toastBody.textContent = notification.body;

    const toastEl = document.getElementById('liveToast');
    let toast = new bootstrap.Toast(toastEl);
    toast.show();
}



