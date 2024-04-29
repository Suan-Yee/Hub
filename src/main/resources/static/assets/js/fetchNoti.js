document.addEventListener('DOMContentLoaded',() => {
    fetchAllNoti();
})

const fetchAllNoti = async () => {

    const response = await fetch('/noti/all');

    if(!response.ok) {
        if (response.status === 404) {
            const noti_container = document.getElementById('notiContainer');
            noti_container.innerHTML = '<li style="margin: 5px">There is no notification yet.</li>';
            return;
        }
        throw new Error('Network response was not ok.');
    }
    const data = await response.json();

    const noti_container = document.getElementById('notiContainer');
    noti_container.innerHTML = '';
    noti_container.childNodes.forEach(child => child.remove());
    data.forEach(noti => {
        document.getElementById('count').innerText  = `${noti.totalNotification || 0}`;
        const noti_li = document.createElement('li');
        noti_li.classList.add('notification-item','dis');
        noti_li.innerHTML = `
        <div style="margin-right: 5px"><img class="img" src="${noti.userPhoto}"></div>
                                <div>
                                    <p style="font-style: italic; " >${noti.type}</p>
                                 
                                    <p style="color: #795548;  font-weight: bold ">${noti.message}</p>
                                    <p>${noti.time}</p>
                                </div>
        `
        noti_container.appendChild(noti_li);

        const divider_li = document.createElement('li');
        divider_li.innerHTML = '<hr class="dropdown-divider">';
        noti_container.appendChild(divider_li);
    })
}
document.getElementById('notify').addEventListener('click',fetchAllNoti)
