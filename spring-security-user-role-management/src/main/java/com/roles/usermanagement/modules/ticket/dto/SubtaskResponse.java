package com.roles.usermanagement.modules.ticket.dto;

import java.time.LocalDateTime;

// [Nueva Funcionalidad: T-06 - Respuesta de subtarea de ticket] - Expone subtareas y estado de cumplimiento - Conecta con: TicketSubtask
public record SubtaskResponse(
        Long id,
        Long ticketId,
        String title,
        Boolean isCompleted,
        String assignedTo,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}
