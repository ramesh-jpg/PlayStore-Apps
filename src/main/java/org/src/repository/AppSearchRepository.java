package org.src.repository;

import org.src.model.App;
import java.util.List;

/**
 * Repository interface for managing App data in OpenSearch.
 */
public interface AppSearchRepository {

  /**
   * Adds or updates an app document in the search index.
   */
  void indexApp(final App app);

  /**
   * Removes an app document from the search index by its ID.
   */
  void deleteIndex(final int appId);

  /**
   * Performs a fuzzy search across app names and descriptions.
   */
  List<App> searchIndex(final String searchTerm);
}
