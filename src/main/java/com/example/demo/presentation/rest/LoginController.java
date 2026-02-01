package com.example.demo.presentation.rest;

import com.example.demo.dto.GroupDto;
import com.example.demo.dto.response.ChatRoomResponse;
import com.example.demo.entity.OTP;
import com.example.demo.entity.User;
import com.example.demo.dto.request.ResetPasswordRequest;
import com.example.demo.dto.request.SendCodeRequest;
import com.example.demo.dto.request.VerifyOtpRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.application.usecase.*;
import com.example.demo.utils.OTPGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final UserService userService;
    private final OtpService otpService;
    private final BCryptPasswordEncoder encoder;
    private final GroupService groupService;


    @Transactional
    @PostMapping(value = "/sendCode", consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @Transactional
    @PostMapping(value = "/resetpassword", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Password does not match"));
        }
        User user = userService.findById(request.userId());
        user.setPassword(encoder.encode(request.password()));
        userService.save(user);
        return ResponseEntity.ok(AuthResponse.success(null, "/login"));
    }

    @GetMapping("/group")
    public ResponseEntity<List<User>> group() {
        return ResponseEntity.ok(groupService.getAll());
    }
    @GetMapping("/viewCommunity")
    public ResponseEntity<Object> viewCommunity() {
        return ResponseEntity.ok().build();
    }
    @GetMapping("/user_profile")
    public ResponseEntity<Object> userProfile() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/group-chat")
    public ResponseEntity<User> groupChat() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByStaffId(auth.getName());
        return ResponseEntity.ok(user);
    }
    @GetMapping("/chart")
    public ResponseEntity<Object> chart() {
        return ResponseEntity.ok().build();
    }
    

    @GetMapping("/groupPage/{id}")
    @ResponseBody
    public ResponseEntity<GroupDto> getGroupPage(@PathVariable("id") Long id) {
        GroupDto groups = groupService.getCommunityById(id);
        return ResponseEntity.ok(groups);
    }
//    @GetMapping("/checkUserRequest/{groupId}")
//    public ResponseEntity<UserRequestGroupCheck> checkUserRequest(Principal principal, @PathVariable Long groupId) {
//        UserRequestGroupCheck userStatus = userService.checkUserRequest(principal, groupId);
//        return ResponseEntity.ok(userStatus);
//    }

    @GetMapping("/chatRoom/{id}")
    public ResponseEntity<ChatRoomResponse> chatRoom(@PathVariable("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByStaffId(auth.getName());
        GroupDto groups = groupService.getCommunityById(id);
        return ResponseEntity.ok(new ChatRoomResponse(groups, user));
    }
}

