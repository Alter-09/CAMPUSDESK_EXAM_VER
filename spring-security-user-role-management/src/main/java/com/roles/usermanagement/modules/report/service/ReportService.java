package com.roles.usermanagement.modules.report.service;

import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [Nueva Funcionalidad: U-19/A-11 - Servicio de generación y exportación de reportes] - Exporta tickets a formato CSV para usuarios y administradores - Conecta con: TicketRepository
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public String exportUserTicketsCsv(String username) {
        Page<Ticket> page = ticketRepository.findByApplicant_Username(username, PageRequest.of(0, 500));
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Titulo,Categoria,Prioridad,Estado,FechaCreacion\n");

        for (Ticket t : page.getContent()) {
            csv.append(t.getId()).append(",")
               .append("\"").append(t.getTitle().replace("\"", "\"\"")).append("\",")
               .append(t.getCategory()).append(",")
               .append(t.getPriority()).append(",")
               .append(t.getStatus()).append(",")
               .append(t.getCreatedAt()).append("\n");
        }

        return csv.toString();
    }
}
