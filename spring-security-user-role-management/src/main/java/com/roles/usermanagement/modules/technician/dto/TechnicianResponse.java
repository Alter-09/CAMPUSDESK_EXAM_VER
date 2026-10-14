package com.roles.usermanagement.modules.technician.dto;

import com.roles.usermanagement.modules.technician.enums.TechnicianAvailabilityStatus;
import java.time.LocalDateTime;

/**
 * Complete technician profile for administrator inquiry.
 * User is identified only by username; UserEntity is never exposed.
 */
public record TechnicianResponse(
        String username,
        String specialty,
        TechnicianAvailabilityStatus availabilityStatus,
        Integer activeTicketsCount,
        LocalDateTime lastAssignedAt
) {
}