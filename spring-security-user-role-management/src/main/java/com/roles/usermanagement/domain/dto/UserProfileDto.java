package com.roles.usermanagement.domain.dto;

import lombok.*;

// [Nueva Funcionalidad: U-15 - DTO de perfil de usuario] - Información de perfil editable por el usuario - Conecta con: UserEntity y UserService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private String username;
    private String email;
    private String phoneNumber;
    private String department;
}
