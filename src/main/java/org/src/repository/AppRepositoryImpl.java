package org.src.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.src.model.App;
import org.src.model.Review;
import org.src.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link AppRepository} interface for database operations.
 */
@Repository
public class AppRepositoryImpl implements AppRepository {
  private static final Logger logger = LoggerFactory.getLogger(AppRepositoryImpl.class);

  private final DataSource appDataSource;

  @Autowired
  public AppRepositoryImpl(final DataSource appDataSource) {
    this.appDataSource = appDataSource;
  }

  /**
   * Saves a new App to the database along with its features and author.
   */
  @Override
  public void save(final App app) {
    final String insertApp =
        "INSERT INTO app ( name, description, version, rating, "
            + "installed_count, author_id) VALUES ( ?, ?, ?, ?, ?, ?)RETURNING id";
    final String insertFeatures = "INSERT INTO features (app_id, features) VALUES (?, ?)";

    Connection connection = null;
    try {
      connection = appDataSource.getConnection();
      connection.setAutoCommit(false);

      try (final PreparedStatement statement = connection.prepareStatement(insertApp)) {
        statement.setString(1, app.getName());
        statement.setString(2, app.getDescription());
        statement.setDouble(3, app.getVersion());
        statement.setDouble(4, 0.0);
        statement.setInt(5, 0);
        statement.setInt(6, app.getAuthor().getId());

        try (final ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            final int appId = resultSet.getInt("id");

            if (app.getFeatures() != null && !app.getFeatures().isEmpty()) {
              try (final PreparedStatement preparedStatement =
                       connection.prepareStatement(insertFeatures)) {
                for (String feature : app.getFeatures()) {
                  preparedStatement.setInt(1, appId);
                  preparedStatement.setString(2, feature.trim());
                  preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
              }
            }

            connection.commit();
            logger.info("App saved successfully with ID: {}", appId);
          }
        }
      }
    } catch (final SQLException exception) {
      if (connection != null) {
        try {
          connection.rollback();
          logger.warn("Transaction rollback due to error.");
        } catch (SQLException sqlException) {
          logger.error("Error during rollback: {}", sqlException.getMessage());
        }
      }

      logger.error("Error saving app: {}", exception.getMessage());
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (final SQLException exception) {
          logger.error("Error closing connection: {}", exception.getMessage());
        }
      }
    }
  }

  /**
   * Finds an App by its unique ID.
   */
  @Override
  public Optional<App> findById(final int id) {
    final String findApp =
        "SELECT a.*, u.username, u.role FROM app a "
            + "JOIN users u ON a.author_id = u.id "
            + "WHERE a.id = ?";

    try (final Connection connection = appDataSource.getConnection();
         final PreparedStatement statement = connection.prepareStatement(findApp)) {
      statement.setInt(1, id);

      try (final ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          final List<String> features = getAppFeatures(connection, id);

          final User author =
              new User(
                  resultSet.getInt("author_id"),
                  resultSet.getString("username"),
                  null,
                  null,
                  0,
                  resultSet.getString("role"));

          return Optional.of(
              new App(
                  resultSet.getInt("id"),
                  resultSet.getString("name"),
                  author,
                  resultSet.getString("description"),
                  resultSet.getDouble("version"),
                  features,
                  resultSet.getDouble("rating"),
                  resultSet.getInt("installed_count")));
        }
      }
    } catch (final SQLException exception) {
      logger.error("Error finding app with ID {}: {}", id, exception.getMessage());
    }

    return Optional.empty();
  }

  /**
   * Updates an existing App's basic details and features.
   */
  @Override
  public void update(final App app) {
    final String updateQuery = "UPDATE app SET name = ?, description = ?, version = ? WHERE id = ?";

    Connection connection = null;
    try {
      connection = appDataSource.getConnection();
      connection.setAutoCommit(false);

      try (final PreparedStatement statement = connection.prepareStatement(updateQuery)) {
        statement.setString(1, app.getName());
        statement.setString(2, app.getDescription());
        statement.setDouble(3, app.getVersion());
        statement.setInt(4, app.getId());

        final int updateCount = statement.executeUpdate();

        if (updateCount > 0) {
          updateFeatures(connection, app.getId(), app.getFeatures());

          connection.commit();
          logger.info("App ID {} updated successfully.", app.getId());
        } else {
          logger.warn("Update failed: App ID {} not found.", app.getId());
        }
      }
    } catch (final SQLException exception) {
      if (connection != null) {
        try {
          connection.rollback();
          logger.warn("Transaction rollback for App ID: {}", app.getId());
        } catch (final SQLException sqlException) {
          logger.error(
              "Error during rollback for App ID {}: {}", app.getId(), sqlException.getMessage());
        }
      }

      logger.error("Error updating app ID {}: {}", app.getId(), exception.getMessage());
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (final SQLException exception) {
          logger.error("Closing error connection: {}", exception.getMessage());
        }
      }
    }
  }

  /**
   * Deletes an App by its ID.
   *
   * @param id The unique identifier of the App to delete.
   * @return true if the deletion was successful, false otherwise.
   */
  @Override
  public boolean delete(final int id) {
    final String deleteQuery = "DELETE FROM app WHERE id = ?";
    try (final Connection connection = appDataSource.getConnection();
         final PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
      statement.setInt(1, id);

      final int updateCount = statement.executeUpdate();
      if (updateCount > 0) {
        logger.info("App ID {} deleted successfully.", id);
        return true;
      } else {
        logger.warn("Delete failed: App ID {} not found.", id);
        return false;
      }
    } catch (final SQLException exception) {
      logger.error("Error deleting app ID {}: {}", id, exception.getMessage());
    }

    return false;
  }

  /**
   * Retrieves all Apps from the database.
   */
  @Override
  public Collection<App> getAll() {
    final Collection<App> apps = new ArrayList<>();
    final String getAllQuery =
        "SELECT a.*, u.username, u.role FROM app a " + "JOIN users u ON a.author_id = u.id";

    try (final Connection connection = appDataSource.getConnection();
         final Statement statement = connection.createStatement();
         final ResultSet resultSet = statement.executeQuery(getAllQuery)) {
      while (resultSet.next()) {
        int appId = resultSet.getInt("id");
        final List<String> features = getAppFeatures(connection, appId);

        final User author =
            new User(
                resultSet.getInt("author_id"),
                resultSet.getString("username"),
                null,
                null,
                0,
                resultSet.getString("role"));

        apps.add(
            new App(
                appId,
                resultSet.getString("name"),
                author,
                resultSet.getString("description"),
                resultSet.getDouble("version"),
                features,
                resultSet.getDouble("rating"),
                resultSet.getInt("installed_count")));
      }

      logger.info("Retrieved {} apps from the database.", apps.size());
    } catch (final SQLException exception) {
      logger.error("Error retrieving all apps: {}", exception.getMessage());
    }

    return apps;
  }

  /**
   * Adds a user review and updates the App's average rating.
   */
  @Override
  public void addReview(final Review review) {
    final String insertReview =
        "INSERT INTO reviews (user_id, app_id, rating, comment) VALUES (?, ?, ?, ?)";

    Connection connection = null;
    try {
      connection = appDataSource.getConnection();
      connection.setAutoCommit(false);

      try (final PreparedStatement statement = connection.prepareStatement(insertReview)) {
        statement.setInt(1, review.getUserId());
        statement.setInt(2, review.getAppId());
        statement.setDouble(3, review.getRating());
        statement.setString(4, review.getComment());
        statement.executeUpdate();

        updateRating(connection, review.getAppId());

        connection.commit();
        logger.info("Review added and rating updated for App ID: {}", review.getAppId());
      }
    } catch (final SQLException exception) {
      if (connection != null) {
        try {
          connection.rollback();
          logger.warn("Transaction rolled back for Review on App ID: {}", review.getAppId());
        } catch (final SQLException sqlException) {
          logger.error("Rollback failed: {}", sqlException.getMessage());
        }
      }

      logger.error(
          "Error adding review for App ID {}: {}", review.getAppId(), exception.getMessage());
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (final SQLException exception) {
          logger.error("Closing connection: {}", exception.getMessage());
        }
      }
    }
  }

  /**
   * Fetches the list of feature strings associated with an App ID.
   */
  private List<String> getAppFeatures(final Connection connection, final int appId)
      throws SQLException {
    final List<String> features = new ArrayList<>();
    final String featuresQuery = "SELECT features FROM features WHERE app_id = ?";

    try (PreparedStatement statement = connection.prepareStatement(featuresQuery)) {
      statement.setInt(1, appId);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        features.add(resultSet.getString("features"));
      }
    }

    return features;
  }

  /**
   * Updates features for an App by deleting old ones and inserting new ones.
   */
  private void updateFeatures(
      final Connection connection, final int appId, final List<String> newFeatures)
      throws SQLException {
    final String deleteFeatures = "DELETE FROM features WHERE app_id = ?";

    try (final PreparedStatement statement = connection.prepareStatement(deleteFeatures)) {
      statement.setInt(1, appId);
      statement.executeUpdate();
    }

    if (newFeatures != null && !newFeatures.isEmpty()) {
      final String insertFeatures = "INSERT INTO features (app_id, features) VALUES (?, ?)";
      try (final PreparedStatement statement = connection.prepareStatement(insertFeatures)) {
        for (String features : newFeatures) {
          statement.setInt(1, appId);
          statement.setString(2, features.trim());
          statement.addBatch();
        }
        statement.executeBatch();
      }
    }
  }

  /**
   * Calculates the average rating from the reviews table and updates the app table.
   */
  private void updateRating(final Connection connection, final int appId) throws SQLException {
    final String updateReviewsTable = "SELECT AVG(rating) FROM reviews WHERE app_id = ?";
    final String updateAppTable = "UPDATE app SET rating = ? WHERE id = ?";
    double rating = 0.0;
    try (final PreparedStatement statement = connection.prepareStatement(updateReviewsTable)) {
      statement.setInt(1, appId);
      final ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        rating = resultSet.getDouble(1);
      }
    }

    try (final PreparedStatement statement = connection.prepareStatement(updateAppTable)) {
      statement.setDouble(1, rating);
      statement.setInt(2, appId);
      statement.executeUpdate();
    }
  }
}
