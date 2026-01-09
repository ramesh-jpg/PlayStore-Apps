package org.src.service;

import org.src.model.App;

import java.util.Collection;

/**
 * Service interface for generating reports and viewing application statistics.
 *
 * <p>Focuses on data retrieval and analysis, such as listing user-installed apps and calculating
 * installation metrics for authors.
 */
public interface AppStaticsService {

  /**
   * Retrieves a list of all applications currently installed by a specific user.
   *
   * @param userId the unique identifier of the user.
   * @return a collection of {@link App} objects installed by the user.
   */
  Collection<App> showInstalledApps(final int userId);

  /**
   * Calculates the total number of installations for a specific author.
   *
   * @param authorName the name of the author.
   * @return the total count of installations across all apps owned by the author.
   */
  int countInstallsByAuthor(final String authorName);
}
