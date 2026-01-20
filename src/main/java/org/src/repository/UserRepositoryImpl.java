package org.src.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImpl.class);

  private static final int USERNAME = 1;
  private static final int PASSWORD = 2;
  private static final int EMAIL = 3;
  private static final int PHONE = 4;
  private static final int ROLE = 5;

  private final DataSource appDataSource;

  @Autowired
  public UserRepositoryImpl(final DataSource appDataSource) {
    this.appDataSource = appDataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void save(final User user) {
    final String saveQuery = "INSERT INTO users(username,password,email,phone,role) VALUES (?,?,?,?,?)";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try (final PreparedStatement statement = connection.prepareStatement(saveQuery)) {
      statement.setString(USERNAME, user.getUsername());
      statement.setString(PASSWORD, user.getPassword());
      statement.setString(EMAIL, user.getEmail());
      statement.setLong(PHONE, user.getPhone());
      statement.setString(ROLE, user.getRole());

      statement.executeUpdate();
      LOGGER.info("User '{}' registered successfully in Database.", user.getUsername());

    } catch (SQLException exception) {
      LOGGER.error(
          "Database error while saving user '{}': {}", user.getUsername(), exception.getMessage());

      throw new RuntimeException("User registration failed", exception);
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public Optional<User> getByUsername(final String username) {
    final String userQuery ="SELECT id, username, password, email, phone, role FROM users"
        + "WHERE username = ?";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try (final PreparedStatement statement =
             connection.prepareStatement(userQuery)) {
      statement.setString(USERNAME, username);

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
      LOGGER.warn("User search failed: Username '{}' not found in database.", username);

    } catch (final SQLException exception) {
      LOGGER.error("DataBase error while fetching user '{}': {}", username, exception.getMessage());

      throw new RuntimeException("Error for accessing user data", exception);
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }

    LOGGER.warn("Username '{}' not found.", username);

    return Optional.empty();
  }
}
