package com.roles.usermanagement.modules.tag.repository;

import com.roles.usermanagement.modules.tag.entity.TicketTagAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: U-22/T-16 - Repositorio de asignación de tags] - Consulta y desasignación de tags vinculados a tickets - Conecta con: TicketTagAssignment y TicketTagService
public interface TicketTagAssignmentRepository extends JpaRepository<TicketTagAssignment, Long> {
    List<TicketTagAssignment> findByTicketId(Long ticketId);
    void deleteByTicketId(Long ticketId);
}
