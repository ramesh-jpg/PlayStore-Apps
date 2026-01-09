package org.src.service;

import org.src.model.App;
import org.src.model.Review;

import java.util.Collection;

/**
 * Service interface for managing application business logic and administrative tasks.
 *
 * <p>Handles core operations like app creation, updates, installations, and reviews.
 */
public interface AppService {

  /**
   * Creates a new application and persists it to storage.
   *
   * @param app the {@link App} object containing details to be saved.
   */
  void createApp(final App app);

  /**
   * Updates details of an existing application.
   *
   * @param app the {@link App} object containing updated information.
   */
  void updateApp(final App app);

  /**
   * Permanently removes an application from the system.
   *
   * @param appId    the unique identifier of the app to delete.
   * @param authorId the ID of the author requesting the deletion.
   */
  void deleteApp(final int appId, final int authorId);

  /**
   * Retrieves all applications available in the PlayStore.
   *
   * @return a collection of all {@link App} objects.
   */
  Collection<App> listApps();

  /**
   * Installs an application on the user's device.
   *
   * @param userId the ID of the user installing the app.
   * @param appId  the ID of the app to be installed.
   */
  void installApp(final int userId, final int appId);

  /**
   * Removes an installed application from the user's device.
   *
   * @param userId the ID of the user uninstalling the app.
   * @param appId  the ID of the app to be uninstalled.
   */
  void uninstallApp(final int userId, final int appId);

  /**
   * Submits a user review for an application.
   *
   * @param review the {@link Review} object containing the rating and feedback.
   */
  void writeReview(final Review review);
}
