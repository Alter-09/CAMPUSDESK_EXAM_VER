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
        emailRequired: login.emailRequired,
        emailInvalid: login.emailInvalid,
        passwordRequired: login.passwordRequired,
        summaryPrefix: login.summaryPrefix,
        submitting: login.submitting,
        submit: login.submit,
      },
      onSubmit: async () => {
        // Cablear AuthService cuando el contrato de autenticación (Q-04) esté listo.
      },
    });
  }
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initLoginPage, { once: true });
} else {
  initLoginPage();
}
