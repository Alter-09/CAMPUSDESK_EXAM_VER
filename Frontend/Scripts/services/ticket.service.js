/**
 * Servicio para gestión e interacción con el ciclo de vida de tiquetes.
 * Sigue principios SOLID y DRY mediante el cliente HTTP unificado (apiRequest).
 */
import { apiRequest } from "../config/http-client.js";

/**
 * @typedef {{
 *   id: number,
 *   title: string,
 *   description: string,
 *   category: "HARDWARE" | "SOFTWARE" | "NETWORK" | "ACCESS" | "OTHER",
 *   priority: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL",
 *   status: "OPEN" | "ASSIGNED" | "IN_PROGRESS" | "RESOLVED" | "CLOSED",
 *   applicantUsername: string,
 *   technicianUsername?: string,
 *   createdAt: string,
 *   updatedAt: string,
 * }} TicketDto
 */

/**
 * @typedef {{
 *   id: number,
 *   ticketId: number,
 *   content: string,
 *   authorUsername: string,
 *   visibility: "PUBLIC" | "INTERNAL",
 *   createdAt: string,
 * }} CommentDto
 */

/**
 * @typedef {{
 *   id: number,
 *   ticketId: number,
 *   previousStatus?: string,
 *   newStatus: string,
 *   changedBy: string,
 *   action: "CREATION" | "ASSIGNMENT" | "STATUS_TRANSITION" | "CLOSURE",
 *   changedAt: string,
 * }} StatusHistoryDto
 */

/**
 * Consulta la lista de tiquetes visibles para el usuario autenticado.
 * Para técnicos, el backend filtra automáticamente los tiquetes asignados a su cuenta.
 * @param {{ status?: string, priority?: string, category?: string }} [filters]
 * @returns {Promise<TicketDto[]>}
 */
export async function listTickets(filters = {}) {
  const query = new URLSearchParams();
  if (filters.status) query.set("status", filters.status);
  if (filters.priority) query.set("priority", filters.priority);
  if (filters.category) query.set("category", filters.category);

  const queryString = query.toString();
  const path = queryString ? `/api/tickets?${queryString}` : "/api/tickets";
  return apiRequest(path, { method: "GET" });
}

/**
 * Obtiene el detalle completo de un tiquete por su ID.
 * @param {number|string} ticketId
 * @returns {Promise<TicketDto>}
 */
export function getTicketById(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}`, { method: "GET" });
}

/**
 * Ejecuta una transición de estado en el ciclo de vida del tiquete (RF-04).
 * Técnicos: "START_PROGRESS" (de ASSIGNED a IN_PROGRESS), "RESOLVE" (de IN_PROGRESS a RESOLVED).
 * @param {number|string} ticketId
 * @param {"START_PROGRESS" | "RESOLVE" | "CONFIRM_CLOSE" | "ASSIGN" | "REASSIGN"} action
 * @returns {Promise<any>}
 */
export function transitionTicket(ticketId, action) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/transitions`, {
    method: "POST",
    body: { action },
  });
}

/**
 * Asigna por primera vez un técnico a una incidencia abierta (RF-01, RF-04).
 * Endpoint backend Spring Boot: PUT /api/tickets/{ticketId}/assign
 * @param {number|string} ticketId
 * @param {string} technicianUsername
 * @returns {Promise<any>}
 */
export function assignTicket(ticketId, technicianUsername) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/assign`, {
    method: "PUT",
    body: { technicianUsername },
  });
}

/**
 * Reasigna una incidencia asignada a un técnico distinto con justificación (RF-01, RF-04, Reto 3).
 * Endpoint backend Spring Boot: PUT /api/tickets/{ticketId}/reassign
 * @param {number|string} ticketId
 * @param {string} technicianUsername
 * @param {string} [justification]
 * @returns {Promise<any>}
 */
export function reassignTicket(ticketId, technicianUsername, justification = "Reasignación administrativa") {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/reassign`, {
    method: "PUT",
    body: { technicianUsername, justification },
  });
}

/**
 * Consulta la lista de comentarios de un tiquete ordenados cronológicamente (RF-05).
 * @param {number|string} ticketId
 * @returns {Promise<CommentDto[]>}
 */
export function getTicketComments(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/comments`, {
    method: "GET",
  });
}

/**
 * Publica un nuevo comentario en la incidencia (RF-05).
 * @param {number|string} ticketId
 * @param {{ content: string, visibility?: "PUBLIC" | "INTERNAL" }} data
 * @returns {Promise<CommentDto>}
 */
export function addTicketComment(ticketId, { content, visibility = "PUBLIC" }) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/comments`, {
    method: "POST",
    body: { content, visibility },
  });
}

/**
 * Consulta el historial de cambios de estado del tiquete (RF-06).
 * @param {number|string} ticketId
 * @returns {Promise<StatusHistoryDto[]>}
 */
export function getTicketHistory(ticketId) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}/history`, {
    method: "GET",
  });
}

/**
 * Crea un nuevo tiquete de soporte. Solo usuarios con rol USER (RF-03).
 * El estado inicial es establecido automáticamente por el backend como OPEN.
 * @param {{ title: string, description: string, category: string, priority: string }} data
 * @returns {Promise<TicketDto>}
 */
export function createTicket({ title, description, category, priority }) {
  return apiRequest("/api/tickets", {
    method: "POST",
    body: { title, description, category, priority },
  });
}

/**
 * Actualiza los campos de un tiquete. Solo el solicitante mientras esté OPEN y sin técnico (RF-03).
 * @param {number|string} ticketId
 * @param {{ title: string, description: string, category: string, priority: string }} data
 * @returns {Promise<TicketDto>}
 */
export function updateTicket(ticketId, { title, description, category, priority }) {
  return apiRequest(`/api/tickets/${encodeURIComponent(ticketId)}`, {
    method: "PUT",
    body: { title, description, category, priority },
  });
}
