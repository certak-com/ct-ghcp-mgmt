package com.certak.ghcpmgmt.api;

/**
 * Exception thrown when a GitHub API request fails.
 */
public class GitHubApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public GitHubApiException(int statusCode, String responseBody) {
        super("GitHub API error " + statusCode + ": " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public GitHubApiException(String message) {
        super(message);
        this.statusCode = -1;
        this.responseBody = null;
    }

    public int getStatusCode() { return statusCode; }
    public String getResponseBody() { return responseBody; }
}
