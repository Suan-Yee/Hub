package com.example.demo.application.usecase.impl;

import com.example.demo.entity.OTP;
import com.example.demo.entity.User;
import com.example.demo.exception.ApiException;
import com.example.demo.infrastructure.persistence.repository.OtpRepository;
import com.example.demo.infrastructure.persistence.repository.UserRepository;
import com.example.demo.application.usecase.ExcelUploadService;
import com.example.demo.application.usecase.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
            log.info("IsValidCode will return false");
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        otpRepository.deleteByUserId(userId);
    }


}
