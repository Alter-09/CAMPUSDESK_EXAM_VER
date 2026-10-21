import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: T-02/A-15 - Servicio Frontend de Control de Tiempo] - Imputación y consulta de tiempo trabajado en tickets - Conecta con: /api/v1/timetracking
export function logTicketTime(ticketId, { durationMinutes, description }) {
  return apiRequest(`/api/v1/timetracking/tickets/${encodeURIComponent(ticketId)}/logs`, {
    method: "POST",
    body: { durationMinutes, description },
  });
}

export function getTicketTimeLogs(ticketId) {
  return apiRequest(`/api/v1/timetracking/tickets/${encodeURIComponent(ticketId)}/logs`, {
    method: "GET",
  });
}

export function getTotalTicketMinutes(ticketId) {
  return apiRequest(`/api/v1/timetracking/tickets/${encodeURIComponent(ticketId)}/total-minutes`, {
    method: "GET",
  });
}
