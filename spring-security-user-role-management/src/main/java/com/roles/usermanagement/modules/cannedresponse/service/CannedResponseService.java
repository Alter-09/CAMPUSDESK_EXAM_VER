package com.roles.usermanagement.modules.cannedresponse.service;

import com.roles.usermanagement.modules.cannedresponse.dto.CannedResponseRequest;
import com.roles.usermanagement.modules.cannedresponse.dto.CannedResponseResponse;
import com.roles.usermanagement.modules.cannedresponse.entity.CannedResponse;
import com.roles.usermanagement.modules.cannedresponse.repository.CannedResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: T-04/A-19 - Servicio de respuestas rápidas] - Acceso a respuestas predefinidas para optimizar la atención técnica - Conecta con: CannedResponseRepository
@Service
@RequiredArgsConstructor
public class CannedResponseService {

    private final CannedResponseRepository cannedResponseRepository;

    @Transactional(readOnly = true)
    public List<CannedResponseResponse> getAvailableResponses(String technicianUsername) {
        return cannedResponseRepository.findByIsGlobalTrueOrCreatedByUsername(technicianUsername)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CannedResponseResponse createResponse(CannedResponseRequest request, String createdByUsername) {
        CannedResponse canned = CannedResponse.builder()
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .isGlobal(request.isGlobal() != null && request.isGlobal())
                .createdByUsername(createdByUsername)
                .build();

        return toResponse(cannedResponseRepository.save(canned));
    }

    private CannedResponseResponse toResponse(CannedResponse c) {
        return new CannedResponseResponse(
                c.getId(),
                c.getTitle(),
                c.getContent(),
                c.getCategory(),
                c.getIsGlobal(),
                c.getCreatedByUsername(),
                c.getCreatedAt()
        );
    }
}
