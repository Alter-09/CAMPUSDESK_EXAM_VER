package com.roles.usermanagement.modules.notification.dto;

import com.roles.usermanagement.modules.notification.enums.NotificationChannel;
import com.roles.usermanagement.modules.notification.enums.NotificationType;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-10/T-17 - Respuesta de notificación] - Expone notificaciones al centro de notificaciones de la UI - Conecta con: Notification
public record NotificationResponse(
        Long id,
        String recipientUsername,
        String title,
        String message,
        NotificationType notificationType,
        NotificationChannel channel,
        Long ticketId,
        Boolean isRead,
        LocalDateTime createdAt
) {
}
