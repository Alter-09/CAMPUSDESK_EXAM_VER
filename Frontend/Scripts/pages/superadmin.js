// [Nueva Funcionalidad: A-01/A-13/A-18 - Panel de Gestión de Superadministrador] - Gestiona políticas SLA, catálogo de servicios, auditoría de seguridad y vistas del panel admin - Conecta con: sla.service.js, service-catalog.service.js, audit.service.js, auth-guard.js
import { getSlaPolicies, saveSlaPolicy } from "../services/sla.service.js";
import { getActiveCatalogItems, createCatalogItem } from "../services/service-catalog.service.js";
import { getAuditLogs } from "../services/audit.service.js";
import { enforceRoleAccess, isSuperAdmin } from "../modules/auth-guard.js";

// Validar que solo administradores o superadministradores ingresen
enforceRoleAccess(["ADMIN", "SUPERADMIN"]);

/**
 * Adapta la interfaz distinguiendo entre Superadministrador y Administrador Operativo
 */
function adaptSuperadminUI() {
  const isSuper = isSuperAdmin();
  const brandView = document.querySelector(".superadmin-brand__view");
  if (brandView) {
    brandView.textContent = isSuper ? "Vista SuperAdministrador" : "Vista Administrador";
  }

  if (!isSuper) {
    // Ocultar opciones exclusivas del Superadministrador
    const auditLink = document.querySelector('a[href="#audit-logs"]');
    if (auditLink) auditLink.style.display = "none";

    const rolesLink = document.querySelector('a[href="./roleselector.html"]');
    if (rolesLink) rolesLink.style.display = "none";
  }
}

/**
 * Notificación visual tipo Toast
 */
function notify(type, title, message) {
  window.dispatchEvent(
    new CustomEvent("app-toast", {
      detail: { type, title, message },
    })
  );
}

/**
 * Navegación y cambio de pestañas/secciones en el panel de superadmin
 */
function initSectionNavigation() {
  adaptSuperadminUI();

  const sections = {
    overview: document.getElementById("section-overview"),
    "sla-policies": document.getElementById("section-sla-policies"),
    "service-catalog": document.getElementById("section-service-catalog"),
    "audit-logs": document.getElementById("section-audit-logs"),
  };

  const navLinks = document.querySelectorAll('.superadmin-nav-link[href^="#"]');

  function showSection(targetId) {
    const activeKey = targetId.replace("#", "") || "overview";

    if (activeKey === "audit-logs" && !isSuperAdmin()) {
      notify("warning", "Acceso Restringido", "La sección de auditoría está reservada exclusivamente para el Superadministrador.");
      showSection("overview");
      return;
    }

    Object.keys(sections).forEach((key) => {
      const el = sections[key];
      if (el) {
        if (key === activeKey) {
          el.removeAttribute("hidden");
          el.style.display = "block";
        } else {
          el.setAttribute("hidden", "true");
          el.style.display = "none";
        }
      }
    });

    navLinks.forEach((link) => {
      if (link.getAttribute("href") === `#${activeKey}`) {
        link.setAttribute("aria-current", "page");
      } else {
        link.removeAttribute("aria-current");
      }
    });

    // Cargar datos según sección
    if (activeKey === "sla-policies") {
      loadSlaPolicies();
    } else if (activeKey === "service-catalog") {
      loadServiceCatalog();
    } else if (activeKey === "audit-logs") {
      loadAuditLogs();
    }
  }

  navLinks.forEach((link) => {
    link.addEventListener("click", (e) => {
      const href = link.getAttribute("href");
      if (href && href.startsWith("#")) {
        e.preventDefault();
        window.location.hash = href;
        showSection(href);
      }
    });
  });

  // Escuchar cambio de hash en URL
  window.addEventListener("hashchange", () => {
    showSection(window.location.hash || "overview");
  });

  // Sección inicial
  showSection(window.location.hash || "overview");
}

/**
 * Gestión de Políticas SLA (A-01 / A-02 / A-08)
 */
