package com.roles.usermanagement.modules.audit.dto;

import java.time.LocalDateTime;

// [Nueva Funcionalidad: A-18 - Respuesta de log de auditoría] - Expone traza de auditoría de operaciones administrativas - Conecta con: AuditLog
public record AuditLogResponse(
        Long id,
        String actorUsername,
        String action,
        String entityName,
        String entityId,
        String details,
        String ipAddress,
        LocalDateTime timestamp
) {
}
