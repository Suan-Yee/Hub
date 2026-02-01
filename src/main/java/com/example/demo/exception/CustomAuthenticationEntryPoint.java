package com.example.demo.exception;

import com.example.demo.entity.User;
import com.example.demo.application.usecase.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationSuccessHandler {

    private final  UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        User user = userService.findByStaffId(authentication.getName());

        if(Objects.equals(user.getPassword(), "dat123")){
            response.sendRedirect("/index");
        }

    }
}
