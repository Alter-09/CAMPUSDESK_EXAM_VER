package com.roles.usermanagement.modules.assignment.dto;

import com.roles.usermanagement.modules.assignment.enums.AssignmentType;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import java.time.LocalDateTime;

/**
 * Resultado de una asignación o reasignación, incluyendo el estado vigente.
 */
public record TicketAssignmentResponse(
        Long id,
        Long ticketId,
        String previousTechnicianUsername,
        String newTechnicianUsername,
        String assignedByUsername,
        AssignmentType assignmentType,
        String justification,
        LocalDateTime assignedAt,
        TicketStatus status
) {
}
