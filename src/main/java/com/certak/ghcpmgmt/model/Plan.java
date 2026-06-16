package com.certak.ghcpmgmt.model;

/**
 * GitHub User Plan (nested object within User).
 *
 * API Reference: https://docs.github.com/en/rest/users/users?apiVersion=2026-03-10#get-the-authenticated-user
 *
 * @see <a href="https://docs.github.com/en/rest/users/users?apiVersion=2026-03-10#get-the-authenticated-user">GET /user - GitHub REST API Docs</a>
 */
public class Plan {

    private Integer collaborators;
    private String name;
    private Integer space;
    private Integer private_repos;

    public Integer getCollaborators() { return collaborators; }
    public String getName() { return name; }
    public Integer getSpace() { return space; }
    public Integer getPrivate_repos() { return private_repos; }
}
