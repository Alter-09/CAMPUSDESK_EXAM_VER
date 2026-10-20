package com.roles.usermanagement.modules.timetracking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// [Nueva Funcionalidad: T-02 - Petición de registro de tiempo] - Minutos trabajados y descripción de tareas realizadas - Conecta con: TicketTimeLog
public record TimeLogRequest(
        @NotNull(message = "Los minutos trabajados son obligatorios")
        @Min(value = 1, message = "La duración mínima es de 1 minuto")
        Integer durationMinutes,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description
) {
}
