package com.minionslab.core.domain.tools.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.minionslab.core.common.annotation.Tool;
import com.minionslab.core.common.annotation.Toolbox;
import com.minionslab.core.domain.tools.BaseAPIToolBox;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Tool for performing Google searches using SerpApi. Provides structured access to Google search results.
 */
@Slf4j
@Component
@Toolbox(name = "SerpApi", version = "1.0", categories = "data")
public class SerpApiTool extends BaseAPIToolBox {

  @Value("${serp.api.key}")
  private String apiKey;

  @Value("${serp.api.base-url:https://serpapi.com/search}")
  private String baseUrl;

  @Override
  protected void validateConfig() {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalStateException("SerpApi key is not configured");
    }
    if (baseUrl == null || baseUrl.isEmpty()) {
      throw new IllegalStateException("SerpApi base URL is not configured");
    }
  }

  @Override
  protected String getBaseUrl() {
    return baseUrl;
  }

  @Override
  protected String getApiVersion() {
    return ""; // SerpApi doesn't use versioning in the URL
  }

  /**
   * Performs a Google search and returns structured results
   *
   * @param query      The search query
   * @param location   Optional location for localized results
   * @param numResults Number of results to return (default: 10)
   * @return Search results
   */
  @Tool(
      id = "googleSearch",
      name = "Google Search",
      description = "Perform a Google search and return structured results",
      categories = {"search", "information"}
  )
  public SearchResponse search(String query, String location, Integer numResults) {
    validateConfig();

    Map<String, Object> params = Map.of(
        "q", query,
        "api_key", apiKey,
        "engine", "google",
        "num", numResults != null ? numResults : 10
    );

    if (location != null && !location.isEmpty()) {
      params.put("location", location);
    }

    String url = getBaseUrl() + "?" + buildQueryString(params);
    ResponseEntity<SearchResponse> response = makeRequest(url, HttpMethod.GET, null, SearchResponse.class);
    return response.getBody();
  }

  private String buildQueryString(Map<String, Object> params) {
    return params.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .reduce((a, b) -> a + "&" + b)
        .orElse("");
  }

  @Data
  public static class SearchResponse {

    @JsonProperty("organic_results")
    private List<SearchResult> organicResults;
    private String status;
    private String error;
  }

  @Data
  public static class SearchResult {

    private String title;
    private String link;
    private String snippet;
    private String position;
    private Map<String, Object> richSnippet;
  }
} 