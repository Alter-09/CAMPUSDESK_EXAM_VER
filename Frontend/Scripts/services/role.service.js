import { apiRequest } from "../config/http-client.js";
const PROTECTED_ROLES = Object.freeze(["ADMIN"]);
export function isProtectedRole(roleName) {
  return PROTECTED_ROLES.includes(roleName);
}
export function listRoles() {
  return apiRequest("/api/roles");
}
export async function listAssignableRoles() {
  const roles = await listRoles();
  return roles.filter((role) => !isProtectedRole(role.name));
}