async function loadSlaPolicies() {
  const tbody = document.getElementById("sla-table-body");
  if (!tbody) return;

  tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px;">Cargando políticas SLA...</td></tr>';

  try {
    const policies = await getSlaPolicies();
    if (!policies || policies.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px; color: #64748b;">No hay políticas SLA configuradas aún.</td></tr>';
      return;
    }

    tbody.innerHTML = policies
      .map(
        (p) => `
        <tr>
          <td><strong>${p.priority || "—"}</strong></td>
          <td>${p.name || "Política por defecto"}</td>
          <td>${p.firstResponseHours ?? "—"} hrs</td>
          <td>${p.resolutionHours ?? "—"} hrs</td>
          <td>
            <span class="badge ${p.isActive ? "badge--success" : "badge--secondary"}">
              ${p.isActive ? "Activo" : "Inactivo"}
            </span>
          </td>
          <td>
            <button class="button button--secondary button--sm btn-edit-sla" 
              data-priority="${p.priority}"
              data-name="${p.name || ""}"
              data-response="${p.firstResponseHours || ""}"
              data-resolution="${p.resolutionHours || ""}">
              Editar
            </button>
          </td>
        </tr>
      `
      )
      .join("");

    // Manejar clic en editar
    tbody.querySelectorAll(".btn-edit-sla").forEach((btn) => {
      btn.addEventListener("click", () => {
        const priorityInput = document.getElementById("sla-priority");
        const nameInput = document.getElementById("sla-name");
        const responseInput = document.getElementById("sla-response-hours");
        const resolutionInput = document.getElementById("sla-resolution-hours");

        if (priorityInput) priorityInput.value = btn.dataset.priority;
        if (nameInput) nameInput.value = btn.dataset.name;
        if (responseInput) responseInput.value = btn.dataset.response;
        if (resolutionInput) resolutionInput.value = btn.dataset.resolution;

        document.getElementById("sla-form")?.scrollIntoView({ behavior: "smooth" });
      });
    });
  } catch (err) {
    console.warn("Error cargando políticas SLA:", err);
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #ef4444; padding: 20px;">Error al cargar políticas SLA: ${err.message || err}</td></tr>`;
  }
}

function initSlaForm() {
  const form = document.getElementById("sla-form");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btnSubmit = form.querySelector('button[type="submit"]');
    if (btnSubmit) btnSubmit.disabled = true;

    try {
      const priority = document.getElementById("sla-priority")?.value;
      const name = document.getElementById("sla-name")?.value.trim();
      const firstResponseHours = Number(document.getElementById("sla-response-hours")?.value);
      const resolutionHours = Number(document.getElementById("sla-resolution-hours")?.value);
      const isActive = document.getElementById("sla-is-active")?.checked ?? true;

      await saveSlaPolicy({
        priority,
        name,
        firstResponseHours,
        resolutionHours,
        isActive,
      });

      notify("success", "Política SLA Guardada", `Se actualizó la política para prioridad ${priority}.`);
      form.reset();
      loadSlaPolicies();
    } catch (err) {
      notify("error", "Error al Guardar SLA", err.message || "No se pudo guardar la política SLA.");
    } finally {
      if (btnSubmit) btnSubmit.disabled = false;
    }
  });
}

/**
 * Gestión del Catálogo de Servicios (A-13 / U-01 / U-26)
 */
async function loadServiceCatalog() {
  const container = document.getElementById("catalog-items-grid");
  if (!container) return;

  container.innerHTML = '<p style="grid-column: 1 / -1; text-align: center; padding: 20px;">Cargando catálogo de servicios...</p>';

  try {
    const items = await getActiveCatalogItems();
    if (!items || items.length === 0) {
      container.innerHTML = '<p style="grid-column: 1 / -1; text-align: center; color: #64748b; padding: 20px;">No hay servicios registrados en el catálogo.</p>';
      return;
    }

    container.innerHTML = items
      .map(
        (item) => `
        <div class="profile-card" style="display: flex; flex-direction: column; justify-content: space-between; border-left: 4px solid #2563eb;">
          <div>
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
              <h3 style="margin: 0; font-size: 1.1rem; color: #0f172a;">${item.name}</h3>
              <span class="badge badge--primary" style="font-size: 0.75rem;">${item.category || "General"}</span>
            </div>
            <p style="font-size: 0.88rem; color: #64748b; margin: 0 0 12px 0;">${item.description || "Sin descripción proporcionada."}</p>
          </div>
          <div style="border-top: 1px solid #f1f5f9; padding-top: 10px; display: flex; justify-content: space-between; font-size: 0.8rem; color: #475569;">
            <span>⏱️ SLA estimado: <strong>${item.estimatedHours ? `${item.estimatedHours} hrs` : "N/A"}</strong></span>
            <span>ID: #${item.id}</span>
          </div>
        </div>
      `
      )
      .join("");
  } catch (err) {
    console.warn("Error cargando catálogo de servicios:", err);
    container.innerHTML = `<p style="grid-column: 1 / -1; text-align: center; color: #ef4444; padding: 20px;">Error al cargar catálogo: ${err.message || err}</p>`;
  }
}

