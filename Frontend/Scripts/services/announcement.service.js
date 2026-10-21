import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: U-16/A-05 - Servicio Frontend de Anuncios] - Obtiene avisos activos e institucionales - Conecta con: /api/v1/announcements
export function getActiveAnnouncements() {
  return apiRequest("/api/v1/announcements/active", { method: "GET" });
}

export function createAnnouncement({ title, message, type = "INFO", startsAt, expiresAt }) {
  return apiRequest("/api/v1/announcements", {
    method: "POST",
    body: { title, message, type, startsAt, expiresAt },
  });
}
