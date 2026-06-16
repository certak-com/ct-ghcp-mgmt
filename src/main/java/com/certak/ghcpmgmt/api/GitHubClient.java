package com.certak.ghcpmgmt.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.certak.ghcpmgmt.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
        this.httpClient = HttpClients.shared();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Perform a GET request to the GitHub API and deserialize the response.
     */
    public <T> T get(String path, Class<T> responseType) throws GitHubApiException {
        String url = config.getBaseUrl() + path;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            String attemptLabel = attempt == 0 ? "Request" : "Retry " + attempt + "/" + MAX_RETRIES;
            System.err.println(attemptLabel + ": GET " + url);

            if (attempt == 0) {
                HttpClients.printProxyInfo(URI.create(url));
            }

            if (attempt > 0) {
                long delay = (long) Math.pow(2, attempt);
                System.err.println("  Waiting " + delay + "s before retry...");
                try {
                    Thread.sleep(delay * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println(attemptLabel + " FAILED: Request interrupted");
                    ie.printStackTrace();
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
                    System.err.println("  Response: " + response.statusCode());
                    return objectMapper.readValue(response.body(), responseType);
                }

                System.err.println("  Response: " + response.statusCode());

                if (isRetryable(response.statusCode())) {
                    System.err.println("  Retryable status code, will retry...");
                    continue;
                }

                throw new GitHubApiException(response.statusCode(), response.body());

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.err.println(attemptLabel + " FAILED: Request interrupted");
                ie.printStackTrace();
                throw new GitHubApiException("Request interrupted", ie);
            } catch (java.io.IOException e) {
                System.err.println(attemptLabel + " FAILED: " + e.getMessage());
                e.printStackTrace();
                throw new GitHubApiException(-1, "Request failed: " + e.getMessage());
            }
        }

        System.err.println("FAILED: Max retries exceeded for " + url);
        throw new GitHubApiException("Max retries exceeded for " + url);
    }

    private boolean isRetryable(int statusCode) {
        return statusCode == 429 || (statusCode >= 500 && statusCode < 600);
    }
}
