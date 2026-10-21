package com.roles.usermanagement.modules.cannedresponse.dto;

import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuickResponseRequest(
        @NotBlank(message = "El título de la plantilla es obligatorio")
        @Size(max = 100, message = "El título no puede superar los 100 caracteres")
        String titulo,

        @NotBlank(message = "El contenido de la plantilla es obligatorio")
        String contenido,

        @NotNull(message = "La categoría asociada es obligatoria")
        TicketCategory categoria
) {}
