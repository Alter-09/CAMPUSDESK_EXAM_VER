import { apiRequest } from "../config/http-client.js";
const NEW_USER_DEFAULTS = Object.freeze({
  locked: false,
  disabled: false,
  mustChangePassword: true,
});
export function listUsers() {
  return apiRequest("/api/user/all");
}
export function createUser({ username, email, password, role, additionalPermissions = [] }) {
  return apiRequest("/api/user/add", {
    method: "POST",
    body: { username, email, password, role, additionalPermissions, ...NEW_USER_DEFAULTS },
  });
}
export function deleteUser(username) {
  return apiRequest(`/api/user/delete/${encodeURIComponent(username)}`, {
    method: "DELETE",
  });
}

// [Nueva Funcionalidad: U-15 - Consulta de perfil de usuario] - Retorna datos del perfil propio - Conecta con: /api/user/me/profile
export function getUserProfile() {
  return apiRequest("/api/user/me/profile", { method: "GET" });
}

// [Nueva Funcionalidad: U-15 - Actualización de perfil propio] - Permite modificar teléfono y departamento - Conecta con: /api/user/me/profile
export function updateUserProfile({ phoneNumber, department }) {
  return apiRequest("/api/user/me/profile", {
    method: "PUT",
    body: { phoneNumber, department }
  });
}

// [Nueva Funcionalidad: A-22 - Bloqueo masivo de usuarios] - Modifica en lote el estado locked de usuarios - Conecta con: /api/user/bulk-lock
export function bulkLockUsers(usernames, locked) {
  return apiRequest(`/api/user/bulk-lock?locked=${locked}`, {
    method: "POST",
    body: usernames
  });
}