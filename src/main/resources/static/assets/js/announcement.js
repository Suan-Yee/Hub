const btn = document.getElementById('hpyBtn');

const editBtn = document.getElementById('hpyEditBtn')


/*to check button disable*/
const checkInputs = () => {
    const title = document.getElementById('title').value.trim();
    const startDate = document.getElementById('startdate').value.trim();
    const endDate=document.getElementById('enddate').value.trim();
    const message = document.getElementById('message').value.trim();
    if (title === '' || startDate === '' || endDate === '' || message === '') {
        btn.disabled = true;
        editBtn.disabled=true;
        console.log("Disabled true")
    } else {
        btn.disabled = false;
        editBtn.disabled=false;
        console.log("Disabled False")
    }
}
document.getElementById('title').addEventListener('input', checkInputs);
document.getElementById('startdate').addEventListener('input', checkInputs);
document.getElementById('enddate').addEventListener('input', checkInputs);
document.getElementById('message').addEventListener('input', checkInputs);
checkInputs();

/*to create announcement*/
btn.addEventListener('click', async ()  => {
    await createAnnouncement();
 });
const createAnnouncement = async () => {
    const title = document.getElementById('title').value.trim();
    const startDate = document.getElementById('startdate').value.trim();
    const endDate=document.getElementById('enddate').value.trim();
    const message = document.getElementById('message').value.trim();

    const response = await fetch('/announcement/createAnnouncement', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            title : title,
            message : message,
            announcementStartDate : startDate,
            announcementEndDate: endDate
        })
    })
    if (!response.ok) {
        console.error('Failed to create announcement:', response.status, response.statusText);
        // Log response body if needed
        const responseBody = await response.text();
        console.error('Response body:', responseBody);
        throw new Error("Cannot create");
    } else {
        console.log('Announcement created successfully!');
        $('#eventform').modal('hide');
        location.reload();
    }
}

/*to clear input data */
function resetFormFields() {
    document.getElementById("title").value = "";
    document.getElementById("startdate").value = "";
    document.getElementById("enddate").value = "";
    document.getElementById("message").value = "";
}

document.getElementById('resetBtn').addEventListener('click', function() {
    resetFormFields();
});

document.getElementById('eventform').addEventListener('hidden.bs.modal', function () {
    resetFormFields();
});

/*to show event list*/
document.addEventListener('DOMContentLoaded', function() {
    fetch('/announcement/showAnnouncement')
        .then(response => response.json())
        .then(users => {
            const cardContainer = document.getElementById('row-container');
            users.forEach(data => {
                const StartDate = new Date(data.announcementStartDate);
                const formattedStartDay = StartDate.toLocaleDateString('en-GB', { day: 'numeric' });
                const formattedStartMonth = StartDate.toLocaleDateString('en-GB', { month: 'short' });

                const EndDate=new Date(data.announcementEndDate);
                const formattedEndDay=EndDate.toLocaleDateString('en-GB',{ day: 'numeric' });
                const formattedEndMonth=EndDate.toLocaleDateString('en-GB', { month: 'short' });

                const cardDiv = document.createElement('div');
                cardDiv.classList.add('col-lg-4');
                cardDiv.innerHTML = `
                            <div class="card card-margin">
                                <div class="card-body pt-0" style="float: top;margin-top: 5px; height: 200px">
                                    <div class="widget-49">
                                        <div class="widget-49-title-wrapper">
                                            <div class="widget-49-date-primary">
                                                <span class="widget-49-date-day">${formattedStartDay}</span>
                                                <span class="widget-49-date-month">${formattedStartMonth}</span>
                                            </div>
                                            <div class="widget-49-meeting-info">
                                                <span class="widget-49-pro-title">${data.title}</span>
                                                <span class="widget-49-meeting-time">${formattedStartDay} ${formattedStartMonth} To ${formattedEndDay} ${formattedEndMonth}</span>
                                            </div>
                                        </div>
                                       <div class="widget-49-meeting-item">
                                        <p>${data.message}</p>
                                        </div>
                                        <div class="widget-49-meeting-action">
                                            <a onclick="getAnnouncement(${data.id})" class="btn btn-sm btn-flash-border-primary" style="color:#09fd57;">Edit</a>
                                            <a href="#" onclick="confirmDelete(${data.id})" class="btn btn-sm btn-flash-border-primary" style="color:red;">Delete</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        `;
                cardContainer.appendChild(cardDiv);
            });
        })
        .catch(error => console.error('Error fetching users:', error));
});

