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

// @Service: marca la clase como lógica de negocio y la registra en Spring
@Service
public class TicketService {

    // Dependencias: final = no se pueden reemplazar después de crear el objeto
    private final TicketRepository ticketRepository;   // habla con la tabla tickets
    private final UserCrudRepository userRepository;   // habla con la tabla de usuarios
    private final TicketMapper mapper;                 // convierte Ticket -> TicketResponse
    private final TicketAccessPolicy policy;           // reglas de quién puede ver qué
    private final StatusHistoryService historyService;

    // Constructor: Spring detecta los 4 tipos y los entrega solo (inyección de dependencias)
    public TicketService(TicketRepository ticketRepository,
                         UserCrudRepository userRepository,
                         TicketMapper mapper,
                         TicketAccessPolicy policy,
                         StatusHistoryService historyService) {
        // Guarda cada dependencia recibida en su campo
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.policy = policy;
        this.historyService = historyService;
    }

    // ---------- CREATE ----------

    // @Transactional: todo el método es una unidad; si falla algo, se revierte todo
    @Transactional
    public TicketResponse create(TicketCreateRequest request, Authentication auth) {
        // auth.getName() = username del token; se busca ese usuario en la BD
        UserEntity applicant = userRepository.findById(auth.getName())
                // Si el usuario no existe, lanza error 404
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // Construye el ticket campo por campo (patrón Builder de Lombok)
        Ticket ticket = Ticket.builder()
                .title(request.title().trim())               // trim() quita espacios sobrantes
                .description(request.description().trim())
                .category(request.category())
                .priority(request.priority())
                .status(TicketStatus.OPEN)                   // el estado inicial lo fija el servidor
                .applicant(applicant)                        // el solicitante sale del token
                .build();                                    // termina de crear el objeto

        // Guarda el ticket; el resultado ya trae el id generado por la BD
        Ticket savedTicket = ticketRepository.save(ticket);

// Registra en el historial la creación (sin estado anterior), en la misma transacción
        historyService.record(
                savedTicket,
                null,                           // no hay estado anterior: el ticket acaba de nacer
                TicketStatus.OPEN,              // estado nuevo
                applicant,                      // quien lo creó
                StatusHistoryAction.CREATION);  // tipo de cambio

        return mapper.toResponse(savedTicket);
    }

    // ---------- GET BY ID ----------

    // readOnly = true: solo lee, no modifica; mantiene la sesión abierta para leer relaciones LAZY
    @Transactional(readOnly = true)
    public TicketResponse getById(Long id, Authentication auth) {
        // findById devuelve un Optional (una "caja" que puede estar vacía)
        Ticket ticket = ticketRepository.findById(id)
                // Si está vacía, error 404; si no, saca el ticket de la caja
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        // El "guardia": si no tiene permiso, lanza 403 y el método se corta aquí
        policy.assertCanView(ticket, auth);

        // Solo llega a esta línea si pasó el guardia
        return mapper.toResponse(ticket);
    }

    // ---------- LIST ----------

    @Transactional(readOnly = true)
    public List<TicketResponse> list(TicketStatus status,      // filtros opcionales: pueden venir null
                                     TicketPriority priority,
                                     TicketCategory category,
                                     Authentication auth) {

        // Variable que guardará la condición de PERMISO (qué tickets puede ver este usuario)
        Specification<Ticket> visibility;
        if (policy.isAdmin(auth)) {
            // Admin: sin restricción, ve todos los tickets
            visibility = Specification.unrestricted();
        } else if (policy.isTechnician(auth)) {
            // Técnico: solo los tickets asignados a él
            visibility = TicketSpecifications.assignedTo(auth.getName());
        } else {
            // Cualquier otro: solo los tickets que él creó (lo más restrictivo por defecto)
            visibility = TicketSpecifications.appliedBy(auth.getName());
        }

        // Une con AND el permiso + los filtros; los filtros null se ignoran
        Specification<Ticket> spec = Specification.allOf(
                visibility,
                TicketSpecifications.hasStatus(status),
                TicketSpecifications.hasPriority(priority),
                TicketSpecifications.hasCategory(category));

        // findAll ejecuta UNA consulta SQL con todas las condiciones, ordenada del más reciente al más antiguo
        return ticketRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()                       // recorre la lista de tickets
                .map(mapper::toResponse)        // convierte cada Ticket en TicketResponse
                .toList();                      // junta los resultados en una lista
    }

    /**
     * Actualiza un ticket. Solo puede hacerlo su solicitante y únicamente
     * mientras el ticket esté abierto (OPEN) y sin técnico asignado.
     */
    @Transactional
    public TicketResponse update(Long id, TicketUpdateRequest request, Authentication auth) {
        // Busca el ticket; si no existe, responde 404
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        // Solo el solicitante puede modificarlo; cualquier otro recibe 403
        if (!policy.isApplicant(ticket, auth)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the applicant can modify the ticket");
        }

        // Si ya no está abierto o ya tiene técnico, el estado actual no lo permite: 409
        if (ticket.getStatus() != TicketStatus.OPEN || ticket.getTechnician() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Only an open, unassigned ticket can be modified");
        }

        // Copia los datos nuevos del request a la entidad (sin espacios sobrantes)
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());

        // Guarda y devuelve el DTO de respuesta
        return mapper.toResponse(ticketRepository.save(ticket));
    }
}