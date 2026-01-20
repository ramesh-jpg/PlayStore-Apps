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
  private static final Logger LOGGER = LoggerFactory.getLogger(AppStaticsServiceImpl.class);

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
   * {@inheritDoc}
   */
  @Override
  public Collection<App> showInstalledApps(final int userId) {
    LOGGER.info("Generating report: Fetching installed apps for User ID: {}", userId);

    final Collection<App> installedApps = installationRepository.getInstalledApps(userId);

    LOGGER.info("App {} installed apps for User ID: {}",
        installedApps.size(), userId);

    return installedApps;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int countInstallsByAuthor(final String authorName) {
    LOGGER.info("Calculating total installations for Author: '{}'", authorName);

    int totalCount = 0;
    final String searchName = authorName.trim().toLowerCase();
    for (final App app : appRepository.getAll()) {
      if (app.getAuthorName().trim().equalsIgnoreCase(searchName)) {
        totalCount += app.getInstalledCount();
      }
    }

    LOGGER.info("Total installs for Author '{}': {}",
        authorName, totalCount);

    return totalCount;
  }
}
