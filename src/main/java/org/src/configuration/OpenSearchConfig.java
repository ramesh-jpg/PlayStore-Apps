package org.src.configuration;

import org.apache.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;

/**
 * Configuration class to initialize the OpenSearch client.
 */
@Configuration
public final class OpenSearchConfig {

  /**
   * Creates and configures the OpenSearchClient bean for the application.
   * Connects to the local OpenSearch instance on port 9200.
   */
  @Bean
  public OpenSearchClient openSearchClient(){

    // REST client to communicate with the OpenSearch node
    final RestClient restClient = RestClient.builder(
        new HttpHost("localhost", 9200, "http")
    ).build();

    // Configure the transport layer with Jackson for JSON mapping
    final OpenSearchTransport searchTransport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());

    // Return the high-level OpenSearch client
    return new OpenSearchClient(searchTransport);
  }
}
