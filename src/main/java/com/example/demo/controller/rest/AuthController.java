package com.example.demo.controller.rest;

import com.example.demo.domain.ApiResponse;
import com.example.demo.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        jwtService.clearAccessTokenCookie(response);

        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Logout successful",
            Map.of("loggedOut", true)
        );

        return ResponseEntity.ok(apiResponse);
    }
}
