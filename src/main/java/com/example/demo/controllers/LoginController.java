package com.example.demo.controllers;

import com.example.demo.entity.OTP;
import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;
import com.example.demo.form.ResetPasswordForm;
import com.example.demo.services.EmailService;
import com.example.demo.services.OtpService;
import com.example.demo.services.UserService;
import com.example.demo.utils.OTPGenerator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final UserService userService;
    private final EmailService emailService;
    private final OtpService otpService;
    private final BCryptPasswordEncoder encoder;

    @GetMapping("/")
    public String showWelcomePage(HttpSession httpSession) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByStaffId(auth.getName());
        log.info("User name {}", auth.getName());
        if (auth != null && auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals(Role.USER.name()) ||
                        a.getAuthority().equals(Role.ADMIN.name()))) {
            log.info("User role {}", user.getRole().name());
            return "redirect:/index";
        } else {
            return "/login";
        }
    }
    @Transactional
    @PostMapping("/sendCode")
    public String change(@RequestParam("email")String email){
        User result = userService.findByEmail(email);
        String otpCode = OTPGenerator.otpCode();
        OTP checkOTP = otpService.findByUserId(result.getId());
        log.info("Here i am in sendCode {}",checkOTP);
        if(checkOTP != null){
            otpService.deleteByUserId(result.getId());
        }
        log.info("Here i am in sendCode 2");
        otpService.saveCode(otpCode,result);
        /*emailService.sendOtpEmail(result.getName(),email,otpCode);*/
        return "redirect:/verify/" + result.getId();
    }
    @GetMapping("/verify/{userId}")
    public String viewVerify(@PathVariable Long userId, Model model){
        model.addAttribute("userId",userId);
        return "/verifyCode";
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam("hiddenId") Long userId, @RequestParam("otp") String otp,Model model) {

        OTP otpCode = otpService.findByUserId(userId);
        String dbCode = otpCode.getOtpCode();

        if(otp.equals(dbCode)){
            if(!otpService.isValidCode(userId)){
                model.addAttribute("error","The code is expire.Try again!");
                return "/forgetPassword";
            }
        }else{
            model.addAttribute("error","The code is not valid.Try again!");
            return "redirect:/verify/" + userId;
        }
        return "redirect:/resetPassword/" + userId;
    }

    @GetMapping("/resetPassword/{userId}")
    public String resetPasswordForm(@PathVariable("userId")Long userId, Model model){
        model.addAttribute("form",new ResetPasswordForm());
        model.addAttribute("userId",userId);
        return "/changePassword";
    }
    @PostMapping("/resetpassword")
    public String resetPassword(@ModelAttribute("form")ResetPasswordForm form,
                                @RequestParam("hiddenId")Long userId,Model model){
        if(form.getPassword().equals(form.getConfirmPassword())){
            log.info("User id {}",userId);
            User user = userService.findById(userId);
            user.setPassword(encoder.encode(form.getPassword()));
            userService.save(user);
            log.info("User password {}",user.getPassword());
            return "redirect:/welcome";
        }else{
            model.addAttribute("error","Password does not match");
            return "/resetPassword";
        }
    }
    @GetMapping("/index")
    public String adminView(){
        return "index.html";
    }
    @GetMapping("/list")
    public String userlist(){
        return "user-listing.html";
    }
    @GetMapping("/profile")
    public String userProfile(){
        return "admin_profile.html";
    }
    @GetMapping("/home")
    public String homePage(){
        return "home.html";
    }
//    @GetMapping("/loginForm")
//    public String loginPage(){
//        return "login.html";
//    }
    @GetMapping("/otp")
    public String otpForm(){
        return "otpForm.html";
    }
    @GetMapping("/status")
    public String updateStatus(){
        return "update_status.html";
    }
    @GetMapping("/data")
    public String dataTable(){
        return "table_data.html";
    }
    @GetMapping("/chart")
    public String chart(){
        return "chart.html";
    }
}
