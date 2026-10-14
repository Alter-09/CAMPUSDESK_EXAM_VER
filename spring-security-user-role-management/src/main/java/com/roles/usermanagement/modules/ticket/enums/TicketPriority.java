package com.roles.usermanagement.modules.ticket.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TicketPriority {
    BAJA,
    MEDIA,
    ALTA,
    CRITICA,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    @JsonCreator
    public static TicketPriority fromString(String value) {
        if (value == null) return null;
        String v = value.trim().toUpperCase();
        return switch (v) {
            case "BAJA", "LOW" -> BAJA;
            case "MEDIA", "MEDIUM" -> MEDIA;
            case "ALTA", "HIGH" -> ALTA;
            case "CRITICA", "CRÍTICA", "CRITICAL" -> CRITICA;
            default -> valueOf(v);
        };
    }
}