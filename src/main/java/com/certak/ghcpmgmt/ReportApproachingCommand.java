package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.config.AppConfig;
import com.certak.ghcpmgmt.model.Budget;
import com.certak.ghcpmgmt.model.BudgetOperationResponse;
import com.certak.ghcpmgmt.model.User;
import picocli.CommandLine;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Show Copilot users approaching their monthly quota limit, with optional budget creation/update.
 */
@CommandLine.Command(
        name = "approaching",
        description = "Show users approaching their monthly quota (within a specified percentage of the limit)",
        mixinStandardHelpOptions = true)
public class ReportApproachingCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = {"--within"},
            description = "Flag users within this percentage of their limit (default: ${DEFAULT-VALUE}%)",
            defaultValue = "10")
    private double withinPercent;

    @CommandLine.Option(
            names = {"--report"},
            description = "Path to the CSV usage report file (skips interactive selection)",
            paramLabel = "<file>")
    private Path reportFile;

    @CommandLine.Option(
            names = {"--budget-action"},
            description = "Budget action to perform without prompting: 1 = create missing budgets, 2 = create or update all",
            paramLabel = "<1|2>")
    private Integer budgetAction;

    @CommandLine.Option(
            names = {"--budget-amount"},
            description = "Budget amount in dollars (skips interactive prompt)",
            paramLabel = "<dollars>")
    private Integer budgetAmount;

    @CommandLine.Option(
            names = {"--yes", "-y"},
            description = "Auto-confirm when new budget is less than the existing budget")
    private boolean yes;

    @Override
    public Integer call() {
        try {
            Path report = CopilotReportUtils.selectReport(reportFile);
            if (report == null) return 1;

            List<UserUsage> users = CopilotReportUtils.parseReport(report);
            double threshold = 100.0 - withinPercent;
            List<UserUsage> approaching = users.stream()
                    .filter(u -> u.usagePercent() >= threshold)
                    .toList();

            Map<String, UserInfo> userInfos = CopilotReportUtils.resolveNames(approaching);
            Map<String, Budget> budgetObjects = CopilotReportUtils.fetchUserBudgetObjects();
            Map<String, Integer> budgets = budgetObjects.entrySet().stream()
                    .filter(e -> e.getValue().getBudget_amount() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getBudget_amount()));

            System.out.println();
            System.out.printf("Users within %.0f%% of their monthly quota (usage >= %.0f%%):%n",
                    withinPercent, threshold);
            System.out.println();
            CopilotReportUtils.printTable(approaching, userInfos, budgets);

            if (approaching.isEmpty()) return 0;

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String choice;
            if (budgetAction != null) {
                choice = String.valueOf(budgetAction);
            } else {
                System.out.println();
                System.out.println("Budget actions:");
                System.out.println("  1) Create budgets for users in this list without a budget");
                System.out.println("  2) Create or update budgets for all users in this list");
                System.out.println("  (press Enter to skip)");
                System.out.print("Choice: ");
                System.out.flush();
                choice = reader.readLine();
            }
            if (choice == null || choice.isBlank()) return 0;
            choice = choice.trim();
            if (!choice.equals("1") && !choice.equals("2")) return 0;

            int resolvedAmount;
            if (budgetAmount != null) {
                resolvedAmount = budgetAmount;
            } else {
                System.out.print("Budget amount in dollars: ");
                System.out.flush();
                String amountStr = reader.readLine();
                if (amountStr == null || amountStr.isBlank()) {
                    System.err.println("No amount provided.");
                    return 1;
                }
                try {
                    resolvedAmount = Integer.parseInt(amountStr.trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid amount: " + amountStr);
                    return 1;
                }
            }

            AppConfig config = AppConfig.load();
            String enterprise = config.getEnterprise();
            if (enterprise == null || enterprise.isBlank()) {
                System.err.println("Error: github.enterprise not configured.");
                return 1;
            }

            GitHubClient client = new GitHubClient(config);
            String myLogin = client.get("/user", User.class).getLogin();

            System.out.println();
            boolean createOnly = choice.equals("1");

            List<UserUsage> processed = new ArrayList<>();

            for (UserUsage u : approaching) {
                Budget existing = budgetObjects.get(u.userId);
                boolean hasBudget = existing != null;

                if (createOnly && hasBudget) continue;

                String body = buildBudgetBody(resolvedAmount, u.userId, myLogin);

                if (!hasBudget) {
                    System.out.printf("Creating budget of $%d for %s...%n", resolvedAmount, u.userId);
                    String path = "/enterprises/" + enterprise + "/settings/billing/budgets";
                    BudgetOperationResponse resp = client.post(path, body, BudgetOperationResponse.class);
                    System.out.println("  -> " + resp.getMessage());
                    processed.add(u);
                } else {
                    Integer existingAmount = existing.getBudget_amount();
                    if (existingAmount != null && resolvedAmount < existingAmount) {
                        if (yes) {
                            System.out.printf("  Warning: new budget $%d is less than existing budget $%d for %s. Auto-confirmed (--yes).%n",
                                    resolvedAmount, existingAmount, u.userId);
                        } else {
                            System.out.printf("  Warning: new budget $%d is less than existing budget $%d for %s. Confirm? (y/N): ",
                                    resolvedAmount, existingAmount, u.userId);
                            System.out.flush();
                            String confirm = reader.readLine();
                            if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
                                System.out.printf("  Skipped %s.%n", u.userId);
                                continue;
                            }
                        }
                    }
                    System.out.printf("Updating budget of $%d for %s (ID: %s)...%n", resolvedAmount, u.userId, existing.getId());
                    String path = "/enterprises/" + enterprise + "/settings/billing/budgets/" + existing.getId();
                    BudgetOperationResponse resp = client.patch(path, body, BudgetOperationResponse.class);
                    System.out.println("  -> " + resp.getMessage());
                    processed.add(u);
                }
            }

            if (!processed.isEmpty()) {
                writeBudgetUpdateSummary(processed, userInfos, resolvedAmount);
            }

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private void writeBudgetUpdateSummary(List<UserUsage> processed, Map<String, UserInfo> userInfos, int budgetAmount) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String fileTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        List<String> emails = processed.stream()
                .map(u -> {
                    UserInfo info = userInfos.get(u.userId);
                    return (info != null && info.email() != null && !info.email().isBlank())
                            ? info.email() : null;
                })
                .filter(e -> e != null)
                .distinct()
                .collect(Collectors.toList());

        String emailList = String.join(":", emails);

        StringBuilder summary = new StringBuilder();
        summary.append("Subject: GitHub Copilot Budget Update — ").append(timestamp).append("\n\n");
        summary.append("The following ").append(processed.size())
               .append(" user budget(s) were created/updated to $").append(budgetAmount).append(":\n\n");

        for (UserUsage u : processed) {
            UserInfo info = userInfos.get(u.userId);
            String name = (info != null && info.name() != null) ? info.name() : "";
            String email = (info != null && info.email() != null) ? info.email() : "(no email)";
            summary.append(String.format("  %-20s  %-30s  %s%n", u.userId, name, email));
        }

        summary.append("\nEmail list (colon-separated):\n").append(emailList).append("\n");

        System.out.println();
        System.out.println("=== Budget Update Summary ===");
        System.out.println("Email list: " + emailList);
        System.out.println(summary);

        try {
            Path reportsDir = CopilotReportUtils.REPORTS_DIR;
            Files.createDirectories(reportsDir);
            Path summaryFile = reportsDir.resolve("budget-update-" + fileTimestamp + ".txt");
            Files.writeString(summaryFile, summary.toString());
            Path absolutePath = summaryFile.toAbsolutePath();
            System.out.println("Summary written to: " + absolutePath);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(absolutePath.toFile());
            } else {
                String os = System.getProperty("os.name", "").toLowerCase();
                ProcessBuilder pb;
                if (os.contains("win")) {
                    pb = new ProcessBuilder("notepad.exe", absolutePath.toString());
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", absolutePath.toString());
                } else {
                    pb = new ProcessBuilder("xdg-open", absolutePath.toString());
                }
                pb.start();
            }
        } catch (Exception e) {
            System.err.println("Warning: could not write or open summary file: " + e.getMessage());
        }
    }

    private static String buildBudgetBody(int budgetAmount, String targetUser, String alertRecipient) {
        return String.format(
                "{\"budget_amount\":%d,\"user\":\"%s\",\"prevent_further_usage\":true," +
                "\"budget_scope\":\"user\",\"budget_entity_name\":\"%s\"," +
                "\"budget_type\":\"BundlePricing\",\"budget_product_sku\":\"ai_credits\"," +
                "\"budget_alerting\":{\"will_alert\":true,\"alert_recipients\":[\"%s\"]}}",
                budgetAmount, targetUser, targetUser, alertRecipient);
    }
}
