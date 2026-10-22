package com.roles.usermanagement.modules.sla.service;

import com.roles.usermanagement.modules.sla.entity.SlaPolicy;
import com.roles.usermanagement.modules.sla.entity.SlaViolation;
import com.roles.usermanagement.modules.sla.enums.SlaBreachType;
import com.roles.usermanagement.modules.sla.enums.SlaPriority;
import com.roles.usermanagement.modules.sla.repository.SlaPolicyRepository;
import com.roles.usermanagement.modules.sla.repository.SlaViolationRepository;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SlaService {

    private final SlaPolicyRepository policyRepository;
    private final SlaViolationRepository violationRepository;
    private final TicketRepository ticketRepository;

    public LocalDateTime calculateResolutionDueDate(TicketPriority priority) {
        SlaPriority slaPriority = toSlaPriority(priority);
        int hours = policyRepository.findByPriorityAndIsActiveTrue(slaPriority)
                .map(SlaPolicy::getResolutionHours)
                .orElse(defaultResolutionHours(slaPriority));
        return LocalDateTime.now().plusHours(hours);
    }

    public LocalDateTime calculateFirstResponseDueDate(TicketPriority priority) {
        SlaPriority slaPriority = toSlaPriority(priority);
        int hours = policyRepository.findByPriorityAndIsActiveTrue(slaPriority)
                .map(SlaPolicy::getFirstResponseHours)
                .orElse(defaultFirstResponseHours(slaPriority));
        return LocalDateTime.now().plusHours(hours);
    }

    @Transactional
    public void recordBreach(Long ticketId, SlaBreachType type, String notes) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SlaViolation violation = SlaViolation.builder()
                    .ticket(ticketOpt.get())
                    .breachType(type)
                    .breachedAt(LocalDateTime.now())
                    .notes(notes)
                    .build();
            violationRepository.save(violation);
        }
    }

    private SlaPriority toSlaPriority(TicketPriority priority) {
        if (priority == null) return SlaPriority.MEDIUM;
        return switch (priority) {
            case BAJA, LOW -> SlaPriority.LOW;
            case MEDIA, MEDIUM -> SlaPriority.MEDIUM;
            case ALTA, HIGH -> SlaPriority.HIGH;
            case CRITICA, CRITICAL -> SlaPriority.CRITICAL;
        };
    }

    private int defaultResolutionHours(SlaPriority priority) {
        return switch (priority) {
            case LOW -> 48;
            case MEDIUM -> 24;
            case HIGH -> 8;
            case CRITICAL -> 4;
        };
    }

    private int defaultFirstResponseHours(SlaPriority priority) {
        return switch (priority) {
            case LOW -> 24;
            case MEDIUM -> 12;
            case HIGH -> 4;
            case CRITICAL -> 1;
        };
    }
}
