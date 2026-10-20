package com.roles.usermanagement.modules.attachment.dto;

import java.time.LocalDateTime;

// [Nueva Funcionalidad: U-02/T-11 - DTO de respuesta para archivo adjunto] - Proporciona metadatos de archivos subidos - Conecta con: TicketAttachment
public record AttachmentResponse(
        Long id,
        Long ticketId,
        Long commentId,
        String fileName,
        String fileType,
        Long fileSize,
        String uploadedBy,
        LocalDateTime createdAt
) {
}
