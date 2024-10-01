let stompClient = null;
let id = null;
let username=null;
let selectedRoomId = null;
let userImage = null;

let currentPage = 0;
const pageSize = 10;
let isFetching = false;
let mediaRecorder;
let audioChunks = [];

document.addEventListener('DOMContentLoaded', async () => {
    const currentUrl = window.location.href;
    let urlParts = currentUrl.split('/');
    selectedRoomId = urlParts[urlParts.length - 1];

    try {
        const userData = await currentUser();
        id = userData.id;
        username = userData.name;
        userImage = userData.photo;
        connectWebSocket();
        changeGroupChatHeader(selectedRoomId);

        document.getElementById('groupChatSend').addEventListener('click', sendMessage, true);
        document.querySelector('.emoji-button').addEventListener('click', fetchAndDisplayEmojis, true);
        document.querySelector('.audioButton').addEventListener('click', toggleRecording, true);

        document.getElementById('imageUploadIcon').addEventListener('click', () => {
            document.getElementById('imageUpload').click();
        });

        document.getElementById('imageUploadIcon').addEventListener('click', () => {
            document.getElementById('imageUpload').click();
        });

        document.getElementById('imageUpload').addEventListener('change', handleImageUpload);

        document.querySelector('.chat-container').addEventListener('click', (event) => {
            if (event.target.classList.contains('editButton')) {
                const messageElement = event.target.closest('.message');
                const messageId = messageElement.dataset.messageId;
                const currentContent = messageElement.querySelector('.text').textContent;
                handleEditMessage(messageId, currentContent);
            }
            if (event.target.classList.contains('deleteButton')) {
                handleDeleteMessage(event.target.closest('.message').dataset.messageId);
            }
        });

        if (event.target.classList.contains('deleteButton')) {
            const messageElement = event.target.closest('.message');
            const messageId = messageElement.dataset.messageId;
            handleDeleteMessage(messageId);
        }

    } catch (error) {
        console.error('Error fetching user data:', error);
    }

    await fetchAndDisplayUserChat(true);


    document.getElementById('vd-icon').addEventListener('click', handleMeeting);
});
function handleEditMessage(messageId, currentContent) {
    // Set the current message content in the modal input field
    document.getElementById('editMessageInput').value = currentContent;

    // Store the messageId in the modal for later use
    document.getElementById('editMessageModal').setAttribute('data-message-id', messageId);

    // Open the modal
    var editMessageModal = new bootstrap.Modal(document.getElementById('editMessageModal'));
    editMessageModal.show();
}
document.getElementById('saveEditMessage').addEventListener('click', function() {
    // Get the new content from the modal input
    const newContent = document.getElementById('editMessageInput').value;
    const messageId = document.getElementById('editMessageModal').getAttribute('data-message-id');

    if (newContent) {
        const editMessage = {
            messageId: messageId,
            content: newContent,
            roomId: selectedRoomId,
            senderId: id,
            type: 'text'
        };
        stompClient.send("/app/edit-message", {}, JSON.stringify(editMessage));
    }

    // Close the modal
    var editMessageModal = bootstrap.Modal.getInstance(document.getElementById('editMessageModal'));
    editMessageModal.hide();
});

function handleDeleteMessage(messageId) {
    document.getElementById('deleteMessageModal').setAttribute('data-message-id', messageId);
    var deleteMessageModal = new bootstrap.Modal(document.getElementById('deleteMessageModal'));
    deleteMessageModal.show();
}

document.getElementById('confirmDeleteMessage').addEventListener('click', function() {
    const messageId = document.getElementById('deleteMessageModal').getAttribute('data-message-id');

    if (messageId) {
        const deleteMessage = {
            messageId: messageId,
            roomId: selectedRoomId
        };
        stompClient.send("/app/delete-message", {}, JSON.stringify(deleteMessage));
    }

    var deleteMessageModal = bootstrap.Modal.getInstance(document.getElementById('deleteMessageModal'));
    deleteMessageModal.hide();
});
function updateMessageInUI(message) {
    const messageElement = document.querySelector(`[data-message-id="${message.messageId}"] .text`);
    messageElement.textContent = message.content;
}

