package com.roles.usermanagement.modules.ticket.service;

import com.roles.usermanagement.modules.ticket.dto.SubtaskRequest;
import com.roles.usermanagement.modules.ticket.dto.SubtaskResponse;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.entity.TicketSubtask;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import com.roles.usermanagement.modules.ticket.repository.TicketSubtaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// [Nueva Funcionalidad: T-06 - Servicio de subtareas] - Creación y marcado de subtareas en tickets - Conecta con: TicketSubtaskRepository y TicketRepository
@Service
@RequiredArgsConstructor
public class TicketSubtaskService {

    private final TicketSubtaskRepository subtaskRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public SubtaskResponse createSubtask(Long ticketId, SubtaskRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        TicketSubtask subtask = TicketSubtask.builder()
                .ticket(ticket)
                .title(request.title())
                .assignedTo(request.assignedTo())
                .isCompleted(false)
                .build();

        return toResponse(subtaskRepository.save(subtask));
    }

    @Transactional
    public SubtaskResponse toggleCompleted(Long subtaskId) {
        TicketSubtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new IllegalArgumentException("Subtarea no encontrada"));

        subtask.setIsCompleted(!subtask.getIsCompleted());
        subtask.setCompletedAt(subtask.getIsCompleted() ? LocalDateTime.now() : null);

        return toResponse(subtask);
    }

    @Transactional(readOnly = true)
    public List<SubtaskResponse> getSubtasksByTicket(Long ticketId) {
        return subtaskRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::toResponse)
                .toList();
    }

    private SubtaskResponse toResponse(TicketSubtask s) {
        return new SubtaskResponse(
                s.getId(),
                s.getTicket().getId(),
                s.getTitle(),
                s.getIsCompleted(),
                s.getAssignedTo(),
                s.getCreatedAt(),
                s.getCompletedAt()
        );
    }
}