/*to delete announcement*/
function confirmDelete(id){
    console.log(id);
    const modalHtml=`
<div class="modal fade" id="deleteModal" data-bs-keyboard="false" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="exampleModalLabel">Announcement Deletion</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      
      <div class="modal-body">
        Are you sure want to delete this Announcement?
      </div>
      <div class="modal-footer">
        <button type="button" id="deleteConfirmBtn" class="btn btn-primary">Yes</button>
        <button type="button" id="deleteCancelBtn" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
      </div>
    </div>
  </div>
</div>

<!--<div id="deleteModal" class="modal fade">
  <div class="modal-dialog modal-confirm">
    <div class="modal-content">
      <div class="modal-header">
        <div class="icon-box">
          <i class="material-icons">&#xE5CD;</i>
        </div>
        <h4 class="modal-title">Are you sure?</h4>
        <button type="button" class="close" data-bs-dismiss="modal" aria-hidden="true">&times;</button>
      </div>
      <div class="modal-body">
        <p>Do you really want to delete these records? This process cannot be undone.</p>
      </div>
      <div class="modal-footer">
        <button type="button"  id="deleteCancelBtn" class="btn btn-info" data-bs-dismiss="modal">Cancel</button>
        <button type="button" id="deleteConfirmBtn" class="btn btn-danger">Delete</button>
      </div>
    </div>
  </div>
</div>-->
    `;
    document.body.insertAdjacentHTML('beforeend', modalHtml);

    const modal = document.getElementById('deleteModal');
    const confirmBtn = document.getElementById('deleteConfirmBtn');
    const cancelBtn = document.getElementById('deleteCancelBtn');

    confirmBtn.onclick = function() {
        deleteAnnouncement(id);
        /*modal.style.display = 'none';*/
        $('#deleteModal').modal('hide');
    }
    $('#deleteModal').modal('show');
}

function deleteAnnouncement(id) {
    fetch(`/announcement/delete/${id}`)
        .then(response => {
            if (response.ok) {
                location.reload();
            } else {
                throw new Error('Failed to delete announcement');
            }
        })
        .catch(error => console.error('Error deleting announcement:', error));
}


/*to show edit form and data*/
function getAnnouncement(id){
    fetch(`/announcement/setUpeditevent/${id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(announcement=>{
            document.getElementById('id').value=announcement.id;
            document.getElementById('title').value = announcement.title;
            document.getElementById('startdate').value = announcement.announcementStartDate;
            document.getElementById('enddate').value = announcement.announcementEndDate;
            document.getElementById('message').value = announcement.message;
            console.log(announcement);

            const header =document.getElementById('eventheader');
            header.textContent='Edit Event Detail';
            btn.style.display  ='none';
            editBtn.value='Edit';
            editBtn.style.display = 'block';
            editBtn.setAttribute('onclick', `editAnnouncement()`);
            checkInputs();
            $('#eventform').modal('show');
            setTimeout(function() {
                setMinimumDate();
            }, 100);
            setMinEndDate();

            $('#eventform').on('hidden.bs.modal', function () {
                header.textContent='Add Event Detail';
                editBtn.style.display='none';
                btn.value = 'Save';
                btn.style.display  ='block';
                btn.disabled = true;
                btn.setAttribute('onclick', 'event_form()');
            });

            $('#eventform').on('show.bs.modal', function () {
                checkInputs();
                /*setMinimumDate();*/
                setTimeout(function() {
                    setMinimumDate();
                    setMinEndDate();
                }, 100);
            });
        })
        .catch(error => {
            console.error("Error fetching existing announcement:", error);
        });
}

/*edit announcement*/
function editAnnouncement(){
    const id=document.getElementById('id').value.trim();
    const title = document.getElementById('title').value.trim();
    const startDate = document.getElementById('startdate').value.trim();
    const endDate=document.getElementById('enddate').value.trim();
    const message = document.getElementById('message').value.trim();
    console.log(id);
    console.log(title);
    console.log(startDate);
    console.log(endDate);
    console.log(message);
    fetch(`/announcement/edit/${id}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            id: id,
            title: title,
            announcementStartDate: startDate,
            announcementEndDate: endDate,
            message: message
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(result => {
            console.log("Announcement edited successfully:", result);
            $('#eventform').modal('hide');
            location.reload();
        })
        .catch(error => {
            console.error("Error editing announcement:", error);
        });
}

/*date validate*/
const startDateInput = document.getElementById("startdate");
const endDateInput = document.getElementById("enddate");

document.addEventListener("DOMContentLoaded", function() {
    setMinimumDate();
});

function setMinimumDate() {
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth() + 1; // January is 0!
    var yyyy = today.getFullYear();

    if (dd < 10) {
        dd = '0' + dd;
    }
    if (mm < 10) {
        mm = '0' + mm;
    }
    today = yyyy + '-' + mm + '-' + dd;
    /*var today = new Date().toISOString().split('T')[0];*/
    startDateInput.setAttribute("min", today);
    endDateInput.setAttribute("min", today);
}


function setMinEndDate() {
    var startDate = new Date(startDateInput.value);
    // Set the minimum end date to be one day after the start date
    var minEndDate = new Date(startDate);
    minEndDate.setDate(startDate.getDate());
    // Set the minimum end date in the end date input
    endDateInput.min = minEndDate.toISOString().split('T')[0];
    // If the currently selected end date is before the minimum end date, reset it
    if (new Date(endDateInput.value) < minEndDate) {
        endDateInput.value = minEndDate.toISOString().split('T')[0];
    }
}
startDateInput.addEventListener('change', setMinEndDate);