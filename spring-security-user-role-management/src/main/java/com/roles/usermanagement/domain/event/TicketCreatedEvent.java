package com.roles.usermanagement.domain.event;

import com.roles.usermanagement.modules.ticket.enums.TicketPriority;

// [Nueva Funcionalidad: U-01/U-10 - Evento de creación de ticket] - Notifica la apertura del ticket para cálculo de SLA y alertas - Conecta con: TicketService y NotificationService
public record TicketCreatedEvent(
        Long ticketId,
        String applicantUsername,
        TicketPriority priority,
        Long serviceCatalogId
) {
}
