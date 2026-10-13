package com.roles.usermanagement.modules.assignment.controller;

import com.roles.usermanagement.modules.assignment.dto.TicketAssignRequest;
import com.roles.usermanagement.modules.assignment.dto.TicketAssignmentResponse;
import com.roles.usermanagement.modules.assignment.dto.TicketReassignRequest;
import com.roles.usermanagement.modules.assignment.service.TicketAssignmentService;
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
 * Endpoints administrativos para asignar y reasignar técnicos.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketAssignmentController {

    private final TicketAssignmentService assignmentService;

    public TicketAssignmentController(TicketAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Asigna por primera vez un técnico a una incidencia abierta.
     */
    @org.springframework.web.bind.annotation.RequestMapping(
            value = "/{ticketId}/assign",
            method = {org.springframework.web.bind.annotation.RequestMethod.PUT, org.springframework.web.bind.annotation.RequestMethod.PATCH}
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketAssignmentResponse> assign(
            @PathVariable @Positive Long ticketId,
            @Valid @RequestBody TicketAssignRequest request,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(assignmentService.assign(ticketId, request.technicianUsername(), username));
    }

    /**
     * Reasigna una incidencia a otro técnico e incluye la justificación administrativa.
     */
    @PutMapping("/{ticketId}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketAssignmentResponse> reassign(
            @PathVariable @Positive Long ticketId,
            @Valid @RequestBody TicketReassignRequest request,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(assignmentService.reassign(
                ticketId, request.technicianUsername(), request.justification(), username));
    }
}
