export const TECHNICIANS_MESSAGES = Object.freeze({
  list: Object.freeze({
    eyebrow: "Personal de Soporte",
    title: "Lista de Técnicos",
    loading: "Cargando directorio de técnicos…",
    empty: "No hay técnicos registrados en el sistema.",
    loadError: "No se pudo cargar el directorio de técnicos.",
    retry: "Reintentar",
    columns: Object.freeze({
      username: "Nombre / Usuario",
      email: "Correo Electrónico",
      activeTickets: "Tiquetes Activos",
      status: "Estado",
      actions: "Acciones",
    }),
    statusLabels: Object.freeze({
      AVAILABLE: "Disponible",
      BUSY: "En servicio",
      ON_LEAVE: "De permiso",
      OFFLINE: "Inactivo",
    }),
    deleteConfirmTitle: "Eliminar técnico",
    deleteConfirmMessage: (username) =>
      `¿Estás seguro de que deseas eliminar al técnico «${username}»? Esta acción no se puede deshacer.`,
    deleteSuccess: (username) =>
      `El técnico «${username}» ha sido eliminado correctamente.`,
    deleteError: "No se pudo eliminar el técnico.",
    cancelButton: "Cancelar",
    confirmDeleteButton: "Sí, eliminar",
  }),
});
