package com.certak.ghcpmgmt.model;

/**
 * Response from create (POST) or update (PATCH) budget operations.
 *
 * API Reference: https://docs.github.com/en/enterprise-cloud@latest/rest/billing/budgets?apiVersion=2026-03-10#create-a-budget
 *
 * @see <a href="https://docs.github.com/en/enterprise-cloud@latest/rest/billing/budgets?apiVersion=2026-03-10#create-a-budget">POST /enterprises/{enterprise}/settings/billing/budgets - GitHub REST API Docs</a>
 */
public class BudgetOperationResponse {

    private String message;
    private Budget budget;

    public String getMessage() { return message; }
    public Budget getBudget() { return budget; }
}
