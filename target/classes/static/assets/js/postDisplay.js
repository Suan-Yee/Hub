import { fetchLike } from './giveLike.js';
import { renderVideoContent,renderContent } from './renderVideo.js';
import { renderPhotos } from './renderPhoto.js';
import { renderInteractionIcons } from './renderIcon.js';
import { deletePost } from './postDelete.js';
import { openUpdateModal } from './postUpdate.js';
import { state } from "./state.js";

document.addEventListener('DOMContentLoaded', async () => {

    const scrollPosition = localStorage.getItem('scrollPosition');
    const savedPage = localStorage.getItem('currentPage');

    if (scrollPosition !== null && savedPage !== null) {
        const targetPage = parseInt(savedPage, 10);
        while (state.currentPage < targetPage) {
            await showAllPosts();
        }
        setTimeout(() => {
            window.scrollTo({
                top: parseInt(scrollPosition, 10),
                behavior: 'smooth'
            });
        }, 100);

        window.scrollTo(0, parseInt(scrollPosition, 10));
        console.log('Scrolling to position:', scrollPosition);

        localStorage.removeItem('scrollPosition');
        localStorage.removeItem('currentPage');
        console.log('Scroll position and page removed from localStorage');
    }
    await showAllPosts();
    await showAllTopic();
    await likePost();
    await getAllGroupForm();
    await fetchAnnouncement();

    console.log("Current pathname:", window.location.pathname);
    document.querySelector('.tweets-container').addEventListener('click', function (event) {
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
            openUpdateModal(postId);
        }
    });
    checkBirthday();

    const modal = document.getElementById("birthdayModal");
    const closeModal = document.getElementById("closeModal");

    closeModal.onclick = function() {
        modal.style.display = "none";
    }

    window.onclick = function(event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    }

})
function checkBirthday() {
    fetch('/user/check-birthday')
        .then(response => response.json())
        .then(data => {
            if (data.isBirthday) {
                showBirthdayModal(data.age);
            }
        })
        .catch(error => console.error('Error checking birthday:', error));
}

