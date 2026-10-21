function setupTechnicianNavigation() {
  const links = document.querySelectorAll(".superadmin-navigation a[href^='#']");
  const assignedSection = document.getElementById("section-assigned-tickets");
  const overviewSection = document.getElementById("section-overview");
  const queueSection = document.getElementById("section-general-queue");
  const breadcrumbCurrent = document.querySelector(".app-header__crumbs [aria-current='page']");
  const headerRole = document.querySelector(".app-header__role");
  const headerAvatar = document.querySelector(".app-header__avatar");
  try {
    const userJson = sessionStorage.getItem("currentUser");
    const storedUsername = sessionStorage.getItem("username");
    let name = storedUsername;
    if (userJson) {
      const userObj = JSON.parse(userJson);
      name = userObj.username || name;
    }
    if (name && headerRole) {
      headerRole.textContent = `Técnico: ${name}`;
    }
    if (name && headerAvatar) {
      headerAvatar.textContent = name.slice(0, 2).toUpperCase();
    }
  } catch (err) {
    console.warn("No se pudo leer datos de usuario en sesión:", err);
  }

  function handleRoute() {
    const hash = window.location.hash || "#overview";

    // Update visual state of sidebar links
    links.forEach((link) => {
      const isTarget = link.getAttribute("href") === hash;
      if (isTarget) {
        link.setAttribute("aria-current", "page");
      } else {
        link.removeAttribute("aria-current");
      }
    });

    // Hide all sections
    if (assignedSection) assignedSection.hidden = true;
    if (overviewSection) overviewSection.hidden = true;
    if (queueSection) queueSection.hidden = true;

    const ticketsComp = document.querySelector("cd-assigned-tickets");

    if (hash === "#overview") {
      if (overviewSection) overviewSection.hidden = false;
      if (breadcrumbCurrent) breadcrumbCurrent.textContent = "Dashboard";
    } else if (hash === "#general-queue") {
      if (queueSection) queueSection.hidden = false;
      if (breadcrumbCurrent) breadcrumbCurrent.textContent = "Cola general";
    } else if (hash === "#resolved-tickets") {
      if (assignedSection) assignedSection.hidden = false;
      if (breadcrumbCurrent) breadcrumbCurrent.textContent = "Historial de Resueltos";
      // Activate resolved filter in component if available
      if (ticketsComp && ticketsComp.shadowRoot) {
        const tab = ticketsComp.shadowRoot.querySelector("[data-filter='RESOLVED']");
        if (tab) tab.click();
      }
    } else {
      // Default: Assigned Tickets
      if (assignedSection) assignedSection.hidden = false;
      if (breadcrumbCurrent) breadcrumbCurrent.textContent = "Tiquetes Asignados";
      if (ticketsComp && ticketsComp.shadowRoot && hash === "#assigned-tickets") {
        const tab = ticketsComp.shadowRoot.querySelector("[data-filter='ALL']");
        if (tab) tab.click();
      }
    }
  }

  const logoutLink = document.querySelector(".superadmin-nav-link--danger");
  if (logoutLink) {
    logoutLink.addEventListener("click", () => {
      sessionStorage.clear();
    });
  }

  window.addEventListener("hashchange", handleRoute);
  handleRoute();
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", setupTechnicianNavigation, { once: true });
} else {
  setupTechnicianNavigation();
}
