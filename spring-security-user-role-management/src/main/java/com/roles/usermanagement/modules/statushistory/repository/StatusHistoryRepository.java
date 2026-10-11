package com.roles.usermanagement.modules.statushistory.repository;

import com.roles.usermanagement.modules.statushistory.entity.StatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos del historial de estados de los tickets.
 */
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    /**
     * Devuelve el historial de un ticket en orden cronológico
     * (del cambio más antiguo al más reciente).
     */
    List<StatusHistory> findByTicketIdOrderByChangedAtAscIdAsc(Long ticketId);
}