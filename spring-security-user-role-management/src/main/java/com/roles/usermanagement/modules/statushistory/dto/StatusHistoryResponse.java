package com.roles.usermanagement.modules.statushistory.dto;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.statushistory.enums.StatusHistoryAction;
import java.time.LocalDateTime;

/**
 * Public representation of a ticket status change.
 * Exposes safe data only: responsible user is identified solely by username.
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