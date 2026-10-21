import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: U-26/A-13 - Servicio Frontend de Catálogo de Servicios] - Consulta servicios IT disponibles para solicitudes - Conecta con: /api/v1/service-catalog
export function getActiveCatalogItems() {
  return apiRequest("/api/v1/service-catalog/items", { method: "GET" });
}

export function getCatalogItemsByCategory(category) {
  return apiRequest(`/api/v1/service-catalog/items/category/${encodeURIComponent(category)}`, {
    method: "GET",
  });
}

export function createCatalogItem({ name, description, category, icon, estimatedHours, formSchema }) {
  return apiRequest("/api/v1/service-catalog/items", {
    method: "POST",
    body: { name, description, category, icon, estimatedHours, formSchema },
  });
}
