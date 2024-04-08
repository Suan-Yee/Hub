package com.example.demo.services;

import com.example.demo.entity.OTP;
import com.example.demo.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface OtpService {

    OTP getByCode(String code);
    OTP saveCode(String code, User user);
    OTP findByUserId(Long userId);
    boolean isValidCode(Long userId);
    void deleteByUserId(Long userId);

}
