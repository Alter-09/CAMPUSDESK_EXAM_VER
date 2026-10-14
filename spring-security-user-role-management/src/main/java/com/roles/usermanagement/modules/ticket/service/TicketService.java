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

    // Constructor: Spring injects required dependencies
    public TicketService(TicketRepository ticketRepository,
                         UserCrudRepository userRepository,
                         TicketMapper mapper,
                         TicketAccessPolicy policy,
                         StatusHistoryService historyService) {
        // Stores each received dependency in its field
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.policy = policy;
        this.historyService = historyService;
    }

    // ---------- CREATE ----------

    // @Transactional: method executes as a single unit; rolls back on failure
    @Transactional
    public TicketResponse create(TicketCreateRequest request, Authentication auth) {
        // auth.getName() = username from token; query user in DB
        UserEntity applicant = userRepository.findById(auth.getName())
                // If user does not exist, throw 404 error
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // Build ticket field by field (Lombok Builder pattern)
        Ticket ticket = Ticket.builder()
                .title(request.title().trim())               // trim() quita espacios sobrantes
                .description(request.description().trim())
                .category(request.category())
                .priority(request.priority())
                .status(TicketStatus.OPEN)                   // initial status set by server
                .applicant(applicant)                        // applicant retrieved from token
                .build();                                    // build complete object

        // Save ticket; returned entity contains DB-generated ID
        Ticket savedTicket = ticketRepository.save(ticket);

// Record creation in history (no previous status), in same transaction
        historyService.record(
                savedTicket,
                null,                           // no previous status: newly created ticket
                TicketStatus.OPEN,              // estado nuevo
                applicant,                      // who created it
                StatusHistoryAction.CREATION);  // change type

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
}