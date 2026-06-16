package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubApiException;
import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.config.AppConfig;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Shared utilities for GitHub API commands.
 * Generic — works with any response type.
 */
final class ApiUtils {

    private ApiUtils() {}

    /**
     * Run an API call with shared error handling, then print the result.
     *
     * @return exit code (0 = success, 1 = failure)
     */
    static <T> int run(Function<GitHubClient, T> apiCall, Consumer<T> printer) {
        try {
            AppConfig config = AppConfig.load();
            GitHubClient client = new GitHubClient(config);
            T result = apiCall.apply(client);
            printer.accept(result);
            return 0;
        } catch (GitHubApiException e) {
            printApiError(e);
            return 1;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    static void printApiError(GitHubApiException e) {
        System.err.println("Error: " + e.getMessage());
        if (e.getStatusCode() > 0) {
            System.err.println("Status: " + e.getStatusCode());
        }
        if (e.getResponseBody() != null) {
            System.err.println("Response: " + e.getResponseBody());
        }
    }
}
