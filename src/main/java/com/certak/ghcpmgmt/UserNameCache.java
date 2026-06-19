package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubApiException;
import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache for GitHub user display names and emails backed by mappings/mappings.txt.
 * Format: one entry per line as  userId=Full Name|email@example.com
 *
 * Lookups check the file first; misses are resolved via GET /users/{id} and persisted.
 */
final class UserNameCache {

    static final Path MAPPINGS_FILE = Path.of("mappings", "mappings.txt");
    private static final long THROTTLE_MS = 50;

    private UserNameCache() {}

    static Map<String, UserInfo> load() {
        Map<String, UserInfo> map = new LinkedHashMap<>();
        if (!Files.exists(MAPPINGS_FILE)) return map;
        try {
            for (String line : Files.readAllLines(MAPPINGS_FILE)) {
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                int pipe = value.indexOf('|');
                if (pipe >= 0) {
                    map.put(key, new UserInfo(value.substring(0, pipe), value.substring(pipe + 1)));
                } else {
                    map.put(key, new UserInfo(value, null));
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read " + MAPPINGS_FILE + ": " + e.getMessage());
        }
        return map;
    }

    static void save(Map<String, UserInfo> mappings) {
        try {
            Files.createDirectories(MAPPINGS_FILE.getParent());
            List<String> lines = new ArrayList<>();
            mappings.forEach((k, v) -> {
                String email = (v.email() != null) ? v.email() : "";
                lines.add(k + "=" + v.name() + "|" + email);
            });
            Files.write(MAPPINGS_FILE, lines);
        } catch (IOException e) {
            System.err.println("Warning: could not save " + MAPPINGS_FILE + ": " + e.getMessage());
        }
    }

    /**
     * Resolves display names and emails for all provided user IDs.
     * Hits the cache first; fetches from GitHub for unknowns (throttled at 50ms per call).
     * Persists any new entries to the mappings file.
     */
    static Map<String, UserInfo> resolveNames(List<String> userIds, GitHubClient client) {
        Map<String, UserInfo> cache = load();
        boolean changed = false;

        for (String userId : userIds) {
            if (cache.containsKey(userId)) {
                UserInfo info = cache.get(userId);
                // System.err.println("Name lookup [cached]: " + userId + " -> " + info.name()
                //         + (info.email() != null && !info.email().isBlank() ? " <" + info.email() + ">" : ""));
                continue;
            }

            System.err.println("Name lookup [API]: GET /users/" + userId);
            try {
                User user = client.get("/users/" + userId, User.class);
                String name = (user.getName() != null && !user.getName().isBlank())
                        ? user.getName()
                        : user.getLogin();
                String resolved = (name != null && !name.isBlank()) ? name : userId;
                String email = user.getEmail();
                System.err.println("  Resolved: " + resolved
                        + (email != null && !email.isBlank() ? " <" + email + ">" : ""));
                cache.put(userId, new UserInfo(resolved, email));
                changed = true;
            } catch (GitHubApiException e) {
                System.err.println("  Could not resolve " + userId + ": " + e.getMessage());
                cache.put(userId, new UserInfo(userId, null));
                changed = true;
            }

            try { Thread.sleep(THROTTLE_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }

        if (changed) {
            save(cache);
            System.err.println("Mappings saved to " + MAPPINGS_FILE.toAbsolutePath());
        }

        return cache;
    }
}
