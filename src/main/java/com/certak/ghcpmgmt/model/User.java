package com.certak.ghcpmgmt.model;

/**
 * GitHub User model.
 *
 * API Reference: https://docs.github.com/en/rest/users/users?apiVersion=2026-03-10#get-the-authenticated-user
 *
 * This model unifies the "Private User" and "Public User" schemas from the GitHub REST API.
 * All fields use wrapper types for null-safety and fault tolerance.
 *
 * @see <a href="https://docs.github.com/en/rest/users/users?apiVersion=2026-03-10#get-the-authenticated-user">GET /user - GitHub REST API Docs</a>
 */
public class User {

    private String login;
    private Long id;
    private String user_view_type;
    private String node_id;
    private String avatar_url;
    private String gravatar_id;
    private String url;
    private String html_url;
    private String followers_url;
    private String following_url;
    private String gists_url;
    private String starred_url;
    private String subscriptions_url;
    private String organizations_url;
    private String repos_url;
    private String events_url;
    private String received_events_url;
    private String type;
    private Boolean site_admin;
    private String name;
    private String company;
    private String blog;
    private String location;
    private String email;
    private String notification_email;
    private Boolean hireable;
    private String bio;
    private String twitter_username;
    private Integer public_repos;
    private Integer public_gists;
    private Integer followers;
    private Integer following;
    private String created_at;
    private String updated_at;
    private Integer private_gists;
    private Integer total_private_repos;
    private Integer owned_private_repos;
    private Integer disk_usage;
    private Integer collaborators;
    private Boolean two_factor_authentication;
    private Plan plan;
    private Boolean business_plus;
    private String ldap_dn;

    // --- Identity ---
    public String getLogin() { return login; }
    public Long getId() { return id; }
    public String getUser_view_type() { return user_view_type; }
    public String getNode_id() { return node_id; }
    public String getType() { return type; }

    // --- URLs ---
    public String getAvatar_url() { return avatar_url; }
    public String getGravatar_id() { return gravatar_id; }
    public String getUrl() { return url; }
    public String getHtml_url() { return html_url; }
    public String getFollowers_url() { return followers_url; }
    public String getFollowing_url() { return following_url; }
    public String getGists_url() { return gists_url; }
    public String getStarred_url() { return starred_url; }
    public String getSubscriptions_url() { return subscriptions_url; }
    public String getOrganizations_url() { return organizations_url; }
    public String getRepos_url() { return repos_url; }
    public String getEvents_url() { return events_url; }
    public String getReceived_events_url() { return received_events_url; }

    // --- Profile ---
    public Boolean getSite_admin() { return site_admin; }
    public String getName() { return name; }
    public String getCompany() { return company; }
    public String getBlog() { return blog; }
    public String getLocation() { return location; }
    public String getEmail() { return email; }
    public String getNotification_email() { return notification_email; }
    public Boolean getHireable() { return hireable; }
    public String getBio() { return bio; }
    public String getTwitter_username() { return twitter_username; }

    // --- Stats ---
    public Integer getPublic_repos() { return public_repos; }
    public Integer getPublic_gists() { return public_gists; }
    public Integer getFollowers() { return followers; }
    public Integer getFollowing() { return following; }

    // --- Timestamps ---
    public String getCreated_at() { return created_at; }
    public String getUpdated_at() { return updated_at; }

    // --- Additional (Private User) ---
    public Integer getPrivate_gists() { return private_gists; }
    public Integer getTotal_private_repos() { return total_private_repos; }
    public Integer getOwned_private_repos() { return owned_private_repos; }
    public Integer getDisk_usage() { return disk_usage; }
    public Integer getCollaborators() { return collaborators; }
    public Boolean getTwo_factor_authentication() { return two_factor_authentication; }
    public Plan getPlan() { return plan; }
    public Boolean getBusiness_plus() { return business_plus; }
    public String getLdap_dn() { return ldap_dn; }
}
