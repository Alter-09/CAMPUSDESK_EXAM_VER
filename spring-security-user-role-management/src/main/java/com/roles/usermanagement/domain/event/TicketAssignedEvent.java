package com.roles.usermanagement.domain.event;

// [Nueva Funcionalidad: T-17 - Evento de asignación de ticket] - Dispara alerta al técnico sobre nuevo ticket asignado - Conecta con: TicketAssignmentService y NotificationService
public record TicketAssignedEvent(
        Long ticketId,
        String technicianUsername,
        String applicantUsername
) {
}
