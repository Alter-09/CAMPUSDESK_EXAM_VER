package com.roles.usermanagement.domain.service;

import com.roles.usermanagement.domain.dto.UserDto;
import com.roles.usermanagement.domain.dto.UserPermissionDto;
import com.roles.usermanagement.domain.dto.UserPermissionsDto;
import com.roles.usermanagement.domain.dto.UserRoleDto;
import com.roles.usermanagement.persistance.repository.UserRepository;
import com.roles.usermanagement.persistance.repository.UserRoleRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final com.roles.usermanagement.persistance.crud.UserCrudRepository userCrudRepository;

  @Autowired
  public UserService(UserRepository userRepository,
                     UserRoleRepository userRoleRepository,
                     com.roles.usermanagement.persistance.crud.UserCrudRepository userCrudRepository) {
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.userCrudRepository = userCrudRepository;
  }

  public UserDto saveUser(UserDto userDto) {
    return userRepository.save(userDto);
  }

  public UserDto updateUser(UserDto userDto) {
    return userRepository.update(userDto);
  }

  public List<UserDto> getAllUsers() {
    return userRepository.getAllUsers();
  }

  public void deleteUser(String name) {
    userRepository.deleteUser(name);
  }

  public boolean exists(String name) {
    return userRepository.existsByUsername(name);
  }

  public UserDto grantPermission(UserPermissionDto dto) { return userRepository.grantPermission(dto); }
  public UserDto revokePermission(String username, String permission) {return userRepository.revokePermission(username,permission);}
  public UserPermissionsDto permissionDetails(String username) {return userRepository.permissionDetails(username);}
  public List<String> permissionCatalog() {return userRepository.permissionCatalog();}

  public UserRoleDto assignRoleToUser(UserRoleDto userRoleDto) {
    return userRoleRepository.save(userRoleDto);
  }

  public void changePassword(String username, String currentPassword, String newPassword) {
    userRepository.changeUserPassword(username, currentPassword, newPassword);
  }

  public void adminResetPassword(String targetUsername, String temporaryPassword) {
    userRepository.adminResetPassword(targetUsername, temporaryPassword);
  }

  public UserDto register(com.roles.usermanagement.domain.dto.RegisterDto dto) {
    return userRepository.register(dto);
  }

  // [Nueva Funcionalidad: U-15 - Consulta de perfil de usuario propio] - Obtiene información de perfil ampliado - Conecta con: UserProfileDto y UserEntity
  public com.roles.usermanagement.domain.dto.UserProfileDto getUserProfile(String username) {
    var user = userCrudRepository.findById(username)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    return com.roles.usermanagement.domain.dto.UserProfileDto.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .department(user.getDepartment())
            .build();
  }

  // [Nueva Funcionalidad: U-15 - Actualización de perfil propio] - Permite al usuario actualizar su teléfono y departamento - Conecta con: UserEntity
  public com.roles.usermanagement.domain.dto.UserProfileDto updateUserProfile(
          String username, com.roles.usermanagement.domain.dto.UserProfileUpdateRequest request) {
    var user = userCrudRepository.findById(username)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber().trim());
    if (request.department() != null) user.setDepartment(request.department().trim());
    userCrudRepository.save(user);

    return com.roles.usermanagement.domain.dto.UserProfileDto.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .department(user.getDepartment())
            .build();
  }

  // [Nueva Funcionalidad: A-22 - Bloqueo/desbloqueo masivo de usuarios] - Modifica en lote el estado locked de usuarios - Conecta con: UserEntity
  public int bulkUpdateStatus(List<String> usernames, boolean locked) {
    int count = 0;
    for (String username : usernames) {
      var opt = userCrudRepository.findById(username);
      if (opt.isPresent()) {
        opt.get().setLocked(locked);
        userCrudRepository.save(opt.get());
        count++;
      }
    }
    return count;
  }
}
