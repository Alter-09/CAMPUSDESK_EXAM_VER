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
 * Assigns technicians and records the corresponding audit trail.
 */
@Service
public class TicketAssignmentService {

    private final TicketAssignmentLogRepository assignmentLogs;
    private final UserCrudRepository users;
    private final com.roles.usermanagement.modules.technician.repository.TechnicianRepository technicianRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TicketAssignmentService(TicketAssignmentLogRepository assignmentLogs,
                                   UserCrudRepository users,
                                   com.roles.usermanagement.modules.technician.repository.TechnicianRepository technicianRepository) {
        this.assignmentLogs = assignmentLogs;
        this.users = users;
        this.technicianRepository = technicianRepository;
    }

    /**
     * Assigns an open ticket for the first time and records the status change.
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
     * Atomically reassigns an assigned ticket to a different technician.
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
        if (user != null && ("superadmin".equalsIgnoreCase(user.getUsername())
                || (user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "SUPERADMIN".equalsIgnoreCase(r.getRole()))))) {
            return; // Superadmin has all permissions and satisfies any administrative or operational role requirement
        }
        boolean hasRole = user != null && user.getRoles() != null && user.getRoles().stream()
                .map(UserRoleEntity::getRole)
                .anyMatch(role::equalsIgnoreCase);
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

    // [Nueva Funcionalidad: T-01 - Auto-asignación de ticket por técnico] - Permite al técnico tomar un ticket libre de la cola - Conecta con: Ticket y TicketAssignmentLog
    @Transactional
    public TicketAssignmentResponse selfAssign(Long ticketId, String techUsername) {
        UserEntity tech = findUser(techUsername, "Técnico no encontrado");
        requireRole(tech, "TECHNICIAN", "Solo un técnico puede tomar tickets");

        Ticket ticket = findTicket(ticketId);
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw badRequest("Solo se pueden tomar tickets en estado ABIERTO");
        }

        ticket.setTechnician(tech);
        ticket.setStatus(TicketStatus.ASSIGNED);

        TicketAssignmentLog log = TicketAssignmentLog.builder()
                .ticket(ticket)
                .newTechnician(tech)
                .assignedBy(tech)
                .assignmentType(AssignmentType.INITIAL)
                .build();
        assignmentLogs.save(log);

        recordStatusHistory(ticket, tech, TicketStatus.OPEN, TicketStatus.ASSIGNED);
        return toResponse(log, ticket);
    }

    // [Nueva Funcionalidad: T-04/A-16 - Reglas de asignación automática] - Asigna ticket al técnico con menor carga disponible - Conecta con: TechnicianRepository y TicketAssignmentLog
    @Transactional
    public TicketAssignmentResponse autoAssign(Long ticketId) {
        var availableTechs = technicianRepository.findAvailableTechniciansWithCapacity();
        if (availableTechs.isEmpty()) {
            throw badRequest("No hay técnicos disponibles con capacidad libre para auto-asignación");
        }
        var chosenTech = availableTechs.get(0);
        Ticket ticket = findTicket(ticketId);
        ticket.setTechnician(chosenTech.getUser());
        ticket.setStatus(TicketStatus.ASSIGNED);
        chosenTech.setActiveTicketsCount(chosenTech.getActiveTicketsCount() + 1);

        TicketAssignmentLog log = TicketAssignmentLog.builder()
                .ticket(ticket)
                .newTechnician(chosenTech.getUser())
                .assignedBy(chosenTech.getUser())
                .assignmentType(AssignmentType.INITIAL)
                .justification("Auto-asignado por balanceo de carga")
                .build();
        assignmentLogs.save(log);

        recordStatusHistory(ticket, chosenTech.getUser(), TicketStatus.OPEN, TicketStatus.ASSIGNED);
        return toResponse(log, ticket);
    }

    // [Nueva Funcionalidad: A-26 - Reasignación masiva de tickets] - Transfiere todos los tickets activos de un técnico a otro - Conecta con: Ticket y TicketAssignmentLog
    @Transactional
    public int bulkReassign(String fromTechUsername, String toTechUsername, String adminUsername) {
        UserEntity admin = findUser(adminUsername, "Admin no encontrado");
        requireRole(admin, "ADMIN", "Solo ADMIN puede realizar reasignación masiva");

        UserEntity toTech = findUser(toTechUsername, "Técnico destino no encontrado");
        requireRole(toTech, "TECHNICIAN", "Usuario destino no es técnico");

        var query = entityManager.createQuery(
                "SELECT t FROM Ticket t WHERE t.technician.username = :fromTech AND t.status IN (com.roles.usermanagement.modules.lifecycle.enums.TicketStatus.ASSIGNED, com.roles.usermanagement.modules.lifecycle.enums.TicketStatus.IN_PROGRESS)",
                Ticket.class);
        query.setParameter("fromTech", fromTechUsername);
        var tickets = query.getResultList();

        for (Ticket ticket : tickets) {
            ticket.setTechnician(toTech);
            TicketAssignmentLog log = TicketAssignmentLog.builder()
                    .ticket(ticket)
                    .previousTechnician(findUser(fromTechUsername, ""))
                    .newTechnician(toTech)
                    .assignedBy(admin)
                    .assignmentType(AssignmentType.REASSIGNMENT)
                    .justification("Reasignación masiva administrativa")
                    .build();
            assignmentLogs.save(log);
        }
        return tickets.size();
    }
}
