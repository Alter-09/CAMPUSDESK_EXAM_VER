/**
 * <cd-user-portal>: Componente Web con Shadow DOM para el portal de usuario.
 * Modo 'create-ticket': formulario para crear nuevas solicitudes (RF-03).
 * Modo 'my-tickets': cuadrícula de tiquetes propios con detalle completo (RF-01 Usuario).
 *
 * Principios: SOLID · DRY · Mobile-First · Web Components + Shadow DOM.
 */

import { MESSAGES } from "../constants/messages.es.js";
import {
  listTickets,
  getTicketById,
  createTicket,
  updateTicket,
  transitionTicket,
  getTicketComments,
  addTicketComment,
  getTicketHistory,
} from "../services/ticket.service.js";
import { attachStyles } from "../utils/shadow-styles.js";

const TEXT = MESSAGES.userPortal;
const TICKET_TEXT = MESSAGES.tickets;

// ─── Utilidades puras ───────────────────────────────────────────────────────

function formatDate(isoString) {
  if (!isoString) return "—";
  try {
    const date = new Date(isoString);
    if (Number.isNaN(date.getTime())) return isoString;
    return new Intl.DateTimeFormat("es-ES", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  } catch {
    return isoString;
  }
}

const STATUS_CLASSES = {
  OPEN:        "badge--status-open",
  ASSIGNED:    "badge--status-assigned",
  IN_PROGRESS: "badge--status-in-progress",
  RESOLVED:    "badge--status-resolved",
  CLOSED:      "badge--status-closed",
};

const PRIO_CLASSES = {
  LOW:      "badge--prio-low",
  MEDIUM:   "badge--prio-medium",
  HIGH:     "badge--prio-high",
  CRITICAL: "badge--prio-critical",
};

// ─── Plantillas HTML ────────────────────────────────────────────────────────

function buildCreateTicketTemplate() {
  return `
    <section class="portal-section" aria-labelledby="create-heading">
      <header class="portal-header">
        <p class="portal-header__eyebrow">${TEXT.createTicket.eyebrow}</p>
        <h1 id="create-heading" class="portal-header__title">${TEXT.createTicket.title}</h1>
        <p class="portal-header__subtitle">${TEXT.createTicket.subtitle}</p>
      </header>

      <div class="create-form-panel" id="create-form-panel">
        <form id="create-ticket-form" novalidate autocomplete="off">
          <div class="form-grid">
            <div class="field form-grid__full">
              <label class="field__label field__label--required" for="ct-title">
                ${TEXT.createTicket.fieldTitle}
              </label>
              <input
                class="field__input"
                type="text"
                id="ct-title"
                name="title"
                placeholder="${TEXT.createTicket.fieldTitlePlaceholder}"
                maxlength="150"
                required
                autocomplete="off"
              />
              <span class="char-count" id="ct-title-count">0 / 150</span>
              <span class="field__error" id="ct-title-error" role="alert" aria-live="polite"></span>
            </div>

            <div class="field form-grid__full">
              <label class="field__label field__label--required" for="ct-description">
                ${TEXT.createTicket.fieldDescription}
              </label>
              <textarea
                class="field__textarea"
                id="ct-description"
                name="description"
                rows="5"
                placeholder="${TEXT.createTicket.fieldDescriptionPlaceholder}"
                maxlength="2000"
                required
              ></textarea>
              <span class="char-count" id="ct-desc-count">0 / 2000</span>
              <span class="field__error" id="ct-desc-error" role="alert" aria-live="polite"></span>
            </div>

            <div class="field">
              <label class="field__label field__label--required" for="ct-category">
                ${TEXT.createTicket.fieldCategory}
              </label>
              <select class="field__select" id="ct-category" name="category" required>
                <option value="">${TEXT.createTicket.selectDefault}</option>
                <option value="HARDWARE">${TEXT.createTicket.categoryHardware}</option>
                <option value="SOFTWARE">${TEXT.createTicket.categorySoftware}</option>
                <option value="NETWORK">${TEXT.createTicket.categoryNetwork}</option>
                <option value="ACCESS">${TEXT.createTicket.categoryAccess}</option>
                <option value="OTHER">${TEXT.createTicket.categoryOther}</option>
              </select>
              <span class="field__error" id="ct-category-error" role="alert" aria-live="polite"></span>
            </div>

            <div class="field">
              <label class="field__label field__label--required" for="ct-priority">
                ${TEXT.createTicket.fieldPriority}
              </label>
              <select class="field__select" id="ct-priority" name="priority" required>
                <option value="">${TEXT.createTicket.selectDefault}</option>
                <option value="LOW">${TEXT.createTicket.priorityLow}</option>
                <option value="MEDIUM">${TEXT.createTicket.priorityMedium}</option>
                <option value="HIGH">${TEXT.createTicket.priorityHigh}</option>
                <option value="CRITICAL">${TEXT.createTicket.priorityCritical}</option>
              </select>
              <span class="field__error" id="ct-priority-error" role="alert" aria-live="polite"></span>
            </div>
          </div>

          <div class="form-actions" style="margin-top: 1.5rem;">
            <span class="field__error" id="ct-global-error" role="alert" aria-live="assertive"></span>
            <button type="submit" class="button button--primary" id="ct-submit-btn">
              <span>${TEXT.createTicket.submit}</span>
            </button>
          </div>
        </form>

        <!-- Estado de éxito (oculto por defecto) -->
        <div class="success-state" id="success-state" hidden>
          <span class="success-state__icon">✅</span>
          <h2 class="success-state__heading">${TEXT.createTicket.successHeading}</h2>
          <p class="success-state__message">${TEXT.createTicket.successMessage}</p>
          <button class="button button--secondary" id="ct-new-btn" type="button">
            ${TEXT.createTicket.successNewBtn}
          </button>
        </div>
      </div>
    </section>
  `;
}

function buildMyTicketsTemplate() {
  return `
    <section class="portal-section" aria-labelledby="my-tickets-heading">
      <header class="portal-header">
        <p class="portal-header__eyebrow">${TEXT.myTickets.eyebrow}</p>
        <h1 id="my-tickets-heading" class="portal-header__title">${TEXT.myTickets.title}</h1>
        <p class="portal-header__subtitle">${TEXT.myTickets.subtitle}</p>

        <div class="my-tickets-controls">
          <div class="filter-tabs" role="tablist" aria-label="Filtrar solicitudes por estado">
            <button class="filter-tab is-active" type="button" role="tab" data-filter="ALL" aria-selected="true">
              ${TEXT.myTickets.filterAll}
            </button>
            <button class="filter-tab" type="button" role="tab" data-filter="OPEN" aria-selected="false">
              ${TEXT.myTickets.filterOpen}
            </button>
            <button class="filter-tab" type="button" role="tab" data-filter="IN_PROGRESS" aria-selected="false">
              ${TEXT.myTickets.filterInProgress}
            </button>
            <button class="filter-tab" type="button" role="tab" data-filter="RESOLVED" aria-selected="false">
              ${TEXT.myTickets.filterResolved}
            </button>
            <button class="filter-tab" type="button" role="tab" data-filter="CLOSED" aria-selected="false">
              ${TEXT.myTickets.filterClosed}
            </button>
          </div>
          <div class="search-input-wrap">
            <input
              type="search"
              class="search-input"
              id="mt-search"
              placeholder="${TEXT.myTickets.searchPlaceholder}"
              aria-label="Buscar solicitud"
            />
          </div>
        </div>
      </header>

      <div id="mt-body" aria-busy="true">
        <p class="portal-message" id="mt-status-msg" role="status">${TEXT.myTickets.loading}</p>
        <button class="button button--secondary" id="mt-retry-btn" type="button" hidden>
          ${TEXT.myTickets.retry}
        </button>
        <div class="tickets-grid" id="mt-grid" role="region" aria-label="Mis tiquetes"></div>
      </div>

      <!-- Modal de detalle del tiquete -->
      <dialog class="ticket-modal" id="mt-modal" aria-labelledby="mt-modal-title">
        <div class="modal-layout">
          <header class="modal-header">
            <div class="modal-header__main">
              <div class="modal-header__eyebrow">
                <span id="mt-modal-code" class="ticket-card__code">#—</span>
                <span id="mt-modal-status" class="badge"></span>
                <span id="mt-modal-priority" class="badge"></span>
                <span id="mt-modal-category" class="badge badge--category"></span>
              </div>
              <h2 id="mt-modal-title" class="modal-header__title">Cargando…</h2>
            </div>
            <button class="modal-close-btn" id="mt-modal-close" type="button" aria-label="Cerrar">✕</button>
          </header>

          <div class="modal-body">
            <!-- Descripción y metadatos -->
            <section class="detail-panel" aria-labelledby="mt-desc-heading">
              <h3 id="mt-desc-heading" class="detail-panel__heading">${TEXT.detail.sectionGeneral}</h3>
              <p class="detail-panel__text" id="mt-modal-desc"></p>
              <div class="metadata-grid">
                <div class="metadata-item">
                  <span class="metadata-item__label">${TEXT.detail.requesterLabel}</span>
                  <span class="metadata-item__value" id="mt-modal-applicant">—</span>
                </div>
                <div class="metadata-item">
                  <span class="metadata-item__label">${TEXT.detail.technicianLabel}</span>
                  <span class="metadata-item__value" id="mt-modal-tech">—</span>
                </div>
                <div class="metadata-item">
                  <span class="metadata-item__label">${TEXT.detail.createdAtLabel}</span>
                  <span class="metadata-item__value" id="mt-modal-created">—</span>
                </div>
                <div class="metadata-item">
                  <span class="metadata-item__label">${TEXT.detail.updatedAtLabel}</span>
                  <span class="metadata-item__value" id="mt-modal-updated">—</span>
                </div>
              </div>
            </section>

            <!-- Estado y acción del ciclo de vida (usuario: CONFIRM_CLOSE) -->
            <section class="detail-panel" aria-labelledby="mt-lifecycle-heading">
              <h3 id="mt-lifecycle-heading" class="detail-panel__heading">${TEXT.detail.sectionLifecycle}</h3>
              <div class="lifecycle-actions" id="mt-lifecycle-actions"></div>
            </section>

            <!-- Edición de solicitud (solo si OPEN y sin técnico) -->
            <section class="detail-panel" id="mt-edit-panel" aria-labelledby="mt-edit-heading" hidden>
              <h3 id="mt-edit-heading" class="detail-panel__heading">${TEXT.detail.sectionEditTicket}</h3>
              <form class="edit-form" id="mt-edit-form" novalidate>
                <div class="edit-form-grid">
                  <div class="field edit-form-grid__full">
                    <label class="field__label" for="mt-edit-title">Título</label>
                    <input class="field__input" type="text" id="mt-edit-title" maxlength="150" required />
                  </div>
                  <div class="field edit-form-grid__full">
                    <label class="field__label" for="mt-edit-desc">Descripción</label>
                    <textarea class="field__textarea" id="mt-edit-desc" rows="4" maxlength="2000" required></textarea>
                  </div>
                  <div class="field">
                    <label class="field__label" for="mt-edit-category">Categoría</label>
                    <select class="field__select" id="mt-edit-category" required>
                      <option value="HARDWARE">Hardware</option>
                      <option value="SOFTWARE">Software</option>
                      <option value="NETWORK">Redes</option>
                      <option value="ACCESS">Accesos</option>
                      <option value="OTHER">Otros</option>
                    </select>
                  </div>
                  <div class="field">
                    <label class="field__label" for="mt-edit-priority">Prioridad</label>
                    <select class="field__select" id="mt-edit-priority" required>
                      <option value="LOW">Baja</option>
                      <option value="MEDIUM">Media</option>
                      <option value="HIGH">Alta</option>
                      <option value="CRITICAL">Crítica</option>
                    </select>
                  </div>
                </div>
                <div style="display:flex;justify-content:flex-end;margin-top:0.75rem;">
                  <button type="submit" class="button button--primary" id="mt-edit-save">
                    ${TEXT.detail.editSave}
                  </button>
                </div>
                <span class="field__error" id="mt-edit-error" role="alert" aria-live="polite"></span>
              </form>
            </section>

            <!-- Comentarios -->
            <section class="detail-panel" aria-labelledby="mt-comments-heading">
              <h3 id="mt-comments-heading" class="detail-panel__heading">${TEXT.detail.sectionComments}</h3>
              <div class="comments-feed" id="mt-comments-feed"></div>
              <form class="comment-form" id="mt-comment-form">
                <textarea
                  class="comment-textarea"
                  id="mt-comment-input"
                  rows="3"
                  placeholder="${TEXT.detail.commentPlaceholder}"
                  required
                ></textarea>
                <div class="comment-form-footer">
                  <button class="button button--secondary" type="submit" id="mt-submit-comment">
                    ${TEXT.detail.submitComment}
                  </button>
                </div>
              </form>
            </section>

            <!-- Historial de cambios -->
            <section class="detail-panel" aria-labelledby="mt-history-heading">
              <h3 id="mt-history-heading" class="detail-panel__heading">${TEXT.detail.sectionHistory}</h3>
              <div class="history-timeline" id="mt-history-timeline"></div>
            </section>
          </div>
        </div>
      </dialog>
    </section>
  `;
}

// ─── Componente Web ──────────────────────────────────────────────────────────

export class UserPortal extends HTMLElement {
  /** @type {"create-ticket"|"my-tickets"} */
  #mode = "create-ticket";

  #root;
  #initialized = null;

  // Estado: Mis Tiquetes
  #tickets = [];
  #activeFilter = "ALL";
  #searchQuery = "";
  #currentTicket = null;

  constructor() {
    super();
    this.#root = this.attachShadow({ mode: "open" });
  }

  static get observedAttributes() {
    return ["mode"];
  }

  attributeChangedCallback(name, _old, value) {
    if (name === "mode" && (value === "create-ticket" || value === "my-tickets")) {
      this.#mode = value;
      if (this.#initialized) {
        // Ya fue inicializado; re-renderizar
        this.#initialized = null;
        this.#root.innerHTML = "";
        this.#initialize();
      }
    }
  }

  connectedCallback() {
    this.#mode = this.getAttribute("mode") === "my-tickets" ? "my-tickets" : "create-ticket";
    this.#initialized ??= this.#initialize();
  }

  /** Permite que el módulo de navegación fuerce una recarga (e.g. al volver a la sección) */
  async reload() {
    await this.#initialized;
    if (this.#mode === "my-tickets") {
      return this.#loadMyTickets();
    }
  }

  // ─── Inicialización ────────────────────────────────────────────────────────

  async #initialize() {
    await attachStyles(this.#root, "user-portal.css");

    const template = document.createElement("template");
    template.innerHTML =
      this.#mode === "my-tickets" ? buildMyTicketsTemplate() : buildCreateTicketTemplate();
    this.#root.append(template.content);

    if (this.#mode === "my-tickets") {
      this.#setupMyTickets();
    } else {
      this.#setupCreateTicket();
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // MODO: CREAR TIQUETE
  // ──────────────────────────────────────────────────────────────────────────

  #setupCreateTicket() {
    const root = this.#root;
    const form = root.getElementById("create-ticket-form");
    const titleInput = root.getElementById("ct-title");
    const descInput = root.getElementById("ct-description");
    const titleCount = root.getElementById("ct-title-count");
    const descCount = root.getElementById("ct-desc-count");
    const submitBtn = root.getElementById("ct-submit-btn");
    const successState = root.getElementById("success-state");
    const newBtn = root.getElementById("ct-new-btn");

    // Contadores de caracteres en tiempo real
    titleInput.addEventListener("input", () => {
      const len = titleInput.value.length;
      titleCount.textContent = `${len} / 150`;
      titleCount.className = `char-count${len > 130 ? " char-count--warn" : ""}`;
      this.#clearFieldError("ct-title-error");
    });

    descInput.addEventListener("input", () => {
      const len = descInput.value.length;
      descCount.textContent = `${len} / 2000`;
      descCount.className = `char-count${len > 1800 ? " char-count--warn" : ""}`;
      this.#clearFieldError("ct-desc-error");
    });

    root.getElementById("ct-category").addEventListener("change", () =>
      this.#clearFieldError("ct-category-error")
    );
    root.getElementById("ct-priority").addEventListener("change", () =>
      this.#clearFieldError("ct-priority-error")
    );

    // Envío del formulario
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      this.#clearAllCreateErrors();

      const title = titleInput.value.trim();
      const description = descInput.value.trim();
      const category = root.getElementById("ct-category").value;
      const priority = root.getElementById("ct-priority").value;

      // Validaciones front-end
      let hasError = false;
      if (title.length < 5 || title.length > 150) {
        this.#setFieldError("ct-title-error", TEXT.createTicket.validationTitle);
        hasError = true;
      }
      if (description.length < 10 || description.length > 2000) {
        this.#setFieldError("ct-desc-error", TEXT.createTicket.validationDescription);
        hasError = true;
      }
      if (!category) {
        this.#setFieldError("ct-category-error", TEXT.createTicket.validationCategory);
        hasError = true;
      }
      if (!priority) {
        this.#setFieldError("ct-priority-error", TEXT.createTicket.validationPriority);
        hasError = true;
      }
      if (hasError) return;

      // Envío a la API
      submitBtn.disabled = true;
      submitBtn.querySelector("span").textContent = TEXT.createTicket.submitting;

      try {
        await createTicket({ title, description, category, priority });
        form.hidden = true;
        successState.hidden = false;
      } catch (err) {
        this.#setFieldError("ct-global-error", err.message || TEXT.createTicket.errorSend);
      } finally {
        submitBtn.disabled = false;
        submitBtn.querySelector("span").textContent = TEXT.createTicket.submit;
      }
    });

    // Botón «Crear otra solicitud»
    newBtn.addEventListener("click", () => {
      form.reset();
      titleCount.textContent = "0 / 150";
      descCount.textContent = "0 / 2000";
      successState.hidden = true;
      form.hidden = false;
    });
  }

  #setFieldError(id, msg) {
    const el = this.#root.getElementById(id);
    if (el) el.textContent = msg;
  }

  #clearFieldError(id) {
    const el = this.#root.getElementById(id);
    if (el) el.textContent = "";
  }

  #clearAllCreateErrors() {
    ["ct-title-error", "ct-desc-error", "ct-category-error", "ct-priority-error", "ct-global-error"]
      .forEach((id) => this.#clearFieldError(id));
  }

  // ──────────────────────────────────────────────────────────────────────────
  // MODO: MIS TIQUETES
  // ──────────────────────────────────────────────────────────────────────────

  #setupMyTickets() {
    const root = this.#root;

    // Filtros por pestaña
    const tabs = root.querySelectorAll(".filter-tab");
    tabs.forEach((tab) => {
      tab.addEventListener("click", () => {
        tabs.forEach((t) => { t.classList.remove("is-active"); t.setAttribute("aria-selected", "false"); });
        tab.classList.add("is-active");
        tab.setAttribute("aria-selected", "true");
        this.#activeFilter = tab.dataset.filter || "ALL";
        this.#renderMyTicketsGrid();
      });
    });

    // Búsqueda
    root.getElementById("mt-search").addEventListener("input", (e) => {
      this.#searchQuery = e.target.value.toLowerCase().trim();
      this.#renderMyTicketsGrid();
    });

    // Reintentar
    root.getElementById("mt-retry-btn").addEventListener("click", () => this.#loadMyTickets());

    // Cierre del modal
    root.getElementById("mt-modal-close").addEventListener("click", () => this.#closeModal());
    root.getElementById("mt-modal").addEventListener("click", (e) => {
      if (e.target === root.getElementById("mt-modal")) this.#closeModal();
    });

    // Comentario
    root.getElementById("mt-comment-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.#submitComment();
    });

    // Edición
    root.getElementById("mt-edit-form").addEventListener("submit", (e) => {
      e.preventDefault();
      this.#saveEdit();
    });

    this.#loadMyTickets();
  }

  async #loadMyTickets() {
    const root = this.#root;
    const msgEl = root.getElementById("mt-status-msg");
    const retryBtn = root.getElementById("mt-retry-btn");
    const grid = root.getElementById("mt-grid");

    msgEl.textContent = TEXT.myTickets.loading;
    msgEl.hidden = false;
    retryBtn.hidden = true;
    grid.innerHTML = "";

    try {
      this.#tickets = await listTickets();
      this.#renderMyTicketsGrid();
    } catch (err) {
      console.error("Error al cargar solicitudes del usuario:", err);
      msgEl.textContent = `${TEXT.myTickets.loadError} ${err.message || ""}`;
      msgEl.hidden = false;
      retryBtn.hidden = false;
    }
  }

  #renderMyTicketsGrid() {
    const root = this.#root;
    const grid = root.getElementById("mt-grid");
    const msgEl = root.getElementById("mt-status-msg");
    const retryBtn = root.getElementById("mt-retry-btn");

    // Filtrar por estado activo
    let filtered = this.#tickets;
    if (this.#activeFilter !== "ALL") {
      filtered = filtered.filter((t) => String(t.status || "").toUpperCase() === this.#activeFilter);
    }

    // Filtrar por búsqueda
    if (this.#searchQuery) {
      filtered = filtered.filter((t) => {
        return (
          String(t.id).includes(this.#searchQuery) ||
          (t.title || "").toLowerCase().includes(this.#searchQuery)
        );
      });
    }

    grid.innerHTML = "";

    if (filtered.length === 0) {
      msgEl.textContent = TEXT.myTickets.empty;
      msgEl.hidden = false;
      retryBtn.hidden = true;
      return;
    }

    msgEl.hidden = true;
    retryBtn.hidden = true;

    filtered.forEach((ticket) => {
      const statusKey = String(ticket.status || "").toUpperCase();
      const priorityKey = String(ticket.priority || "").toUpperCase();
      const categoryKey = String(ticket.category || "").toUpperCase();

      const statusLabel = TICKET_TEXT.statuses[statusKey] || statusKey;
      const priorityLabel = TICKET_TEXT.priorities[priorityKey] || priorityKey;
      const categoryLabel = TICKET_TEXT.categories[categoryKey] || categoryKey;

      const statusClass = STATUS_CLASSES[statusKey] || "";
      const prioClass = PRIO_CLASSES[priorityKey] || "";

      const card = document.createElement("article");
      card.className = "ticket-card";
      card.tabIndex = 0;
      card.setAttribute("role", "button");
      card.setAttribute("aria-haspopup", "dialog");
      card.setAttribute("aria-label", `Tiquete #${ticket.id}: ${ticket.title}`);

      card.innerHTML = `
        <div class="ticket-card__header">
          <span class="ticket-card__code">#${ticket.id}</span>
          <div class="ticket-card__badges">
            <span class="badge ${statusClass}">${statusLabel}</span>
          </div>
        </div>
        <div class="ticket-card__badges">
          <span class="badge ${prioClass}">${priorityLabel}</span>
          <span class="badge badge--category">${categoryLabel}</span>
        </div>
        <h2 class="ticket-card__title">${ticket.title}</h2>
        <p class="ticket-card__description">${ticket.description || "Sin descripción"}</p>
        <div class="ticket-card__meta">
          <time datetime="${ticket.updatedAt}">${TEXT.myTickets.cardUpdated}: ${formatDate(ticket.updatedAt)}</time>
          <span class="ticket-card__action-hint">${TEXT.myTickets.cardViewDetails}</span>
        </div>
      `;

      card.addEventListener("click", () => this.#openDetail(ticket.id));
      card.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") { e.preventDefault(); this.#openDetail(ticket.id); }
      });

      grid.append(card);
    });
  }

  async #openDetail(ticketId) {
    const root = this.#root;

    // Buscar en memoria primero; si no lo tiene, pedir al backend
    try {
      this.#currentTicket = await getTicketById(ticketId);
    } catch {
      this.#currentTicket = this.#tickets.find((t) => t.id === ticketId) || null;
    }

    if (!this.#currentTicket) return;

    const ticket = this.#currentTicket;
    const statusKey  = String(ticket.status   || "").toUpperCase();
    const priorityKey = String(ticket.priority || "").toUpperCase();
    const categoryKey = String(ticket.category || "").toUpperCase();

    // Llenar cabecera
    root.getElementById("mt-modal-code").textContent = `#${ticket.id}`;
    root.getElementById("mt-modal-title").textContent = ticket.title;

    const statusEl = root.getElementById("mt-modal-status");
    statusEl.textContent = TICKET_TEXT.statuses[statusKey] || statusKey;
    statusEl.className = `badge ${STATUS_CLASSES[statusKey] || ""}`;

    const prioEl = root.getElementById("mt-modal-priority");
    prioEl.textContent = TICKET_TEXT.priorities[priorityKey] || priorityKey;
    prioEl.className = `badge ${PRIO_CLASSES[priorityKey] || ""}`;

    root.getElementById("mt-modal-category").textContent = TICKET_TEXT.categories[categoryKey] || categoryKey;

    // Metadatos
    root.getElementById("mt-modal-desc").textContent = ticket.description || "Sin descripción";
    root.getElementById("mt-modal-applicant").textContent = ticket.applicantUsername || "—";
    root.getElementById("mt-modal-tech").textContent = ticket.technicianUsername || "Sin asignar";
    root.getElementById("mt-modal-created").textContent = formatDate(ticket.createdAt);
    root.getElementById("mt-modal-updated").textContent = formatDate(ticket.updatedAt);

    // Ciclo de vida (acciones del usuario)
    this.#renderUserLifecycle(ticket);

    // Panel de edición (solo si el ticket está OPEN y sin técnico)
    const editPanel = root.getElementById("mt-edit-panel");
    const isEditable = statusKey === "OPEN" && !ticket.technicianUsername;
    editPanel.hidden = !isEditable;
    if (isEditable) {
      root.getElementById("mt-edit-title").value = ticket.title;
      root.getElementById("mt-edit-desc").value = ticket.description || "";
      root.getElementById("mt-edit-category").value = categoryKey;
      root.getElementById("mt-edit-priority").value = priorityKey;
      root.getElementById("mt-edit-error").textContent = "";
    }

    // Gestión del formulario de comentarios según estado
    const isClosed = statusKey === "CLOSED";
    const commentForm = root.getElementById("mt-comment-form");
    commentForm.hidden = isClosed;

    // Mostrar modal
    root.getElementById("mt-modal").showModal();

    // Cargar comentarios e historial en paralelo
    this.#loadComments(ticket.id);
    this.#loadHistory(ticket.id);
  }

  #renderUserLifecycle(ticket) {
    const container = this.#root.getElementById("mt-lifecycle-actions");
    container.innerHTML = "";
    const status = String(ticket.status || "").toUpperCase();

    const statusMessages = {
      OPEN:        TEXT.detail.openNotice,
      ASSIGNED:    TEXT.detail.assignedNotice,
      IN_PROGRESS: TEXT.detail.inProgressNotice,
      CLOSED:      TEXT.detail.closedNotice,
    };

    if (status === "RESOLVED") {
      // El usuario puede confirmar el cierre (RF-04, regla 6)
      const notice = document.createElement("div");
      notice.className = "status-notice";
      notice.textContent = TEXT.detail.resolvedNotice;
      container.append(notice);

      const closeBtn = document.createElement("button");
      closeBtn.type = "button";
      closeBtn.className = "button button--confirm-close";
      closeBtn.innerHTML = `<span>${TEXT.detail.actionConfirmClose}</span>`;
      closeBtn.addEventListener("click", () => this.#handleTransition("CONFIRM_CLOSE"));
      container.append(closeBtn);
    } else {
      const notice = document.createElement("div");
      notice.className = "status-notice";
      notice.textContent = statusMessages[status] || `Estado: ${TICKET_TEXT.statuses[status] || status}`;
      container.append(notice);
    }
  }

  async #handleTransition(action) {
    if (!this.#currentTicket) return;
    const ticketId = this.#currentTicket.id;
    const root = this.#root;

    const container = root.getElementById("mt-lifecycle-actions");
    container.innerHTML = `<span class="status-notice">Actualizando estado…</span>`;

    try {
      await transitionTicket(ticketId, action);
      this.#currentTicket = await getTicketById(ticketId);
      await this.#loadMyTickets();
      this.#openDetail(ticketId);
    } catch (err) {
      console.error("Error en transición:", err);
      alert(err.message || TEXT.detail.transitionError);
      this.#renderUserLifecycle(this.#currentTicket);
    }
  }

  async #saveEdit() {
    if (!this.#currentTicket) return;
    const root = this.#root;
    const ticketId = this.#currentTicket.id;
    const saveBtn = root.getElementById("mt-edit-save");
    const errorEl = root.getElementById("mt-edit-error");

    const title       = root.getElementById("mt-edit-title").value.trim();
    const description = root.getElementById("mt-edit-desc").value.trim();
    const category    = root.getElementById("mt-edit-category").value;
    const priority    = root.getElementById("mt-edit-priority").value;

    if (title.length < 5 || description.length < 10) {
      errorEl.textContent = TEXT.detail.editError;
      return;
    }

    saveBtn.disabled = true;
    saveBtn.textContent = TEXT.detail.editSaving;
    errorEl.textContent = "";

    try {
      await updateTicket(ticketId, { title, description, category, priority });
      await this.#loadMyTickets();
      this.#openDetail(ticketId);
    } catch (err) {
      errorEl.textContent = err.message || TEXT.detail.editError;
    } finally {
      saveBtn.disabled = false;
      saveBtn.textContent = TEXT.detail.editSave;
    }
  }

  async #loadComments(ticketId) {
    const container = this.#root.getElementById("mt-comments-feed");
    container.innerHTML = `<p class="comment-card__content" style="color:#94a3b8;">Cargando comentarios…</p>`;

    try {
      const comments = await getTicketComments(ticketId);
      container.innerHTML = "";

      if (!comments || comments.length === 0) {
        container.innerHTML = `<p class="comment-card__content" style="color:#94a3b8;">${TEXT.detail.noComments}</p>`;
        return;
      }

      comments.forEach((c) => {
        const card = document.createElement("div");
        card.className = "comment-card";
        const isInternal = c.visibility === "INTERNAL";

        // Los usuarios no ven comentarios internos (notas entre técnicos/admins)
        if (isInternal) return;

        card.innerHTML = `
          <div class="comment-card__header">
            <span class="comment-card__author">${c.authorUsername || "Usuario"}</span>
            <time datetime="${c.createdAt}">${formatDate(c.createdAt)}</time>
          </div>
          <p class="comment-card__content">${c.content || ""}</p>
        `;
        container.append(card);
      });

      if (container.children.length === 0) {
        container.innerHTML = `<p class="comment-card__content" style="color:#94a3b8;">${TEXT.detail.noComments}</p>`;
      }
    } catch (err) {
      container.innerHTML = `<p class="comment-card__content" style="color:#f87171;">Error al cargar comentarios: ${err.message || ""}</p>`;
    }
  }

  async #submitComment() {
    if (!this.#currentTicket) return;
    const root = this.#root;
    const ticketId = this.#currentTicket.id;
    const input = root.getElementById("mt-comment-input");
    const submitBtn = root.getElementById("mt-submit-comment");
    const content = input.value.trim();

    if (!content) return;

    submitBtn.disabled = true;
    submitBtn.textContent = TEXT.detail.submittingComment;

    try {
      // Los usuarios solo envían comentarios públicos
      await addTicketComment(ticketId, { content, visibility: "PUBLIC" });
      input.value = "";
      await this.#loadComments(ticketId);
    } catch (err) {
      console.error("Error al publicar comentario:", err);
      alert(err.message || TEXT.detail.commentError);
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = TEXT.detail.submitComment;
    }
  }

  async #loadHistory(ticketId) {
    const container = this.#root.getElementById("mt-history-timeline");
    container.innerHTML = `<p style="color:#94a3b8;font-size:0.8125rem;">Cargando historial…</p>`;

    try {
      const history = await getTicketHistory(ticketId);
      container.innerHTML = "";

      if (!history || history.length === 0) {
        container.innerHTML = `<p style="color:#94a3b8;font-size:0.8125rem;">${TEXT.detail.noHistory}</p>`;
        return;
      }

      history.forEach((h) => {
        const item = document.createElement("div");
        item.className = "history-item";

        const prevLabel = h.previousStatus
          ? (TICKET_TEXT.statuses[h.previousStatus] || h.previousStatus)
          : "Inicio";
        const newLabel    = TICKET_TEXT.statuses[h.newStatus]  || h.newStatus;
        const actionLabel = TICKET_TEXT.historyActions[h.action] || h.action;

        item.innerHTML = `
          <span class="history-item__dot"></span>
          <div class="history-item__body">
            <div>
              <span class="history-item__change">${prevLabel} → ${newLabel}</span>
              <span style="color:#94a3b8;margin-left:0.5rem;font-size:0.75rem;">(${actionLabel})</span>
            </div>
            <div class="history-item__meta">
              <span>Por <strong>${h.changedBy || "Sistema"}</strong></span> ·
              <time datetime="${h.changedAt}">${formatDate(h.changedAt)}</time>
            </div>
          </div>
        `;
        container.append(item);
      });
    } catch (err) {
      container.innerHTML = `<p style="color:#f87171;font-size:0.8125rem;">Error al cargar historial: ${err.message || ""}</p>`;
    }
  }

  #closeModal() {
    this.#currentTicket = null;
    this.#root.getElementById("mt-modal")?.close();
  }
}

if (!customElements.get("cd-user-portal")) {
  customElements.define("cd-user-portal", UserPortal);
}
