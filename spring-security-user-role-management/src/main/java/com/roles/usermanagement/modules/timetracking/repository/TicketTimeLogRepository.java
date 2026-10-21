package com.roles.usermanagement.modules.timetracking.repository;

import com.roles.usermanagement.modules.timetracking.entity.TicketTimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// [Nueva Funcionalidad: T-02/A-15 - Repositorio de control de tiempo] - Consulta de logs de tiempo por ticket y cálculo de minutos acumulados - Conecta con: TicketTimeLog y TimeTrackingService
public interface TicketTimeLogRepository extends JpaRepository<TicketTimeLog, Long> {

    List<TicketTimeLog> findByTicketIdOrderByLoggedAtDesc(Long ticketId);

    @Query("SELECT COALESCE(SUM(l.durationMinutes), 0) FROM TicketTimeLog l WHERE l.ticket.id = :ticketId")
    int sumMinutesByTicketId(@Param("ticketId") Long ticketId);
}
