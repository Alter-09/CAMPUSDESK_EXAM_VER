/**
 * Textos de la gestión de usuarios (interfaz en español).
 * Se integra en MESSAGES con: users: USERS_MESSAGES
 */
export const USERS_MESSAGES = {
  roleLabels: {
    ADMIN: "Superadministrador",
    TECHNICIAN: "Soporte Técnico",
    USER: "Usuario / Solicitante",
  },

  list: {
    eyebrow: "Gestión de accesos",
    title: "Usuarios registrados",
    addButton: "Add new user",
    loading: "Cargando usuarios…",
    empty: "Todavía no hay usuarios registrados.",
    loadError: "No se pudo cargar el listado de usuarios.",
    retry: "Reintentar",
    columns: {
      username: "Usuario",
      role: "Rol",
      permissions: "Permisos",
    },
    noPermissions: "Sin permisos",
    permissionsSummary: (count) => (count === 1 ? "1 permiso" : `${count} permisos`),
    created: (username) => `Usuario «${username}» creado correctamente.`,
  },

  modal: {
    title: "Registro de Nuevo Usuario",
    close: "Cerrar",
    cancel: "Cancelar",
    submit: "Registrar usuario",
    submitting: "Registrando…",
    loading: "Cargando roles y permisos…",
    loadError: "No se pudieron cargar los roles y permisos.",
    retry: "Reintentar",
    fields: {
      username: { label: "Nombre de usuario", placeholder: "Ej. ana.gomez" },
      email: { label: "Correo institucional", placeholder: "usuario@campusdesk.edu" },
      role: { label: "Rol asignado", placeholder: "Selecciona un rol" },
      password: { label: "Contraseña provisional", placeholder: "Mínimo 8 caracteres" },
      permissions: {
        label: "Permisos adicionales (opcional)",
        hint: "El rol ya incluye sus permisos básicos. Aquí solo aparecen los que no trae.",
        chooseRole: "Selecciona un rol para ver los permisos que se pueden agregar.",
        none: "Este rol ya incluye todos los permisos disponibles.",
        selected: (count) => (count === 1 ? "1 seleccionado" : `${count} seleccionados`),
      },
    },
    validation: {
      summaryPrefix: "Revisa los siguientes campos:",
      usernameRequired: "El nombre de usuario es obligatorio.",
      usernameSpaces: "El nombre de usuario no puede contener espacios.",
      usernameShort: (min) => `El nombre de usuario debe tener al menos ${min} caracteres.`,
      emailRequired: "El correo institucional es obligatorio.",
      emailInvalid: "Ingresa un correo válido, por ejemplo usuario@campusdesk.edu.",
      roleRequired: "Selecciona un rol.",
      roleInvalid: "El rol seleccionado no es válido.",
      passwordRequired: "La contraseña provisional es obligatoria.",
      passwordShort: (min) => `La contraseña debe tener al menos ${min} caracteres.`,
    },
  },
};
