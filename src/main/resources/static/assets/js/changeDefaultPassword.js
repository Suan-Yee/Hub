
/*Need to cursor the password input box*/
const modal = document.querySelectorAll('modal');
const guideLineModal = document.getElementById('staticBackdrop3');
const guideLineModalBox= new bootstrap.Modal(guideLineModal);
const myModal = document.getElementById('staticBackdrop');
const modalBox = new bootstrap.Modal(myModal);

const skillModal = document.getElementById('staticBackdrop2');
const skillModalBox = new bootstrap.Modal(skillModal);

const checkBox = document.getElementById('checkBox');
const inputPassword = document.getElementById('inputPassword');
const confirmPassword = document.getElementById('confirmPassword');
const changePasswordBtn = document.getElementById('changePasswordBtn');
const passwordErrorMessage = document.getElementById('passwordErrorMessage');
const confirmPasswordErrorMessage= document.getElementById('confirmPasswordErrorMessage');

modal.forEach(modal=>{
    modal.style.background = '#757575';
})
const agreeBtn = document.getElementById('agreeBtn');
agreeBtn.disabled = true;
changePasswordBtn.disabled = true;
const url = '/user/check-default-password';
fetch(url,{
    method:'GET',
    headers:{'Content-type':'application/json'},
})
    .then(response=>response.json())
    .then(data=>{
        if(data===true){
            console.log("Need to change Password");
            guideLineModalBox.show();
        }
    });
checkBox.onchange=(function () {
    if(checkBox.checked){
        agreeBtn.disabled = false;
    }
    else{
        agreeBtn.disabled= true;
    }
})
const agree=()=>{
    if(checkBox.checked){
        guideLineModalBox.hide();
        modalBox.show();
    }
}
let password;
inputPassword.oninput=(function () {
    password = this.value;
    const pattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{8,}$/;
    if(password === ''){
        console.log("Password :"+password);
        passwordErrorMessage.innerHTML = 'Password cannot be blank!';
        inputPassword.classList.remove('is-valid');
        inputPassword.classList.add('is-invalid');
    }
    else if(!password.match(pattern)){
        passwordErrorMessage.innerHTML='Password must include at least one uppercase letter, one lowercase letter, and one digit, and be at least 8 characters long!';
        inputPassword.classList.remove('is-valid');
        inputPassword.classList.add('is-invalid');
    }
    else{
        passwordErrorMessage.innerHTML = '';
        inputPassword.classList.remove('is-invalid');
        inputPassword.classList.add('is-valid');
        changePasswordBtn.disabled=false;
    }
})
confirmPassword.oninput=(function () {
    let confirmPasswordValue = confirmPassword.value;
    if(confirmPasswordValue===''){
        confirmPasswordErrorMessage.innerText = 'Confrim Password cannot be blank!!';
        confirmPassword.classList.remove('is-valid');
        confirmPassword.classList.add('is-invalid');
        changePasswordBtn.disabled =true;
    }
    else if(confirmPasswordValue !== password){
        confirmPassword.classList.remove('is-valid');
        confirmPassword.classList.add('is-invalid');
        changePasswordBtn.disabled =true;
    }
    else{
        confirmPasswordErrorMessage.innerHTML = '';
        confirmPassword.classList.remove('is-invalid');
        confirmPassword.classList.add('is-valid');
        changePasswordBtn.disabled =false;
    }
});

const changeDefaultPassword=()=>{
    let password = document.getElementById('inputPassword').value;
    const url = 'user/change-default-password';
    let changeDefaultPassword={
        newPassword:password
    }
    fetch(url,{
        method:'POST',
        headers:{'Content-type':'application/json'},
        body:JSON.stringify(changeDefaultPassword)
    })
        .then(response=>response.json())
        .then(data=>{
            console.log(data);
            modalBox.hide();
            skillModalBox.show();
        })
        .catch(error=>{
            console.error("Error Occurred :"+error);
        })
}
