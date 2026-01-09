package org.src.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.src.model.App;
import org.src.model.Review;
import org.src.service.AppService;

import java.util.Collection;

/**
 * REST Controller for managing applications in the PlayStore
 *
 * <p>Handles CRUD operations, installations, and reviews by interacting with {@link AppService}.
 */
@RestController
@RequestMapping("/api/apps")
public final class AppController {
  private static final Logger logger = LoggerFactory.getLogger(AppController.class);

  private final AppService appService;

  /**
   * Constructs the AppController with the required service dependency.
   */
  @Autowired
  public AppController(final AppService appService) {
    this.appService = appService;
  }

  /**
   * Creates a new application in the PlayStore.
   */
  @PostMapping
  public ResponseEntity<String> createApp(@Valid @RequestBody final App app) {
    logger.info("Request received to create app: {}", app.getName());

    appService.createApp(app);
    logger.info("App '{}' created successfully.", app.getName());

    return ResponseEntity.status(HttpStatus.CREATED).body("App Created Successfully!");
  }

  /**
   * Retrieves a list of all available applications.
   */
  @GetMapping
  public ResponseEntity<Collection<App>> listApps() {
    logger.info("Request received to list all apps.");

    return ResponseEntity.ok(appService.listApps());
  }

  /**
   * Updates an existing application's
   */
  @PutMapping
  public ResponseEntity<String> updateApp(@Valid @RequestBody final App app) {
    logger.info("Request received to update App ID: {}", app.getId());

    appService.updateApp(app);
    logger.info("App ID '{}' updated successfully.", app.getId());

    return ResponseEntity.ok("App Updated Successfully!");
  }

  /**
   * Deletes an application from the PlayStore.
   *
   * <p>Requires both the App ID and Author ID to ensure only Author can delete it.
   *
   * @param appId    the unique identifier of the app to be deleted
   * @param authorId the unique identifier of the author performing the deletion
   */
  @DeleteMapping("/{appId}")
  public ResponseEntity<String> deleteApp(
      @PathVariable final int appId, @RequestParam final int authorId) {
    logger.info("Request received to delete App ID: {} by Author ID: {}", appId, authorId);

    appService.deleteApp(appId, authorId);
    logger.info("App ID {} deleted successfully.", appId);

    return ResponseEntity.ok("App Deleted Successfully!");
  }

  /**
   * Installs an application for a specific user.
   */
  @PostMapping("/install")
  public ResponseEntity<String> installApp(
      @RequestParam final int userId, @RequestParam final int appId) {
    logger.info("Request received: User {} installing App {}", userId, appId);

    appService.installApp(userId, appId);
    logger.info("App {} installed successfully for User {}.", appId, userId);

    return ResponseEntity.ok("App Installed Successfully!");
  }

  /**
   * Uninstalls an application for a specific user.
   */
  @PostMapping("/uninstall")
  public ResponseEntity<String> uninstallApp(
      @RequestParam final int userId, @RequestParam final int appId) {
    logger.info("Request received: User {} uninstalling App {}", userId, appId);

    appService.uninstallApp(userId, appId);
    logger.info("App {} uninstalled successfully for User {}.", appId, userId);

    return ResponseEntity.ok("App Uninstalled Successfully!");
  }

  /**
   * Submits a review and rating for an application.
   */
  @PostMapping("/review")
  public ResponseEntity<String> writeReview(@Valid @RequestBody final Review review) {
    logger.info("Request received: Review for App ID {}", review.getAppId());

    appService.writeReview(review);
    logger.info("Review added successfully for App ID {}.", review.getAppId());

    return ResponseEntity.ok("Review Added Successfully!");
  }
}
