package org.src.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.src.exception.InvalidRoleException;
import org.src.model.App;
import org.src.model.Review;
import org.src.repository.AppRepository;
import org.src.repository.AppSearchRepository;
import org.src.repository.InstallationRepository;

import java.util.Collection;
import java.util.Objects;

/**
 * Implementation of the application management services.
 *
 * <p>Central logic layer for handling administrative tasks, user interactions, and validation.
 */
@Service
public class AppServiceImpl implements AppService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppServiceImpl.class);

  private final AppRepository appRepository;
  private final InstallationRepository installationRepository;
  private final AppSearchRepository appSearchRepository;

  /**
   * Constructs AppServiceImpl with necessary repository dependencies.
   */
  @Autowired
  public AppServiceImpl(
      final AppRepository appRepository,
      final InstallationRepository installationRepository,
      final AppSearchRepository appSearchRepository) {
    this.appRepository = appRepository;
    this.installationRepository = installationRepository;
    this.appSearchRepository = appSearchRepository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createApp(final App app) {
    Objects.requireNonNull(app.getAuthor(), "Author details missing.");

    if (!"AUTHOR".equalsIgnoreCase(app.getAuthor().getRole())) {
      throw new InvalidRoleException("Only AUTHOR can create apps.");
    }

    appRepository.save(app);

    try {
      appSearchRepository.indexApp(app);

      LOGGER.info("App indexed in OpenSearch: {}", app.getName());
    } catch (final Exception exception) {
      LOGGER.error("Failed to index app '{}' in OpenSearch: {}",
          app.getName(), exception.getMessage());
    }
    LOGGER.info("New app '{}' created successfully.", app.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateApp(final App app) {
    final App existingApp = appRepository.findById(app.getId())
        .orElseThrow(() -> new RuntimeException("App not found "));

    if (existingApp.getAuthor().getId() != app.getAuthor().getId()) {
      throw new RuntimeException(" You are not allowed Update this app.");
    }

    existingApp.setName(app.getName());
    existingApp.setFeatures(app.getFeatures());
    existingApp.setDescription(app.getDescription());
    existingApp.setVersion(app.getVersion());

    appRepository.update(existingApp);

    try {
      appSearchRepository.indexApp(existingApp);

      LOGGER.info("OpenSearch index updated for App ID: {}", app.getId());
    } catch (final Exception exception) {
      LOGGER.error("Failed to update OpenSearch index for App ID {}: {}",
          app.getId(), exception.getMessage());
    }

    LOGGER.info("App ID {} updated successfully.", app.getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteApp(final int appId, final int authorId) {
    final App existingApp = appRepository.findById(appId)
        .orElseThrow(() -> new RuntimeException("App not found "));

    if (existingApp.getAuthor().getId() != authorId) {
      throw new RuntimeException("You are not allowed delete this app.");
    }

    appRepository.delete(appId);

    try {
      appSearchRepository.deleteIndex(appId);

      LOGGER.info("App ID {} deleted from OpenSearch index.", appId);
    } catch (final Exception exception) {
      LOGGER.error("Failed to delete App ID {} from OpenSearch: {}",
          appId, exception.getMessage());
    }

    LOGGER.info("App ID {} deleted by Author ID {}.", appId, authorId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<App> listApps() {
    return appRepository.getAll();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void installApp(final int userId, final int appId) {
    appRepository.findById(appId)
        .orElseThrow(() -> new RuntimeException("App not found."));

    if (installationRepository.isInstalled(userId, appId)) {
      throw new RuntimeException("Already installed.");
    }

    installationRepository.installed(userId, appId);
    try {
      appRepository.findById(appId)
          .ifPresent(appSearchRepository::indexApp);

      LOGGER.info("App ID {} installation to OpenSearch.", appId);
    } catch (final Exception exception) {
      LOGGER.error("AppID {} installation failed to OpenSearch", exception.getMessage());
    }

    LOGGER.info("App ID {} installed for User ID {}.", appId, userId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void uninstallApp(final int userId, final int appId) {
    appRepository.findById(appId)
        .orElseThrow(() -> new RuntimeException("App not found."));

    if (!installationRepository.isInstalled(userId, appId)) {
      throw new RuntimeException("App is not installed in this account.");
    }

    final boolean success = installationRepository
        .uninstalled(userId, appId);
    if (!success) {
      throw new RuntimeException("Uninstall Failed due to server error.");
    }

    LOGGER.info("App ID {} uninstalled for User ID {}.", appId, userId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeReview(final Review review) {
    appRepository.findById(review.getAppId())
        .orElseThrow(() -> new RuntimeException("App not found with ID: " + review.getAppId()));

    if (review.getRating() < Review.MIN_RATING ||
        review.getRating() > Review.MAX_RATING) {
      throw new RuntimeException("Rating must be between 1 and 5.");
    }

    appRepository.addReview(review);

    LOGGER.info("Review added for App ID {} by User ID {}.",
        review.getAppId(), review.getUserId());
  }
}
