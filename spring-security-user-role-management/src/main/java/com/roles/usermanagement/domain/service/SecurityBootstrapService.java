package com.roles.usermanagement.domain.service;

import com.roles.usermanagement.persistance.crud.*;
import com.roles.usermanagement.persistance.entity.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class SecurityBootstrapService {
    private final PermissionCrudRepository permissions;
    private final RoleCrudRepository roles;
    private final UserCrudRepository users;
    private final UserRoleCrudRepository userRoles;
    private final PasswordEncoder encoder;

    @PersistenceContext
    private EntityManager entityManager;

    // Configuración de Superadministrador (Acceso total a la plataforma)
    private final boolean superAdminEnabled;
    private final String superAdminUsername;
    private final String superAdminEmail;
    private final String superAdminPassword;

    // Configuración de Administrador Operativo (Gestión de tickets, técnicos y usuarios)
    private final boolean adminEnabled;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public SecurityBootstrapService(
            PermissionCrudRepository permissions,
            RoleCrudRepository roles,
            UserCrudRepository users,
            UserRoleCrudRepository userRoles,
            PasswordEncoder encoder,
            // Superadmin properties (compatibilidad con app.bootstrap.*)
            @Value("${app.bootstrap.enabled:false}") boolean superAdminEnabled,
            @Value("${app.bootstrap.username:superadmin}") String superAdminUsername,
            @Value("${app.bootstrap.email:superadmin@localhost}") String superAdminEmail,
            @Value("${app.bootstrap.password:}") String superAdminPassword,
            // Admin properties (app.bootstrap.admin.*)
            @Value("${app.bootstrap.admin.enabled:false}") boolean adminEnabled,
            @Value("${app.bootstrap.admin.username:admin}") String adminUsername,
            @Value("${app.bootstrap.admin.email:admin@localhost}") String adminEmail,
            @Value("${app.bootstrap.admin.password:}") String adminPassword) {
        this.permissions = permissions;
        this.roles = roles;
        this.users = users;
        this.userRoles = userRoles;
        this.encoder = encoder;

        this.superAdminEnabled = superAdminEnabled;
        this.superAdminUsername = superAdminUsername;
        this.superAdminEmail = superAdminEmail;
        this.superAdminPassword = superAdminPassword;

        this.adminEnabled = adminEnabled;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Transactional
    public void initialize() {
        // 1. Inicializar catálogo completo de permisos en base de datos
        Map<String, PermissionEntity> catalog = new LinkedHashMap<>();
        for (UserRoles.Authority authority : UserRoles.Authority.values()) {
            String name = authority.value();
            PermissionEntity permission = permissions.findById(name).orElseGet(() -> {
                PermissionEntity created = new PermissionEntity();
                created.setName(name);
                return permissions.save(created);
            });
            catalog.put(name, permission);
        }

        // 2. Asignar permisos diferenciados a cada uno de los 4 roles
        for (UserRoles.Role roleName : UserRoles.Role.values()) {
            RoleEntity role = roles.findById(roleName.name()).orElseGet(() -> {
                RoleEntity created = new RoleEntity();
                created.setName(roleName.name());
                return created;
            });

            for (PermissionEntity permission : catalog.values()) {
                String permName = permission.getName();
                boolean shouldAssign = false;

                if (roleName == UserRoles.Role.SUPERADMIN) {
                    // Superadmin tiene acceso a la TOTALIDAD de las funcionalidades (100% de los permisos)
                    shouldAssign = true;
                } else if (roleName == UserRoles.Role.ADMIN) {
                    // Admin tiene permisos administrativos operativos, EXCLUYENDO las facultades críticas de Superadmin
                    try {
                        UserRoles.Authority auth = UserRoles.Authority.valueOf(permName);
                        shouldAssign = UserRoles.isAdminAuthority(auth);
                    } catch (IllegalArgumentException e) {
                        shouldAssign = false;
                    }
                } else if (roleName == UserRoles.Role.TECHNICIAN) {
                    shouldAssign = permName.equals(UserRoles.Authority.TICKET_READ_ASSIGNED.name())
                            || permName.equals(UserRoles.Authority.TICKET_STATUS_UPDATE.name())
                            || permName.equals(UserRoles.Authority.COMMENT_CREATE.name())
                            || permName.equals(UserRoles.Authority.COMMENT_READ.name())
                            || permName.equals(UserRoles.Authority.HISTORY_READ.name())
                            || permName.equals(UserRoles.Authority.USER_READ.name());
                } else if (roleName == UserRoles.Role.USER) {
                    shouldAssign = permName.equals(UserRoles.Authority.TICKET_CREATE.name())
                            || permName.equals(UserRoles.Authority.TICKET_READ_OWN.name())
                            || permName.equals(UserRoles.Authority.TICKET_UPDATE_OWN.name())
                            || permName.equals(UserRoles.Authority.TICKET_CLOSE.name())
                            || permName.equals(UserRoles.Authority.COMMENT_CREATE.name())
                            || permName.equals(UserRoles.Authority.COMMENT_READ.name())
                            || permName.equals(UserRoles.Authority.HISTORY_READ.name());
                }

                if (shouldAssign) {
                    boolean present = role.getPermissions().stream()
                            .anyMatch(existing -> existing.getName().equals(permName));
                    if (!present) {
                        role.getPermissions().add(permission);
                    }
                } else {
                    // Asegurar que permisos no correspondientes queden removidos del rol
                    role.getPermissions().removeIf(existing -> existing.getName().equals(permName));
                }
            }
            roles.save(role);
        }

        // 3. Crear o actualizar la cuenta de Superadministrador
        bootstrapUser(superAdminEnabled, superAdminUsername, superAdminEmail, superAdminPassword, UserRoles.Role.SUPERADMIN);

        // 4. Crear o actualizar la cuenta de Administrador Operativo
        bootstrapUser(adminEnabled, adminUsername, adminEmail, adminPassword, UserRoles.Role.ADMIN);
    }

    private void bootstrapUser(boolean isEnabled, String targetUsername, String targetEmail, String rawPassword, UserRoles.Role targetRole) {
        if (!isEnabled) return;
        if (targetUsername == null || targetUsername.isBlank() || targetUsername.length() > 50
                || targetEmail == null || targetEmail.isBlank() || targetEmail.length() > 200) {
            throw new IllegalStateException("Configura username y email válidos para " + targetRole.name());
        }

        if (users.existsById(targetUsername)) {
            UserEntity user = users.findById(targetUsername).orElse(null);
            if (user != null) {
                boolean hasTargetRole = user.getRoles() != null && user.getRoles().stream()
                        .anyMatch(r -> targetRole.name().equalsIgnoreCase(r.getRole()));

                if (!hasTargetRole) {
                    entityManager.createNativeQuery("DELETE FROM user_role WHERE username = ?1")
                            .setParameter(1, targetUsername)
                            .executeUpdate();
                    entityManager.flush();

                    UserRoleEntity assignment = new UserRoleEntity();
                    assignment.setUsername(targetUsername);
                    assignment.setRole(targetRole.name());
                    userRoles.save(assignment);
                    entityManager.flush();
                }

                boolean changed = false;
                if (Boolean.TRUE.equals(user.getLocked())) { user.setLocked(false); changed = true; }
                if (Boolean.TRUE.equals(user.getDisabled())) { user.setDisabled(false); changed = true; }
                if (Boolean.TRUE.equals(user.getMustChangePassword())) { user.setMustChangePassword(false); changed = true; }
                if (rawPassword != null && !rawPassword.isBlank()) {
                    user.setPassword(encoder.encode(rawPassword));
                    changed = true;
                }
                if (changed) users.save(user);
            }
            return;
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException("Configura una contraseña para crear el usuario inicial: " + targetUsername);
        }

        UserEntity user = new UserEntity();
        user.setUsername(targetUsername);
        user.setEmail(targetEmail.trim().toLowerCase());
        user.setPassword(encoder.encode(rawPassword));
        user.setLocked(false);
        user.setDisabled(false);
        user.setMustChangePassword(false);
        user.setRoles(new ArrayList<>());
        users.save(user);

        UserRoleEntity assignment = new UserRoleEntity();
        assignment.setUsername(targetUsername);
        assignment.setRole(targetRole.name());
        userRoles.save(assignment);
    }
}