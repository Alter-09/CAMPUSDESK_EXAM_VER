import { apiRequest } from "../config/http-client.js";

// [Nueva Funcionalidad: U-08/U-09/U-20 - Servicio Frontend de Base de Conocimiento] - Búsqueda, sugerencias en vivo y artículos públicos - Conecta con: /api/v1/kb
export function getPublishedArticles({ page = 0, size = 10 } = {}) {
  return apiRequest(`/api/v1/kb/articles?page=${page}&size=${size}`, { method: "GET" });
}

export function searchKbArticles(query) {
  return apiRequest(`/api/v1/kb/articles/search?query=${encodeURIComponent(query)}`, { method: "GET" });
}

export function suggestKbArticles(title) {
  return apiRequest(`/api/v1/kb/articles/suggest?title=${encodeURIComponent(title)}`, { method: "GET" });
}

export function getKbArticleById(id) {
  return apiRequest(`/api/v1/kb/articles/${encodeURIComponent(id)}`, { method: "GET" });
}

export function voteKbArticle(id, helpful) {
  return apiRequest(`/api/v1/kb/articles/${encodeURIComponent(id)}/vote?helpful=${helpful}`, { method: "POST" });
}

export function getKbCategories() {
  return apiRequest("/api/v1/kb/categories", { method: "GET" });
}
