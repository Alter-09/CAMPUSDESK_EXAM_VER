import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: U-22/A-23 - Servicio Frontend de Etiquetas] - Consulta y asignación de tags en tickets - Conecta con: /api/v1/tags
export function getAllTags() {
  return apiRequest("/api/v1/tags", { method: "GET" });
}

export function createTag({ name, color }) {
  return apiRequest("/api/v1/tags", {
    method: "POST",
    body: { name, color },
  });
}

export function assignTagToTicket(ticketId, tagId) {
  return apiRequest(`/api/v1/tags/ticket/${encodeURIComponent(ticketId)}/assign/${encodeURIComponent(tagId)}`, {
    method: "POST",
  });
}

export function getTagsByTicket(ticketId) {
  return apiRequest(`/api/v1/tags/ticket/${encodeURIComponent(ticketId)}`, {
    method: "GET",
  });
}
