package com.certak.ghcpmgmt;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

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
        subcommands = {UserCommand.class})
public class GhcpMgmtApp implements Runnable {

    @Spec
    private CommandSpec spec;

    private static final String[] CONNECTIVITY_CHECK_URLS = {
            "http://cp.cloudflare.com",
            "http://neverssl.com/"
    };

    public static void main(String[] args) {
        checkConnectivity();
        int exitCode = new CommandLine(new GhcpMgmtApp()).execute(args);
        System.exit(exitCode);
    }

    private static void checkConnectivity() {
        System.err.println("Checking internet connectivity...");
        for (String urlStr : CONNECTIVITY_CHECK_URLS) {
            try {
                URL url = URI.create(urlStr).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setInstanceFollowRedirects(false);
                int code = conn.getResponseCode();
                String host = url.getHost();
                if (code >= 200 && code < 300) {
                    System.err.println("  Internet: OK (" + host + " returned " + code + ")");
                    return;
                } else {
                    System.err.println("  " + host + " returned " + code + ", trying next...");
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
