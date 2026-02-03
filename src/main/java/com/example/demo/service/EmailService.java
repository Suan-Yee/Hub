package com.example.demo.service;

public interface EmailService {

    void sendOtpEmail(String name, String to, String code);
}