function removeMessageFromUI(messageId) {
    const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
    if (messageElement) {
        messageElement.remove();
    }
}
async function fetchAndDisplayEmojis() {
    try {
        const response = await fetch('https://emoji-api.com/emojis?access_key=e40cfdf86d4bada254a679a06cf4ef2bfb9efda9');
        const emojis = await response.json();

        const popup = document.createElement('div');
        popup.classList.add('emoji-popup');

        const closeButton = document.createElement('button');
        closeButton.innerText = 'X';
        closeButton.classList.add('close-button');
        closeButton.addEventListener('click', () => {
            popup.remove();
        });

        const searchInput = document.createElement('input');
        searchInput.type = 'text';
        searchInput.placeholder = 'Search...';
        searchInput.classList.add('search-input');

        searchInput.addEventListener('input', () => {
            const searchTerm = searchInput.value.toLowerCase();
            const filteredEmojis = emojis.filter(emoji => emoji.unicodeName.toLowerCase().includes(searchTerm));
            displayEmojis(filteredEmojis, emojiContainer);
        });

        const emojiContainer = document.createElement('div');
        emojiContainer.classList.add('emoji-container');

        popup.appendChild(closeButton);
        popup.appendChild(searchInput);
        popup.appendChild(emojiContainer);

        document.body.appendChild(popup);

        displayEmojis(emojis, emojiContainer);
    } catch (error) {
        console.error('Error fetching emojis:', error);
    }
}

function displayEmojis(emojis, container) {
    container.innerHTML = '';
    emojis.forEach(emoji => {
        const emojiButton = document.createElement('button');
        emojiButton.classList.add('emoji');
        emojiButton.innerText = emoji.character;
        emojiButton.addEventListener('click', () => {
            const messageInput = document.getElementById('message');
            messageInput.value += emoji.character;
        });
        container.appendChild(emojiButton);
    });
}
const connectWebSocket = () => {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected);
};
const onConnected = () => {
    console.log('WebSocket connected');
    stompClient.subscribe(`/topic/group-messages/${selectedRoomId}`, onMessageReceived);

    stompClient.subscribe(`/topic/group-messages/edit/${selectedRoomId}`, (payload) => {
        const message = JSON.parse(payload.body);
        console.log('Received edit payload');
        updateMessageInUI(message);
    });
    stompClient.subscribe(`/topic/group-messages/delete/${selectedRoomId}`, (payload) => {
        const messageId = payload.body;
        removeMessageFromUI(messageId);
    });
};
async function onMessageReceived(payload) {
    try {
        const message = JSON.parse(payload.body);
        const imageUrl = message.userImage ? message.userImage :'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';

        if (message.type === 'voice') {
            displayVoiceMessage(message.senderId, message.content, message.time, message.name, imageUrl,message.messageId);
        } else if (message.type === 'image') {
            displayImageMessage(message.senderId, message.content, message.time, message.name, imageUrl,message.messageId);
        } else if (message.type === 'video') {
            displayVideoMessage(message.senderId, message.content, message.time, message.name, imageUrl,message.messageId);
        } else {
            displayMessage(message.senderId, message.content, message.time, message.name, imageUrl,message.messageId);
        }
        scrollToBottom();
    } catch (error) {
        console.error('Error parsing or processing the message:', error);
    }
}

function displayVideoMessage(senderId, videoUrl, time, name, userImage, messageId, prepend = false) {
    const messageBox = document.querySelector('.chat-container');

    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    messageContainer.dataset.messageId = messageId; // Add message ID for identification

    const videoElement = document.createElement('video');
    videoElement.controls = true;
    videoElement.src = videoUrl;

    if (senderId === id) {
        messageContainer.classList.add('sender');
        messageContainer.innerHTML = `
            <div class="message-info"><span class="groupUserName">${name}:</span>${time}</div>
            <div class="message-content">
                <div class="text"></div>
                <img class="userProfile" src="${userImage}" alt="User">
             
            </div>
        `;
    } else {
        messageContainer.innerHTML = `
            <div class="message-info"><span class="groupUserName">${name}:</span>${time}</div>
            <div class="message-content">
                <img class="userProfile" src="${userImage}" alt="User">
                <div class="text"></div>
            </div>
        `;
    }
    messageContainer.querySelector('.text').appendChild(videoElement);

    if (prepend) {
        messageBox.prepend(messageContainer);
    } else {
        messageBox.appendChild(messageContainer);
    }
}

