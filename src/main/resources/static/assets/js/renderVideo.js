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
                    <i class="fa-solid fa-up-right-and-down-left-from-center"></i>
                </div>
            </div>
        </div>
    `;
}