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
