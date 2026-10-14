package com.roles.usermanagement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class LoginResponseDto {
    @Schema(description = "Token JWT")
    private String token;
    @Schema(description = "Nombre del usuario")
    private String username;
    @Schema(description = "true si debe cambiar la contraseña obligatoriamente")
    private Boolean mustChangePassword;
}
