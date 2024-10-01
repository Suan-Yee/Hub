package com.example.demo.restController;

import com.example.demo.entity.OTP;
import com.example.demo.form.UserIdAndOTPViewObject;
import com.example.demo.services.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OTPRestController{

    private final OtpService otpService;
    @PostMapping("/verify-OTPCode")
    public ResponseEntity<Boolean> verifyOTPCode(@RequestBody UserIdAndOTPViewObject UserIdAndOTPViewObject){
        OTP otpCode = otpService.findByUserId(UserIdAndOTPViewObject.getUserId());
        String dbCode = otpCode.getOtpCode();

        if(UserIdAndOTPViewObject.getOtpCode().equals(dbCode)){
            if(otpService.isValidCode(UserIdAndOTPViewObject.getUserId())){
                return ResponseEntity.ok(true);
            }
        }
        return ResponseEntity.ok(false);
    }
}
