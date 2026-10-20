package com.roles.usermanagement.modules.audit.repository;

import com.roles.usermanagement.modules.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// [Nueva Funcionalidad: A-18 - Repositorio de logs de auditoría] - Búsqueda paginada de registros de auditoría por entidad o actor - Conecta con: AuditLog y AuditService
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId, Pageable pageable);
    Page<AuditLog> findByActorUsernameOrderByTimestampDesc(String actorUsername, Pageable pageable);
}
