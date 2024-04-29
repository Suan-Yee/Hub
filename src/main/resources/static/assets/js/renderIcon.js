export const renderInteractionIcons = (post) => {

    const heartClass = post.likedByCurrentUser ? 'fa-solid' : 'fa-regular';
    const heartColor = post.likedByCurrentUser ? 'color: #c91818;' : '';
    const dataLiked = post.likedByCurrentUser ? 'true' : 'false';
    const likeCount = post.likes || '0';
    const bookMark = post.bookMark ? 'true' : 'false';
    const bookMarkClass = post.bookMark ? 'fa-solid' : 'fa-regular';
    const url = `/post/details/${post.id}`;
    return `
        <div class="content-icons d-flex justify-evenly">
            <a href="${url}"><i class="far fa-comment blue">${post.commentCount}</i></a>
            <i id="likeIcon-${post.id}"
                       class="${heartClass} fa-heart" style="cursor: pointer; ${heartColor}; padding-top: 2px"
                       data-liked="${dataLiked}">${likeCount}</i>
            <i id="bookMarkIcon-${post.id}" data-liked="${bookMark}" class="${bookMarkClass} fa-bookmark" style="cursor: pointer; color: #75bef5;"></i>
        </div>`;
};