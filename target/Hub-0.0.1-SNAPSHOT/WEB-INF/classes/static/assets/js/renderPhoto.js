export const renderPhotos = (images, postId) => {
    const displayedImages = images.slice(0, 4);
    let html = '<div class="row">';
    displayedImages.forEach((image, index) => {
        let colSize = 'col-12';
        if (images.length === 2 || images.length === 4 || images.length > 4) {
            colSize = 'col-6';
        } else if (images.length === 3) {
            index < 2 ? colSize = 'col-6' : colSize = 'col-12';
        }
        let imageClass = 'img-fluid imagett';
        if (images.length === 1) {
            imageClass += ' imagett-full-height';
        }
        if (images.length > 4 && index === displayedImages.length - 1) {
            html += `<div class="${colSize} mb-2 position-relative">
                        <img src="${image.name}" alt="photo" class="${imageClass} blur-image" data-src="${image.name}" style="max-height: 320px;">
                        <div class="overlay-text">+${images.length - 4}</div>
                     </div>`;
        } else {
            html += `<div class="${colSize} mb-2">
                    <img src="${image.name}" alt="photo" class="${imageClass}" data-src="${image.name}" style="max-height: 320px;">
                 </div>`;
        }
    });
    html += '</div>';

    // Add lightbox HTML to the body
    document.body.insertAdjacentHTML('beforeend', `
        <div id="lightbox-${postId}" class="carousel slide lightbox">
             <div class="carousel-indicators">
                ${images.map((_, index) => `
                    <button type="button" data-bs-target="#lightbox-${postId}" data-bs-slide-to="${index}" ${index === 0 ? 'class="active"' : ''} aria-label="Slide ${index + 1}"></button>
                `).join('')}
            </div>
            <div class="carousel-inner">
                ${images.map((image, index) => `
                    <div class="carousel-item  ${index === 0 ? 'active' : ''}">
                        <img src="${getHighResUnsplashUrl(image.name)}" class="d-block w-100" style="max-height: 90vh; object-fit: contain; width: auto; margin: auto;" alt="photo">
                        
                    </div>
                `).join('')}
            </div>
            ${images.length > 1 ? `
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
    return html;
};
const getHighResUnsplashUrl = (url, width = 1080) => {
    const urlObj = new URL(url);
    urlObj.searchParams.set('w', width);
    return urlObj.toString();
};