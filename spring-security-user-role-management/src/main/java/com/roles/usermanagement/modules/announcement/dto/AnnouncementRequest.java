package com.roles.usermanagement.modules.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: A-05 - Petición de anuncio del sistema] - Datos para programar avisos institucionales - Conecta con: SystemAnnouncement
public record AnnouncementRequest(
        @NotBlank(message = "El título es obligatorio")
        String title,

        @NotBlank(message = "El mensaje es obligatorio")
        String message,

        String type, // INFO, WARNING, DANGER

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime startsAt,

        @NotNull(message = "La fecha de expiración es obligatoria")
        LocalDateTime expiresAt,

        Boolean isActive
) {
}
