/**
 * Arranque de la página de login y enrutamiento según rol.
 */
import { bindPasswordToggle } from "./modules/password-toggle.js";
import { bindLoginForm } from "./modules/login-form.js";
import { MESSAGES } from "./constants/messages.es.js";
import { API_BASE_URL, TOKEN_STORAGE_KEY } from "./config/api.config.js";

/**
 * Determina la URL de redirección según el rol de usuario.
 * @param {string} role
 * @returns {string}
 */
export function getRedirectUrlForRole(role) {
  const upperRole = String(role || "").toUpperCase().trim();

  if (
    upperRole.includes("SUPERADMIN") ||
    upperRole.includes("ADMIN")
  ) {
    return "./superadmin.html";
  }

  if (
    upperRole.includes("TECH") ||
    upperRole.includes("TECNICO") ||
    upperRole.includes("SUPPORT")
  ) {
    return "./technician.html";
  }

  return "./user.html";
}

/**
 * Extrae el rol principal desde diferentes estructuras devueltas por la API.
 * @param {any} data
 * @returns {string}
 */
export function extractRole(data) {
  if (!data) return "";
  if (typeof data === "string") return data;
  if (typeof data.role === "string" && data.role) return data.role;
  if (typeof data.rol === "string" && data.rol) return data.rol;

  if (Array.isArray(data.roles) && data.roles.length > 0) {
    const first = data.roles[0];
    if (typeof first === "string") return first;
    if (first && typeof first === "object") {
      return first.role || first.name || first.authority || "";
    }
  }

  if (Array.isArray(data.authorities) && data.authorities.length > 0) {
    const first = data.authorities[0];
    if (typeof first === "string") return first;
    if (first && typeof first === "object") {
      return first.authority || first.name || "";
    }
  }

  return "";
}

/**
 * Muestra mensaje de error en el cuadro de alerta del formulario.
 * @param {HTMLFormElement} form
 * @param {string} message
 */
function showFormError(form, message) {
  const summary = form.querySelector("#login-form-summary");
  if (summary instanceof HTMLElement) {
    summary.textContent = message;
    summary.classList.add("is-visible");
  } else {
    alert(message);
  }
}

function initLoginPage() {
  const form = document.querySelector("#login-form");
  const passwordInput = document.querySelector("#password");
  const toggleButton = document.querySelector("#toggle-password");
  const { login, errors } = MESSAGES;

  if (toggleButton instanceof HTMLButtonElement && passwordInput instanceof HTMLInputElement) {
    toggleButton.dataset.labelShow = login.showPassword;
    toggleButton.dataset.labelHide = login.hidePassword;
    bindPasswordToggle(toggleButton, passwordInput);
  }

  if (form instanceof HTMLFormElement) {
    bindLoginForm(form, {
      messages: {
        usernameRequired: login.usernameRequired,
        passwordRequired: login.passwordRequired,
        summaryPrefix: login.summaryPrefix,
        submitting: login.submitting,
        submit: login.submit,
      },
      onSubmit: async (credentials) => {
        try {
          const loginPayload = {
            username: credentials.username,
            email: credentials.username,
            password: credentials.password,
            identifier: credentials.username
          };

          let response;
          try {
            response = await fetch(`${API_BASE_URL}/api/auth/login`, {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify(loginPayload),
            });
          } catch (networkError) {
            console.warn("Backend no disponible por red:", networkError);
            // Modo offline/demostración si el backend no está disponible
            const lowerUser = credentials.username.toLowerCase();
            let fallbackRole = "USER";
            if (lowerUser.includes("superadmin") || lowerUser.includes("admin")) {
              fallbackRole = "ADMIN";
            } else if (lowerUser.includes("tech") || lowerUser.includes("tecnico")) {
              fallbackRole = "TECHNICIAN";
            }
            sessionStorage.setItem("userRole", fallbackRole);
            sessionStorage.setItem("username", credentials.username);
            window.location.href = getRedirectUrlForRole(fallbackRole);
            return;
          }

          if (!response.ok) {
            let errorMsg = "Usuario o contraseña incorrectos.";
            try {
              const errData = await response.json();
              if (errData && errData.error) {
                errorMsg = errData.error;
              } else if (errData && errData.message) {
                errorMsg = errData.message;
              }
            } catch {
              // Mantener mensaje por defecto si no es json
            }
            showFormError(form, errorMsg);
            return;
          }

          const dataResult = await response.json();
          const token = dataResult.token || dataResult.accessToken || dataResult.jwt || "";

          if (token) {
            sessionStorage.setItem(TOKEN_STORAGE_KEY, token);
          }

          let role = extractRole(dataResult);
          let userProfile = null;

          // Consultar /api/auth/me para obtener rol y permisos del usuario autenticado
          if (token) {
            try {
              const profileResponse = await fetch(`${API_BASE_URL}/api/auth/me`, {
                headers: {
                  "Authorization": `Bearer ${token}`
                }
              });

              if (profileResponse.ok) {
                userProfile = await profileResponse.json();
                const fetchedRole = extractRole(userProfile);
                if (fetchedRole) {
                  role = fetchedRole;
                }
              }
            } catch (profileError) {
              console.warn("No se pudo obtener información adicional de perfil:", profileError);
            }
          }

          // Guardar información relevante en sessionStorage
          if (role) {
            sessionStorage.setItem("userRole", role);
          }
          if (userProfile) {
            sessionStorage.setItem("currentUser", JSON.stringify(userProfile));
            if (userProfile.username) {
              sessionStorage.setItem("username", userProfile.username);
            }
          } else if (credentials.username) {
            sessionStorage.setItem("username", credentials.username);
          }

          // Redirección según rol:
          // ADMIN / SUPERADMIN -> superadmin.html
          // TECHNICIAN -> technician.html
          // USER -> user.html
          const targetUrl = getRedirectUrlForRole(role);
          window.location.href = targetUrl;

        } catch (error) {
          console.error("Error al iniciar sesión:", error);
          showFormError(form, errors?.unknown || "Ocurrió un error inesperado. Inténtalo de nuevo.");
        }
      },
    });
  }
}

if (typeof document !== "undefined") {
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initLoginPage, { once: true });
  } else {
    initLoginPage();
  }
}