function displayMessage(senderId, content, time, name, userImage, messageId, prepend = false) {
    const messageBox = document.querySelector('.chat-container');

    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    messageContainer.dataset.messageId = messageId; // Add message ID for identification

    if (senderId === id) {
        messageContainer.classList.add('sender');
        messageContainer.innerHTML = `
            <div class="message-info"><span class="groupUserName">${name}:</span>${time}</div>
            <div class="message-content">
                <div class="buttonContainer" style="margin-right: 10px;"></div>
                <div class="text">${content}</div>
                <img class="userProfile" src="${userImage}" alt="User">
                <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" style="color: black" role="button" data-bs-toggle="dropdown" aria-expanded="false"></i>
                <ul class="dropdown-menu">
                                <div class="chatBtnContainer">
                                    <div class="editButton" id="editButton" style="padding-left: 20px;background: aliceblue">Edit<i class="fa-solid fa-pen-to-square" style="color: #4635dd"></i> </div>
                                    <div class="deleteButton" id="deleteButton" style="margin-left: 20px">Delete<i class="fa-solid fa-trash" style="color: #f00b0b"></i></div>
                                </div>
                            </ul>
            </div>
        `;
    } else {
        messageContainer.innerHTML = `
            <div class="message-info"><span class="groupUserName">${name}:</span>${time}</div>
            <div class="message-content">
                <img class="userProfile" src="${userImage}" alt="User">
                <div class="text">${content}</div>
            </div>
        `;
    }

    // Handle link transformation
    const linkText = "Join here:";
    const linkIndex = content.indexOf(linkText);
    const messageTextContainer = messageContainer.querySelector('.text');

    if (linkIndex !== -1) {
        const beforeLinkText = content.substring(0, linkIndex + linkText.length);
        const linkUrl = content.substring(linkIndex + linkText.length).trim();

        // Clear the original content
        messageTextContainer.innerHTML = '';

        // Create text nodes and anchor element
        const textBeforeLink = document.createTextNode(beforeLinkText);
        const anchor = document.createElement('a');
        anchor.href = linkUrl;
        anchor.style.color = '#1559be';
        anchor.textContent = linkUrl;

        // Append nodes in order
        messageTextContainer.appendChild(textBeforeLink);
        messageTextContainer.appendChild(anchor);
    } else {
        // If no specific link text is found, treat the whole content as a link
        if (content.startsWith('http://') || content.startsWith('https://')) {
            messageTextContainer.innerHTML = '';
            const anchor = document.createElement('a');
            anchor.href = content;
            anchor.textContent = content;
            messageTextContainer.appendChild(anchor);
        }
    }

    if (prepend) {
        messageBox.prepend(messageContainer);
    } else {
        messageBox.appendChild(messageContainer);
    }
}


function displayImageMessage(senderId, imageUrl, time, name, userImage, messageId, prepend = false) {
    const messageBox = document.querySelector('.chat-container');

    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    messageContainer.dataset.messageId = messageId; // Add message ID for identification

    if (senderId === id) {
        messageContainer.classList.add('sender');
        messageContainer.innerHTML = `
            <div class="message-info"><span class="groupUserName">${name}:</span>${time}</div>
            <div class="message-content">
                <div class="imageMessage"><img class="imageSend" src="${imageUrl}" alt="Image"></div>
                <img class="userProfile" src="${userImage}" alt="User">
                <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" style="color: black" role="button" data-bs-toggle="dropdown" aria-expanded="false"></i>
                <ul class="dropdown-menu">
                                <div class="chatBtnContainer">
                                    <div class="deleteButton" id="deleteButton" style="margin-left: 20px">Delete<i class="fa-solid fa-trash" style="color: #f00b0b"></i></div>
                                </div>
                            </ul>
            </div>
        `;
    } else {
        messageContainer.innerHTML = `
            <div class="message-info"><span class="groupUserName">${name}:</span>${time}</div>
            <div class="message-content">
                <img class="userProfile" src="${userImage}" alt="User">
                <div class="imageMessage"><img class="imageSend" src="${imageUrl}" alt="Image"></div>
            </div>
        `;
    }

    if (prepend) {
        messageBox.prepend(messageContainer);
    } else {
        messageBox.appendChild(messageContainer);
    }
}

