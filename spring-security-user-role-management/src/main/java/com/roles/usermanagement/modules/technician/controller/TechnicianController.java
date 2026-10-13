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
 * Endpoints de consulta de técnicos. Solo el administrador puede usarlos.
 */
@RestController
@RequestMapping({"/api/technicians", "/api/users/technicians"})
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Technicians", description = "Consulta de técnicos y su disponibilidad")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TechnicianController {

    private final TechnicianService technicianService;

    // Spring inyecta el service por constructor
    public TechnicianController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    /** Lista todos los técnicos, sin importar su disponibilidad. */
    @GetMapping
    @Operation(summary = "List all technicians")
    public List<TechnicianResponse> listAll() {
        return technicianService.listAll();
    }

    /** Lista los técnicos disponibles, del menos al más cargado. */
    @GetMapping("/available")
    @Operation(summary = "List available technicians, least loaded first")
    public List<TechnicianAvailabilityResponse> listAvailable() {
        return technicianService.listAvailable();
    }
}