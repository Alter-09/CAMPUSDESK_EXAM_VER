/**
 * Calcula qué cambió entre el estado guardado de un usuario y el borrador del formulario.
 * Lógica pura: sin DOM ni red.
 */

/**
 * @typedef {{ role: string, additionalPermissions: string[] }} AccessBaseline
 * @typedef {{ role: string, additionalPermissions: Set<string> }} AccessDraft
 * @typedef {{
 *   roleChanged: boolean,
 *   role: string,
 *   added: string[],
 *   removed: string[],
 *   hasChanges: boolean,
 * }} AccessChanges
 */

/**
 * @param {AccessBaseline} baseline Estado actual en el backend.
 * @param {AccessDraft} draft Estado que el formulario quiere dejar.
 * @returns {AccessChanges}
 */
export function diffAccess(baseline, draft) {
  const saved = new Set(baseline.additionalPermissions);

  const added = [...draft.additionalPermissions].filter((permission) => !saved.has(permission)).sort();
  const removed = baseline.additionalPermissions.filter((permission) => !draft.additionalPermissions.has(permission)).sort();
  const roleChanged = Boolean(draft.role) && draft.role !== baseline.role;

  return {
    roleChanged,
    role: draft.role,
    added,
    removed,
    hasChanges: roleChanged || added.length > 0 || removed.length > 0,
  };
}
