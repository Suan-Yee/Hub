package com.example.demo.service.impl;

import com.example.demo.exception.ApiException;
import com.example.demo.service.EmailService;
import com.example.demo.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailUtils emailUtils;

    @Override
    @Async
    public void sendOtpEmail(String name, String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // message.setFrom(emailUtils.getFromEmail());
            message.setTo(to);
            message.setSubject("Password Reset OTP");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your OTP code for password reset is: %s\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Social Hub Team",
                name, code
            ));
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new ApiException("Failed to send email: " + e.getMessage());
        }
    }
}
