import { renderVideoContent } from './renderVideo.js';
import { renderPhotos } from './renderPhoto.js';

const period = document.getElementById('period');
document.addEventListener('DOMContentLoaded',async () => {
    await updatePeriodAndFetchPosts();
});
const weekLi = document.querySelector('.week');
weekLi.addEventListener('click',async function () {
    await updatePeriodAndFetchPosts();
});
async function updatePeriodAndFetchPosts() {
    period.innerText = '| This Week';
    await fetchUserPosts('/post/get-trending-posts-inOneWeek');
}
const monthLi = document.getElementById('dropdown-item-month');
monthLi.addEventListener('click',async function () {
    period.innerText = '| This Month';
    const url = '/post/get-trending-posts-inOneMonth';
    await fetchUserPosts(url);
})
const yearLi = document.getElementById('dropdown-item-year');
yearLi.addEventListener('click',async function () {
    period.innerText = '| This Year'
    const url = '/post/get-trending-posts-inOneYear';
    await fetchUserPosts(url);
})
const fetchUserPosts = async (url) => {
    const response = await fetch(url);

    if(!response.ok){
        throw new Error("Error!");
        // if(response.status === 404){
        //     throw new Error("There is no BookMark")
        // }
    }
    const data = await response.json();
    console.log("Trending Posts",data);
    const postsContainer = document.querySelector('.postsContainer');
    postsContainer.innerHTML = '';
    if(data!=null){
        data.forEach(post => {

            const postDiv = document.createElement('div');
            postDiv.classList.add('tweets');
            postDiv.id = `post-${post.id}`;
            const videoContent = post.content.videos.length > 0 ? renderVideoContent(post) : '';
            const photoContent = post.content.images.length > 0 ? renderPhotos(post.content.images) : '';
            const imageUrl = post.photo!=null? post.photo :'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';
            postDiv.innerHTML =
                `<div class="col-auto">
                    <div class="user-pics"><img src="${imageUrl}" alt="user3"></div>
                </div>
            <div class="user-content-box col">
                <!-- User profile and date -->
                <div class="d-flex justify-content-between user-names" style="height: 35px">

                    <a href="userprofile/ + ${post.user.id}"><h1 class="full-name" style="margin-top: 5px">${post.user.name}.</h1></a>
                    ${post.groupName ? `<p class="grouptag">${post.groupName}</p>` : ''}

                    <p class="time me-3 ">${post.time}</p>
                </div>
                <!-- User profile and date End-->

                <!-- User Content image and text -->
                <div class="user-content">
                    <p>${post.content.text}</p>

                    ${videoContent || photoContent}
                </div>
               <div class="content-icons d-flex justify-evenly" style="justify-content: space-evenly;">
           <a href="/post/details/${post.id}"><i class="far fa-comment blue">${post.commentCount}</i></a>
            <i class="fa-solid fa-heart" style="cursor: pointer; color: #b20f0f; padding-top: 2px">${post.likes}</i>
        </div>
            </div>`;

            postsContainer.appendChild(postDiv);
        })
    }else{
        const ErrorMessage = document.createElement('div');
        ErrorMessage.innerHTML =`<span style="color: red">No Matched Result is found</span>`;
        postsContainer.appendChild(ErrorMessage);
    }
}