function showBirthdayModal(age) {
    const modal = document.getElementById("birthdayModal");
    const message = document.getElementById("birthdayMessage");
    message.textContent = `Happy ${age}th Birthday! 🎉`;

    modal.style.display = "block";

    const balloons = document.getElementById("balloons");
    for (let i = 0; i < 5; i++) {
        const balloon = document.createElement("div");
        balloon.className = "balloon";
        balloon.textContent = '🎈';
        balloons.appendChild(balloon);
    }
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
//search Posts
document.querySelector('#search-box').addEventListener('input', debounce(async (event) => {
    const searchTerm = event.target.value;
    state.currentPage = 0;
    state.hasMore = true;
    await showAllPosts(searchTerm, '', false);
}, 500));

let pollsData;
let stompClient = null;
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
let customDivAdded = false;
//display posts
export const showAllPosts = async (searchTerm = '', topicName = '', append = true) => {
    if (!state.hasMore || state.loading) return;

    state.loading = true;
    const url = buildUrl(searchTerm, topicName, state.currentPage, 5);

    try {
        showSkeletons();
        const minimumDisplayTime = new Promise(resolve => setTimeout(resolve, 500));
        const response = await fetchData(url);

        await minimumDisplayTime;

        const tweetsContainer = document.querySelector('.tweets-container');

        if (!append) {
            tweetsContainer.innerHTML = '';
            customDivAdded = false;
        }

        if (response.content && response.content.length) {
            let carouselWrapper = document.querySelector('.ivar');
            if (!carouselWrapper) {
                carouselWrapper = document.createElement('div');
                carouselWrapper.classList.add('ivar');
            }

            for (let index = 0; index < response.content.length; index++) {
                const post = response.content[index];
                const postDiv = renderPost(post);
                carouselWrapper.appendChild(postDiv);
                requestAnimationFrame(() => {
                    postDiv.classList.add('fade-in');
                    attachEventListenersToPost(postDiv, post.id);
                });

                if (!customDivAdded && ((state.currentPage * 5 + index + 1) % 5 === 0 || (state.currentPage * 5 + index + 1) % 7 === 0)) {
                    try {
                        const groupList = await fetchRandomGroup();

                        if (groupList.length > 0) {
                            const customDiv = document.createElement('div');
                            customDiv.classList.add('custom-div');
                            const text = document.createElement('h6');
                            text.style.marginTop = '10px';
                            text.style.color = '#727a84';
                            text.textContent = 'Discover New Groups';
                            customDiv.appendChild(text);

                            groupList.forEach((group, index) => {
                                const isLast = index === groupList.length - 1;
                                const groupContainer = document.createElement('div');
                                groupContainer.classList.add('randomGroupCard');
                                const defaultImageUrl = 'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';
                                const userImagesHtml = group.userImages.map(image => {
                                    const imageUrl = image ? image : defaultImageUrl;
                                    return `<img class="dog" src="${imageUrl}" alt="image">`;
                                }).join('');
                                groupContainer.innerHTML = `
                                    <div class="randomGroupImage">
                                        <img src="${group.groupPhoto}" alt="image">
                                    </div>
                                    <div class="box-container">
                                        <h1>${group.groupName}</h1>
                                        <h1 style="color: #83898e">${group.totalMember} members</h1>
                                        <div class="bottom">
                                            <div class="text-container">
                                                <div class="imageContainer">
                                                   ${userImagesHtml}
                                                </div>
                                                <div class="text"></div>
                                            </div>
                                        </div>
                                   </div>
                                `;
                                groupContainer.addEventListener('click', () => {
                                    window.location.href = `http://localhost:8080/groupPage/${group.id}`;
                                });

                                customDiv.appendChild(groupContainer);
                                if (isLast) {
                                    const hr = document.createElement('hr');
                                    customDiv.appendChild(hr);
                                }
                            });

                            carouselWrapper.appendChild(customDiv);
                            customDivAdded = true;
                        }
                    } catch (error) {
                        console.error('Failed to fetch random group:', error);
                    }
                }
            }
            tweetsContainer.appendChild(carouselWrapper);
            state.currentPage++;
            state.hasMore = !response.last;
        } else {
            state.hasMore = false;
            if (!append) {
                tweetsContainer.innerHTML = `<p class="no-posts">No more posts to display.</p>`;
            }
        }
    } catch (error) {
        console.error('Error fetching posts:', error);
    } finally {
        state.loading = false;
        removeSkeletons();
    }
};

const fetchRandomGroup = async () => {
    try {
        const response = await fetch('/randomGroup');
        if (!response.ok) {
            if (response.status === 404) {
                console.log('No groups found.');
                return [];
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        console.log(data);
        return data;
    } catch (error) {
        console.error('Failed to fetch random group:', error);
        return [];
    }
};

const renderPost = (post) => {
    const postDiv = document.createElement('div');
    postDiv.classList.add('tweets');
    postDiv.id = `post-${post.id}`;

    const videoContent = post.content.videos.length > 0 ? renderContent(post) : '';
    const photoContent = post.content.images.length > 0 ? renderPhotos(post.content.images, post.id) : '';
    const userImageUrl = post.photo != null ? post.photo : 'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';

    postDiv.innerHTML = `
        <div class="col-auto">
            <div class="user-pics"><img src="${userImageUrl}" alt="user3"></div>
        </div>
        <div class="user-content-box col">
            <!-- User profile and date -->
            <div class="d-flex justify-content-between user-names" style="height: 35px"> 
                <a href="userprofile/${post.user.id}"><h1 class="full-name" style="margin-top: 5px">${post.user.name}.</h1></a>
                ${post.groupName ? `<p class="grouptag">${post.groupName}</p>` : ''}
                <div class="d-flex align-items-center">
                    <p class="time me-3">${post.time}</p>
                    <div class="dropdown">
                        <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" style="padding-left: 10px;padding-bottom: 13px"></i>
                        <ul class="dropdown-menu">
                            <div class="postBtnContainer">
                                ${post.owner ? `
                                    <div class="postEditButton" id="update-post-${post.id}">Edit<i class="fa-solid fa-pen" style="margin-left: 20px;color: #3012c7;"></i></div>
                                ` : ''}
                                ${post.admin || post.owner ? `
                                    <div class="postDeleteButton" data-bs-toggle="modal" data-bs-target="#deletePostModal-${post.id}" id="postDeleteButton">Delete<i class="fa-solid fa-trash"  style="margin-left: 20px;color: #e82b55;" ></i></div>
                                ` : ''}
                            </div>
                        </ul>
                    </div>
                </div>
            </div>
            <!-- User profile and date End -->
            
            <!-- User Content image and text -->
            <div class="user-content">
                <p class="contentText">${post.content.text}</p>
                <div id="mentions-container-${post.id}"></div>
                <div id="topicContainer-${post.id}"></div>
                ${videoContent || photoContent}
            </div>
            
            ${renderInteractionIcons(post)}

            <!-- Modal HTML placed within the post container -->
            ${post.admin || post.owner ? `
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
            ` : ''}
        </div>`;

    const mentionsContainer = postDiv.querySelector(`#mentions-container-${post.id}`);
    post.mentionUserList.forEach((mention, index) => {
        const mentionLink = document.createElement('a');
        mentionLink.href = `/userprofile/${mention.userId}`;

        const mentionSpan = document.createElement('span');
        mentionSpan.classList.add('userMention');
        mentionSpan.style.color = '#0d6efd';
        mentionSpan.textContent = `@${mention.userName}${index < post.mentionUserList.length - 1 ? ',' : ''}`;

        mentionLink.appendChild(mentionSpan);
        mentionsContainer.appendChild(mentionLink);
    });

    const topicContainer = postDiv.querySelector(`#topicContainer-${post.id}`);
    post.topicList.forEach((topic, index) => {
        const spanTopic = document.createElement('span');
        spanTopic.classList.add('topicName');
        spanTopic.style.color = '#0d6efd';
        spanTopic.style.cursor = 'pointer';
        spanTopic.textContent = `#${topic}${index < post.topicList.length - 1 ? ',' : ''}`;

        spanTopic.addEventListener('click', () => {
            let backUrlHolder = document.getElementById('backUrlHolder');

            if (!backUrlHolder) {
                backUrlHolder = document.createElement('div');
                backUrlHolder.id = 'backUrlHolder';
                backUrlHolder.innerHTML = `<i class="fa-solid fa-arrow-left-long backArrow"></i>`;
                backUrlHolder.addEventListener('click', () => {
                    const tweetsContainer = document.querySelector('.tweets-container');
                    tweetsContainer.innerHTML = '';

                    state.currentPage = 0;
                    state.hasMore = true;
                    showAllPosts('', '', false);

                    backUrlHolder.style.display = 'none';
                    const home = document.querySelector('.home');
                    home.querySelector('h1').text
                    home.querySelector('h1').textContent = 'Home';
                    document.getElementById('poll').style.display = 'block';
                    document.querySelectorAll('.nav-list').forEach(item => item.classList.remove('focused'));

                    history.back();
                });

                const mainContent = document.querySelector('#mainContent');
                const firstChild = mainContent.firstChild;
                mainContent.insertBefore(backUrlHolder, firstChild);
            } else {
                backUrlHolder.style.display = 'block';
            }

            const home = document.querySelector('.home');
            home.querySelector('h1').textContent = topic;
            document.getElementById('poll').style.display = 'none';
            document.querySelectorAll('.nav-list').forEach(item => item.classList.remove('focused'));
            spanTopic.classList.add('focused');
            state.currentPage = 0;
            state.hasMore = true;
            showAllPosts('', topic, false);

            history.pushState({ topicName: topic }, '', `?topicName=${topic}`);
        });

        topicContainer.appendChild(spanTopic);
    });

    return postDiv;
};


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

 window.addEventListener('scroll', () => {
     const {scrollTop, scrollHeight, clientHeight} = document.documentElement;
     if (scrollTop + clientHeight >= scrollHeight - 5 && state.hasMore && !state.loading) {
         console.log('Attempting to fetch more posts due to scroll.');
         showAllPosts('', '', true);
     }
 });

//webSocketForComment

    function likePost() {

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            stompClient.subscribe('/user/queue/notifications', function (notification) {
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
        return function () {
            const context = this;
            const args = arguments;
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => func.apply(context, args), delay);
        }
    }

    const showAllTopic = async () => {
        const response = await fetch("/topic/all");
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

            topicLi.addEventListener('click', () => {
                let backUrlHolder = document.getElementById('backUrlHolder');

                if (!backUrlHolder) {
                    backUrlHolder = document.createElement('div');
                    backUrlHolder.id = 'backUrlHolder';
                    backUrlHolder.innerHTML = `<i class="fa-solid fa-arrow-left-long backArrow"></i>`;
                    backUrlHolder.addEventListener('click', () => {
                        // Clear the tweets container before showing all posts
                        const tweetsContainer = document.querySelector('.tweets-container');
                        tweetsContainer.innerHTML = ''; // This line clears the previous posts

                        // Reset state and posts when back button is clicked
                        state.currentPage = 0;
                        state.hasMore = true;
                        showAllPosts('', '', false);

                        // Hide the back button and reset the home title
                        backUrlHolder.style.display = 'none';
                        const home = document.querySelector('.home');
                        home.querySelector('h1').textContent = 'Home';
                        document.getElementById('poll').style.display = 'block';
                        document.querySelectorAll('.nav-list').forEach(item => item.classList.remove('focused'));

                        // Navigate back to the initial state
                        history.back();
                    });
                    const mainContent = document.querySelector('#mainContent');
                    const firstChild = mainContent.firstChild;
                    mainContent.insertBefore(backUrlHolder, firstChild);
                } else {
                    backUrlHolder.style.display = 'block';
                }

                const home = document.querySelector('.home');
                home.querySelector('h1').textContent = topic.name;
                document.getElementById('poll').style.display = 'none';
                document.querySelectorAll('.nav-list').forEach(item => item.classList.remove('focused'));
                topicLi.classList.add('focused');
                state.currentPage = 0;
                state.hasMore = true;
                showAllPosts('', topic.name, false);

                history.pushState({ topicName: topic.name }, '', `?topicName=${topic.name}`);
            });
        });
    };
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

        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    loadHighResImage(entry.target);
                    observer.unobserve(entry.target);
                }
            });
        }, {rootMargin: "0px 0px 50px 0px", threshold: 0.01});

        lazyImages.forEach(img => {
            imageObserver.observe(img);
        })

}

