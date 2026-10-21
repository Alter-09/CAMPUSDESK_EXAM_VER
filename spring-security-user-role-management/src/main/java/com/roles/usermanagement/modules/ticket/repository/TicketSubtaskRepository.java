package com.roles.usermanagement.modules.ticket.repository;

import com.roles.usermanagement.modules.ticket.entity.TicketSubtask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: T-06 - Repositorio de subtareas] - Acceso a datos de subtareas asociadas a un ticket - Conecta con: TicketSubtask y TicketSubtaskService
public interface TicketSubtaskRepository extends JpaRepository<TicketSubtask, Long> {
    List<TicketSubtask> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
