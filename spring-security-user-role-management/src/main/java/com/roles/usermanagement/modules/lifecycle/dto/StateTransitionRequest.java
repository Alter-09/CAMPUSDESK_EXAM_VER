package com.roles.usermanagement.modules.lifecycle.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;
import jakarta.validation.constraints.NotNull;

/**
 * Solicitud de cambio de estado de un ticket.
 * El cliente indica QUÉ acción quiere ejecutar; el estado destino lo decide la regla del ciclo de vida.
 */
public record StateTransitionRequest(
        @NotNull(message = "La acción es obligatoria")
        TransitionAction action,

        // Solo es obligatorio para ASSIGN y REASSIGN; el service valida ese caso.
        // En las demás acciones se ignora.
        String technicianUsername
) {
}