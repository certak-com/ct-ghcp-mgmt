package com.certak.ghcpmgmt.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads configuration from .ghcp-mgmt.properties.
 *
 * Search path (in order):
 *   1. ghcp-mgmt.home system property (set by launcher scripts)
 *   2. Current working directory
 */
public class AppConfig {

    private String token;
    private String baseUrl;
    private String enterprise;
    private String org;

    public static AppConfig load() throws IOException {
        AppConfig config = new AppConfig();
        String home = System.getProperty("ghcp-mgmt.home");
        Path propertiesPath = (home != null && !home.isBlank())
                ? Path.of(home, ".ghcp-mgmt.properties")
                : Path.of(".ghcp-mgmt.properties");

        if (!Files.exists(propertiesPath)) {
            throw new IOException("Config file not found: " + propertiesPath
                    + "\nPlease create .ghcp-mgmt.properties in the working directory.");
        }

        try (InputStream is = Files.newInputStream(propertiesPath)) {
            Properties props = new Properties();
            props.load(is);

            config.token = props.getProperty("github.token", "");
            config.baseUrl = props.getProperty("github.base-url", "https://api.github.com");
            config.enterprise = props.getProperty("github.enterprise", "");
            config.org = props.getProperty("github.org", "");
        }

        if (config.token.isBlank()) {
            throw new IOException("github.token is not set in .ghcp-mgmt.properties");
        }

        return config;
    }

    public String getToken() { return token; }
    public String getBaseUrl() { return baseUrl; }
    public String getEnterprise() { return enterprise; }
    public String getOrg() { return org; }
}
