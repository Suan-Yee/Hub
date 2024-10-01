const ctx = document.getElementById('canvas');
const reportTitle = document.getElementById('reportTitle');
let myChart; // To hold the chart instance
document.addEventListener('DOMContentLoaded',async () => {
    await getPostsInEachGroup();
});
document.getElementById('groupsInMemberCountOption').addEventListener('click',async function () {
    reportTitle.innerText = '|Number of Members in Top 5 Group'
    const response = await fetch('/get-members-in-each-group');
    const groups = await response.json();
    showPieChart(ctx,groups.groupList,groups.memberCount,'Total Number of Member');
});
document.getElementById('groupsInPostCountOption').addEventListener('click',async function(){
    await getPostsInEachGroup();
})
async function getPostsInEachGroup(){
    reportTitle.innerText = '|Number of Posts in Top 5 Group'
    const response = await fetch('/get-posts-in-each-group');
    const groups = await response.json();
    showPieChart(ctx,groups.groupList,groups.postCount,'Total Number of Post');
}

const showPieChart = (ctx,labels, data, label) => {
    if (myChart) {
        myChart.destroy(); // Destroy the previous chart instance if it exists
    }
    myChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                label: label,
                data: data,
                backgroundColor: [
                    '#D3D3D3',
                    '#48D1CC',
                    '#FFCCBC',
                    '#FFCCBC',
                    '#EF9A9A'
                ],
                hoverOffset: 4
            }]
        }
    });
};