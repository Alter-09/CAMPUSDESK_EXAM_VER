package com.roles.usermanagement.modules.timetracking.repository;

import com.roles.usermanagement.modules.timetracking.entity.TicketWorklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketWorklogRepository extends JpaRepository<TicketWorklog, Long> {

    @Query("SELECT COALESCE(SUM(w.minutosDedicados), 0) FROM TicketWorklog w WHERE w.ticket.id = :ticketId")
    Integer sumMinutosByTicketId(@Param("ticketId") Long ticketId);

    List<TicketWorklog> findByTicketIdOrderByFechaRegistroDesc(Long ticketId);
}
