package com.certak.ghcpmgmt;

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

    static void printTable(List<UserUsage> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        System.out.printf("%-5s  %-10s  %15s  %13s  %8s%n", "#", "User ID", "Credits Used", "Monthly Quota", "Usage %");
        System.out.println("-".repeat(59));
        int rank = 1;
        for (UserUsage u : users) {
            System.out.printf("%-5d  %-10s  %15.2f  %13.0f  %7.1f%%%n",
                    rank++, u.userId, u.totalQuantity, u.quota, u.usagePercent());
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
