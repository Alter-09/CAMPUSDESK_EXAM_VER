package com.roles.usermanagement.modules.announcement.repository;

import com.roles.usermanagement.modules.announcement.entity.SystemAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

// [Nueva Funcionalidad: U-16/A-05 - Repositorio de anuncios] - Consulta de avisos vigentes para usuarios y panel de administración - Conecta con: SystemAnnouncement y AnnouncementService
public interface SystemAnnouncementRepository extends JpaRepository<SystemAnnouncement, Long> {

    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isActive = true AND a.startsAt <= :now AND a.expiresAt >= :now ORDER BY a.startsAt DESC")
    List<SystemAnnouncement> findActiveAnnouncements(@Param("now") LocalDateTime now);
}
