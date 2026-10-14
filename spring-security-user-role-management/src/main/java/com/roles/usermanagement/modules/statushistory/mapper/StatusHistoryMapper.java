package com.roles.usermanagement.modules.statushistory.mapper;

import com.roles.usermanagement.modules.statushistory.dto.StatusHistoryResponse;
import com.roles.usermanagement.modules.statushistory.entity.StatusHistory;
import org.springframework.stereotype.Component;

/**
 * Converts StatusHistory entities into response DTOs.
 * Must be used within a transaction, as it reads LAZY relations (ticket and changedBy).
 */
@Component
public class StatusHistoryMapper {

    /** Converts a history record into its public representation. */
    public StatusHistoryResponse toResponse(StatusHistory history) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getTicket().getId(),             // only ticket id, not entire ticket entity
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedBy().getUsername(),    // only username, never UserEntity
                history.getAction(),
                history.getChangedAt());
    }
}