package com.roles.usermanagement.modules.timetracking.dto;

import java.util.List;

public record TiempoTotalResponse(
        Long ticketId,
        Integer totalMinutos,
        List<WorklogResponse> registros
) {}
