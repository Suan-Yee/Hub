import {fetchLike} from "./giveLike.js";
import {renderVideoContent} from "./renderVideo.js";
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
        const videoContent = post.content.videos.length > 0 ? renderVideoContent(post) : '';
        const photoContent = post.content.images.length > 0 ? renderPhotos(post.content.images) : '';
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
