package com.roles.usermanagement.modules.assignment.mapper;

import com.roles.usermanagement.modules.assignment.dto.TicketAssignmentResponse;
import com.roles.usermanagement.modules.assignment.entity.TicketAssignmentLog;
import org.springframework.stereotype.Component;

@Component // Creates an instance of this class for injection into services.
public class TicketAssignmentMapper {

    public TicketAssignmentResponse toResponse(TicketAssignmentLog talog){
        return new TicketAssignmentResponse(
                talog.getId(),
                talog.getTicket().getId(),
                talog.getPreviousTechnician() != null ? talog.getPreviousTechnician().getUsername() : null,
                talog.getNewTechnician().getUsername(),
                talog.getAssignedBy().getUsername(),
                talog.getAssignmentType(),
                talog.getJustification(),
                talog.getAssignedAt(),
                talog.getTicket().getStatus()
        );
    }

}
