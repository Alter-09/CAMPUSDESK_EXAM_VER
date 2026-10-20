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
 * REST endpoints for managing tickets.
 * Only handles HTTP requests and responses; business rules reside in TicketService.
 */
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Gestión de incidencias tecnológicas")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TicketController {

    private final TicketService ticketService;

    // Spring injects service via constructor
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** Creates a ticket. USER role only; initial status is OPEN. */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create ticket")
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest request,
                                                 Authentication auth) {
        // 201 Created: new resource created
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request, auth));
    }

    /** Lists tickets visible to the user, with optional filters. */
    @GetMapping
    @Operation(summary = "List visible tickets with optional filters")
    public List<TicketResponse> list(@RequestParam(required = false) TicketStatus status,
                                     @RequestParam(required = false) TicketPriority priority,
                                     @RequestParam(required = false) TicketCategory category,
                                     Authentication auth) {
        return ticketService.list(status, priority, category, auth);
    }

    /** Returns ticket details if user has permission to view it. */
    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by id")
    public TicketResponse getById(@PathVariable Long id, Authentication auth) {
        return ticketService.getById(id, auth);
    }

    /** Updates a ticket. Requester only while OPEN and unassigned. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update ticket")
    public TicketResponse update(@PathVariable Long id,
                                 @Valid @RequestBody TicketUpdateRequest request,
                                 Authentication auth) {
        return ticketService.update(id, request, auth);
    }

    // [Nueva Funcionalidad: U-01 - Endpoint de cancelación de ticket propio] - Permite al solicitante cancelar un ticket abierto - Conecta con: TicketService
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel own ticket")
    public ResponseEntity<TicketResponse> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ticketService.cancelTicket(id, auth));
    }

    // [Nueva Funcionalidad: U-05 - Endpoint de reapertura de ticket] - Permite solicitar reapertura de un ticket cerrado - Conecta con: TicketService
    @PostMapping("/{id}/reopen")
    @Operation(summary = "Reopen closed ticket")
    public ResponseEntity<TicketResponse> reopen(@PathVariable Long id,
                                                 @RequestParam(required = false) String reason,
                                                 Authentication auth) {
        return ResponseEntity.ok(ticketService.reopenTicket(id, reason, auth));
    }

    // [Nueva Funcionalidad: U-13 - Endpoint de duplicación de ticket] - Duplica un ticket propio para usarlo de plantilla - Conecta con: TicketService
    @PostMapping("/{id}/clone")
    @Operation(summary = "Clone ticket")
    public ResponseEntity<TicketResponse> clone(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.cloneTicket(id, auth));
    }

    // [Nueva Funcionalidad: U-24 - Endpoint de solicitud de escalación] - Marca un ticket para escalación prioritaria - Conecta con: TicketService
    @PostMapping("/{id}/escalate")
    @Operation(summary = "Request ticket escalation")
    public ResponseEntity<TicketResponse> escalate(@PathVariable Long id,
                                                   @RequestParam(required = false) String reason,
                                                   Authentication auth) {
        return ResponseEntity.ok(ticketService.requestEscalation(id, reason, auth));
    }

    // [Nueva Funcionalidad: T-27 - Endpoint de creación a nombre de otro usuario] - Permite a técnicos registrar tickets en nombre de solicitantes - Conecta con: TicketService
    @PostMapping("/on-behalf")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Create ticket on behalf of a user")
    public ResponseEntity<TicketResponse> createOnBehalf(@Valid @RequestBody TicketCreateRequest request,
                                                         @RequestParam String applicantUsername,
                                                         Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createOnBehalf(request, applicantUsername, auth));
    }

    // [Nueva Funcionalidad: U-03/U-21/T-21 - Endpoint de listado paginado de tickets] - Consulta tickets con paginación y filtros - Conecta con: TicketService
    @GetMapping("/paginated")
    @Operation(summary = "List visible tickets with pagination")
    public ResponseEntity<com.roles.usermanagement.domain.dto.PageResponse<TicketResponse>> listPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            Authentication auth) {
        return ResponseEntity.ok(ticketService.listPaginated(page, size, status, priority, category, auth));
    }
}