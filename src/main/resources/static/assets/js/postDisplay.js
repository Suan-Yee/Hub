import { saveBookMark } from './bookMark.js';
import { fetchLike } from './giveLike.js';
import { renderVideoContent } from './renderVideo.js';
import { renderPhotos } from './renderPhoto.js';
import { renderInteractionIcons } from './renderIcon.js';

document.addEventListener('DOMContentLoaded', () => {
        showAllPosts();
        showAllTopic();
        likePost();

    document.querySelector('.tweets-container').addEventListener('click', function(event) {
        const target = event.target;
        if (target.matches('[id^="likeIcon-"]')) {
            const postId = target.id.split('-')[1];
            fetchLike(postId, target);
        } else if (target.matches('[id^="bookMarkIcon-"]')) {
            const postId = target.id.split('-')[1];
                saveBookMark(postId, target);
        }
    });
})

//search Posts
document.querySelector('#search-box').addEventListener('input', debounce((event) => {
    const searchTerm = event.target.value;
    currentPage = 0;
    hasMore = true;
    showAllPosts(searchTerm,'',false);
}, 500));


let userId = null;
let currentPage = 0;
let loading = false;
let hasMore = true;

let stompClient = null;

//build url to fetch post
const buildUrl = (searchTerm, topicName, page = 0, size = 5) => {
    const params = new URLSearchParams();
    searchTerm && params.append('search', searchTerm);
    topicName && params.append('topicName', topicName);
    params.append('page', page);
    params.append('size', size);
    return `/post/all${params.toString() ? '?' + params : ''}`;
};
//fetch
const fetchData = async (url) => {
    const response = await fetch(url);
    if (!response.ok) {
        if (response.status === 404) return 'notFound';
        throw new Error('Network response was not ok.');
    }
    return response.json();
};
//render post
const renderPost = (post) => {

    const postDiv = document.createElement('div');
    postDiv.classList.add('tweets');
    postDiv.id = `${post.id}`;
    const videoContent = post.content.videos.length > 0 ? renderVideoContent(post) : '';
    const photoContent = post.content.images.length > 0 ? renderPhotos(post.content.images) : '';

    postDiv.innerHTML = `
        <div class="col-auto">
            <div class="user-pics"><img src="${post.photo}" alt="user3"></div>
        </div>
        <div class="user-content-box col">
            <!-- User profile and date -->
            <div class="user-names" style="height: 35px"> <!--d-flex justify-content-between-->
                <h1 class="full-name" style="margin-top: 5px">${post.user.name}.</h1>
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
    return postDiv;
};

//display posts
const showAllPosts = async (searchTerm = '', topicName = '', append = true) => {
    if (!hasMore || loading) return;

    loading = true;
    console.log("TOPIC NAME " + topicName);
    const url = buildUrl(searchTerm, topicName, currentPage, 5);

    try {
        showSkeletons();
        const minimumDisplayTime = new Promise(resolve => setTimeout(resolve, 500));
        const response = await fetchData(url);

        await minimumDisplayTime;

        const tweetsContainer = document.querySelector('.tweets-container');

        if (!append) {
            tweetsContainer.innerHTML = '';
        }

        if (response.content && response.content.length) {
            response.content.forEach(post => {
                const postDiv = renderPost(post);
                tweetsContainer.appendChild(postDiv);
                requestAnimationFrame(() => postDiv.classList.add('fade-in'));
            });
            currentPage++;
            hasMore = !response.last;
        } else {
            hasMore = false;
            if (!append) {
                tweetsContainer.innerHTML = `<p class="no-posts">No more posts to display.</p>`;
            }
        }
    } catch (error) {
    } finally {
        loading = false;
        removeSkeletons();
    }
};
window.addEventListener('scroll', () => {
    const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
    if (scrollTop + clientHeight >= scrollHeight - 5 && hasMore && !loading) {
        console.log('Attempting to fetch more posts due to scroll.');
        showAllPosts('', '', true);
    }
});
//webSocketForComment

function likePost() {

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        stompClient.subscribe('/user/queue/notifications', function(notification) {
            console.log("Notification received:", notification);
            displayToastNotification(notification);

            // showToast(notification.body,'black');
        });
    });
}
const displayToastNotification = (notification) => {

    let toastBody = document.querySelector('#liveToast .toast-body');
    toastBody.textContent = notification.body;

    const toastEl = document.getElementById('liveToast');
    let toast = new bootstrap.Toast(toastEl);
    toast.show();
}

function debounce(func, delay) {
    let debounceTimer;
    return function() {
        const context = this;
        const args = arguments;
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => func.apply(context, args), delay);
    }
}
function removeSkeletons() {
    const oldSkeletons = document.querySelectorAll('.skeleton-container');
    oldSkeletons.forEach(skeleton => skeleton.remove());
}
function showSkeletons(count = 5) {
    const tweets = document.querySelector('.tweets-container');

    // Create a container for skeletons to keep them separate from posts
    const skeletonContainer = document.createElement('div');
    skeletonContainer.className = 'skeleton-container';

    for (let i = 0; i < count; i++) {
        const skeleton = document.createElement('div');
        skeleton.className = 'skeleton';
        skeleton.innerHTML = `
            <div class="skeleton-img"></div>
            <div class="skeleton-text"></div>
            <div class="skeleton-text"></div>
            <div class="skeleton-text" style="width: 80%;"></div>
        `;
        skeletonContainer.appendChild(skeleton);
    }

    // Append the new skeleton container to the tweets container
    tweets.appendChild(skeletonContainer);
}
const showAllTopic = async  () => {
    const response = await fetch("/topic/all");
    const data = await response.json();

    const topic_container = document.querySelector('#trends-topic');

    data.forEach(topic => {
        const topicLi = document.createElement('li');
        topicLi.classList.add('nav-list');
        topicLi.innerHTML = `
      
        <div class="trend-list" >
        <p class="main-text">${topic.name}</p>
        <p class="sub-text">fake</p>
        </div>
     
        `;
        topic_container.appendChild(topicLi);

        topicLi.addEventListener('click', () => {
            console.log(`Topic ${topic.name} clicked`);
            document.querySelectorAll('.nav-list').forEach(item => item.classList.remove('focused'));
            topicLi.classList.add('focused');
            currentPage = 0;
            hasMore = true;
            showAllPosts('', topic.name, false);
        });
    })
}
const lazyLoadImage = () => {
    const lazyImages = document.querySelectorAll('.lazy-load');

    const loadHighResImage = (img) => {
        const highResImage = new Image();
        highResImage.src = img.getAttribute('data-src');
        highResImage.onload = () => {
            img.src = highResImage.src;
            img.classList.add('loaded');
        }
    }

    const imageObserver = new IntersectionObserver((entries,observer) => {
        entries.forEach(entry => {
            if(entry.isIntersecting){
                loadHighResImage(entry.target);
                observer.unobserve(entry.target);
            }
        });
    },{rootMargin: "0px 0px 50px 0px",threshold: 0.01});

    lazyImages.forEach(img => {
        imageObserver.observe(img);
    })
}


