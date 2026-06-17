package com.certak.ghcpmgmt.api;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Centralized HttpClient factory.
 * All HTTP clients in this project share this builder config.
 */
public final class HttpClients {

    private HttpClients() {}

    private static final HttpClient INSTANCE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .proxy(ProxySelector.getDefault())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static HttpClient shared() {
        return INSTANCE;
    }

    /**
     * Print proxy details for the given URI to stderr.
     */
    public static void printProxyInfo(URI uri) {
        try {
            ProxySelector selector = INSTANCE.proxy().orElse(null);
            if (selector != null) {
                List<Proxy> proxies = selector.select(uri);
                if (proxies != null && !proxies.isEmpty()) {
                    Proxy proxy = proxies.get(0);
                    if (proxy.type() != Proxy.Type.DIRECT) {
                        InetSocketAddress addr = (InetSocketAddress) proxy.address();
                        if (addr != null) {
                            System.err.println("  Proxy: " + addr.getHostString() + ":" + addr.getPort());
                        } else {
                            System.err.println("  Proxy: " + proxy.type());
                        }
                    } else {
                        System.err.println("  Proxy: none (direct)");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  Proxy: error detecting (" + e.getMessage() + ")");
        }
    }
}