function displayVoiceMessage(senderId, audioUrl, time, name, userImage, messageId, prepend = false) {
    const messageBox = document.querySelector('.chat-container');

    const messageContainer = document.createElement('div');
    messageContainer.classList.add('message');
    messageContainer.dataset.messageId = messageId; // Add message ID for identification

    if (senderId === id) {
        messageContainer.classList.add('sender');
    }

    const audioElement = document.createElement('audio');
    audioElement.controls = true;
    audioElement.src = audioUrl;
    audioElement.style.width = '250px';

    messageContainer.innerHTML = `
        <div class="message-info"><span class="groupUserName">${name}:</span>${time}</div>
        <div class="message-content">
            <img class="userProfile" src="${userImage}" alt="User">
            <div class="audio"></div>
            <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" style="color: black" role="button" data-bs-toggle="dropdown" aria-expanded="false"></i>
                <ul class="dropdown-menu">
                                <div class="chatBtnContainer">
                                    <div class="deleteButton" id="deleteButton" style="margin-left: 20px">Delete<i class="fa-solid fa-trash" style="color: #f00b0b"></i></div>
                                </div>
                            </ul>
        </div>
    `;
    messageContainer.querySelector('.audio').appendChild(audioElement);

    if (prepend) {
        messageBox.prepend(messageContainer);
    } else {
        messageBox.appendChild(messageContainer);
    }
}

function toggleRecording() {
    const recordingIndicator = document.getElementById('recordingIndicator');
    if (!mediaRecorder || mediaRecorder.state === "inactive") {
        navigator.mediaDevices.getUserMedia({ audio: true })
            .then(stream => {
                mediaRecorder = new MediaRecorder(stream);
                mediaRecorder.start();
                recordingIndicator.style.display = 'block';

                mediaRecorder.ondataavailable = function (e) {
                    audioChunks.push(e.data);
                };

                mediaRecorder.onstop = function () {
                    recordingIndicator.style.display = 'none';
                    const audioBlob = new Blob(audioChunks, { type: 'audio/ogg; codecs=opus' });
                    audioChunks = [];
                    const reader = new FileReader();

                    reader.onloadend = function () {
                        const base64String = reader.result.split(',')[1];
                        if (stompClient) {
                            const chatMessage = {
                                roomId: selectedRoomId,
                                senderId: id,
                                name: username,
                                content: base64String,
                                type: 'voice',
                                time: new Date().toISOString()
                            };
                            stompClient.send("/app/group-chat", {}, JSON.stringify(chatMessage));
                        }
                    };

                    reader.readAsDataURL(audioBlob);
                };
            })
            .catch(error => {
                console.error('Error accessing audio stream:', error);
            });
    } else if (mediaRecorder.state === "recording") {
        mediaRecorder.stop();
    }
}
async function handleImageUpload(event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onloadend = function () {
            const base64String = reader.result.split(',')[1];
            if (stompClient) {
                const chatMessage = {
                    roomId: selectedRoomId,
                    senderId: id,
                    name: username,
                    content: base64String,
                    type: 'image',
                    time: new Date().toISOString()
                };
                stompClient.send("/app/group-chat", {}, JSON.stringify(chatMessage));
            }
        };
        reader.readAsDataURL(file);
    }
}

