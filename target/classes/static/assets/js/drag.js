document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('fileInput');
    let fileInputs = [];  // Global array to store file inputs
    let removedFiles = []; // Global array to track removed files
    let imageIdCounter = 0;

    fileInput.addEventListener('change', handleFileSelection);
    document.getElementById('objectAdd').addEventListener('click', function() {
        const newFileInput = document.createElement('input');
        newFileInput.type = 'file';
        newFileInput.accept = 'image/*, video/*, image/gif';
        newFileInput.multiple = true;
        newFileInput.name = 'files';
        newFileInput.addEventListener('change', handleFileSelection);
        newFileInput.setAttribute('data-id', `image-${imageIdCounter}`);
        imageIdCounter++;
        fileInputs.push(newFileInput);  // Add new input to the global array
        newFileInput.click();
    });

    document.getElementById('dropbtn').addEventListener('click', fetchGroup);
    document.getElementById('gifButton').addEventListener('click', function() {
        document.getElementById('gifModal').style.display = 'block';
        fetchRandomGifs();
    });

    document.getElementsByClassName('close')[0].addEventListener('click', function() {
        document.getElementById('gifModal').style.display = 'none';
    });

    document.getElementById('gifSearch').addEventListener('input', function() {
        const query = this.value;
        if (query.length > 2) {
            fetchGifs(query);
        }
    });

    const inputElement = document.getElementById('tagButton');
    inputElement.addEventListener('focus', () => {
        if (!inputElement.value.startsWith('@')) {
            inputElement.value = '@' + inputElement.value;
        }
        userMention(inputElement);
    });

    const deleteButton = document.getElementById('objectDelete');
    deleteButton.addEventListener('click', () => {
        const activeItem = document.querySelector('.carousel-item.active');
        if (activeItem) {
            const mediaId = activeItem.getAttribute('data-id');
            deleteMedia(mediaId);
        }
    });

    document.querySelector('.post-form').addEventListener('submit', function(event) {
        const formData = new FormData(this);

        fileInputs.forEach(input => {
            Array.from(input.files).forEach(file => {
                const inputId = input.getAttribute('data-id');
                if (!removedFiles.includes(inputId)) {
                    formData.append('files', file);
                }
            });
        });

        fetch('/post/create', {
            method: 'POST',
            body: formData
        }).then(response => {
            if (response.ok) {
                window.location.href = '/index';
            } else {
                console.error('Error submitting form:', response.statusText);
            }
        }).catch(error => {
            console.error('Error submitting form:', error);
        });

        event.preventDefault();
    });

    function handleFileSelection(event) {
        const files = event.target.files;
        handleFiles(files, event.target.getAttribute('data-id'));
    }

    function handleFiles(files, inputId) {
        const dropContainer = document.querySelector('#drop-container');
        const carousel = document.getElementById('carouselExample');

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            if (file.type.startsWith('image/')) {
                handleImage(file, inputId);
            } else if (file.type.startsWith('video/')) {
                handleVideo(file, inputId);
            } else if (file.type === 'image/gif') {
                handleGif(file, inputId);
            } else {
                console.log('Unsupported file type:', file.type);
                continue;
            }
            dropContainer.style.display = 'none';
            carousel.style.display = 'block';
        }
        updateCarouselDisplay();
    }

    function handleVideo(file,inputId) {
        const video = document.createElement('video');
        video.src = URL.createObjectURL(file);
        video.classList.add('d-block', 'w-100');
        video.controls = true;
        video.onload = function() {
            URL.revokeObjectURL(this.src);
        };

        const carouselItem = document.createElement('div');
        carouselItem.classList.add('carousel-item');
        carouselItem.setAttribute('data-id', inputId);
        carouselItem.appendChild(video);

        const imageContainer = document.querySelector('.carousel-inner');
        imageContainer.appendChild(carouselItem);

        // Hide the Add button

        updateCarouselDisplay();
    }

    function handleGif(file, inputId) {
        const img = document.createElement('img');
        img.src = URL.createObjectURL(file);
        img.classList.add('d-block', 'w-100', 'imageCreate');
        img.onload = function() {
            URL.revokeObjectURL(this.src);
        };

        const carouselItem = document.createElement('div');
        carouselItem.classList.add('carousel-item');
        carouselItem.setAttribute('data-id', inputId);
        carouselItem.appendChild(img);

        const mediaContainer = document.querySelector('.carousel-inner');
        mediaContainer.appendChild(carouselItem);

        updateCarouselDisplay();
    }

    function handleImage(file, inputId) {
        const img = document.createElement('img');
        img.src = URL.createObjectURL(file);
        img.classList.add('d-block', 'w-100', 'imageCreate');
        img.onload = function() {
            URL.revokeObjectURL(this.src);
        };

        const carouselItem = document.createElement('div');
        carouselItem.classList.add('carousel-item');
        carouselItem.setAttribute('data-id', inputId);
        carouselItem.appendChild(img);

        const imageContainer = document.querySelector('.carousel-inner');
        imageContainer.appendChild(carouselItem);

        updateCarouselDisplay();
    }

    function deleteMedia(mediaId) {
        const carouselInner = document.querySelector('.carousel-inner');
        const itemToDelete = carouselInner.querySelector(`.carousel-item[data-id="${mediaId}"]`);

        if (itemToDelete) {
            carouselInner.removeChild(itemToDelete);
            updateCarouselDisplay();
            if (carouselInner.children.length === 0) {
                document.getElementById('carouselExample').style.display = 'none';
                const dropContainer = document.getElementById('drop-container');
                dropContainer.style.display = 'flex';
            }
        }
        removedFiles.push(mediaId);
        fileInputs = fileInputs.filter(input => input.getAttribute('data-id') !== mediaId);
        document.getElementById('fileInput').value = '';
    }
    function selectGif(url) {
        const img = document.createElement('img');
        img.src = url;
        img.classList.add('d-block', 'w-100', 'imageCreate');

        const carouselItem = document.createElement('div');
        carouselItem.classList.add('carousel-item');
        carouselItem.setAttribute('data-id', `gif-${imageIdCounter}`);
        imageIdCounter++;
        carouselItem.appendChild(img);

        const form = document.querySelector('.post-form');
        if (!form) {
            console.error('Form with class "post-form" not found.');
            return;
        }
        const gifUrlInput = document.createElement('input');
        gifUrlInput.type = 'hidden';
        gifUrlInput.name = 'gifUrl';
        gifUrlInput.value = url;
        document.querySelector('.post-form').appendChild(gifUrlInput);

        const imageContainer = document.querySelector('.carousel-inner');
        imageContainer.appendChild(carouselItem);

        updateCarouselDisplay();

        document.querySelector('.drop-container').style.display = 'none';
        document.getElementById('carouselExample').style.display = 'block';
        document.getElementById('gifModal').style.display = 'none';
    }
    function displayGifResults(gifs) {
        const gifResultsContainer = document.getElementById('gifResults');
        gifResultsContainer.innerHTML = '';

        gifs.forEach(gif => {
            const img = document.createElement('img');
            img.src = gif.images.fixed_height.url;
            img.alt = gif.title;
            img.addEventListener('click', () => selectGif(gif.images.fixed_height.url));
            gifResultsContainer.appendChild(img);
        });
    }
    const apiKey = '5VW1Y0ZX7CS79koB0OgUpK2m1A9t7Lra';

    function fetchRandomGifs() {
        const url = `https://api.giphy.com/v1/gifs/trending?api_key=${apiKey}&limit=50`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                const safeGifs = data.data.filter(gif => gif.rating !== 'r' && gif.rating !== 'pg-13');
                const randomGifs = [];
                while (randomGifs.length < 10 && safeGifs.length > 0) {
                    const randomIndex = Math.floor(Math.random() * safeGifs.length);
                    randomGifs.push(safeGifs.splice(randomIndex, 1)[0]);
                }
                displayGifResults(randomGifs);
            })
            .catch(error => console.error('Error fetching random GIFs:', error));
    }

    function fetchGifs(query) {
        const url = `https://api.giphy.com/v1/gifs/search?api_key=${apiKey}&q=${query}&limit=18`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                const safeGifs = data.data.filter(gif => gif.rating !== 'r' && gif.rating !== 'pg-13');
                displayGifResults(safeGifs);
            })
            .catch(error => console.error('Error fetching GIFs:', error));
    }


});
function updateCarouselDisplay() {
    const mediaContainer = document.querySelector('.carousel-inner');
    const items = mediaContainer.querySelectorAll('.carousel-item');
    items.forEach(item => item.classList.remove('active'));
    if (items.length > 0) {
        items[0].classList.add('active');
    }
    const controls = ['carousel-control-prev', 'carousel-control-next'];
    controls.forEach(control => {
        document.querySelector('.' + control).style.display = items.length > 1 ? 'block' : 'none';
    });
}
const fetchGroup = async () => {
    const response = await fetch('/groupForm');
    const data = await response.json();
    console.log(data);

    const dropdownContent = document.querySelector('.dropdown-content');
    dropdownContent.innerHTML = ''; // Clear previous content

    const everyoneLink = document.createElement('div');
    everyoneLink.setAttribute('data-id', '0');
    everyoneLink.style.padding = '5px';
    everyoneLink.onclick = function() { selectGroup(this); };

    const icon = document.createElement('i');
    icon.classList.add('fa-solid', 'fa-earth-americas');
    icon.style.color = 'black';

    everyoneLink.appendChild(icon);
    everyoneLink.appendChild(document.createTextNode('Everyone'));
    dropdownContent.appendChild(everyoneLink);

    data.forEach(group => {
        const link = document.createElement('div');
        link.setAttribute('data-id', group.id);
        link.style.padding = '5px';
        link.onclick = function() { selectGroup(this); };

        const img = document.createElement('img');
        img.src = group.groupPhoto;
        img.alt = group.groupName;
        img.style.width = '35px';
        img.style.height = '35px';
        img.style.borderRadius = '10px';
        img.style.marginRight = '5px';

        link.appendChild(img);
        link.appendChild(document.createTextNode(group.groupName));
        dropdownContent.appendChild(link);
    });

    // Show the dropdown content
    dropdownContent.style.display = 'block';
}

