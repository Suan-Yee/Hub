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
                       class="${heartClass} fa-heart" style="cursor: pointer; ${heartColor}; padding: 5px"
                       data-liked="${dataLiked}">${likeCount}</i>
            <i id="bookMarkIcon-${post.id}" data-liked="${bookMark}" class="${bookMarkClass} fa-bookmark" style="cursor: pointer; color: #75bef5; padding: 5px"></i>
          
        </div>`;
};

// ${post.owner ? `
//            <i id="update-post-${post.id}" class="fa-solid fa-pen-to-square" style="cursor: pointer; padding: 5px;"></i>` : ''}
//
// ${post.admin || post.owner? `
//             <i data-bs-toggle="modal" data-bs-target="#deletePostModal-${post.id}" class="fa-solid fa-trash" style="cursor: pointer; padding: 5px;"></i>
//
// <!-- Modal -->
// <div class="modal fade" id="deletePostModal-${post.id}" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
//     <div class="modal-dialog modal-dialog-centered">
//     <div class="modal-content">
//     <div class="modal-header">
//     <h1 class="modal-title fs-5" id="exampleModalLabel">Are You Sure Want to Delete?</h1>
// <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
// </div>
// <div class="modal-footer">
//     <button type="button" id="cancelDeleteBtn" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
//     <button type="button" class="btn btn-danger" id="delete-post-${post.id}">Delete</button>
// </div>
// </div>
// </div>
// </div>
// ` : ''}