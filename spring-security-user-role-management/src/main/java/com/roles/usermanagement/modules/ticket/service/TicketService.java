package com.roles.usermanagement.modules.ticket.service;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.statushistory.enums.StatusHistoryAction;
import com.roles.usermanagement.modules.statushistory.service.StatusHistoryService;
import com.roles.usermanagement.modules.ticket.dto.TicketCreateRequest;
import com.roles.usermanagement.modules.ticket.dto.TicketResponse;
import com.roles.usermanagement.modules.ticket.dto.TicketUpdateRequest;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import com.roles.usermanagement.modules.ticket.mapper.TicketMapper;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import com.roles.usermanagement.modules.ticket.repository.TicketSpecifications;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import com.roles.usermanagement.persistance.entity.UserEntity;
import com.roles.usermanagement.modules.comment.entity.Comment;
import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import com.roles.usermanagement.modules.tag.entity.Tag;
import com.roles.usermanagement.modules.ticket.dto.ReaperturaRequest;
import com.roles.usermanagement.modules.ticket.dto.TicketDiagnosisRequest;
import com.roles.usermanagement.modules.ticket.dto.TransferirTicketRequest;
import com.roles.usermanagement.domain.dto.PageResponse;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

// @Service: marks class as business logic and registers it with Spring
@Service
public class TicketService {

    private final TicketRepository ticketRepository; // talks to tickets table
    private final UserCrudRepository userRepository; // talks to users table
    private final TicketMapper mapper; // convierte Ticket -> TicketResponse
    private final TicketAccessPolicy policy; // rules for who can view what
    private final StatusHistoryService historyService;
    private final com.roles.usermanagement.modules.sla.service.SlaService slaService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final com.roles.usermanagement.modules.tag.repository.TagRepository tagRepository;
    private final com.roles.usermanagement.modules.comment.repository.CommentRepository commentRepository;

    // Constructor: Spring injects required dependencies
    public TicketService(TicketRepository ticketRepository,
            UserCrudRepository userRepository,
            TicketMapper mapper,
            TicketAccessPolicy policy,
            StatusHistoryService historyService,
            com.roles.usermanagement.modules.sla.service.SlaService slaService,
            org.springframework.context.ApplicationEventPublisher eventPublisher,
            com.roles.usermanagement.modules.tag.repository.TagRepository tagRepository,
            com.roles.usermanagement.modules.comment.repository.CommentRepository commentRepository) {
        // Stores each received dependency in its field
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.policy = policy;
        this.historyService = historyService;
        this.slaService = slaService;
        this.eventPublisher = eventPublisher;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
    }

    // ---------- CREATE ----------

