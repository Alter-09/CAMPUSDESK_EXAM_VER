/**
 * Bootstrap de my-tickets.html.
 * Responsabilidad única: inicializar la cabecera con datos de sesión.
 * El componente <cd-user-portal mode="my-tickets"> gestiona la lista y el modal.
 */
import { initUserPage } from "../utils/user-page.utils.js";

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initUserPage, { once: true });
} else {
  initUserPage();
}
