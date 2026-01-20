package org.src.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.src.model.App;
import org.src.model.Review;
import org.src.service.AppService;
import org.src.validation.OnCreate;
import org.src.validation.OnUpdate;

import java.util.Collection;

/**
 * REST Controller for managing applications in the PlayStore
 *
 * <p>Handles CRUD operations, installations, and reviews by interacting with {@link AppService}.
 */
@RestController
@RequestMapping("/api/apps")
public final class AppController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppController.class);

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
  public ResponseEntity<String> createApp(@Validated(OnCreate.class)
                                            @RequestBody final App app) {
    LOGGER.info("Request received to create app: {}", app.getName());

    appService.createApp(app);

    LOGGER.info("App '{}' created successfully.", app.getName());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body("App Created Successfully!");
  }

  /**
   * Retrieves a list of all available applications.
   */
  @GetMapping
  public ResponseEntity<Collection<App>> listApps() {
    LOGGER.info("Request received to list all apps.");

    return ResponseEntity
        .ok(appService.listApps());
  }

  /**
   * Updates an existing application's
   */
  @PutMapping
  public ResponseEntity<String> updateApp(@Validated(OnUpdate.class)
                                            @RequestBody final App app) {
    LOGGER.info("Request received to update App ID: {}", app.getId());

    appService.updateApp(app);

    LOGGER.info("App ID '{}' updated successfully.", app.getId());

    return ResponseEntity
        .ok("App Updated Successfully!");
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
      @PathVariable final int appId,
      @RequestParam final int authorId) {
    LOGGER.info("Request received to delete App ID: {} by Author ID: {}", appId, authorId);

    appService.deleteApp(appId, authorId);

    LOGGER.info("App ID {} deleted successfully.", appId);

    return ResponseEntity
        .ok("App Deleted Successfully!");
  }

  /**
   * Installs an application for a specific user.
   */
  @PostMapping("/install")
  public ResponseEntity<String> installApp(
      @RequestParam final int userId, @RequestParam final int appId) {
    LOGGER.info("Request received: User {} installing App {}", userId, appId);

    appService.installApp(userId, appId);

    LOGGER.info("App {} installed successfully for User {}.", appId, userId);

    return ResponseEntity
        .ok("App Installed Successfully!");
  }

  /**
   * Uninstalls an application for a specific user.
   */
  @PostMapping("/uninstall")
  public ResponseEntity<String> uninstallApp(
      @RequestParam final int userId, @RequestParam final int appId) {
    LOGGER.info("Request received: User {} uninstalling App {}", userId, appId);

    appService.uninstallApp(userId, appId);

    LOGGER.info("App {} uninstalled successfully for User {}.", appId, userId);

    return ResponseEntity
        .ok("App Uninstalled Successfully!");
  }

  /**
   * Submits a review and rating for an application.
   */
  @PostMapping("/review")
  public ResponseEntity<String> writeReview(@Validated(OnCreate.class)
                                              @RequestBody final Review review) {
    LOGGER.info("Request received: Review for App ID {}", review.getAppId());

    appService.writeReview(review);

    LOGGER.info("Review added successfully for App ID {}.", review.getAppId());

    return ResponseEntity
        .ok("Review Added Successfully!");
  }
}
