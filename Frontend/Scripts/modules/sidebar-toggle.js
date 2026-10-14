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

    if (isOpen()) {
      sidebar.querySelector("a")?.focus();
    }
  });
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
