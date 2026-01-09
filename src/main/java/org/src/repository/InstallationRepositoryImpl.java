package org.src.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.src.model.App;
import org.src.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of the {@link InstallationRepository} interface.
 *
 * <p>Manages user-app relationships in the 'installation' table and updates app install counts.
 */
@Repository
public class InstallationRepositoryImpl implements InstallationRepository {
  private static final Logger logger = LoggerFactory.getLogger(InstallationRepositoryImpl.class);

  private final DataSource appDataSource;

  @Autowired
  public InstallationRepositoryImpl(final DataSource appDataSource) {
    this.appDataSource = appDataSource;
  }

  /**
   * Records a new installation and increments the app's install count.
   *
   * @param userId the ID of the user installing the app
   * @param appId  the ID of the app being installed
   * @return {@code true} if successful, {@code false} otherwise
   */
  @Override
  public boolean installed(final int userId, final int appId) {
    final String insertQuery = "INSERT INTO installation (user_id, app_id) VALUES (?, ?)";
    final String updateQuery = "UPDATE app SET installed_count = installed_count + 1 WHERE id = ?";

    try (final Connection connection = appDataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (final PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
           final PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
        // Add to installation table
        insertStatement.setInt(1, userId);
        insertStatement.setInt(2, appId);
        insertStatement.executeUpdate();
        // Increment count in app table
        updateStatement.setInt(1, appId);
        updateStatement.executeUpdate();

        connection.commit();
        logger.info("Successfully installed App ID {}", appId);

        return true;
      } catch (final SQLException exception) {
        connection.rollback();
        logger.error(
            "Installation failed for User {} App {}: {}", userId, appId, exception.getMessage());
        return false;
      }
    } catch (final SQLException exception) {
      logger.error("DataBase Connection error during installation: {}", exception.getMessage());
      return false;
    }
  }

  /**
   * Removes an installation record and decrements the app's install count.
   *
   * @param userId the ID of the user uninstalling the app
   * @param appId  the ID of the app being uninstalled
   * @return {@code true} if uninstallation was successful, {@code false} otherwise
   */
  @Override
  public boolean uninstalled(final int userId, final int appId) {
    final String deleteQuery = "DELETE FROM installation WHERE user_id = ? AND app_id = ?";
    final String updateQuery = "UPDATE app SET installed_count = installed_count - 1 WHERE id = ?";

    try (final Connection connection = appDataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (final PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
           final PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
        deleteStatement.setInt(1, userId);
        deleteStatement.setInt(2, appId);
        final int rowsDeleted = deleteStatement.executeUpdate();

        if (rowsDeleted > 0) {
          updateStatement.setInt(1, appId);
          updateStatement.executeUpdate();
          connection.commit();
          logger.info("Successfully uninstalled By App ID {}", appId);

          return true;
        } else {
          connection.rollback();
          return false;
        }

      } catch (final SQLException exception) {
        connection.rollback();
        logger.error("Uninstall error for App {}: {}", appId, exception.getMessage());
        return false;
      }
    } catch (final SQLException exception) {
      logger.error("DataBase Connection error during uninstall: {}", exception.getMessage());
      return false;
    }
  }

  /**
   * Checks if a user has installed a specific app.
   *
   * @param userId the ID of the user
   * @param appId  the ID of the app
   * @return {@code true} if a record exists, {@code false} otherwise
   */
  @Override
  public boolean isInstalled(final int userId, final int appId) {
    final String installedQuery = "SELECT id FROM installation WHERE user_id = ? AND app_id = ?";

    try (final Connection connection = appDataSource.getConnection();
         final PreparedStatement statement = connection.prepareStatement(installedQuery)) {
      statement.setInt(1, userId);
      statement.setInt(2, appId);

      try (final ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next();
      }
    } catch (final SQLException exception) {
      logger.error("Error checking installation status for : {}", exception.getMessage());
    }

    return false;
  }

  /**
   * Retrieves all apps installed by a specific user.
   *
   * <p>This method joins the 'app', 'installation', and 'users' (author) tables to construct full
   * App objects.
   *
   * @param userId the ID of the user
   * @return a collection of installed {@link App} objects
   */
  @Override
  public Collection<App> getInstalledApps(final int userId) {
    final Collection<App> installedApps = new ArrayList<>();
    String installQuery =
        "SELECT a.*, u.username, u.role FROM app a "
            + "JOIN installation i ON a.id = i.app_id "
            + "JOIN users u ON a.author_id = u.id "
            + "WHERE i.user_id = ?";

    try (final Connection connection = appDataSource.getConnection();
         final PreparedStatement statement = connection.prepareStatement(installQuery)) {
      statement.setInt(1, userId);

      try (final ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          final User author =
              new User(
                  resultSet.getInt("author_id"),
                  resultSet.getString("username"),
                  null,
                  null,
                  0,
                  resultSet.getString("role"));

          final App app =
              new App(
                  resultSet.getInt("id"),
                  resultSet.getString("name"),
                  author,
                  resultSet.getString("description"),
                  resultSet.getDouble("version"),
                  new ArrayList<>(),
                  resultSet.getDouble("rating"),
                  resultSet.getInt("installed_count"));

          installedApps.add(app);
        }
      }

      logger.info("Fetched {} installed apps for user ID {}", installedApps.size(), userId);
    } catch (final SQLException exception) {
      logger.error("Error fetching installed apps for : {}", exception.getMessage());
    }

    return installedApps;
  }
}
