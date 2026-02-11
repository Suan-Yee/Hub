package com.example.demo.security;

import com.example.demo.domain.ApiAuthentication;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = jwtService.resolveTokenFromCookies(request);
            if (token != null) {
                try {
                    String email = jwtService.extractSubject(token);
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user != null) {
                        UserPrincipal principal = UserPrincipal.fromUser(user);
                        if (jwtService.isTokenValid(token, principal)) {
                            ApiAuthentication authentication = ApiAuthentication.authenticated(principal, principal.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (JwtException ignored) {
                    // Invalid/expired token: continue without authentication.
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
