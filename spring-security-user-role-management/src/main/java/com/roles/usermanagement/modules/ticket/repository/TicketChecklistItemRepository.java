package com.roles.usermanagement.modules.ticket.repository;

import com.roles.usermanagement.modules.ticket.entity.TicketChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: T-30 - Repositorio de checklist de resolución] - Consulta ítems requeridos para la resolución de un ticket - Conecta con: TicketChecklistItem y TicketChecklistService
public interface TicketChecklistItemRepository extends JpaRepository<TicketChecklistItem, Long> {
    List<TicketChecklistItem> findByTicketId(Long ticketId);
    long countByTicketIdAndIsCheckedFalse(Long ticketId);
}
