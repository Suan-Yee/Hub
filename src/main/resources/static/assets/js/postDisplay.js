document.addEventListener('DOMContentLoaded',() => {
    showAllPosts();
    showAllTopic();
})
let currentPost = null;
function formatDate(dateArray) {
    const [year, month, day, hour, minute, second] = dateArray;
    const date = new Date(year, month - 1, day, hour, minute, second);
    return date.toLocaleString();
}

const showAllTopic = async  () => {
    const response = await fetch("/topic/all");
    const data = await response.json();

    const topic_container = document.querySelector('#trends-topic');

    data.forEach(topic => {
        const topicLi = document.createElement('li');
        topicLi.classList.add('nav-list');
        topicLi.innerHTML = `
        <div class="trend-list">
        <p class="main-text">${topic.name}</p>
        <p class="sub-text">fake</p>
        </div>
        `;
        topic_container.appendChild(topicLi);
    })
}
const showAllPosts = async () => {
    const response = await fetch("/post/all");
    const data = await response.json();

    const tweets = document.querySelector('.tweets-container');

    data.forEach(post => {
        const postDiv = document.createElement('div');
        postDiv.classList.add('tweets');
        postDiv.id = `${post.id}`;
        postDiv.innerHTML = `
            <div class="user-pics"><img src="/static/assets/img/user3.jpg" alt="user3"></div>
        <div class="user-content-box">
            <div class="user-names">
                <hi class="full-name">${post.user.name}</hi>

                <p class="time">${formatDate(post.createdAt)}</p>
            </div>

            <div class="user-content">
                <p>${post.content.text}</p>
                <img src="/static/assets/img/content5.jpg" alt="content5">
            </div>

            <div class="content-icons">
                <i onclick="openWebSocket(${post.id})" class="far fa-comment blue"> 109</i>
                <i class="far fa-heart red">1.6k</i>
                <i class="fas fa-chevron-up blue"></i>
            </div>
        </div>
            `;
        tweets.appendChild(postDiv);
    })
}
const openWebSocket = (postId) => {
    currentPost = postId;
    connectCommentSession(postId);
    const commentDialog = document.getElementById('commentDialog');
    commentDialog.showModal();
    const closeDialogBtn = document.getElementById('closeDialogBtn');
    closeDialogBtn.addEventListener('click', () => {
        commentDialog.close();
});
}

let stompClient = null;
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
    console.log("Received message:", messageOutput);
    let chatContainer = document.getElementById('chat-container');
    let messageElement = document.createElement('div');
    messageElement.classList.add('message');

    var fromMe = messageOutput.userId === 1;
    if (fromMe) {
        messageElement.classList.add('from-me');
    } else {
        messageElement.classList.add('from-them');
    }

    messageElement.innerText = messageOutput.text;
    chatContainer.appendChild(messageElement);
    chatContainer.scrollTop = chatContainer.scrollHeight;
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




// function showAllPosts(posts) {
//     const tweets = document.querySelector('.tweets-container');
//     tweets.innerHTML = ''; // Clear existing posts
//
//     posts.forEach(post => {
//         const postDiv = document.createElement('div');
//         postDiv.classList.add('tweets');
//
//         postDiv.innerHTML = `
//             <div class="user-pics"><img src="/static/assets/img/user3.jpg" alt="user3"></div>
//             <div class="user-content-box">
//                 <div class="user-names">
//                     <h1 class="full-name">${post.user.name}</h1>
//                     <p class="time"></p>
//                 </div>
//                 <div class="user-content">
//                     <p>${post.content.text}</p>
//                     <img src="/static/assets/img/content5.jpg" alt="content5">
//                 </div>
//                 <div class="content-icons">
//                     <i class="far fa-comment blue"> 109</i>
//                     <i class="far fa-heart red">1.6k</i>
//                     <i class="fas fa-chevron-up blue"></i>
//                 </div>
//             </div>`;
//         tweets.appendChild(postDiv);
//     });
// }
// let stompClient = null;
//
// function connect(){
//
//     const socket = new SockJS('/ws');
//     stompClient = Stomp.over(socket);
//
//     stompClient.connect({}, function(frame) {
//         console.log('Connected: ' + frame);
//         stompClient.subscribe('/app/all' , function(message) {
//             console.log("Hello dffsdf")
//             showAllPosts(JSON.parse(message.body));
//             console.log("Message" + message.body)
//         });
//     });
// }
