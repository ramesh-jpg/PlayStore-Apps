package org.src.repository;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.src.model.App;

import java.io.IOException;
import java.util.List;

@Repository
public class AppSearchRepositoryImpl implements AppSearchRepository{
  private static final Logger LOGGER = LoggerFactory.getLogger(AppSearchRepositoryImpl.class);

  private static final String INDEX_NAME = "apps";

  private final OpenSearchClient openSearchClient;

  public AppSearchRepositoryImpl(final OpenSearchClient openSearchClient) {
    this.openSearchClient = openSearchClient;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void indexApp(final App app) {
    try{
      openSearchClient.index(request -> request
          .index(INDEX_NAME)
          .id(String.valueOf(app.getId()))
          .document(app)
      );
    }catch (final IOException exception){
      throw new RuntimeException("OpenSearch Index is failed",exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteIndex(final int appId) {
    try {
      openSearchClient.delete(request -> request
          .index(INDEX_NAME)
          .id(String.valueOf(appId))
      );
    }catch(final IOException exception){
      throw new RuntimeException("OpenSearch Failed to Delete", exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<App> searchIndex(final String searchTerm) {
    try {
      return openSearchClient
          .search(searchRequest -> searchRequest
              .index(INDEX_NAME)
              .query(queryBuilder -> queryBuilder
                  .multiMatch(multiMatchQuery -> multiMatchQuery
                      .fields("name", "description")
                      .query(searchTerm)
                      .fuzziness("AUTO")
                  )
              ), App.class)
          .hits().hits()
          .stream()
          .map(Hit::source)
          .toList();
    } catch (final IOException exception) {
      LOGGER.error("OpenSearch search failed : {}", exception.getMessage());

      return List.of();
    }

  }
}
