package com.roles.usermanagement.modules.lifecycle.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;

import java.time.LocalDateTime;

/** Result of a status change: what happened, from which status to which, and who performed it. */
public record StateTransitionResponse(
        Long ticketId,
        TransitionAction action,
        TicketStatus previousStatus,
        TicketStatus newStatus,
        String technicianUsername,   // assigned technician after transition (can be null)
        String performedBy,          // user who performed the action
        LocalDateTime transitionedAt
) {
}