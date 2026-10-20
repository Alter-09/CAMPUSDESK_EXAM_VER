package com.roles.usermanagement.modules.announcement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-16/A-05 - Anuncios del sistema] - Avisos institucionales, mantenimiento o fallas generales programadas - Conecta con: AnnouncementRepository y AnnouncementService
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "system_announcements", indexes = {
        @Index(name = "idx_announcement_active", columnList = "is_active, starts_at, expires_at")
})
public class SystemAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(length = 20)
    @Builder.Default
    private String type = "INFO"; // INFO, WARNING, DANGER

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.startsAt == null) this.startsAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
        if (this.type == null) this.type = "INFO";
    }
}
