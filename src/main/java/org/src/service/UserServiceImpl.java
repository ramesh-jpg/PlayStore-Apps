package org.src.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.src.exception.UnauthorizedException;
import org.src.model.User;
import org.src.repository.UserRepository;

/**
 * Manages user authentication including sign-up and sign-in processes.
 *
 * <p>Handles user registration validations and credential verification.
 */
@Service
public class UserServiceImpl implements UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;

  /**
   * Constructs the UserServiceImpl with the required repository dependency.
   */
  @Autowired
  public UserServiceImpl(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void signUp(final User user) {
    LOGGER.info("Signup process for user: {}", user.getUsername());

    if (!user.getUsername().matches("[A-Za-z][A-Za-z0-9@#$%^&+=._-]*")) {
      LOGGER.warn("Signup failed: Invalid username format '{}'", user.getUsername());

      throw new IllegalArgumentException("Invalid Username.");
    }

    if (userRepository.getByUsername(user.getUsername()).isPresent()) {
      LOGGER.warn("Signup failed: Username '{}' already exists", user.getUsername());

      throw new IllegalStateException("Username Already Exist.");
    }

    if (user.getPassword().equals(user.getUsername())) {
      LOGGER.warn("Signup failed: Password same as username for '{}'", user.getUsername());

      throw new IllegalArgumentException("Password Can't be Same as Username.");
    }

    if (user.getPassword().length() < 6) {
      LOGGER.warn("Signup failed: Password short for '{}'", user.getUsername());

      throw new IllegalArgumentException("Password must be at least 6 characters.");
    }

    try {
      userRepository.save(user);

      LOGGER.info("Signup successfully for User '{}'", user.getUsername());
    } catch (final RuntimeException exception) {
      handleDatabaseError(exception, user);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User signIn(final String username, final String password) {
    LOGGER.info("Login for username: {}", username);

    final User user = userRepository.getByUsername(username)
        .orElseThrow(() -> {
          LOGGER.warn("Login failed: Username '{}' not found.", username);

          return new RuntimeException("Username Not Found.");
        });

    if (user.getPassword().equals(password)) {
      return user;
    } else {
      LOGGER.error("Login failed for incorrect Password");

      throw new UnauthorizedException("Wrong PassWord. ");
    }
  }

  /**
   * Handles database-specific exceptions such as unique constraint violations.
   */
  private void handleDatabaseError(final RuntimeException exception,
                                   final User user) {
    final String errorMessage = exception.getMessage();

    if (errorMessage.contains("users_phone_key")) {
      LOGGER.warn("Signup DataBase Error: Phone number '{}' already registered.", user.getPhone());
    } else if (errorMessage.contains("users_email_key")) {
      LOGGER.warn("Signup DataBase Error: Email '{}' already registered.", user.getEmail());
    } else {
      LOGGER.error("Signup failed for DataBase error: {}", errorMessage);
    }
  }
}
