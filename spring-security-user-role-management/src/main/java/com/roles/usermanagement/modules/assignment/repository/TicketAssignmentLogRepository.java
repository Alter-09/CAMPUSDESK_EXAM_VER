package com.roles.usermanagement.modules.assignment.repository;

import com.roles.usermanagement.modules.assignment.entity.TicketAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence for the ticket assignment log.
 */
public interface TicketAssignmentLogRepository extends JpaRepository<TicketAssignmentLog, Long> {
}
