package com.roles.usermanagement.domain.repository;

import com.roles.usermanagement.domain.dto.UserDto;
import com.roles.usermanagement.domain.dto.UserRoleDto;
import com.roles.usermanagement.persistance.entity.UserEntity;
import java.util.List;

/**
 * Interface defining operations to access users in the domain.
 */
public interface IUserRepository {

  /**
   * Loads a user by their username.
   *
   * @param username The username of the user to find.
   * @return The UserEntity corresponding to the provided username.
   */
  UserDto loadUserByUsername(String username);

  /**
   * Creates or updates a user in the database.
   *
   * @param userDto The UserEntity to create or update.
   *                   If the username already exists, it is updated.
   *                   If the username does not exist, it is created.
   */
  UserDto save(UserDto userDto);

  /**
   * Retrieves all users from the database.
   * @return List of users.
   */
  List<UserDto> getAllUsers();

  /**
   * Deletes a user from the database.
   * @param username The username of the user to delete.
   */
  void deleteUser(String username);

  /**
   * Checks if a user exists with the provided username.
   * @param username The username of the user to check.
   */
  boolean existsByUsername(String username);
}