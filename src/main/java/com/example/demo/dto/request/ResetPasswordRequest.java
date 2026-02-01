package com.example.demo.dto.request;

public record ResetPasswordRequest(String password, String confirmPassword, Long userId) {}
