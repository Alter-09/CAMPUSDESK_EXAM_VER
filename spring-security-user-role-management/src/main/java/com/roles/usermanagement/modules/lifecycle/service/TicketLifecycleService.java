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

    // Inyección por constructor: Spring entrega cada dependencia al crear el service
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

    // Una sola transacción: el cambio de estado y su historial se confirman juntos o ninguno (RF-06)
    @Transactional
    public StateTransitionResponse transition(Long ticketId, StateTransitionRequest request, Authentication auth) {

        // 1. Buscar el ticket BLOQUEÁNDOLO; si no existe, 404
        Ticket ticket = ticketRepository.findForUpdate(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Quien no puede ver el ticket no debe enterarse de su estado (ni por un 409)
        policy.assertCanView(ticket, auth);

        TicketStatus previousStatus = ticket.getStatus();

        if (request.action() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Acción o estado requerido no válido");
        }

        // 2. Buscar la regla; si no existe, la transición no está permitida -> 409
        LifecycleRule rule = ruleRepository
                .findByCurrentStatusAndAction(previousStatus, request.action())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Action " + request.action() + " is not allowed when the ticket is " + previousStatus));

        // 3. Verificar que quien lo pide cumple lo que exige la regla
        assertCanPerform(rule, ticket, auth);

        // 4. Si es una asignación, validar al técnico ANTES de modificar el ticket
        if (request.action() == TransitionAction.ASSIGN || request.action() == TransitionAction.REASSIGN) {
            UserEntity technician = resolveTechnician(request.technicianUsername());

            // Reasignar al mismo técnico no tiene sentido
            if (ticket.getTechnician() != null
                    && ticket.getTechnician().getUsername().equals(technician.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The ticket is already assigned to that technician");
            }
            ticket.setTechnician(technician);
        }

        // 5. Cambiar el estado y registrar el historial (misma transacción)
        UserEntity actor = userRepository.findById(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ticket.setStatus(rule.getTargetStatus());
        ticketRepository.save(ticket);

        StatusHistory entry = historyService.record(
                ticket, previousStatus, rule.getTargetStatus(), actor, toHistoryAction(request.action()));

        // 6. Armar la respuesta (sin exponer entidades)
        return new StateTransitionResponse(
                ticket.getId(),
                request.action(),
                previousStatus,
                ticket.getStatus(),
                ticket.getTechnician() == null ? null : ticket.getTechnician().getUsername(),
                actor.getUsername(),
                entry.getChangedAt());
    }

    // Verifica que quien pide la acción cumple lo que exige la regla; si no, lanza 403
    private void assertCanPerform(LifecycleRule rule, Ticket ticket, Authentication auth) {
        // El usuario debe tener el rol que exige la regla
        if (!policy.hasRole(auth, rule.getRequiredRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your role cannot perform this action");
        }
        // Si la regla exige ser el solicitante, debe ser quien creó el ticket
        if (rule.isMustBeApplicant() && !policy.isApplicant(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the applicant can perform this action");
        }
        // Si la regla exige ser el técnico asignado, debe ser el responsable de este ticket
        if (rule.isMustBeAssignedTechnician() && !policy.isAssignedTechnician(ticket, auth)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned technician can perform this action");
        }
    }

    // Valida que el username corresponda a un técnico (tiene perfil) y devuelve su usuario
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

    // Traduce la acción del ciclo de vida al tipo de evento que se guarda en el historial
    private StatusHistoryAction toHistoryAction(TransitionAction action) {
        return switch (action) {
            case ASSIGN, REASSIGN -> StatusHistoryAction.ASSIGNMENT;
            case CONFIRM_CLOSE -> StatusHistoryAction.CLOSURE;
            case START_PROGRESS, RESOLVE -> StatusHistoryAction.STATUS_TRANSITION;
        };
    }
}