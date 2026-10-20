package com.roles.usermanagement.domain.service;

import com.roles.usermanagement.domain.dto.UserDto;
import com.roles.usermanagement.persistance.crud.RoleCrudRepository;
import com.roles.usermanagement.persistance.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserSecurityService implements UserDetailsService {
    public static final String RANDOM_ORDER = "random_order";
    public static final String ROLE_PREFIX = "ROLE_";
    private final UserRepository userRepository;
    private final RoleCrudRepository roleRepository;

    public UserSecurityService(UserRepository userRepository, RoleCrudRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto user = userRepository.loadUserByUsername(username);
        if (user == null) throw new UsernameNotFoundException("User " + username + " not found.");
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        boolean isSuperAdmin = "superadmin".equalsIgnoreCase(username)
                || "SUPERADMIN".equalsIgnoreCase(user.getRole());
        if (user.getRoles() != null) {
            for (var assignment : user.getRoles()) {
                if ("SUPERADMIN".equalsIgnoreCase(assignment.getRole())
                        || ("ADMIN".equalsIgnoreCase(assignment.getRole()) && "superadmin".equalsIgnoreCase(username))) {
                    isSuperAdmin = true;
                    break;
                }
            }
        }

        if (isSuperAdmin) {
            // Superadmin has absolute access to every role and authority across the system
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_TECHNICIAN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            for (UserRoles.Authority auth : UserRoles.Authority.values()) {
                authorities.add(new SimpleGrantedAuthority(auth.name()));
            }

            if (user.getAdditionalPermissions() != null) {
                user.getAdditionalPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            }

            return User.builder().username(user.getUsername()).password(user.getPassword())
                    .authorities(authorities).accountLocked(false)
                    .disabled(false).build();
        }

        Set<String> roleNames = new LinkedHashSet<>();
        if (user.getRole() != null && !user.getRole().isBlank()) {
            roleNames.add(user.getRole().toUpperCase().trim());
        }
        if (user.getRoles() != null) {
            user.getRoles().forEach(assignment -> {
                if (assignment != null && assignment.getRole() != null && !assignment.getRole().isBlank()) {
                    roleNames.add(assignment.getRole().toUpperCase().trim());
                }
            });
        }

        for (String roleName : roleNames) {
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + roleName));
            roleRepository.findById(roleName).ifPresent(role ->
                    role.getPermissions().forEach(permission ->
                            authorities.add(new SimpleGrantedAuthority(permission.getName()))));
        }
        if (user.getAdditionalPermissions() != null) {
            user.getAdditionalPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }
        return User.builder().username(user.getUsername()).password(user.getPassword())
                .authorities(authorities).accountLocked(Boolean.TRUE.equals(user.getLocked()))
                .disabled(Boolean.TRUE.equals(user.getDisabled())).build();
    }
}
