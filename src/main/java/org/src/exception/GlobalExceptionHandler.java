package org.src.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler to intercept and format all application-level exceptions.
 *
 * <p>This class ensures that the client receives a consistent JSON response format of the error
 * type.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles cases where a user has an incorrect role for an operation.
   */
  @ExceptionHandler(InvalidRoleException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRole(
      final InvalidRoleException roleException) {
    logger.warn("Invalid Role Access : {}", roleException.getMessage());

    return buildResponse(HttpStatus.FORBIDDEN, roleException.getMessage());
  }

  /**
   * Handles unauthorized access attempts (e.g. wrong password).
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Map<String, Object>> handleUnauthorized(
      final UnauthorizedException unauthorizedException) {
    logger.warn("Unauthorized Access : {}", unauthorizedException.getMessage());

    return buildResponse(HttpStatus.UNAUTHORIZED, unauthorizedException.getMessage());
  }

  /**
   * Helper method to build a standardized error response.
   *
   * @param status  the HTTP status to return
   * @param message the error message to display
   * @return a ResponseEntity containing the error details
   */
  private ResponseEntity<Map<String, Object>> buildResponse(
      final HttpStatus status, final String message) {
    final Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now());
    response.put("status", status.value());
    response.put("error", status.getReasonPhrase());
    response.put("message", message);

    return ResponseEntity.status(status).body(response);
  }
}
