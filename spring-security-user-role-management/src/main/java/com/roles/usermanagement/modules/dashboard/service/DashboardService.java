package com.roles.usermanagement.modules.dashboard.service;

import com.roles.usermanagement.modules.dashboard.model.DashboardMetrics;
import com.roles.usermanagement.modules.dashboard.repository.DashboardReportRepository;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import com.roles.usermanagement.persistance.entity.UserEntity;
import com.roles.usermanagement.persistance.entity.UserRoleEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Calculates global metrics or metrics limited to the authenticated requester
 * or technician, and returns an internal result independent of the HTTP DTO.
 */
@Service
public class DashboardService {

    private final DashboardReportRepository reports;
    private final UserCrudRepository users;

    public DashboardService(DashboardReportRepository reports, UserCrudRepository users) {
        this.reports = reports;
        this.users = users;
    }

    /**
     * Returns the counts permitted for the requesting user's role and identity.
     */
    @Transactional(readOnly = true)
    public DashboardMetrics getMetricsForUser(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario autenticado");
        }
        UserEntity user = users.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));

        if (hasRole(user, "ADMIN")) {
            return new DashboardMetrics(
                    reports.count(),
                    reports.countByStatus(TicketStatus.OPEN),
                    reports.countByStatus(TicketStatus.ASSIGNED),
                    reports.countByStatus(TicketStatus.IN_PROGRESS),
                    reports.countByStatus(TicketStatus.RESOLVED),
                    reports.countByStatus(TicketStatus.CLOSED)
            );
        }

        if (hasRole(user, "TECHNICIAN")) {
            String technicianUsername = user.getUsername();
            return new DashboardMetrics(
                    reports.countByTechnician_Username(technicianUsername),
                    0,
                    reports.countByTechnician_UsernameAndStatus(technicianUsername, TicketStatus.ASSIGNED),
                    reports.countByTechnician_UsernameAndStatus(technicianUsername, TicketStatus.IN_PROGRESS),
                    reports.countByTechnician_UsernameAndStatus(technicianUsername, TicketStatus.RESOLVED),
                    reports.countByTechnician_UsernameAndStatus(technicianUsername, TicketStatus.CLOSED)
            );
        }

        if (hasRole(user, "USER")) {
            String applicantUsername = user.getUsername();
            return new DashboardMetrics(
                    reports.countByApplicant_Username(applicantUsername),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.OPEN),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.ASSIGNED),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.IN_PROGRESS),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.RESOLVED),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.CLOSED)
            );
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "El rol del usuario no está autorizado para consultar las métricas");
    }

    private boolean hasRole(UserEntity user, String expectedRole) {
        return user.getRoles() != null && user.getRoles().stream()
                .map(UserRoleEntity::getRole)
                .anyMatch(expectedRole::equals);
    }
}
