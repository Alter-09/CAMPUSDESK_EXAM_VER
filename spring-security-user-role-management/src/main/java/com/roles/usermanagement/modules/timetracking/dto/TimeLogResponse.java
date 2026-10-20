package com.roles.usermanagement.modules.timetracking.dto;

import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-02/A-15 - Respuesta de registro de tiempo] - Expone tiempos imputados por técnicos en tickets - Conecta con: TicketTimeLog
public record TimeLogResponse(
        Long id,
        Long ticketId,
        String technicianUsername,
        Integer durationMinutes,
        String description,
        LocalDateTime loggedAt
) {
}
