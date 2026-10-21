package com.roles.usermanagement.modules.satisfaction.repository;

import com.roles.usermanagement.modules.satisfaction.entity.TicketSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

// [Nueva Funcionalidad: U-06/A-21 - Repositorio de satisfacción CSAT] - Consulta y agregación de calificaciones de tickets - Conecta con: TicketSatisfaction y SatisfactionService
public interface TicketSatisfactionRepository extends JpaRepository<TicketSatisfaction, Long> {

    Optional<TicketSatisfaction> findByTicketId(Long ticketId);

    @Query("SELECT AVG(CASE s.score " +
           "WHEN com.roles.usermanagement.modules.satisfaction.enums.CsatScore.VERY_DISSATISFIED THEN 1 " +
           "WHEN com.roles.usermanagement.modules.satisfaction.enums.CsatScore.DISSATISFIED THEN 2 " +
           "WHEN com.roles.usermanagement.modules.satisfaction.enums.CsatScore.NEUTRAL THEN 3 " +
           "WHEN com.roles.usermanagement.modules.satisfaction.enums.CsatScore.SATISFIED THEN 4 " +
           "WHEN com.roles.usermanagement.modules.satisfaction.enums.CsatScore.VERY_SATISFIED THEN 5 END) FROM TicketSatisfaction s")
    Double calculateAverageScore();
}
