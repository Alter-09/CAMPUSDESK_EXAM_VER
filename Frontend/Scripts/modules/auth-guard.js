import { TOKEN_STORAGE_KEY } from "../config/api.config.js";
import { getRedirectUrlForRole } from "../main.js";

// [Nueva Funcionalidad: Seguridad y Control de Acceso por Roles] - Valida autenticación y permisos en cada vista - Conecta con: sessionStorage, localStorage y roles de usuario
export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_STORAGE_KEY)
    || localStorage.getItem(TOKEN_STORAGE_KEY)
    || sessionStorage.getItem("token")
    || localStorage.getItem("token")
    || sessionStorage.getItem("jwt")
    || localStorage.getItem("jwt")
    || "";
}

export function getCurrentUser() {
  return sessionStorage.getItem("username") || localStorage.getItem("username") || "";
}

export function getCurrentRole() {
  const role = sessionStorage.getItem("userRole") || localStorage.getItem("userRole") || "USER";
  return String(role).toUpperCase().trim();
}

export function isSuperAdmin() {
  const current = getCurrentRole();
  const username = getCurrentUser().toLowerCase();
  return current.includes("SUPERADMIN") || username === "superadmin";
}

export function hasRole(...allowedRoles) {
  const current = getCurrentRole();
  // Superadmin has omnipotent access to all views
  if (isSuperAdmin()) {
    return true;
  }
  return allowedRoles.some(r => current.includes(r.toUpperCase()));
}

export function syncHeaderRoleDisplay() {
  const role = getCurrentRole();
  const username = getCurrentUser();
  const roleDisplays = document.querySelectorAll(".app-header__role, #header-role");
  roleDisplays.forEach(el => {
    let roleText = "Usuario";
    if (isSuperAdmin()) {
      roleText = "Superadministrador";
    } else if (role.includes("ADMIN")) {
      roleText = "Administrador";
    } else if (role.includes("TECH") || role.includes("TECNICO") || role.includes("SUPPORT")) {
      roleText = "Técnico";
    }
    el.textContent = username ? `${roleText} (${username})` : roleText;
  });
}

export function enforceRoleAccess(allowedRoles = []) {
  const token = getStoredToken();
  if (!token) {
    window.location.href = "./index.html";
    return false;
  }

  if (allowedRoles.length > 0) {
    const isAllowed = hasRole(...allowedRoles);
    if (!isAllowed) {
      alert("No tienes permisos suficientes para acceder a esta vista.");
      window.location.href = getRedirectUrlForRole(getCurrentRole());
      return false;
    }
  }

  syncHeaderRoleDisplay();
  return true;
}

export function logout() {
  sessionStorage.removeItem(TOKEN_STORAGE_KEY);
  sessionStorage.removeItem("userRole");
  sessionStorage.removeItem("username");
  sessionStorage.removeItem("currentUser");
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem("userRole");
  localStorage.removeItem("username");
  localStorage.removeItem("currentUser");
  window.location.href = "./index.html";
}

if (typeof document !== "undefined") {
  document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.querySelector("#nav-logout, .superadmin-nav-link--danger");
    if (logoutBtn) {
      logoutBtn.addEventListener("click", (e) => {
        e.preventDefault();
        logout();
      });
    }
  });
}

