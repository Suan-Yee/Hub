import { state } from './state.js'; // Import state for post pagination

let notificationPage = 0;
let notificationSize = 4;
let notificationLoading = false;
let notificationHasMore = true;
let removeAllNotificationButton = null;
let turnOffNotificationButton = null;
const fetchAllNoti = async () => {
    if (!notificationHasMore || notificationLoading) return;

    notificationLoading = true;
    const response = await fetch(`/noti/all?page=${notificationPage}&size=${notificationSize}`);
    notificationLoading = false;

    if (!response.ok) {
        if (response.status === 404) {
            document.getElementById('notiContainer').innerHTML = '<li style="margin: 5px">There is no notification yet.</li>';
            notificationHasMore = false; // No more notifications to load
            return;
        }
        throw new Error('Network response was not ok.');
    }
    const responseData = await response.json();
    updateNotificationsDisplay(responseData);
};

document.addEventListener('DOMContentLoaded', async () => {
    await notificationCount();
    document.body.addEventListener('click', function (event) {
        if (event.target.classList.contains('delete')) {
            event.stopPropagation();
            event.preventDefault();
            console.log('Read button clicked, dropdown should not close.');
        } else if (event.target.id === 'seeMoreButton') {
            event.stopPropagation();
            notificationPage++;
            fetchAllNoti();
        }
    });

    const notifyButton = document.getElementById('notify');
    notifyButton.addEventListener('click', () => {
        if (document.getElementById('notiContainer').children.length === 0) {
            notificationPage = 0; // Ensure starting page is 0
            fetchAllNoti();
            notificationCount();
        } else {
            addRemoveAllNotification(); // Ensure button is added when opening the modal
        }
    });

    $('#notify').on('hidden.bs.dropdown', function () {
        document.getElementById('notiContainer').innerHTML = '';
        notificationPage = 0;
        notificationHasMore = true;
        removeAllNotificationButton = null; // Reset button to ensure it gets recreated
    });
});

function updateNotificationsDisplay(data) {
    const { content, last, empty } = data;
    const notiContainer = document.getElementById('notiContainer');

    if (content.length === 0 && notificationPage === 0) {
        notiContainer.innerHTML = '<li style="margin: 5px">No notifications found.</li>';
        return;
    }
    console.log(data);
    content.forEach(noti => appendNotification(noti, notiContainer));
    manageSeeMoreButton(notiContainer, last);
    addRemoveAllNotification(notiContainer, empty);
}

function appendNotification(noti, container) {

    const notiLi = document.createElement('div');
    notiLi.classList.add('notification-card');
    notiLi.style.backgroundColor = noti.read ? 'rgb(229, 235, 240)' : '';
    notiLi.id = `noti-${noti.id}`;
    notiLi.innerHTML = `
        <div class="avatar"><img alt="image" src="${noti.userPhoto}"></div>
        <div class="content">
            <div class="topContent">
                <p>${noti.message}</p>
                <div class="btnn ${noti.type === 'COMMENT' ? 'commentt' : noti.type === 'LIKE' ? 'like' : 'mentionNoti'}">${noti.type}</div>
            </div>
            <div class="box">
                <div class="btnn ${noti.read ? 'read' : 'unread'}">${noti.read ? 'Read' : 'Unread'}</div>
                <span class="timestamp">${noti.time}</span>
            </div>
        </div>`;
    container.appendChild(notiLi);
    notiLi.addEventListener('click', async () => {
        if (noti.type === 'COMMENT') {
            localStorage.setItem('commentId', noti.commentId);
            if (noti.rootCommentId) {
                localStorage.setItem('rootCommentId', noti.rootCommentId);
            }
            window.location.href = `/post/details/${noti.postId}`;
        } else {
            if (window.location.pathname.endsWith("/index") || window.location.pathname === "/") {
                await loadAndExecutePostActions(noti.postId);
            } else {
                const url = `/post/details/${noti.postId}`;
                window.location.href = url;
            }
        }
        // Ensure the DOM has updated before changing the notification status
    });
    notiLi.addEventListener('click',() => changeNotificationStatus(noti));
}

function manageSeeMoreButton(container, last) {
    let seeMoreLi = document.getElementById('seeMoreLi');
    if (last) {
        notificationHasMore = false;
        if (seeMoreLi) seeMoreLi.remove(); // Remove see more button if last page
    } else {
        if (!seeMoreLi) {
            seeMoreLi = document.createElement('div');
            seeMoreLi.id = 'seeMoreLi';
            const seeMoreButton = document.createElement('button');
            seeMoreButton.id = 'seeMoreButton';
            seeMoreButton.innerText = 'See More';
            seeMoreButton.classList.add('seeMoreBtn');
            seeMoreButton.onclick = (event) => {
                event.preventDefault();
                notificationPage++;
                fetchAllNoti();
            };
            seeMoreLi.appendChild(seeMoreButton);
        }
        container.appendChild(seeMoreLi);
    }
}

