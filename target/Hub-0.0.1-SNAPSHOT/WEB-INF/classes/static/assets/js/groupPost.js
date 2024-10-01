import { renderVideoContent } from "./renderVideo.js";
import { renderPhotos } from "./renderPhoto.js";
import { renderInteractionIcons } from "./renderIcon.js";
import {fetchLike} from "./giveLike.js";
import {deletePost} from "./postDelete.js";
// import {openUpdateModal} from "./postUpdate.js";

let groupId = null;
let userStatus = null;
let isAdmin = false;
document.addEventListener('DOMContentLoaded', async () => {
    const currentUrl = window.location.href;
    let urlParts = currentUrl.split('/');
    let lastPart = urlParts[urlParts.length - 1];
    groupId = lastPart;
    console.log(lastPart);

    await userProfileFetch(lastPart);
    await fetchTopPost(lastPart);
    await getJoinedUser(lastPart);

    const userInfo = await checkUserRequest(lastPart);
    // const isOwner = await checkOwnerOrNot(lastPart);
    userStatus = userInfo.status;
    isAdmin = userInfo.admin;

    toggleButton(userStatus);

    const topPost = document.getElementById('topPost');
    const latestPost = document.getElementById('latestPost');
    const aboutCommunity = document.getElementById('aboutCommunity');
    const postContainer = document.querySelector('.groupPostContainer');
    const topPostContainer = document.querySelector('.topPostContainer');
    const aboutContainer = document.querySelector('.about_community');
    const chatOpenDiv = document.querySelector('.openChat');
    const groupPollContainer = document.querySelector('.group-polls-container');


    if (userStatus === 'visitor') {
        chatOpenDiv.classList.add('hiddenChat');

    } else {
        chatOpenDiv.classList.remove('hiddenChat');
    }

    topPost.classList.add('activeTab');

    topPost.addEventListener('click', () => {
        if (!topPost.classList.contains('activeTab')) {
            fetchTopPost(lastPart);
            postContainer.style.display = 'none';
            topPostContainer.style.display = 'block';
            aboutContainer.style.display = 'none';
            chatOpenDiv.style.display = 'flex';
            groupPollContainer.style.display = 'none';
            setActiveTab(topPost);
        }
    });

    latestPost.addEventListener('click', () => {
        if (!latestPost.classList.contains('activeTab')) {
            fetchPostFromGroup(lastPart);
            postContainer.style.display = 'block';
            topPostContainer.style.display = 'none';
            aboutContainer.style.display = 'none';
            chatOpenDiv.style.display = 'flex';
            groupPollContainer.style.display = 'none';
            setActiveTab(latestPost);
        }
    });

    aboutCommunity.addEventListener('click', () => {
        if (!aboutCommunity.classList.contains('activeTab')) {
            postContainer.style.display = 'none';
            topPostContainer.style.display = 'none';
            aboutContainer.style.display = 'block';
            chatOpenDiv.style.display = 'none';
            groupPollContainer.style.display = 'none';
            setActiveTab(aboutCommunity);
        }
    });

    function setActiveTab(activeTab) {
        document.querySelectorAll('#mediaTag li').forEach(tab => {
            tab.classList.remove('activeTab');
        });
        activeTab.classList.add('activeTab');
    }

    const joinedRequest = document.getElementById('userRequestToJoinBtn');
    joinedRequest.addEventListener('click', () => {
        sendUserRequestToJoin();
    });

    let btn = document.getElementById("openModalsya");
    const inviteBtn = document.getElementById('inviteBtn');

    if (isAdmin) {
        inviteBtn.style.display = 'block';
        btn.style.display = 'block';
    } else {
        inviteBtn.style.display = 'none';
        btn.style.display = 'none';
    }

    let modal = document.getElementById("myModalsya");
    btn.addEventListener('click',() => {
        fetchAllTheRequestList(lastPart);
        modal.style.display = 'block';
    })
    let span = document.getElementsByClassName("closesya")[0];
    span.addEventListener('click',() => {
        modal.style.display = "none";
    })

    document.querySelector('.topPostContainer').addEventListener('click', function (event) {
        const target = event.target;
        if (target.matches('[id^="likeIcon-"]')) {
            const postId = target.id.split('-')[1];
            fetchLike(postId, target);
        } else if (target.matches('[id^="bookMarkIcon-"]')) {
            const postId = target.id.split('-')[1];
            saveBookMark(postId, target);
        }
        else if (target.matches('[id^="delete-post-"]')) {
            const postId = target.id.split('-')[2];
            deletePost(postId);
        } else if (target.matches('[id^="update-post-"]')) {
            const postId = target.id.split('-')[2];
            // openUpdateModal(postId);
        }
    });

    document.querySelector('.groupPostContainer').addEventListener('click', function (event) {
        const target = event.target;
        if (target.matches('[id^="likeIcon-"]')) {
            const postId = target.id.split('-')[1];
            fetchLike(postId, target);
        } else if (target.matches('[id^="bookMarkIcon-"]')) {
            const postId = target.id.split('-')[1];
            saveBookMark(postId, target);
        }
        else if (target.matches('[id^="delete-post-"]')) {
            const postId = target.id.split('-')[2];
            deletePost(postId);
        } else if (target.matches('[id^="update-post-"]')) {
            const postId = target.id.split('-')[2];
            // openUpdateModal(postId);
        }
    });

});

