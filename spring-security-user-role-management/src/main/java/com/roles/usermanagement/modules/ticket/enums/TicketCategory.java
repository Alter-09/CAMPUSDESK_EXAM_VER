package com.roles.usermanagement.modules.ticket.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TicketCategory {
    HARDWARE,
    SOFTWARE,
    REDES,
    ACCESOS,
    OTROS,
    NETWORK,
    ACCESS,
    OTHER;

    @JsonCreator
    public static TicketCategory fromString(String value) {
        if (value == null) return null;
        String v = value.trim().toUpperCase();
        return switch (v) {
            case "REDES", "NETWORK" -> REDES;
            case "ACCESOS", "ACCESS" -> ACCESOS;
            case "OTROS", "OTHER" -> OTROS;
            case "HARDWARE" -> HARDWARE;
            case "SOFTWARE" -> SOFTWARE;
            default -> valueOf(v);
        };
    }
}