export const attachEventListenersToPost = (postElement,postId) => {
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

const topicPill = document.getElementById('topicPill');
const groupPill = document.getElementById('groupPill');

topicPill.addEventListener('click', () => togglePills('trends'));
groupPill.addEventListener('click', () => togglePills('groups'));

const togglePills = (pill) => {
    const trends = document.querySelector('.trends');
    const groups = document.querySelector('.groups');
    const events = document.querySelector('.eventContainer');
    if (pill === 'trends') {
        trends.style.display = 'block';
        groups.style.display = 'none';
        events.style.display = 'block';
    } else if (pill === 'groups') {
        trends.style.display = 'none';
        groups.style.display = 'block';
        events.style.display = 'none';
    }
}

const getAllGroupForm = async () => {

    const response = await fetch('/groupForm');

    if (response.status === 404) {
        console.error('Error: Groups not found.');
        return;
    }

    const data = await response.json();

    const parentContainer = document.querySelector('.parentDiv');

    data.forEach(group => {
        const groupContainer = document.createElement('div');
        groupContainer.classList.add('groupCardcontainer');
        const defaultImageUrl = 'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';
        const userImagesHtml = group.userImages.map(image => {
            const imageUrl = image ? image : defaultImageUrl;
            return `<img class="dot" src="${imageUrl}">`;
        }).join('');
        groupContainer.innerHTML = `
            <div class="groupImage">
                                <img src=${group.groupPhoto}>
                                </div>
                            <div class="box-container">
                                <h1 class="h1tag">${group.groupName}</h1>

                                <div class="bottom">
                                    <div class="text-container">
                                        <div class="imageContainer">
                                        ${userImagesHtml}
                                        </div>
                                        <div class="text">${group.totalPost}</div>
                                    </div>
                                    <button class="viewGroup">View</button>
                                </div>
                            </div>
        `;
        parentContainer.appendChild(groupContainer);
    })
}
function fetchAnnouncement() {
    fetch('/announcement/showAnnouncement')
        .then(response => response.json())
        .then(users => {
            const cardContainer = document.querySelector('#eventCarousel');
            let isActiveClassAdded = false; // Track if the 'active' class has been added

            users.forEach(data => {
                const StartDate = new Date(data.announcementStartDate);
                const formattedStartDay = StartDate.toLocaleDateString('en-GB', { day: 'numeric' });
                const formattedStartMonth = StartDate.toLocaleDateString('en-GB', { month: 'short' });

                const EndDate = new Date(data.announcementEndDate);
                const formattedEndDay = EndDate.toLocaleDateString('en-GB', { day: 'numeric' });
                const formattedEndMonth = EndDate.toLocaleDateString('en-GB', { month: 'short' });

                const cardDiv = document.createElement('div');
                cardDiv.classList.add('col-lg-4', 'carousel-item');
                if (!isActiveClassAdded) {
                    cardDiv.classList.add('active');
                    isActiveClassAdded = true; // Set the flag to true after adding 'active' class to the first element
                }
                cardDiv.innerHTML = `
                    <div class="card card-margin cardIndexCustom" style="height: 180px; border-radius: 13px; width: 333px">
                        <div class="card-body pt-0" style="float: top; margin-top: 5px; height: 200px">
                            <div class="widget-49">
                                <div class="widget-49-title-wrapper">
                                    <div class="widget-49-date-primary" style="margin-top: 10px;">
                                        <span class="widget-49-date-day">${formattedStartDay}</span>
                                        <span class="widget-49-date-month">${formattedStartMonth}</span>
                                    </div>
                                    <div class="widget-49-meeting-info">
                                        <span class="widget-49-pro-title">${data.title}</span>
                                        <span class="widget-49-meeting-time">${formattedStartDay} ${formattedStartMonth} To ${formattedEndDay} ${formattedEndMonth}</span>
                                    </div>
                                </div>
                                <div class="widget-49-meeting-item">
                                    <p class="post">${data.message}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
                cardContainer.appendChild(cardDiv);
            });
        })
        .catch(error => console.error('Error fetching users:', error));
}

const markAnswer = (poll, answerId, i) => {
    poll.selectedAnswer = +i;

    const answers = document.querySelectorAll(`.poll[data-id="${poll.id}"] .answers .answer`);
    answers.forEach((answer) => {
        answer.classList.remove("selected");
        answer.classList.add("disabled");
        answer.onclick = null;
    });

    const selectedAnswer = document.querySelector(`.poll[data-id="${poll.id}"] .answers .answer:nth-child(${i + 1})`);
    if (selectedAnswer) {
        selectedAnswer.classList.add("selected");
    }
    showRemoveVoteButton(answerId,poll);
    showResults(poll);
};
let userSelectedAnswerIndex = -1;
const checkAnswerUserVoted = async (poll) => {
    const response = await fetch('/check-answer-user-voted?pollId=' + poll.id);
    const data = await response.json();
    if ("optionIdUserVoted" in data) {
        let userVotedOptionId = data.optionIdUserVoted;

        if (userVotedOptionId !== null) {
            let selectedAnswerIndex = Object.keys(poll.answers).findIndex(key => parseInt(key) === userVotedOptionId);
            if (selectedAnswerIndex !== -1) {
                userSelectedAnswerIndex =selectedAnswerIndex;
                poll.selectedAnswer = selectedAnswerIndex;
                markAnswer(poll, userVotedOptionId, selectedAnswerIndex);
            }
        }
    }
}

const getPolls = async () => {
    const response = await fetch("/get-all-poll");
    const data = await response.json();
    pollsData = data;
    console.log("Poll Data", pollsData);
    data.forEach(pollOption => {
        const poll = {
            id: pollOption.id,
            question: pollOption.question,
            answers: pollOption.answers,
            pollCount: pollOption.pollCount,
            answersWeight: pollOption.answersWeight,
            expiredDate : pollOption.expiredDate,
            userId : pollOption.user.id,
            userImage :pollOption.user.photo,
            userName :pollOption.user.name,
            isLoginUserPoll:pollOption.isLoginUserPoll,
            selectedAnswer: -1
        };
        const pollHTML = createPollHTML(poll);
        appendPollToContainer(pollHTML);
        checkAnswerUserVoted(poll);
    });
};

const poll = document.getElementById('poll');
const home = document.getElementById('home');
let fetchPoll = false;

poll.addEventListener('click',async  () => {
    const tweetsContainer = document.querySelector('.tweets-container');
    tweetsContainer.style.display = 'none';

    const pollsContainer = document.querySelector('.polls-container');
    pollsContainer.style.display = 'block';
    home.classList.remove('activeTabPost');
    poll.classList.add('activeTabPost');

    if(!fetchPoll){
        await getPolls();
        fetchPoll = true;
    }


})

home.addEventListener('click',async  () => {
    const tweetsContainer = document.querySelector('.tweets-container');
    tweetsContainer.style.display = 'block';
    poll.classList.remove('activeTabPost');
    home.classList.add('activeTabPost');

    const pollsContainer = document.querySelector('.polls-container');
    pollsContainer.style.display = 'none';

})
const createPollHTML = (poll) => {
    const userImage = poll.userImage !=null ? poll.userImage :'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';
    const userName = poll.userName;
    let isLoginUserPoll = poll.isLoginUserPoll;
    let answersHTML = "";
    const trashIconHTML = isLoginUserPoll ? `<i class="fa-regular fa-trash-can trash-button" id="poll-${poll.id}" title="delete"></i>` : '';
    for (const [answerId, answer] of Object.entries(poll.answers)) {
        answersHTML += `
            <div class="answer" data-answer-id="${answerId}" data-poll-id="${poll.id}">
                ${answer}
                <span class="percentage-bar"></span>
                <span class="percentage-value"></span>
            </div>`;
    }
    return `
        <div class="poll" style="background-color: #eaecef" data-id="${poll.id}">
        <div class="poll-user-profile">
        <img src="${userImage}">
        <a href="userprofile/ + ${poll.userId}"><div class="poll-user-name">${userName}</div></a>
           ${trashIconHTML}
</div>
            <div class="question">${poll.question}
                <div class="pollInfo-container">
                    <div class="totalVote pollDetailResult" data-toggle="modal" data-target="#pollTotalVoteResult" data-poll-id="${poll.id}">
                           ${poll.pollCount} votes
                    </div>
                    <div class="expiration-date">${poll.expiredDate}</div>
                </div>
            </div>
            <div class="answers">${answersHTML}</div>
        </div>`;
};

document.addEventListener('click', function (event) {
    if (event.target.classList.contains('pollDetailResult')) {
        const pollId = event.target.dataset.pollId;
        const modal = document.getElementById('pollTotalVoteResult');
        const modalInstance = new bootstrap.Modal(modal); // Assuming you are using Bootstrap 5
        modalInstance.show();
        const poll = pollsData.find(poll => poll.id === parseInt(pollId));
        const modalTitle = modal.querySelector('.modal-title');
        modalTitle.innerText = poll.pollCount +" votes";

        const pollOptionLi = modal.querySelector('.pollOptionLi');
        console.log("PollOptionLi",pollOptionLi);
        pollOptionLi.innerHTML = '';

        // Loop through the answers and create list items
        Object.entries(poll.answers).forEach(([answerId, answer], index) => {
            const li = document.createElement('li');
            li.innerText = `${answer} (${poll.answersWeight[index] || 0})`;
            li.dataset.optionKey = answerId;  // Store the key of the answer
            li.dataset.pollId = pollId;  // Store the pollId
            pollOptionLi.appendChild(li);
        });

        // Auto-select the first list item
        const firstLi = pollOptionLi.querySelector('li');
        if (firstLi) {
            firstLi.classList.add('activeLi');  // Highlight the first item
            firstLi.click();  // Trigger click event for the first item to load its details
        }
    }
});
document.querySelector('.pollOptionLi').addEventListener('click',function (event){
    const modal = document.getElementById('pollTotalVoteResult');
    const pollContentCount = modal.querySelector('.pollCount-content');
    const pollOptionLi = modal.querySelector('.pollOptionLi');
    if(event.target.tagName ==='LI'){
        // Remove focus from all items
        Array.from(pollOptionLi.children).forEach(item => item.classList.remove('activeLi'));

        // Highlight the clicked item
        event.target.classList.add('activeLi');
        const optionId = event.target.dataset.optionKey;
        console.log('OptionId :',optionId);
        fetch('get-votes-for-poll-option?answerId='+optionId,{
            method:'GET'
        })
            .then(response=>response.json())
            .then(users=>{
                if(users!==null && users.length >0){
                    pollContentCount.innerHTML = '';
                    let tableContent = `
                    <table class="table table-striped">
                            
                   `;

                    users.forEach(user=>{
                        const photoUrl = user.photo ? user.photo : "http://res.cloudinary.com/dwdplsd2c/image/upload/v1713536112/fllinoozg9hh1ghk5mef.png";
                        tableContent+=`
                        <tr>
                            <td><img class="rounded-circle me-2" height="50" width="50" src="${photoUrl}" alt="${user.name}"> ${user.name}</td>
                        </tr>
                       `
                    });
                    tableContent += `
                        </tbody>
                    </table>
                `;
                    pollContentCount.innerHTML = tableContent;
                }
                else{
                    pollContentCount.innerHTML = '';
                    const notFoundMessage = document.createElement('h4');
                    notFoundMessage.innerText = 'No user are found for this option!!!';
                    pollContentCount.appendChild(notFoundMessage);
                }
            });
    }
});

const appendPollToContainer = (pollHTML) => {
    const pollsContainer = document.querySelector('.polls-container');
    pollsContainer.insertAdjacentHTML('beforeend', pollHTML);
    const newPollElement = pollsContainer.lastChild;
    setupAnswerEventListeners(newPollElement);
};
let initializedPolls = new Set();

const setupAnswerEventListeners = (pollElement) => {
    const pollId = pollElement.dataset.id;

    if (!initializedPolls.has(pollId)) {
        const answers = pollElement.querySelectorAll('.answer');
        answers.forEach(answer => {
            // Define click handler function
            const clickHandler = async () => {
                const answerId = answer.getAttribute('data-answer-id');

                await markAnswerAfterClick(answerId, pollId);
            };

            // Add event listener
            answer.addEventListener('click', clickHandler);
        });

        initializedPolls.add(pollId);
    }
};


const markAnswerAfterClick = async (answerId, pollId) => {
    const poll = pollsData.find(poll => poll.id === parseInt(pollId));
    console.log("markAnswerAfterClick Poll :",poll)
    const selectedAnswerIndex = Object.keys(poll.answers).findIndex(key => key === answerId);
    poll.selectedAnswer = +selectedAnswerIndex;

    const answers = document.querySelectorAll(`.poll[data-id="${poll.id}"] .answers .answer`);
    answers.forEach((answer) => {
        answer.classList.add("disabled");
    });

    const selectedAnswer = document.querySelector(`.poll[data-id="${poll.id}"] .answer:nth-child(${poll.selectedAnswer + 1})`);
    if (selectedAnswer) {
        selectedAnswer.classList.add("selected");
    }
    let result = await saveUserVotedAnswer(poll, answerId);
    if (result === true) {
        const answerIndex = Object.keys(poll.answers).findIndex(key => key === answerId);
        poll.answersWeight[answerIndex] +=1;
        poll.pollCount +=1;
        showResultsAfterClick(poll);
        showRemoveVoteButton(answerId, poll);
    }
};

const saveUserVotedAnswer = async (poll, answerId) => {
    const response = await fetch('/save-user-voted-answer', {
        method: 'POST',
        headers: {
            'Content-type': 'application/json'
        },
        body: JSON.stringify({ pollId: poll.id, answerId: answerId })
    });
    let result = await response.json();
    return result;
};

function showRemoveVoteButton(answerId, poll) {

    const existingRemoveButton = document.querySelector(`.poll[data-id="${poll.id}"] .remove-vote`);

    // If a remove vote button exists, remove it
    if (existingRemoveButton) {
        existingRemoveButton.remove();
    }
    const removeVoteButton = document.createElement('button');
    removeVoteButton.textContent = 'Remove Vote';
    removeVoteButton.classList.add('remove-vote');
    removeVoteButton.addEventListener('click', () => removeVote(answerId, poll.id));

    const answersContainer = document.querySelector(`.poll[data-id="${poll.id}"] .answers`);
    answersContainer.insertAdjacentElement('beforeend', removeVoteButton);
}
// const setupRemoveVoteListener = (pollElement) => {
//     const removeBtn = pollElement.querySelector('.remove-vote');
//     if (removeBtn) {
//         removeBtn.addEventListener('click', () => handleRemoveVote(pollElement.dataset.id));
//     }
// };
const removeVote = async (answerId, pollId) => {
    try {
        const response = await fetch('/remove-user-voted-answer', {
            method: 'POST',
            headers: { 'Content-type': 'application/json' },
            body: JSON.stringify({
                answerId: answerId,
                pollId: pollId
            })
        });

        if (!response.ok) {
            throw new Error('Network request failed');
        }

        const data = await response.json();
        if (data.success) {
            handleRemoveVote(pollId,answerId);
        } else {
            console.error('Failed to remove the vote');
        }
    } catch (error) {
        console.error('Error while removing vote:', error);
    }
};
const handleRemoveVote = (pollId, answerId) => {
    const pollElement = document.querySelector(`.poll[data-id="${pollId}"]`);
    let pollObj = pollsData.find(p => p.id == pollId);
    console.log("Poll After click remove button ", pollObj);
    if(userSelectedAnswerIndex !== -1){ //check user have already voted or not
        if (pollObj.answersWeight[userSelectedAnswerIndex] < 0) {
            pollObj.answersWeight[userSelectedAnswerIndex] = 0;
        }else  pollObj.answersWeight[userSelectedAnswerIndex] -= 1;
        userSelectedAnswerIndex=-1; //after click remove button assign 0 to selectedAnswerIndex
    }else{
        const answerIndex = Object.keys(pollObj.answers).findIndex(key => key === answerId);
        pollObj.answersWeight[answerIndex] -= 1;
        if (pollObj.answersWeight[answerIndex] < 0) {
            pollObj.answersWeight[answerIndex] = 0;
        }
    }

    pollObj.selectedAnswer = -1;
    pollObj.pollCount -= 1;

    const totalVoteElement = pollElement.querySelector('.totalVote');
    const currentVoteCount = parseInt(totalVoteElement.textContent);
    if (!isNaN(currentVoteCount) && currentVoteCount > 0) {
        totalVoteElement.textContent = `${currentVoteCount - 1} votes`;
    }

    const answers = pollElement.querySelectorAll('.answer');
    answers.forEach(answer => {
        answer.classList.remove('selected', 'disabled');
        const percentageBar = answer.querySelector('.percentage-bar');
        const percentageValue = answer.querySelector('.percentage-value');
        percentageBar.style.width = '0%';
        percentageValue.textContent = '';

    });

    const removeBtn = pollElement.querySelector('.remove-vote');
    removeBtn.remove();
    setupAnswerEventListeners(pollElement);

    // showResultsAfterClick(pollId);
};

const showResults = (poll) => {
    const totalVotes = poll.pollCount;
    const answers = document.querySelectorAll(`.poll[data-id="${poll.id}"] .answers .answer`);

    answers.forEach((answer, i) => {
        let percentage = 0;
        if (totalVotes > 0) {
            percentage = Math.round((poll.answersWeight[i]) * 100 / totalVotes);
        }
        answer.querySelector(".percentage-bar").style.width = percentage + "%";
        answer.querySelector(".percentage-value").innerText = percentage + "%";
    });
};

const showResultsAfterClick = (poll) => {
    const totalVotes = poll.pollCount;
    const answers = document.querySelectorAll(`.poll[data-id="${poll.id}"] .answers .answer`);
    const totalVoteDiv = document.querySelector(`.poll[data-id="${poll.id}"] .pollInfo-container .totalVote`);
    totalVoteDiv.innerHTML = '';
    totalVoteDiv.innerHTML = totalVotes +"votes";
    answers.forEach((answer, i) => {
        let percentage = 0;
        if (i === poll.selectedAnswer) {
            percentage = Math.floor((poll.answersWeight[i]) * 100 / (totalVotes));
        } else {
            percentage = Math.floor((poll.answersWeight[i]) * 100 / (totalVotes));
        }
        const  percentageBar= answer.querySelector(".percentage-bar");
        if(percentageBar){
            percentageBar.style.background = "#ccd8f1";
            percentageBar.style.width = percentage +"%";
        }
        answer.querySelector(".percentage-value").innerText = percentage + "%";
    });
};
document.addEventListener('DOMContentLoaded', function () {
    // Global scope within this function
    const pollContainer = document.querySelector('.polls-container');
    const deleteModal = document.getElementById('deletePoll');
    const deletePollModal = new bootstrap.Modal(deleteModal);
    const confirmDeleteButton = document.getElementById('confirmDeleteButton');
    let currentPollId = null; // Variable accessible throughout this function

    pollContainer.addEventListener('click', function (event) {
        const target = event.target;
        if (target.matches('[id^="poll-"]')) {
            currentPollId = target.id.split('-')[1]; // Update the currentPollId
            console.log(currentPollId); // Log the current poll ID
            deletePollModal.show(); // Show the modal
        }
    });

    confirmDeleteButton.addEventListener('click', async function () {
        if (currentPollId) { // Check if currentPollId is set
            try {
                await deletePoll(currentPollId); // Use the currentPollId
                deletePollModal.hide(); // Hide the modal after deletion
                showToast('Delete Successful', '#0073ff'); // Show success message
                document.querySelector(`[data-id="${currentPollId}"]`).remove();
            } catch (error) {
                console.error(error); // Log any errors
                showToast('Delete Failed', '#FF0000'); // Show error message
            }
        }
    });

    const deletePoll = async (pollId) => {
        console.log("Attempting to delete poll with ID:", pollId); // Log the poll ID being deleted
        const response = await fetch('/delete-poll', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id:pollId }) // Send the pollId in the request body
        });

        if (!response.ok) {
            throw new Error('Failed to delete');
        }

        const data = await response.json();
        console.log("Delete Poll ", data); // Log the response data
    };
});