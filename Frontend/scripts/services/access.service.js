/**
 * Cambios de rol y permisos individuales sobre un usuario existente
 * (grupo "Usuarios" del Swagger).
 */
import { apiRequest } from "../config/http-client.js";

/**
 * POST /api/user/assignRole — reemplaza el rol y conserva los permisos individuales.
 * @param {string} username
 * @param {string} role
 */
export function assignRole(username, role) {
  return apiRequest("/api/user/assignRole", { method: "POST", body: { username, role } });
}

/**
 * POST /api/user/assignPermission — se suma a los del rol; repetirlo no crea duplicados.
 * @param {string} username
 * @param {string} permission
 */
export function assignPermission(username, permission) {
  return apiRequest("/api/user/assignPermission", { method: "POST", body: { username, permission } });
}

/**
 * DELETE /api/user/{username}/permissions/{permission} — retira un permiso individual;
 * los heredados del rol permanecen.
 * @param {string} username
 * @param {string} permission
 */
export function removePermission(username, permission) {
  return apiRequest(
    `/api/user/${encodeURIComponent(username)}/permissions/${encodeURIComponent(permission)}`,
    { method: "DELETE" },
  );
}

/**
 * @typedef {{ permission: string, action: "assign" | "remove", message: string }} PermissionFailure
 */

/**
 * Aplica los cambios en orden: primero el rol y luego cada permiso individual.
 * - Si falla el cambio de rol, lanza el error y no intenta nada más.
 * - Si falla un permiso, sigue con los demás y lo devuelve en `failures`.
 * Las llamadas son secuenciales a propósito: todas modifican el mismo usuario.
 *
 * @param {{ username: string, role?: string, assign: string[], remove: string[] }} changes
 * @returns {Promise<{ failures: PermissionFailure[] }>}
 */
export async function applyAccessChanges({ username, role, assign, remove }) {
  if (role) {
    await assignRole(username, role);
  }

  const operations = [
    ...assign.map((permission) => ({ permission, action: "assign", run: () => assignPermission(username, permission) })),
    ...remove.map((permission) => ({ permission, action: "remove", run: () => removePermission(username, permission) })),
  ];

  /** @type {PermissionFailure[]} */
  const failures = [];

  for (const { permission, action, run } of operations) {
    try {
      await run();
    } catch (error) {
      failures.push({ permission, action, message: error?.message ?? "" });
    }
  }

  return { failures };
}
