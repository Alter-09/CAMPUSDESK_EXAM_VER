package com.roles.usermanagement.modules.lifecycle.enums;

public enum TransitionAction {

    // [Nueva Funcionalidad: T-01/T-09/U-01 - Acciones de transición de estado] - Habilita transiciones de flujo controlado - Conecta con: TicketLifecycleService y LifecycleRule
    ASSIGN,
    REASSIGN,
    START_PROGRESS,
    PAUSE,
    RESUME,
    CANCEL,
    ESCALATE,
    RESOLVE,
    CONFIRM_CLOSE
}
