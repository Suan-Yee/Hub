document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('post-update-form');

    form.addEventListener('submit', async function(event) {
        event.preventDefault();

        const id = document.getElementById('updateId').value;
        const text = document.getElementById('post-body').value;
        const fileInputs = document.querySelectorAll('.files');

        console.log(id);
        console.log(text);

        const formData = new FormData();
        formData.append('exitId', id);
        formData.append('updateText', text);

        /*for (let i = 0; i < files.length; i++) {
            formData.append('updateFiles', files[i]);
        }*/

        fileInputs.forEach(input => {
            const files = input.files;
            for (let i = 0; i < files.length; i++) {
                formData.append('updateFiles', files[i]);
            }
        });

        try {
            const response = await fetch('/post/update', {
                method: 'POST',
                body: formData
            });

            /*if (response.ok) {
                const data = await response.json();
                console.log('Response:', data);
                // Optionally close the modal here if desired
                const updateModal = new bootstrap.Modal(document.getElementById('updateModal'));
                updateModal.hide();
            } else {
                console.error('Error:', response.statusText);
            }*/

            /*if (response.ok) {
                const data = await response.json();
                console.log(data);
            } else {
                console.error('Error', response.statusText);
            }*/
        } catch (e) {
            console.error('Error', e);
        }
        this.submit();
    });
});

