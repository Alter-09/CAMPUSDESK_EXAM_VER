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