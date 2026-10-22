package com.roles.usermanagement.modules.sla.repository;

import com.roles.usermanagement.modules.sla.entity.SlaPolicy;
import com.roles.usermanagement.modules.sla.enums.SlaPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// [Nueva Funcionalidad: A-01/T-15 - Repositorio de políticas SLA] - Acceso a datos para definición de tiempos de SLA por prioridad - Conecta con: SlaPolicy y SlaService
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long>{
    Optional<SlaPolicy> findByPriorityAndIsActiveTrue(SlaPriority priority);
}
