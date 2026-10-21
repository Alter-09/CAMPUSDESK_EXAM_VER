package com.roles.usermanagement.modules.asset.enums;

// [Nueva Funcionalidad: T-23/A-27 - Estados operativos del activo IT] - Clasifica el ciclo de vida del equipo asignado a usuarios - Conecta con: Asset y AssetService
public enum AssetStatus {
    IN_USE,
    IN_STOCK,
    MAINTENANCE,
    DECOMMISSIONED
}
