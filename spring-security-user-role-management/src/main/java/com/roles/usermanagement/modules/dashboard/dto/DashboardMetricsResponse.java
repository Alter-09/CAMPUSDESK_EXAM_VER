package com.roles.usermanagement.modules.dashboard.dto;

/**
 * Conteos de incidencias visibles para el usuario autenticado.
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
