package com.roles.usermanagement.modules.notification.repository;

import com.roles.usermanagement.modules.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// [Nueva Funcionalidad: U-10/T-17/A-24 - Repositorio de notificaciones] - Consulta notificaciones paginadas, no leídas y conteo pendiente - Conecta con: Notification y NotificationService
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUsernameAndIsReadFalseOrderByCreatedAtDesc(String recipientUsername);

    Page<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername, Pageable pageable);

    long countByRecipientUsernameAndIsReadFalse(String recipientUsername);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUsername = :username")
    void markAllAsRead(@Param("username") String username);
}
