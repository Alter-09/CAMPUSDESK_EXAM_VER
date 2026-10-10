package com.roles.usermanagement.modules.assignment.repository;

import com.roles.usermanagement.modules.assignment.entity.TicketAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistencia de la bitácora de asignaciones de incidencias.
 */
public interface TicketAssignmentLogRepository extends JpaRepository<TicketAssignmentLog, Long> {
}
