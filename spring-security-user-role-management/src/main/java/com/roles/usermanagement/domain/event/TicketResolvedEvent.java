package com.roles.usermanagement.domain.event;

// [Nueva Funcionalidad: U-06 - Evento de resolución de ticket] - Dispara la invitación a calificar el servicio (CSAT) al solicitante - Conecta con: TicketLifecycleService y SatisfactionService
public record TicketResolvedEvent(
        Long ticketId,
        String applicantUsername,
        String technicianUsername
) {
}
