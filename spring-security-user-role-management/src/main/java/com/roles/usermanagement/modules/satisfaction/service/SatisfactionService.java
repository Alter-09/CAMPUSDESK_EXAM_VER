package com.roles.usermanagement.modules.satisfaction.service;

import com.roles.usermanagement.modules.satisfaction.dto.CsatRequest;
import com.roles.usermanagement.modules.satisfaction.dto.CsatResponse;
import com.roles.usermanagement.modules.satisfaction.entity.TicketSatisfaction;
import com.roles.usermanagement.modules.satisfaction.repository.TicketSatisfactionRepository;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [Nueva Funcionalidad: U-06/A-21 - Servicio de satisfacción CSAT] - Registra y calcula calificaciones promedio del servicio de soporte - Conecta con: TicketSatisfactionRepository y TicketRepository
@Service
@RequiredArgsConstructor
public class SatisfactionService {

    private final TicketSatisfactionRepository satisfactionRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public CsatResponse submitCsat(Long ticketId, CsatRequest request, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        if (satisfactionRepository.findByTicketId(ticketId).isPresent()) {
            throw new IllegalStateException("El ticket ya cuenta con una evaluación de satisfacción registrada");
        }

        TicketSatisfaction satisfaction = TicketSatisfaction.builder()
                .ticket(ticket)
                .score(request.score())
                .feedback(request.feedback())
                .submittedBy(username)
                .build();

        TicketSatisfaction saved = satisfactionRepository.save(satisfaction);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CsatResponse getByTicketId(Long ticketId) {
        return satisfactionRepository.findByTicketId(ticketId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Double getAverageScore() {
        Double avg = satisfactionRepository.calculateAverageScore();
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    private CsatResponse toResponse(TicketSatisfaction s) {
        return new CsatResponse(
                s.getId(),
                s.getTicket().getId(),
                s.getScore(),
                s.getFeedback(),
                s.getSubmittedBy(),
                s.getCreatedAt()
        );
    }
}
