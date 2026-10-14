package com.roles.usermanagement.modules.technician.repository;

import com.roles.usermanagement.modules.technician.entity.TechnicianProfile;
import com.roles.usermanagement.modules.technician.enums.TechnicianAvailabilityStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for technician profiles.
 * Primary key is username (String), not a numeric id.
 */
public interface TechnicianRepository extends JpaRepository<TechnicianProfile, String> {

    /**
     * Returns technicians with specified availability,
     * ordered from lowest to highest active ticket count.
     */
    List<TechnicianProfile> findByAvailabilityStatusOrderByActiveTicketsCountAsc(
            TechnicianAvailabilityStatus status);
}