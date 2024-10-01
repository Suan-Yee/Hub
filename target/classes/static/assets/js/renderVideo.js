export const renderContent = (post) => {
    const videos = post.content.videos;

    if (videos.length === 1) {
        // Render the custom video player if there's only one video
        return renderVideoContent(post);
    } else if (videos.length > 1) {
        // Render photo-like display for multiple videos
        return renderVideosAsPhotos(videos, post.id);
    } else {
        // Handle case where there are no videos
        return `<p>No videos available</p>`;
    }
};
export const renderVideoContent = (post) => {

    let videoId;
    let videoName;
    if(post.content.videos && post.content.videos.length>0){
        videoId = post.content.videos[0].id;
        videoName =post.content.videos[0].name;
    }

    return `
        <div class="container" id="container${videoId}">
            <button id="playButton${videoId}" class="playButton" onclick="playVideo('${videoId}','${videoName}')">
                <i class="fa-solid fa-play"></i>
            </button>
            <div class="loading-container">
                <div id="loadingIndicator${videoId}" class="loading" style="display: none;">Loading...</div>
            </div>
            <video id="video${videoId}" src="${videoName}"></video>
            <div class="controls" id="control${videoId}">
                <div class="left"> 
                    <button id="skipminus-10${videoId}"> 
                        <i class="fa-solid fa-backward"></i> 
                    </button> 
                    <button id="play-pause${videoId}"> 
                        <i class="fa-solid fa-play"></i> 
                    </button> 
                    <button id="skip-10${videoId}"> 
                        <i class="fa-solid fa-forward"></i> 
                    </button> 
                </div> 
                <div class="video-timer"> 
                    <span id="current-time${videoId}">00:00</span> 
                    <span id="separator">/</span> 
                    <span id="max-duration${videoId}">00:00</span> 
                </div> 
                <div id="playBackLine${videoId}" class="playback-line"> 
                    <div id="progressBar${videoId}" class="progress-bar"></div>
                </div> 
                <div class="right"> 
                    <div class="volume-container"> 
                        <div id="mute${videoId}"> 
                            <i class="fas fa-volume-up"></i> 
                        </div> 
                        <input type="range" id="volume" class="volumeSlider${videoId}" min="0" max="1" step="0.01" value="1"> 
                    </div> 
                </div>
                <div id="expand${videoId}">
                    <i class="fa-solid fa-expand"></i>
                </div>
            </div>
        </div>
    `;
}

const renderVideosAsPhotos = (videos, postId) => {
    const displayedVideos = videos.slice(0, 4);
    let html = '<div class="row">';
    displayedVideos.forEach((video, index) => {
        let colSize = 'col-12';
        if (videos.length === 2 || videos.length === 4 || videos.length > 4) {
            colSize = 'col-6';
        } else if (videos.length === 3) {
            index < 2 ? colSize = 'col-6' : colSize = 'col-12';
        }
        let videoClass = 'img-fluid imagett';
        if (videos.length === 1) {
            videoClass += ' imagett-full-height';
        }
        if (videos.length > 4 && index === displayedVideos.length - 1) {
            html += `<div class="${colSize} mb-2 position-relative">
                        <video src="${video.name}" class="${videoClass} blur-image" style="max-height: 320px; border-radius: 20px"></video>
                        <div class="overlay-text">+${videos.length - 4}</div>
                        <div class="play-button-overlay"><i style="font-size: larger;" class="fa-solid fa-play"></i></div>
                     </div>`;
        } else {
            html += `<div class="${colSize} mb-2 position-relative">
                        <video src="${video.name}" class="${videoClass}" style="max-height: 320px; border-radius: 20px"></video>
                        <div class="play-button-overlay"><i class="fa-solid fa-play"></i></div>
                     </div>`;
        }
    });
    html += '</div>';

    document.body.insertAdjacentHTML('beforeend', `
        <div id="lightbox-${postId}" class="carousel slide lightbox">
             <div class="carousel-indicators">
                ${videos.map((_, index) => `
                    <button type="button" data-bs-target="#lightbox-${postId}" data-bs-slide-to="${index}" ${index === 0 ? 'class="active"' : ''} aria-label="Slide ${index + 1}"></button>
                `).join('')}
            </div>
            <div class="carousel-inner">
                ${videos.map((video, index) => `
                    <div class="carousel-item  ${index === 0 ? 'active' : ''}">
                        <video src="${video.name}" class="d-block w-100" style="max-height: 90vh; object-fit: contain; width: auto; margin: auto;" controls></video>
                    </div>
                `).join('')}
            </div>
            ${videos.length > 1 ? `
                <button class="carousel-control-prev" type="button" data-bs-target="#lightbox-${postId}" data-bs-slide="prev">
                    <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                    <span class="visually-hidden">Previous</span>
                </button>
                <button class="carousel-control-next" type="button" data-bs-target="#lightbox-${postId}" data-bs-slide="next">
                    <span class="carousel-control-next-icon" aria-hidden="true"></span>
                    <span class="visually-hidden">Next</span>
                </button>
            ` : ''}
            <span style="position: absolute; top: 20px; right: 20px; font-size: 30px; cursor: pointer; color: white; z-index: 1060;" class="close-lightbox" data-post-id="${postId}">×</span>
        </div>
    `);

    document.querySelector(`.close-lightbox[data-post-id="${postId}"]`).addEventListener('click', () => {
        document.querySelectorAll(`#lightbox-${postId} video`).forEach(video => {
            video.pause();
        });
    });

    document.querySelector(`#lightbox-${postId} .carousel-control-prev`).addEventListener('click', () => {
        document.querySelectorAll(`#lightbox-${postId} video`).forEach(video => {
            video.pause();
        });
    });

    document.querySelector(`#lightbox-${postId} .carousel-control-next`).addEventListener('click', () => {
        document.querySelectorAll(`#lightbox-${postId} video`).forEach(video => {
            video.pause();
        });
    });

    return html;
};