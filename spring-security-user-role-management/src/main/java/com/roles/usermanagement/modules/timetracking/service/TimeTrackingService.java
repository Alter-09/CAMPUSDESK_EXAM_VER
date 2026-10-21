package com.roles.usermanagement.modules.timetracking.service;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import com.roles.usermanagement.modules.timetracking.dto.TimeLogRequest;
import com.roles.usermanagement.modules.timetracking.dto.TimeLogResponse;
import com.roles.usermanagement.modules.timetracking.entity.TicketTimeLog;
import com.roles.usermanagement.modules.timetracking.repository.TicketTimeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: T-02/A-15 - Servicio de control de tiempo] - Imputación de horas/minutos y cálculo de tiempo total por ticket - Conecta con: TicketTimeLogRepository y TicketRepository
@Service
@RequiredArgsConstructor
public class TimeTrackingService {

    private final TicketTimeLogRepository timeLogRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public TimeLogResponse logTime(Long ticketId, TimeLogRequest request, String technicianUsername) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        TicketTimeLog log = TicketTimeLog.builder()
                .ticket(ticket)
                .technicianUsername(technicianUsername)
                .durationMinutes(request.durationMinutes())
                .description(request.description())
                .build();

        TicketTimeLog saved = timeLogRepository.save(log);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TimeLogResponse> getTimeLogs(Long ticketId) {
        return timeLogRepository.findByTicketIdOrderByLoggedAtDesc(ticketId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public int getTotalMinutes(Long ticketId) {
        return timeLogRepository.sumMinutesByTicketId(ticketId);
    }

    private TimeLogResponse toResponse(TicketTimeLog l) {
        return new TimeLogResponse(
                l.getId(),
                l.getTicket().getId(),
                l.getTechnicianUsername(),
                l.getDurationMinutes(),
                l.getDescription(),
                l.getLoggedAt()
        );
    }
}
