package com.roles.usermanagement.modules.cannedresponse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// [Nueva Funcionalidad: T-04/A-19 - Petición de respuesta predefinida] - Datos para crear plantilla de respuesta reutilizable - Conecta con: CannedResponse
public record CannedResponseRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String title,

        @NotBlank(message = "El contenido es obligatorio")
        String content,

        String category,
        Boolean isGlobal
) {
}
