package com.roles.usermanagement.modules.sla.service;

import com.roles.usermanagement.modules.notification.enums.NotificationType;
import com.roles.usermanagement.modules.notification.service.NotificationService;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaBreachScheduler {

    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    // Ejecuta cada 5 minutos (300000 ms) y se encarga de validar los tiempos de atencion de cada TICKET bajo LocalDateTime
    @Scheduled(fixedRate = 300000)
    public void checkSlaBreaches() {
        LocalDateTime threshold = LocalDateTime.now().plusHours(2);
        List<Ticket> nearingBreach = ticketRepository.findTicketsNearingSlaBreach(threshold);

        for (Ticket ticket : nearingBreach) {
            if (ticket.getTechnician() != null) {
                notificationService.createNotification(
                        ticket.getTechnician().getUsername(),
                        "Alerta de SLA: Ticket #" + ticket.getId(),
                        "El ticket '" + ticket.getTitle() + "' está a menos de 2 horas de su fecha límite de resolución.",
                        NotificationType.SLA_WARNING,
                        ticket.getId()
                );
            }

            if (ticket.getApplicant() != null) {
                notificationService.createNotification(
                        ticket.getApplicant().getUsername(),
                        "Actualización de SLA: Ticket #" + ticket.getId(),
                        "Su ticket se encuentra en etapa prioritaria de atención.",
                        NotificationType.SLA_WARNING,
                        ticket.getId()

                );
            }
        }
    }
}