function initCatalogForm() {
  const form = document.getElementById("catalog-item-form");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btnSubmit = form.querySelector('button[type="submit"]');
    if (btnSubmit) btnSubmit.disabled = true;

    try {
      const name = document.getElementById("cat-name")?.value.trim();
      const category = document.getElementById("cat-category")?.value;
      const description = document.getElementById("cat-description")?.value.trim();
      const estimatedHours = Number(document.getElementById("cat-hours")?.value) || 24;

      await createCatalogItem({
        name,
        category,
        description,
        icon: "service.svg",
        estimatedHours,
        formSchema: "{}",
      });

      notify("success", "Servicio Creado", `El servicio "${name}" ha sido agregado al catálogo.`);
      form.reset();
      loadServiceCatalog();
    } catch (err) {
      notify("error", "Error al Crear Servicio", err.message || "No se pudo registrar el servicio.");
    } finally {
      if (btnSubmit) btnSubmit.disabled = false;
    }
  });
}

/**
 * Visor de Auditoría de Seguridad (A-18)
 */
async function loadAuditLogs(page = 0) {
  const tbody = document.getElementById("audit-table-body");
  if (!tbody) return;

  tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px;">Cargando registro de auditoría...</td></tr>';

  try {
    const response = await getAuditLogs(page, 20);
    const logs = Array.isArray(response) ? response : response?.content || [];

    if (logs.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px; color: #64748b;">No hay registros de auditoría disponibles.</td></tr>';
      return;
    }

    tbody.innerHTML = logs
      .map((log) => {
        const dateStr = log.timestamp ? new Date(log.timestamp).toLocaleString("es-ES") : "—";
        let actionBadge = "badge--secondary";
        if (log.action?.includes("CREATE")) actionBadge = "badge--success";
        else if (log.action?.includes("DELETE")) actionBadge = "badge--danger";
        else if (log.action?.includes("UPDATE") || log.action?.includes("STATUS")) actionBadge = "badge--warning";

        return `
        <tr>
          <td style="font-size: 0.82rem; color: #64748b; white-space: nowrap;">${dateStr}</td>
          <td><strong>${log.username || "Sistema"}</strong></td>
          <td><span class="badge ${actionBadge}">${log.action || "ACCION"}</span></td>
          <td>${log.entityName || "—"} ${log.entityId ? `#${log.entityId}` : ""}</td>
          <td style="max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${log.details || ""}">
            ${log.details || "—"}
          </td>
          <td style="font-size: 0.8rem; font-family: monospace; color: #475569;">${log.ipAddress || "127.0.0.1"}</td>
        </tr>
      `;
      })
      .join("");
  } catch (err) {
    console.warn("Error cargando logs de auditoría:", err);
    tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #ef4444; padding: 20px;">Error al cargar auditoría: ${err.message || err}</td></tr>`;
  }
}

function initAuditControls() {
  const btnRefresh = document.getElementById("btn-refresh-audit");
  if (btnRefresh) {
    btnRefresh.addEventListener("click", () => {
      loadAuditLogs(0);
      notify("info", "Auditoría", "Registro de auditoría actualizado.");
    });
  }
}

/**
 * Inicialización al cargar el DOM
 */
function initSuperadminPage() {
  initSectionNavigation();
  initSlaForm();
  initCatalogForm();
  initAuditControls();
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initSuperadminPage, { once: true });
} else {
  initSuperadminPage();
}
