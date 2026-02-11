package com.example.demo.utils;

import com.example.demo.domain.ApiResponse;
import com.example.demo.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public final class RequestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RequestUtils() {
    }

    private static final BiConsumer<HttpServletResponse, ApiResponse> writeResponse = (httpServletResponse, response) -> {
        try {
            var outputStream = httpServletResponse.getOutputStream();
            OBJECT_MAPPER.writeValue(outputStream, response);
            outputStream.flush();
        } catch (Exception exception) {
            throw new ApiException(exception.getMessage());
        }
    };

    private static final BiFunction<Exception, HttpStatus, String> errorResponse = (exception, httpStatus) -> {
        if (httpStatus.isSameCodeAs(FORBIDDEN)) {
            return "You do not have enough permission";
        }
        if (httpStatus.isSameCodeAs(UNAUTHORIZED)) {
            return "You are not logged in";
        }
        if (exception instanceof DisabledException
            || exception instanceof LockedException
            || exception instanceof BadCredentialsException
            || exception instanceof CredentialsExpiredException
            || exception instanceof ApiException) {
            return exception.getMessage();
        }
        if (httpStatus.is5xxServerError()) {
            return "An internal server error occurred.";
        }
        return "An error occurred. Please try again.";
    };

    public static ApiResponse getResponse(HttpServletRequest request, Map<?, ?> data, String message, HttpStatus status) {
        return new ApiResponse(
            status.value(),
            status,
            request.getRequestURI(),
            message,
            data,
            null
        );
    }

    public static void handleErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if (exception instanceof AccessDeniedException) {
            ApiResponse apiResponse = getErrorResponse(request, response, exception, FORBIDDEN);
            writeResponse.accept(response, apiResponse);
            return;
        }
        if (exception instanceof AuthenticationException) {
            ApiResponse apiResponse = getErrorResponse(request, response, exception, UNAUTHORIZED);
            writeResponse.accept(response, apiResponse);
            return;
        }
        ApiResponse apiResponse = getErrorResponse(request, response, exception, HttpStatus.INTERNAL_SERVER_ERROR);
        writeResponse.accept(response, apiResponse);
    }

    private static ApiResponse getErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception, HttpStatus status) {
        response.setContentType("application/json");
        response.setStatus(status.value());
        return new ApiResponse(
            status.value(),
            status,
            request.getRequestURI(),
            errorResponse.apply(exception, status),
            emptyMap(),
            getRootCauseMessage(exception)
        );
    }

    private static String getRootCauseMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}
