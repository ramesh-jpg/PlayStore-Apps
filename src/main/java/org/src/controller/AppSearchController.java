package org.src.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.src.model.App;
import org.src.repository.AppSearchRepository;
import java.util.List;

/**
 * REST controller for handling app search operations via OpenSearch.
 */
@RestController
@RequestMapping("/api/apps")
public final class AppSearchController {

  private final AppSearchRepository appSearchRepository;

  public AppSearchController(final AppSearchRepository appSearchRepository) {
    this.appSearchRepository = appSearchRepository;
  }

  /**
   * Endpoint to search apps by name or description.
   */
  @GetMapping("/search")
  public ResponseEntity<List<App>>searchApp(@RequestParam final String searchTerm){
    return ResponseEntity.ok(appSearchRepository.searchIndex(searchTerm));
  }
}
