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