const toggleButton = (status) => {
    const joinBtn = document.getElementById('userRequestToJoinBtn');

    switch (status) {
        case 'joined':
            joinBtn.textContent = 'Joined';
            joinBtn.disabled = true;
            break;
        case 'visitor':
            joinBtn.textContent = 'Request';
            joinBtn.disabled = false;
            break;
        case 'requested':
            joinBtn.textContent = 'Pending';
            joinBtn.disabled = true;
            break;
        default:
            joinBtn.textContent = 'Request';
            joinBtn.disabled = false;
            break;
    }
};
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

const fetchPostFromGroup = async (groupId) => {
    const response = await fetch(`/post/groupPost/${groupId}`);
    const data = await response.json();

    const postContainer = document.querySelector('.groupPostContainer');
    postContainer.innerHTML = '';

    data.forEach(post => {
        const postDiv = document.createElement('div');
        postDiv.classList.add('tweets');
        postDiv.id = `post-${post.id}`;
        const videoContent = post.content.videos.length > 0 ? renderVideoContent(post) : '';
        const photoContent = post.content.images.length > 0 ? renderPhotos(post.content.images) : '';
        postDiv.innerHTML = `
            <div class="col-auto">
                <div class="user-pics"><img src="${post.photo}" alt="user3"></div>
            </div>
            <div class="user-content-box col">
                <div class="d-flex justify-content-between user-names" style="height: 35px"> 
                    <h1 class="full-name" style="margin-top: 5px;text-wrap: nowrap">${post.user.name}.</h1>
                    <p class="time me-3" style="text-wrap: nowrap">${post.time}</p>
                    <div class="dropdown">
                        <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" style="padding-left: 10px;padding-bottom: 13px"></i>
                        <ul class="dropdown-menu">
                            <div class="postBtnContainer">
                                <div class="postEditButton" id="update-post-${post.id}">Edit<i class="fa-solid fa-pen"></i></div>
                                <div class="postDeleteButton" data-bs-toggle="modal" data-bs-target="#deletePostModal-${post.id}" id="postDeleteButton">Delete<i class="fa-solid fa-trash"></i></div>
                            </div>
                        </ul>
                    </div>
                </div>
                <div class="user-content">
                    <p>${post.content.text}</p>
                    ${videoContent || photoContent}
                </div>
                ${renderInteractionIcons(post)}
                <div class="modal fade" id="deletePostModal-${post.id}" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h1 class="modal-title fs-5" id="exampleModalLabel">Are You Sure Want to Delete?</h1>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-footer">
                            <button type="button" id="cancelDeleteBtn" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" class="btn btn-danger" id="delete-post-${post.id}">Delete</button>
                        </div>
                    </div>
                </div>
            </div>
            </div>`;
        postContainer.appendChild(postDiv);
    });
};

