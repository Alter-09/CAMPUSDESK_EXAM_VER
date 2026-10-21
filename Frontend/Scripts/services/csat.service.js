import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: U-06/A-21 - Servicio Frontend de Calificación CSAT] - Envío y consulta de encuestas de satisfacción - Conecta con: /api/v1/satisfaction
export function submitCsat(ticketId, { score, feedback }) {
  return apiRequest(`/api/v1/satisfaction/tickets/${encodeURIComponent(ticketId)}`, {
    method: "POST",
    body: { score, feedback },
  });
}

export function getCsatByTicket(ticketId) {
  return apiRequest(`/api/v1/satisfaction/tickets/${encodeURIComponent(ticketId)}`, {
    method: "GET",
  });
}

export function getAverageCsat() {
  return apiRequest("/api/v1/satisfaction/average", {
    method: "GET",
  });
}
