/**
 * Textos de la gestión de usuarios (interfaz en español).
 * Se integra en MESSAGES con: users: USERS_MESSAGES
 */
export const USERS_MESSAGES = Object.freeze({
  roleLabels: Object.freeze({
    ADMIN: "Superadministrador",
    TECHNICIAN: "Soporte Técnico",
    USER: "Usuario / Solicitante",
  }),

  list: Object.freeze({
    eyebrow: "Gestión de accesos",
    title: "Usuarios registrados",
    addButton: "Agregar nuevo Usuario",
    loading: "Cargando directorio de usuarios…",
    empty: "No hay usuarios registrados en el sistema.",
    loadError: "No se pudo cargar el directorio de usuarios.",
    retry: "Reintentar",
    columns: Object.freeze({
      username: "Usuario",
      email: "Correo electrónico",
      role: "Rol",
      status: "Estado",
      actions: "Acciones",
      permissions: "Permisos",
    }),
    noPermissions: "Sin permisos",
    permissionsSummary: (count) => (count === 1 ? "1 permiso" : `${count} permisos`),
    created: (username) => `Usuario «${username}» creado correctamente.`,
    deleteConfirmTitle: "Eliminar usuario",
    deleteConfirmMessage: (username) => `¿Estás seguro de que deseas eliminar al usuario «${username}»? Esta acción no se puede deshacer.`,
    deleteSuccess: (username) => `El usuario «${username}» ha sido eliminado correctamente.`,
    deleteError: "No se pudo eliminar el usuario.",
    cancelButton: "Cancelar",
    confirmDeleteButton: "Sí, eliminar",
  }),

  modal: Object.freeze({
    title: "Registro de Nuevo Usuario",
    close: "Cerrar",
    cancel: "Cancelar",
    submit: "Registrar usuario",
    submitting: "Registrando…",
    loading: "Cargando roles y permisos…",
    loadError: "No se pudieron cargar los roles y permisos.",
    retry: "Reintentar",
    fields: Object.freeze({
      username: Object.freeze({ label: "Nombre de usuario", placeholder: "Ej. ana.gomez" }),
      email: Object.freeze({ label: "Correo institucional", placeholder: "usuario@campusdesk.edu" }),
      role: Object.freeze({ label: "Rol asignado", placeholder: "Selecciona un rol" }),
      password: Object.freeze({ label: "Contraseña provisional", placeholder: "Mínimo 8 caracteres" }),
      permissions: Object.freeze({
        label: "Permisos adicionales (opcional)",
        hint: "El rol ya incluye sus permisos básicos. Aquí solo aparecen los que no trae.",
        chooseRole: "Selecciona un rol para ver los permisos que se pueden agregar.",
        none: "Este rol ya incluye todos los permisos disponibles.",
        selected: (count) => (count === 1 ? "1 seleccionado" : `${count} seleccionados`),
      }),
    }),
    validation: Object.freeze({
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
    }),
  }),
});