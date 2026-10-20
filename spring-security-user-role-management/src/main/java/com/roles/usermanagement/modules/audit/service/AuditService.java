package com.roles.usermanagement.modules.audit.service;

import com.roles.usermanagement.domain.dto.PageResponse;
import com.roles.usermanagement.modules.audit.dto.AuditLogResponse;
import com.roles.usermanagement.modules.audit.entity.AuditLog;
import com.roles.usermanagement.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: A-18 - Servicio de auditoría] - Registro inmutable y consulta paginada de trazas de seguridad y administración - Conecta con: AuditLogRepository
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void recordAction(String actorUsername, String action, String entityName,
                             String entityId, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .actorUsername(actorUsername)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getLogsByEntity(String entityName, String entityId, int page, int size) {
        Page<AuditLog> p = auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId, PageRequest.of(page, size));
        List<AuditLogResponse> content = p.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getActorUsername(),
                a.getAction(),
                a.getEntityName(),
                a.getEntityId(),
                a.getDetails(),
                a.getIpAddress(),
                a.getTimestamp()
        );
    }
}
