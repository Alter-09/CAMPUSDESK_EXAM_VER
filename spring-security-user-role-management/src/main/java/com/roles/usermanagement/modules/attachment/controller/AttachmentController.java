package com.roles.usermanagement.modules.attachment.controller;

import com.roles.usermanagement.modules.attachment.dto.AttachmentResponse;
import com.roles.usermanagement.modules.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// [Nueva Funcionalidad: U-02/T-11 - Controlador REST de archivos adjuntos] - Endpoints para registrar y listar adjuntos en tickets - Conecta con: AttachmentService
@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/ticket/{ticketId}")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable Long ticketId,
            @RequestBody Map<String, Object> payload,
            Authentication auth) {

        String fileName = (String) payload.getOrDefault("fileName", "archivo.bin");
        String fileType = (String) payload.getOrDefault("fileType", "application/octet-stream");
        Long fileSize = payload.get("fileSize") != null ? Long.valueOf(payload.get("fileSize").toString()) : 1024L;
        String path = (String) payload.getOrDefault("storagePath", "/uploads/" + fileName);
        Long commentId = payload.get("commentId") != null ? Long.valueOf(payload.get("commentId").toString()) : null;

        AttachmentResponse response = attachmentService.saveAttachment(
                ticketId, commentId, fileName, fileType, fileSize, path, auth.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByTicket(ticketId));
    }
}
