import { MESSAGES } from "../constants/messages.es.js";
import { listTechnicians, deleteTechnician } from "../services/technician.service.js";
import { attachStyles } from "../utils/shadow-styles.js";

const TEXT = MESSAGES.technicians.list;

export class TechnicianDirectory extends HTMLElement {
  #root;
  #refs = null;
  #initialization = null;
  #requestId = 0;
  #pendingDeleteUsername = null;

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
    await attachStyles(this.#root, "technician-directory.css");
    this.#root.append(this.#buildSkeleton());
    this.#refs = this.#collectRefs();
    this.#bindEvents();
    await this.#load();
  }

  #buildSkeleton() {
    const template = document.createElement("template");
    template.innerHTML = `
      <section class="technician-directory" aria-labelledby="tech-dir-title">
        <header class="technician-directory__header">
          <div>
            <p class="technician-directory__eyebrow">${TEXT.eyebrow}</p>
            <h1 id="tech-dir-title" class="technician-directory__title">${TEXT.title}</h1>
          </div>
        </header>

        <div class="technician-directory__body" aria-busy="true">
          <p class="technician-directory__message" role="status">${TEXT.loading}</p>
          <button class="button technician-directory__retry" type="button" hidden>
            <span class="button__label">${TEXT.retry}</span>
          </button>
          <div class="technician-directory__table-wrap" hidden>
            <table class="technician-table">
              <thead>
                <tr>
                  <th scope="col">${TEXT.columns.username}</th>
                  <th scope="col">${TEXT.columns.email}</th>
                  <th scope="col">${TEXT.columns.activeTickets}</th>
                  <th scope="col">${TEXT.columns.status}</th>
                  <th scope="col" class="technician-table__cell--center">${TEXT.columns.actions}</th>
                </tr>
              </thead>
              <tbody></tbody>
            </table>
          </div>
        </div>

        <!-- Dialogo Modal de Confirmacion -->
        <dialog class="confirm-dialog" id="confirm-dialog">
          <div class="confirm-dialog__content">
            <h2 class="confirm-dialog__title">${TEXT.deleteConfirmTitle}</h2>
            <p class="confirm-dialog__message" id="confirm-dialog-msg"></p>
            <p class="confirm-dialog__error" id="confirm-dialog-error" role="alert" hidden></p>
            <div class="confirm-dialog__actions">
              <button class="button button--secondary" type="button" id="dialog-cancel-btn">
                <span class="button__label">${TEXT.cancelButton}</span>
              </button>
              <button class="button button--danger" type="button" id="dialog-confirm-btn">
                <span class="button__label">${TEXT.confirmDeleteButton}</span>
              </button>
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
      body: find(".technician-directory__body"),
      message: find(".technician-directory__message"),
      retryButton: find(".technician-directory__retry"),
      tableWrap: find(".technician-directory__table-wrap"),
      tbody: find(".technician-table tbody"),
      dialog: find("#confirm-dialog"),
      dialogMsg: find("#confirm-dialog-msg"),
      dialogError: find("#confirm-dialog-error"),
      dialogCancelBtn: find("#dialog-cancel-btn"),
      dialogConfirmBtn: find("#dialog-confirm-btn"),
    };
  }

  #bindEvents() {
    this.#refs.retryButton.addEventListener("click", () => this.#load());
    this.#refs.dialogCancelBtn.addEventListener("click", () => this.#closeDialog());
    this.#refs.dialogConfirmBtn.addEventListener("click", () => this.#handleConfirmDelete());

    // Cierre al hacer clic fuera del contenido del dialogo
    this.#refs.dialog.addEventListener("click", (e) => {
      if (e.target === this.#refs.dialog) {
        this.#closeDialog();
      }
    });
  }

  async #load() {
    const requestId = ++this.#requestId;
    this.#refs.body.setAttribute("aria-busy", "true");
    this.#showMessage(TEXT.loading);

    try {
      const technicians = await listTechnicians();
      if (requestId !== this.#requestId) return;

      this.#renderTechnicians(technicians);
    } catch (error) {
      if (requestId !== this.#requestId) return;
      const detail = error?.message ? ` ${error.message}` : "";
      this.#showMessage(`${TEXT.loadError}${detail}`, { retry: true });
    } finally {
      if (requestId === this.#requestId) {
        this.#refs.body.setAttribute("aria-busy", "false");
      }
    }
  }

  #showMessage(text, { retry = false } = {}) {
    const { message, retryButton, tableWrap } = this.#refs;
    message.textContent = text;
    message.setAttribute("role", retry ? "alert" : "status");
    message.hidden = false;
    retryButton.hidden = !retry;
    tableWrap.hidden = true;
  }

  #renderTechnicians(technicians) {
    if (!technicians || technicians.length === 0) {
      this.#showMessage(TEXT.empty);
      return;
    }

    this.#refs.tbody.replaceChildren(
      ...technicians.map((tech) => {
        const row = document.createElement("tr");

        // Nombre / Usuario
        const userCell = document.createElement("th");
        userCell.scope = "row";
        userCell.textContent = tech.username;

        // Correo Electrónico
        const emailCell = document.createElement("td");
        emailCell.textContent = tech.email || "—";

        // Tiquetes Activos
        const ticketsCell = document.createElement("td");
        const ticketsBadge = document.createElement("span");
        ticketsBadge.className = "tickets-badge";
        ticketsBadge.textContent = String(tech.activeTicketsCount ?? 0);
        ticketsCell.append(ticketsBadge);

        // Estado
        const statusCell = document.createElement("td");
        const statusBadge = document.createElement("span");

        const statusKey = (tech.availabilityStatus || "AVAILABLE").toUpperCase();
        let statusLabel = TEXT.statusLabels[statusKey] || statusKey;
        let statusClass = "success";

        if (statusKey === "BUSY") {
          statusClass = "warning";
        } else if (statusKey === "ON_LEAVE") {
          statusClass = "info";
        } else if (statusKey === "OFFLINE" || tech.disabled || tech.locked) {
          statusClass = "danger";
          statusLabel = TEXT.statusLabels.OFFLINE || "Inactivo";
        }

        statusBadge.className = `status-badge status-badge--${statusClass}`;
        statusBadge.textContent = statusLabel;
        statusCell.append(statusBadge);

        // Acciones (Botón eliminar)
        const actionsCell = document.createElement("td");
        actionsCell.className = "technician-table__cell--center";

        const deleteBtn = document.createElement("button");
        deleteBtn.type = "button";
        deleteBtn.className = "action-button action-button--danger";
        deleteBtn.setAttribute("aria-label", `Eliminar técnico ${tech.username}`);
        deleteBtn.innerHTML = `
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
          </svg>
        `;
        deleteBtn.addEventListener("click", () => this.#openDeleteDialog(tech.username));

        actionsCell.append(deleteBtn);

        row.append(userCell, emailCell, ticketsCell, statusCell, actionsCell);
        return row;
      })
    );

    this.#refs.message.hidden = true;
    this.#refs.retryButton.hidden = true;
    this.#refs.tableWrap.hidden = false;
  }

  #openDeleteDialog(username) {
    this.#pendingDeleteUsername = username;
    const msg = TEXT.deleteConfirmMessage
      ? TEXT.deleteConfirmMessage(username)
      : `¿Estás seguro de que deseas eliminar al técnico «${username}»?`;

    this.#refs.dialogMsg.textContent = msg;
    this.#refs.dialogError.hidden = true;
    this.#refs.dialogConfirmBtn.disabled = false;
    this.#refs.dialog.showModal();
  }

  #closeDialog() {
    this.#pendingDeleteUsername = null;
    this.#refs.dialog.close();
  }

  async #handleConfirmDelete() {
    if (!this.#pendingDeleteUsername) return;

    const username = this.#pendingDeleteUsername;
    const { dialogConfirmBtn, dialogError } = this.#refs;

    try {
      dialogConfirmBtn.disabled = true;
      dialogError.hidden = true;

      await deleteTechnician(username);

      this.#closeDialog();
      await this.#load(); // Recarga la lista inmediatamente
    } catch (error) {
      dialogConfirmBtn.disabled = false;
      dialogError.textContent = error?.message || TEXT.deleteError || "Error al eliminar técnico.";
      dialogError.hidden = false;
    }
  }
}

if (!customElements.get("cd-technician-directory")) {
  customElements.define("cd-technician-directory", TechnicianDirectory);
}
