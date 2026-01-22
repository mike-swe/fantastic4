package com.revature.fantastic4.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        String message = ex.getMessage();
        
        // Check for authorization failures (403 Forbidden)
        if (message != null && (
            message.contains("Only Admin") ||
            message.contains("can only view your own") ||
            message.contains("Only admin")
        )) {
            errorResponse.put("error", message);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        // Check for not found errors (404 Not Found)
        if (message != null && (
            message.contains("not found") ||
            message.contains("does not exist")
        )) {
            errorResponse.put("error", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Check for authentication failures (401 Unauthorized)
        if (message != null && (
            message.contains("Invalid username or password") ||
            message.contains("Unauthorized") ||
            message.contains("Authorization header")
        )) {
            errorResponse.put("error", message);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Default to 400 Bad Request for other IllegalArgumentException
        errorResponse.put("error", message != null ? message : "Invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
