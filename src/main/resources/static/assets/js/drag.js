document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('fileInput');
    fileInput.addEventListener('change', handleFileSelection);

    const deleteButton = document.getElementById('objectDelete');
    deleteButton.addEventListener('click', deleteImage);

    document.getElementById('objectAdd').addEventListener('click', function() {
        document.getElementById('fileInput').click();
    });
});

function changeText(link) {
    var newText = link.textContent;
    document.getElementById('dropbtn').textContent = newText;
    document.querySelector('.dropdown-content').style.display = 'none';

}
function selectTab(event) {
    const tabs = document.querySelectorAll('.tab');
    const underline = document.querySelector('.underline');
    const targetTab = event.target;

    // Calculate the width and position of the target tab
    const tabWidth = targetTab.offsetWidth;
    const tabLeft = targetTab.offsetLeft;

    // Update the underline to align with the target tab
    underline.style.width = `${tabWidth}px`;
    underline.style.left = `${tabLeft}px`;
    underline.style.height = '3px';

    // Optional: Add a class to the active tab
    tabs.forEach(tab => tab.classList.remove('active'));
    targetTab.classList.add('active');
}
document.addEventListener('DOMContentLoaded', function() {
    var button = document.getElementById('dropbtn');
    if (button) {
        button.onclick = function() {
            var dropdownContent = this.nextElementSibling;
            if (dropdownContent.style.display === 'block') {
                dropdownContent.style.display = 'none';
            } else {
                dropdownContent.style.display = 'block';
            }
        };
    } else {
        console.log('Button with ID "dropbtn" was not found.');
    }
});

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

function deleteImage() {
    const carouselInner = document.querySelector('.carousel-inner');
    const allItems = carouselInner.querySelectorAll('.carousel-item');

    if (allItems.length === 1) {
        carouselInner.removeChild(allItems[0]);
        document.getElementById('carouselExample').style.display = 'none';
        const dropContainer = document.getElementById('drop-container');
        dropContainer.style.display = 'flex';
    } else if (allItems.length > 1) {
        if (confirm("Are you sure you want to delete all images?")) {
            while (carouselInner.firstChild) {
                carouselInner.removeChild(carouselInner.firstChild);
            }
            document.getElementById('carouselExample').style.display = 'none';
            const dropContainer = document.getElementById('drop-container');
            dropContainer.style.display = 'flex';
        }
    }
    document.getElementById('fileInput').value = '';
}
// document.getElementById('fileInput').addEventListener('change', function(event) {
//     const files = event.target.files;
//     handleFiles(files);
// });

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

