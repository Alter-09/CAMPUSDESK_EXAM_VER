package com.roles.usermanagement.modules.technician.mapper;

import com.roles.usermanagement.modules.technician.dto.TechnicianAvailabilityResponse;
import com.roles.usermanagement.modules.technician.dto.TechnicianResponse;
import com.roles.usermanagement.modules.technician.entity.TechnicianProfile;
import org.springframework.stereotype.Component;

/**
 * Convierte entidades TechnicianProfile en DTOs de respuesta.
 * Debe usarse dentro de una transacción, porque lee la relación LAZY con el usuario.
 */
@Component
public class TechnicianMapper {

    /** Convierte un perfil en la ficha completa del técnico. */
    public TechnicianResponse toResponse(TechnicianProfile profile) {
        return new TechnicianResponse(
                profile.getUsername(),              // la clave primaria del perfil es el username
                profile.getSpecialty(),
                profile.getAvailabilityStatus(),
                profile.getActiveTicketsCount(),
                profile.getLastAssignedAt());
    }

    /** Convierte un perfil en la versión corta usada para elegir a quién asignar. */
    public TechnicianAvailabilityResponse toAvailabilityResponse(TechnicianProfile profile) {
        return new TechnicianAvailabilityResponse(
                profile.getUsername(),
                profile.getSpecialty(),
                profile.getActiveTicketsCount());
    }
}