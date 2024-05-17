export const openUpdateModal=async (postId) =>{
    fetch(`/post/${postId}`)
        .then(response => {
            if (!response.ok){
                throw new Error('Network response is not OK!');
            }
            return response.json()})
        .then(post =>{
            console.log('Post details:', post);
            const modal = document.getElementById('updateModal');
            const id = modal.querySelector('#updateId');
            const text = modal.querySelector('#post-body');

            id.value = post.id;
            text.value = post.content.text;

            $(modal).modal('show');
        })
        .catch(error =>{
            console.error('Error fetching post details' , error);
        });
}