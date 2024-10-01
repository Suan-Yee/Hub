document.addEventListener("DOMContentLoaded", function() {
    const skillInput = document.getElementById("skill-input");
    const skillsContainer = document.getElementById("skills-container");
    const addButton = document.getElementById("skillAddBtn");
    const doneButton=document.getElementById("skillDoneBtn");
    const skills = [];
    doneButton.disabled = true;

    // Function to create a skill box
    function createSkillBox(skillName) {
        const skillBox = document.createElement("div");
        skillBox.classList.add("skill-box");

        const skillElement = document.createElement("div");
        skillElement.classList.add("skill");
        skillElement.textContent = skillName;

        const cancelElement = document.createElement("span");
        cancelElement.classList.add("cancel-sign");
        cancelElement.textContent = "✖";
        cancelElement.addEventListener("click", function () {
            skillBox.remove();
            removeSkillFromArray(skillName);
            updateButtonStatus();
        });

        skillBox.appendChild(skillElement);
        skillBox.appendChild(cancelElement);
        return skillBox;
    }

    // Function to add a skill
    function addSkill() {
        const skill = skillInput.value.trim();
        if (skill !== "") {
            const skillBox = createSkillBox(skill);
            skillsContainer.appendChild(skillBox);
            skillInput.value = ""; // Clear the input after adding skill
            skills.push(skill); // Add skill to array
            console.log(skills);
            updateButtonStatus();
        }
    }

    // Function to remove a skill from the array
    function removeSkillFromArray(skillName) {
        const index = skills.indexOf(skillName);
        if (index !== -1) {
            skills.splice(index, 1);
        }
    }

    // Function to update button status based on input field
    function updateButtonStatus() {
        doneButton.disabled = skillInput.value.trim() === "" && skills.length === 0;
    }

    // Event listener for pressing Enter in the input field
    skillInput.addEventListener("keypress", function (event) {
        if (event.key === "Enter") {
            addSkill();
        }
    });
    addButton.addEventListener("click", () =>{
        addSkill();
    });

    doneButton.addEventListener("click", () => {
        addSkill();
        saveSkills(skills);
    });

    function saveSkills(skills) {
        console.log("Add Button is clicked.")
        fetch(`/saveSkills`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(skills)
        })
            .then(response => {
                if (response.ok) {
                    showToast("Add Skills","green");
                    skillModalBox.hide();
                } else {
                    throw new Error('Failed to save skills.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    }
});