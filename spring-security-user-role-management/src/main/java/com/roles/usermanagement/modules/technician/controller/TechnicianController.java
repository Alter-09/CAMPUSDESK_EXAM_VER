package com.roles.usermanagement.modules.technician.controller;

import com.roles.usermanagement.modules.technician.dto.TechnicianAvailabilityResponse;
import com.roles.usermanagement.modules.technician.dto.TechnicianResponse;
import com.roles.usermanagement.modules.technician.service.TechnicianService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Technician inquiry endpoints. Only administrators can access them.
 */
@RestController
@RequestMapping({ "/api/technicians", "/api/users/technicians" })
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Technicians", description = "Consulta de técnicos y su disponibilidad")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TechnicianController {

    private final TechnicianService technicianService;

    // Spring injects service via constructor
    public TechnicianController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    /** Lists all technicians, regardless of availability. */
    @GetMapping
    @Operation(summary = "List all technicians")
    public List<TechnicianResponse> listAll() {
        return technicianService.listAll();
    }

    /** Lists available technicians, ordered from lowest to highest workload. */
    @GetMapping("/available")
    @Operation(summary = "List available technicians, least loaded first")
    public List<TechnicianAvailabilityResponse> listAvailable() {
        return technicianService.listAvailable();
    }
}