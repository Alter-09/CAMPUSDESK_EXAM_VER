package com.roles.usermanagement.modules.ticket.service;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.dto.TicketCreateRequest;
import com.roles.usermanagement.modules.ticket.dto.TicketResponse;
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

    // Constructor: Spring detecta los 4 tipos y los entrega solo (inyección de dependencias)
    public TicketService(TicketRepository ticketRepository,
                         UserCrudRepository userRepository,
                         TicketMapper mapper,
                         TicketAccessPolicy policy) {
        // Guarda cada dependencia recibida en su campo
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.policy = policy;
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

        // save() inserta en la BD y devuelve el ticket ya con id y fechas
        // toResponse() lo convierte al DTO seguro que se devuelve al cliente
        return mapper.toResponse(ticketRepository.save(ticket));
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
}