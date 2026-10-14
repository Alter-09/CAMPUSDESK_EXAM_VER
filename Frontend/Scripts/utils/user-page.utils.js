export function syncSessionHeader() {
  const headerRole = document.getElementById("header-role");
  const headerAvatar = document.getElementById("header-avatar");

  try {
    const userJson = sessionStorage.getItem("currentUser");
    const storedUsername = sessionStorage.getItem("username");
    let name = storedUsername;

    if (userJson) {
      const userObj = JSON.parse(userJson);
      name = userObj.username || name;
    }

    if (name && headerRole) {
      headerRole.textContent = `Usuario: ${name}`;
    }
    if (name && headerAvatar) {
      headerAvatar.textContent = name.slice(0, 2).toUpperCase();
    }
  } catch (err) {
    console.warn("No se pudo leer datos de usuario de la sesión:", err);
  }
}

/**
 * Enlaza el enlace de cierre de sesión para limpiar sessionStorage al hacer clic.
 */
export function bindLogout() {
  const logoutLink = document.getElementById("nav-logout");
  if (logoutLink) {
    logoutLink.addEventListener("click", () => {
      sessionStorage.clear();
    });
  }
}

/**
 * Inicialización estándar para páginas del portal de usuario.
 * Llama a syncSessionHeader y bindLogout cuando el DOM está listo.
 */
export function initUserPage() {
  syncSessionHeader();
  bindLogout();
}
