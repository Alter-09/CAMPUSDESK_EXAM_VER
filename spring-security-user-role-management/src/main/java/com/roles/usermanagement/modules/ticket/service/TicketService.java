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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// @Service: marks class as business logic and registers it with Spring
@Service
public class TicketService {

    // Dependencies: final = immutable after instantiation
    private final TicketRepository ticketRepository;   // talks to tickets table
    private final UserCrudRepository userRepository;   // talks to users table
    private final TicketMapper mapper;                 // convierte Ticket -> TicketResponse
    private final TicketAccessPolicy policy;           // rules for who can view what
    private final StatusHistoryService historyService;
    private final com.roles.usermanagement.modules.sla.service.SlaService slaService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    // Constructor: Spring injects required dependencies
    public TicketService(TicketRepository ticketRepository,
                         UserCrudRepository userRepository,
                         TicketMapper mapper,
                         TicketAccessPolicy policy,
                         StatusHistoryService historyService,
                         com.roles.usermanagement.modules.sla.service.SlaService slaService,
                         org.springframework.context.ApplicationEventPublisher eventPublisher) {
        // Stores each received dependency in its field
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.policy = policy;
        this.historyService = historyService;
        this.slaService = slaService;
        this.eventPublisher = eventPublisher;
    }

    // ---------- CREATE ----------

    // [Nueva Funcionalidad: U-01/U-04/U-26 - Creación de ticket con SLA y eventos] - Asigna tiempos de SLA y emite evento de creación - Conecta con: SlaService y TicketCreatedEvent
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
                savedTicket.getServiceCatalogId()
        ));

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
    public List<TicketResponse> list(TicketStatus status,      // filtros opcionales: pueden venir null
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

        // findAll executes single SQL query with all predicates, sorted from newest to oldest
        return ticketRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()                       // stream ticket list
                .map(mapper::toResponse)        // converts each Ticket to TicketResponse
                .toList();                      // collect results into list
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

        // Only applicant can modify ticket; any other user receives 403
        if (!policy.isApplicant(ticket, auth)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the applicant can modify the ticket");
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

    // [Nueva Funcionalidad: U-01 - Cancelar ticket propio] - Permite al solicitante cancelar un ticket en estado OPEN - Conecta con: Ticket y StatusHistoryService
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

    // [Nueva Funcionalidad: U-05 - Reabrir ticket cerrado/resuelto] - Solicita reapertura de un ticket con justificación - Conecta con: Ticket y StatusHistoryService
    @Transactional
    public TicketResponse reopenTicket(Long id, String reason, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!policy.isApplicant(ticket, auth) && !policy.isAdmin(auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para reabrir este ticket");
        }
        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo tickets resueltos o cerrados pueden reabrirse");
        }

        TicketStatus prev = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setReopenedCount(ticket.getReopenedCount() + 1);
        Ticket saved = ticketRepository.save(ticket);

        UserEntity user = userRepository.findById(auth.getName()).orElse(null);
        historyService.record(saved, prev, TicketStatus.IN_PROGRESS, user, StatusHistoryAction.STATUS_TRANSITION);
        return mapper.toResponse(saved);
    }

    // [Nueva Funcionalidad: U-13 - Duplicar ticket como plantilla] - Crea un nuevo ticket basándose en los datos de uno existente - Conecta con: Ticket
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
                null
        );
        return create(req, auth);
    }

    // [Nueva Funcionalidad: U-24 - Solicitar escalación de ticket] - Marca el ticket como escalado para atención prioritaria - Conecta con: Ticket y NotificationService
    @Transactional
    public TicketResponse requestEscalation(Long id, String reason, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        policy.assertCanView(ticket, auth);

        ticket.setIsEscalated(true);
        ticket.setStatus(TicketStatus.ESCALATED);
        return mapper.toResponse(ticketRepository.save(ticket));
    }

    // [Nueva Funcionalidad: T-27 - Crear ticket a nombre de un usuario] - Permite a un técnico registrar un ticket para un solicitante - Conecta con: Ticket
    @Transactional
    public TicketResponse createOnBehalf(TicketCreateRequest request, String applicantUsername, Authentication auth) {
        UserEntity applicant = userRepository.findById(applicantUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario solicitante no encontrado"));

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

    // [Nueva Funcionalidad: U-03/U-21/T-21 - Listado paginado de tickets] - Retorna página de tickets según permisos de visualización - Conecta con: TicketRepository y PageResponse
    @Transactional(readOnly = true)
    public com.roles.usermanagement.domain.dto.PageResponse<TicketResponse> listPaginated(
            int page, int size, TicketStatus status, TicketPriority priority, TicketCategory category, Authentication auth) {

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
                spec, org.springframework.data.domain.PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        java.util.List<TicketResponse> content = p.getContent().stream().map(mapper::toResponse).toList();
        return new com.roles.usermanagement.domain.dto.PageResponse<>(
                content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }
}