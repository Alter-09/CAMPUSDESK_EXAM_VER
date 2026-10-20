package com.roles.usermanagement.modules.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// [Nueva Funcionalidad: T-06 - Petición de creación de subtarea] - Datos para añadir subtarea a un ticket - Conecta con: TicketSubtask
public record SubtaskRequest(
        @NotBlank(message = "El título de la subtarea es obligatorio")
        @Size(max = 200, message = "Máximo 200 caracteres")
        String title,

        String assignedTo
) {
}