const addRemoveAllNotification = async (container, empty = false) => {
    if (empty) {
        return;
    }

    if (!removeAllNotificationButton) {
        removeAllNotificationButton = document.createElement('button');
        removeAllNotificationButton.innerHTML = '<i class="fa-solid fa-trash-can" style="color: black;"></i> Remove All Notification';
        removeAllNotificationButton.classList.add('removeNotiButton');
        removeAllNotificationButton.onclick = async (event) => {
            event.preventDefault();
            try {
                await deleteAllNotification();
                container.innerHTML = '';
                notificationCount();
            } catch (error) {
                console.error('Failed to remove all notifications:', error);
            }
        };

        const removeNotificationBtnContainer = document.createElement('div');
        removeNotificationBtnContainer.classList.add('removeContainer');
        removeNotificationBtnContainer.appendChild(removeAllNotificationButton);

        // Add Turn Off Notifications Button
        // turnOffNotificationButton = document.createElement('button');
        // turnOffNotificationButton.innerHTML = 'Turn Off Notifications';
        // turnOffNotificationButton.classList.add('turnOffNotiButton');
        // turnOffNotificationButton.onclick = (event) => {
        //     event.preventDefault();
        //     alert('Notifications have been turned off');
        // };

        // removeNotificationBtnContainer.appendChild(turnOffNotificationButton);
        // container.prepend(removeNotificationBtnContainer);
    }
};

// const turnToggleNotification = async () => {
//     const response = fetch('/noti/turnToggleNotification')
// }

async function loadAndExecutePostActions(postId) {
    if (window.location.pathname.endsWith("/index") || window.location.pathname === "/") {
        const [stateModule, postDisplayModule] = await Promise.all([
            import('./state.js'),
            import('./postDisplay.js')
        ]);
        window.state = stateModule.state;
        window.showAllPosts = postDisplayModule.showAllPosts;
    }
    await fetchPost(postId);
}

const fetchPost = async (postId) => {
    const postElement = document.getElementById(`post-${postId}`);
    if (postElement) {
        console.log(postElement.getBoundingClientRect());
        postElement.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });
        postElement.classList.add('element-being-scrolled');
        setTimeout(() => {
            postElement.classList.remove('element-being-scrolled');
        }, 500);
    } else {
        try {
            await showPostsUntilFound(postId);
            console.log('Post not found');
        } catch (error) {
            console.error('Error fetching posts:', error);
        }
    }
};

const showPostsUntilFound = async (postId, searchTerm = '', topicName = '', append = true) => {
    while (state.hasMore && !document.getElementById(`post-${postId}`)) {
        await showAllPosts(searchTerm, topicName, append);
    }
    const postElement = document.getElementById(`post-${postId}`);
    if (postElement) {
        postElement.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });
    } else {
        console.log('Post not found after fetching all available posts.');
    }
};

const changeNotificationStatus = async (noti) => {
    const notiId = noti.id;
    console.log("Noti id: " + notiId);

    const notiDiv = document.getElementById(`noti-${notiId}`);
    if (!notiDiv) {
        console.error(`Element with ID noti-${notiId} not found`);
        return; // Exit the function if the element is not found
    }

    console.log(noti);
    console.log(notiDiv);

    // Find the button with the unread class, if it exists
    const deleteBtn = notiDiv.querySelector('.unread');

    // Check if deleteBtn exists before modifying it
    if (deleteBtn) {
        deleteBtn.textContent = 'Read';
        deleteBtn.classList.replace('unread', 'read');
    }

    notiDiv.style.backgroundColor = 'rgb(229 235 240)';

    const response = await fetch('/noti/changeToReadStatus', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            id: noti.id
        })
    });

    if (response.ok) {
        console.log("success");
    }
};

const notificationCount = async () => {
    const response = await fetch('/noti/totalNotiCount');
    if (!response.ok) {
        throw new Error("Error occurs at fetching Notification count");
    }
    const data = await response.json();
    const count = document.getElementById('count');
    count.innerText = data;
};

const deleteAllNotification = async () => {
    try {
        const response = await fetch("/noti/deleteAllNotification", { method: "DELETE" });
        if (response.ok) {
            console.log("All notifications deleted successfully");
        } else {
            console.error("Failed to delete all notifications");
        }
    } catch (error) {
        console.error("An error occurred:", error);
    }
    const count = document.getElementById('count');
    count.innerText = '0';
};
