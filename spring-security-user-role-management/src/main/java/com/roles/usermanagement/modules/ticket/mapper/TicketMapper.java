package com.roles.usermanagement.modules.ticket.mapper;

import com.roles.usermanagement.modules.ticket.dto.TicketResponse;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.stereotype.Component;

@Component // creates an instance of this class for injection into services.
public class TicketMapper {

    public TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getCategory(), t.getPriority(), t.getStatus(),
                t.getApplicant().getUsername(),
                // If ticket has no assigned technician yet, returns null instead of failing
                t.getTechnician() == null ? null : t.getTechnician().getUsername(),
                t.getCreatedAt(), t.getUpdatedAt()
        );

    }
}