async function fetchAndDisplayUserChat(initialLoad = false) {
    if (isFetching) return;
    isFetching = true;

    try {
        const response = await fetch(`/messages/${selectedRoomId}?page=${currentPage}&size=${pageSize}`);
        const userChat = await response.json();

        if (userChat.length > 0) {
            const messageBox = document.querySelector('.chat-container');

            if (initialLoad) {
                userChat.reverse().forEach(chat => {
                    let imageUrl = chat.userImage ? chat.userImage : 'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';
                    if (chat.type === 'voice') {
                        displayVoiceMessage(chat.senderId, chat.content, chat.time, chat.name, imageUrl,chat.messageId);
                    } else if (chat.type === 'image') {
                        displayImageMessage(chat.senderId, chat.content, chat.time, chat.name, imageUrl,chat.messageId);
                    } else {
                        displayMessage(chat.senderId, chat.content, chat.time, chat.name, imageUrl,chat.messageId);
                    }
                });
                const messageBox = document.querySelector('.chat-container');
                scrollToBottom();
            } else {
                const scrollPosition = messageBox.scrollHeight;

                userChat.forEach(chat => {
                    if (chat.type === 'voice') {
                        displayVoiceMessage(chat.senderId, chat.content, chat.time, chat.name, chat.userImage,chat.messageId, true);
                    } else if (chat.type === 'image') {
                        displayImageMessage(chat.senderId, chat.content, chat.time, chat.name, chat.userImage,chat.messageId, true);
                    } else {
                        displayMessage(chat.senderId, chat.content, chat.time, chat.name, chat.userImage,chat.messageId, true);
                    }
                });

                messageBox.scrollTop = messageBox.scrollHeight - scrollPosition;

            }
            currentPage++;
        }
    } catch (error) {
        console.error('Error fetching user chat:', error);
    } finally {
        isFetching = false;
    }
}


document.querySelector('.chat-container').addEventListener('scroll', async (event) => {
    if (event.target.scrollTop === 0) {
        await fetchAndDisplayUserChat();
    }
});

function sendMessage() {
    const messageInput = document.getElementById('message');
    const messageContent = messageInput.value.trim();

    console.log("Message" + messageContent);
    if (messageContent && stompClient) {
        const chatMessage = {
            roomId: selectedRoomId,
            senderId: id,
            name: username,
            content: messageInput.value.trim(),
            time: new Date()
        };

        console.log(chatMessage);
        stompClient.send("/app/group-chat", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}
async function currentUser() {
    const response = await fetch('/user/current');
    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    const data = await response.json();
    return data;
}

const changeGroupChatHeader = async (groupId) => {

    const response = await fetch(`/groupDetails/${groupId}`)
    const data = await response.json();

    if(response.ok){
        const header = document.querySelector('.chat-header');
        const groupImage = header.querySelector('.groupProfile');
        groupImage.src = data.image;

        const groupName = header.querySelector('.title');
        groupName.textContent = data.name;

        const memberCount = header.querySelector('.subtitle');
        memberCount.textContent = `${data.memberCount} members.`
    }
}


const openChat = document.querySelector('.openChat');
openChat.addEventListener('click', function () {
    const chatRoom = document.getElementById('chatRoom');

    document.querySelector('.groupPostContainer').style.width = '500px';
    document.querySelector('.topPostContainer').style.width = '500px';
    document.getElementById('mainContent').style.marginLeft = '0px';

    chatRoom.style.display = 'block';
    setTimeout(() => {
        chatRoom.style.opacity = '1';
        chatRoom.style.transform = 'translateX(0)';
    }, 10);

    openChat.style.display = 'none';
});


const closeChat = document.querySelector('.closeChat');
closeChat.addEventListener('click', function () {
    const chatRoom = document.getElementById('chatRoom');

    document.querySelector('.groupPostContainer').style.width = '600px';
    document.querySelector('.topPostContainer').style.width = '600px';
    document.getElementById('mainContent').style.marginLeft = '200px';

    chatRoom.style.opacity = '0';
    chatRoom.style.transform = 'translateX(100%)';

    setTimeout(() => {
        chatRoom.style.display = 'none';
    }, 500);

    document.querySelector('.openChat').style.display = 'flex';
});
function scrollToBottom() {
    const messageBox = document.querySelector('.chat-container');
    messageBox.scrollTop = messageBox.scrollHeight;
}
const handleMeeting = async () => {
    if (!selectedRoomId) {
        alert('There is something wrong!Please try again');
    }
    const roomId = Math.floor(Math.random() * 10000) + "";
    const callLink = window.location.protocol + '//' + window.location.host + '/static/videocall.html?roomID=' + roomId;

    window.open(callLink, "_blank");
    const video = `Join here: ${callLink}`;
    console.log("VIdioVLInk",video)
    if (stompClient && selectedRoomId) {
        const chatMessage = {
            roomId: selectedRoomId,
            senderId:id,
            name:username,
            content: video,
            time: new Date()
        };
        stompClient.send("/app/group-chat", {}, JSON.stringify(chatMessage));

    }
};


