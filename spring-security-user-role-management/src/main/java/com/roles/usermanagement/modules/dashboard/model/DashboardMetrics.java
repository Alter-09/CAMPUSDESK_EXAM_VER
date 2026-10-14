package com.roles.usermanagement.modules.dashboard.model;

/**
 * Internal result of the dashboard use case, independent of the HTTP format.
 */
public record DashboardMetrics(
        long totalTickets,
        long openTickets,
        long assignedTickets,
        long inProgressTickets,
        long resolvedTickets,
        long closedTickets
) {
}
