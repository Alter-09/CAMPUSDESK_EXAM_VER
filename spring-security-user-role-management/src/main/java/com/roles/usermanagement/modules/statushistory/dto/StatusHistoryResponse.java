package com.roles.usermanagement.modules.statushistory.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.statushistory.enums.StatusHistoryAction;
import java.time.LocalDateTime;

/**
 * Representación pública de un cambio de estado de un ticket.
 * Solo expone datos seguros: el usuario responsable aparece únicamente por su username.
 */
public record StatusHistoryResponse(
        Long id,
        Long ticketId,
        TicketStatus previousStatus,
        TicketStatus newStatus,
        String changedBy,
        StatusHistoryAction action,
        LocalDateTime changedAt
) {
}