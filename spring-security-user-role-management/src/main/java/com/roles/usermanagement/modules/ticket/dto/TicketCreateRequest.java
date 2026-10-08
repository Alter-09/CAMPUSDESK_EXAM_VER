package com.roles.usermanagement.modules.ticket.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;

public record TicketCreateRequest(
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status
){
}
