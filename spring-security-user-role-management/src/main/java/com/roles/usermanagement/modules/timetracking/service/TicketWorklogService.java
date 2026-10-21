package com.roles.usermanagement.modules.timetracking.service;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import com.roles.usermanagement.modules.timetracking.dto.TiempoTotalResponse;
import com.roles.usermanagement.modules.timetracking.dto.WorklogRequest;
import com.roles.usermanagement.modules.timetracking.dto.WorklogResponse;
import com.roles.usermanagement.modules.timetracking.entity.TicketWorklog;
import com.roles.usermanagement.modules.timetracking.repository.TicketWorklogRepository;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import com.roles.usermanagement.persistance.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Servicio para la bitácora de tiempo técnico (Worklog) - Ejercicio 4 Examen.
 */
@Service
@RequiredArgsConstructor
public class TicketWorklogService {

    private final TicketWorklogRepository worklogRepository;
    private final TicketRepository ticketRepository;
    private final UserCrudRepository userRepository;

    @Transactional
    public WorklogResponse registerWorklog(Long ticketId, WorklogRequest request, Authentication auth) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        // Regla 1: Solo el TECHNICIAN asignado puede registrar tiempo
        if (ticket.getTechnician() == null || !ticket.getTechnician().getUsername().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el técnico asignado a este ticket puede registrar tiempos de trabajo");
        }

        // Regla 2: El ticket debe estar en estado EN_PROCESO
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden registrar bloques de tiempo en tickets en estado EN_PROCESO (IN_PROGRESS)");
        }

        // Regla 3: Minutos entre 5 y 480 (reforzado a nivel de servicio)
        if (request.minutosDedicados() == null || request.minutosDedicados() < 5 || request.minutosDedicados() > 480) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los minutos dedicados deben ser mínimo 5 y máximo 480 (8 horas)");
        }

        UserEntity technician = userRepository.findById(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));

        TicketWorklog worklog = TicketWorklog.builder()
                .ticket(ticket)
                .technician(technician)
                .minutosDedicados(request.minutosDedicados())
                .descripcionActividad(request.descripcionActividad().trim())
                .build();

        TicketWorklog saved = worklogRepository.save(worklog);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TiempoTotalResponse getTiempoTotal(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado");
        }

        Integer totalMinutos = worklogRepository.sumMinutosByTicketId(ticketId);
        List<WorklogResponse> registros = worklogRepository.findByTicketIdOrderByFechaRegistroDesc(ticketId).stream()
                .map(this::toResponse)
                .toList();

        return new TiempoTotalResponse(ticketId, totalMinutos != null ? totalMinutos : 0, registros);
    }

    private WorklogResponse toResponse(TicketWorklog w) {
        return new WorklogResponse(
                w.getId(),
                w.getTicket().getId(),
                w.getTechnician().getUsername(),
                w.getMinutosDedicados(),
                w.getDescripcionActividad(),
                w.getFechaRegistro()
        );
    }
}
