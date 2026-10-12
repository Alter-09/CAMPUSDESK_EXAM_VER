/**
 * Cliente HTTP único del frontend.
 * Agrega el token, interpreta la respuesta (JSON o texto plano)
 * y convierte cualquier fallo en un ApiError con mensaje listo para mostrar.
 */
import { API_BASE_URL, TOKEN_STORAGE_KEY } from "../config/api.config.js";
import { MESSAGES } from "../constants/messages.es.js";

const JSON_CONTENT_TYPE = "application/json";

export class ApiError extends Error {
  /**
   * @param {number} status Código HTTP (0 si no hubo conexión).
   * @param {string} message Mensaje legible para el usuario.
   */
  constructor(status, message) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

/**
 * El backend responde errores en texto plano (ej. 409 "El usuario ya existe"),
 * pero se contempla también un JSON con message/error.
 * @param {unknown} payload
 * @param {number} status
 * @returns {string}
 */
function extractMessage(payload, status) {
  if (typeof payload === "string" && payload.trim()) {
    return payload.trim();
  }

  if (payload && typeof payload === "object") {
    const message = payload.message ?? payload.error;
    if (typeof message === "string" && message.trim()) {
      return message.trim();
    }
  }

  return MESSAGES.errors.byStatus[status] ?? MESSAGES.errors.unknown;
}

/**
 * @param {Response} response
 * @returns {Promise<unknown>}
 */
async function readPayload(response) {
  const text = await response.text();
  const isJson = (response.headers.get("content-type") ?? "").includes(JSON_CONTENT_TYPE);

  if (!isJson || !text) {
    return text;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

/**
 * @param {string} path Ruta relativa a la API, ej. "/api/user/all".
 * @param {{ method?: string, body?: unknown }} [options]
 * @returns {Promise<any>}
 * @throws {ApiError}
 */
export async function apiRequest(path, { method = "GET", body } = {}) {
  /** @type {Record<string, string>} */
  const headers = {};
  const token = sessionStorage.getItem(TOKEN_STORAGE_KEY);

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  if (body !== undefined) {
    headers["Content-Type"] = JSON_CONTENT_TYPE;
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, MESSAGES.errors.network);
  }

  const payload = await readPayload(response);

  if (!response.ok) {
    throw new ApiError(response.status, extractMessage(payload, response.status));
  }

  return payload;
}
