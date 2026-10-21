package com.roles.usermanagement.modules.ticket.repository;

import com.roles.usermanagement.modules.ticket.entity.TicketRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// [Nueva Funcionalidad: T-07/T-28 - Repositorio de relaciones entre tickets] - Consulta vínculos y tickets duplicados o dependientes - Conecta con: TicketRelation y TicketLinkService
public interface TicketRelationRepository extends JpaRepository<TicketRelation, Long> {

    @Query("SELECT r FROM TicketRelation r WHERE r.sourceTicket.id = :ticketId OR r.targetTicket.id = :ticketId")
    List<TicketRelation> findAllRelationsForTicket(@Param("ticketId") Long ticketId);
}
