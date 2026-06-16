package com.certak.ghcpmgmt.api;

import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Centralized HttpClient factory.
 * All HTTP clients in this project share this builder config.
 */
public final class HttpClients {

    private HttpClients() {}

    private static final HttpClient INSTANCE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .proxy(ProxySelector.getDefault())
            .build();

    public static HttpClient shared() {
        return INSTANCE;
    }
}
