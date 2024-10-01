const uploadForm = document.getElementById("uploadForm");
const excelFileInput = document.getElementById("excelFile");

uploadForm.addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent default form submission

    const formData = new FormData();
    const files = excelFileInput.files;

    if (files.length === 0) {
        alert("Please select a file.");
        return;
    }

    formData.append("file", files[0]); // Only upload the first selected file

    fetch("/user/upload", {
        method: "POST",
        body: formData
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error("Failed to upload file.");
            }
        })
        .then(data => {
            console.log(data); // Handle successful response
            alert("File uploaded successfully.");
        })
        .catch(error => {
            console.error(error); // Handle error
            alert("Error uploading file.");
        });
});
document.addEventListener('DOMContentLoaded', function() {
    const excelFileInput = document.getElementById('excelFile');
    const fileLabel = document.getElementById('fileLabel');

    excelFileInput.addEventListener('change', function() {
        const files = excelFileInput.files;
        if (files.length > 0) {
            const fileName = files[0].name;
            fileLabel.textContent = fileName;
        } else {
            fileLabel.textContent = 'Input Excel File';
        }
    });
});
document.addEventListener('DOMContentLoaded', function() {
    const excelFileInput = document.getElementById('excelFile');
    const submitButton = document.getElementById('submitButton');

    excelFileInput.addEventListener('change', function() {
        submitButton.click(); // Simulate click event on the hidden submit button
    });
});