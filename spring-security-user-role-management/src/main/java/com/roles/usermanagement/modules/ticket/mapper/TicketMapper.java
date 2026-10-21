package com.roles.usermanagement.modules.ticket.mapper;

import com.roles.usermanagement.modules.ticket.dto.TicketResponse;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.stereotype.Component;

@Component // creates an instance of this class for injection into services.
public class TicketMapper {

    // [Nueva Funcionalidad: U-04/T-18/U-26/U-28 - Mapeo de ticket extendido] - Mapea campos de SLA, catálogo y trazabilidad a la respuesta del ticket - Conecta con: Ticket y TicketResponse
    public TicketResponse toResponse(Ticket t) {
        java.util.Set<String> tagNames = t.getTags() == null ? java.util.Collections.emptySet()
                : t.getTags().stream()
                .map(com.roles.usermanagement.modules.tag.entity.Tag::getNombre)
                .collect(java.util.stream.Collectors.toSet());

        return new TicketResponse(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getCategory(), t.getPriority(), t.getStatus(),
                t.getApplicant().getUsername(),
                // If ticket has no assigned technician yet, returns null instead of failing
                t.getTechnician() == null ? null : t.getTechnician().getUsername(),
                t.getCreatedAt(), t.getUpdatedAt(),
                t.getServiceCatalogId(),
                t.getSlaDueDate(),
                t.getFirstResponseDueDate(),
                t.getFirstResponseAt(),
                t.getResolvedAt(),
                t.getReopenedCount(),
                t.getIsEscalated(),
                t.getUrgentReason(),
                t.getRootCause(),
                t.getResolutionSummary(),
                tagNames
        );
    }
}
