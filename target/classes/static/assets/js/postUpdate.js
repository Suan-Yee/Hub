import {renderInteractionIcons} from "./renderIcon.js";
import {renderVideoContent,renderContent} from "./renderVideo.js";
import {renderPhotos} from "./renderPhoto.js";
import {state} from "./state.js";
import {showAllPosts} from "./postDisplay.js";

document.addEventListener('DOMContentLoaded', function() {

    const deleteButton = document.getElementById('objectDelete');
    deleteButton.addEventListener('click', () => {
        const activeItem = document.querySelector('.carousel-item.active');
        if (activeItem) {
            const img = activeItem.querySelector('img');
            const video = activeItem.querySelector('video');
            if (img) {
                const imageSrc = img.src;
                deleteMedia(imageSrc);
            } else if (video) {
                const videoSrc = video.src;
                deleteMedia(videoSrc);
            } else {
                console.error('No image or video found in the active carousel item.');
            }
        } else {
            console.error('No active carousel item found.');
        }
    });
    const fileInput = document.getElementById('fileInput');
    fileInput.addEventListener('change', handleFileSelection);

    document.getElementById('objectAdd').addEventListener('click', function() {
        document.getElementById('fileInput').click();
    });

    const form = document.getElementById('post-update-form');

    form.addEventListener('submit', async function(event) {
        event.preventDefault();

        const id = document.getElementById('updateId').value;
        const text = document.getElementById('post-body').value;
        const fileInputs = document.querySelectorAll('.files');
        const existingImagesInputs = document.querySelectorAll('input[name="existingImages"]');

        console.log("Post Id " + id);
        console.log(text);
        console.log(existingImagesInputs);
        console.log("FileInput ",fileInputs);

        const formData = new FormData();
        formData.append('exitId', id);
        formData.append('updateText', text);

        removedTags.forEach(tag => {
            formData.append('removeTag', tag);
        });

        removedMentionUsers.forEach(user => {
            formData.append('removeUser', user);
        });

        removedImages.forEach(image => {
            formData.append('removedImages', image);
            console.log(removedImages)
        });

        existingImagesInputs.forEach(input => {
            formData.append('existingImages', input.value);
        });

        newMentionUsers.forEach(user => {
            formData.append('mentionStaff', user);
        });


        fileInputs.forEach(input => {
            const files = input.files;
            console.log("Files ",files);
            for (let i = 0; i < files.length; i++) {
                console.log("New Files ",files[i]);
                formData.append('updateFiles', files[i]);
            }
        });
        const modal = document.getElementById('updateModal');
        $(modal).modal('hide');
        $('#loadingModalForPostUpdate').modal('show');

        try {
            const response = await fetch('/post/update', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error('Network response was not OK!');
            } else {
                $('#loadingModalForPostUpdate').modal('hide');
                const data = await response.json();
                console.log(data);
                changingPost(data);
                showToast('Update Successful','#0073ff');
            }
        } catch (e) {
            console.error('Error', e);
        }
    });

    const inputElement = document.getElementById('tagButton');
    inputElement.addEventListener('focus', () => {
        if (!inputElement.value.startsWith('@')) {
            inputElement.value = '@' + inputElement.value;
        }
        userMention(inputElement);
    });
});
function handleFileSelection(event) {
    const files = event.target.files;
    handleFiles(files);
}
function handleFiles(files) {
    const dropContainer = document.querySelector('#drop-container');
    const carousel = document.getElementById('carouselExample');

    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (file.type.startsWith('image/')) {
            handleImage(file);
        } else if (file.type.startsWith('video/')) {
            handleVideo(file);
        } else {
            console.log('Unsupported file type:', file.type);
            continue;
        }
        dropContainer.style.display = 'none';
        carousel.style.display = 'block';
    }
    updateCarouselDisplay();
}
function handleImage(file) {
    const img = document.createElement('img');
    img.src = URL.createObjectURL(file);
    img.classList.add('d-block', 'w-100','imageCreate');
    img.onload = function() {
        URL.revokeObjectURL(this.src);
    };

    const carouselItem = document.createElement('div');
    carouselItem.classList.add('carousel-item');
    carouselItem.appendChild(img);

    const imageContainer = document.querySelector('.carousel-inner');
    imageContainer.appendChild(carouselItem);

    updateCarouselDisplay();
}

function handleVideo(file) {
    const video = document.createElement('video');
    video.src = URL.createObjectURL(file);
    video.classList.add('d-block', 'w-100');
    video.controls = true;
    video.onload = function() {
        URL.revokeObjectURL(this.src);
    };

    const carouselItem = document.createElement('div');
    carouselItem.classList.add('carousel-item');
    carouselItem.appendChild(video);

    const imageContainer = document.querySelector('.carousel-inner');
    imageContainer.appendChild(carouselItem);

    document.getElementById('objectAdd').style.display = 'none'; // Hide the Add button

    updateCarouselDisplay();
}

