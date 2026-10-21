package com.roles.usermanagement.modules.ticket.controller;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.dto.ReaperturaRequest;
import com.roles.usermanagement.modules.ticket.dto.SubtaskRequest;
import com.roles.usermanagement.modules.ticket.dto.SubtaskResponse;
import com.roles.usermanagement.modules.ticket.dto.TicketCreateRequest;
import com.roles.usermanagement.modules.ticket.dto.TicketDiagnosisRequest;
import com.roles.usermanagement.modules.ticket.dto.TicketResponse;
import com.roles.usermanagement.modules.ticket.dto.TicketUpdateRequest;
import com.roles.usermanagement.modules.ticket.dto.TransferirTicketRequest;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import com.roles.usermanagement.modules.ticket.service.TicketService;
import com.roles.usermanagement.modules.ticket.service.TicketSubtaskService;
import com.roles.usermanagement.modules.timetracking.dto.TiempoTotalResponse;
import com.roles.usermanagement.modules.timetracking.dto.WorklogRequest;
import com.roles.usermanagement.modules.timetracking.dto.WorklogResponse;
import com.roles.usermanagement.modules.timetracking.service.TicketWorklogService;
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
    private final TicketWorklogService ticketWorklogService;
    private final TicketSubtaskService ticketSubtaskService;

    // Spring injects service via constructor
    public TicketController(TicketService ticketService,
                            TicketWorklogService ticketWorklogService,
                            TicketSubtaskService ticketSubtaskService) {
        this.ticketService = ticketService;
        this.ticketWorklogService = ticketWorklogService;
        this.ticketSubtaskService = ticketSubtaskService;
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

    // Permiso exclusivo para SuperAdmin: eliminar incidencias de forma definitiva
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKET_DELETE') or hasRole('SUPERADMIN')")
    @Operation(summary = "Eliminar incidencia definitivamente (Exclusivo Superadmin)")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    // [Ejercicio 1 Examen: Endpoint para asociar etiquetas a un ticket]
    @PostMapping("/{id}/tags")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'TECHNICIAN')")
    @Operation(summary = "Asociar etiquetas a una incidencia (ADMIN o Técnico asignado)")
    public ResponseEntity<TicketResponse> asociarEtiquetas(
            @PathVariable Long id,
            @RequestBody List<String> tags,
            Authentication auth) {
        return ResponseEntity.ok(ticketService.asociarEtiquetas(id, tags, auth));
    }

    // [Ejercicio 1 Examen: Endpoint para filtrar tickets por etiqueta con visibilidad por rol]
    @GetMapping("/por-tag")
    @Operation(summary = "Filtrar tickets por etiqueta considerando rol del usuario autenticado")
    public ResponseEntity<List<TicketResponse>> getTicketsPorTag(
            @RequestParam String nombre,
            Authentication auth) {
        return ResponseEntity.ok(ticketService.getTicketsPorTag(nombre, auth));
    }

    // [Ejercicio 2 Examen: Endpoint para reabrir ticket resuelto dentro de las 48 horas]
    @PostMapping("/{id}/reabrir")
    @Operation(summary = "Reabrir ticket resuelto dentro de 48 horas posteriores (Solo solicitante original)")
    public ResponseEntity<TicketResponse> reabrirTicket(
            @PathVariable Long id,
            @Valid @RequestBody ReaperturaRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ticketService.reabrirTicketExamen(id, request, auth));
    }

    // [Ejercicio 2 Examen: Endpoint para listar tickets reabiertos para administradores]
    @GetMapping("/reabiertos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "Listar tickets que hayan sido reabiertos al menos una vez (Exclusivo ADMIN)")
    public ResponseEntity<List<TicketResponse>> getTicketsReabiertos() {
        return ResponseEntity.ok(ticketService.getTicketsReabiertos());
    }

    // [Ejercicio 3 Examen: Endpoint para transferir ticket de categoría]
    @PutMapping("/{id}/transferir")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'TECHNICIAN')")
    @Operation(summary = "Transferir ticket a nueva categoría, desasignando técnico y reseteando estado a OPEN")
    public ResponseEntity<TicketResponse> transferirTicket(
            @PathVariable Long id,
            @Valid @RequestBody TransferirTicketRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ticketService.transferirTicket(id, request, auth));
    }

    // [Ejercicio 4 Examen: Endpoint para registrar bloques de trabajo/tiempo]
    @PostMapping("/{id}/worklogs")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN', 'SUPERADMIN')")
    @Operation(summary = "Registrar bloque de tiempo dedicado a ticket EN_PROCESO (Técnico asignado)")
    public ResponseEntity<WorklogResponse> registrarWorklog(
            @PathVariable Long id,
            @Valid @RequestBody WorklogRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketWorklogService.registerWorklog(id, request, auth));
    }

    // [Ejercicio 4 Examen: Endpoint para consultar tiempo total dedicado y desglose]
    @GetMapping("/{id}/tiempo-total")
    @Operation(summary = "Consultar tiempo total acumulado y desglose de horas trabajadas en un ticket")
    public ResponseEntity<TiempoTotalResponse> getTiempoTotal(@PathVariable Long id) {
        return ResponseEntity.ok(ticketWorklogService.getTiempoTotal(id));
    }

    // [Nueva Funcionalidad: A-25 - Ver cola global de tickets sin asignar]
    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'TECHNICIAN')")
    @Operation(summary = "Ver cola global de tickets OPEN sin asignar (A-25)")
    public ResponseEntity<com.roles.usermanagement.domain.dto.PageResponse<TicketResponse>> getUnassignedTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ticketService.getUnassignedTickets(page, size));
    }

    // [Nueva Funcionalidad: T-24 - Registrar diagnóstico técnico y causa raíz]
    @PutMapping("/{id}/diagnosis")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Registrar diagnóstico técnico y causa raíz (T-24)")
    public ResponseEntity<TicketResponse> registerDiagnosis(
            @PathVariable Long id,
            @Valid @RequestBody TicketDiagnosisRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ticketService.registerDiagnosis(id, request, auth));
    }

    // [Nueva Funcionalidad: T-06/T-30 - Crear subtarea / checklist dentro de un ticket]
    @PostMapping("/{id}/subtasks")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Crear subtarea o paso de verificación dentro del ticket (T-06, T-30)")
    public ResponseEntity<SubtaskResponse> createSubtask(
            @PathVariable Long id,
            @Valid @RequestBody SubtaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketSubtaskService.createSubtask(id, request));
    }

    // [Nueva Funcionalidad: T-06/T-30 - Listar subtareas de un ticket]
    @GetMapping("/{id}/subtasks")
    @Operation(summary = "Listar subtareas o checklist de un ticket (T-06, T-30)")
    public ResponseEntity<List<SubtaskResponse>> getSubtasks(@PathVariable Long id) {
        return ResponseEntity.ok(ticketSubtaskService.getSubtasksByTicket(id));
    }

    // [Nueva Funcionalidad: T-06/T-30 - Marcar o desmarcar subtarea como completada]
    @PatchMapping("/subtasks/{subtaskId}/toggle")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Alternar estado completado de una subtarea (T-06, T-30)")
    public ResponseEntity<SubtaskResponse> toggleSubtask(@PathVariable Long subtaskId) {
        return ResponseEntity.ok(ticketSubtaskService.toggleCompleted(subtaskId));
    }
}