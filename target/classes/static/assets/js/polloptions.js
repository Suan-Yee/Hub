const pollCreateBtn = document.getElementById('pollCreateBtn');
const pollQuestion = document.getElementById('pollQuestion');
const pollOptions = document.getElementById('pollOptions');
const duration = document.getElementById('duration');
const questionErrorMessage = document.querySelector('.questionErrorMessage');
let selectedGroup = document.getElementById('selectedGroup');
let optionInputs = []; // Array to store input fields
let pollQuestionValue;


// Create initial input field
createNewOptionInput();
pollCreateBtn.disabled = true;
pollQuestion.oninput = function () {
    questionErrorMessage.innerHTML = '';
    if(this.value === ''){
        questionErrorMessage.innerHTML = 'Poll Question can not be blank!';
        pollCreateBtn.disabled = true;
    }
}


// Function to create a new input field
function createNewOptionInput() {
    if(optionInputs.length > 1){
        pollCreateBtn.disabled = false;
    }
    const newOptionInput = document.createElement('input');
    newOptionInput.setAttribute('type', 'text');
    newOptionInput.setAttribute('class', 'form-control');
    newOptionInput.setAttribute('placeholder', 'Add option');
    newOptionInput.style.marginBottom = '10px';
    pollOptions.appendChild(newOptionInput);
    optionInputs.push(newOptionInput); // Store input field

    // Add event listener to the new input field
    newOptionInput.addEventListener('input', function () {
        let lastInput = optionInputs[optionInputs.length - 1];
        let lastInputValue = lastInput.value.trim();
        if (lastInputValue.length > 0) {
            createNewOptionInput();
        } else {
            // Check if there are more than one empty input boxes
            let emptyCount = 0;
            for (let input of optionInputs) {
                if (input.value.trim() === "") {
                    emptyCount++;
                }
            }
            if (emptyCount >= 2) {
                pollOptions.removeChild(lastInput);
                optionInputs.pop();
            }
        }
    });


}

pollCreateBtn.addEventListener("click",()=>{
    let selectedDurationValue = duration.value;

    sendToServer(selectedDurationValue);
});
pollQuestion.addEventListener("input",()=>{
    pollQuestionValue = pollQuestion.value.trim();
})

function sendToServer(selectedDurationValue){
    const optionInputValues = [];
    if(optionInputs.length>0){
        pollOptions.removeChild(optionInputs.pop());
    }
    optionInputs.forEach(option=>{
        optionInputValues.push(option.value);
    });
    console.log("Poll Question : "+pollQuestionValue);
    console.log("Option Values : "+optionInputValues);
    console.log("durationSelectedValue :"+selectedDurationValue);
    const data = {
        pollQuestion: pollQuestionValue,
        pollOptions: optionInputValues,
        duration: selectedDurationValue,
        groupId:selectedGroup.value
    };
    fetch('/createPoll',{
        method:'POST',
        headers:{'Content-type':'application/json'},
        body:JSON.stringify(data),
    })
        .then(response=>{
            if(response.ok)
                return response.json();
            else throw new Error("Network fail");
        })
        .then(success=>{
            console.log(success);
            if(success != null){
                document.getElementById('pollBoxCloseBtn').click();
                showToast("Post Successful","#0073ff");
            }
            else {
                showToast("Fail to create poll","red")
            }
        })
        .catch(error=>{
            console.error("Error Occurred :"+error);
        })
}

