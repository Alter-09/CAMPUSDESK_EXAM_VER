package com.roles.usermanagement.modules.technician.dto;

import com.roles.usermanagement.modules.technician.enums.TechnicianAvailabilityStatus;
import java.time.LocalDateTime;

/**
 * Ficha completa de un técnico para consulta del administrador.
 * El usuario aparece solo por su username; nunca se expone el UserEntity.
 */
public record TechnicianResponse(
        String username,
        String specialty,
        TechnicianAvailabilityStatus availabilityStatus,
        Integer activeTicketsCount,
        LocalDateTime lastAssignedAt
) {
}