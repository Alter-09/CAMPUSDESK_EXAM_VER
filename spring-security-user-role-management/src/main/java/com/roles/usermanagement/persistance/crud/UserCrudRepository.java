package com.roles.usermanagement.persistance.crud;

import com.roles.usermanagement.persistance.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Interface that defines CRUD operations (Create, Read, Update, Delete) for the UserEntity entity.
 * Extends CrudRepository provided by Spring Data JPA.
 *
 * Allows performing basic persistence operations for UserEntity objects in the database.
 */
public interface UserCrudRepository extends CrudRepository<UserEntity, String> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from UserEntity u where u.username = :username")
    java.util.Optional<UserEntity> findForUpdate(@org.springframework.data.repository.query.Param("username") String username);

    boolean existsByEmailAndUsernameNot(String email, String username);

    boolean existsByEmail(String email);

    java.util.Optional<UserEntity> findByEmail(String email);
}
