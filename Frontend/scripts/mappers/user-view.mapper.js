/**
 * Convierte datos de la API en el modelo que consume el listado de usuarios.
 * Lógica pura: sin DOM ni red, fácil de probar en consola.
 */
import { MESSAGES } from "../constants/messages.es.js";

/**
 * @typedef {{
 *   username: string,
 *   roleName: string,
 *   roleLabel: string,
 *   permissions: string[],
 * }} UserView
 */

/**
 * Cruza un usuario con el catálogo de roles para obtener sus permisos efectivos:
 * los del rol más los adicionales, sin repetir y ordenados.
 * @param {import("../services/user.service.js").User} user
 * @param {import("../services/role.service.js").Role[]} roles
 * @returns {UserView}
 */
export function toUserView(user, roles) {
  const role = roles.find((item) => item.name === user.role);
  const rolePermissions = role?.permissions ?? [];
  const additionalPermissions = user.additionalPermissions ?? [];

  return {
    username: user.username,
    roleName: user.role,
    roleLabel: MESSAGES.users.roleLabels[user.role] ?? user.role,
    permissions: [...new Set([...rolePermissions, ...additionalPermissions])].sort(),
  };
}

/**
 * @param {import("../services/user.service.js").User[]} users
 * @param {import("../services/role.service.js").Role[]} roles
 * @returns {UserView[]}
 */
export function toUserViews(users, roles) {
  return users.map((user) => toUserView(user, roles));
}
