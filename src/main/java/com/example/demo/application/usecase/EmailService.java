package com.example.demo.application.usecase;

public interface EmailService {

    void sendOtpEmail(String name,String to,String code);
}
