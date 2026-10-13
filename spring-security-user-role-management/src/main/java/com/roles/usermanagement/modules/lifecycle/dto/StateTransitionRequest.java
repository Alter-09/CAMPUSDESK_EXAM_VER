package com.roles.usermanagement.modules.lifecycle.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;
import jakarta.validation.constraints.NotNull;

/**
 * Solicitud de cambio de estado de un ticket.
 * El cliente indica QUÉ acción quiere ejecutar; el estado destino lo decide la regla del ciclo de vida.
 */
public record StateTransitionRequest(
        TransitionAction action,
        String technicianUsername,
        String status
) {
    public StateTransitionRequest {
        if (action == null && status != null && !status.isBlank()) {
            String s = status.trim().toUpperCase();
            action = switch (s) {
                case "IN_PROGRESS", "EN_PROCESO" -> TransitionAction.START_PROGRESS;
                case "RESOLVED", "RESUELTA" -> TransitionAction.RESOLVE;
                case "CLOSED", "CERRADA" -> TransitionAction.CONFIRM_CLOSE;
                case "ASSIGNED", "ASIGNADA" -> TransitionAction.ASSIGN;
                default -> null;
            };
        }
    }
}