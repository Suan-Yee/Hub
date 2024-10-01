
const playVideo = (videoId,videoName)=>{
    const container = document.getElementById('container'+videoId);
    const control = document.getElementById('control'+videoId);
    const video = document.getElementById('video'+videoId);
    const playButton = document.getElementById('playButton'+videoId);
    const playPauseButton = document.getElementById('play-pause'+videoId);
    const skipForwardBtn = document.getElementById('skip-10'+videoId);
    const skipBackwardBtn = document.getElementById('skipminus-10'+videoId);
    const playBackLine = document.getElementById('playBackLine'+videoId);
    const currentTimeRef = document.getElementById('current-time'+videoId);
    const maxDuration = document.getElementById('max-duration'+videoId);
    const progressBar = document.getElementById('progressBar'+videoId);
    const muteBtn = document.getElementById('mute'+videoId);
    const fullscreenBtn = document.getElementById('expand'+videoId);
    const volumeSlider = document.getElementsByClassName('volumeSlider'+videoId)[0];
    const loadingIndicator = document.getElementById('loadingIndicator'+videoId);

    const handlePlayPauseClick = () => {
        playPauseButton.removeEventListener('click', handlePlayPauseClick); // Remove existing event listener
        playVideo(videoId, videoName);
    };
    const handleBuffering =()=>{
        loadingIndicator.style.display = 'block';
    };
    const handleBufferingComplete =()=>{
        loadingIndicator.style.display = 'none';
    }

    video.addEventListener('waiting',()=>handleBuffering());
    video.addEventListener('playing',()=>handleBufferingComplete());

    let timer;
    const hideControls = ()=>{
        if(video.paused) return; // if video is paused,return
        timer = setTimeout(()=>{ // remove show-controls class after 3
            control.style.opacity = '0';
        },3000)
    }
    hideControls();
    container.addEventListener("mousemove",()=>{
        control.style.opacity = '1'; // add  show-controls class on mousemove
        clearTimeout(timer); // clear timer
        hideControls(); // calling hideControls
    });

    const formatTime = (time) =>{
        let seconds = Math.floor(time%60);
        let minutes = Math.floor(time/60)%60;
        let hours = Math.floor(time/3600);

        seconds = seconds<10 ? '0'+seconds : seconds;
        minutes = minutes<10 ? '0'+minutes : minutes;
        hours = hours<10 ? '0'+hours : hours;

        if(hours==0){
            return minutes+':'+seconds;
        }
        return hours+":"+minutes+":"+seconds;
    }

    if(video.paused){
        if (video.currentTime === 0) { // Only set the src if the video is at the beginning
            video.src = videoName; // Set the video source
        }
        video.play();
        playButton.innerHTML = '<i class="fa-solid fa-pause"></i>'
        playPauseButton.innerHTML = '<i class="fa-solid fa-pause"></i>';
        setTimeout(() => {
            playButton.style.display = 'none';
        }, 3000);
    }else{
        video.pause();
        playButton.style.display= 'block';
        playButton.innerHTML = '<i class="fa-solid fa-play"></i>';
        playPauseButton.innerHTML = '<i class="fa-solid fa-play"></i>';
    }
    playPauseButton.addEventListener('click',handlePlayPauseClick);

    /* skip forward 5s if click skipForWard button*/
    skipForwardBtn.addEventListener('click',()=>{
        console.log(skipForwardBtn+ "is click");
        video.currentTime +=5;
    });

    /* skip backward 5s if click skipBackWard button*/
    skipBackwardBtn.addEventListener('click',()=>{
        video.currentTime -=5;
    });
    /*fullscreenBtn.addEventListener('click',()=>{
        if(video.requestFullscreen){
            video.requestFullscreen();
        }else if(video.mozRequestFullScreen){
            video.mozRequestFullScreen();
        }else if(video.webkitRequestFullscreen){
            video.webkitRequestFullscreen();
        }else{
            video.msRequestFullscreen();
        }
    });*/
    fullscreenBtn.addEventListener('click',()=>{
        container.classList.toggle("fullscreen");
        let fullScreenIcon = fullscreenBtn.querySelector('i');
        if(document.fullscreenElement){
            fullScreenIcon.classList.replace("fa-compress","fa-expand");
            return document.exitFullscreen();
        }
        fullScreenIcon.classList.replace("fa-expand","fa-compress");
        container.requestFullscreen();
    });

    /* For volume icon and volume up and down*/
    muteBtn.addEventListener('click',()=>{
        if(video.muted){
            video.muted = false;
            muteBtn.innerHTML = '<i class="fas fa-volume-up"></i>';
            volumeSlider.value = video.volume;
        }
        else{
            video.muted = true;
            muteBtn.innerHTML = '<i class="fa-solid fa-volume-xmark"></i>';
            volumeSlider.value = 0;
        }
    });
    volumeSlider.addEventListener("input",function () {
        video.volume = volumeSlider.value;
        if(video.volume ===0){
            muteBtn.innerHTML = '<i class="fa-solid fa-volume-xmark"></i>';
        }else {
            muteBtn.innerHTML = '<i class="fas fa-volume-up"></i>';
        }
    });

    video.addEventListener("timeupdate",()=>{
        const currentTime = video.currentTime;
        const duration = video.duration;
        const percent = (currentTime/duration)*100;
        progressBar.style.width = percent+"%";
        currentTimeRef.innerHTML = formatTime(currentTime);
        maxDuration.innerText = formatTime(duration);
    });
    video.addEventListener("ended", () => {
        progressBar.style.width = "0%";
        playPauseButton.innerHTML = '<i class="fa-solid fa-play"></i>';
    });
    playBackLine.addEventListener('click',e =>{
        let timeLineWidth = playBackLine.clientWidth;
        video.currentTime = (e.offsetX/timeLineWidth)*video.duration;
    });
    container.addEventListener('mouseenter',()=>{
        control.style.opacity = 1;
    });
    container.addEventListener('mouseleave',()=>{
        control.style.opacity = 1;
    });
    // Function to check if video is in picture-in-picture mode
    function isInPictureInPicture(video) {
        return document.pictureInPictureElement === video;
    }

// Intersection Observer to pause the video when out of view
    const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
            const video = entry.target;
            if (!entry.isIntersecting && !isInPictureInPicture(video)) {
                if (!video.paused) {
                    video.pause();
                    //playPauseButton element to update
                    playPauseButton.innerHTML = '<i class="fa-solid fa-play"></i>';
                }
            }
        });
    }, { threshold: 0.5 }); // Change the threshold value as needed
    observer.observe(video);

}