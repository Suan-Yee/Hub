import {fetchLike} from "./giveLike.js";
import {renderContent} from "./renderVideo.js";
import {renderPhotos} from "./renderPhoto.js";
import {renderInteractionIcons} from "./renderIcon.js";

document.addEventListener('DOMContentLoaded', async () => {
    document.querySelector('.BMtweets-container').addEventListener('click', function (event) {
        const target = event.target;
        if (target.matches('[id^="likeIcon-"]')) {
            const postId = target.id.split('-')[1];
            fetchLike(postId, target);
        } else if (target.matches('[id^="bookMarkIcon-"]')) {
            const postId = target.id.split('-')[1];
            saveBookMark(postId, target);
        }
    });
    await fetchBookMarkPost();
    await fetchTopicForBookMark();
    await fetchTotalPost();
});

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
// const showBookMark = async () => {
//
//         const mainContent = document.querySelector('#mainContent');
//         if (mainContent.style.display === 'block') {
//             mainContent.style.display = 'none';
//         }
//         const bookMark = document.getElementById('bookMark');
//         bookMark.style.display = 'block';
//         await fetchBookMarkPost();
// }

const fetchBookMarkPost = async () => {
    const response = await fetch('/bookmark/all');

    if(!response.ok){
        throw new Error("Error!");
        // if(response.status === 404){
        //     throw new Error("There is no BookMark")
        // }
    }
    const data = await response.json();
    const tweetsContainer = document.querySelector('.BMtweets-container');
    tweetsContainer.innerHTML = '';
    data.forEach(post => {

        const postDiv = document.createElement('div');
        postDiv.classList.add('tweets');
        postDiv.id = `post-${post.id}`;
        const videoContent = post.content.videos.length > 0 ? renderContent(post) : '';
        const photoContent = post.content.images.length > 0 ? renderPhotos(post.content.images,post.id) : '';
        postDiv.innerHTML = `
       <div class="col-auto">
           <div class="user-pics"><img src="${post.photo}" alt="user3"></div>
       </div>
       <div class="user-content-box col">
            <!-- User profile and date -->
           <div class="d-flex justify-content-between user-names" style="height: 35px"> 
            
            <h1 class="full-name" style="margin-top: 5px">${post.user.name}.</h1>
                   ${post.groupName ? `<p class="grouptag">${post.groupName}</p>` : ''}
                   
                <p class="time me-3 ">${post.time}</p>
            </div>
            <!-- User profile and date End-->
            
            <!-- User Content image and text -->
            <div class="user-content">
                <p>${post.content.text}</p>
          
                ${videoContent || photoContent}
           </div>
            
           ${renderInteractionIcons(post)}
       </div>`;

        tweetsContainer.appendChild(postDiv);

        requestAnimationFrame(() => {
            postDiv.classList.add('fade-in');
            attachEventListenersToPost(postDiv, post.id);
        });
    })
}
const fetchTopicForBookMark = async () => {
    const response = await fetch('/bookmark/bookMarkTopic');
    const data = await response.json();

    const topic_container = document.querySelector('#trends-topic');

    data.forEach(topic => {
        const topicLi = document.createElement('li');
        topicLi.classList.add('nav-list');
        topicLi.innerHTML = `
      
        <div class="trend-list" >
        <p class="main-text">${topic.name}</p>
        <p class="sub-text">${topic.totalPost} posts</p>
        </div>
     
        `;
        topic_container.appendChild(topicLi);
    })
}
const fetchTotalPost = async () => {
    const response = await fetch('/bookmark/total');
    const data = await response.json();

    const text = document.getElementById('bookmarkList');
    text.textContent = `You have bookmarked ${data} posts`;
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

const deletePost= async (postId) => {
    try {
        const response = await fetch('/post/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                postId: postId
            })
        });
        const postToDelete = document.getElementById(`post-${postId}`);
        const deletePostModal = document.getElementById(`deletePostModal-${postId}`);
        const deletePostModalElement = bootstrap.Modal.getInstance(deletePostModal);
        const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
        console.log("Cancel Button ", cancelDeleteBtn);
        if(response.ok){
            cancelDeleteBtn.click();
            deletePostModalElement.hide();
            deletePostModalElement.dispose(); // Ensure the modal is disposed of properly
            showToast("Successfully Deleted","#595757");
            postToDelete.remove();
        }
    }catch (e) {
        console.error('Error : ', e);
    }
}