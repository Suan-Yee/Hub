export const renderPhotos = (images) => {
    let html = '<div class="row" >';
    images.forEach((image, index) => {
        // Define the column size based on the number of images
        let colSize = 'col-12';
        if (images.length === 2 || images.length === 4) {
            colSize = 'col-6';
        } else if (images.length === 3) {
            if (index < 2) {
                colSize = 'col-6';
            } else {
                colSize = 'col-12'; // Full width for the single photo in the second row
            }
        }

        let imageClass = 'img-fluid imagett';
        if (images.length === 1) {
            imageClass += ' imagett-full-height';
        }
        html += `<div class="${colSize} mb-2">
                    <img src="${image.name}" alt="photo" class="${imageClass}">
                 </div>`;
    });
    html += '</div>';
    return html;
};