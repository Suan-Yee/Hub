package com.example.demo.service;

import com.example.demo.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(UserPrincipal userPrincipal);

    String extractSubject(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    void addAccessTokenCookie(HttpServletResponse response, String token);

    void clearAccessTokenCookie(HttpServletResponse response);

    String resolveTokenFromCookies(HttpServletRequest request);
}
