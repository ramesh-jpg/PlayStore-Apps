package org.src.service;

import org.src.model.User;

/**
 * Defines the contract for user authentication and account management.
 *
 * <p>Outlines essential operations for users, such as registration (sign-up) and authentication
 * (sign-in).
 */
public interface UserService {

  /**
   * Registers a new user in the system.
   *
   * @param user the {@link User} object containing registration details.
   */
  void signUp(final User user);

  /**
   * Authenticates a user based on their credentials.
   *
   * @param username the username provided during login.
   * @param password the password provided during login.
   * @return the authenticated {@link User} object if credentials are valid.
   */
  User signIn(final String username, final String password);
}
