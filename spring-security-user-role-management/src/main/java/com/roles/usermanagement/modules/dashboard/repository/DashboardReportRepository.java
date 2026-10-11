package com.roles.usermanagement.modules.dashboard.repository;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consultas agregadas para calcular indicadores por estado y propietario.
 */
public interface DashboardReportRepository extends JpaRepository<Ticket, Long> {

    /**
     * Cuenta incidencias asignadas a un técnico.
     */
    long countByTechnician_Username(String username);

    /**
     * Cuenta incidencias asignadas a un técnico en un estado concreto.
     */
    long countByTechnician_UsernameAndStatus(String username, TicketStatus status);

    /**
     * Cuenta incidencias creadas por un solicitante.
     */
    long countByApplicant_Username(String username);

    /**
     * Cuenta incidencias de un solicitante en un estado concreto.
     */
    long countByApplicant_UsernameAndStatus(String username, TicketStatus status);

    /**
     * Cuenta todas las incidencias globales en un estado concreto.
     */
    long countByStatus(TicketStatus status);
}
