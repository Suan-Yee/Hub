document.addEventListener('DOMContentLoaded', async () => {
    const currentUrl = window.location.href;
    let urlParts = currentUrl.split('/');
    let lastPart = urlParts[urlParts.length - 1];
    let pollsData = [];

    const postContainer = document.querySelector('.groupPostContainer');
    const topPostContainer = document.querySelector('.topPostContainer');
    const aboutContainer = document.querySelector('.about_community');
    const chatOpenDiv = document.querySelector('.openChat');
    const groupPoll = document.getElementById('groupPoll');
    const groupPollContainer = document.querySelector('.group-polls-container');

    groupPoll.addEventListener('click', () => {
        if (!groupPoll.classList.contains('activeTab')) {
            fetchPollFromGroup(lastPart);
            postContainer.style.display = 'none';
            groupPollContainer.style.display = 'block';
            topPostContainer.style.display = 'none';
            aboutContainer.style.display = 'none';
            chatOpenDiv.style.display = 'none';
            setActiveTab(groupPoll);
        }
    });

    const fetchPollFromGroup = async (groupId) => {
        try {
            const response = await fetch(`/get-all-poll-by-groupId/${groupId}`);
            const groups = await response.json();
            pollsData = groups;
            groups.forEach(pollOption => {
                const poll = {
                    id: pollOption.id,
                    question: pollOption.question,
                    answers: pollOption.answers,
                    pollCount: pollOption.pollCount,
                    answersWeight: pollOption.answersWeight,
                    expiredDate: pollOption.expiredDate,
                    userId: pollOption.user.id,
                    userImage: pollOption.user.photo,
                    userName: pollOption.user.name,
                    isLoginUserPoll: pollOption.isLoginUserPoll,
                    selectedAnswer: -1
                };
                const pollHTML = createPollHTMLForGroup(poll);
                appendPollToContainerForGroup(pollHTML);
                checkAnswerUserVotedForGroup(poll);
            });
        } catch (error) {
            console.error('Error fetching polls:', error);
        }
    };

    function setActiveTab(activeTab) {
        document.querySelectorAll('#mediaTag li').forEach(tab => {
            tab.classList.remove('activeTab');
        });
        activeTab.classList.add('activeTab');
    }

    const createPollHTMLForGroup = (poll) => {
        const userImage = poll.userImage != null ? poll.userImage : 'http://res.cloudinary.com/dwdplsd2c/image/upload/v1712047327/fllinoozg9hh1ghk5mef.png';
        const userName = poll.userName;
        const isLoginUserPoll = poll.isLoginUserPoll;
        let answersHTML = "";
        const trashIconHTML = isLoginUserPoll ? `<i class="fa-regular fa-trash-can trash-button" id="poll-${poll.id}" title="delete"></i>` : '';
        for (const [answerId, answer] of Object.entries(poll.answers)) {
            answersHTML += `
                <div class="answer" data-answer-id="${answerId}" data-poll-id="${poll.id}">
                    ${answer}
                    <span class="percentage-bar"></span>
                    <span class="percentage-value"></span>
                </div>`;
        }
        return `
            <div class="poll" style="background-color: #eaecef" data-id="${poll.id}">
                <div class="poll-user-profile">
                    <img src="${userImage}">
                    <a href="userprofile/${poll.userId}"><div class="poll-user-name">${userName}</div></a>
                    ${trashIconHTML}
                </div>
                <div class="question">${poll.question}
                    <div class="pollInfo-container">
                        <div class="totalVote pollDetailResult" data-toggle="modal" data-target="#pollTotalVoteResultForGroup" data-poll-id="${poll.id}">
                            ${poll.pollCount} votes
                        </div>
                        <div class="expiration-date">${poll.expiredDate}</div>
                    </div>
                </div>
                <div class="answers">${answersHTML}</div>
            </div>`;
    };

    const markAnswerForGroup = (poll, answerId, i) => {
        poll.selectedAnswer = +i;
        const answers = document.querySelectorAll(`.poll[data-id="${poll.id}"] .answers .answer`);
        answers.forEach(answer => {
            answer.classList.remove("selected");
            answer.classList.add("disabled");
            answer.onclick = null;
        });

        const selectedAnswer = document.querySelector(`.poll[data-id="${poll.id}"] .answers .answer:nth-child(${i + 1})`);
        if (selectedAnswer) {
            selectedAnswer.classList.add("selected");
        }
        showRemoveVoteButtonForGroup(answerId, poll);
        showResultsAfterClickForGroup(poll);
    };

    let userSelectedAnswerIndexForGroup = -1;

    const checkAnswerUserVotedForGroup = async (poll) => {
        try {
            const response = await fetch('/check-answer-user-voted?pollId=' + poll.id);
            const data = await response.json();
            if ("optionIdUserVoted" in data) {
                let userVotedOptionId = data.optionIdUserVoted;
                if (userVotedOptionId !== null) {
                    let selectedAnswerIndex = Object.keys(poll.answers).findIndex(key => parseInt(key) === userVotedOptionId);
                    if (selectedAnswerIndex !== -1) {
                        userSelectedAnswerIndexForGroup = selectedAnswerIndex;
                        poll.selectedAnswer = selectedAnswerIndex;
                        markAnswerForGroup(poll, userVotedOptionId, selectedAnswerIndex);
                    }
                }
            }
        } catch (error) {
            console.error('Error checking user voted answer:', error);
        }
    };

    const appendPollToContainerForGroup = (pollHTML) => {
        const pollsContainer = document.querySelector('.group-polls-container');
        pollsContainer.insertAdjacentHTML('beforeend', pollHTML);
        const newPollElement = pollsContainer.lastChild;
        setupAnswerEventListenersForGroup(newPollElement);
    };

    let initializedPollsForGroup = new Set();

    const setupAnswerEventListenersForGroup = (pollElement) => {
        const pollId = pollElement.dataset.id;

        if (!initializedPollsForGroup.has(pollId)) {
            const answers = pollElement.querySelectorAll('.answer');
            answers.forEach(answer => {
                // Define click handler function
                const clickHandler = async () => {
                    const answerId = answer.getAttribute('data-answer-id');
                    await markAnswerAfterClickForGroup(answerId, pollId);
                };
                // Add event listener
                answer.addEventListener('click', clickHandler);
            });

            initializedPollsForGroup.add(pollId);
        }
    };

    const markAnswerAfterClickForGroup = async (answerId, pollId) => {
        const poll = pollsData.find(poll => poll.id === parseInt(pollId));
        console.log("markAnswerAfterClick Poll :",poll)
        const selectedAnswerIndex = Object.keys(poll.answers).findIndex(key => key === answerId);
        poll.selectedAnswer = +selectedAnswerIndex;

        const answers = document.querySelectorAll(`.poll[data-id="${poll.id}"] .answers .answer`);
        answers.forEach((answer) => {
            answer.classList.add("disabled");
        });

        const selectedAnswer = document.querySelector(`.poll[data-id="${poll.id}"] .answer:nth-child(${poll.selectedAnswer + 1})`);
        if (selectedAnswer) {
            selectedAnswer.classList.add("selected");
        }
        let result = await saveUserVotedAnswerForGroup(poll, answerId);
        if (result === true) {
            const answerIndex = Object.keys(poll.answers).findIndex(key => key === answerId);
            poll.answersWeight[answerIndex] +=1;
            console.log("Poll's answersWeight ", poll.answersWeight);
            poll.pollCount +=1;
            showResultsAfterClickForGroup(poll);
            showRemoveVoteButtonForGroup(answerId, poll);
        }
    };


    function showRemoveVoteButtonForGroup(answerId, poll) {
        const existingRemoveButtonForGroup = document.querySelector(`.poll[data-id="${poll.id}"] .remove-vote`);

        // If a remove vote button exists, remove it
        if (existingRemoveButtonForGroup) {
            existingRemoveButtonForGroup.remove();
        }

        const removeVoteButtonForGroup = document.createElement('button');
        removeVoteButtonForGroup.textContent = 'Remove Vote';
        removeVoteButtonForGroup.classList.add('remove-vote');
        removeVoteButtonForGroup.addEventListener('click', () => removeVoteForGroup(answerId, poll.id));

        const answersContainer = document.querySelector(`.poll[data-id="${poll.id}"] .answers`);
        answersContainer.insertAdjacentElement('beforeend', removeVoteButtonForGroup);
    }

    const removeVoteForGroup = async (answerId, pollId) => {
        try {
            const response = await fetch('/remove-user-voted-answer', {
                method: 'POST',
                headers: { 'Content-type': 'application/json' },
                body: JSON.stringify({
                    answerId: answerId,
                    pollId: pollId
                })
            });

            if (!response.ok) {
                throw new Error('Network request failed');
            }

            const data = await response.json();
            if (data.success) {
                handleRemoveVoteForGroup(pollId, answerId);
            } else {
                console.error('Failed to remove the vote');
            }
        } catch (error) {
            console.error('Error while removing vote:', error);
        }
    };

    const handleRemoveVoteForGroup = (pollId, answerId) => {
        const pollElement = document.querySelector(`.poll[data-id="${pollId}"]`);
        let pollObj = pollsData.find(p => p.id == pollId);
        if (userSelectedAnswerIndexForGroup !== -1) { // Check if user has already voted or not
            if (pollObj.answersWeight[userSelectedAnswerIndexForGroup] < 0) {
                pollObj.answersWeight[userSelectedAnswerIndexForGroup] = 0;
            } else {
                pollObj.answersWeight[userSelectedAnswerIndexForGroup] -= 1;
            }
            userSelectedAnswerIndexForGroup = -1; // After clicking remove button, assign 0 to selectedAnswerIndex
        } else {
            const answerIndex = Object.keys(pollObj.answers).findIndex(key => key === answerId);
            pollObj.answersWeight[answerIndex] -= 1;
            if (pollObj.answersWeight[answerIndex] < 0) {
                pollObj.answersWeight[answerIndex] = 0;
            }
        }

        pollObj.selectedAnswer = -1;
        pollObj.pollCount -= 1;

        const totalVoteElement = pollElement.querySelector('.totalVote');
        const currentVoteCount = parseInt(totalVoteElement.textContent);
        if (!isNaN(currentVoteCount) && currentVoteCount > 0) {
            totalVoteElement.textContent = `${currentVoteCount - 1} votes`;
        }

        const answers = pollElement.querySelectorAll('.answer');
        answers.forEach(answer => {
            answer.classList.remove('selected', 'disabled');
            const percentageBar = answer.querySelector('.percentage-bar');
            const percentageValue = answer.querySelector('.percentage-value');
            percentageBar.style.width = '0%';
            percentageValue.textContent = '';
        });

        const removeBtn = pollElement.querySelector('.remove-vote');
        removeBtn.remove();
        setupAnswerEventListenersForGroup(pollElement);
    };

    const saveUserVotedAnswerForGroup = async (poll, answerId) => {
        try {
            const response = await fetch('/save-user-voted-answer', {
                method: 'POST',
                headers: { 'Content-type': 'application/json' },
                body: JSON.stringify({ pollId: poll.id, answerId: answerId })
            });
            const result = await response.json();
            return result;
        } catch (error) {
            console.error('Error saving user voted answer:', error);
            return false;
        }
    };

    const showResultsAfterClickForGroup = (pollObj) => {
        const totalVotes = pollObj.pollCount;
        const answers = document.querySelectorAll(`.poll[data-id="${pollObj.id}"] .answers .answer`);
        console.log("answers ",answers);
        const totalVoteDivElement = document.querySelector(`.poll[data-id="${pollObj.id}"] .pollInfo-container .totalVote`);
        totalVoteDivElement.innerHTML = '';
        totalVoteDivElement.innerHTML = `${totalVotes} votes`;
        answers.forEach((answer, i) => {
            let percentage = Math.floor((pollObj.answersWeight[i]) * 100 / totalVotes);
            const percentageBar = answer.querySelector(".percentage-bar");
            if (percentageBar) {
                percentageBar.style.background = "#ccd8f1";
                percentageBar.style.width = `${percentage}%`;
            }
            answer.querySelector(".percentage-value").innerText = `${percentage}%`;
        });
    };

    document.addEventListener('click', function (event) {
        if (event.target.classList.contains('pollDetailResult')) {
            const pollId = event.target.dataset.pollId;
            const modal = document.getElementById('pollTotalVoteResultForGroup');
            const pollResultModal = new bootstrap.Modal(modal); // Assuming you are using Bootstrap 5
            pollResultModal.show();
            const pollObj = pollsData.find(poll => poll.id === parseInt(pollId));
            const modalTitle = modal.querySelector('.modal-title');
            modalTitle.innerText = pollObj.pollCount +" votes";

            const pollOptionLi = modal.querySelector('.pollOptionLi');
            console.log("PollOptionLi",pollOptionLi);
            pollOptionLi.innerHTML = '';

            // Loop through the answers and create list items
            Object.entries(pollObj.answers).forEach(([answerId, answer], index) => {
                const li = document.createElement('li');
                li.innerText = `${answer} (${pollObj.answersWeight[index] || 0})`;
                li.dataset.optionKey = answerId;  // Store the key of the answer
                li.dataset.pollId = pollId;  // Store the pollId
                pollOptionLi.appendChild(li);
            });

            // Auto-select the first list item
            const firstLi = pollOptionLi.querySelector('li');
            if (firstLi) {
                firstLi.classList.add('activeLi');  // Highlight the first item
                firstLi.click();  // Trigger click event for the first item to load its details
            }
        }
    });

    document.querySelector('.pollOptionLi').addEventListener('click',function (event){
        const modal = document.getElementById('pollTotalVoteResultForGroup');
        const pollContentCount = modal.querySelector('.pollCount-content');
        const pollOptionLi = modal.querySelector('.pollOptionLi');
        if(event.target.tagName ==='LI'){
            // Remove focus from all items
            Array.from(pollOptionLi.children).forEach(item => item.classList.remove('activeLi'));

            // Highlight the clicked item
            event.target.classList.add('activeLi');
            const optionId = event.target.dataset.optionKey;
            console.log('OptionId :',optionId);
            fetch('/get-votes-for-poll-option?answerId='+optionId,{
                method:'GET'
            })
                .then(response=>response.json())
                .then(users=>{
                    if(users!==null && users.length >0){
                        pollContentCount.innerHTML = '';
                        let tableContent = `
                    <table class="table table-striped">
                            
                   `;

                        users.forEach(user=>{
                            const photoUrl = user.photo ? user.photo : "http://res.cloudinary.com/dwdplsd2c/image/upload/v1713536112/fllinoozg9hh1ghk5mef.png";
                            tableContent+=`
                        <tr>
                            <td><img class="rounded-circle me-2" height="50" width="50" src="${photoUrl}" alt="${user.name}"> ${user.name}</td>
                        </tr>
                       `
                        });
                        tableContent += `
                        </tbody>
                    </table>
                `;
                        pollContentCount.innerHTML = tableContent;
                    }
                    else{
                        pollContentCount.innerHTML = '';
                        const notFoundMessage = document.createElement('h4');
                        notFoundMessage.innerText = 'No user are found for this option!!!';
                        pollContentCount.appendChild(notFoundMessage);
                    }
                });
        }
    });
});
document.addEventListener('DOMContentLoaded', function () {
    // Global scope within this function
    const pollContainer = document.querySelector('.group-polls-container');
    const deleteModal = document.getElementById('deletePollFromGroup');
    const deleteGroupPollModal = new bootstrap.Modal(deleteModal);
    const confirmDeleteButton = document.getElementById('confirmDeleteButton');
    let currentPollId = null; // Variable accessible throughout this function

    pollContainer.addEventListener('click', function (event) {
        const target = event.target;
        if (target.matches('[id^="poll-"]')) {
            currentPollId = target.id.split('-')[1]; // Update the currentPollId
            console.log(currentPollId); // Log the current poll ID
            deleteGroupPollModal.show(); // Show the modal
        }
    });

    confirmDeleteButton.addEventListener('click', async function () {
        if (currentPollId) { // Check if currentPollId is set
            try {
                await deleteGroupPoll(currentPollId); // Use the currentPollId
                deleteGroupPollModal.hide(); // Hide the modal after deletion
                showToast('Delete Successful', '#2d3436'); // Show success message
                document.querySelector(`[data-id="${currentPollId}"]`).remove();
            } catch (error) {
                console.error(error); // Log any errors
                showToast('Delete Failed', '#FF0000'); // Show error message
            }
        }
    });

    const deleteGroupPoll = async (pollId) => {
        console.log("Attempting to delete poll with ID:", pollId); // Log the poll ID being deleted
        const response = await fetch('/delete-poll', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id:pollId }) // Send the pollId in the request body
        });

        if (!response.ok) {
            throw new Error('Failed to delete');
        }

        const data = await response.json();
        console.log("Delete Poll ", data); // Log the response data
    };
});
