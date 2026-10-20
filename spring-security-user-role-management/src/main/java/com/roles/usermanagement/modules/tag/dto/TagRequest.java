package com.roles.usermanagement.modules.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// [Nueva Funcionalidad: A-23 - Petición de etiqueta] - Creación de etiqueta descriptiva para clasificación de tickets - Conecta con: TicketTag
public record TagRequest(
        @NotBlank(message = "El nombre de la etiqueta es obligatorio")
        @Size(max = 50, message = "Máximo 50 caracteres")
        String name,

        String color
) {
}
