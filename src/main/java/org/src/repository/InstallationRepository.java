package org.src.repository;

import org.src.model.App;

import java.util.Collection;

/**
 * Repository interface for managing application installations.
 *
 * <p>Defines operations to track user-app installations, uninstalls, and status checks.
 */
public interface InstallationRepository {

  /**
   * Records a new installation of an app for a user.
   *
   * @param userId the unique identifier of the user.
   * @param appId  the unique identifier of the app.
   * @return {@code true} if recorded successfully, {@code false} otherwise.
   */
  boolean installed(final int userId, final int appId);

  /**
   * Removes an installation record (Uninstalls the app).
   *
   * @param userId the unique identifier of the user
   * @param appId  the unique identifier of the app to be uninstalled
   * @return {@code true} if the uninstallation was successful, {@code false} otherwise
   */
  boolean uninstalled(final int userId, final int appId);

  /**
   * Checks if a specific app is currently installed by a user.
   *
   * @param userId the unique identifier of the user
   * @param appId  the unique identifier of the app
   * @return {@code true} if the app is installed, {@code false} otherwise
   */
  boolean isInstalled(final int userId, final int appId);

  /**
   * Retrieves a list of all apps installed by a specific user.
   *
   * @param userId the unique identifier of the user
   * @return a collection of {@link App} objects representing the user's installed apps
   */
  Collection<App> getInstalledApps(final int userId);
}
