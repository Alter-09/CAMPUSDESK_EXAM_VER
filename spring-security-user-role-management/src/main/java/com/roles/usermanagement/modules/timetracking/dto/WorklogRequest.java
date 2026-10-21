package com.roles.usermanagement.modules.timetracking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorklogRequest(
        @NotNull(message = "Los minutos dedicados son obligatorios")
        @Min(value = 5, message = "Los minutos mínimos son 5")
        @Max(value = 480, message = "Los minutos máximos por sesión son 480 (8 horas)")
        Integer minutosDedicados,

        @NotBlank(message = "La descripción de la actividad es obligatoria")
        String descripcionActividad
) {}
