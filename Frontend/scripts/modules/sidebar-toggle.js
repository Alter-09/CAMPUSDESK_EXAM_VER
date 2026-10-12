/**
 * Menú lateral deslizable para pantallas menores a 1024px.
 * Responsabilidad única: abrir y cerrar el sidebar y mantener aria-expanded sincronizado.
 * Los estilos viven en layouts/superadmin.css; aquí solo se alterna data-sidebar-open.
 * Los hooks de comportamiento son atributos data-*, no clases, para no mezclarlos con el estilo.
 */

// Debe coincidir con el breakpoint del sidebar fijo en layouts/superadmin.css
const DESKTOP_MEDIA_QUERY = "(min-width: 1024px)";

export function initSidebarToggle() {
  const shell = document.querySelector("[data-shell]");
  const sidebar = document.querySelector("[data-sidebar]");
  const toggleButton = document.querySelector("[data-sidebar-toggle]");

  if (!shell || !sidebar || !toggleButton) {
    return;
  }

  const isOpen = () => shell.hasAttribute("data-sidebar-open");

  function setOpen(open) {
    shell.toggleAttribute("data-sidebar-open", open);
    toggleButton.setAttribute("aria-expanded", String(open));
  }

  toggleButton.addEventListener("click", () => {
    setOpen(!isOpen());

    // El foco entra al menú para que el teclado y los lectores de pantalla lo recorran
    if (isOpen()) {
      sidebar.querySelector("a")?.focus();
    }
  });

  // Cierra al tocar el fondo oscuro o al elegir un enlace del menú
  shell.addEventListener("click", (event) => {
    const clickedBackdrop = event.target.closest("[data-sidebar-backdrop]");
    const clickedLink = event.target.closest("[data-sidebar] a");

    if (clickedBackdrop || clickedLink) {
      setOpen(false);
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && isOpen()) {
      setOpen(false);
      toggleButton.focus();
    }
  });

  // Si la pantalla crece hasta el layout de escritorio, el menú deja de ser un panel superpuesto
  window.matchMedia(DESKTOP_MEDIA_QUERY).addEventListener("change", (event) => {
    if (event.matches) {
      setOpen(false);
    }
  });
}

initSidebarToggle();
