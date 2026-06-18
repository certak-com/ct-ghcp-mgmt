package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.config.AppConfig;
import com.certak.ghcpmgmt.model.Budget;
import com.certak.ghcpmgmt.model.BudgetsResponse;
import com.certak.ghcpmgmt.model.DeleteBudgetResponse;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Lists all user-scoped budgets for the enterprise, then optionally deletes them.
 *
 * API Reference: https://docs.github.com/en/enterprise-cloud@latest/rest/billing/budgets?apiVersion=2026-03-10#get-all-budgets
 *
 * @see <a href="https://docs.github.com/en/enterprise-cloud@latest/rest/billing/budgets?apiVersion=2026-03-10#get-all-budgets">GET /enterprises/{enterprise}/settings/billing/budgets - GitHub REST API Docs</a>
 */
@CommandLine.Command(
        name = "user-list",
        description = "List all user-scoped budgets (highest to lowest) and optionally delete them",
        mixinStandardHelpOptions = true)
public class BudgetUserListCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            AppConfig config = AppConfig.load();
            String enterprise = config.getEnterprise();
            if (enterprise == null || enterprise.isBlank()) {
                System.err.println("Error: github.enterprise is not configured in .ghcp-mgmt.properties");
                return 1;
            }

            GitHubClient client = new GitHubClient(config);

            List<Budget> budgets = fetchAllUserBudgets(client, enterprise);

            if (budgets.isEmpty()) {
                System.out.println("No user-scoped budgets found.");
                return 0;
            }

            budgets.sort(Comparator.comparingInt((Budget b) ->
                    b.getBudget_amount() != null ? b.getBudget_amount() : 0).reversed());

            List<String> userIds = budgets.stream()
                    .map(Budget::getUser)
                    .filter(u -> u != null && !u.isBlank())
                    .distinct()
                    .collect(Collectors.toList());

            Map<String, UserInfo> nameCache = UserNameCache.resolveNames(userIds, client);

            System.out.println();
            printBudgetTable(budgets, nameCache);

            System.out.println();
            System.out.print("Delete all " + budgets.size() + " user budget(s)? [y/N]: ");
            System.out.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String answer = reader.readLine();
            if (answer == null || !answer.trim().equalsIgnoreCase("y")) {
                System.out.println("No budgets deleted.");
                return 0;
            }

            deleteAllBudgets(client, enterprise, budgets);
            return 0;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private List<Budget> fetchAllUserBudgets(GitHubClient client, String enterprise) throws Exception {
        List<Budget> all = new ArrayList<>();
        int page = 1;
        while (true) {
            String path = "/enterprises/" + enterprise
                    + "/settings/billing/budgets?scope=user&per_page=100&page=" + page;
            BudgetsResponse response = client.get(path, BudgetsResponse.class);
            if (response.getBudgets() != null) {
                all.addAll(response.getBudgets());
            }
            if (!Boolean.TRUE.equals(response.getHas_next_page())) break;
            page++;
        }
        return all;
    }

    private void printBudgetTable(List<Budget> budgets, Map<String, UserInfo> nameCache) {
        System.out.printf("%-5s  %-18s  %-30s  %-35s  %13s  %-20s  %8s  %s%n",
                "#", "Username", "Name", "Email", "Budget ($)", "SKU", "Enforce", "Alert Recipients");
        System.out.println("-".repeat(160));

        int rank = 1;
        for (Budget b : budgets) {
            String username = b.getUser() != null ? b.getUser() : "(no user)";
            UserInfo info = nameCache.get(username);
            String name = (info != null) ? info.name() : "";
            String email = (info != null && info.email() != null) ? info.email() : "";
            String sku = b.getBudget_product_sku() != null ? b.getBudget_product_sku() : "";
            String enforce = Boolean.TRUE.equals(b.getPrevent_further_usage()) ? "Yes" : "No";
            String recipients = "";
            if (b.getBudget_alerting() != null && b.getBudget_alerting().getAlert_recipients() != null) {
                recipients = String.join(", ", b.getBudget_alerting().getAlert_recipients());
            }
            int amount = b.getBudget_amount() != null ? b.getBudget_amount() : 0;
            System.out.printf("%-5d  %-18s  %-30s  %-35s  %13d  %-20s  %8s  %s%n",
                    rank++, username, name, email, amount, sku, enforce, recipients);
        }
    }

    private void deleteAllBudgets(GitHubClient client, String enterprise, List<Budget> budgets) {
        int deleted = 0;
        int failed = 0;
        for (Budget b : budgets) {
            String path = "/enterprises/" + enterprise + "/settings/billing/budgets/" + b.getId();
            try {
                DeleteBudgetResponse resp = client.delete(path, DeleteBudgetResponse.class);
                String msg = resp.getMessage() != null ? resp.getMessage() : "deleted";
                System.out.printf("  Deleted budget %s (%s): %s%n", b.getId(), b.getUser(), msg);
                deleted++;
            } catch (Exception e) {
                System.err.printf("  Failed to delete budget %s (%s): %s%n", b.getId(), b.getUser(), e.getMessage());
                failed++;
            }
            try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
        System.out.printf("%nDone: %d deleted, %d failed.%n", deleted, failed);
    }
}
