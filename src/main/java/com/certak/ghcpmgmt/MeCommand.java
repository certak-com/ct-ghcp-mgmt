package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubApiException;
import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.config.AppConfig;
import com.certak.ghcpmgmt.model.Plan;
import com.certak.ghcpmgmt.model.User;
import picocli.CommandLine;
import java.util.concurrent.Callable;

/**
 * GET /user — Get the authenticated user.
 *
 * API Reference: https://docs.github.com/en/rest/users/users?apiVersion=2026-03-10#get-the-authenticated-user
 */
@CommandLine.Command(name = "me",
        description = "Get the authenticated user (GET /user)",
        mixinStandardHelpOptions = true)
public class MeCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            AppConfig config = AppConfig.load();
            GitHubClient client = new GitHubClient(config);
            User user = client.get("/user", User.class);
            printUser(user);
            return 0;
        } catch (GitHubApiException e) {
            System.err.println("Error: " + e.getMessage());
            if (e.getStatusCode() > 0) {
                System.err.println("Status: " + e.getStatusCode());
            }
            if (e.getResponseBody() != null) {
                System.err.println("Response: " + e.getResponseBody());
            }
            return 1;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private void printUser(User u) {
        System.out.println("GitHub User");
        System.out.println("=".repeat(52));

        field("Login", u.getLogin());
        field("Name", u.getName());
        field("ID", u.getId());
        field("Type", u.getType());
        field("Avatar URL", u.getAvatar_url());
        field("HTML URL", u.getHtml_url());
        field("Location", u.getLocation());
        field("Company", u.getCompany());
        field("Blog", u.getBlog());
        field("Email", u.getEmail());
        field("Bio", u.getBio());
        field("Twitter", u.getTwitter_username());
        field("Public Repos", u.getPublic_repos());
        field("Public Gists", u.getPublic_gists());
        field("Followers", u.getFollowers());
        field("Following", u.getFollowing());
        field("Created", u.getCreated_at());
        field("Updated", u.getUpdated_at());
        field("Site Admin", u.getSite_admin());
        field("2FA", u.getTwo_factor_authentication());

        Plan plan = u.getPlan();
        if (plan != null) {
            System.out.println();
            System.out.println("  Plan:");
            field("    Name", plan.getName());
            field("    Collaborators", plan.getCollaborators());
            field("    Private Repos", plan.getPrivate_repos());
            field("    Space (MB)", plan.getSpace());
        }
    }

    private void field(String key, String value) {
        if (value != null) {
            System.out.printf("  %-16s %s%n", key + ":", value);
        }
    }

    private void field(String key, Long value) {
        if (value != null) {
            System.out.printf("  %-16s %d%n", key + ":", value);
        }
    }

    private void field(String key, Number value) {
        if (value != null) {
            System.out.printf("  %-16s %d%n", key + ":", value);
        }
    }

    private void field(String key, Boolean value) {
        if (value != null) {
            System.out.printf("  %-16s %b%n", key + ":", value);
        }
    }
}
