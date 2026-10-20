import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: T-23/A-27 - Servicio Frontend de Activos IT] - Consulta y vinculación de equipos a tickets - Conecta con: /api/v1/assets
export function getUserAssets(username) {
  return apiRequest(`/api/v1/assets/user/${encodeURIComponent(username)}`, { method: "GET" });
}

export function getTicketAssets(ticketId) {
  return apiRequest(`/api/v1/assets/ticket/${encodeURIComponent(ticketId)}`, { method: "GET" });
}

export function linkAssetToTicket(ticketId, assetId) {
  return apiRequest(`/api/v1/assets/ticket/${encodeURIComponent(ticketId)}/link/${encodeURIComponent(assetId)}`, {
    method: "POST",
  });
}

export function createAsset({ name, serialNumber, category, status, assignedToUsername, location }) {
  return apiRequest("/api/v1/assets", {
    method: "POST",
    body: { name, serialNumber, category, status, assignedToUsername, location },
  });
}
