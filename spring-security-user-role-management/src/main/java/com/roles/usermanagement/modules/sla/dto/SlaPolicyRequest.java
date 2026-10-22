package com.roles.usermanagement.modules.sla.dto;

import com.roles.usermanagement.modules.sla.enums.SlaPriority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SlaPolicyRequest(
        @NotBlank(message = "El nombre de la política es obligatorio")
        String name,
        @NotNull(message = "La prioridad es obligatoria")
        SlaPriority priority,
        @NotNull(message = "El tiempo de primera respuesta es obligatorio")
        @Min(value = 1, message = "Las horas deben ser al menos 1")
        Integer firstResponseHours,
        @NotNull(message = "El tiempo de resolución es obligatorio")
        @Min(value = 1, message = "Las horas deben ser al menos 1")
        Integer resolutionHours,
        Boolean isActive
) {
}
