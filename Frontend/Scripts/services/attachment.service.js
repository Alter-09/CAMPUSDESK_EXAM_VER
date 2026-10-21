import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: U-02/T-11 - Servicio Frontend de Archivos Adjuntos] - Registro y listado de archivos adjuntos - Conecta con: /api/v1/attachments
export function uploadAttachmentMetadata(ticketId, { fileName, fileType, fileSize, storagePath, commentId }) {
  return apiRequest(`/api/v1/attachments/ticket/${encodeURIComponent(ticketId)}`, {
    method: "POST",
    body: { fileName, fileType, fileSize, storagePath, commentId },
  });
}

export function getAttachmentsForTicket(ticketId) {
  return apiRequest(`/api/v1/attachments/ticket/${encodeURIComponent(ticketId)}`, {
    method: "GET",
  });
}
export const getTicketAttachments = getAttachmentsForTicket;

export function deleteAttachment(attachmentId) {
  return apiRequest(`/api/v1/attachments/${encodeURIComponent(attachmentId)}`, {
    method: "DELETE",
  }).catch(() => null);
}
