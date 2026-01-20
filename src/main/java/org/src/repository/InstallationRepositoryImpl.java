package org.src.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.src.model.App;
import org.src.model.User;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of the {@link InstallationRepository} interface.
 *
 * <p>Manages user-app relationships in the 'installation' table and updates app install counts.
 */
@Repository
public class InstallationRepositoryImpl implements InstallationRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstallationRepositoryImpl.class);

  private static final int USER_ID = 1;
  private static final int APP_ID = 2;

  private final DataSource appDataSource;


  @Autowired
  public InstallationRepositoryImpl(final DataSource appDataSource) {
    this.appDataSource = appDataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean installed(final int userId, final int appId) {
    final String insertQuery = "INSERT INTO installation (user_id, app_id) VALUES (?, ?)";
    final String updateQuery = "UPDATE app SET installed_count = installed_count + 1 WHERE id = ?";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try {
      try (final PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
           final PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
        // Add to installation table
        insertStatement.setInt(USER_ID, userId);
        insertStatement.setInt(APP_ID, appId);
        insertStatement.executeUpdate();

        updateStatement.setInt(APP_ID, appId);
        updateStatement.executeUpdate();

        LOGGER.info("Successfully installed App ID {}", appId);

        return true;
      } catch (final SQLException exception) {
        LOGGER.error(
            "Installation failed for User {} App {}: {}", userId, appId, exception.getMessage());

        throw new RuntimeException("Database error during installation", exception);
      }
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean uninstalled(final int userId, final int appId) {
    final String deleteQuery = "DELETE FROM installation WHERE user_id = ? AND app_id = ?";
    final String updateQuery = "UPDATE app SET installed_count = installed_count - 1 WHERE id = ?";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try {
      try (final PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
           final PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
        deleteStatement.setInt(USER_ID, userId);
        deleteStatement.setInt(APP_ID, appId);

        final int rowsDeleted = deleteStatement.executeUpdate();

        if (rowsDeleted > 0) {
          updateStatement.setInt(APP_ID, appId);
          updateStatement.executeUpdate();

          LOGGER.info("Successfully uninstalled By App ID {}", appId);

          return true;
        }

        return false;
      } catch (final SQLException exception) {
        LOGGER.error("Uninstall error for App {}: {}", appId, exception.getMessage());

        throw new RuntimeException("Database error during uninstallation", exception);
      }
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public boolean isInstalled(final int userId, final int appId) {
    final String installedQuery = "SELECT id FROM installation WHERE user_id = ? AND app_id = ?";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try (final PreparedStatement statement = connection.prepareStatement(installedQuery)) {
      statement.setInt(USER_ID, userId);
      statement.setInt(APP_ID, appId);

      try (final ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next();
      }

    } catch (final SQLException exception) {
      LOGGER.error("Error checking installation status: {}", exception.getMessage());

      throw new RuntimeException("Installation status failed", exception);
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public Collection<App> getInstalledApps(final int userId) {
    final Collection<App> installedApps = new ArrayList<>();
    String installQuery =
        "SELECT a.id, a.name, a.description, a.version, a.rating, "
            + "a.installed_count, a.author_id, u.username, u.role "
            + "FROM app a "
            + "JOIN installation i ON a.id = i.app_id "
            + "JOIN users u ON a.author_id = u.id "
            + "WHERE i.user_id = ?";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try (final PreparedStatement statement = connection.prepareStatement(installQuery)) {
      statement.setInt(USER_ID, userId);

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
      LOGGER.info("Fetched {} installed apps for user ID {}", installedApps.size(), userId);

    } catch (final SQLException exception) {
      LOGGER.error("Error fetching installed apps for : {}", exception.getMessage());

      throw new RuntimeException("Installed fetch failed", exception);
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }

    return installedApps;
  }
}
