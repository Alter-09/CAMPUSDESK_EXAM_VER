package com.roles.usermanagement.modules.notification.enums;

// [Nueva Funcionalidad: U-10/A-06 - Canales de entrega de notificación] - Define el medio por el cual se emite la notificación (en app o correo) - Conecta con: NotificationEntity y EmailSenderService
public enum NotificationChannel {
    IN_APP,
    EMAIL
}
