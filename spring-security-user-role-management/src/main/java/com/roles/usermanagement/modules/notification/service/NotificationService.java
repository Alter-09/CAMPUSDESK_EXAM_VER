package com.roles.usermanagement.modules.notification.service;

import com.roles.usermanagement.domain.dto.PageResponse;
import com.roles.usermanagement.modules.notification.dto.NotificationResponse;
import com.roles.usermanagement.modules.notification.entity.Notification;
import com.roles.usermanagement.modules.notification.enums.NotificationChannel;
import com.roles.usermanagement.modules.notification.enums.NotificationType;
import com.roles.usermanagement.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: U-10/T-17/A-24 - Servicio de notificaciones] - Crea, lista y gestiona el estado de lectura de notificaciones para usuarios y técnicos - Conecta con: NotificationRepository
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse createNotification(String recipientUsername, String title, String message,
                                                   NotificationType type, Long ticketId) {
        Notification notification = Notification.builder()
                .recipientUsername(recipientUsername)
                .title(title)
                .message(message)
                .notificationType(type)
                .channel(NotificationChannel.IN_APP)
                .ticketId(ticketId)
                .isRead(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(String username) {
        return notificationRepository.findByRecipientUsernameAndIsReadFalseOrderByCreatedAtDesc(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(String username, int page, int size) {
        Page<Notification> p = notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username, PageRequest.of(page, size));
        List<NotificationResponse> content = p.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipientUsernameAndIsReadFalse(username);
    }

    @Transactional
    public void markAsRead(Long id, String username) {
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getRecipientUsername().equals(username)) {
                n.setIsRead(true);
            }
        });
    }

    @Transactional
    public void markAllAsRead(String username) {
        notificationRepository.markAllAsRead(username);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getRecipientUsername(),
                n.getTitle(),
                n.getMessage(),
                n.getNotificationType(),
                n.getChannel(),
                n.getTicketId(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}
