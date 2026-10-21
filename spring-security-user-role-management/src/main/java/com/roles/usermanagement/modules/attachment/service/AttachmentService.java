package com.roles.usermanagement.modules.attachment.service;

import com.roles.usermanagement.modules.attachment.dto.AttachmentResponse;
import com.roles.usermanagement.modules.attachment.entity.TicketAttachment;
import com.roles.usermanagement.modules.attachment.repository.TicketAttachmentRepository;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [Nueva Funcionalidad: U-02/T-11 - Servicio de archivos adjuntos] - Almacena metadatos y lista adjuntos de tickets y comentarios - Conecta con: TicketAttachmentRepository y TicketRepository
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final TicketAttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public AttachmentResponse saveAttachment(Long ticketId, Long commentId, String fileName,
                                             String fileType, Long fileSize, String storagePath, String uploadedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        TicketAttachment attachment = TicketAttachment.builder()
                .ticket(ticket)
                .commentId(commentId)
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .storagePath(storagePath)
                .uploadedBy(uploadedBy)
                .isDeleted(false)
                .build();

        TicketAttachment saved = attachmentRepository.save(attachment);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByTicket(Long ticketId) {
        return attachmentRepository.findByTicketIdAndIsDeletedFalse(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AttachmentResponse toResponse(TicketAttachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getTicket().getId(),
                a.getCommentId(),
                a.getFileName(),
                a.getFileType(),
                a.getFileSize(),
                a.getUploadedBy(),
                a.getCreatedAt()
        );
    }
}
