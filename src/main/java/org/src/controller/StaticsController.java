package org.src.controller;

import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.src.model.App;
import org.src.service.AppStaticsService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for retrieving application statistics and analytics.
 *
 * <p>Handles generating reports for user-installed apps and author installation counts.
 */
@RestController
@RequestMapping("/api/statics")
public final class StaticsController {
  private static final Logger LOGGER = LoggerFactory.getLogger(StaticsController.class);

  private static final String AUTHOR = "author";
  private static final String TOTAL_INSTALLS = "totalInstalls";

  private final AppStaticsService staticsService;

  /**
   * Constructs the StaticsController with the required service dependency.
   */
  @Autowired
  public StaticsController(final AppStaticsService staticsService) {
    this.staticsService = staticsService;
  }

  /**
   * Retrieves the list of applications installed by a specific user.
   *
   * @param userId the unique identifier of the user.
   */
  @GetMapping("/installed/{userId}")
  public ResponseEntity<Collection<App>> showInstalledApps(@PathVariable final int userId) {
    LOGGER.info("Request received: Fetch installed apps for User ID: {}", userId);

    final Collection<App> apps = staticsService.showInstalledApps(userId);

    if (apps.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    LOGGER.info("Successfully {} installed apps for User ID: {}", apps.size(), userId);

    return ResponseEntity.ok(apps);
  }

  /**
   * Calculates total installations for all apps by a specific author.
   *
   * @param authorName the name of the author.
   */
  @GetMapping("/author")
  public ResponseEntity<Map<String, Object>> countInstallsByAuthor(
      @RequestParam
      @NotBlank(message = "Author name is required")
      final String authorName) {
    LOGGER.info("Request received: Count total installs for Author: '{}'", authorName);

    final int count = staticsService.countInstallsByAuthor(authorName);

    final Map<String, Object> response = new HashMap<>();
    response.put(AUTHOR, authorName);
    response.put(TOTAL_INSTALLS, count);

    LOGGER.info("Total installs calculated for Author '{}': {}", authorName, count);

    return ResponseEntity.ok(response);
  }
}
