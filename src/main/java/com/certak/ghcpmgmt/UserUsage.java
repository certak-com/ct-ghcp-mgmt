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

    double usagePercent() {
        return quota > 0 ? (totalQuantity / quota) * 100.0 : 0.0;
    }
}
