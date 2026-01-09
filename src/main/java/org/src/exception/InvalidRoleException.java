package org.src.exception;

/**
 * Exception thrown when a user's role does not match the required permissions for an operation.
 *
 * <p>For example, if a "USER" role attempts to perform an action restricted to "AUTHOR", this
 * exception will be triggered. It typically results in an HTTP 403 Forbidden status.
 */
public class InvalidRoleException extends RuntimeException {

  /**
   * Constructs a new InvalidRoleException with a specific error message.
   *
   * @param message the detail message explaining why the role is invalid
   */
  public InvalidRoleException(final String message) {
    super(message);
  }
}
