package com.roles.usermanagement.modules.notification.enums;

// [Nueva Funcionalidad: U-10/T-17/A-24 - Tipos de eventos para notificaciones] - Clasifica el evento que genera la notificación al usuario o técnico - Conecta con: NotificationEntity y NotificationService
public enum NotificationType {
    TICKET_CREATED,
    STATUS_CHANGED,
    ASSIGNED,
    SLA_WARNING,
    COMMENT_ADDED,
    MENTION
}
