package com.example.demo.exception;

import com.example.demo.domain.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API endpoints
 * Returns standardized ApiResponse for all exceptions
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed for request {}: {}", request.getRequestURI(), errors);
        
        ApiResponse response = new ApiResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST,
            request.getRequestURI(),
            "Validation failed",
            Map.of("errors", errors),
            null
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle API exceptions (custom application exceptions)
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        log.error("API Exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ApiResponse response = ApiResponse.error(
            request.getRequestURI(),
            ex.getMessage(),
            HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle access denied exceptions
     */
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ApiResponse> handleAccessDeniedException(
//            AccessDeniedException ex,
//            HttpServletRequest request
//    ) {
//        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
//
//        ApiResponse response = ApiResponse.forbidden(
//            request.getRequestURI(),
//            "You don't have permission to access this resource"
//        );
//
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//    }

    /**
     * Handle authentication exceptions
     */
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ApiResponse> handleBadCredentialsException(
//            BadCredentialsException ex,
//            HttpServletRequest request
//    ) {
//        log.warn("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage());
//
//        ApiResponse response = ApiResponse.unauthorized(
//            request.getRequestURI(),
//            "Invalid credentials"
//        );
//
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//    }

    /**
     * Handle file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request
    ) {
        log.warn("File upload size exceeded at {}", request.getRequestURI());
        
        ApiResponse response = ApiResponse.badRequest(
            request.getRequestURI(),
            "File size exceeds maximum allowed limit"
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = String.format(
            "Parameter '%s' should be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        
        log.warn("Type mismatch at {}: {}", request.getRequestURI(), message);
        
        ApiResponse response = ApiResponse.badRequest(
            request.getRequestURI(),
            message
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle 404 - Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found: {}", request.getRequestURI());
        
        ApiResponse response = ApiResponse.notFound(
            request.getRequestURI(),
            "The requested resource was not found"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ApiResponse response = ApiResponse.badRequest(
            request.getRequestURI(),
            ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ApiResponse response = ApiResponse.internalError(
            request.getRequestURI(),
            "An unexpected error occurred. Please try again later.",
            ex.getClass().getSimpleName() + ": " + ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
