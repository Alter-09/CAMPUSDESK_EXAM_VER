import { MESSAGES } from "../constants/messages.es.js";
import { listUsers, deleteUser } from "../services/user.service.js";
import { listRoles } from "../services/role.service.js";
import { attachStyles } from "../utils/shadow-styles.js";

const TEXT = MESSAGES.users.list;

export class UserDirectory extends HTMLElement {
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
    await attachStyles(this.#root, "user-directory.css");
    this.#root.append(this.#buildSkeleton());
    this.#refs = this.#collectRefs();
    this.#bindEvents();
    await this.#load();
  }

  #buildSkeleton() {
    const template = document.createElement("template");
    template.innerHTML = `
      <section class="user-directory" aria-labelledby="user-dir-title">
        <header class="user-directory__header">
          <div>
            <p class="user-directory__eyebrow">Directorio General</p>
            <h1 id="user-dir-title" class="user-directory__title">Lista de Usuarios Registrados</h1>
          </div>
        </header>

        <div class="user-directory__body" aria-busy="true">
          <p class="user-directory__message" role="status">${TEXT.loading}</p>
          <button class="button user-directory__retry" type="button" hidden>
            <span class="button__label">${TEXT.retry}</span>
          </button>
          <div class="user-directory__table-wrap" hidden>
            <table class="user-table">
              <thead>
                <tr>
                  <th scope="col">Usuario</th>
                  <th scope="col">Correo Electrónico</th>
                  <th scope="col">Rol</th>
                  <th scope="col">Estado</th>
                  <th scope="col" class="user-table__cell--center">Acciones</th>
                </tr>
              </thead>
              <tbody></tbody>
            </table>
          </div>
        </div>

        <!-- Dialogo Modal de Confirmacion -->
        <dialog class="confirm-dialog" id="confirm-dialog">
          <div class="confirm-dialog__content">
            <h2 class="confirm-dialog__title">${TEXT.deleteConfirmTitle ?? "Eliminar usuario"}</h2>
            <p class="confirm-dialog__message" id="confirm-dialog-msg"></p>
            <p class="confirm-dialog__error" id="confirm-dialog-error" role="alert" hidden></p>
            <div class="confirm-dialog__actions">
              <button class="button button--secondary" type="button" id="dialog-cancel-btn">
                <span class="button__label">${TEXT.cancelButton ?? "Cancelar"}</span>
              </button>
              <button class="button button--danger" type="button" id="dialog-confirm-btn">
                <span class="button__label">${TEXT.confirmDeleteButton ?? "Sí, eliminar"}</span>
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
      body: find(".user-directory__body"),
      message: find(".user-directory__message"),
      retryButton: find(".user-directory__retry"),
      tableWrap: find(".user-directory__table-wrap"),
      tbody: find(".user-table tbody"),
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

    // Close when clicking outside dialog content
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
      const [users, roles] = await Promise.all([listUsers(), listRoles()]);
      if (requestId !== this.#requestId) return;

      this.#renderUsers(users, roles);
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

  #renderUsers(users, roles) {
    if (!users || users.length === 0) {
      this.#showMessage(TEXT.empty);
      return;
    }

    // Filter to display only users with "USER" role
    const standardUsers = users.filter((user) => {
      const role = user.role?.toUpperCase();
      return role === "USER";
    });

    if (standardUsers.length === 0) {
      this.#showMessage(TEXT.empty);
      return;
    }

    this.#refs.tbody.replaceChildren(
      ...standardUsers.map((user) => {
        const row = document.createElement("tr");

        // Usuario
        const userCell = document.createElement("th");
        userCell.scope = "row";
        userCell.textContent = user.username;

        // Email
        const emailCell = document.createElement("td");
        emailCell.textContent = user.email || "—";

        // Rol
        const roleCell = document.createElement("td");
        const roleBadge = document.createElement("span");
        roleBadge.className = "role-badge";
        roleBadge.dataset.role = user.role;
        roleBadge.textContent = MESSAGES.users.roleLabels[user.role] ?? user.role;
        roleCell.append(roleBadge);

        // Account Status
        const statusCell = document.createElement("td");
        const statusBadge = document.createElement("span");
        
        const isLocked = user.locked === true;
        const isDisabled = user.disabled === true;

        let statusLabel = "Activa";
        let statusClass = "success";

        if (isDisabled) {
          statusLabel = "Desactivada";
          statusClass = "danger";
        } else if (isLocked) {
          statusLabel = "Bloqueada";
          statusClass = "danger";
        }

        statusBadge.className = `status-badge status-badge--${statusClass}`;
        statusBadge.textContent = statusLabel;
        statusCell.append(statusBadge);

        // Actions (Delete button)
        const actionsCell = document.createElement("td");
        actionsCell.className = "user-table__cell--center";

        const deleteBtn = document.createElement("button");
        deleteBtn.type = "button";
        deleteBtn.className = "action-button action-button--danger";
        deleteBtn.setAttribute("aria-label", `Eliminar usuario ${user.username}`);
        deleteBtn.innerHTML = `
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
          </svg>
        `;
        deleteBtn.addEventListener("click", () => this.#openDeleteDialog(user.username));

        actionsCell.append(deleteBtn);

        row.append(userCell, emailCell, roleCell, statusCell, actionsCell);
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
      : `¿Estás seguro de que deseas eliminar al usuario «${username}»?`;

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

      await deleteUser(username);

      this.#closeDialog();
      await this.#load(); // Reloads list immediately
    } catch (error) {
      dialogConfirmBtn.disabled = false;
      dialogError.textContent = error?.message || TEXT.deleteError || "Error al eliminar usuario.";
      dialogError.hidden = false;
    }
  }
}

customElements.define("cd-user-directory", UserDirectory);