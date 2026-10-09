/**
 * Application bootstrap for the login page.
 */
import { bindPasswordToggle } from "./modules/password-toggle.js";
import { bindLoginForm } from "./modules/login-form.js";

function initLoginPage() {
  const form = document.querySelector("#login-form");
  const passwordInput = document.querySelector("#password");
  const toggleButton = document.querySelector("#toggle-password");

  if (toggleButton instanceof HTMLButtonElement && passwordInput instanceof HTMLInputElement) {
    bindPasswordToggle(toggleButton, passwordInput);
  }

  if (form instanceof HTMLFormElement) {
    bindLoginForm(form, {
      onSubmit: async (credentials) => {
        // Placeholder until the auth API is connected.
        console.info("Login submitted", { username: credentials.username });
      },
    });
  }
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initLoginPage, { once: true });
} else {
  initLoginPage();
}
