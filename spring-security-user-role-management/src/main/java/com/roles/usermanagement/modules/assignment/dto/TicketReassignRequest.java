package com.roles.usermanagement.modules.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para reasignar una incidencia a otro técnico.
 */
public record TicketReassignRequest(
        @NotBlank(message = "El usuario del técnico es obligatorio")
        @Size(max = 50, message = "El usuario del técnico no puede superar 50 caracteres")
        String technicianUsername,
        @NotBlank(message = "La justificación de la reasignación es obligatoria")
        @Size(max = 300, message = "La justificación no puede superar 300 caracteres")
        String justification
) {
}
