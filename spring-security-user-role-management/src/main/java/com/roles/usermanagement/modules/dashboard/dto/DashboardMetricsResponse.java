package com.roles.usermanagement.modules.dashboard.dto;

/**
 * Counts of tickets visible to the authenticated user.
 */
public record DashboardMetricsResponse(
        long totalTickets,
        long openTickets,
        long assignedTickets,
        long inProgressTickets,
        long resolvedTickets,
        long closedTickets
) {
}
