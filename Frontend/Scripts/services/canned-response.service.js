import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: T-04/A-19 - Servicio Frontend de Respuestas Predefinidas] - Consulta y creación de plantillas de respuesta rápida - Conecta con: /api/v1/canned-responses
export function getAvailableCannedResponses() {
  return apiRequest("/api/v1/canned-responses", { method: "GET" });
}
export const getActiveCannedResponses = getAvailableCannedResponses;

export function createCannedResponse({ title, content, category, isGlobal }) {
  return apiRequest("/api/v1/canned-responses", {
    method: "POST",
    body: { title, content, category, isGlobal },
  });
}
