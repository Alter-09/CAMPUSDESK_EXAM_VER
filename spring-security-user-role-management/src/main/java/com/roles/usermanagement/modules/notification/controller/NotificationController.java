package com.roles.usermanagement.modules.notification.controller;

import com.roles.usermanagement.domain.dto.PageResponse;
import com.roles.usermanagement.modules.notification.dto.NotificationResponse;
import com.roles.usermanagement.modules.notification.service.NotificationService;
import com.roles.usermanagement.web.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// [Nueva Funcionalidad: U-10/T-17 - Controlador REST de notificaciones] - Endpoints para listar no leídas, paginar y marcar lectura - Conecta con: NotificationService
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Centro de notificaciones in-app y gestión de lecturas")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread")
    @Operation(summary = "Obtener lista rápida de notificaciones no leídas del usuario autenticado (MOD-42)")
    public ResponseEntity<List<NotificationResponse>> getUnread(Authentication auth) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(auth.getName()));
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones paginadas del usuario autenticado (MOD-42, U-10, T-17)")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        return ResponseEntity.ok(notificationService.getNotifications(auth.getName(), page, size));
    }

    @GetMapping("/count")
    @Operation(summary = "Contador de notificaciones pendientes/no leídas (MOD-42)")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(auth.getName())));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar notificación individual como leída (MOD-42)")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication auth) {
        notificationService.markAsRead(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todas las notificaciones pendientes como leídas (MOD-42)")
    public ResponseEntity<Void> markAllAsRead(Authentication auth) {
        notificationService.markAllAsRead(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
