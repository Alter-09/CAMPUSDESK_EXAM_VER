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
 * Query technicians and their availability, allowing administrator to assign
 * tickets.
 */
@Service
public class TechnicianService {

    private final TechnicianRepository technicianRepository; // profile data access
    private final TechnicianMapper mapper; // entidad -> DTO

    // Spring supplies both dependencies via constructor
    public TechnicianService(TechnicianRepository technicianRepository, TechnicianMapper mapper) {
        this.technicianRepository = technicianRepository;
        this.mapper = mapper;
    }

    /** Returns all technicians, regardless of availability. */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> listAll() {
        return technicianRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** Returns only available technicians, from least to most loaded. */
    @Transactional(readOnly = true)
    public List<TechnicianAvailabilityResponse> listAvailable() {
        return technicianRepository
                .findByAvailabilityStatusOrderByActiveTicketsCountAsc(TechnicianAvailabilityStatus.AVAILABLE)
                .stream()
                .map(mapper::toAvailabilityResponse)
                .toList();
    }
}