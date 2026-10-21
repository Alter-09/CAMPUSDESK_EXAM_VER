package com.roles.usermanagement.modules.timetracking.dto;

import java.time.LocalDateTime;

public record WorklogResponse(
        Long id,
        Long ticketId,
        String technicianUsername,
        Integer minutosDedicados,
        String descripcionActividad,
        LocalDateTime fechaRegistro
) {}
