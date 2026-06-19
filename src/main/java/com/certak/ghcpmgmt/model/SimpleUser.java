package com.certak.ghcpmgmt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A GitHub user as returned by team membership list endpoints.
 *
 * API Reference: https://docs.github.com/en/rest/teams/members?apiVersion=2026-03-10#list-team-members
 *
 * @see <a href="https://docs.github.com/en/rest/teams/members?apiVersion=2026-03-10#list-team-members">GET /enterprises/{enterprise}/teams/{team_slug}/memberships - GitHub REST API Docs</a>
 */
public class SimpleUser {

    @JsonProperty("login")
    private String login;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    public String getLogin() { return login; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
