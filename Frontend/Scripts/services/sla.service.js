import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: A-01/T-15 - Servicio de Políticas SLA] - Consulta y configuración de acuerdos de nivel de servicio - Conecta con: /api/v1/sla
export function getSlaPolicies() {
  return apiRequest("/api/v1/sla/policies", { method: "GET" });
}

// [Nueva Funcionalidad: A-01 - Crear o actualizar política SLA] - Guarda tiempos de respuesta y resolución por prioridad - Conecta con: /api/v1/sla/policies
export function saveSlaPolicy({ name, priority, firstResponseHours, resolutionHours, isActive }) {
  return apiRequest("/api/v1/sla/policies", {
    method: "POST",
    body: { name, priority, firstResponseHours, resolutionHours, isActive }
  });
}
