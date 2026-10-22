package com.roles.usermanagement.modules.sla.repository;

import com.roles.usermanagement.modules.sla.entity.SlaViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SlaViolationRepository extends JpaRepository<SlaViolation, Long> {
    List<SlaViolation> findByTicketId(Long ticketId);
}
