package com.certak.ghcpmgmt;

/**
 * Aggregated Copilot AI credit usage for a single user.
 */
class UserUsage {

    final String userId;
    final double totalQuantity;
    final double quota;

    UserUsage(String userId, double totalQuantity, double quota) {
        this.userId = userId;
        this.totalQuantity = totalQuantity;
        this.quota = quota;
    }

    private double usagePercent() {
        return quota > 0 ? (totalQuantity / quota) * 100.0 : 0.0;
    }

    /**
     * Usage percent against the budget if one exists (budget dollars × 100 = credits), otherwise falls back to quota.
     */
    double usagePercent(Integer budgetAmountDollars) {
        if (budgetAmountDollars != null && budgetAmountDollars > 0) {
            return (totalQuantity / (budgetAmountDollars * 100.0)) * 100.0;
        }
        return usagePercent();
    }
}
