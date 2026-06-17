package com.certak.ghcpmgmt.model;

/**
 * A single budget entry from the enterprise billing budgets API.
 *
 * API Reference: https://docs.github.com/en/rest/enterprise-admin/billing?apiVersion=2026-03-10#get-enterprise-billing-budgets
 *
 * @see <a href="https://docs.github.com/en/rest/enterprise-admin/billing?apiVersion=2026-03-10#get-enterprise-billing-budgets">GET /enterprises/{enterprise}/settings/billing/budgets - GitHub REST API Docs</a>
 */
public class Budget {

    private String id;
    private String budget_type;
    private Integer budget_amount;
    private Boolean prevent_further_usage;
    private String budget_scope;
    private String budget_entity_name;
    private String user;
    private String budget_product_sku;
    private BudgetAlerting budget_alerting;

    public String getId() { return id; }
    public String getBudget_type() { return budget_type; }
    public Integer getBudget_amount() { return budget_amount; }
    public Boolean getPrevent_further_usage() { return prevent_further_usage; }
    public String getBudget_scope() { return budget_scope; }
    public String getBudget_entity_name() { return budget_entity_name; }
    public String getUser() { return user; }
    public String getBudget_product_sku() { return budget_product_sku; }
    public BudgetAlerting getBudget_alerting() { return budget_alerting; }
}
