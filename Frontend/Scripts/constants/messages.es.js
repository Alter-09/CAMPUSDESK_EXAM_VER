import { USERS_MESSAGES } from "./messages.users.es.js";
import { ACCESS_MESSAGES } from "./messages.access.es.js";
import { TECHNICIANS_MESSAGES } from "./messages.technicians.es.js";
import { TICKETS_MESSAGES } from "./messages.tickets.es.js";
import { USER_PORTAL_MESSAGES } from "./messages.user.es.js";
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
  technicians: TECHNICIANS_MESSAGES,
  tickets: TICKETS_MESSAGES,
  access: ACCESS_MESSAGES,
  userPortal: USER_PORTAL_MESSAGES,
});