package com.roles.usermanagement.modules.technician.mapper;

import com.roles.usermanagement.modules.technician.dto.TechnicianAvailabilityResponse;
import com.roles.usermanagement.modules.technician.dto.TechnicianResponse;
import com.roles.usermanagement.modules.technician.entity.TechnicianProfile;
import org.springframework.stereotype.Component;

/**
 * Converts TechnicianProfile entities into response DTOs.
 * Must be used within a transaction, as it reads the LAZY relation with user.
 */
@Component
public class TechnicianMapper {

    /** Converts profile into full technician response DTO. */
    public TechnicianResponse toResponse(TechnicianProfile profile) {
        return new TechnicianResponse(
                profile.getUsername(),              // profile primary key is username
                profile.getSpecialty(),
                profile.getAvailabilityStatus(),
                profile.getActiveTicketsCount(),
                profile.getLastAssignedAt());
    }

    /** Converts profile into short version used for assignment selection. */
    public TechnicianAvailabilityResponse toAvailabilityResponse(TechnicianProfile profile) {
        return new TechnicianAvailabilityResponse(
                profile.getUsername(),
                profile.getSpecialty(),
                profile.getActiveTicketsCount());
    }
}