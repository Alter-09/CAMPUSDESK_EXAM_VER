package com.roles.usermanagement.modules.satisfaction.dto;

import com.roles.usermanagement.modules.satisfaction.enums.CsatScore;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-06/A-21 - Respuesta de encuesta CSAT] - Datos de evaluación de servicio del ticket - Conecta con: TicketSatisfaction
public record CsatResponse(
        Long id,
        Long ticketId,
        CsatScore score,
        String feedback,
        String submittedBy,
        LocalDateTime createdAt
) {
}
