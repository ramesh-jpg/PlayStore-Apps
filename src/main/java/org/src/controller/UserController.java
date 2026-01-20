package org.src.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.src.model.User;
import org.src.service.UserService;
import org.src.validation.OnCreate;

/**
 * REST Controller for managing user accounts and authentication.
 *
 * <p>Handles user registration (sign-up) and authentication (sign-in) via {@link UserService}.
 */
@RestController
@RequestMapping("/api/users")
public final class UserController {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  /**
   * Constructs the UserController with the required service dependency.
   */
  @Autowired
  public UserController(final UserService userService) {
    this.userService = userService;
  }

  /**
   * Registers a new user in the application.
   *
   * @param user the user details for registration.
   */
  @PostMapping("/signup")
  public ResponseEntity<String> signUp(@Validated(OnCreate.class)
                                         @RequestBody final User user) {
    LOGGER.info("Signup request received for Username: {}", user.getUsername());

    userService.signUp(user);

    LOGGER.info("User '{}' registered successfully.", user.getUsername());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body("Signup Successful!");
  }

  /**
   * Authenticates a user based on provided credentials.
   *
   * @param loginDetails user object containing username and password.
   */
  @PostMapping("/login")
  public ResponseEntity<User> signIn(@RequestBody final User loginDetails) {
    LOGGER.info("Login for Username: {}", loginDetails.getUsername());

    final User user = userService.signIn(
        loginDetails.getUsername(),
        loginDetails.getPassword());

    LOGGER.info("Login successful for User: {}", user.getUsername());

    return ResponseEntity.ok(user);
  }
}
