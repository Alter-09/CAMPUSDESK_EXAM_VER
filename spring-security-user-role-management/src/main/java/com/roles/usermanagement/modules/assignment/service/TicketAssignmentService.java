package com.roles.usermanagement.modules.assignment.service;

import com.roles.usermanagement.modules.assignment.dto.TicketAssignmentResponse;
import com.roles.usermanagement.modules.assignment.entity.TicketAssignmentLog;
import com.roles.usermanagement.modules.assignment.enums.AssignmentType;
import com.roles.usermanagement.modules.assignment.repository.TicketAssignmentLogRepository;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.statushistory.entity.StatusHistory;
import com.roles.usermanagement.modules.statushistory.enums.StatusHistoryAction;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.persistance.entity.UserEntity;
import com.roles.usermanagement.persistance.entity.UserRoleEntity;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Ejecuta la asignación de técnicos y registra sus trazas de auditoría.
 */
@Service
public class TicketAssignmentService {

    private final TicketAssignmentLogRepository assignmentLogs;
    private final UserCrudRepository users;

    @PersistenceContext
    private EntityManager entityManager;

    public TicketAssignmentService(TicketAssignmentLogRepository assignmentLogs, UserCrudRepository users) {
        this.assignmentLogs = assignmentLogs;
        this.users = users;
    }

    /**
     * Asigna por primera vez una incidencia abierta y registra el cambio de estado.
     */
    @Transactional
    public TicketAssignmentResponse assign(Long ticketId, String technicianUsername, String adminUsername) {
        UserEntity admin = findUser(adminUsername, "El administrador autenticado no existe");
        requireRole(admin, "ADMIN", "Solo un ADMIN puede asignar incidencias");

        Ticket ticket = findTicket(ticketId);
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw badRequest("Solo se pueden asignar incidencias en estado ABIERTA");
        }

        UserEntity technician = findUser(technicianUsername, "El técnico seleccionado no existe");
        requireRole(technician, "TECHNICIAN", "El usuario seleccionado no tiene el rol TECHNICIAN");

        ticket.setTechnician(technician);
        ticket.setStatus(TicketStatus.ASSIGNED);

        TicketAssignmentLog log = TicketAssignmentLog.builder()
                .ticket(ticket)
                .newTechnician(technician)
                .assignedBy(admin)
                .assignmentType(AssignmentType.INITIAL)
                .build();
        TicketAssignmentLog saved = assignmentLogs.save(log);
        recordStatusHistory(ticket, admin, TicketStatus.OPEN, ticket.getStatus());

        return toResponse(saved, ticket);
    }

    /**
     * Reasigna una incidencia asignada a un técnico distinto, de forma atómica.
     */
    @Transactional
    public TicketAssignmentResponse reassign(
            Long ticketId,
            String technicianUsername,
            String justification,
            String adminUsername
    ) {
        UserEntity admin = findUser(adminUsername, "El administrador autenticado no existe");
        requireRole(admin, "ADMIN", "Solo un ADMIN puede reasignar incidencias");

        Ticket ticket = findTicket(ticketId);
        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            throw badRequest("Solo se pueden reasignar incidencias en estado ASIGNADA");
        }

        UserEntity technician = findUser(technicianUsername, "El técnico seleccionado no existe");
        requireRole(technician, "TECHNICIAN", "El usuario seleccionado no tiene el rol TECHNICIAN");
        if (ticket.getTechnician() == null) {
            throw badRequest("La incidencia no tiene un técnico asignado");
        }
        if (Objects.equals(ticket.getTechnician().getUsername(), technicianUsername)) {
            throw badRequest("La incidencia ya está asignada a ese técnico");
        }

        UserEntity previousTechnician = ticket.getTechnician();
        ticket.setTechnician(technician);

        TicketAssignmentLog log = TicketAssignmentLog.builder()
                .ticket(ticket)
                .previousTechnician(previousTechnician)
                .newTechnician(technician)
                .assignedBy(admin)
                .assignmentType(AssignmentType.REASSIGNMENT)
                .justification(justification.trim())
                .build();
        TicketAssignmentLog saved = assignmentLogs.save(log);
        recordStatusHistory(ticket, admin, TicketStatus.ASSIGNED, ticket.getStatus());

        return toResponse(saved, ticket);
    }

    private Ticket findTicket(Long ticketId) {
        Ticket ticket = entityManager.find(Ticket.class, ticketId, LockModeType.PESSIMISTIC_WRITE);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La incidencia no existe");
        }
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw badRequest("No se puede asignar un técnico a una incidencia cerrada");
        }
        return ticket;
    }

    private UserEntity findUser(String username, String message) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario autenticado");
        }
        return users.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, message));
    }

    private void requireRole(UserEntity user, String role, String message) {
        boolean hasRole = user.getRoles() != null && user.getRoles().stream()
                .map(UserRoleEntity::getRole)
                .anyMatch(role::equals);
        if (!hasRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void recordStatusHistory(Ticket ticket, UserEntity admin, TicketStatus previous, TicketStatus current) {
        StatusHistory history = StatusHistory.builder()
                .ticket(ticket)
                .previousStatus(previous)
                .newStatus(current)
                .changedBy(admin)
                .action(StatusHistoryAction.ASSIGNMENT)
                .build();
        entityManager.persist(history);
    }

    private TicketAssignmentResponse toResponse(TicketAssignmentLog log, Ticket ticket) {
        return new TicketAssignmentResponse(
                log.getId(),
                ticket.getId(),
                log.getPreviousTechnician() == null ? null : log.getPreviousTechnician().getUsername(),
                log.getNewTechnician().getUsername(),
                log.getAssignedBy().getUsername(),
                log.getAssignmentType(),
                log.getJustification(),
                log.getAssignedAt(),
                ticket.getStatus()
        );
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
