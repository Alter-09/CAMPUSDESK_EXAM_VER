package com.roles.usermanagement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    @Schema(description = "Nombre de usuario unico", example = "usuario_campus")
    private String username;

    @Schema(description = "Correo electronico institucional", example = "usuario@technova.com")
    private String email;

    @Schema(description = "Contraseña segura (minimo 6 caracteres)", example = "Password123!")
    private String password;
}
