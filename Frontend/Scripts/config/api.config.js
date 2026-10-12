/**
 * Configuración central de la API.
 * Único lugar donde viven la URL base y la clave del token (DRY).
 */

export const API_BASE_URL = "http://localhost:8051";

/** Clave de sessionStorage donde el login guarda el JWT. */
export const TOKEN_STORAGE_KEY = "authToken";