    // [Nueva Funcionalidad: U-01/U-04/U-26 - Creación de ticket con SLA y eventos]
    // - Asigna tiempos de SLA y emite evento de creación - Conecta con: SlaService
    // y TicketCreatedEvent
    @Transactional
    public TicketResponse create(TicketCreateRequest request, Authentication auth) {
        UserEntity applicant = userRepository.findById(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Ticket ticket = Ticket.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .category(request.category())
                .priority(request.priority())
                .status(TicketStatus.OPEN)
                .applicant(applicant)
                .serviceCatalogId(request.serviceCatalogId())
                .urgentReason(request.urgentReason())
                .slaDueDate(slaService.calculateResolutionDueDate(request.priority()))
                .firstResponseDueDate(slaService.calculateFirstResponseDueDate(request.priority()))
                .reopenedCount(0)
                .isEscalated(false)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        historyService.record(
                savedTicket,
                null,
                TicketStatus.OPEN,
                applicant,
                StatusHistoryAction.CREATION);

        eventPublisher.publishEvent(new com.roles.usermanagement.domain.event.TicketCreatedEvent(
                savedTicket.getId(),
                applicant.getUsername(),
                savedTicket.getPriority(),
                savedTicket.getServiceCatalogId()));

        return mapper.toResponse(savedTicket);
    }

    // ---------- GET BY ID ----------

    // readOnly = true: read-only; keeps session open to fetch LAZY relations
    @Transactional(readOnly = true)
    public TicketResponse getById(Long id, Authentication auth) {
        // findById returns an Optional (may be empty)
        Ticket ticket = ticketRepository.findById(id)
                // If empty, 404 error; otherwise, unwrap ticket
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        // Security guard: if no permission, throw 403 and abort
        policy.assertCanView(ticket, auth);

        // Reached only if permission check passed
        return mapper.toResponse(ticket);
    }

    // ---------- LIST ----------

    @Transactional(readOnly = true)
    public List<TicketResponse> list(TicketStatus status, // filtros opcionales: pueden venir null
            TicketPriority priority,
            TicketCategory category,
            Authentication auth) {

        // Variable holding PERMISSION predicate (which tickets user can view)
        Specification<Ticket> visibility;
        if (policy.isAdmin(auth)) {
            // Admin: unrestricted, views all tickets
            visibility = Specification.unrestricted();
        } else if (policy.isTechnician(auth)) {
            // Technician: only assigned tickets
            visibility = TicketSpecifications.assignedTo(auth.getName());
        } else {
            // Requester/Other: only tickets created by user (most restrictive default)
            visibility = TicketSpecifications.appliedBy(auth.getName());
        }

        // Combine permission + filters with AND; null filters are ignored
        Specification<Ticket> spec = Specification.allOf(
                visibility,
                TicketSpecifications.hasStatus(status),
                TicketSpecifications.hasPriority(priority),
                TicketSpecifications.hasCategory(category));

        // findAll executes single SQL query with all predicates, sorted from newest to
        // oldest
        return ticketRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream() // stream ticket list
                .map(mapper::toResponse) // converts each Ticket to TicketResponse
                .toList(); // collect results into list
    }

    /**
     * Updates a ticket. Can only be done by its applicant and only
     * while ticket is open (OPEN) and without an assigned technician.
     */
    @Transactional
    public TicketResponse update(Long id, TicketUpdateRequest request, Authentication auth) {
        // Find ticket; if not found, respond 404
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        // Only applicant or administrator can modify ticket; any other user receives
        // 403
        if (!policy.isAdmin(auth) && !policy.isApplicant(ticket, auth)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the applicant or administrator can modify the ticket");
        }

        // If no longer open or already assigned, current state forbids update: 409
        if (ticket.getStatus() != TicketStatus.OPEN || ticket.getTechnician() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Only an open, unassigned ticket can be modified");
        }

        // Copy new request data to entity (trimming excess whitespace)
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());

        // Save and return response DTO
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    // [Nueva Funcionalidad: U-01 - Cancelar ticket propio] - Permite al solicitante
    // cancelar un ticket en estado OPEN - Conecta con: Ticket y
    // StatusHistoryService
    @Transactional
    public TicketResponse cancelTicket(Long id, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!policy.isApplicant(ticket, auth) && !policy.isAdmin(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para cancelar este ticket");
        }
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden cancelar tickets en estado ABIERTO");
        }

        TicketStatus prev = ticket.getStatus();
        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket saved = ticketRepository.save(ticket);

        UserEntity user = userRepository.findById(auth.getName()).orElse(null);
        historyService.record(saved, prev, TicketStatus.CANCELLED, user, StatusHistoryAction.CLOSURE);
        return mapper.toResponse(saved);
    }

    // [Nueva Funcionalidad: U-05 - Reabrir ticket cerrado/resuelto] - Solicita
    // reapertura de un ticket con justificación - Conecta con: Ticket y
    // StatusHistoryService
    @Transactional
    public TicketResponse reopenTicket(Long id, String reason, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!policy.isApplicant(ticket, auth) && !policy.isAdmin(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para reabrir este ticket");
        }
        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo tickets resueltos o cerrados pueden reabrirse");
        }

        TicketStatus prev = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setReopenedCount(ticket.getReopenedCount() + 1);
        Ticket saved = ticketRepository.save(ticket);

