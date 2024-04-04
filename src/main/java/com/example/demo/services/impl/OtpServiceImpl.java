package com.example.demo.services.impl;

import com.example.demo.entity.OTP;
import com.example.demo.entity.User;
import com.example.demo.repository.OtpRepository;
import com.example.demo.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    @Override
    public OTP getByCode(String code) {
        return null;
    }

    @Override
    public OTP saveCode(String code, User user) {
        OTP otp = new OTP();
        otp.setOtpCode(code);
        otp.setUser(user);
        return otpRepository.save(otp);
    }

    @Override
    public OTP findByUserId(Long userId) {
        return otpRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public boolean isValidCode(Long userId){

        LocalDateTime now = LocalDateTime.now();
        OTP resultCode = otpRepository.findByUserId(userId).orElse(null);

        if(resultCode != null && resultCode.getExpireDate().isBefore(now)){
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        otpRepository.deleteByUserId(userId);
    }
}
