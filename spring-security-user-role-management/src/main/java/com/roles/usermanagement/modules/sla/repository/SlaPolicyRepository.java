package com.roles.usermanagement.modules.sla.repository;

import com.roles.usermanagement.modules.sla.entity.SlaPolicy;
import com.roles.usermanagement.modules.sla.enums.SlaPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long>{
    Optional<SlaPolicy> findByPriorityAndIsActiveTrue(SlaPriority priority);
}
