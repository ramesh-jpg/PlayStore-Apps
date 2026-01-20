package org.src.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.src.model.App;
import org.src.model.Review;
import org.src.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link AppRepository} interface for database operations.
 */
@Repository
public class AppRepositoryImpl implements AppRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppRepositoryImpl.class);

  private static final int APP_NAME = 1;
  private static final int APP_DESCRIPTION = 2;
  private static final int APP_VERSION = 3;
  private static final int APP_RATING = 4;
  private static final int APP_INSTALLED = 5;
  private static final int APP_AUTHOR = 6;
  private static final int APP_ID = 1;

  private static final int FEATURES_APP_ID = 1;
  private static final int FEATURES_NAME = 2;

  private static final int REVIEW_USER = 1;
  private static final int REVIEW_APP = 2;
  private static final int REVIEW_RATING = 3;
  private static final int REVIEW_COMMENT = 4;

  private final DataSource appDataSource;

  @Autowired
  public AppRepositoryImpl(final DataSource appDataSource) {
    this.appDataSource = appDataSource;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void save(final App app) {
    final String insertApp = """
        INSERT INTO app (
            name, description, version, rating, installed_count, author_id
        ) VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id
        """;
    final String insertFeatures = "INSERT INTO features (app_id, features) VALUES (?, ?)";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try {

      try (final PreparedStatement statement = connection.prepareStatement(insertApp)) {
        statement.setString(APP_NAME, app.getName());
        statement.setString(APP_DESCRIPTION, app.getDescription());
        statement.setDouble(APP_VERSION, app.getVersion());
        statement.setDouble(APP_RATING, 0.0);
        statement.setInt(APP_INSTALLED, 0);
        statement.setInt(APP_AUTHOR, app.getAuthor().getId());

        try (final ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            final int appId = resultSet.getInt("id");

            if (app.getFeatures() != null && !app.getFeatures().isEmpty()) {
              try (final PreparedStatement preparedStatement =
                       connection.prepareStatement(insertFeatures)) {
                for (String feature : app.getFeatures()) {
                  preparedStatement.setInt(FEATURES_APP_ID, appId);
                  preparedStatement.setString(FEATURES_NAME, feature.trim());
                  preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
              }

              LOGGER.info("App saved successfully with ID: {}", appId);
            }
          }
        }
      }
    } catch (final SQLException exception) {
      LOGGER.error("Database error while saving app : {}", exception.getMessage());

      throw new RuntimeException("Save operation failed", exception);
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public Optional<App> findById(final int id) {
    final String findApp =
        "SELECT a.id, a.name, a.description, a.version, a.rating,"
            + "a.installed_count, a.author_id, u.username, u.role "
            + "FROM app a "
            + "JOIN users u ON a.author_id = u.id "
            + "WHERE a.id = ?";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try {
      try (final PreparedStatement statement = connection.prepareStatement(findApp)) {
        statement.setInt(APP_ID, id);

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
        LOGGER.error("Error finding app with ID {}: {}", id, exception.getMessage());

        throw new RuntimeException("FindById is failed", exception);
      }
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }

    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(final App app) {
    final String updateQuery = "UPDATE app SET name = ?, description = ?, version = ? WHERE id = ?";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try {
      try (final PreparedStatement statement = connection.prepareStatement(updateQuery)) {
        statement.setString(APP_NAME, app.getName());
        statement.setString(APP_DESCRIPTION, app.getDescription());
        statement.setDouble(APP_VERSION, app.getVersion());
        statement.setInt(APP_ID, app.getId());

        final int updateCount = statement.executeUpdate();

        if (updateCount > 0) {
          updateFeatures(connection, app.getId(), app.getFeatures());

          LOGGER.info("App ID {} updated successfully.", app.getId());
        } else {
          LOGGER.warn("Update failed: App ID {} not found.", app.getId());
        }
      }
    } catch (final SQLException exception) {
      LOGGER.error("Error updating app ID {}: {}", app.getId(), exception.getMessage());

      throw new RuntimeException("Update failed", exception);
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean delete(final int id) {
    final String deleteQuery = "DELETE FROM app WHERE id = ?";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try {
      try (final PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
        statement.setInt(APP_ID, id);

        final int updateCount = statement.executeUpdate();
        if (updateCount > 0) {
          LOGGER.info("App ID {} deleted successfully.", id);

          return true;
        } else {
          LOGGER.warn("Delete failed: App ID {} not found.", id);

          return false;
        }

      }
    } catch (final SQLException exception) {
      LOGGER.error("Error deleting app ID {}: {}", id, exception.getMessage());
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public Collection<App> getAll() {
    final Collection<App> apps = new ArrayList<>();

    final String getAllQuery = "SELECT a.id, a.name, a.description, a.version, a.rating,"
        + "a.installed_count, a.author_id, u.username, u.role "
        + "FROM app a "
        + "JOIN users u ON a.author_id = u.id";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try {

      try (final Statement statement = connection.createStatement();
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
      }
      LOGGER.info("Retrieved {} apps from the database.", apps.size());

    } catch (final SQLException exception) {
      LOGGER.error("Error retrieving all apps: {}", exception.getMessage());

      throw new RuntimeException("Fetch all failed", exception);
    }

    return apps;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void addReview(final Review review) {
    final String insertReview =
        "INSERT INTO reviews (user_id, app_id, rating, comment) VALUES (?, ?, ?, ?)";

    final Connection connection = DataSourceUtils.getConnection(appDataSource);
    try {
      try (final PreparedStatement statement = connection.prepareStatement(insertReview)) {
        statement.setInt(REVIEW_USER, review.getUserId());
        statement.setInt(REVIEW_APP, review.getAppId());
        statement.setDouble(REVIEW_RATING, review.getRating());
        statement.setString(REVIEW_COMMENT, review.getComment());
        statement.executeUpdate();

        updateRating(connection, review.getAppId());

        LOGGER.info("Review added and rating updated for App ID: {}", review.getAppId());
      }
    } catch (final SQLException exception) {
      LOGGER.error("Error adding review for App ID {}: {}", review.getAppId(), exception.getMessage());

      throw new RuntimeException("Review failed", exception);
    } finally {
      DataSourceUtils.releaseConnection(connection, appDataSource);
    }
  }

  /**
   * Fetches the list of feature strings associated with an App ID.
   */
  private List<String> getAppFeatures(final Connection connection, final int appId)
      throws SQLException {
    final List<String> features = new ArrayList<>();
    final String featuresQuery = "SELECT features FROM features WHERE app_id = ?";

    try (final PreparedStatement statement = connection.prepareStatement(featuresQuery)) {
      statement.setInt(APP_ID, appId);

      try (final ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          String feature = resultSet.getString("features");
          if (feature != null) {
            features.add(feature.trim());
          }
        }
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
      statement.setInt(APP_ID, appId);
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
    final String review = "SELECT AVG(rating) FROM reviews WHERE app_id = ?";
    final String updateAppTable = "UPDATE app SET rating = ? WHERE id = ?";

    double rating = 0.0;

    try (final PreparedStatement statement = connection.prepareStatement(review)) {
      statement.setInt(APP_ID, appId);

      try (final ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          rating = resultSet.getDouble(1);
        }
      }
    }

    try (final PreparedStatement statement = connection.prepareStatement(updateAppTable)) {
      statement.setDouble(1, rating);
      statement.setInt(2, appId);
      statement.executeUpdate();
    }
  }
}
