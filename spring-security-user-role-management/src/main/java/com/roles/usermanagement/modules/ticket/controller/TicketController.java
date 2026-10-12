package com.roles.usermanagement.modules.ticket.controller;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.dto.TicketCreateRequest;
import com.roles.usermanagement.modules.ticket.dto.TicketResponse;
import com.roles.usermanagement.modules.ticket.dto.TicketUpdateRequest;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import com.roles.usermanagement.modules.ticket.service.TicketService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST para gestionar tickets.
 * Solo recibe y responde HTTP; las reglas de negocio viven en TicketService.
 */
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Gestión de incidencias tecnológicas")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TicketController {

    private final TicketService ticketService;

    // Spring inyecta el service por constructor
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** Crea un ticket. Solo el rol USER; nace en estado OPEN. */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create ticket")
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest request,
                                                 Authentication auth) {
        // 201 Created: se creó un recurso nuevo
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request, auth));
    }

    /** Lista los tickets visibles para el usuario, con filtros opcionales. */
    @GetMapping
    @Operation(summary = "List visible tickets with optional filters")
    public List<TicketResponse> list(@RequestParam(required = false) TicketStatus status,
                                     @RequestParam(required = false) TicketPriority priority,
                                     @RequestParam(required = false) TicketCategory category,
                                     Authentication auth) {
        return ticketService.list(status, priority, category, auth);
    }

    /** Devuelve el detalle de un ticket si el usuario tiene permiso para verlo. */
    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by id")
    public TicketResponse getById(@PathVariable Long id, Authentication auth) {
        return ticketService.getById(id, auth);
    }

    /** Modifica un ticket. Solo su solicitante y mientras esté OPEN y sin técnico. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update ticket")
    public TicketResponse update(@PathVariable Long id,
                                 @Valid @RequestBody TicketUpdateRequest request,
                                 Authentication auth) {
        return ticketService.update(id, request, auth);
    }
}