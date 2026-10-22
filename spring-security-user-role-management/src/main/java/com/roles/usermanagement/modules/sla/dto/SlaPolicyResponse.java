package com.roles.usermanagement.modules.sla.dto;

import com.roles.usermanagement.modules.sla.enums.SlaPriority;
import java.time.LocalDateTime;

public record SlaPolicyResponse(
        Long id,
        String name,
        SlaPriority priority,
        Integer firstResponseHours,
        Integer resolutionHours,
        Boolean isActive,
        LocalDateTime createdAt
) {
}
