package com.example.demo.security;

import com.example.demo.domain.ApiAuthentication;
import com.example.demo.domain.ApiResponse;
import com.example.demo.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

public class ApiAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public ApiAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        super(new AntPathRequestMatcher("/api/auth/login", "POST"));
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        LoginPayload payload = objectMapper.readValue(request.getInputStream(), LoginPayload.class);
        String identity = normalizeIdentity(payload);
        if (identity == null || payload.password == null || payload.password.isBlank()) {
            throw new AuthenticationServiceException("Email and password are required");
        }

        ApiAuthentication authRequest = ApiAuthentication.unauthenticated(identity, payload.password);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        UserPrincipal principal = (UserPrincipal) authResult.getPrincipal();
        String token = jwtService.generateToken(principal);
        jwtService.addAccessTokenCookie(response, token);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Login successful",
            Map.of(
                "userId", principal.getId(),
                "email", principal.getUsername(),
                "authorities", principal.getAuthorities().stream().map(Object::toString).toList()
            )
        );
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse apiResponse = ApiResponse.unauthorized(request.getRequestURI(), failed.getMessage());
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

    private String normalizeIdentity(LoginPayload payload) {
        if (payload.email != null && !payload.email.isBlank()) {
            return payload.email.trim();
        }
        if (payload.staffId != null && !payload.staffId.isBlank()) {
            return payload.staffId.trim();
        }
        if (payload.username != null && !payload.username.isBlank()) {
            return payload.username.trim();
        }
        return null;
    }

    private static final class LoginPayload {
        public String email;
        public String staffId;
        public String username;
        public String password;
    }
}
