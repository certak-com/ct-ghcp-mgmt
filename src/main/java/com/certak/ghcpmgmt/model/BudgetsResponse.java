package com.certak.ghcpmgmt.model;

import java.util.List;

/**
 * Response from the enterprise billing budgets endpoint.
 *
 * API Reference: https://docs.github.com/en/rest/enterprise-admin/billing?apiVersion=2026-03-10#get-enterprise-billing-budgets
 *
 * @see <a href="https://docs.github.com/en/rest/enterprise-admin/billing?apiVersion=2026-03-10#get-enterprise-billing-budgets">GET /enterprises/{enterprise}/settings/billing/budgets - GitHub REST API Docs</a>
 */
public class BudgetsResponse {

    private List<Budget> budgets;
    private String user;
    private Boolean has_next_page;
    private Integer total_count;

    public List<Budget> getBudgets() { return budgets; }
    public String getUser() { return user; }
    public Boolean getHas_next_page() { return has_next_page; }
    public Integer getTotal_count() { return total_count; }
}
