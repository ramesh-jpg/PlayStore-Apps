package org.src.repository;

import org.src.model.App;
import org.src.model.Review;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository interface for managing App entities.
 *
 * <p>Defines standard CRUD operations and review management for the application data layer.
 */
public interface AppRepository {

  /**
   * Persists a new application to the storage.
   *
   * @param app the {@link App} object containing details to be saved
   */
  void save(final App app);

  /**
   * Retrieves a specific application by its unique identifier.
   *
   * @param id The unique ID of the application.
   * @return an {@link Optional} containing the {@link App} if found, or empty if not found.
   */
  Optional<App> findById(final int id);

  /**
   * Updates the details of an existing application.
   *
   * @param app The {@link App} object containing updated information.
   */
  void update(final App app);

  /**
   * Removes an application from the storage permanently.
   *
   * @param id The unique ID of the application to delete.
   * @return {@code true} if the deletion was successful, {@code false} otherwise.
   */
  boolean delete(final int id);

  /**
   * Retrieves all applications currently stored in the system.
   *
   * @return a collection of all {@link App} objects available in the store
   */
  Collection<App> getAll();

  /**
   * Adds a user review to a specific application.
   *
   * @param review the review object containing the user's rating and comment.
   */
  void addReview(final Review review);
}
