package com.roles.usermanagement.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginDto {
    @Schema(description = "Nombre de usuario o correo electronico", example = "usuario@technova.com")
    private String username;

    @Schema(description = "Correo electronico alternativo", example = "usuario@technova.com")
    private String email;

    @Schema(description = "Contraseña de la cuenta", example = "Password123!")
    private String password;

    public void setIdentifier(String identifier) {
        if (this.username == null) {
            this.username = identifier;
        }
    }

    public String getIdentifier() {
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return null;
    }
}
