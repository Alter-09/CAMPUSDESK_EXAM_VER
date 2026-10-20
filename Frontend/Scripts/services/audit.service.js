import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: A-18 - Servicio de Auditoría] - Consulta del log inmutable de auditoría para administradores - Conecta con: /api/v1/audit
export function getAuditLogs(page = 0, size = 20) {
  const query = new URLSearchParams({ page, size });
  return apiRequest(`/api/v1/audit?${query.toString()}`, { method: "GET" });
}

// [Nueva Funcionalidad: A-18 - Auditoría por Entidad] - Consulta eventos asociados a una entidad específica (ej: Ticket) - Conecta con: /api/v1/audit/entity/{name}/{id}
export function getEntityAuditLogs(entityName, entityId, page = 0, size = 20) {
  const query = new URLSearchParams({ page, size });
  return apiRequest(`/api/v1/audit/entity/${encodeURIComponent(entityName)}/${encodeURIComponent(entityId)}?${query.toString()}`, {
    method: "GET"
  });
}
