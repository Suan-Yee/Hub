package com.example.demo.dto.response;

public record AuthResponse(boolean success, Long userId, String error, String redirectUrl) {
    public static AuthResponse success(Long userId, String redirectUrl) {
        return new AuthResponse(true, userId, null, redirectUrl);
    }
    public static AuthResponse error(String error) {
        return new AuthResponse(false, null, error, null);
    }
}
