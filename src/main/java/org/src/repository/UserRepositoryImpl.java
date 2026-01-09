package org.src.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.src.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * SQL implementation of the {@link UserRepository} interface.
 *
 * <p>Handles user authentication and registration data access using JDBC.
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
  private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

  private final DataSource appDataSource;

  @Autowired
  public UserRepositoryImpl(final DataSource appDataSource) {
    this.appDataSource = appDataSource;
  }

  /**
   * Inserts a new user record into the database.
   *
   * @param user the {@link User} object containing details to be saved.
   */
  @Override
  public void save(final User user) {
    try (final Connection connection = appDataSource.getConnection();
         final PreparedStatement statement = connection.prepareStatement(
             "INSERT INTO users(username,password,email,phone,role) VALUES (?,?,?,?,?)")) {
      statement.setString(1, user.getUsername());
      statement.setString(2, user.getPassword());
      statement.setString(3, user.getEmail());
      statement.setLong(4, user.getPhone());
      statement.setString(5, user.getRole());
      statement.executeUpdate();

      logger.info("User '{}' registered successfully in Database.", user.getUsername());

    } catch (SQLException exception) {
      logger.error(
          "Database error while saving user '{}': {}", user.getUsername(), exception.getMessage());
    }
  }

  /**
   * Retrieves a user record based on the username.
   *
   * @param username the username to search for.
   * @return an {@link Optional} containing the user if found, otherwise empty.
   */
  @Override
  public Optional<User> getByUsername(final String username) {
    try (final Connection connection = appDataSource.getConnection();
         final PreparedStatement statement =
             connection.prepareStatement("SELECT * FROM users WHERE username=?")) {
      statement.setString(1, username);

      try (final ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          return Optional.of(
              new User(
                  result.getInt("id"),
                  result.getString("username"),
                  result.getString("password"),
                  result.getString("email"),
                  result.getLong("phone"),
                  result.getString("role")));
        }
      }

      logger.warn("User search failed: Username '{}' not found in database.", username);
    } catch (final SQLException exception) {
      logger.error("DataBase error while fetching user '{}': {}", username, exception.getMessage());
    }

    return Optional.empty();
  }
}
