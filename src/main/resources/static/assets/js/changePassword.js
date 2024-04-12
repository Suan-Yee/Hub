$('#changePasswordBtn').prop('disabled',true);
$(document).ready(function () {
    $('#currentPassword').on('input',function () {
        $('#currentPasswordErrorMessage').text('');
        if($('#currentPassword').val()===''){
            $('#currentPasswordErrorMessage').text('Current Password cannot be blank!!');
        }
        else{
            checkCurrentPassword();
        }
        $('#comfirmPassword').prop('disabled',true);
        $('#newPassword').prop('disabled',true);
        $('#changePasswordBtn').prop('disabled',true);
    })
});
let currentComfirmPassword;
const checkCurrentPassword=()=>{
    let currentPassword = $('#currentPassword').val();
    $.ajax({
        type:'POST',
        url:'/user/check-current-password',
        contentType:'application/json',
        data:JSON.stringify({currentPassword:currentPassword}),
        success:function (data) {
            if(data==false){
                $('#currentPassword').removeClass('is-valid').addClass('is-invalid');
            }
            else{
                $('#currentPassword').removeClass('is-invalid').addClass('is-valid').prop('disabled',true);
                currentComfirmPassword=currentPassword;
                $('#newPassword').prop('disabled',false).focus();
                $('#comfirmPassword').prop('disabled',false);
            }
        },
        error:function (xhr,error) {
            console.error('Error Checking Current Password'+error.responseText);
        }
    })
}
$('#newPassword').on('input',function () {
    let pattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{8,}$/;
    if($('#newPassword').val()===''){
        $('#newPasswordErrorMessage').text('New Password cannot be blank!!!');
        $('#comfirmPassword').prop('disabled', true);
        $('#changePasswordBtn').prop('disabled', true);
    }
    else if(!pattern.test($('#newPassword').val())){
        $('#newPasswordErrorMessage').text('New Password must include at least one uppercase letter, one lowercase letter, and one digit, and be at least 8 characters long!');
        $('#comfirmPassword').prop('disabled', true);
        $('#changePasswordBtn').prop('disabled', true);
    }
    else if($('#newPassword').val()===currentComfirmPassword){
        $('#newPasswordErrorMessage').text('New Password cannot be the same with Current Password');
        $('#comfirmPassword').prop('disabled', true);
        $('#changePasswordBtn').prop('disabled', true);
    }else {
        $('#newPasswordErrorMessage').text('');
        $('#comfirmPassword').prop('disabled', false);
        /* $('#changePasswordBtn').prop('disabled', false);*/
    }
    if ($('#comfirmPassword').val() !== '') {
        if ($('#newPassword').val() === $('#comfirmPassword').val()) {
            $('#changePasswordBtn').prop('disabled', false);
            $('#newPassword').addClass('is-valid').removeClass('is-invalid');
            $('#comfirmPassword').addClass('is-valid').removeClass('is-invalid');
        } else {
            $('#changePasswordBtn').prop('disabled', true);
            $('#newPassword').addClass('is-invalid').removeClass('is-valid');
            $('#comfirmPassword').addClass('is-invalid').removeClass('is-valid');
        }
    }
});
$('#comfirmPassword').on('input', function () {
    if ($('#comfirmPassword').val() === '') {
        $('#comfirmPasswordErrorMessage').text("Confirm Password cannot be blank!");
    } else {
        $('#comfirmPasswordErrorMessage').text('');
    }
    if ($('#newPassword').val() === $('#comfirmPassword').val() && $('#newPasswordErrorMessage').text() === '' && $('#comfirmPasswordErrorMessage').text() === '') {
        $('#changePasswordBtn').prop('disabled', false);
        $('#newPassword').addClass('is-valid').removeClass('is-invalid');
        $('#comfirmPassword').addClass('is-valid').removeClass('is-invalid');
    } else {
        $('#changePasswordBtn').prop('disabled', true);
        $('#newPassword').addClass('is-invalid').removeClass('is-valid');
        $('#comfirmPassword').addClass('is-invalid').removeClass('is-valid');
    }
});

function changePassword() {
    console.log("change password process")
    let newPassword = $('#newPassword').val();
    let comfirmPassword = $('#comfirmPassword').val();
    let currentPassword = $('#currentPassword').val();
    let changePassword = {
        currentPassword: currentPassword,
        newPassword: newPassword,
        comfirmPassword: comfirmPassword
    }
    // Serialize form data
    $.ajax({
        type: 'POST',
        url: '/user/change-password',
        contentType: 'application/json',
        data: JSON.stringify(changePassword),
        success: function (data) {
            // $('#profile-overview').tab('show');
            $('#currentPassword').val('').prop('disabled', false)
                .removeClass('is-valid').removeClass('is-invalid');
            $('#newPassword').val('').prop('disabled', true)
                .removeClass('is-valid').removeClass('is-invalid');
            $('#comfirmPassword').val('').prop('disabled', true)
                .removeClass('is-valid').removeClass('is-invalid');
            showToast("Update","#21db59");
            $('#changePasswordBtn').prop('disabled',true);
        },
        error: function (xhr, error) {
            console.error('Error changing password:', error.responseText);
            $('#errorMessage').text('Error changing password: ' + error.responseText);
        }
    });

    return false;
}