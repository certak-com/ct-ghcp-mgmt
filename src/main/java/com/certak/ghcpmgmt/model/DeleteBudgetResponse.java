package com.certak.ghcpmgmt.model;

/**
 * Response from the delete budget endpoint.
 *
 * API Reference: https://docs.github.com/en/enterprise-cloud@latest/rest/billing/budgets?apiVersion=2026-03-10#delete-a-budget
 *
 * @see <a href="https://docs.github.com/en/enterprise-cloud@latest/rest/billing/budgets?apiVersion=2026-03-10#delete-a-budget">DELETE /enterprises/{enterprise}/settings/billing/budgets/{budget_id} - GitHub REST API Docs</a>
 */
public class DeleteBudgetResponse {

    private String message;
    private String id;

    public String getMessage() { return message; }
    public String getId() { return id; }
}
