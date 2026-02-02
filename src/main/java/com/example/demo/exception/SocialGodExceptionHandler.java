package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice
@Slf4j
public class SocialGodExceptionHandler {

    @ExceptionHandler(value = SocialGodException.class)
    public String exceptionHandler(SocialGodException e, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request)
                .put("message", e.getMessage());
        log.info("SocialGodException occurred: {}", e.getMessage());
        return null;
    }
}
