/**
 * Bootstrap de create-ticket.html.
 * Responsabilidad única: inicializar la cabecera con datos de sesión.
 * El componente <cd-user-portal mode="create-ticket"> gestiona el formulario.
 */
import { initUserPage } from "../utils/user-page.utils.js";

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initUserPage, { once: true });
} else {
  initUserPage();
}