const userProfileFetch = async (groupId) => {
    const response = await fetch(`/user/groupUserProfile/${groupId}`);
    const data = await response.json();

    const userProfileUL = document.querySelector('.groupUserProfile');
    data.forEach(user => {
        const profileContainer = document.createElement('li');
        profileContainer.classList.add('groupUserProfile');

        const userImage = document.createElement('img');
        userImage.src = user ? user : 'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';
        userImage.alt = "userImage";

        profileContainer.appendChild(userImage);
        userProfileUL.appendChild(profileContainer);
    });
};

const fetchTopPost = async (groupId) => {
    try {
        const response = await fetch(`/post/topPostsInGroup/${groupId}`);
        if (response.ok) {
            const data = await response.json();
            if (data.length > 0) {
                const topPostContainer = document.querySelector('.topPostContainer');
                topPostContainer.innerHTML = '';

                data.forEach(post => {
                    const postDiv = document.createElement('div');
                    postDiv.classList.add('tweets');
                    postDiv.id = `post-${post.id}`;
                    const videoContent = post.content.videos.length > 0 ? renderVideoContent(post) : '';
                    const photoContent = post.content.images.length > 0 ? renderPhotos(post.content.images) : '';
                    postDiv.innerHTML = `
                        <div class="col-auto">
                            <div class="user-pics"><img src="${post.photo}" alt="user3"></div>
                        </div>
                        <div class="user-content-box col">
                            <div class="d-flex justify-content-between user-names" style="height: 35px"> 
                                <h1 class="full-name" style="margin-top: 5px">${post.user.name}.</h1>
                                <p class="time me-3 ">${post.time}</p>
                                <div class="dropdown">
                        <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" style="padding-left: 10px;padding-bottom: 13px"></i>
                        <ul class="dropdown-menu">
                            <div class="postBtnContainer">
                                <div class="postEditButton" id="update-post-${post.id}">Edit<i class="fa-solid fa-pen"></i></div>
                                <div class="postDeleteButton" data-bs-toggle="modal" data-bs-target="#deletePostModal-${post.id}" id="postDeleteButton">Delete<i class="fa-solid fa-trash"></i></div>
                            </div>
                        </ul>
                    </div>
                            </div>
                            <div class="user-content">
                                <p>${post.content.text}</p>
                                ${videoContent || photoContent}
                            </div>
                            ${renderInteractionIcons(post)}
                            <div class="modal fade" id="deletePostModal-${post.id}" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h1 class="modal-title fs-5" id="exampleModalLabel">Are You Sure Want to Delete?</h1>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-footer">
                            <button type="button" id="cancelDeleteBtn" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" class="btn btn-danger" id="delete-post-${post.id}">Delete</button>
                        </div>
                    </div>
                </div>
            </div>
                        </div>`;
                    topPostContainer.appendChild(postDiv);
                });
            } else {
                console.log('No top posts found for this group.');
            }
        } else {
            console.error('Failed to fetch top posts:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching top posts:', error);
    }
};

const sendUserRequestToJoin = async () => {
    const response = await fetch('/group/requestToJoin', {
        method: 'POST',
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ "id": groupId })
    });
    if (!response.ok) {
        throw new Error("Cannot request to Join!");
    } else {
        const joinBtn = document.getElementById('userRequestToJoinBtn');
        joinBtn.textContent = 'Pending';
        joinBtn.disabled = true;
    }
};

const getJoinedUser = async (groupId) => {
    const response = await fetch(`/group/userList/${groupId}`);

    const userContainer = document.querySelector('.userContainer');
    const groupMembers = document.querySelector('.GroupMembers');
    const userSearchGP = document.querySelector('.userSearchGP');

    userContainer.innerHTML = '';

    if (response.ok) {
        const data = await response.json();
        console.log("UserList" + JSON.stringify(data));
        window.usersData = data;

        if (data.length === 0) {
            userContainer.textContent = 'There are no members yet.';
            userSearchGP.style.display = 'none';
        } else {
            userSearchGP.style.display = 'block';
            data.forEach(user => {

                const userDiv = createUserDiv(user);
                userContainer.appendChild(userDiv);
            });
        }
    } else if (response.status === 404) {
        userContainer.textContent = 'There are no members yet.';
        userSearchGP.style.display = 'none';
    } else {
        console.error('Failed to fetch user list.');
    }
};

