package com.roles.usermanagement.modules.lifecycle.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TicketStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    @JsonCreator
    public static TicketStatus fromString(String value) {
        if (value == null) return null;
        String v = value.trim().toUpperCase();
        return switch (v) {
            case "ABIERTA", "OPEN" -> OPEN;
            case "ASIGNADA", "ASSIGNED" -> ASSIGNED;
            case "EN_PROCESO", "IN_PROGRESS" -> IN_PROGRESS;
            case "RESUELTA", "RESOLVED" -> RESOLVED;
            case "CERRADA", "CLOSED" -> CLOSED;
            default -> valueOf(v);
        };
    }
}