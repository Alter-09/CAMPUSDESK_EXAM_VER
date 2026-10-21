import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: U-10/T-17 - Servicio Frontend de Notificaciones] - Consulta no leídas, contador y marcado como leída - Conecta con: /api/v1/notifications
export function getUnreadNotifications() {
  return apiRequest("/api/v1/notifications/unread", { method: "GET" });
}
export const getMyNotifications = getUnreadNotifications;

export function getNotificationsList({ page = 0, size = 10 } = {}) {
  return apiRequest(`/api/v1/notifications?page=${page}&size=${size}`, { method: "GET" });
}

export function getUnreadNotificationCount() {
  return apiRequest("/api/v1/notifications/count", { method: "GET" });
}

export function markNotificationAsRead(id) {
  return apiRequest(`/api/v1/notifications/${encodeURIComponent(id)}/read`, { method: "PATCH" });
}

export function markAllNotificationsAsRead() {
  return apiRequest("/api/v1/notifications/read-all", { method: "PATCH" });
}
