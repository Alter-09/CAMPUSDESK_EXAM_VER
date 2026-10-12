/**
 * Acceso a los endpoints de usuarios (grupo "Usuarios" del Swagger).
 */
import { apiRequest } from "../config/http-client.js";

/**
 * @typedef {{
 *   username: string,
 *   email: string,
 *   role: string,
 *   locked: boolean,
 *   disabled: boolean,
 *   mustChangePassword: boolean,
 *   additionalPermissions: string[],
 * }} User
 */

/**
 * @typedef {{
 *   username: string,
 *   email: string,
 *   password: string,
 *   role: string,
 *   additionalPermissions?: string[],
 * }} NewUserData
 */

/** Todo usuario nuevo nace activo y con la contraseña provisional por cambiar. */
const NEW_USER_DEFAULTS = Object.freeze({
  locked: false,
  disabled: false,
  mustChangePassword: true,
});

/** GET /api/user/all */
export function listUsers() {
  return apiRequest("/api/user/all");
}

/**
 * POST /api/user/add
 * @param {NewUserData} data
 * @returns {Promise<User>}
 */
export function createUser({ username, email, password, role, additionalPermissions = [] }) {
  return apiRequest("/api/user/add", {
    method: "POST",
    body: { username, email, password, role, additionalPermissions, ...NEW_USER_DEFAULTS },
  });
}
