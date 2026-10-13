package com.roles.usermanagement.modules.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data required to initially assign a ticket to a technician.
 */
public record TicketAssignRequest(
        @NotBlank(message = "El usuario del técnico es obligatorio")
        @Size(max = 50, message = "El usuario del técnico no puede superar 50 caracteres")
        String technicianUsername
) {
}
