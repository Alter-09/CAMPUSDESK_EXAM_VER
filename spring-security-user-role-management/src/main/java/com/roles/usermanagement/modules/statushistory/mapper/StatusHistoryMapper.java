package com.roles.usermanagement.modules.statushistory.mapper;

import com.roles.usermanagement.modules.statushistory.dto.StatusHistoryResponse;
import com.roles.usermanagement.modules.statushistory.entity.StatusHistory;
import org.springframework.stereotype.Component;

/**
 * Convierte entidades StatusHistory en DTOs de respuesta.
 * Debe usarse dentro de una transacción, porque lee relaciones LAZY (ticket y changedBy).
 */
@Component
public class StatusHistoryMapper {

    /** Convierte una fila del historial en su representación pública. */
    public StatusHistoryResponse toResponse(StatusHistory history) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getTicket().getId(),             // solo el id del ticket, no el ticket entero
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedBy().getUsername(),    // solo el username, nunca el UserEntity
                history.getAction(),
                history.getChangedAt());
    }
}