package com.roles.usermanagement.modules.technician.service;

import com.roles.usermanagement.modules.technician.dto.TechnicianAvailabilityResponse;
import com.roles.usermanagement.modules.technician.dto.TechnicianResponse;
import com.roles.usermanagement.modules.technician.enums.TechnicianAvailabilityStatus;
import com.roles.usermanagement.modules.technician.mapper.TechnicianMapper;
import com.roles.usermanagement.modules.technician.repository.TechnicianRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta de técnicos y su disponibilidad, para que el administrador decida a quién asignar.
 */
@Service
public class TechnicianService {

    private final TechnicianRepository technicianRepository;  // acceso a los perfiles
    private final TechnicianMapper mapper;                    // entidad -> DTO

    // Spring entrega las dos dependencias por constructor
    public TechnicianService(TechnicianRepository technicianRepository, TechnicianMapper mapper) {
        this.technicianRepository = technicianRepository;
        this.mapper = mapper;
    }

    /** Devuelve todos los técnicos, sin importar su disponibilidad. */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> listAll() {
        return technicianRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** Devuelve solo los técnicos disponibles, del menos al más cargado. */
    @Transactional(readOnly = true)
    public List<TechnicianAvailabilityResponse> listAvailable() {
        return technicianRepository
                .findByAvailabilityStatusOrderByActiveTicketsCountAsc(TechnicianAvailabilityStatus.AVAILABLE)
                .stream()
                .map(mapper::toAvailabilityResponse)
                .toList();
    }
}