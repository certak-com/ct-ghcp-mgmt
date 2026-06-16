package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.model.Plan;
import com.certak.ghcpmgmt.model.User;

import java.util.function.Consumer;

/**
 * User-specific utilities (printing, formatting, etc.).
 */
final class UserUtils {

    private UserUtils() {}

    /**
     * Print a User object in a human-friendly format.
     */
    static void printUser(User u) {
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

    private static void field(String key, String value) {
        if (value != null) {
            System.out.printf("  %-16s %s%n", key + ":", value);
        }
    }

    private static void field(String key, Long value) {
        if (value != null) {
            System.out.printf("  %-16s %d%n", key + ":", value);
        }
    }

    private static void field(String key, Number value) {
        if (value != null) {
            System.out.printf("  %-16s %d%n", key + ":", value);
        }
    }

    private static void field(String key, Boolean value) {
        if (value != null) {
            System.out.printf("  %-16s %b%n", key + ":", value);
        }
    }
}
