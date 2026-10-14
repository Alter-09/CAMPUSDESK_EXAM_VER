import { apiRequest } from "../config/http-client.js";
export function listPermissions() {
  return apiRequest("/api/permissions");
}
