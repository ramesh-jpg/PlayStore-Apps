package org.src.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.src.exception.InvalidRoleException;
import org.src.model.App;
import org.src.model.Review;
import org.src.repository.AppRepository;
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
  private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

  private final AppRepository appRepository;
  private final InstallationRepository installationRepository;

  /**
   * Constructs AppServiceImpl with necessary repository dependencies.
   */
  @Autowired
  public AppServiceImpl(
      final AppRepository appRepository, final InstallationRepository installationRepository) {
    this.appRepository = appRepository;
    this.installationRepository = installationRepository;
  }

  /**
   * Creates a new application after validating author permissions.
   */
  @Override
  public void createApp(final App app) {
    Objects.requireNonNull(app.getAuthor(), "Author details missing.");

    if (!"AUTHOR".equalsIgnoreCase(app.getAuthor().getRole())) {
      throw new InvalidRoleException("Only AUTHOR can create apps.");
    }

    appRepository.save(app);
    logger.info("New app '{}' created successfully.", app.getName());
  }

  /**
   * Updates an existing application's details.
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
    logger.info("App ID {} updated successfully.", app.getId());
  }

  /**
   * Deletes an application from the store.
   */
  @Override
  public void deleteApp(final int appId, final int authorId) {
    final App existingApp = appRepository.findById(appId)
        .orElseThrow(() -> new RuntimeException("App not found "));

    if (existingApp.getAuthor().getId() != authorId) {
      throw new RuntimeException("You are not allowed delete this app.");
    }

    appRepository.delete(appId);
    logger.info("App ID {} deleted by Author ID {}.", appId, authorId);
  }

  /**
   * Lists all available applications in the repository.
   */
  @Override
  public Collection<App> listApps() {
    return appRepository.getAll();
  }

  /**
   * Installs an application for a user.
   */
  @Override
  public void installApp(final int userId, final int appId) {
    appRepository.findById(appId)
        .orElseThrow(() -> new RuntimeException("App not found."));

    if (installationRepository.isInstalled(userId, appId)) {
      throw new RuntimeException("Already installed.");
    }

    installationRepository.installed(userId, appId);
    logger.info("App ID {} installed for User ID {}.", appId, userId);
  }

  /**
   * Uninstalls an application.
   */
  @Override
  public void uninstallApp(final int userId, final int appId) {
    appRepository.findById(appId)
        .orElseThrow(() -> new RuntimeException("App not found."));

    if (!installationRepository.isInstalled(userId, appId)) {
      throw new RuntimeException("App is not installed in this account.");
    }

    final boolean success = installationRepository.uninstalled(userId, appId);
    if (!success) {
      throw new RuntimeException("Uninstall Failed due to server error.");
    }

    logger.info("App ID {} uninstalled for User ID {}.", appId, userId);
  }

  /**
   * Submits a user review for an application.
   */
  @Override
  public void writeReview(final Review review) {
    appRepository
        .findById(review.getAppId())
        .orElseThrow(() -> new RuntimeException("App not found with ID: " + review.getAppId()));

    if (review.getRating() < Review.MIN_RATING || review.getRating() > Review.MAX_RATING) {
      throw new RuntimeException("Rating must be between 1 and 5.");
    }

    appRepository.addReview(review);
    logger.info("Review added for App ID {} by User ID {}.", review.getAppId(), review.getUserId());
  }
}
