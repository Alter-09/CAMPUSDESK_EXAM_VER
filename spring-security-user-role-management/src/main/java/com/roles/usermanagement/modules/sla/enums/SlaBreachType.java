package com.roles.usermanagement.modules.sla.enums;

// [Nueva Funcionalidad: A-01/T-18 - Tipo de incumplimiento de SLA] - Distingue si la brecha es de primera respuesta o de resolución final - Conecta con: SlaViolation y SlaPolicy
public enum SlaBreachType {
    FIRST_RESPONSE,
    RESOLUTION
}
