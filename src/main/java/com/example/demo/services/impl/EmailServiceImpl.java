package com.example.demo.services.impl;

import com.example.demo.exception.ApiException;
import com.example.demo.services.EmailService;
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

   /* @Value("${spring.mail.verify.host}")
    private String host;*/
   /* @Value("${spring.mail.username}")
    private String fromEmail;*/

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendOtpEmail(String name, String email, String code) {

        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Reset Password!!");
            message.setFrom("kosuanyeeaung44250@gmail.com");
            message.setTo(email);
            message.setText(EmailUtils.getOtpSendingMessage(name,code));
            mailSender.send(message);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("Unable to send Email");
        }
    }
}
