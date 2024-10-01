import {renderContent} from "./renderVideo.js";
import {renderPhotos} from "./renderPhoto.js";
import {renderInteractionIcons} from "./renderIcon.js";
import {fetchLike} from "./giveLike.js";

document.addEventListener('DOMContentLoaded', async () => {
    document.querySelector('.postsContainer').addEventListener('click', function (event) {
        const target = event.target;
        if (target.matches('[id^="likeIcon-"]')) {
            const postId = target.id.split('-')[1];
            fetchLike(postId, target);
        } else if (target.matches('[id^="bookMarkIcon-"]')) {
            const postId = target.id.split('-')[1];
            saveBookMark(postId, target);
        }else if (target.matches('[id^="delete-post-"]')) {
            const postId = target.id.split('-')[2];
            deletePost(postId);
        } else if (target.matches('[id^="update-post-"]')) {
            const postId = target.id.split('-')[2];
            // openUpdateModal(postId);
        }
    });
    const url = window.location.href;
    const regex = /userprofile\/(\d+)/;
    const match = url.match(regex);
    if (match) {
        const userId = match[1];
        await fetchSingleUserPosts(userId);
    }
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

const fetchSingleUserPosts = async (userId) => {
    const response = await fetch(`/post/findByUser/${userId}`);

    if(!response.ok){
        throw new Error("Error!");
        // if(response.status === 404){
        //     throw new Error("There is no BookMark")
        // }
    }
    const data = await response.json();
    const postsContainer = document.querySelector('.postsContainer');
    postsContainer.innerHTML = '';
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
                <div class="dropdown">
                        <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" style="padding-left: 10px;padding-bottom: 13px; color: black"></i>
                        <ul class="dropdown-menu" style="padding-right: 30px">
                            <div class="postBtnContainer">
                                <div class="postEditButton" id="update-post-${post.id}">Edit<i class="fa-solid fa-pen"></i></div>
                                <div class="postDeleteButton" data-bs-toggle="modal" data-bs-target="#deletePostModal-${post.id}" id="postDeleteButton">Delete<i class="fa-solid fa-trash"></i></div>
                            </div>
                        </ul>
                    </div>
            </div>
            <!-- User profile and date End-->
            
            <!-- User Content image and text -->
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

        postsContainer.appendChild(postDiv);

        requestAnimationFrame(() => {
            postDiv.classList.add('fade-in');
            attachEventListenersToPost(postDiv, post.id);
        });
    })
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