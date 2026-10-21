package com.roles.usermanagement.domain.dto;

import jakarta.validation.constraints.Size;

// [Nueva Funcionalidad: U-15 - Petición de actualización de perfil de usuario] - Permite editar teléfono y departamento - Conecta con: UserEntity y UserService
public record UserProfileUpdateRequest(
        @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
        String phoneNumber,

        @Size(max = 100, message = "El departamento no puede superar 100 caracteres")
        String department
) {
}
