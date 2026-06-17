package com.certak.ghcpmgmt.model;

import java.util.List;

/**
 * Budget alerting configuration embedded in a {@link Budget}.
 *
 * API Reference: https://docs.github.com/en/rest/enterprise-admin/billing?apiVersion=2026-03-10#get-enterprise-billing-budgets
 *
 * @see <a href="https://docs.github.com/en/rest/enterprise-admin/billing?apiVersion=2026-03-10#get-enterprise-billing-budgets">GET /enterprises/{enterprise}/settings/billing/budgets - GitHub REST API Docs</a>
 */
public class BudgetAlerting {

    private Boolean will_alert;
    private List<String> alert_recipients;

    public Boolean getWill_alert() { return will_alert; }
    public List<String> getAlert_recipients() { return alert_recipients; }
}
