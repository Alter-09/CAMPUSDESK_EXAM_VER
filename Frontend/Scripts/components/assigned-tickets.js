import { MESSAGES } from "../constants/messages.es.js";
import {
  listTickets,
  getTicketById,
  transitionTicket,
  getTicketComments,
  addTicketComment,
  getTicketHistory,
} from "../services/ticket.service.js";
import { attachStyles } from "../utils/shadow-styles.js";

// [Nueva Funcionalidad: Integración Web Components Técnico] - Conecta SLA, cronómetro, adjuntos y respuestas predefinidas - Conecta con: cd-sla-indicator, cd-time-tracker, cd-attachment-uploader, cd-pagination, canned-response.service.js
import "./cd-sla-indicator.js";
import "./cd-time-tracker.js";
import "./cd-attachment-uploader.js";
import "./cd-pagination.js";
import { getActiveCannedResponses } from "../services/canned-response.service.js";

const TEXT = MESSAGES.tickets;

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

export class AssignedTickets extends HTMLElement {
  #root;
  #refs = null;
  #initialization = null;
  #tickets = [];
  #activeFilter = "ALL";
  #searchQuery = "";
  #currentTicket = null;
  #currentPage = 0;
  #pageSize = 10;

  constructor() {
    super();
    this.#root = this.attachShadow({ mode: "open" });
  }

  connectedCallback() {
    this.#initialization ??= this.#initialize();
  }

  async reload() {
    await this.#initialization;
    return this.#load();
  }

  async #initialize() {
    await attachStyles(this.#root, "assigned-tickets.css");
    this.#root.append(this.#buildSkeleton());
    this.#refs = this.#collectRefs();
    this.#bindEvents();
    await this.#load();
  }