const selectGroup = (element) => {
    const selectedGroupId = element.getAttribute('data-id');
    const dropbtn = document.getElementById('dropbtn');
    const dropdownContent = document.querySelector('.dropdown-content');

    document.getElementById('selectedGroupId').value = selectedGroupId;

    if (selectedGroupId === '0') {
        const icon = document.createElement('i');
        icon.classList.add('fa-solid', 'fa-earth-americas');
        icon.style.color = 'black';

        dropbtn.innerHTML = '';
        dropbtn.appendChild(icon);
        dropbtn.appendChild(document.createTextNode('Everyone'));
    } else {
        dropbtn.textContent = element.textContent;
    }
    dropdownContent.style.display = 'none';
};

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
                                div.style.display = 'flex';
                                div.style.alignItems = 'center';
                                div.style.cursor = 'pointer';
                                div.style.marginBottom = '10px';
                                div.id = user.staffId;
                                const userImage = document.createElement('img');
                                userImage.src = user.photo;
                                userImage.alt = user.name;
                                userImage.style.width = '30px';
                                userImage.style.height = '30px';
                                userImage.style.marginRight = '10px';
                                userImage.style.borderRadius = '50%';

                                const userName = document.createTextNode(user.name);
                                const mentionContainer = document.querySelector('.mention-list');

                                div.appendChild(userImage);
                                div.appendChild(userName);
                                div.onclick = () => {
                                    const container = document.createElement('div');
                                    container.id = `mentionUser-${user.id}`;
                                    container.classList.add('mentionContainer');
                                    container.innerHTML = `
                                        <span class="userList">${user.name}</span>
                                        <span class="remove-btn">x</span>
                                    `;

                                    mentionContainer.style.display = 'flex';
                                    mentionContainer.appendChild(container);

                                    const mentionInput = document.createElement('input');
                                    mentionInput.type = 'hidden';
                                    mentionInput.name = 'mentionStaff';
                                    mentionInput.id = `mentionInput-${user.id}`;
                                    mentionInput.value = user.staffId;
                                    document.querySelector('.post-form').appendChild(mentionInput);

                                    suggestions.innerHTML = '';
                                    suggestions.style.display = 'none';
                                    document.getElementById('tagButton').value = '';
                                    mentionProfile.style.display = 'none';

                                    deleteMention(user.id);
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

const deleteMention = (userId) => {
    const node = document.querySelector(`#mentionUser-${userId}`);
    const mentionInput = document.getElementById(`mentionInput-${userId}`);
    node.addEventListener('click', function() {
        mentionInput.remove();
        node.remove();
    });
};
document.addEventListener('click', function(event) {
    const dropdownContent = document.querySelector('.dropdown-content');
    const dropbtn = document.getElementById('dropbtn');

    if (!dropbtn.contains(event.target)) {
        dropdownContent.style.display = 'none';
    }
});