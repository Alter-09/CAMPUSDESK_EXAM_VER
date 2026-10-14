package com.roles.usermanagement.modules.dashboard.mapper;

import com.roles.usermanagement.modules.dashboard.dto.DashboardMetricsResponse;
import com.roles.usermanagement.modules.dashboard.model.DashboardMetrics;
import org.springframework.stereotype.Component;

/**
 * Converts metrics calculated by the use case into the HTTP response.
 */
@Component
public class DashboardMapper {

    public DashboardMetricsResponse toResponse(DashboardMetrics metrics) {
        return new DashboardMetricsResponse(
                metrics.totalTickets(),
                metrics.openTickets(),
                metrics.assignedTickets(),
                metrics.inProgressTickets(),
                metrics.resolvedTickets(),
                metrics.closedTickets()
        );
    }
}