  #buildSkeleton() {
    const template = document.createElement("template");
    template.innerHTML = `
      <section class="assigned-section" aria-labelledby="assigned-heading">
        <header class="assigned-header">
          <p class="assigned-header__eyebrow">${TEXT.assigned.eyebrow}</p>
          <h1 id="assigned-heading" class="assigned-header__title">${TEXT.assigned.title}</h1>
          <p class="assigned-header__subtitle">${TEXT.assigned.subtitle}</p>

          <div class="assigned-controls">
            <div class="filter-tabs" role="tablist" aria-label="Filtrar tiquetes por estado">
              <button class="filter-tab is-active" type="button" role="tab" data-filter="ALL" aria-selected="true">
                ${TEXT.assigned.filterAll}
              </button>
              <button class="filter-tab" type="button" role="tab" data-filter="ASSIGNED" aria-selected="false">
                ${TEXT.assigned.filterAssigned}
              </button>
              <button class="filter-tab" type="button" role="tab" data-filter="IN_PROGRESS" aria-selected="false">
                ${TEXT.assigned.filterInProgress}
              </button>
              <button class="filter-tab" type="button" role="tab" data-filter="RESOLVED" aria-selected="false">
                ${TEXT.assigned.filterResolved}
              </button>
            </div>

            <div class="search-input-wrap">
              <input
                type="search"
                class="search-input"
                id="search-input"
                placeholder="${TEXT.assigned.searchPlaceholder}"
                aria-label="Buscar tiquete"
              />
            </div>
          </div>
        </header>

        <div class="assigned-body" aria-busy="true">
          <p class="assigned-message" id="status-message" role="status">${TEXT.assigned.loading}</p>
          <button class="button button--secondary" id="retry-btn" type="button" hidden>
            <span>${TEXT.assigned.retry}</span>
          </button>
          <div class="tickets-grid" id="tickets-grid" role="region" aria-label="Cuadrícula de tiquetes asignados"></div>
          <cd-pagination id="assigned-pagination"></cd-pagination>
        </div>

        <!-- Modal de Detalle Completo de la Incidencia -->
        <dialog class="ticket-modal" id="ticket-modal" aria-labelledby="modal-ticket-title">
          <div class="modal-layout">
            <header class="modal-header">
              <div class="modal-header__main">
                <div class="modal-header__eyebrow">
                  <span id="modal-ticket-code" class="ticket-card__code">#—</span>
                  <span id="modal-ticket-status" class="badge"></span>
                  <span id="modal-ticket-priority" class="badge"></span>
                  <span id="modal-ticket-category" class="badge badge--category"></span>
                  <cd-sla-indicator id="modal-ticket-sla"></cd-sla-indicator>
                </div>
                <h2 id="modal-ticket-title" class="modal-header__title">Cargando…</h2>
              </div>
              <button class="modal-close-btn" id="modal-close-btn" type="button" aria-label="Cerrar ventana de detalle">✕</button>
            </header>

            <div class="modal-body">
              <!-- Información General -->
              <section class="detail-panel" aria-labelledby="desc-panel-title">
                <h3 id="desc-panel-title" class="detail-panel__heading">${TEXT.detail.descriptionLabel}</h3>
                <p class="detail-panel__text" id="modal-ticket-desc"></p>

                <div class="metadata-grid">
                  <div class="metadata-item">
                    <span class="metadata-item__label">${TEXT.detail.requesterLabel}</span>
                    <span class="metadata-item__value" id="modal-ticket-applicant">—</span>
                  </div>
                  <div class="metadata-item">
                    <span class="metadata-item__label">${TEXT.detail.technicianLabel}</span>
                    <span class="metadata-item__value" id="modal-ticket-tech">—</span>
                  </div>
                  <div class="metadata-item">
                    <span class="metadata-item__label">${TEXT.detail.createdAtLabel}</span>
                    <span class="metadata-item__value" id="modal-ticket-created">—</span>
                  </div>
                  <div class="metadata-item">
                    <span class="metadata-item__label">${TEXT.detail.updatedAtLabel}</span>
                    <span class="metadata-item__value" id="modal-ticket-updated">—</span>
                  </div>
                </div>
              </section>

              <!-- Control y Registro de Horas Invertidas -->
              <section class="detail-panel" aria-labelledby="timetrack-panel-title">
                <h3 id="timetrack-panel-title" class="detail-panel__heading">Control de Horas Invertidas</h3>
                <cd-time-tracker id="modal-ticket-time-tracker"></cd-time-tracker>
              </section>

              <!-- Evidencias y Archivos Adjuntos -->
              <section class="detail-panel" aria-labelledby="attachments-panel-title">
                <h3 id="attachments-panel-title" class="detail-panel__heading">Evidencias y Adjuntos</h3>
                <cd-attachment-uploader id="modal-ticket-attachments"></cd-attachment-uploader>
              </section>

              <!-- Ciclo de Vida y Transiciones -->
              <section class="detail-panel" aria-labelledby="lifecycle-panel-title">
                <h3 id="lifecycle-panel-title" class="detail-panel__heading">${TEXT.detail.sectionLifecycle}</h3>
                <div class="lifecycle-actions" id="lifecycle-actions"></div>
              </section>

              <!-- Comentarios -->
              <section class="detail-panel" aria-labelledby="comments-panel-title">
                <h3 id="comments-panel-title" class="detail-panel__heading">${TEXT.detail.sectionComments}</h3>
                <div class="comments-feed" id="comments-feed"></div>

                <form class="comment-form" id="comment-form">
                  <div style="margin-bottom: 8px;">
                    <select class="visibility-select" id="canned-response-select" style="width: 100%; border: 1px solid #cbd5e1; border-radius: 6px; padding: 6px 10px;">
                      <option value="">⚡ Seleccionar respuesta rápida predefinida (canned)...</option>
                    </select>
                  </div>

                  <textarea
                    class="comment-textarea"
                    id="comment-input"
                    rows="3"
                    placeholder="${TEXT.detail.commentPlaceholder}"
                    required
                  ></textarea>

                  <div class="comment-form-controls">
                    <label class="comment-card__header" for="visibility-select">
                      <span>${TEXT.detail.visibilityLabel}</span>
                      <select class="visibility-select" id="visibility-select">
                        <option value="PUBLIC">Pública (Visible para solicitante)</option>
                        <option value="INTERNAL">Nota Interna (Técnicos y Admin)</option>
                      </select>
                    </label>

                    <button class="button button--secondary" type="submit" id="submit-comment-btn">
                      <span>${TEXT.detail.submitComment}</span>
                    </button>
                  </div>
                </form>
              </section>

              <!-- Historial de Cambios -->
              <section class="detail-panel" aria-labelledby="history-panel-title">
                <h3 id="history-panel-title" class="detail-panel__heading">${TEXT.detail.sectionHistory}</h3>
                <div class="history-timeline" id="history-timeline"></div>
              </section>
            </div>
          </div>
        </dialog>
      </section>
    `;
    return template.content;
  }

  #collectRefs() {
    const find = (selector) => this.#root.querySelector(selector);
    return {
      grid: find("#tickets-grid"),
      statusMessage: find("#status-message"),
      retryBtn: find("#retry-btn"),
      searchInput: find("#search-input"),
      filterTabs: this.#root.querySelectorAll(".filter-tab"),
      modal: find("#ticket-modal"),
      modalCloseBtn: find("#modal-close-btn"),
      modalCode: find("#modal-ticket-code"),
      modalStatus: find("#modal-ticket-status"),
      modalPriority: find("#modal-ticket-priority"),
      modalCategory: find("#modal-ticket-category"),
      modalTitle: find("#modal-ticket-title"),
      modalDesc: find("#modal-ticket-desc"),
      modalApplicant: find("#modal-ticket-applicant"),
      modalTech: find("#modal-ticket-tech"),
      modalCreated: find("#modal-ticket-created"),
      modalUpdated: find("#modal-ticket-updated"),
      lifecycleActions: find("#lifecycle-actions"),
      commentsFeed: find("#comments-feed"),
      commentForm: find("#comment-form"),
      commentInput: find("#comment-input"),
      visibilitySelect: find("#visibility-select"),
      submitCommentBtn: find("#submit-comment-btn"),
      historyTimeline: find("#history-timeline"),
    };
  }

  #bindEvents() {
    this.#refs.retryBtn.addEventListener("click", () => this.#load());

    // Filter by tabs
    this.#refs.filterTabs.forEach((tab) => {
      tab.addEventListener("click", () => {
        this.#refs.filterTabs.forEach((t) => {
          t.classList.remove("is-active");
          t.setAttribute("aria-selected", "false");
        });
        tab.classList.add("is-active");
        tab.setAttribute("aria-selected", "true");
        this.#activeFilter = tab.dataset.filter || "ALL";
        this.#renderGrid();
      });
    });

    this.#refs.searchInput.addEventListener("input", (e) => {
      this.#searchQuery = e.target.value.toLowerCase().trim();
      this.#renderGrid();
    });

    this.#refs.modalCloseBtn.addEventListener("click", () => this.#closeDetail());
    this.#refs.modal.addEventListener("click", (e) => {
      if (e.target === this.#refs.modal) {
        this.#closeDetail();
      }
    });

    // Paginación
    const pagination = this.#root.getElementById("assigned-pagination");
    if (pagination) {
      pagination.addEventListener("page-change", (e) => {
        this.#currentPage = e.detail.page;
        this.#pageSize = e.detail.size;
        this.#renderGrid();
      });
    }

    // Respuestas Rápidas Predefinidas
    const cannedSelect = this.#root.getElementById("canned-response-select");
    cannedSelect?.addEventListener("change", (e) => {
      if (e.target.value) {
        const input = this.#refs.commentInput;
        input.value = (input.value ? input.value + "\n\n" : "") + e.target.value;
      }
    });

    this.#refs.commentForm.addEventListener("submit", (e) => {
      e.preventDefault();
      this.#handleCommentSubmit();
    });
  }

  async #load() {
    const { statusMessage, retryBtn, grid } = this.#refs;
    statusMessage.textContent = TEXT.assigned.loading;
    statusMessage.hidden = false;
    retryBtn.hidden = true;
    grid.innerHTML = "";

    try {
      this.#tickets = await listTickets();
      this.#renderGrid();
    } catch (err) {
      console.error("Error al cargar tiquetes asignados:", err);
      statusMessage.textContent = `${TEXT.assigned.loadError} ${err.message || ""}`;
      statusMessage.hidden = false;
      retryBtn.hidden = false;
    }
  }

  #renderGrid() {
    const { grid, statusMessage, retryBtn } = this.#refs;

    let filtered = this.#tickets;
    if (this.#activeFilter !== "ALL") {
      filtered = filtered.filter((t) => {
        const s = String(t.status || "").toUpperCase();
        return s === this.#activeFilter;
      });
    }

    if (this.#searchQuery) {
      filtered = filtered.filter((t) => {
        const idMatch = String(t.id).includes(this.#searchQuery);
        const titleMatch = (t.title || "").toLowerCase().includes(this.#searchQuery);
        const applicantMatch = (t.applicantUsername || "").toLowerCase().includes(this.#searchQuery);
        return idMatch || titleMatch || applicantMatch;
      });
    }

    grid.innerHTML = "";

    const pagination = this.#root.getElementById("assigned-pagination");

    if (filtered.length === 0) {
      statusMessage.textContent = TEXT.assigned.empty;
      statusMessage.hidden = false;
      retryBtn.hidden = true;
      if (pagination) pagination.hidden = true;
      return;
    }

    statusMessage.hidden = true;
    retryBtn.hidden = true;

    if (pagination) {
      pagination.hidden = false;
      pagination.setAttribute("total-elements", String(filtered.length));
      pagination.setAttribute("total-pages", String(Math.ceil(filtered.length / this.#pageSize) || 1));
      pagination.setAttribute("page", String(this.#currentPage));
      pagination.setAttribute("size", String(this.#pageSize));
    }

    const startIndex = this.#currentPage * this.#pageSize;
    const pageItems = filtered.slice(startIndex, startIndex + this.#pageSize);

    pageItems.forEach((ticket) => {
      const card = document.createElement("article");
      card.className = "ticket-card";
      card.tabIndex = 0;
      card.role = "button";
      card.setAttribute("aria-haspopup", "dialog");
      card.setAttribute("aria-label", `Tiquete #${ticket.id}: ${ticket.title}`);

      const statusKey = String(ticket.status || "").toUpperCase();
      const priorityKey = String(ticket.priority || "").toUpperCase();
      const categoryKey = String(ticket.category || "").toUpperCase();

      const statusLabel = TEXT.statuses[statusKey] || statusKey;
      const priorityLabel = TEXT.priorities[priorityKey] || priorityKey;
      const categoryLabel = TEXT.categories[categoryKey] || categoryKey;

      const statusClass = `badge--status-${statusKey.toLowerCase().replace(/_/g, "-")}`;
      const prioClass = `badge--prio-${priorityKey.toLowerCase()}`;

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
          <div class="ticket-card__meta-row">
            <span><strong>${TEXT.assigned.cardRequester}:</strong> ${ticket.applicantUsername || "—"}</span>
            <time datetime="${ticket.createdAt}">${formatDate(ticket.createdAt)}</time>
          </div>
          <span class="ticket-card__action-hint">${TEXT.assigned.cardViewDetails}</span>
        </div>
      `;

      card.addEventListener("click", () => this.#openDetail(ticket.id));
      card.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          this.#openDetail(ticket.id);
        }
      });

      grid.append(card);
    });
  }

  async #openDetail(ticketId) {
    try {
      this.#currentTicket = await getTicketById(ticketId);
    } catch {
      this.#currentTicket = this.#tickets.find((t) => t.id === ticketId);
    }

    if (!this.#currentTicket) return;

    const ticket = this.#currentTicket;
    const statusKey = String(ticket.status || "").toUpperCase();
    const priorityKey = String(ticket.priority || "").toUpperCase();
    const categoryKey = String(ticket.category || "").toUpperCase();

    const statusLabel = TEXT.statuses[statusKey] || statusKey;
    const priorityLabel = TEXT.priorities[priorityKey] || priorityKey;
    const categoryLabel = TEXT.categories[categoryKey] || categoryKey;

    const statusClass = `badge--status-${statusKey.toLowerCase().replace(/_/g, "-")}`;
    const prioClass = `badge--prio-${priorityKey.toLowerCase()}`;

    // Llenar cabecera y metadatos
    this.#refs.modalCode.textContent = `#${ticket.id}`;
    this.#refs.modalStatus.textContent = statusLabel;
    this.#refs.modalStatus.className = `badge ${statusClass}`;
    this.#refs.modalPriority.textContent = priorityLabel;
    this.#refs.modalPriority.className = `badge ${prioClass}`;
    this.#refs.modalCategory.textContent = categoryLabel;

    this.#refs.modalTitle.textContent = ticket.title;
    this.#refs.modalDesc.textContent = ticket.description || "Sin descripción";
    this.#refs.modalApplicant.textContent = ticket.applicantUsername || "—";
    this.#refs.modalTech.textContent = ticket.technicianUsername || "—";
    this.#refs.modalCreated.textContent = formatDate(ticket.createdAt);
    this.#refs.modalUpdated.textContent = formatDate(ticket.updatedAt);

    // Configurar Indicador de SLA
    const slaEl = this.#root.getElementById("modal-ticket-sla");
    if (slaEl) {
      slaEl.setAttribute("due-date", ticket.slaDueDate || "");
      slaEl.setAttribute("breached", String(ticket.slaBreached || false));
      slaEl.setAttribute("priority", priorityKey);
      slaEl.setAttribute("status", statusKey);
    }

    // Configurar Time Tracker
    const trackerEl = this.#root.getElementById("modal-ticket-time-tracker");
    if (trackerEl) {
      trackerEl.setAttribute("ticket-id", ticket.id);
    }

    // Configurar Evidencias
    const attachmentsEl = this.#root.getElementById("modal-ticket-attachments");
    if (attachmentsEl) {
      attachmentsEl.setAttribute("ticket-id", ticket.id);
    }

    // Cargar respuestas rápidas predefinidas
    this.#loadCannedResponses();

    this.#renderLifecycleActions(ticket);

    this.#refs.modal.showModal();
    this.#loadComments(ticket.id);
    this.#loadHistory(ticket.id);
  }

  async #loadCannedResponses() {
    const select = this.#root.getElementById("canned-response-select");
    if (!select || select.dataset.loaded === "true") return;
    try {
      const list = await getActiveCannedResponses();
      if (Array.isArray(list)) {
        list.forEach(c => {
          const opt = document.createElement("option");
          opt.value = c.content;
          opt.textContent = `${c.title} (${c.category || "General"})`;
          select.appendChild(opt);
        });
        select.dataset.loaded = "true";
      }
    } catch (err) {
      console.warn("cd-assigned-tickets: Could not load canned responses", err);
    }
  }

  #renderLifecycleActions(ticket) {
    const container = this.#refs.lifecycleActions;
    container.innerHTML = "";

    const status = String(ticket.status || "").toUpperCase();

    if (status === "ASSIGNED") {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "button button--action-primary";
      btn.innerHTML = `<span>▶ ${TEXT.detail.actionStartProgress}</span>`;
      btn.addEventListener("click", () => this.#handleTransition("START_PROGRESS"));
      container.append(btn);
    } else if (status === "IN_PROGRESS") {
      const resolveBtn = document.createElement("button");
      resolveBtn.type = "button";
      resolveBtn.className = "button button--action-success";
      resolveBtn.innerHTML = `<span>✓ ${TEXT.detail.actionResolve}</span>`;
      resolveBtn.addEventListener("click", () => this.#handleTransition("RESOLVE"));
      container.append(resolveBtn);

      const pauseBtn = document.createElement("button");
      pauseBtn.type = "button";
      pauseBtn.className = "button button--secondary";
      pauseBtn.innerHTML = `<span>⏸️ Pausar Tiquete</span>`;
      pauseBtn.addEventListener("click", () => this.#handleTransition("PAUSE"));
      container.append(pauseBtn);
    } else if (status === "PENDING_USER" || status === "PENDING_VENDOR") {
      const resumeBtn = document.createElement("button");
      resumeBtn.type = "button";
      resumeBtn.className = "button button--action-primary";
      resumeBtn.innerHTML = `<span>▶ Reanudar Tiquete</span>`;
      resumeBtn.addEventListener("click", () => this.#handleTransition("RESUME"));
      container.append(resumeBtn);
    } else if (status === "RESOLVED") {
      const notice = document.createElement("div");
      notice.className = "status-notice";
      notice.textContent = TEXT.detail.resolvedNotice;
      container.append(notice);
    } else if (status === "CLOSED") {
      const notice = document.createElement("div");
      notice.className = "status-notice";
      notice.textContent = TEXT.detail.closedNotice;
      container.append(notice);
    } else {
      const notice = document.createElement("div");
      notice.className = "status-notice";
      notice.textContent = `Estado actual: ${TEXT.statuses[status] || status}`;
      container.append(notice);
    }
  }

  async #handleTransition(action) {
    if (!this.#currentTicket) return;
    const ticketId = this.#currentTicket.id;

    try {
      this.#refs.lifecycleActions.innerHTML = `<span class="status-notice">Actualizando estado…</span>`;
      await transitionTicket(ticketId, action);

      this.#currentTicket = await getTicketById(ticketId);
      await this.#load();

      this.#openDetail(ticketId);
    } catch (err) {
      console.error("Error en transición de estado:", err);
      alert(err.message || TEXT.detail.transitionError);
      this.#renderLifecycleActions(this.#currentTicket);
    }
  }

  async #loadComments(ticketId) {
    const container = this.#refs.commentsFeed;
    container.innerHTML = `<p class="comment-card__content" style="color: #94a3b8;">Cargando comentarios…</p>`;

    try {
      const comments = await getTicketComments(ticketId);
      container.innerHTML = "";

      if (!comments || comments.length === 0) {
        container.innerHTML = `<p class="comment-card__content" style="color: #94a3b8;">${TEXT.detail.noComments}</p>`;
        return;
      }

      comments.forEach((c) => {
        const card = document.createElement("div");
        card.className = "comment-card";

        const isInternal = c.visibility === "INTERNAL";
        const visLabel = isInternal ? "Nota Interna" : "Pública";
        const visBadgeClass = isInternal ? "badge--prio-high" : "badge--category";

        card.innerHTML = `
          <div class="comment-card__header">
            <span class="comment-card__author">${c.authorUsername || "Usuario"}</span>
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <span class="badge ${visBadgeClass}">${visLabel}</span>
              <time datetime="${c.createdAt}">${formatDate(c.createdAt)}</time>
            </div>
          </div>
          <p class="comment-card__content">${c.content || ""}</p>
        `;
        container.append(card);
      });
    } catch (err) {
      container.innerHTML = `<p class="comment-card__content" style="color: #f87171;">Error al cargar comentarios: ${err.message || ""}</p>`;
    }
  }

  async #handleCommentSubmit() {
    if (!this.#currentTicket) return;
    const ticketId = this.#currentTicket.id;
    const content = this.#refs.commentInput.value.trim();
    const visibility = this.#refs.visibilitySelect.value || "PUBLIC";

    if (!content) return;

    try {
      this.#refs.submitCommentBtn.disabled = true;
      this.#refs.submitCommentBtn.textContent = TEXT.detail.submittingComment;

      await addTicketComment(ticketId, { content, visibility });

      this.#refs.commentInput.value = "";
      await this.#loadComments(ticketId);
    } catch (err) {
      console.error("Error al publicar comentario:", err);
      alert(err.message || TEXT.detail.commentError);
    } finally {
      this.#refs.submitCommentBtn.disabled = false;
      this.#refs.submitCommentBtn.textContent = TEXT.detail.submitComment;
    }
  }

  async #loadHistory(ticketId) {
    const container = this.#refs.historyTimeline;
    container.innerHTML = `<p style="color: #94a3b8; font-size: 0.8125rem;">Cargando historial…</p>`;

    try {
      const history = await getTicketHistory(ticketId);
      container.innerHTML = "";

      if (!history || history.length === 0) {
        container.innerHTML = `<p style="color: #94a3b8; font-size: 0.8125rem;">${TEXT.detail.noHistory}</p>`;
        return;
      }

      history.forEach((h) => {
        const item = document.createElement("div");
        item.className = "history-item";

        const prevLabel = h.previousStatus ? (TEXT.statuses[h.previousStatus] || h.previousStatus) : "Inicio";
        const newLabel = TEXT.statuses[h.newStatus] || h.newStatus;
        const actionLabel = TEXT.historyActions[h.action] || h.action;

        item.innerHTML = `
          <span class="history-item__dot"></span>
          <div class="history-item__body">
            <div>
              <span class="history-item__change">${prevLabel} → ${newLabel}</span>
              <span style="color: #94a3b8; margin-left: 0.5rem; font-size: 0.75rem;">(${actionLabel})</span>
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
      container.innerHTML = `<p style="color: #f87171; font-size: 0.8125rem;">Error al cargar historial: ${err.message || ""}</p>`;
    }
  }

  #closeDetail() {
    this.#currentTicket = null;
    this.#refs.modal.close();
  }
}

if (!customElements.get("cd-assigned-tickets")) {
  customElements.define("cd-assigned-tickets", AssignedTickets);
}
