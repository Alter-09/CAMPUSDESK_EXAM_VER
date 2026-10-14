package com.roles.usermanagement.modules.assignment.dto;

import com.roles.usermanagement.modules.assignment.enums.AssignmentType;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import java.time.LocalDateTime;

/**
 * Result of an assignment or reassignment, including the current status.
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
