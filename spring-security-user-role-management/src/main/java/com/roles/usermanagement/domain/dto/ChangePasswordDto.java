package com.roles.usermanagement.domain.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @Schema(description = "Contraseña actual o temporal", example = "Temporal123!")
    private String currentPassword;
    @Schema(description = "Nueva contraseña definitiva", example = "MiNuevaClave2026!")
    private String newPassword;
}
