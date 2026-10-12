/**
 * Textos de la pantalla "Roles y permisos" (interfaz en español).
 * Se integra en MESSAGES con: access: ACCESS_MESSAGES
 * Los códigos de permiso (ej. TICKET_READ_ALL) vienen del backend y se muestran tal cual.
 */
export const ACCESS_MESSAGES = {
  eyebrow: "Control de acceso",
  title: "Asignación de roles y permisos",
  loading: "Cargando usuarios, roles y permisos…",
  loadError: "No se pudo cargar la información de accesos.",
  retry: "Reintentar",
  noUsers: "Todavía no hay usuarios para administrar. Crea uno desde «Crear nuevo usuario».",

  fields: {
    user: { label: "Seleccionar usuario", placeholder: "Selecciona un usuario" },
    role: { label: "Rol a asignar", placeholder: "Selecciona un rol" },
  },

  permissions: {
    title: "Permisos del usuario",
    count: (active, total) => `${active} de ${total} permisos`,
    pickUser: "Selecciona un usuario para ver y ajustar sus permisos.",
    hint: "Los permisos del rol no se pueden quitar aquí. Marca los demás para asignarlos solo a este usuario.",
    fromRole: "Incluido en el rol",
    toggleIndividual: "Asignar de forma individual",
    toggleRedundant: "También asignado de forma individual",
  },

  changes: {
    none: "Sin cambios por guardar.",
    role: (roleLabel) => `cambiar el rol a ${roleLabel}`,
    assign: (count) => (count === 1 ? "asignar 1 permiso individual" : `asignar ${count} permisos individuales`),
    remove: (count) => (count === 1 ? "retirar 1 permiso individual" : `retirar ${count} permisos individuales`),
    summary: (parts) => `Cambios pendientes: ${parts.join(", ")}.`,
  },

  unsaved: {
    title: "Cambios sin guardar",
    text: (username) => `Tienes cambios pendientes para «${username}». Si cambias de usuario, se perderán.`,
    keep: "Seguir editando",
    discard: "Descartar cambios",
  },

  save: "Guardar cambios",
  saving: "Guardando…",
  saved: (username) => `Cambios guardados para «${username}».`,
  partialFailure: (username, failures) =>
    `No se pudieron actualizar algunos permisos de «${username}»: ${failures
      .map(({ permission, message }) => (message ? `${permission} (${message})` : permission))
      .join("; ")}.`,
  refreshFailed: "Los cambios se guardaron, pero no se pudo actualizar la vista. Recarga la página.",
};
