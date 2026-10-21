package com.roles.usermanagement.modules.announcement.service;

import com.roles.usermanagement.modules.announcement.dto.AnnouncementRequest;
import com.roles.usermanagement.modules.announcement.dto.AnnouncementResponse;
import com.roles.usermanagement.modules.announcement.entity.SystemAnnouncement;
import com.roles.usermanagement.modules.announcement.repository.SystemAnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// [Nueva Funcionalidad: U-16/A-05 - Servicio de anuncios del sistema] - Publicación y consulta de avisos activos para todos los usuarios - Conecta con: SystemAnnouncementRepository
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final SystemAnnouncementRepository announcementRepository;

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, String createdBy) {
        SystemAnnouncement announcement = SystemAnnouncement.builder()
                .title(request.title())
                .message(request.message())
                .type(request.type() != null ? request.type() : "INFO")
                .startsAt(request.startsAt())
                .expiresAt(request.expiresAt())
                .isActive(request.isActive() == null || request.isActive())
                .createdBy(createdBy)
                .build();

        return toResponse(announcementRepository.save(announcement));
    }

    private AnnouncementResponse toResponse(SystemAnnouncement a) {
        return new AnnouncementResponse(
                a.getId(),
                a.getTitle(),
                a.getMessage(),
                a.getType(),
                a.getStartsAt(),
                a.getExpiresAt(),
                a.getIsActive(),
                a.getCreatedBy(),
                a.getCreatedAt()
        );
    }
}
