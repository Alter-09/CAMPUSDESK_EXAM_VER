import { apiRequest } from "../config/http-client.js";
export async function listTickets(filters = {}) {
  const query = new URLSearchParams();
  if (filters.status) query.set("status", filters.status);
  if (filters.priority) query.set("priority", filters.priority);
  if (filters.category) query.set("category", filters.category);

  const queryString = query.toString();
  const path = queryString ? `/api/tickets?${queryString}` : "/api/tickets";
  return apiRequest(path, { method: "GET" });
}
export function getTicketById(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}`, { method: "GET" });
}
export function transitionTicket(ticketId, action) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/transitions`, {
    method: "POST",
    body: { action },
  });
}
export function assignTicket(ticketId, technicianUsername) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/assign`, {
    method: "PUT",
    body: { technicianUsername },
  });
}
export function reassignTicket(ticketId, technicianUsername, justification = "Reasignación administrativa") {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/reassign`, {
    method: "PUT",
    body: { technicianUsername, justification },
  });
}
export function getTicketComments(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/comments`, {
    method: "GET",
  });
}
export function addTicketComment(ticketId, { content, visibility = "PUBLIC" }) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/comments`, {
    method: "POST",
    body: { content, visibility },
  });
}
export function getTicketHistory(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/history`, {
    method: "GET",
  });
}
export function createTicket({ title, description, category, priority }) {
  return apiRequest("/api/tickets", {
    method: "POST",
    body: { title, description, category, priority },
  });
}
export function updateTicket(ticketId, { title, description, category, priority }) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}`, {
    method: "PUT",
    body: { title, description, category, priority },
  });
}

// [Nueva Funcionalidad: U-01 - Cancelar ticket propio] - Solicita cancelación de un ticket OPEN - Conecta con: /api/tickets/{id}/cancel
export function cancelTicket(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/cancel`, {
    method: "POST"
  });
}

// [Nueva Funcionalidad: U-05 - Reabrir ticket cerrado] - Solicita reapertura con motivo - Conecta con: /api/tickets/{id}/reopen
export function reopenTicket(ticketId, reason = "") {
  const query = reason ? `?reason=${encodeURIComponent(reason)}` : "";
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/reopen${query}`, {
    method: "POST"
  });
}

// [Nueva Funcionalidad: U-13 - Duplicar ticket como plantilla] - Clona un ticket existente - Conecta con: /api/tickets/{id}/clone
export function cloneTicket(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/clone`, {
    method: "POST"
  });
}

// [Nueva Funcionalidad: U-24 - Solicitar escalación de ticket] - Marca el ticket para escalación prioritaria - Conecta con: /api/tickets/{id}/escalate
export function escalateTicket(ticketId, reason = "") {
  const query = reason ? `?reason=${encodeURIComponent(reason)}` : "";
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/escalate${query}`, {
    method: "POST"
  });
}

// [Nueva Funcionalidad: T-27 - Crear ticket a nombre de otro usuario] - Registra ticket a nombre de un solicitante - Conecta con: /api/tickets/on-behalf
export function createTicketOnBehalf({ title, description, category, priority, serviceCatalogId, urgentReason, applicantUsername }) {
  return apiRequest(`/api/tickets/on-behalf?applicantUsername=${encodeURIComponent(applicantUsername)}`, {
    method: "POST",
    body: { title, description, category, priority, serviceCatalogId, urgentReason }
  });
}

// [Nueva Funcionalidad: U-03/U-21/T-21 - Listado paginado de tickets] - Obtiene página de tickets con filtros - Conecta con: /api/tickets/paginated
export function listTicketsPaginated({ page = 0, size = 10, status, priority, category } = {}) {
  const query = new URLSearchParams({ page, size });
  if (status) query.set("status", status);
  if (priority) query.set("priority", priority);
  if (category) query.set("category", category);
  return apiRequest(`/api/tickets/paginated?${query.toString()}`, { method: "GET" });
}

// [Nueva Funcionalidad: T-01 - Auto-asignarse ticket de la cola] - Toma un ticket no asignado - Conecta con: /api/tickets/{id}/self-assign
export function selfAssignTicket(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/self-assign`, {
    method: "POST"
  });
}

// [Nueva Funcionalidad: T-04/A-16 - Auto-asignación por carga activa] - Asigna ticket al técnico óptimo - Conecta con: /api/tickets/{id}/auto-assign
export function autoAssignTicket(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/auto-assign`, {
    method: "POST"
  });
}

// [Nueva Funcionalidad: A-26 - Reasignación masiva de tickets] - Transfiere tickets activos entre técnicos - Conecta con: /api/tickets/bulk-reassign
export function bulkReassignTickets(fromTechnician, toTechnician) {
  const query = new URLSearchParams({ fromTechnician, toTechnician });
  return apiRequest(`/api/tickets/bulk-reassign?${query.toString()}`, {
    method: "POST"
  });
}
