package com.roles.usermanagement.modules.assignment.controller;

import com.roles.usermanagement.modules.assignment.dto.TicketAssignRequest;
import com.roles.usermanagement.modules.assignment.dto.TicketAssignmentResponse;
import com.roles.usermanagement.modules.assignment.dto.TicketReassignRequest;
import com.roles.usermanagement.modules.assignment.service.TicketAssignmentService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative endpoints for assigning and reassigning technicians.
 */
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Assignments", description = "Gestión de asignaciones y reasignaciones de técnicos a incidencias")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TicketAssignmentController {

    private final TicketAssignmentService assignmentService;

    public TicketAssignmentController(TicketAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Assigns a technician to an open ticket for the first time.
     */
    @org.springframework.web.bind.annotation.RequestMapping(
            value = "/{ticketId}/assign",
            method = {org.springframework.web.bind.annotation.RequestMethod.PUT, org.springframework.web.bind.annotation.RequestMethod.PATCH}
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Asignación inicial manual de técnico a ticket (ADMIN)")
    public ResponseEntity<TicketAssignmentResponse> assign(
            @PathVariable @Positive Long ticketId,
            @Valid @RequestBody TicketAssignRequest request,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(assignmentService.assign(ticketId, request.technicianUsername(), username));
    }

    /**
     * Reassigns a ticket to another technician and includes the administrative justification.
     */
    @PutMapping("/{ticketId}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reasignación administrativa de técnico con justificación (ADMIN)")
    public ResponseEntity<TicketAssignmentResponse> reassign(
            @PathVariable @Positive Long ticketId,
            @Valid @RequestBody TicketReassignRequest request,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(assignmentService.reassign(
                ticketId, request.technicianUsername(), request.justification(), username));
    }

    // [Nueva Funcionalidad: T-01 - Endpoint de auto-asignación por técnico] - Permite al técnico tomar un ticket abierto - Conecta con: TicketAssignmentService
    @org.springframework.web.bind.annotation.PostMapping("/{ticketId}/self-assign")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "Auto-asignación de ticket desde la cola por el técnico (T-01)")
    public ResponseEntity<TicketAssignmentResponse> selfAssign(
            @PathVariable @Positive Long ticketId,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(assignmentService.selfAssign(ticketId, username));
    }

    // [Nueva Funcionalidad: T-04/A-16 - Endpoint de auto-asignación por carga activa] - Asigna ticket automáticamente al técnico con menor carga - Conecta con: TicketAssignmentService
    @org.springframework.web.bind.annotation.PostMapping("/{ticketId}/auto-assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Operation(summary = "Asignación automática basada en menor carga de trabajo del técnico (A-16)")
    public ResponseEntity<TicketAssignmentResponse> autoAssign(@PathVariable @Positive Long ticketId) {
        return ResponseEntity.ok(assignmentService.autoAssign(ticketId));
    }

    // [Nueva Funcionalidad: A-26 - Endpoint de reasignación masiva] - Reasigna todos los tickets activos de un técnico a otro - Conecta con: TicketAssignmentService
    @org.springframework.web.bind.annotation.PostMapping("/bulk-reassign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reasignación masiva de incidencias activas entre técnicos (A-26)")
    public ResponseEntity<java.util.Map<String, Object>> bulkReassign(
            @org.springframework.web.bind.annotation.RequestParam String fromTechnician,
            @org.springframework.web.bind.annotation.RequestParam String toTechnician,
            @AuthenticationPrincipal String adminUsername) {
        int reassignedCount = assignmentService.bulkReassign(fromTechnician, toTechnician, adminUsername);
        return ResponseEntity.ok(java.util.Map.of("reassignedCount", reassignedCount, "success", true));
    }
}
