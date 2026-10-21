package com.roles.usermanagement.modules.satisfaction.enums;

// [Nueva Funcionalidad: U-06/A-21 - Escala de puntuación de satisfacción CSAT] - Puntuación de 1 a 5 estrellas para evaluar la atención del ticket - Conecta con: TicketSatisfaction y SatisfactionService
public enum CsatScore {
    VERY_DISSATISFIED(1),
    DISSATISFIED(2),
    NEUTRAL(3),
    SATISFIED(4),
    VERY_SATISFIED(5);

    private final int value;

    CsatScore(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
