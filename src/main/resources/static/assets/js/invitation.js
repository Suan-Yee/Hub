window.onload = async function(){
    console.log('hello')

    await startUp()

}
const pathname = window.location.pathname;
const pathSegments = pathname.split('/'); // Split the pathname into segments
const lastSegment = pathSegments[pathSegments.length - 1]; // Get the last segment
let communityId = localStorage.getItem('communityIdForDetailPage');
async function startUp(){
    let communityId = localStorage.getItem('communityIdForDetailPage')

    let community = await fetch(`/getCommunity/${communityId}`)
    let data = await community.json()
    console.log(data)
    document.getElementById('communityImage').src = data.image
    document.getElementById('communityName').textContent = data.name
    document.getElementById('communityMembers').textContent = 10
}

const getAllUsers = async () => {
    const url = '/users';
    const data = await fetch(url);
    if (data.ok) {
        const dataResponse = await data.json();
        console.log('all users', dataResponse);
        return dataResponse;
    }
};

const getUsersByCommunityId = async (id) => {
    const url = `/user/${id}`;
    const data = await fetch(url);
    if (data.ok) {
        const dataResponse = await data.json();
        console.log('community users', dataResponse);
        return dataResponse;
    }
};
document.getElementById('userSearch').addEventListener('input', function() {
    const searchValue = this.value.toLowerCase();
    const usersList = document.getElementById('usersList');
    const allUsers = document.querySelectorAll('#usersList #container');

    allUsers.forEach(userContainer => {
        const userNameElement = userContainer.querySelector('.user-list-span');
        if (userNameElement) {
            const userName = userNameElement.textContent.toLowerCase();
            if (userName.includes(searchValue)) {
                userContainer.style.display = 'block';
            } else {
                userContainer.style.display = 'none';
            }
        }
    });
});



const fetchUsersNotInGroup = async () => {
    const response = await fetch(`/user/invited-user-display/${lastSegment}`);
    const data = await response.json();

    if(!response.ok){
        throw new Error("Response not ok")
    }
    console.log(data);
    return data;
}


async function populateCreateGroupFormForInvitation() {

    const usersList = document.getElementById('usersList');
    const users = await fetchUsersNotInGroup();

    usersList.innerHTML = '';
    users.forEach(user => {
        const userDiv = document.createElement('div');
        userDiv.innerHTML = `
            <input type="checkbox" name="user" id="user1" value="${user.id}"/>
            <label class="form-check-label">
                <span>${user.name}</span>
            </label>
        `;
        usersList.appendChild(userDiv);
    });
    $('#invitationFormModal').modal('show');
}


document.getElementById('inviteUserBtn').addEventListener('click', async () => {
    // Select all checked checkboxes
    const checkedCheckboxes = document.querySelectorAll('input[name="user"]:checked');

    // Extract values from the checked checkboxes and convert them to integers
    const userIds = Array.from(checkedCheckboxes).map(checkbox => parseInt(checkbox.value));

    // Log the userIds array to check the result
    console.log("UserIds", userIds);



    const data = {
        communityId: lastSegment,
        userIds: userIds
    };

    const url = "/user/invitationSend";

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();
        const invitationMessageElement = document.getElementById('invitationMessage');

        if (response.ok) {
            if (invitationMessageElement) {
                invitationMessageElement.innerHTML = result.message;
            } else {
                console.error("Element with ID 'invitationMessage' not found.");
            }
            $('#invitationFormModal').modal('hide');
        } else {
            console.error("Error processing invitation:", response.status);
            if (invitationMessageElement) {
                invitationMessageElement.innerHTML = 'Error processing invitation';
            } else {
                console.error("Element with ID 'invitationMessage' not found.");
            }
        }
    } catch (error) {
        console.error("Error:", error);
        const invitationMessageElement = document.getElementById('invitationMessage');
        if (invitationMessageElement) {
            invitationMessageElement.innerHTML = 'Error processing invitation';
        } else {
            console.error("Element with ID 'invitationMessage' not found.");
        }
    }
});

document.getElementById('inviteModalCloseBtn').addEventListener('click', () => {
    $('#invitationFormModal').modal('hide');
});



























