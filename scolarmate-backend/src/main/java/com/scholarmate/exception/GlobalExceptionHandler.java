package com.scholarmate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of(
                        "status", ex.getStatusCode().value(),
                        "error", ex.getStatusCode().toString(),
                        "message", ex.getReason() != null ? ex.getReason() : "Request error"
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";

        if ("Profile not found".equalsIgnoreCase(message)
                || "User not found".equalsIgnoreCase(message)
                || "Student profile not found".equalsIgnoreCase(message)) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "Not Found", "message", message));
        }

        if ("Profile access denied".equalsIgnoreCase(message)
                || "Access denied".equalsIgnoreCase(message)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", 403, "error", "Forbidden", "message", message));
        }

        if ("Invalid Email or Password".equalsIgnoreCase(message)
                || "Email already exists".equalsIgnoreCase(message)
                || "Invalid refresh token".equalsIgnoreCase(message)
                || "Invalid or expired refresh token".equalsIgnoreCase(message)
                || "Invalid or expired password reset token.".equalsIgnoreCase(message)
                || "Password reset token has expired. Please request a new one.".equalsIgnoreCase(message)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "error", "Bad Request", "message", message));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", 500, "error", "Internal Server Error", "message", message));
    }
}
