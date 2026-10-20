package com.roles.usermanagement.modules.announcement.dto;

import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-16/A-05 - Respuesta de anuncio del sistema] - Datos del aviso mostrados en el banner superior - Conecta con: SystemAnnouncement
public record AnnouncementResponse(
        Long id,
        String title,
        String message,
        String type,
        LocalDateTime startsAt,
        LocalDateTime expiresAt,
        Boolean isActive,
        String createdBy,
        LocalDateTime createdAt
) {
}