let removedTags = [];
let removedMentionUsers = [];
let newMentionUsers = [];
let removedImages = [];

const userMention = (inputElement) => {
    const suggestions = document.getElementById('suggestions');
    const mentionProfile = document.getElementById('mentionProfile');

    if (inputElement) {
        inputElement.addEventListener('input', (e) => {
            const inputVal = e.target.value;
            const lastAtPos = inputVal.lastIndexOf('@');
            const search = inputVal.substring(lastAtPos + 1);

            if (lastAtPos === -1 || search.length === 0) {
                suggestions.innerHTML = '';
                suggestions.style.display = 'none';
                mentionProfile.style.display = 'none';
                return;
            }

            if (lastAtPos !== -1 && search.length > 1) {
                fetch(`/user/search?query=${search}`)
                    .then(response => response.json())
                    .then(data => {
                        suggestions.innerHTML = '';

                        if (data.length > 0) {
                            data.forEach(user => {
                                const div = document.createElement('div');
                                div.classList.add('userMentionName');
                                div.id = user.staffId;
                                const userImage = document.createElement('img');
                                userImage.src = user.photo;
                                userImage.alt = user.name;
                                userImage.classList.add('userMentionProfile');

                                const userName = document.createTextNode(user.name);
                                const mentionContainer = document.querySelector('.mention-list');

                                div.appendChild(userImage);
                                div.appendChild(userName);
                                div.onclick = () => {
                                    // inputElement.value = inputVal.substring(0, lastAtPos) + `@${user.name} `;
                                    // inputElement.classList.add('mention-selected');

                                    const container = document.createElement('div');
                                    container.id = `mentionUser-${user.id}`;
                                    container.classList.add('mentionContainer')
                                    container.innerHTML =`
                                        <span class="userList">${user.name}</span>
                                        <span class="remove-btn">x</span>
                                    `;

                                    mentionContainer.style.display = 'flex'
                                    mentionContainer.appendChild(container);

                                    newMentionUsers.push(user.staffId);
                                    console.log(newMentionUsers)
                                    suggestions.innerHTML = '';
                                    suggestions.style.display = 'none';
                                    document.getElementById('tagButton').value = '';
                                    mentionProfile.style.display = 'none';

                                    document.querySelector('.remove-btn').addEventListener('click',() => {
                                        const node = document.querySelector(`#mentionUser-${user.id}`);
                                        node.remove();
                                        newMentionUsers.pop(user.staffId);
                                    })
                                };
                                suggestions.appendChild(div);

                            });
                            suggestions.style.display = 'block';
                        } else {
                            suggestions.style.display = 'none';
                            mentionProfile.style.display = 'none';
                        }
                    })
                    .catch(error => {
                        console.error('Fetch error:', error);
                        suggestions.innerHTML = '';
                        suggestions.style.display = 'none';
                        mentionProfile.style.display = 'none';
                    });
            } else {
                suggestions.innerHTML = '';
                suggestions.style.display = 'none';
                mentionProfile.style.display = 'none';
            }
        });
    } else {
        console.error('Input element was not found.');
    }
};
function updateCarouselDisplay() {
    const imageContainer = document.querySelector('.carousel-inner');
    const items = imageContainer.querySelectorAll('.carousel-item');
    items.forEach(item => item.classList.remove('active'));
    if (items.length > 0) {
        items[0].classList.add('active');
    }
    const controls = ['carousel-control-prev', 'carousel-control-next'];
    controls.forEach(control => {
        document.querySelector('.' + control).style.display = items.length > 1 ? 'block' : 'none';
    });
}
export const openUpdateModal = async (postId) => {
    try {
        const response = await fetch(`/post/${postId}`);
        if (!response.ok) {
            throw new Error('Network response is not OK!');
        }
        const post = await response.json();
        console.log('Post details:', post);

        const modal = document.getElementById('updateModal');
        const id = modal.querySelector('#updateId');
        const text = modal.querySelector('#post-body');
        const carouselInner = modal.querySelector('.carousel-inner');
        const existingImagesContainer = modal.querySelector('#existing-images-container');
        const carouselControlPrev = modal.querySelector('.carousel-control-prev');
        const carouselControlNext = modal.querySelector('.carousel-control-next');
        const hashTag = modal.querySelector('.hashTag');

        hashTag.innerHTML = '<span>Current hashTag: </span>';

        id.value = post.id;
        text.value = post.content.text;

        // Display mention user list if exists
        if(post.mentionUserList && post.mentionUserList.length > 0){
            console.log("There is mentionUser List.");
            const mentionContainer = document.querySelector('.mention-list');
            mentionContainer.innerHTML = '';
            post.mentionUserList.forEach(user => {
                const container = document.createElement('div');
                container.id = `mentionUser-${user.userId}`;
                container.classList.add('mentionContainer');
                container.innerHTML = `
                    <span class="userList">${user.userName}</span>
                    <span class="remove-btn">x</span>
                `;

                mentionContainer.style.display = 'flex';
                mentionContainer.appendChild(container);

                document.querySelector('.remove-btn').addEventListener('click', () => {
                    const node = document.querySelector(`#mentionUser-${user.userId}`);
                    node.remove();
                    removedMentionUsers.push(user.userId);
                });
            });
        }

        carouselInner.innerHTML = '';
        existingImagesContainer.innerHTML = '';

        if (post.topicList && post.topicList.length > 0) {
            hashTag.style.display = 'flex';
            post.topicList.forEach(tag => {
                const container = document.createElement('div');
                container.classList.add('topicTag');
                container.textContent = tag;
                const removeBtn = document.createElement('span');
                removeBtn.textContent = 'x';
                removeBtn.classList.add('tagRemove');
                removeBtn.style.cursor = 'pointer';
                container.appendChild(removeBtn);
                hashTag.appendChild(container);

                removeBtn.addEventListener('click', function () {
                    hashTag.removeChild(container);
                    removedTags.push(tag);
                });
            });
        } else {
            hashTag.style.display = 'none';
        }

        // Handle images
        if (post.content.images && post.content.images.length > 0) {
            post.content.images.forEach((photoUrl, index) => {
                const div = document.createElement('div');
                div.classList.add('carousel-item');
                console.log("PhotoUrl " + JSON.stringify(photoUrl));
                div.setAttribute('data-id', photoUrl.id);
                if (index === 0) div.classList.add('active');

                const img = document.createElement('img');
                img.src = photoUrl.name;
                img.classList.add('d-block', 'w-100');

                div.appendChild(img);
                carouselInner.appendChild(div);

                const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'existingImages';
                hiddenInput.value = photoUrl.name;
                existingImagesContainer.appendChild(hiddenInput);
            });

            modal.querySelector('#drop-container').style.display = 'none';
            document.getElementById('carouselExample').style.display = 'block';
        }

        // Handle videos
        if (post.content.videos && post.content.videos.length > 0) {
            post.content.videos.forEach((videoUrl, index) => {
                const div = document.createElement('div');
                div.classList.add('carousel-item');
                console.log("VideoUrl " + JSON.stringify(videoUrl));
                div.setAttribute('data-id', videoUrl.id);
                if (index === 0) div.classList.add('active');

                const video = document.createElement('video');
                video.src = videoUrl.name;
                video.classList.add('d-block', 'w-100');
                video.controls = true;

                div.appendChild(video);
                carouselInner.appendChild(div);

                const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'existingVideos';
                hiddenInput.value = videoUrl.name;
                existingImagesContainer.appendChild(hiddenInput);
            });

            modal.querySelector('#drop-container').style.display = 'none';
            document.getElementById('carouselExample').style.display = 'block';
        }

        // Show or hide carousel controls based on the number of media items
        if ((post.content.images && post.content.images.length > 1) || (post.content.videos && post.content.videos.length > 1)) {
            carouselControlPrev.style.display = 'block';
            carouselControlNext.style.display = 'block';
        } else {
            carouselControlPrev.style.display = 'none';
            carouselControlNext.style.display = 'none';
        }

        if (!post.content.images && !post.content.videos) {
            document.getElementById('carouselExample').style.display = 'none';
        }

        $(modal).modal('show');
    } catch (error) {
        console.error('Error fetching post details', error);
    }
};

