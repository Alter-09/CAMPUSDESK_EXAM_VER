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
 * Recording and querying ticket status history.
 */
@Service
public class StatusHistoryService {

    private final StatusHistoryRepository historyRepository;  // stores and reads history
    private final TicketRepository ticketRepository;          // to find ticket when querying
    private final TicketAccessPolicy policy;                  // who can view what
    private final StatusHistoryMapper mapper;                 // entidad -> DTO

    // Spring supplies the four dependencies via constructor
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
     * Saves a status change in history.
     * Requires an existing transaction (MANDATORY): ensures ticket status change
     * y su registro se confirman o se revierten juntos.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public StatusHistory record(Ticket ticket,
                                TicketStatus previousStatus,
                                TicketStatus newStatus,
                                UserEntity changedBy,
                                StatusHistoryAction action) {
        // Builds history record with transition data
        StatusHistory entry = StatusHistory.builder()
                .ticket(ticket)                    // which ticket it belongs to
                .previousStatus(previousStatus)    // previous status (null if creation)
                .newStatus(newStatus)              // estado nuevo
                .changedBy(changedBy)              // who performed the change
                .action(action)                    // action type
                .build();                          // changedAt is set by @PrePersist on entity

        // Inserts record into database
        return historyRepository.save(entry);
    }


    /**
     * Returns ticket history in chronological order,
     * only if authenticated user has permission to view that ticket.
     */
    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getByTicket(Long ticketId, Authentication auth) {
        // Finds ticket; if not found, responds 404
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        // Security guard: if user cannot view ticket, throws 403 and execution halts
        policy.assertCanView(ticket, auth);

        // Retrieves sorted history records and maps them to DTO
        return historyRepository.findByTicketIdOrderByChangedAtAscIdAsc(ticketId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}