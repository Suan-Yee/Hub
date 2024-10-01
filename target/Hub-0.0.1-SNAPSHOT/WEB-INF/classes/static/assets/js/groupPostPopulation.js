const barChart = document.getElementById('myBarChart');
let myBarChart;// To hold the chart instance
const yearFilter = document.getElementById('year-filter');
const groupFilter = document.getElementById('group-filter');

document.addEventListener('DOMContentLoaded',async () => {
    await getAllCommunityGroup();
    await getPostPopulationInTrendingGroup();
});
const getPostPopulationInTrendingGroup =async () => {
    const response = await fetch('/get-post-population-in-trending-group');
    const group = await response.json();
    console.log(group);
    showGroupPostPopulationBarChart(group.postPopulation);
}
const years = generateYearsFrom2022() || [];
if(years!=null){
    years.forEach(year=>{
        const option = document.createElement('option');
        option.value = year;
        option.innerText = year;
        yearFilter.appendChild(option);

    })
}
yearFilter.addEventListener('change',async () => {
    let selectedYear = yearFilter.value;
    let selectedGroup = groupFilter.value;
    await getGroupPostPopulationByYear(selectedYear, selectedGroup)

});
groupFilter.addEventListener('change',async () => {
    let selectedYear = yearFilter.value;
    console.log("Selected Year", selectedYear);
    let selectedGroup = groupFilter.value;
    console.log("Selected Group", selectedGroup);
    await getGroupPostPopulationByYear(selectedYear, selectedGroup)
})


async function getGroupPostPopulationByYear(year,groupId){
    const response = await fetch("get-group-population-by-year-and-groupId",{
        method:'GET',
        headers: {
            'Content-Type': 'application/json',
            'Year': year,
            'GroupId': groupId
        }
    });
    const group =await response.json();
    console.log(group);
    showGroupPostPopulationBarChart(group.postPopulation);

}

async function getAllCommunityGroup(){
    let groups = [];
    const response = await fetch('/communityview');
    const data = await response.json();
    groups = data || [];
    if(groups!=null){
        groups.forEach(group=>{
            let option = document.createElement('option');
            option.value = group.id;
            option.innerText = group.name;
            groupFilter.appendChild(option);
        })
    }
}
function generateYearsFrom2022() {
    let currentYear = new Date().getFullYear();
    const years = [];
    for (currentYear; currentYear>=2022; currentYear--) {
        years.push(currentYear);
    }
    return years;
}


function showGroupPostPopulationBarChart(data){
    if (myBarChart) {
        myBarChart.destroy(); // Destroy the previous chart instance if it exists
    }
    myBarChart= new Chart(barChart, {
        type: 'bar',
        data: {
            labels: ['January', 'February', 'March', 'April', 'May', 'June'
                ,'July','August','September','November','December'],
            datasets: [{
                label: 'Total Number of Post',
                data: data,
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}
