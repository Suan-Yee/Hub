package com.example.demo.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String requestedUrl = request.getRequestURI();
            if (requestedUrl.equals("/login")) {
                log.info("User {} already authenticated, redirecting to /index", authentication.getName());
                response.sendRedirect(request.getContextPath() + "/index");
                return;
            }
            log.info("User {} attempted to access unauthorized resource: {}", authentication.getName(), requestedUrl);
        }
        response.sendRedirect(request.getContextPath() + "/access-denied");
    }
}
