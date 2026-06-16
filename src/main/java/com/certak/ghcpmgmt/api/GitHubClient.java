package com.certak.ghcpmgmt.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.certak.ghcpmgmt.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin wrapper around the GitHub REST API using java.net.http.HttpClient.
 * Handles auth headers, API version, and retry logic for 429/5xx.
 */
public class GitHubClient {

    private static final String API_VERSION = "2026-03-10";
    private static final int MAX_RETRIES = 3;

    private final AppConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitHubClient(AppConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Perform a GET request to the GitHub API and deserialize the response.
     */
    public <T> T get(String path, Class<T> responseType) throws GitHubApiException {
        String url = config.getBaseUrl() + path;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            if (attempt > 0) {
                long delay = (long) Math.pow(2, attempt);
                System.err.println("Retry " + attempt + "/" + MAX_RETRIES
                        + " (after " + delay + "s)...");
                try {
                    Thread.sleep(delay * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new GitHubApiException("Request interrupted", ie);
                }
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + config.getToken())
                    .header("X-GitHub-Api-Version", API_VERSION)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-RateLimit-Reset", "true")
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readValue(response.body(), responseType);
                }

                if (isRetryable(response.statusCode())) {
                    continue;
                }

                throw new GitHubApiException(response.statusCode(), response.body());

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new GitHubApiException("Request interrupted", ie);
            } catch (java.io.IOException e) {
                throw new GitHubApiException(-1, "Request failed: " + e.getMessage());
            }
        }

        throw new GitHubApiException("Max retries exceeded for " + url);
    }

    private boolean isRetryable(int statusCode) {
        return statusCode == 429 || (statusCode >= 500 && statusCode < 600);
    }
}
