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
 * Calcula métricas globales o limitadas al solicitante o técnico autenticado,
 * y devuelve un resultado interno independiente del DTO HTTP.
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
     * Devuelve los conteos permitidos para el rol y usuario solicitantes.
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
                    reports.countByStatus(TicketStatus.ABIERTA),
                    reports.countByStatus(TicketStatus.ASIGNADA),
                    reports.countByStatus(TicketStatus.EN_PROCESO),
                    reports.countByStatus(TicketStatus.RESUELTA),
                    reports.countByStatus(TicketStatus.CERRADA)
            );
        }

        if (hasRole(user, "TECHNICIAN")) {
            String technicianUsername = user.getUsername();
            return new DashboardMetrics(
                    reports.countByTechnician_Username(technicianUsername),
                    0,
                    reports.countByTechnician_UsernameAndStatus(technicianUsername, TicketStatus.ASIGNADA),
                    reports.countByTechnician_UsernameAndStatus(technicianUsername, TicketStatus.EN_PROCESO),
                    reports.countByTechnician_UsernameAndStatus(technicianUsername, TicketStatus.RESUELTA),
                    reports.countByTechnician_UsernameAndStatus(technicianUsername, TicketStatus.CERRADA)
            );
        }

        if (hasRole(user, "USER")) {
            String applicantUsername = user.getUsername();
            return new DashboardMetrics(
                    reports.countByApplicant_Username(applicantUsername),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.ABIERTA),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.ASIGNADA),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.EN_PROCESO),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.RESUELTA),
                    reports.countByApplicant_UsernameAndStatus(applicantUsername, TicketStatus.CERRADA)
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