        UserEntity user = userRepository.findById(auth.getName()).orElse(null);
        historyService.record(saved, prev, TicketStatus.IN_PROGRESS, user, StatusHistoryAction.STATUS_TRANSITION);
        return mapper.toResponse(saved);
    }

    // [Nueva Funcionalidad: U-13 - Duplicar ticket como plantilla] - Crea un nuevo
    // ticket basándose en los datos de uno existente - Conecta con: Ticket
    @Transactional
    public TicketResponse cloneTicket(Long id, Authentication auth) {
        Ticket original = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        policy.assertCanView(original, auth);

        TicketCreateRequest req = new TicketCreateRequest(
                "Copia de: " + original.getTitle(),
                original.getDescription(),
                original.getCategory(),
                original.getPriority(),
                original.getServiceCatalogId(),
                null);
        return create(req, auth);
    }

    // [Nueva Funcionalidad: U-24 - Solicitar escalación de ticket] - Marca el
    // ticket como escalado para atención prioritaria - Conecta con: Ticket y
    // NotificationService
    @Transactional
    public TicketResponse requestEscalation(Long id, String reason, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        policy.assertCanView(ticket, auth);

        ticket.setIsEscalated(true);
        ticket.setStatus(TicketStatus.ESCALATED);
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    // [Nueva Funcionalidad: T-27 - Crear ticket a nombre de un usuario] - Permite a
    // un técnico registrar un ticket para un solicitante - Conecta con: Ticket
    @Transactional
    public TicketResponse createOnBehalf(TicketCreateRequest request, String applicantUsername, Authentication auth) {
        UserEntity applicant = userRepository.findById(applicantUsername)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario solicitante no encontrado"));

        Ticket ticket = Ticket.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .category(request.category())
                .priority(request.priority())
                .status(TicketStatus.OPEN)
                .applicant(applicant)
                .serviceCatalogId(request.serviceCatalogId())
                .urgentReason(request.urgentReason())
                .slaDueDate(slaService.calculateResolutionDueDate(request.priority()))
                .firstResponseDueDate(slaService.calculateFirstResponseDueDate(request.priority()))
                .reopenedCount(0)
                .isEscalated(false)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        UserEntity creator = userRepository.findById(auth.getName()).orElse(applicant);
        historyService.record(saved, null, TicketStatus.OPEN, creator, StatusHistoryAction.CREATION);
        return mapper.toResponse(saved);
    }

    // [Nueva Funcionalidad: U-03/U-21/T-21 - Listado paginado de tickets] - Retorna
    // página de tickets según permisos de visualización - Conecta con:
    // TicketRepository y PageResponse
    @Transactional(readOnly = true)
    public com.roles.usermanagement.domain.dto.PageResponse<TicketResponse> listPaginated(
            int page, int size, TicketStatus status, TicketPriority priority, TicketCategory category,
            Authentication auth) {

        Specification<Ticket> visibility;
        if (policy.isAdmin(auth)) {
            visibility = Specification.unrestricted();
        } else if (policy.isTechnician(auth)) {
            visibility = TicketSpecifications.assignedTo(auth.getName());
        } else {
            visibility = TicketSpecifications.appliedBy(auth.getName());
        }

        Specification<Ticket> spec = Specification.allOf(
                visibility,
                TicketSpecifications.hasStatus(status),
                TicketSpecifications.hasPriority(priority),
                TicketSpecifications.hasCategory(category));

        org.springframework.data.domain.Page<Ticket> p = ticketRepository.findAll(
                spec,
                org.springframework.data.domain.PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        java.util.List<TicketResponse> content = p.getContent().stream().map(mapper::toResponse).toList();
        return new com.roles.usermanagement.domain.dto.PageResponse<>(
                content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }

    @Transactional
    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incidencia no encontrada"));
        ticketRepository.delete(ticket);
    }

    // [Ejercicio 1 Examen: Asociar etiquetas a un ticket]
    @Transactional
    public TicketResponse asociarEtiquetas(Long ticketId, List<String> tagNames, Authentication auth) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        if (!policy.isAdmin(auth) && !policy.isAssignedTechnician(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un ADMIN o el técnico asignado pueden asociar etiquetas al ticket");
        }

        if (tagNames != null) {
            for (String raw : tagNames) {
                if (raw == null || raw.trim().isEmpty())
                    continue;
                String normalized = raw.trim().toUpperCase();
                Tag tag = tagRepository.findByNombre(normalized)
                        .orElseGet(() -> tagRepository.save(new Tag(normalized)));
                if (!ticket.getTags().contains(tag)) {
                    ticket.getTags().add(tag);
                }
            }
        }
        Ticket saved = ticketRepository.save(ticket);
        return mapper.toResponse(saved);
    }

    // [Ejercicio 1 Examen: Filtrar tickets por etiqueta considerando rol]
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsPorTag(String nombre, Authentication auth) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la etiqueta es obligatorio");
        }
        String normalized = nombre.trim().toUpperCase();
        List<Ticket> tickets;
        if (policy.isAdmin(auth)) {
            tickets = ticketRepository.findByTagNombre(normalized);
        } else if (policy.isTechnician(auth)) {
            tickets = ticketRepository.findByTagNombreAndTechnician(normalized, auth.getName());
        } else {
            tickets = ticketRepository.findByTagNombreAndApplicant(normalized, auth.getName());
        }
        return tickets.stream().map(mapper::toResponse).toList();
    }

    // [Ejercicio 2 Examen: Reapertura de ticket con límite de 48h y conteo]
    @Transactional
    public TicketResponse reabrirTicketExamen(Long id, ReaperturaRequest request, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        // Regla: Solo el solicitante original (applicant) puede solicitar la reapertura
        if (!policy.isApplicant(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el solicitante original puede reabrir el ticket");
        }

        // Regla: Estado debe ser RESUELTO / RESOLVED
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden reabrir tickets en estado RESUELTO/RESOLVED");
        }

        // Regla: Límite de 48 horas posteriores a la resolución
        LocalDateTime refTime = ticket.getResolvedAt() != null ? ticket.getResolvedAt()
                : (ticket.getUpdatedAt() != null ? ticket.getUpdatedAt() : ticket.getCreatedAt());
        long horasTranscurridas = ChronoUnit.HOURS.between(refTime, LocalDateTime.now());
        if (horasTranscurridas > 48) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El plazo máximo de 48 horas para reabrir el ticket ha expirado");
        }

        TicketStatus prevStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setReopenedCount((ticket.getReopenedCount() != null ? ticket.getReopenedCount() : 0) + 1);
        ticket.setResolvedAt(null);

        UserEntity user = userRepository.findById(auth.getName()).orElse(null);

        // Registro de comentario automático con motivo
        Comment autoComment = Comment.builder()
                .ticket(ticket)
                .author(user)
                .content("[REAPERTURA] " + request.motivo().trim())
                .visibility(CommentVisibility.PUBLIC)
                .isInternal(false)
                .build();
        commentRepository.save(autoComment);

        // Trazabilidad en historial de estados
        historyService.record(ticket, prevStatus, TicketStatus.IN_PROGRESS, user,
                StatusHistoryAction.STATUS_TRANSITION);

        Ticket saved = ticketRepository.save(ticket);
        return mapper.toResponse(saved);
    }

    // [Ejercicio 2 Examen: Listar tickets reabiertos para ADMIN]
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsReabiertos() {
        return ticketRepository.findByReopenedCountGreaterThanOrderByUpdatedAtDesc(0)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // [Ejercicio 3 Examen: Transferir ticket de categoría, resetear a OPEN y
    // desasignar técnico]
    @Transactional
    public TicketResponse transferirTicket(Long id, TransferirTicketRequest request, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        // Regla: Solo ADMIN o el técnico asignado pueden transferir
        if (!policy.isAdmin(auth) && !policy.isAssignedTechnician(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un ADMIN o el técnico asignado pueden transferir este ticket");
        }

        // Regla: La nueva categoría no puede ser idéntica a la categoría actual
        if (ticket.getCategory() == request.nuevaCategoria()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva categoría no puede ser idéntica a la categoría actual");
        }

        TicketCategory categoriaAnterior = ticket.getCategory();
        TicketStatus prevStatus = ticket.getStatus();

        // Aplicar cambios requeridos
        ticket.setCategory(request.nuevaCategoria());
        ticket.setTechnician(null);
        ticket.setStatus(TicketStatus.OPEN);

        UserEntity user = userRepository.findById(auth.getName()).orElse(null);

        // Nota interna o comentario automático
        Comment note = Comment.builder()
                .ticket(ticket)
                .author(user)
                .content(String.format("[TRANSFERENCIA] Ticket transferido de %s a %s. Motivo: %s",
                        categoriaAnterior, request.nuevaCategoria(), request.motivoTransferencia().trim()))
                .visibility(CommentVisibility.PUBLIC)
                .isInternal(false)
                .build();
        commentRepository.save(note);

        // Historial
        historyService.record(ticket, prevStatus, TicketStatus.OPEN, user, StatusHistoryAction.STATUS_TRANSITION);

        Ticket saved = ticketRepository.save(ticket);
        return mapper.toResponse(saved);
    }

    // [Nueva Funcionalidad: A-25 - Consulta de cola global de tickets sin asignar]
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> getUnassignedTickets(int page, int size) {
        org.springframework.data.domain.Page<Ticket> p = ticketRepository.findUnassignedTickets(
                org.springframework.data.domain.PageRequest.of(page, size));
        List<TicketResponse> content = p.getContent().stream().map(mapper::toResponse).toList();
        return new PageResponse<>(
                content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }

    // [Nueva Funcionalidad: T-24 - Registro de diagnóstico técnico y causa raíz]
    @Transactional
    public TicketResponse registerDiagnosis(Long id, TicketDiagnosisRequest request, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        if (!policy.isAdmin(auth) && !policy.isAssignedTechnician(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un ADMIN o el técnico asignado pueden registrar el diagnóstico");
        }

        ticket.setRootCause(request.rootCause().trim());
        ticket.setResolutionSummary(request.resolutionSummary().trim());
        Ticket saved = ticketRepository.save(ticket);
        return mapper.toResponse(saved);
    }
}