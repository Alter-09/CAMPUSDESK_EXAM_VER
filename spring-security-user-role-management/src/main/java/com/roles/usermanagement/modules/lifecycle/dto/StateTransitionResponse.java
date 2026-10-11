package com.roles.usermanagement.modules.lifecycle.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;

import java.time.LocalDateTime;

/** Resultado de un cambio de estado: qué pasó, de qué estado a cuál y quién lo hizo. */
public record StateTransitionResponse(
        Long ticketId,
        TransitionAction action,
        TicketStatus previousStatus,
        TicketStatus newStatus,
        String technicianUsername,   // técnico asignado tras el cambio (puede ser null)
        String performedBy,          // usuario que ejecutó la acción
        LocalDateTime transitionedAt
) {
}