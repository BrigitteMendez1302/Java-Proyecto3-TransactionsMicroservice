package com.example.transactionmicroservice.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler to manage all exceptions in the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResponseStatusException and returns a custom error response.
     *
     * @param ex The exception thrown.
     * @return A ResponseEntity containing the error details.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", ex.getStatusCode().value());
        errorDetails.put("error", HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase());
        errorDetails.put("message", ex.getReason());
        errorDetails.put("path", "");

        return new ResponseEntity<>(errorDetails, ex.getStatusCode());
    }

    /**
     * Handles generic exceptions that are not explicitly defined.
     *
     * @param ex The exception thrown.
     * @return A ResponseEntity containing generic error details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("path", ""); // Optional: Add dynamic path if required

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
