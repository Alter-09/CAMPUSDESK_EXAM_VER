package com.roles.usermanagement.modules.attachment.repository;

import com.roles.usermanagement.modules.attachment.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// [Nueva Funcionalidad: U-02/T-11 - Repositorio de adjuntos] - Acceso a datos de archivos adjuntos no eliminados por ticket o comentario - Conecta con: TicketAttachment y AttachmentService
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    List<TicketAttachment> findByTicketIdAndIsDeletedFalse(Long ticketId);
    List<TicketAttachment> findByCommentIdAndIsDeletedFalse(Long commentId);
}
