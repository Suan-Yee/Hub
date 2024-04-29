export const fetchLike = async (id,element) => {

    try {
        const response = await fetch('/likes/like', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                postId : id,
            })
        });

        if (!response.ok) {
            throw new Error('Failed to like the post');
        }
        const data = await response.json();
        let likeCount = parseInt(element.textContent) || 0;
        if (data.liked) {
            element.classList.replace('fa-regular', 'fa-solid');
            element.style.color = '#c91818';
            element.setAttribute('data-liked', 'true');
            likeCount++;
        } else {
            element.classList.replace('fa-solid', 'fa-regular');
            element.style.color = '';
            element.setAttribute('data-liked', 'false');
            likeCount--;
        }
        element.textContent = likeCount.toString();

    } catch (error) {
        console.error('Error liking the post:', error);
    }
}