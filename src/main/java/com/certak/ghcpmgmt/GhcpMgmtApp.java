package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.HttpClients;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Root command for the GitHub management CLI.
 *
 * Usage:
 *   ghcp-mgmt                    Show this help
 *   ghcp-mgmt user               Show user subcommands
 *   ghcp-mgmt user me            Get authenticated user
 */
@Command(name = "ghcp-mgmt",
        version = "ghcp-mgmt 1.0.0",
        description = "GitHub REST API management CLI",
        mixinStandardHelpOptions = true,
        subcommands = {UserCommand.class, BillingCommand.class, BudgetCommand.class})
public class GhcpMgmtApp implements Runnable {

    @Spec
    private CommandSpec spec;

    private static final String[] CONNECTIVITY_CHECK_URLS = {
            "http://cp.cloudflare.com",
            "http://neverssl.com/"
    };

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        checkConnectivity();
        int exitCode = new CommandLine(new GhcpMgmtApp()).execute(args);
        System.exit(exitCode);
    }

    private static void checkConnectivity() {
        HttpClient client = HttpClients.shared();
        System.err.println("Checking internet connectivity...");
        HttpClients.printProxyInfo(URI.create(CONNECTIVITY_CHECK_URLS[0]));

        for (String urlStr : CONNECTIVITY_CHECK_URLS) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlStr))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());
                int code = response.statusCode();
                if (code >= 200 && code < 300) {
                    System.err.println("  Internet: OK (" + urlStr + " returned " + code + ")");
                    return;
                } else {
                    System.err.println("  " + urlStr + " returned " + code + ", trying next...");
                }
            } catch (Exception e) {
                System.err.println("  " + urlStr + ": " + e.getMessage() + ", trying next...");
            }
        }
        System.err.println("  WARNING: Could not reach any connectivity check endpoint.");
        System.err.println("  Internet access may be blocked or misconfigured.");
    }

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
