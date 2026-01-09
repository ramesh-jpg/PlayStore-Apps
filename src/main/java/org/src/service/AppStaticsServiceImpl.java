package org.src.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.src.model.App;
import org.src.repository.AppRepository;
import org.src.repository.InstallationRepository;

import java.util.Collection;

/**
 * Implementation of the {@link AppStaticsService}.
 *
 * <p>Fetches data from repositories and performs aggregation for reports and statistics.
 */
@Service
public class AppStaticsServiceImpl implements AppStaticsService {
  private static final Logger logger = LoggerFactory.getLogger(AppStaticsServiceImpl.class);

  private final AppRepository appRepository;
  private final InstallationRepository installationRepository;

  /**
   * Constructs the service with required repository dependencies.
   */
  @Autowired
  public AppStaticsServiceImpl(
      final AppRepository appRepository, final InstallationRepository installationRepository) {
    this.appRepository = appRepository;
    this.installationRepository = installationRepository;
  }

  /**
   * Retrieves all apps currently installed by a specific user.
   *
   * @param userId the unique identifier of the user.
   */
  @Override
  public Collection<App> showInstalledApps(final int userId) {
    logger.info("Generating report: Fetching installed apps for User ID: {}", userId);

    final Collection<App> installedApps = installationRepository.getInstalledApps(userId);

    logger.info("App {} installed apps for User ID: {}", installedApps.size(), userId);
    return installedApps;
  }

  /**
   * Calculates the total number of installations for a specific author.
   *
   * <p>This method iterates through all apps, matches the author name, and aggregates the installed
   * counts.
   *
   * @param authorName the name of the author to calculate stats for
   * @return the total installation count across all apps owned by the author
   */
  @Override
  public int countInstallsByAuthor(final String authorName) {
    logger.info("Calculating total installations for Author: '{}'", authorName);

    int totalCount = 0;
    final String searchName = authorName.trim().toLowerCase();
    for (final App app : appRepository.getAll()) {
      if (app.getAuthorName().trim().equalsIgnoreCase(searchName)) {
        totalCount += app.getInstalledCount();
      }
    }

    logger.info("Total installs for Author '{}': {}", authorName, totalCount);
    return totalCount;
  }
}
