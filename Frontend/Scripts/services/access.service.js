import { apiRequest } from "../config/http-client.js";
export function assignRole(username, role) {
  return apiRequest("/api/user/assignRole", { method: "POST", body: { username, role } });
}
export function assignPermission(username, permission) {
  return apiRequest("/api/user/assignPermission", { method: "POST", body: { username, permission } });
}
export function removePermission(username, permission) {
  return apiRequest(
    `/api/user/${encodeURIComponent(username)}/permissions/${encodeURIComponent(permission)}`,
    { method: "DELETE" },
  );
}
export async function applyAccessChanges({ username, role, assign, remove }) {
  if (role) {
    await assignRole(username, role);
  }

  const operations = [
    ...assign.map((permission) => ({ permission, action: "assign", run: () => assignPermission(username, permission) })),
    ...remove.map((permission) => ({ permission, action: "remove", run: () => removePermission(username, permission) })),
  ];
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
