package com.roles.usermanagement.modules.satisfaction.dto;

import com.roles.usermanagement.modules.satisfaction.enums.CsatScore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// [Nueva Funcionalidad: U-06 - Petición de encuesta CSAT] - Calificación de 1 a 5 y feedback opcional del usuario - Conecta con: TicketSatisfaction
public record CsatRequest(
        @NotNull(message = "La puntuación es obligatoria")
        CsatScore score,

        @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
        String feedback
) {
}
