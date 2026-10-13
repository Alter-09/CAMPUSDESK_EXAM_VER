/**
 * Acceso al catálogo de permisos (grupo "Roles y permisos" del Swagger).
 */
import { apiRequest } from "../config/http-client.js";

/**
 * GET /api/permissions
 * @returns {Promise<string[]>} Códigos de permiso, ej. "TICKET_CREATE".
 */
export function listPermissions() {
  return apiRequest("/api/permissions");
}
