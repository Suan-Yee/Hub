/*to rest input feild*/
document.getElementById('reset-button').addEventListener('click', function() {
    document.getElementById("guidelineDescription").value="";
});

document.addEventListener('DOMContentLoaded', function () {
    $('#guidelineModal').on('hidden.bs.modal', function () {
        document.getElementById("guidelineDescription").value = "";
    });
});

/*create GuideLines*/
const savebtn = document.getElementById("savebutton");
savebtn.addEventListener('click',async ()=>{
    await createPolicy();
})

const createPolicy = async () =>{
    const message=document.getElementById("guidelineDescription").value.trim();
console.log(message);

    const response =await fetch('/user/savePolicy',{
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            message: message
        })
    })
    console.log(message);
    if(!response.ok){
        console.error('Failed to create policy:', response.status, response.statusText);
        const responseBody = await response.text();
        console.error('Response body:', responseBody);
        throw new Error("Cannot create");
    } else {
        console.log('Policy created successfully!');
        $('#guidelineModal').modal('hide');
        location.reload();
    }
}

/*show guideLines*/
document.addEventListener("DOMContentLoaded", () => {
    displayPloicy();
});

function displayPloicy() {
    fetch('/user/getPolicy')
        .then(response => response.json())
        .then(data => {
            console.log(data); // Log the data object to see its structure
            const guidelineList = document.getElementById("guideline-list");
            guidelineList.innerHTML = "";
            const createPolicyBtn =document.getElementById("createPolicyBtn");
            const policyContainer = document.querySelector(".policy-container");

            // Check if the 'message' property exists before using it
            if (data && data.length > 0 && data[0].message) {
                const guidelines = data[0].message.split('\n'); // Split by newline

                guidelines.forEach(guideline => {
                    if (guideline.trim() !== '') { // Skip empty guidelines
                        const listItem = document.createElement("li");
                        listItem.textContent = guideline.trim(); // Trim to remove extra spaces
                        guidelineList.appendChild(listItem);
                    }
                });
                createPolicyBtn.style.display = 'none';
                policyContainer.style.display = 'block';
            } else {
                console.error('No guidelines found in the response:', data);
                createPolicyBtn.style.display = 'block';
                policyContainer.style.display = 'none';

            }
        })
        .catch(error => {
            console.error('Error fetching guidelines:', error);
        });
}

let id=null;
/*to show edit policy form*/
function enableEditMode() {
    fetch('/user/getPolicy')
        .then(response => response.json())
        .then(data => {
            id=data[0].id;
            console.log("ID",id);
            if (data  && data.length > 0 && data[0].message) {
                const guidelines = data[0].message.split('\n');
                document.getElementById("view-mode").style.display = "none";
                document.getElementById("edit-mode").style.display = "flex";
                document.getElementById("guideline-editor").value = guidelines.join("\n");
            } else {
                console.error('No guidelines found in the response:', data);
            }
        })
        .catch(error => {
            console.error('Error fetching guidelines:', error);
        });
}

function saveChanges() {
    const message = document.getElementById("guideline-editor").value.trim();
    const data = {
        id: id,
        message: message
    };
    console.log(message);
    fetch('/user/updatePolicy', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(result => {
            if (result.id) {
                console.log('Guidelines saved successfully');
                document.getElementById("view-mode").style.display = "flex";
                location.reload();
                document.getElementById("edit-mode").style.display = "none";
                document.getElementById("guideline-list").innerText = guidelines.split('\n').map(guideline => `<li>${guideline}</li>`).join('');
            } else {
                console.error('Error saving guidelines:', result);
            }
        })
        .catch(error => {
            console.error('Error saving guidelines:', error);
        });
}

function cancelEdit() {
    document.getElementById("edit-mode").style.display = "none";
    document.getElementById("view-mode").style.display = "flex";
}
