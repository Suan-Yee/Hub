let stompClient = null;
let id = null;
let username=null;
let selectedRoomId = null;

document.addEventListener('DOMContentLoaded', () => {

    const messageForm = document.querySelector('#messageForm');
    const messageInput = document.querySelector('#message');
    const connectingElement = document.querySelector('.connecting');
    const chatArea = document.querySelector('#chat-messages');

    const idInput = document.getElementById("id");
    id = idInput.value;
    username=document.getElementById("name").value;

    const  connectWebSocket = () => {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    };
    document.getElementById('file-input-chatRoom').addEventListener('change', async (event) => {
        if (event.target.files.length > 0) {
            console.log('Oh shit  ya nay p')
            await sendMessageWithAttachment();
        }
    });

    findAndDisplayConnectedUsers().then();

    const onConnected = () => {
        stompClient.subscribe(`/user/${selectedRoomId}/queue/messages`, onMessageReceived);
    }
    const emojiSelectorIcon = document.getElementById('emojiSelectorIcon');
    const emojiSelector = document.getElementById('emojiSelector');
    const emojiList=document.getElementById('emojiList');
    const emojiSearch=document.getElementById('emojiSearch');
    emojiSelectorIcon.addEventListener('click', () => {
        emojiSelector.classList.toggle('active');
        fetch('https://emoji-api.com/emojis?access_key=e40cfdf86d4bada254a679a06cf4ef2bfb9efda9')
            .then(res => res.json())
            .then(data => loadEmoji(data))

        function loadEmoji(data) {
            console.log(data);
            data.forEach(emoji => {
                let li = document.createElement('li');
                li.classList.add('emoji-photo')
                li.setAttribute('emoji-name',emoji.slug);
                li.textContent=emoji.character;

                emojiList.appendChild(li);
                li.addEventListener('click', () => {
                    messageInput.value += emoji.character;
                });
            });
        }
    });

    emojiSearch.addEventListener('keyup',e=>{
        let value=e.target.value;
        let emojis=document.querySelectorAll('#emojiList li');
        emojis.forEach(emoji =>{
            if(emoji.getAttribute('emoji-name').toLowerCase().includes(value)){
                emoji.style.display='flex';
            }else{
                emoji.style.display='none';
            }
        })
    })

    const roomId = localStorage.getItem('chatRoomIdForGroup');
    if(roomId){
        findAndDisplayConnectedUsers().then(() => {
            const roomElement = document.getElementById(roomId);
            if (roomElement) {
                roomElement.click();
                localStorage.removeItem('chatRoomIdForGroup');
            }
        });
    } else {
        findAndDisplayConnectedUsers().then();
    }

    async function findAndDisplayConnectedUsers() {
        const response = await fetch('/roomList');
        let connectedRooms = await response.json();
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
        fetchAndDisplayUserChat().then();

        const nbrMsg = clickedUser.querySelector('.nbr-msg');
        nbrMsg.classList.add('hidden');
        nbrMsg.textContent = '0';

        if (!selectedRoomId) {
            messageForm.classList.add('hidden');
        } else {
            messageForm.classList.remove('hidden');
        }

    }

    function formatTime(time) {
        // Create a new Date object from the time variable
        const date = new Date(time);

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
    async function displayMessageForChatRoom(senderId, content, photo,time) {
        const messageContainer = document.createElement('div');
        messageContainer.classList.add('message');
        const userImage = document.createElement('img');
        const image = photo || '/static/assets/img/card.jpg';
        userImage.src = `${image}`;
        userImage.alt = 'User Photo';
        userImage.classList.add('user-photo');
        let createdTime = await formatTime(new Date(time));
        messageContainer.setAttribute('data-toggle', 'tooltip');
        messageContainer.setAttribute('title', `${createdTime}`);
        if (senderId === username) {
            messageContainer.classList.add('sender');
        } else {
            messageContainer.classList.add('receiver');
            messageContainer.style.marginLeft = '45px';
            messageContainer.style.marginTop = '-35px';
            messageContainer.style.borderBottomRightRadius = '10px';
        }
        const messageContentContainer = document.createElement('div');
        messageContentContainer.classList.add('message-content-container');
        const urlPattern = new RegExp('^(https?:\\/\\/)?' +
            '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|' +
            '((\\d{1,3}\\.){3}\\d{1,3}))' +
            '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' +
            '(\\?[;&a-z\\d%_.~+=-]*)?' +
            '(\\#[-a-z\\d_]*)?$', 'i');

        const imageExtensions = /\.(jpeg|jpg|gif|png|bmp|webp)$/i;

        if (urlPattern.test(content) && imageExtensions.test(content)) {
            const messageImage = document.createElement('img');
            messageImage.src = content;
            messageImage.style.width = '150px';
            messageImage.style.height = '150px';
            messageImage.alt = 'Message Image';
            messageContentContainer.appendChild(messageImage);
        } else {
            const message = document.createElement('p');
            const messageContentWithLinks = content.replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank">$1</a>');
            message.innerHTML = messageContentWithLinks;
            messageContentContainer.appendChild(message);
        }
        const spanEl = document.createElement('span');
        if (username !== senderId) {
            spanEl.appendChild(userImage);
        }
        messageContainer.appendChild(messageContentContainer);
        chatArea.appendChild(spanEl);
        chatArea.appendChild(messageContainer);
        chatArea.scrollTop = chatArea.scrollHeight;
    }

    const sendMessageWithAttachment = async () => {
        const fileInput = document.getElementById('file-input-chatRoom');
        const file = fileInput.files[0];

        if (!file) {
            alert('Please select a file!');
            return;
        }
        const formData = new FormData();
        formData.append('file', file);
        formData.append('id', selectedRoomId);
        formData.append('sender', username);
        formData.append('time', new Date());
        let response = await fetch('/send-photo-toChatRoom', {
            method: 'POST',
            body:formData
        });
        if(!response.ok){
            alert('something wrong please try again!');
        }
        const res = await response.json();
        const chatMessage = {
            roomId: selectedRoomId,
            senderId:id,
            name:username,
            content: messageInput.value.trim(),
            time: new Date()
        }
        stompClient.send("/app/chat-withPhoto", {}, JSON.stringify(chatMessage));
        const showedUserPhoto = await fetchUserByLogInId(username);
        const photo = showedUserPhoto.image || '/static/assets/img/profile.jpg';
        await displayMessageForChatRoom(username,res.content,photo,new Date());
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
            <div>
           <span style="color: #7986CB; font-size:small";>${name}</span>
            <div class="msg_cotainer_send">
                <div class="container">
                    ${content}
                    <span class="msg_time_send">${formatTime(currentTime)}</span>
                </div>
            </div>
            </div>
        `;
        } else {
            messageContainer.classList.add('justify-content-start');
            messageContainer.innerHTML = `
            <div class="img_cont_msg">
                <!-- <img src="https://static.turbosquid.com/Preview/001292/481/WV/_D.jpg" class="rounded-circle user_img_msg"> -->
            </div>
             <div>
             <span style="color: #7986CB; font-size:small">${name}</span>
            <div class="msg_cotainer">
                <div class="container">
                    ${content}
                    <span class="msg_time">${formatTime(currentTime)}</span>
                </div>
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
        // chatArea.scrollTop = chatArea.scrollHeight;
        unreadMessages[selectedRoomId] = 0;
    }
    function onError() {
        connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
        connectingElement.style.color = 'red';
    }
    const arrow = document.getElementById('arrow').addEventListener('click',sendMessage,true)

    function sendMessage() {
        const messageInput = document.getElementById('message');
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


    document.getElementById('get-all-member-list').addEventListener('click', async () => {
        if (!selectedRoomId) {
            alert('Please choose the room u want to add member');
            return;
        }
        const memberList = await getAllCommunityMember(selectedRoomId);
        const showUserList = document.getElementById('memberList');
        showUserList.classList.add('container');
        document.getElementById('chat-room-id').value = selectedRoomId;
        showUserList.innerHTML = '';

        const searchBar = document.getElementById('memberSearchForAddChatRoom');
        if (memberList.length > 0) {
            searchBar.parentElement.parentElement.style.display = 'block';
        } else {
            searchBar.parentElement.parentElement.style.display = 'none';
        }

        if (memberList.length === 0) {
            showUserList.innerHTML = 'There is no member to add this chat room';
            return;
        }
        memberList.forEach(user => {
            if (user.senderId === id) {
                return;
            }
            const getData = document.createElement('div');
            getData.classList.add('group');
            // getData.style.border = '1px solid';
            getData.style.borderRadius = '10px';
            getData.style.paddingTop = '20px';
            getData.style.cursor = 'pointer';
            const checkBoxElement = document.createElement('input');
            checkBoxElement.type = 'checkbox';
            checkBoxElement.id = `checkbox-user-${user.id}`;
            checkBoxElement.value = user.id;
            checkBoxElement.name = 'selectedIds';
            const label = document.createElement('label');
            label.classList.add('add-member-chat-room-search');
            label.setAttribute('for', `checkbox-user-${user.id}`);
            label.textContent = user.name;
            const imgDiv = document.createElement('img');
            const photo = user.photo || '/static/assets/img/user1.jpg';
            imgDiv.src = `${photo}`;
            imgDiv.style.width = '50px';
            imgDiv.style.height = '50px';
            imgDiv.style.borderRadius = '50px';
            imgDiv.style.marginLeft = '300px';
            imgDiv.style.marginTop = '-35px';
            getData.appendChild(checkBoxElement);
            getData.appendChild(label);
            getData.appendChild(imgDiv);
            showUserList.appendChild(getData);

            document.getElementById('memberSearchForAddChatRoom').addEventListener('input', function() {
                const searchValue = this.value.toLowerCase();
                const allUsers = document.querySelectorAll('#memberList .group');

                allUsers.forEach(userContainer => {
                    const userNameElement = userContainer.querySelector('.add-member-chat-room-search');
                    if (userNameElement) {
                        const userName = userNameElement.textContent.toLowerCase();
                        if (userName.includes(searchValue)) {
                            userContainer.style.display = 'block';
                        } else {
                            userContainer.style.display = 'none';
                        }
                    }
                });
            });
        });
    });
    document.getElementById('member-add-icon').addEventListener('click', async () => {
        const formData = new FormData(document.getElementById('get-all-user'));
        const validateData = await fetch(`/add-user-chat-room`, {
            method: 'POST',
            body: formData
        });
        if (!validateData.ok) {
            const response = await validateData.json();
            alert(`${response.message}`);
        } else {
            const response1 = await validateData.json();
            alert(`${response1.message}`);
            $('#memberAddModal').modal('hide');
        }
    });

    document.getElementById('kick-room-member').addEventListener('click', async () => {
        if (!selectedRoomId) {
            alert('Please choose the room first!!!');
            return;
        }
        const userList = await getAllChatRoomMember(selectedRoomId);
        const showUserList = document.getElementById('memberListForKick');
        showUserList.classList.add('container');
        showUserList.innerHTML = '';
        document.getElementById('kick-room-id').value = selectedRoomId;

        const searchBar = document.getElementById('memberSearchForKick');
        if (userList.length > 0) {
            searchBar.parentElement.parentElement.style.display = 'block';
        } else {
            searchBar.parentElement.parentElement.style.display = 'none';
        }

        if (userList.length === 0) {
            showUserList.innerHTML = 'There is no member to kick ';
            return;
        }

        userList.forEach(user => {
            if (user.senderId === id) {
                return;
            }
            const getData = document.createElement('div');
            getData.classList.add('group');
            // getData.style.border = '1px solid';
            getData.style.borderRadius = '10px';
            getData.style.paddingTop = '20px';
            getData.style.cursor = 'pointer';
            const checkBoxElement = document.createElement('input');
            checkBoxElement.type = 'checkbox';
            checkBoxElement.id = `checkbox-user-${user.id}`;
            checkBoxElement.value = user.id;
            checkBoxElement.name = 'selectedIds';
            const label = document.createElement('label');
            label.classList.add('kick-member-list-chatRoom');
            label.setAttribute('for', `checkbox-user-${user.id}`);
            label.textContent = user.name;
            const imgDiv = document.createElement('img');
            const photo = user.photo || '/static/assets/img/user1.jpg';
            imgDiv.src = `${photo}`;
            imgDiv.style.width = '50px';
            imgDiv.style.height = '50px';
            imgDiv.style.borderRadius = '50px';
            imgDiv.style.marginLeft = '300px';
            imgDiv.style.marginTop = '-35px';
            getData.appendChild(checkBoxElement);
            getData.appendChild(label);
            getData.appendChild(imgDiv);
            showUserList.appendChild(getData);

            document.getElementById('memberSearchForKick').addEventListener('input', function() {
                const searchValue = this.value.toLowerCase();
                const allUsers = document.querySelectorAll('#memberListForKick .group');

                allUsers.forEach(userContainer => {
                    const userNameElement = userContainer.querySelector('.kick-member-list-chatRoom');
                    if (userNameElement) {
                        const userName = userNameElement.textContent.toLowerCase();
                        if (userName.includes(searchValue)) {
                            userContainer.style.display = 'block';
                        } else {
                            userContainer.style.display = 'none';
                        }
                    }
                });
            });

        });
    });

    document.getElementById('member-kick-icon').addEventListener('click', async () => {
        const formData = new FormData(document.getElementById('kick-room-user'));
        const validateData = await fetch(`/kick-user-chat-room`, {
            method: 'POST',
            body: formData
        });
        if (!validateData.ok) {
            const response = await validateData.json();
            alert(`${response.message}`);
        }
        const response1 = await validateData.json();
        alert(`${response1.message}`);
        $('#memberKickModal').modal('hide');
    });
});
const fetchUserByLogInId = async (id) => {
    const fetchUserData = await fetch(`/get-userData/${id}`);
    if (!fetchUserData.ok) {
        alert('Invalid user');
    }
    const userData = await fetchUserData.json();
    return userData;
};
const getAllCommunityMember = async (id) => {
    const getData = await fetch(`/member-list-chatRoom/${id}`);
    const response = await getData.json();
    return response;
};
const getAllChatRoomMember = async (id) => {
    const getRoomData = await fetch(`/chat-room-memberList/${id}`);
    const response = await getRoomData.json();
    return response;
}
const chooseMalFile = () => {
    document.getElementById('file-input-chatRoom').click();
}