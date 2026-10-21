package com.roles.usermanagement.modules.sla.repository;

import com.roles.usermanagement.modules.sla.entity.SlaViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: A-08 - Repositorio de violaciones de SLA] - Consulta brechas registradas por ticket o para reportes de cumplimiento - Conecta con: SlaViolation y SlaService
public interface SlaViolationRepository extends JpaRepository<SlaViolation, Long> {
    List<SlaViolation> findByTicketId(Long ticketId);
}
