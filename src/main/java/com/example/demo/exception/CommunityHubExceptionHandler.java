package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice
@Slf4j
public class CommunityHubExceptionHandler {


    @ExceptionHandler(value = CommunityHubException.class)
    public String exceptionHandler(CommunityHubException e, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request)
                .put("message", e.getMessage());
        log.info("StudentProjectException occurred: {}", e.getMessage());
        return null;
    }
}


