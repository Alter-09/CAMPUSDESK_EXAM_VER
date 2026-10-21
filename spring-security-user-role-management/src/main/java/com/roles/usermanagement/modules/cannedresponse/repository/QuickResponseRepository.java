package com.roles.usermanagement.modules.cannedresponse.repository;

import com.roles.usermanagement.modules.cannedresponse.entity.QuickResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuickResponseRepository extends JpaRepository<QuickResponse, Long> {
    List<QuickResponse> findByTechnician_UsernameOrderByCreatedAtDesc(String username);
    Optional<QuickResponse> findByIdAndTechnician_Username(Long id, String username);
    boolean existsByTechnician_UsernameAndTituloIgnoreCase(String username, String titulo);
}
