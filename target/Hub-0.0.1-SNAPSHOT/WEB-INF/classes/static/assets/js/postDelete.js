export const deletePost= async (postId) => {
    try {
        const response = await fetch('post/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                postId: postId
            })
        });
        const postToDelete = document.getElementById(`post-${postId}`);
        const deletePostModal = document.getElementById(`deletePostModal-${postId}`);
        const deletePostModalElement = bootstrap.Modal.getInstance(deletePostModal);
        const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
        console.log("Cancel Button ", cancelDeleteBtn);
        if(response.ok){
            cancelDeleteBtn.click();
            deletePostModalElement.hide();
            showToast("Successfully Deleted","#0073ff");
            postToDelete.remove();
        }
    }catch (e) {
        console.error('Error : ', e);
    }
}


