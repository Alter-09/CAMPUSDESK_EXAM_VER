package com.roles.usermanagement.modules.ticket.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        String applicantUsername,
        String technicianUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}