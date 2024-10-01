package com.example.demo.controller;

import com.example.demo.dto.GroupDto;
import com.example.demo.entity.Group;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.entity.OTP;
import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;
import com.example.demo.form.ResetPasswordForm;
import com.example.demo.form.UserRequestGroupCheck;
import com.example.demo.repository.GroupRepository;
import com.example.demo.services.*;
import com.example.demo.utils.OTPGenerator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final UserService userService;
    private final EmailService emailService;
    private final OtpService otpService;
    private final BCryptPasswordEncoder encoder;
    private final PostService postService;
    private final GroupService groupService;
    private final GroupRepository groupRepository;


    @GetMapping("/login")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/index";
        }
        return "login";
    }
    @GetMapping("/demo")
    public String list(Model model, @RequestParam(defaultValue = "1") int page) {
        int totalPages = 30; // Simulate 30 total pages
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageUrlPostfix", "");
        return "page";
    }
    @GetMapping("/")
    public String showWelcomePage(HttpSession httpSession) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByStaffId(auth.getName());
        log.info("User name {}", auth.getName());
        log.info("User have role {}",user.getRole().name());
        httpSession.setAttribute("role",user.getRole().name());
        httpSession.setAttribute("userId",user.getId());
        if (auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals(Role.USER.name()) ||
                        a.getAuthority().equals(Role.ADMIN.name()) ||
                a.getAuthority().equals(Role.SYS_ADMIN.name()))) {
            return "redirect:/index";
        } else {
            return "login";
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
    @GetMapping("/group")
    public String group(Model model){
        List<User> users = groupService.getAll();
        model.addAttribute("users", users);
        return "group";
    }
    @GetMapping("/viewCommunity")
    public String views(){
        return "community-view";
    }
    @GetMapping("/user_profile")
    public String profile(){
        return "user_profile";
    }

    @GetMapping("/group-chat")
    public String groupChat(Model model){
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        User user=userService.findByStaffId(auth.getName());
        model.addAttribute("user",user);
        return "groupchat";
    }
    @GetMapping("/chart")
    public String charts(){
        return "chart";
    }
    

    @GetMapping("/groupPage/{id}")
    public String getGroupPage(@PathVariable("id")Long id, Model model,HttpSession session) {
        GroupDto groups=groupService.getCommunityById(id);
        model.addAttribute("groups",groups);
        session.setAttribute("groupId", id);
        return "groupPage";
    }
//    @GetMapping("/checkUserRequest/{groupId}")
//    public ResponseEntity<UserRequestGroupCheck> checkUserRequest(Principal principal, @PathVariable Long groupId) {
//        UserRequestGroupCheck userStatus = userService.checkUserRequest(principal, groupId);
//        return ResponseEntity.ok(userStatus);
//    }

    @GetMapping("/chatRoom/{id}")
    @ResponseBody
    public String goToChatRoom(@PathVariable("id")Long id,Model model){
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        User user=userService.findByStaffId(auth.getName());
        GroupDto groups=groupService.getCommunityById((long)id);
        model.addAttribute("groups",groups);
        model.addAttribute("user",user);
        return "groupchat";
    }
}

