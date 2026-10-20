package com.roles.usermanagement.modules.lifecycle.service;

import com.roles.usermanagement.modules.lifecycle.dto.StateTransitionRequest;
import com.roles.usermanagement.modules.lifecycle.dto.StateTransitionResponse;
import com.roles.usermanagement.modules.lifecycle.entity.LifecycleRule;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;
import com.roles.usermanagement.modules.lifecycle.repository.LifecycleRuleRepository;
import com.roles.usermanagement.modules.statushistory.entity.StatusHistory;
import com.roles.usermanagement.modules.statushistory.enums.StatusHistoryAction;
import com.roles.usermanagement.modules.statushistory.service.StatusHistoryService;
import com.roles.usermanagement.modules.technician.entity.TechnicianProfile;
import com.roles.usermanagement.modules.technician.repository.TechnicianRepository;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import com.roles.usermanagement.modules.ticket.service.TicketAccessPolicy;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import com.roles.usermanagement.persistance.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketLifecycleService {

    private final TicketRepository ticketRepository;
    private final LifecycleRuleRepository ruleRepository;
    private final TechnicianRepository technicianRepository;
    private final UserCrudRepository userRepository;
    private final StatusHistoryService historyService;
    private final TicketAccessPolicy policy;

    // Constructor injection: Spring provides each dependency when creating the service
    public TicketLifecycleService(TicketRepository ticketRepository,
                                  LifecycleRuleRepository ruleRepository,
                                  TechnicianRepository technicianRepository,
                                  UserCrudRepository userRepository,
                                  StatusHistoryService historyService,
                                  TicketAccessPolicy policy) {
        this.ticketRepository = ticketRepository;
        this.ruleRepository = ruleRepository;
        this.technicianRepository = technicianRepository;
        this.userRepository = userRepository;
        this.historyService = historyService;
        this.policy = policy;
    }

    // Single transaction: status change and history record are committed together or not at all (RF-06)
    @Transactional
    public StateTransitionResponse transition(Long ticketId, StateTransitionRequest request, Authentication auth) {

        // 1. Find ticket with LOCKING; if it does not exist, 404
        Ticket ticket = ticketRepository.findForUpdate(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Users without permission to view ticket must not know its state (not even via 409)
        policy.assertCanView(ticket, auth);

        TicketStatus previousStatus = ticket.getStatus();

        if (request.action() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Acción o estado requerido no válido");
        }

        // 2. Find rule; if not found, transition is not permitted -> 409
        LifecycleRule rule = ruleRepository
                .findByCurrentStatusAndAction(previousStatus, request.action())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Action " + request.action() + " is not allowed when the ticket is " + previousStatus));

        // 3. Verify requester satisfies the rule requirements
        assertCanPerform(rule, ticket, auth);

        // 4. If assignment, validate technician BEFORE modifying ticket
        if (request.action() == TransitionAction.ASSIGN || request.action() == TransitionAction.REASSIGN) {
            UserEntity technician = resolveTechnician(request.technicianUsername());

            // Reassigning to the same technician is not allowed
            if (ticket.getTechnician() != null
                    && ticket.getTechnician().getUsername().equals(technician.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The ticket is already assigned to that technician");
            }
            ticket.setTechnician(technician);
        }

        // 5. Change status and record history (same transaction)
        UserEntity actor = userRepository.findById(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ticket.setStatus(rule.getTargetStatus());
        ticketRepository.save(ticket);

        StatusHistory entry = historyService.record(
                ticket, previousStatus, rule.getTargetStatus(), actor, toHistoryAction(request.action()));

        // 6. Build response (without exposing entities)
        return new StateTransitionResponse(
                ticket.getId(),
                request.action(),
                previousStatus,
                ticket.getStatus(),
                ticket.getTechnician() == null ? null : ticket.getTechnician().getUsername(),
                actor.getUsername(),
                entry.getChangedAt());
    }

    // Verifies requester satisfies rule requirements; otherwise throws 403
    private void assertCanPerform(LifecycleRule rule, Ticket ticket, Authentication auth) {
        // User must have the role required by rule
        if (!policy.hasRole(auth, rule.getRequiredRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your role cannot perform this action");
        }
        // If rule requires applicant, user must be the ticket creator
        if (rule.isMustBeApplicant() && !policy.isApplicant(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the applicant can perform this action");
        }
        // If rule requires assigned technician, user must be responsible for this ticket
        if (rule.isMustBeAssignedTechnician() && !policy.isAssignedTechnician(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned technician can perform this action");
        }
    }

    // Validates that username corresponds to a technician (has profile) and returns user
    private UserEntity resolveTechnician(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "technicianUsername is required for this action");
        }
        TechnicianProfile profile = technicianRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The selected user is not a technician"));
        return profile.getUser();
    }

    // [Nueva Funcionalidad: T-01/T-09/U-01 - Mapeo de acciones ampliadas a historial] - Mapea pausas, cancelaciones y escalaciones al historial de estado - Conecta con: StatusHistoryAction y TransitionAction
    private StatusHistoryAction toHistoryAction(TransitionAction action) {
        return switch (action) {
            case ASSIGN, REASSIGN -> StatusHistoryAction.ASSIGNMENT;
            case CONFIRM_CLOSE, CANCEL -> StatusHistoryAction.CLOSURE;
            case START_PROGRESS, PAUSE, RESUME, ESCALATE, RESOLVE -> StatusHistoryAction.STATUS_TRANSITION;
        };
    }
}