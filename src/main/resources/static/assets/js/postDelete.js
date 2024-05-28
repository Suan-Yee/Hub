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
       /* if (response.ok){
            const data = await response.json();
            console.log(data);
        }else{
            console.error('Error : ', response.statusText);
        }*/
    }catch (e) {
        console.error('Error : ', e);
    }
}


