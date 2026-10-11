package com.roles.usermanagement.modules.statushistory.service;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.statushistory.dto.StatusHistoryResponse;
import com.roles.usermanagement.modules.statushistory.entity.StatusHistory;
import com.roles.usermanagement.modules.statushistory.enums.StatusHistoryAction;
import com.roles.usermanagement.modules.statushistory.mapper.StatusHistoryMapper;
import com.roles.usermanagement.modules.statushistory.repository.StatusHistoryRepository;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import com.roles.usermanagement.modules.ticket.service.TicketAccessPolicy;
import com.roles.usermanagement.persistance.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Registro y consulta del historial de estados de los tickets.
 */
@Service
public class StatusHistoryService {

    private final StatusHistoryRepository historyRepository;  // guarda y lee el historial
    private final TicketRepository ticketRepository;          // para buscar el ticket al consultar
    private final TicketAccessPolicy policy;                  // quién puede ver qué
    private final StatusHistoryMapper mapper;                 // entidad -> DTO

    // Spring entrega las cuatro dependencias por constructor
    public StatusHistoryService(StatusHistoryRepository historyRepository,
                                TicketRepository ticketRepository,
                                TicketAccessPolicy policy,
                                StatusHistoryMapper mapper) {
        this.historyRepository = historyRepository;
        this.ticketRepository = ticketRepository;
        this.policy = policy;
        this.mapper = mapper;
    }

    /**
     * Guarda un cambio de estado en el historial.
     * Exige una transacción ya abierta (MANDATORY): así el cambio de estado del ticket
     * y su registro se confirman o se revierten juntos.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public StatusHistory record(Ticket ticket,
                                TicketStatus previousStatus,
                                TicketStatus newStatus,
                                UserEntity changedBy,
                                StatusHistoryAction action) {
        // Construye la fila del historial con los datos del cambio
        StatusHistory entry = StatusHistory.builder()
                .ticket(ticket)                    // a qué ticket pertenece
                .previousStatus(previousStatus)    // estado anterior (null si es la creación)
                .newStatus(newStatus)              // estado nuevo
                .changedBy(changedBy)              // quién hizo el cambio
                .action(action)                    // tipo de cambio
                .build();                          // changedAt lo pone @PrePersist de la entidad

        // Inserta la fila en la base de datos
        return historyRepository.save(entry);
    }


    /**
     * Devuelve el historial de un ticket en orden cronológico,
     * solo si el usuario autenticado tiene permiso para ver ese ticket.
     */
    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getByTicket(Long ticketId, Authentication auth) {
        // Busca el ticket; si no existe, responde 404
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        // El "guardia": si no puede ver el ticket, lanza 403 y el método se corta aquí
        policy.assertCanView(ticket, auth);

        // Trae las filas del historial ya ordenadas y las convierte a DTO
        return historyRepository.findByTicketIdOrderByChangedAtAscIdAsc(ticketId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}