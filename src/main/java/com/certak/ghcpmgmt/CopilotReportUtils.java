package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.config.AppConfig;
import com.certak.ghcpmgmt.model.Budget;
import com.certak.ghcpmgmt.model.BudgetsResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Shared utilities for Copilot usage report selection and CSV parsing.
 */
final class CopilotReportUtils {

    static final Path REPORTS_DIR = Path.of("reports");
    private static final DateTimeFormatter MODIFIED_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private CopilotReportUtils() {}

    /**
     * Lists CSV files in the reports directory (newest first), prompts the user to pick one.
     *
     * @return the selected Path, or null if unavailable or selection fails
     */
    static Path selectReport() throws IOException {
        if (!Files.exists(REPORTS_DIR)) {
            System.err.println("No reports directory found. Run 'billing copilot-usage' to download a report first.");
            return null;
        }

        List<Path> files;
        try (var stream = Files.list(REPORTS_DIR)) {
            files = stream
                    .filter(p -> p.getFileName().toString().endsWith(".csv"))
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                        } catch (IOException e) {
                            return a.getFileName().toString().compareTo(b.getFileName().toString());
                        }
                    })
                    .toList();
        }

        if (files.isEmpty()) {
            System.err.println("No CSV reports found in " + REPORTS_DIR + ". Run 'billing copilot-usage' to download a report first.");
            return null;
        }

        System.out.println("Available reports:");
        for (int i = 0; i < files.size(); i++) {
            Path f = files.get(i);
            BasicFileAttributes attrs = Files.readAttributes(f, BasicFileAttributes.class);
            String modified = MODIFIED_FMT.format(attrs.lastModifiedTime().toInstant());
            System.out.printf("  %2d.  %-55s  %s%n", i + 1, f.getFileName(), modified);
        }

        System.out.print("\nSelect a report (1-" + files.size() + "): ");
        System.out.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null || line.isBlank()) {
            System.err.println("No input provided.");
            return null;
        }
        try {
            int choice = Integer.parseInt(line.trim());
            if (choice < 1 || choice > files.size()) {
                System.err.println("Invalid selection: " + choice);
                return null;
            }
            return files.get(choice - 1);
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a number.");
            return null;
        }
    }

    /**
     * Parses a Copilot AI credit usage CSV and aggregates usage per user.
     * Rows are summed across all models/dates for each user.
     *
     * @return list sorted by usage percent, highest to lowest
     */
    static List<UserUsage> parseReport(Path csvFile) throws IOException {
        // userId -> [totalQuantity, quota]
        Map<String, double[]> totals = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            String header = reader.readLine();
            if (header == null) return List.of();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] fields = parseCsvLine(line);
                if (fields.length < 12) continue;

                String userId = fields[1];
                double quantity = parseDouble(fields[5]);
                double quota = parseDouble(fields[11]);

                totals.merge(userId, new double[]{quantity, quota}, (existing, incoming) -> {
                    existing[0] += incoming[0];
                    existing[1] = Math.max(existing[1], incoming[1]);
                    return existing;
                });
            }
        }

        return totals.entrySet().stream()
                .map(e -> new UserUsage(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingDouble(UserUsage::usagePercent).reversed())
                .toList();
    }

    static void printTable(List<UserUsage> users, Map<String, String> names, Map<String, Integer> budgets) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        boolean hasBudgets = users.stream().anyMatch(u -> budgets.containsKey(u.userId));

        if (hasBudgets) {
            System.out.printf("%-5s  %-10s  %-30s  %13s  %13s  %8s  %16s  %10s%n",
                    "#", "User ID", "Name", "Credits Used", "Monthly Quota", "Usage %",
                    "Increased Quota", "Usage %");
            System.out.println("-".repeat(117));
        } else {
            System.out.printf("%-5s  %-10s  %-30s  %13s  %13s  %8s%n",
                    "#", "User ID", "Name", "Credits Used", "Monthly Quota", "Usage %");
            System.out.println("-".repeat(87));
        }

        int rank = 1;
        for (UserUsage u : users) {
            String name = names.getOrDefault(u.userId, "");
            if (hasBudgets) {
                Integer budget = budgets.get(u.userId);
                if (budget != null) {
                    long increasedQuota = (long) budget * 100;
                    double quotaPct = increasedQuota > 0 ? (u.totalQuantity / increasedQuota) * 100.0 : 0.0;
                    System.out.printf("%-5d  %-10s  %-30s  %13.2f  %13.0f  %7.1f%%  %15d  %9.1f%%%n",
                            rank++, u.userId, name, u.totalQuantity, u.quota, u.usagePercent(),
                            increasedQuota, quotaPct);
                } else {
                    System.out.printf("%-5d  %-10s  %-30s  %13.2f  %13.0f  %7.1f%%  %15s  %10s%n",
                            rank++, u.userId, name, u.totalQuantity, u.quota, u.usagePercent(),
                            "-", "-");
                }
            } else {
                System.out.printf("%-5d  %-10s  %-30s  %13.2f  %13.0f  %7.1f%%%n",
                        rank++, u.userId, name, u.totalQuantity, u.quota, u.usagePercent());
            }
        }
    }

    /**
     * Fetches all user-scoped budgets from the enterprise billing API (paginated).
     * Returns a map of user login -> budget_amount. Falls back to empty map on failure.
     */
    static Map<String, Integer> fetchUserBudgets() {
        try {
            AppConfig config = AppConfig.load();
            String enterprise = config.getEnterprise();
            if (enterprise == null || enterprise.isBlank()) {
                System.err.println("Warning: github.enterprise not configured — skipping budget lookup.");
                return Map.of();
            }

            GitHubClient client = new GitHubClient(config);
            Map<String, Integer> result = new HashMap<>();
            int page = 1;

            while (true) {
                String path = "/enterprises/" + enterprise + "/settings/billing/budgets?per_page=100&page=" + page;
                BudgetsResponse response = client.get(path, BudgetsResponse.class);

                if (response.getBudgets() != null) {
                    for (Budget b : response.getBudgets()) {
                        if ("user".equals(b.getBudget_scope())
                                && b.getUser() != null && !b.getUser().isBlank()
                                && b.getBudget_amount() != null) {
                            result.put(b.getUser(), b.getBudget_amount());
                        }
                    }
                }

                if (Boolean.TRUE.equals(response.getHas_next_page())) {
                    page++;
                } else {
                    break;
                }
            }

            return result;
        } catch (Exception e) {
            System.err.println("Warning: could not fetch budget info: " + e.getMessage());
            return Map.of();
        }
    }

    /**
     * Resolves display names for all users in the list.
     * Falls back to empty map (IDs only) if config or API is unavailable.
     */
    static Map<String, String> resolveNames(List<UserUsage> users) {
        try {
            AppConfig config = AppConfig.load();
            GitHubClient client = new GitHubClient(config);
            List<String> ids = users.stream().map(u -> u.userId).toList();
            return UserNameCache.resolveNames(ids, client);
        } catch (Exception e) {
            System.err.println("Warning: could not resolve user names: " + e.getMessage());
            return Map.of();
        }
    }

    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        int i = 0;
        while (i < line.length()) {
            if (line.charAt(i) == '"') {
                int end = line.indexOf('"', i + 1);
                if (end == -1) end = line.length();
                fields.add(line.substring(i + 1, end));
                i = end + 1;
                if (i < line.length() && line.charAt(i) == ',') i++;
            } else if (line.charAt(i) == ',') {
                fields.add("");
                i++;
            } else {
                int end = line.indexOf(',', i);
                if (end == -1) end = line.length();
                fields.add(line.substring(i, end));
                i = end + 1;
            }
        }
        return fields.toArray(new String[0]);
    }

    private static double parseDouble(String s) {
        try {
            return (s == null || s.isBlank()) ? 0.0 : Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
