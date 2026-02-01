package com.example.demo.presentation.rest;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = status != null ? Integer.parseInt(status.toString()) : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = statusCode == HttpStatus.NOT_FOUND.value() ? "Resource not found" : "An error occurred";
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        return ResponseEntity.status(statusCode)
                .body(Map.of("status", statusCode, "error", httpStatus != null ? httpStatus.getReasonPhrase() : "Error", "message", message));
    }
}
