package com.roles.usermanagement.modules.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para asignar inicialmente una incidencia a un técnico.
 */
public record TicketAssignRequest(
        @NotBlank(message = "El usuario del técnico es obligatorio")
        @Size(max = 50, message = "El usuario del técnico no puede superar 50 caracteres")
        String technicianUsername
) {
}
