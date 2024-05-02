document.addEventListener('DOMContentLoaded', () => {

    const messageForm = document.querySelector('#messageForm');
    const messageInput = document.querySelector('#message');
    const connectingElement = document.querySelector('.connecting');

    let stompClient = null;
    let id = null;
    let username=null;
    let selectedRoomId = null;
    const idInput = document.getElementById("id");
    id = idInput.value;
    username=document.getElementById("name").value;

    const  connectWebSocket = () => {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    findAndDisplayConnectedUsers().then();

    const onConnected = () => {
        stompClient.subscribe(`/user/${selectedRoomId}/queue/messages`, onMessageReceived);
    }

    async function  findAndDisplayConnectedUsers() {
        const response = await fetch('/roomList');
        let connectedRooms = await response.json();
        console.log(connectedRooms)
        const connectedUsersList = document.getElementById('connectedUsers');
        connectedUsersList.innerHTML = '';

        connectedRooms.forEach(room => {
            appendUserElement(room, connectedUsersList);
            if (connectedRooms.indexOf(room) < connectedRooms.length - 1) {
                const separator = document.createElement('li');
                separator.classList.add('separator');
                connectedUsersList.appendChild(separator);
            }
        });
    }
    function appendUserElement(room, connectedUsersList) {
        const listItem = document.createElement('li');
        listItem.classList.add('user-item');
        listItem.id = room.id;
        listItem.setAttribute('name', room.name);

        const userImage = document.createElement('img');
        userImage.src = room.image;
        userImage.alt = room.name;
        userImage.classList.add('imageIcon');

        const usernameSpan = document.createElement('span');
        usernameSpan.textContent = room.name;

        const receivedMsgs = document.createElement('span');
        receivedMsgs.classList.add('nbr-msg', 'hidden');
        listItem.appendChild(userImage);
        listItem.appendChild(usernameSpan);
        listItem.appendChild(receivedMsgs);
        listItem.addEventListener('click', userItemClick);
        connectedUsersList.appendChild(listItem);
    }

    function userItemClick(event) {
        document.querySelectorAll('.user-item').forEach(item => {
            item.classList.remove('active');
        });

        const clickedUser = event.currentTarget;
        clickedUser.classList.add('active');
        document.getElementById('cardBody').innerHTML = '';

        selectedRoomId = clickedUser.getAttribute('id');
        console.log(selectedRoomId);
        if(selectedRoomId){
            connectWebSocket();
        }
        fetchAndDisplayUserChat();

    }
    function formatTime(time) {
        // Create a new Date object from the time variable
        console.log("Original Time:", time);
        const date = new Date(time);
        console.log("Parsed Date:", date);

        // Check if the date is valid
        if (isNaN(date.getTime())) {
            console.error('Invalid time:', time);
            return ''; // Return an empty string or some default value
        }

        // Format the date and time
        return date.toLocaleString(undefined, {
            month: 'long',
            day: 'numeric',
            year: 'numeric',
            hour: 'numeric',
            minute: 'numeric',
            second: 'numeric'
        });
    }


    function displayMessage(senderId, content, time, name) {
        const messageBox = document.getElementById('cardBody');

        const messageContainer = document.createElement('div');
        messageContainer.classList.add('d-flex');
        messageContainer.classList.add('mb-4');
        const currentTime = new Date(time);

        if (senderId == id) {
            messageContainer.classList.add('justify-content-end');
            messageContainer.innerHTML = `
            <div class="img_cont_msg">
                <!-- <img src="https://static.turbosquid.com/Preview/001292/481/WV/_D.jpg" class="rounded-circle user_img_msg"> -->
            </div>
            <span><b>${name}</b></span>
            <div class="msg_cotainer_send">
                <div class="container">
                
                    ${content}
                    <span class="msg_time_send">${formatTime(currentTime)}</span>
                </div>
            </div>
        `;
        } else {
            messageContainer.classList.add('justify-content-start');
            messageContainer.innerHTML = `
            <div class="img_cont_msg">
                <!-- <img src="https://static.turbosquid.com/Preview/001292/481/WV/_D.jpg" class="rounded-circle user_img_msg"> -->
            </div>
             <span><b>${name}</b></span>
            <div class="msg_cotainer">
                <div class="container">
                    ${content}
                    <span class="msg_time">${formatTime(currentTime)}</span>
                </div>
            </div>
        `;
        }

        messageBox.appendChild(messageContainer);
    }

// Helper function to format time
    function formatTime(time) {
        return time.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }


    async function fetchAndDisplayUserChat() {
        const userChatResponse = await fetch(`/messages/${selectedRoomId}`);
        const userChat = await userChatResponse.json();
        console.log(userChat);
        userChat.forEach(chat => {
            displayMessage(chat.senderId, chat.content,chat.time,chat.name);
        });
        chatArea.scrollTop = chatArea.scrollHeight;
        unreadMessages[selectedRoomId] = 0;
    }
    function onError() {
        connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
        connectingElement.style.color = 'red';
    }
    const arrow = document.getElementById('arrow').addEventListener('click',sendMessage,true)

    function sendMessage() {
        const messageInput = document.getElementById('messageInput');
        const messageContent = messageInput.value.trim();

        console.log("Message" + messageContent);
        if (messageContent && stompClient) {
            const chatMessage = {
                roomId: selectedRoomId,
                senderId:id,
                name:username,
                content: messageInput.value.trim(),
                time: new Date()
            };
            console.log(chatMessage);
            stompClient.send("/app/group-chat", {}, JSON.stringify(chatMessage));
            displayMessage(id, messageInput.value.trim(),new Date(),username);
            messageInput.value = '';
        }

    }

     let unreadMessages = {};
    async function onMessageReceived(payload) {
        try {
            // Parse the incoming message
            const message = JSON.parse(payload.body);

            // Check if the message is not sent by the current user
            if (message.senderId !== id) {
                // Display the message
                displayMessage(message.senderId, message.content, message.time, message.name);

                if(message.name !== username) {
                    displayMessage(message.name, message.content);

                }
                const notifiedUser = document.getElementById(`[id="${message.id}"]`);
                if (notifiedUser && message.name !== username) {
                    // Increment the unread message count for the user
                    const nbrMsg = notifiedUser.querySelector('.nbr-msg');
                    nbrMsg.classList.remove('hidden');
                    nbrMsg.textContent = (parseInt(nbrMsg.textContent) || '0') + 1;
                    await fetchAndDisplayUserChat();
                }
            }
        } catch (error) {
            console.error('Error parsing or processing the message:', error);
        }
    }
    messageForm.addEventListener('submit', sendMessage, true);
});
