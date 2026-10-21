package com.roles.usermanagement.domain.event;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;

// [Nueva Funcionalidad: U-10/T-18 - Evento de cambio de estado de ticket] - Notifica a las partes interesadas ante transiciones de estado - Conecta con: TicketLifecycleService y NotificationService
public record TicketStatusChangedEvent(
        Long ticketId,
        TicketStatus previousStatus,
        TicketStatus newStatus,
        String applicantUsername,
        String technicianUsername
) {
}
