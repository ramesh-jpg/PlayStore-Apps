package org.src.exception;

/**
 * Exception thrown when a user attempts to perform an action without proper authorization.
 *
 * <p>This typically results in an HTTP for 401 Unauthorized response. It is used for
 * security-related failures like role mismatch or incorrect credentials.
 */
public class UnauthorizedException extends RuntimeException {

  /**
   * Constructs a new UnauthorizedException with the specified detail message.
   *
   * @param message the detail message explaining the cause of the authorization failure
   */
  public UnauthorizedException(final String message) {
    super(message);
  }
}