const createUserDiv = (user) => {
    const userContainerDiv = document.createElement('div');
    userContainerDiv.style.display = 'flex';
    userContainerDiv.style.justifyContent = 'space-between';
    userContainerDiv.id = `userId-${user.userId}`

    const userDiv = document.createElement('div');
    userDiv.classList.add('user');
    userDiv.style.display = 'flex';
    userDiv.style.margin = '15px';

    const image = document.createElement('img');
    image.alt = 'image';
    image.classList.add('gpUserImage');
    image.src = user.userProfile ? user.userProfile : 'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';

    const userInfo = document.createElement('div');

    const userInfoContainer = document.createElement('div');
    userInfoContainer.classList.add('userInfoContainer');
    userInfoContainer.style.display = 'flex';

    // UserName
    const userName = document.createElement('div');
    userName.textContent = `${user.userName}.`;
    userName.classList.add('userNameGP');
    userName.style.lineHeight = 'normal';

    // PostCount
    const postCount = document.createElement('p');
    const postCountText = user.postCount === 1 ? 'post' : 'posts';
    postCount.textContent = `(posted ${user.postCount} ${postCountText})`;
    postCount.classList.add('postCount');

    userInfoContainer.appendChild(userName);
    userInfoContainer.appendChild(postCount);

    // JoinedDate
    const joinedDate = document.createElement('p');
    joinedDate.textContent = `Joined ${user.joinedDate}.`;
    joinedDate.classList.add('userJoinedDate');
    joinedDate.style.fontSize = 'smaller';
    joinedDate.style.fontWeight = '500';

    const btnContainer = document.createElement('div');

    const kickBtn = document.createElement('button');
    kickBtn.classList.add('kickAndAdmin');
    kickBtn.textContent = 'Kick';
    kickBtn.id = `kick-${user.userId}`;

    const changeAdmin = document.createElement('button');
    changeAdmin.classList.add('kickAndAdmin');
    changeAdmin.textContent = 'Change Admin';
    changeAdmin.id = `admin-${user.userId}`;

    // Add event listeners for the buttons
    kickBtn.addEventListener('click', () => {
        showModal(user.userId, groupId,'kick');
    });

    changeAdmin.addEventListener('click', () => {
        showModal(user.userId,groupId,'admin');
    });

    if(isAdmin){
        btnContainer.appendChild(kickBtn);
        btnContainer.appendChild(changeAdmin);
    }

    userInfo.appendChild(userInfoContainer);
    userInfo.appendChild(joinedDate);

    userDiv.appendChild(image);
    userDiv.appendChild(userInfo);

    userContainerDiv.appendChild(userDiv);
    userContainerDiv.appendChild(btnContainer);

    return userContainerDiv;
};

const confirmationModal = new bootstrap.Modal(document.getElementById('confirmationModalKick'));
const confirmKickBtn = document.getElementById('confirmKickBtn');

const adminConfirmation = new bootstrap.Modal(document.getElementById('confirmationModalAdmin'));
const confirmAdminBtn = document.getElementById('confirmAdminBtn');

let currentUser;
let currentGroupId;

const showModal = (userId, groupId,status) => {
    currentUser = userId;
    currentGroupId = groupId;
    if(status === 'kick'){
        confirmationModal.show();
    }else{
        adminConfirmation.show();
    }
};

confirmKickBtn.addEventListener('click', async () => {
    confirmationModal.hide();
    await kickUser(currentUser, currentGroupId);
});

confirmAdminBtn.addEventListener('click',async () => {
    adminConfirmation.hide();
    await changeAdminUser(currentUser,currentGroupId);
})