const changingPost = (post) => {
    const postToUpdate = document.getElementById(`post-${post.id}`);
    const videoContent = post.content.videos.length > 0 ? renderContent(post) : '';
    const photoContent = post.content.images.length > 0 ? renderPhotos(post.content.images,post.id) : '';
    const userImageUrl = post.photo != null? post.photo : 'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';
    postToUpdate.innerHTML = `
       <div class="col-auto">
           <div class="user-pics"><img src="${userImageUrl}" alt="user3"></div>
       </div>
       <div class="user-content-box col">
            <!-- User profile and date -->
           <div class="d-flex justify-content-between user-names" style="height: 35px"> 
           
            <a href="userprofile/ + ${post.user.id}"><h1 class="full-name" style="margin-top: 5px">${post.user.name}.</h1></a>
                   ${post.groupName ? `<p class="grouptag">${post.groupName}</p>` : ''}
                   
                <p class="time me-3 ">${post.time}</p>
                <div class="dropdown">
                        <i class="fa-solid fa-ellipsis-vertical dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" style="padding-left: 10px;padding-bottom: 13px"></i>
                        <ul class="dropdown-menu">
                            <div class="postBtnContainer">
                                ${post.owner ? `
                                    <div class="postEditButton" id="update-post-${post.id}">Edit<i class="fa-solid fa-pen" style="margin-left: 20px;color: #3012c7;"></i></div>
                                ` : ''}
                                ${post.admin || post.owner ? `
                                    <div class="postDeleteButton" data-bs-toggle="modal" data-bs-target="#deletePostModal-${post.id}" id="postDeleteButton">Delete<i class="fa-solid fa-trash" style="margin-left: 20px;color: #e82b55;"></i></div>
                                ` : ''}
                            </div>
                        </ul>
                    </div>
            </div>
            <!-- User profile and date End-->
            
            <!-- User Content image and text -->
            <div class="user-content">
                <p class="contentText">${post.content.text}</p>
                <div id="mentions-container-${post.id}"></div>
                <div id="topicContainer-${post.id}"></div>
                
                ${videoContent || photoContent}
           </div>
            
           ${renderInteractionIcons(post)}
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
    const mentionsContainer = postToUpdate.querySelector(`#mentions-container-${post.id}`);
    const mentionsLength = post.mentionUserList.length;
    post.mentionUserList.forEach((mention, index) => {
        const mentionLink = document.createElement('a');
        mentionLink.href = `/userprofile/${mention.userId}`;
        const mentionSpan = document.createElement('span');
        mentionSpan.classList.add('userMention');
        mentionSpan.style.color = '#0d6efd';
        mentionSpan.textContent = `@${mention.userName}${index < mentionsLength - 1 ? ',' : ''}`;
        mentionLink.appendChild(mentionSpan);
        mentionsContainer.appendChild(mentionLink);
    });

    const topicContainer = postToUpdate.querySelector(`#topicContainer-${post.id}`);
    const topicLength = post.topicList.length;
    post.topicList.forEach((topic,index) => {
        const spanTopic = document.createElement('span');
        spanTopic.classList.add('topicName');
        spanTopic.style.color = '#0d6efd';
        spanTopic.style.cursor = 'pointer';
        spanTopic.textContent = `#${topic}${index < topicLength - 1 ? ',' : ''}`;
        spanTopic.addEventListener('click', () =>{
            let backUrlHolder = document.getElementById('backUrlHolder');
            if (!backUrlHolder) {
                backUrlHolder = document.createElement('div');
                backUrlHolder.id = 'backUrlHolder';
                backUrlHolder.innerHTML = `<i class="fa-solid fa-arrow-left-long backArrow"></i>`;
                backUrlHolder.addEventListener('click', () => window.history.back());
                const mainContent = document.querySelector('#mainContent');
                const firstChild = mainContent.firstChild;
                mainContent.insertBefore(backUrlHolder, firstChild);
            }
            const home = document.querySelector('.home');
            home.querySelector('h1').textContent = topic;
            document.getElementById('poll').style.display = 'none';
            document.querySelectorAll('.nav-list').forEach(item => item.classList.remove('focused'));
            state.currentPage = 0;
            state.hasMore = true;
            showAllPosts('', topic, false)
        });
        topicContainer.appendChild(spanTopic);

        requestAnimationFrame(() => {
            postToUpdate.classList.add('fade-in');
            attachEventListenersToPost(postToUpdate, post.id);
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
function deleteMedia(mediaSrc) {
    const carouselInner = document.querySelector('.carousel-inner');
    const itemToDelete = carouselInner.querySelector(`.carousel-item img[src="${mediaSrc}"], .carousel-item video[src="${mediaSrc}"]`).parentElement;

    if (itemToDelete) {
        removedImages.push(mediaSrc); // Assuming you handle videos in the same array
        carouselInner.removeChild(itemToDelete);
        updateCarouselDisplay();
        if (carouselInner.children.length === 0) {
            document.getElementById('carouselExample').style.display = 'none';
            const dropContainer = document.getElementById('drop-container');
            dropContainer.style.display = 'flex';
        }
    } else {
        console.error('Media item to delete not found.');
    }

    document.getElementById('fileInput').value = '';
}

function dragOverHandler(event) {
    event.preventDefault();
    event.currentTarget.classList.add('drag-over');
}

function dropHandler(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('drag-over');
    const files = event.dataTransfer.files;
    handleFiles(files);
}