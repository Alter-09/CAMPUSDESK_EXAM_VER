/**
 * Acceso al catálogo de roles (grupo "Roles y permisos" del Swagger).
 */
import { apiRequest } from "../config/http-client.js";

/** @typedef {{ name: string, permissions: string[] }} Role */

/** Roles que la interfaz nunca ofrece al crear usuarios (ADMIN es el superadmin). */
const PROTECTED_ROLES = Object.freeze(["ADMIN"]);

/** @param {string} roleName */
export function isProtectedRole(roleName) {
  return PROTECTED_ROLES.includes(roleName);
}

/**
 * GET /api/roles
 * @returns {Promise<Role[]>}
 */
export function listRoles() {
  return apiRequest("/api/roles");
}

/**
 * Roles que el superadmin sí puede asignar al crear un usuario.
 * @returns {Promise<Role[]>}
 */
export async function listAssignableRoles() {
  const roles = await listRoles();
  return roles.filter((role) => !isProtectedRole(role.name));
}
