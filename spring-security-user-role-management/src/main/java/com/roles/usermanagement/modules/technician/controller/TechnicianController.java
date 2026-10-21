package com.roles.usermanagement.modules.technician.controller;

import com.roles.usermanagement.modules.technician.dto.TechnicianAvailabilityResponse;
import com.roles.usermanagement.modules.technician.dto.TechnicianResponse;
import com.roles.usermanagement.modules.technician.enums.TechnicianAvailabilityStatus;
import com.roles.usermanagement.modules.technician.service.TechnicianService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Technician inquiry endpoints and availability management.
 */
@RestController
@RequestMapping({ "/api/technicians", "/api/users/technicians" })
@Tag(name = "Technicians", description = "Consulta de técnicos y gestión de disponibilidad operativa")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TechnicianController {

    private final TechnicianService technicianService;

    // Spring injects service via constructor
    public TechnicianController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    /** Lists all technicians, regardless of availability. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all technicians (ADMIN) (MOD-23)")
    public List<TechnicianResponse> listAll() {
        return technicianService.listAll();
    }

    /** Lists available technicians, ordered from lowest to highest workload. */
    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List available technicians, least loaded first (ADMIN) (MOD-24)")
    public List<TechnicianAvailabilityResponse> listAvailable() {
        return technicianService.listAvailable();
    }

    /** Actualiza el estado de disponibilidad del técnico autenticado (T-19). */
    @PatchMapping("/availability")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Actualizar estado de disponibilidad operativa del técnico (MOD-50, T-19)")
    public ResponseEntity<TechnicianResponse> updateAvailability(
            @RequestParam TechnicianAvailabilityStatus status,
            Authentication auth) {
        return ResponseEntity.ok(technicianService.updateAvailability(auth.getName(), status));
    }
}