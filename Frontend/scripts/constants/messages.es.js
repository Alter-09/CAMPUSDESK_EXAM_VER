import { USERS_MESSAGES } from "./messages.users.es.js";

/**
 * Textos visibles de la interfaz (español).
 * Fuente única para mensajes — evita literales dispersos (C-03, C-05).
 */
export const MESSAGES = Object.freeze({
  login: Object.freeze({
    pageTitle: "CampusDesk | Iniciar sesión",
    pageDescription:
      "Inicia sesión en CampusDesk para gestionar y reportar tickets de soporte tecnico de manera eficiente y sencilla.",
    brandLabel: "Marca CampusDesk",
    welcome: "Bienvenido a CampusDesk!",
    heading: "Iniciar sesión",
    usernameLabel: "Usuario",
    usernamePlaceholder: "Ejemplo: superadmin",
    passwordLabel: "Contraseña",
    passwordPlaceholder: "Ingresa tu contraseña",
    submit: "Ingresar",
    submitting: "Ingresando…",
    showPassword: "Mostrar contraseña",
    hidePassword: "Ocultar contraseña",
    usernameRequired: "El usuario es obligatorio.",
    passwordRequired: "La contraseña es obligatoria.",
    summaryPrefix: "Revisa los siguientes campos:",
    privateHint: "Acceso privado · Solicita tu cuenta al Administrador",
  }),
  errors: Object.freeze({
    network: "No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.",
    unknown: "Ocurrió un error inesperado. Inténtalo de nuevo.",
    byStatus: Object.freeze({
      401: "Tu sesión no es válida o expiró. Inicia sesión de nuevo.",
      403: "No tienes permiso para realizar esta acción.",
      500: "El servidor tuvo un problema. Inténtalo más tarde.",
    }),
  }),
  users: USERS_MESSAGES,
});