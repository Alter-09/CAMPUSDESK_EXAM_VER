/**
 * <cd-user-directory>: Directorio general de usuarios basado en la API del backend.
 */
import { MESSAGES } from "../constants/messages.es.js";
import { listUsers } from "../services/user.service.js";
import { listRoles } from "../services/role.service.js";
import { attachStyles } from "../utils/shadow-styles.js";

const TEXT = MESSAGES.users.list;

export class UserDirectory extends HTMLElement {
  #root;
  #refs = null;
  #initialization = null;
  #requestId = 0;

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
                </tr>
              </thead>
              <tbody></tbody>
            </table>
          </div>
        </div>
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
    };
  }

  #bindEvents() {
    this.#refs.retryButton.addEventListener("click", () => this.#load());
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

    // Filtramos para mostrar únicamente los usuarios con rol "USER" (excluyendo ADMIN y TECHNICIAN)
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

        // Correo Electrónico
        const emailCell = document.createElement("td");
        emailCell.textContent = user.email || "—";

        // Rol
        const roleCell = document.createElement("td");
        const roleBadge = document.createElement("span");
        roleBadge.className = "role-badge";
        roleBadge.dataset.role = user.role;
        roleBadge.textContent = MESSAGES.users.roleLabels[user.role] ?? user.role;
        roleCell.append(roleBadge);

        // Estado de Cuenta basado estrictamente en el JSON de la API
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

        row.append(userCell, emailCell, roleCell, statusCell);
        return row;
      })
    );

    this.#refs.message.hidden = true;
    this.#refs.retryButton.hidden = true;
    this.#refs.tableWrap.hidden = false;
  }
}

customElements.define("cd-user-directory", UserDirectory);