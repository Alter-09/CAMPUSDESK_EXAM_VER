/**
 * Arranque de la página de login.
 */
import { bindPasswordToggle } from "./modules/password-toggle.js";
import { bindLoginForm } from "./modules/login-form.js";
import { MESSAGES } from "./constants/messages.es.js";
console.log("Mensajes importados con éxito:", MESSAGES);

function initLoginPage() {
  const form = document.querySelector("#login-form");
  const passwordInput = document.querySelector("#password");
  const toggleButton = document.querySelector("#toggle-password");
  const { login } = MESSAGES;

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
          const response = await fetch("http://localhost:8051/api/auth/login", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(credentials),
          });

          if (!response.ok) {
            throw new Error("Credenciales inválidas");
          }

          const contentType = response.headers.get("content-type");
          let token = "";

          if (contentType && contentType.includes("application/json")) {
            const data = await response.json();
            token = data.token || data.accessToken || data;
          } else {
            token = await response.text();
          }

          if (token) {
            sessionStorage.setItem("authToken", token);
          }

          // Redirección al panel de Superadmin
          window.location.href = "./superadmin.html";
        } catch (error) {
          console.error("Error al iniciar sesión:", error);
          alert("Usuario o contraseña incorrectos.");
        }
      },
    });
  }
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initLoginPage, { once: true });
} else {
  initLoginPage();
}