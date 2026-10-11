package com.roles.usermanagement.modules.ticket.mapper;

import com.roles.usermanagement.modules.ticket.dto.TicketResponse;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.stereotype.Component;

@Component // crea instancia de esta clase y la entra a quien la llame (services).
public class TicketMapper {

    public TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getCategory(), t.getPriority(), t.getStatus(),
                t.getApplicant().getUsername(),
                // Si el ticket aún no tiene técnico asignado, devuelve null en lugar de fallar
                t.getTechnician() == null ? null : t.getTechnician().getUsername(),
                t.getCreatedAt(), t.getUpdatedAt()
        );

    }
}
