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
 * Administrative endpoints for assigning and reassigning technicians.
 */
@RestController
@RequestMapping("/api/tickets")
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
    public ResponseEntity<TicketAssignmentResponse> reassign(
            @PathVariable @Positive Long ticketId,
            @Valid @RequestBody TicketReassignRequest request,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(assignmentService.reassign(
                ticketId, request.technicianUsername(), request.justification(), username));
    }
}
