package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard API response wrapper for all REST endpoints
 * Uses Java 21 record for immutability and clean syntax
 * Lombok @Builder provides fluent builder pattern
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(
    int code,
    HttpStatus status,
    String path,
    String message,
    Map<?, ?> data,
    String exception,
    LocalDateTime timestamp
) {
    
    /**
     * Constructor with automatic timestamp
     */
    public ApiResponse(
        int code,
        HttpStatus status,
        String path,
        String message,
        Map<?, ?> data,
        String exception
    ) {
        this(code, status, path, message, data, exception, LocalDateTime.now());
    }
    
    /**
     * Full constructor with timestamp defaulting
     */
    public ApiResponse(
        int code,
        HttpStatus status,
        String path,
        String message,
        Map<?, ?> data,
        String exception,
        LocalDateTime timestamp
    ) {
        this.code = code;
        this.status = status;
        this.path = path;
        this.message = message;
        this.data = data;
        this.exception = exception;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
    
    /**
     * Success response with data
     */
    public static ApiResponse success(String path, String message, Map<?, ?> data) {
        return new ApiResponse(
            HttpStatus.OK.value(),
            HttpStatus.OK,
            path,
            message,
            data,
            null
        );
    }
    
    /**
     * Success response without data
     */
    public static ApiResponse success(String path, String message) {
        return success(path, message, null);
    }
    
    /**
     * Created response (201)
     */
    public static ApiResponse created(String path, String message, Map<?, ?> data) {
        return new ApiResponse(
            HttpStatus.CREATED.value(),
            HttpStatus.CREATED,
            path,
            message,
            data,
            null
        );
    }
    
    /**
     * Error response
     */
    public static ApiResponse error(String path, String message, HttpStatus status) {
        return new ApiResponse(
            status.value(),
            status,
            path,
            message,
            null,
            null
        );
    }
    
    /**
     * Error response with exception details
     */
    public static ApiResponse error(String path, String message, HttpStatus status, String exception) {
        return new ApiResponse(
            status.value(),
            status,
            path,
            message,
            null,
            exception
        );
    }
    
    /**
     * Bad request response (400)
     */
    public static ApiResponse badRequest(String path, String message) {
        return error(path, message, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Not found response (404)
     */
    public static ApiResponse notFound(String path, String message) {
        return error(path, message, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Unauthorized response (401)
     */
    public static ApiResponse unauthorized(String path, String message) {
        return error(path, message, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Forbidden response (403)
     */
    public static ApiResponse forbidden(String path, String message) {
        return error(path, message, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Internal server error response (500)
     */
    public static ApiResponse internalError(String path, String message, String exception) {
        return error(path, message, HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }
}
