package com.roles.usermanagement.modules.technician.dto;

/**
 * Minimal data of an available technician, for assignment selection.
 * Includes only decision factors: identity, specialty, and current workload.
 */
public record TechnicianAvailabilityResponse(
        String username,
        String specialty,
        Integer activeTicketsCount
) {
}