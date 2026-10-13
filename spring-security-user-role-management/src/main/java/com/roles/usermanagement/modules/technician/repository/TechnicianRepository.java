package com.roles.usermanagement.modules.technician.repository;

import com.roles.usermanagement.modules.technician.entity.TechnicianProfile;
import com.roles.usermanagement.modules.technician.enums.TechnicianAvailabilityStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de los perfiles de técnicos.
 * La clave primaria es el username (String), no un número.
 */
public interface TechnicianRepository extends JpaRepository<TechnicianProfile, String> {

    /**
     * Devuelve los técnicos con la disponibilidad indicada,
     * ordenados del que menos tickets activos tiene al que más.
     */
    List<TechnicianProfile> findByAvailabilityStatusOrderByActiveTicketsCountAsc(
            TechnicianAvailabilityStatus status);
}