const kickUser = async (userId) => {
    console.log("UserID kick" + userId);
    console.log("GroupId kick" + currentGroupId);
    try {
        const response = await fetch('/kickUser', {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                "groupId": currentGroupId,
                "userId": userId
            })
        });
        if (response.ok) {
            // Update the UI based on the response
            const kickBtn = document.getElementById(`kick-${userId}`);
            kickBtn.textContent = 'Kicked';
            kickBtn.disabled = true; // Disable the button after kicking
        } else {
            console.error('Failed to kick user:', response.statusText);
        }
    } catch (error) {
        console.error('Error kicking user:', error);
    }
};
const changeAdminUser = async (userId, groupId) => {
    try {
        const response = await fetch('/changeAdmin', {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                "groupId": groupId,
                "userId": userId
            })
        });
        if (response.ok) {
            const data = await response.json();
            console.log(data);

            const userContainer = document.querySelector('.userContainer');
            const userDiv = document.getElementById(`userId-${data.newAdmin}`);
            console.log("userDiv" + userDiv);
            if (userDiv) {
                userDiv.remove();
            } else {
                console.error(`Element with ID userId-${data.newAdmin} not found.`);
            }

            const newAdmin = createUserDiv(data);
            userContainer.appendChild(newAdmin);
        } else {
            console.error('Failed to change admin:', response.statusText);
        }
    } catch (error) {
        console.error('Error changing admin:', error);
    }
}

// Add event listener for search functionality
document.querySelector('.userSearchGP').addEventListener('keyup', (event) => {
    const searchQuery = event.target.value.toLowerCase();
    const userContainer = document.querySelector('.userContainer');
    userContainer.innerHTML = ''; // Clear the container

    if (searchQuery === '') {
        // When search query is empty, show all users only once
        window.usersData.forEach(user => {
            const userDiv = createUserDiv(user);
            userContainer.appendChild(userDiv);
        });
    } else {
        // Filter users based on search query
        const filteredUsers = window.usersData.filter(user => user.userName.toLowerCase().includes(searchQuery));
        filteredUsers.forEach(user => {
            const userDiv = createUserDiv(user);
            userContainer.appendChild(userDiv);
        });
    }
});


const checkUserRequest = async (groupId) => {
    const response = await fetch(`/user/checkUserRequest/${groupId}`);

    if (response.ok) {
        const data = await response.json();
        console.log(data);
        return data;
    } else {
        throw new Error('Failed to check user request status');
    }
};
const fetchAllTheRequestList = async (groupId) => {
    const response = await fetch(`/get-all-userRequest/${groupId}`);

    if(response.ok){
        const data = await response.json();
        const notificationsDiv = document.getElementById('notificationssya');
        notificationsDiv.innerHTML = '';

        data.forEach(user => {
                const notification = document.createElement('div');
                notification.className = 'notificationsya';
                notification.style.backgroundColor = '#f3e5d3';
                notification.style.borderRadius = '20px';

                notification.innerHTML = `
                <div class="notification-headersya">
                <img src=${user.userProfile} alt="User Image" style=" border-radius: 50%; width: 40px; height: 40px;">
                ${user.requestUserName} requests to join ${user.groupName}</div>
                <div class="notification-bodysya">Would you like to accept or decline this request?</div>
                <div class="notification-actionssya">
                    <button class="btnsya btn-acceptsya">Accept</button>
                    <button class="btnsya btn-declinesya">Decline</button>
                </div>
            `;
                notificationsDiv.appendChild(notification);

            notification.querySelector('.btn-acceptsya').addEventListener('click', () => {
                console.log("ACCEPT");
                console.log(user)
                sendStatus(groupId,user.requestUserStaffId,'ACCEPT')
            });
            notification.querySelector('.btn-declinesya').addEventListener('click', () => {
                console.log("DECLINE");
                sendStatus(groupId,user.requestUserStaffId,'DECLINE')
            });
            });
    } else if (response.status === 404) {
        const notificationsDiv = document.getElementById('notificationssya');
        notificationsDiv.innerHTML = '';
        const para = document.createElement('p');
        para.style.fontSize = 'larger';
        para.style.fontWeight = '500';
        para.textContent = 'There is no request yet!';
        notificationsDiv.appendChild(para);
    } else {
        throw new Error('Failed to load user request List.');
    }
}
const sendStatus = async (groupId,staffId,status) => {
    console.log(groupId,staffId,status)
    const response = await fetch('/group/getStatus', {
        method: 'POST',
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            "groupId": groupId,
            "userId": staffId,
            "status" : status
        })
    });
    if(response.ok){
        let modal = document.getElementById("myModalsya");
        modal.style.display = 'none';
    }
}