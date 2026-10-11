package com.roles.usermanagement.modules.dashboard.model;

/**
 * Resultado interno del caso de uso dashboard, independiente del formato HTTP.
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
