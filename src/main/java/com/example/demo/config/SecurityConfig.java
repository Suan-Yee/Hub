package com.example.demo.config;

import com.example.demo.handler.ApiAccessDeniedHandler;
import com.example.demo.handler.ApiAuthenticationEntryPoint;
import com.example.demo.security.ApiAuthenticationFilter;
import com.example.demo.security.ApiAuthenticationProvider;
import com.example.demo.security.JwtCookieAuthenticationFilter;
import com.example.demo.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiAuthenticationProvider apiAuthenticationProvider;
    private final JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(apiAuthenticationProvider);
    }

    @Bean
    public ApiAuthenticationFilter apiAuthenticationFilter(AuthenticationManager authenticationManager) {
        ApiAuthenticationFilter filter = new ApiAuthenticationFilter(jwtService, objectMapper);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiAuthenticationFilter apiAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/logout", "/error/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(apiAuthenticationEntryPoint)
                .accessDeniedHandler(apiAccessDeniedHandler)
            )
            .authenticationProvider(apiAuthenticationProvider)
            .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(apiAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
