package com.example.demo.presentation.rest;

import com.example.demo.dto.GroupDto;
import com.example.demo.dto.response.ChatRoomResponse;
import com.example.demo.form.ResetPasswordForm;
import com.example.demo.entity.Group;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.entity.OTP;
import com.example.demo.entity.User;
import com.example.demo.enumeration.Role;
import com.example.demo.dto.request.ResetPasswordRequest;
import com.example.demo.dto.request.SendCodeRequest;
import com.example.demo.dto.request.VerifyOtpRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.infrastructure.persistence.repository.GroupRepository;
import com.example.demo.application.usecase.*;
import com.example.demo.utils.OTPGenerator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
    @PostMapping(value = "/sendCode", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<AuthResponse> sendCode(@RequestBody SendCodeRequest request) {
        try {
            User result = userService.findByEmail(request.email());
            String otpCode = OTPGenerator.otpCode();
            OTP checkOTP = otpService.findByUserId(result.getId());
            if (checkOTP != null) {
                otpService.deleteByUserId(result.getId());
            }
            otpService.saveCode(otpCode, result);
            return ResponseEntity.ok(AuthResponse.success(result.getId(), "/verify/" + result.getId()));
        } catch (Exception e) {
            log.warn("sendCode failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthResponse.error("Invalid email"));
        }
    }

    @Transactional
    @PostMapping(value = "/sendCode", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String sendCodeForm(@RequestParam("email") String email) {
        User result = userService.findByEmail(email);
        String otpCode = OTPGenerator.otpCode();
        OTP checkOTP = otpService.findByUserId(result.getId());
        if (checkOTP != null) otpService.deleteByUserId(result.getId());
        otpService.saveCode(otpCode, result);
        return "redirect:/verify/" + result.getId();
    }
    @GetMapping("/verify/{userId}")
    public String viewVerify(@PathVariable Long userId, Model model){
        model.addAttribute("userId",userId);
        return "/verifyCode";
    }

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        OTP otpCode = otpService.findByUserId(request.userId());
        if (otpCode == null) {
            return ResponseEntity.badRequest().body(AuthResponse.error("User not found"));
        }
        String dbCode = otpCode.getOtpCode();
        if (!request.otp().equals(dbCode)) {
            return ResponseEntity.badRequest().body(AuthResponse.error("The code is not valid. Try again!"));
        }
        if (!otpService.isValidCode(request.userId())) {
            return ResponseEntity.badRequest().body(AuthResponse.error("The code is expired. Try again!"));
        }
        return ResponseEntity.ok(AuthResponse.success(request.userId(), "/resetPassword/" + request.userId()));
    }

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String verifyOtpForm(@RequestParam("hiddenId") Long userId, @RequestParam("otp") String otp, Model model) {
        OTP otpCode = otpService.findByUserId(userId);
        String dbCode = otpCode.getOtpCode();
        if (!otp.equals(dbCode)) {
            model.addAttribute("error", "The code is not valid.Try again!");
            return "redirect:/verify/" + userId;
        }
        if (!otpService.isValidCode(userId)) {
            model.addAttribute("error", "The code is expire.Try again!");
            return "/forgetPassword";
        }
        return "redirect:/resetPassword/" + userId;
    }

    @GetMapping("/resetPassword/{userId}")
    public String resetPasswordForm(@PathVariable("userId")Long userId, Model model){
        model.addAttribute("form",new ResetPasswordForm());
        model.addAttribute("userId",userId);
        return "/changePassword";
    }
    @Transactional
    @PostMapping(value = "/resetpassword", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Password does not match"));
        }
        User user = userService.findById(request.userId());
        user.setPassword(encoder.encode(request.password()));
        userService.save(user);
        return ResponseEntity.ok(AuthResponse.success(null, "/login"));
    }

    @PostMapping(value = "/resetpassword", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String resetPasswordForm(@ModelAttribute("form") ResetPasswordForm form,
                                   @RequestParam("hiddenId") Long userId, Model model) {
        if (form.getPassword().equals(form.getConfirmPassword())) {
            User user = userService.findById(userId);
            user.setPassword(encoder.encode(form.getPassword()));
            userService.save(user);
            return "redirect:/login";
        }
        model.addAttribute("error", "Password does not match");
        return "/resetPassword";
    }
    @GetMapping("/group")
    @ResponseBody
    public ResponseEntity<List<User>> group() {
        return ResponseEntity.ok(groupService.getAll());
    }
    @GetMapping("/viewCommunity")
    @ResponseBody
    public ResponseEntity<Object> viewCommunity() {
        return ResponseEntity.ok().build();
    }
    @GetMapping("/user_profile")
    @ResponseBody
    public ResponseEntity<Object> userProfile() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/group-chat")
    @ResponseBody
    public ResponseEntity<User> groupChat() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByStaffId(auth.getName());
        return ResponseEntity.ok(user);
    }
    @GetMapping("/chart")
    @ResponseBody
    public ResponseEntity<Object> chart() {
        return ResponseEntity.ok().build();
    }
    

    @GetMapping("/groupPage/{id}")
    @ResponseBody
    public ResponseEntity<GroupDto> getGroupPage(@PathVariable("id") Long id, HttpSession session) {
        GroupDto groups = groupService.getCommunityById(id);
        session.setAttribute("groupId", id);
        return ResponseEntity.ok(groups);
    }
//    @GetMapping("/checkUserRequest/{groupId}")
//    public ResponseEntity<UserRequestGroupCheck> checkUserRequest(Principal principal, @PathVariable Long groupId) {
//        UserRequestGroupCheck userStatus = userService.checkUserRequest(principal, groupId);
//        return ResponseEntity.ok(userStatus);
//    }

    @GetMapping("/chatRoom/{id}")
    @ResponseBody
    public ResponseEntity<ChatRoomResponse> chatRoom(@PathVariable("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByStaffId(auth.getName());
        GroupDto groups = groupService.getCommunityById(id);
        return ResponseEntity.ok(new ChatRoomResponse(groups, user));
    }
}

