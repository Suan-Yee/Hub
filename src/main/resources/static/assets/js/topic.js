async function createTopic(text) {
    try {
        const response = await fetch('/topic/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: text
            })
        });
        if (!response.ok) {
            throw new Error('Failed to create topic.');
        }
        const data = await response.json();
        console.log('Topic created successfully: ', data);
    }catch (e) {
        console.error("Error creating topic: " , e)
    }
}

async function updateTopic(topicId, text) {
    try {
        const response = await fetch('/topic/update', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                id: topicId,
                name: text
            })
        });
        if (!response.ok) {
            throw new Error('Failed to update topic.');
        }
        const data = await response.json();
        console.log('Topic updated successfully: ', data);
    }catch (e) {
        console.error('Error updating topic: ', e);
        
    }
}

async function deleteTopic(topicId) {
    try {
        const response = await fetch('/topic/delete', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                id: topicId
            })
        });
        if (!response.ok) {
            throw new Error('Failed to delete topic');
        }
        console.log('Topic deleted successfully');
    } catch (error) {
        console.error('Error deleting topic:', error);
    }
}