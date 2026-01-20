package org.src.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import org.src.validation.OnCreate;
import org.src.validation.OnUpdate;

import java.util.Objects;

/**
 * Represents a registered user of the PlayStore application.
 *
 * <p>This model contains authentication details, contact information, and the user's role (e.g.,
 * USER or AUTHOR).
 */
public final class User {

  @NotNull(message = "User id is required for update or delete", groups = OnUpdate.class)
  private int id;

  @NotBlank(message = "Username is required",
      groups = {OnCreate.class, OnUpdate.class})
  private String username;

  @NotBlank(message = "Password is required", groups = OnCreate.class)
  @Size(min = 6, message = "Password must be at least 6 characters long",
      groups = {OnCreate.class, OnUpdate.class})
  private String password;

  @NotBlank(message = "Email is required",
      groups = {OnCreate.class, OnUpdate.class})
  @Email(message = "Please provide a valid email address",
      groups = {OnCreate.class, OnUpdate.class})
  private String email;

  @Positive(message = "Phone number must be positive",
      groups = OnCreate.class)
  private long phone;

  @NotBlank(message = "Role is required",
      groups = {OnCreate.class, OnUpdate.class})
  private String role;

  /**
   * Default constructor required for JSON deserialization.
   */
  public User() {
  }

  /**
   * Constructs a new User with full details.
   *
   * @param id       The unique identifier for the user.
   * @param username The unique login username.
   * @param password The user's password.
   * @param email    The user's email address.
   * @param phone    The user's phone number.
   * @param role     The role of the user.
   */
  public User(
      final int id,
      final String username,
      final String password,
      final String email,
      final long phone,
      final String role) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.phone = phone;
    this.role = role;
  }

  /**
   * Helper constructor for login or user creation.
   *
   * @param username The login username.
   * @param password The login password.
   */
  public User(final String username, final String password) {
    this(0, username, password, null, 0, "USER");
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public long getPhone() {
    return phone;
  }

  public void setPhone(final long phone) {
    this.phone = phone;
  }

  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return String.format("User{id=%d, username='%s', role='%s'}", id, username, role);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    final User user = (User) object;

    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
