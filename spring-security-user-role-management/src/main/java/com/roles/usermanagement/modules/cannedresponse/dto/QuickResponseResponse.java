package com.roles.usermanagement.modules.cannedresponse.dto;

import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import java.time.LocalDateTime;

public record QuickResponseResponse(
        Long id,
        String titulo,
        String contenido,
        TicketCategory categoria,
        String technicianUsername,
        LocalDateTime createdAt
) {}
