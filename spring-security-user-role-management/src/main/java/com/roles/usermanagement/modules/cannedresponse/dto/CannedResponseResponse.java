package com.roles.usermanagement.modules.cannedresponse.dto;

import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-04/A-19 - Respuesta de plantilla rápida] - Expone respuestas predefinidas para técnicos - Conecta con: CannedResponse
public record CannedResponseResponse(
        Long id,
        String title,
        String content,
        String category,
        Boolean isGlobal,
        String createdByUsername,
        LocalDateTime createdAt
) {
}
