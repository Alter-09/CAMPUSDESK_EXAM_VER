/**
 * Bootstrap de user.html (página de inicio / overview del portal de usuario).
 * Responsabilidad única: inicializar la cabecera con datos de sesión.
 */
import { initUserPage } from "../utils/user-page.utils.js";

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initUserPage, { once: true });
} else {
  initUserPage();
}
