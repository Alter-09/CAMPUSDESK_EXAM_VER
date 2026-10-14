package com.roles.usermanagement.modules.dashboard.repository;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Aggregated queries for calculating metrics by status and owner.
 */
public interface DashboardReportRepository extends JpaRepository<Ticket, Long> {

    /**
     * Counts tickets assigned to a technician.
     */
    long countByTechnician_Username(String username);

    /**
     * Counts tickets assigned to a technician with a specific status.
     */
    long countByTechnician_UsernameAndStatus(String username, TicketStatus status);

    /**
     * Counts tickets created by a requester.
     */
    long countByApplicant_Username(String username);

    /**
     * Counts a requester's tickets with a specific status.
     */
    long countByApplicant_UsernameAndStatus(String username, TicketStatus status);

    /**
     * Counts all tickets globally with a specific status.
     */
    long countByStatus(TicketStatus status);
}
