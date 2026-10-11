package com.roles.usermanagement.web.controller;

import com.roles.usermanagement.domain.dto.ChangePasswordDto;
import com.roles.usermanagement.domain.dto.LoginDto;
import com.roles.usermanagement.domain.dto.LoginResponseDto;
import com.roles.usermanagement.domain.dto.UserPermissionsDto;
import com.roles.usermanagement.domain.service.UserService;
import com.roles.usermanagement.persistance.crud.UserCrudRepository;
import com.roles.usermanagement.persistance.entity.UserEntity;
import com.roles.usermanagement.persistance.repository.UserRepository;
import com.roles.usermanagement.web.config.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Emisión de tokens y cambio de contraseña")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository users;
    private final UserCrudRepository userCrudRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, 
                          JwtUtil jwtUtil, 
                          UserRepository users,
                          UserCrudRepository userCrudRepository,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.users = users;
        this.userCrudRepository = userCrudRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Obtener token JWT", description = "Valida credenciales y devuelve token junto a mustChangePassword")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        if (loginDto.getUsername() == null || loginDto.getUsername().isBlank()
                || loginDto.getPassword() == null || loginDto.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario y contraseña son obligatorios"));
        }
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

            String token = jwtUtil.create(authentication.getName());

            // Consultar si el usuario debe cambiar su clave
            UserEntity user = userCrudRepository.findById(authentication.getName()).orElse(null);
            boolean mustChange = user != null && Boolean.TRUE.equals(user.getMustChangePassword());

            return ResponseEntity.ok(new LoginResponseDto(token, authentication.getName(), mustChange));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales invalidas o cuenta no disponible"));
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar contraseña (primer ingreso o voluntario)")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto dto, Principal principal) {
        if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()
                || dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Contraseña actual y nueva son obligatorias"));
        }
        userService.changePassword(principal.getName(), dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente. Ya puedes utilizar la aplicación."));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Consultar rol y permisos de la sesión actual")
    public UserPermissionsDto me(Principal principal) {
        return users.permissionDetails(principal.getName());
    }
}
