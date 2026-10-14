import { MESSAGES } from "../constants/messages.es.js";
import { listUsers } from "../services/user.service.js";
import { listRoles } from "../services/role.service.js";
import { toUserViews } from "../mappers/user-view.mapper.js";
import { attachStyles } from "../utils/shadow-styles.js";

const TEXT = MESSAGES.users.list;
const NOTICE_DURATION_MS = 6000;

export const USER_ADD_REQUESTED_EVENT = "user-add-requested";

export class UserList extends HTMLElement {
  #root;
  #refs = null;
  #initialization = null;
  #requestId = 0;
  #noticeTimer = 0;

  constructor() {
    super();
    this.#root = this.attachShadow({ mode: "open" });
  }

  connectedCallback() {
    this.#initialization ??= this.#initialize();
  }

  disconnectedCallback() {
    clearTimeout(this.#noticeTimer);
  }

  /** Vuelve a consultar usuarios y roles y repinta el listado. */
  async reload() {
    await this.#initialization;
    return this.#load();
  }

  /** @param {string} message Aviso de operación exitosa. */
  async notify(message) {
    await this.#initialization;
    const { notice } = this.#refs;

    notice.textContent = message;
    notice.hidden = false;

    clearTimeout(this.#noticeTimer);
    this.#noticeTimer = setTimeout(() => {
      notice.hidden = true;
    }, NOTICE_DURATION_MS);
  }

  async #initialize() {
    await attachStyles(this.#root, "user-list.css");
    this.#root.append(this.#buildSkeleton());
    this.#refs = this.#collectRefs();
    this.#bindEvents();
    await this.#load();
  }

  #buildSkeleton() {
    const { columns } = TEXT;
    const template = document.createElement("template");

    template.innerHTML = `
      <section class="user-list" aria-labelledby="user-list-title">
        <header class="user-list__header">
          <div class="user-list__heading">
            <p class="user-list__eyebrow">${TEXT.eyebrow}</p>
            <h1 id="user-list-title" class="user-list__title">${TEXT.title}</h1>
          </div>
          <button class="button button--primary user-list__add" type="button">
            <span class="button__label">${TEXT.addButton}</span>
          </button>
        </header>

        <p class="user-list__notice" role="status" hidden></p>

        <div class="user-list__body" aria-busy="true">
          <p class="user-list__message" role="status"></p>
          <button class="button user-list__retry" type="button" hidden>
            <span class="button__label">${TEXT.retry}</span>
          </button>
          <div class="user-list__table-wrap" hidden>
            <table class="user-table">
              <thead>
                <tr>
                  <th scope="col">${columns.username}</th>
                  <th scope="col">${columns.role}</th>
                  <th scope="col">${columns.permissions}</th>
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
      addButton: find(".user-list__add"),
      notice: find(".user-list__notice"),
      body: find(".user-list__body"),
      message: find(".user-list__message"),
      retryButton: find(".user-list__retry"),
      tableWrap: find(".user-list__table-wrap"),
      tbody: find(".user-table tbody"),
    };
  }

  #bindEvents() {
    this.#refs.addButton.addEventListener("click", () => {
      this.dispatchEvent(new CustomEvent(USER_ADD_REQUESTED_EVENT, { bubbles: true, composed: true }));
    });

    this.#refs.retryButton.addEventListener("click", () => this.#load());
  }

  async #load() {
    const requestId = ++this.#requestId;

    this.#refs.body.setAttribute("aria-busy", "true");
    this.#showMessage(TEXT.loading);

    try {
      const [users, roles] = await Promise.all([listUsers(), listRoles()]);
      if (requestId !== this.#requestId) return;

      this.#renderUsers(toUserViews(users, roles));
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

  /**
   * @param {string} text
   * @param {{ retry?: boolean }} [options]
   */
  #showMessage(text, { retry = false } = {}) {
    const { message, retryButton, tableWrap } = this.#refs;

    message.textContent = text;
    message.setAttribute("role", retry ? "alert" : "status");
    message.hidden = false;
    retryButton.hidden = !retry;
    tableWrap.hidden = true;
  }

  /** @param {import("../mappers/user-view.mapper.js").UserView[]} views */
  #renderUsers(views) {
    if (views.length === 0) {
      this.#showMessage(TEXT.empty);
      return;
    }

    this.#refs.tbody.replaceChildren(...views.map((view) => this.#buildRow(view)));
    this.#refs.message.hidden = true;
    this.#refs.retryButton.hidden = true;
    this.#refs.tableWrap.hidden = false;
  }

  /** @param {import("../mappers/user-view.mapper.js").UserView} view */
  #buildRow(view) {
    const row = document.createElement("tr");

    const usernameCell = this.#buildCell("th", TEXT.columns.username);
    usernameCell.scope = "row";
    usernameCell.classList.add("user-table__username");
    usernameCell.textContent = view.username;

    const roleCell = this.#buildCell("td", TEXT.columns.role);
    const badge = document.createElement("span");
    badge.className = "role-badge";
    badge.dataset.role = view.roleName;
    badge.textContent = view.roleLabel;
    roleCell.append(badge);

    const permissionsCell = this.#buildCell("td", TEXT.columns.permissions);
    permissionsCell.append(this.#buildPermissions(view.permissions));

    row.append(usernameCell, roleCell, permissionsCell);
    return row;
  }

  /**
   * @param {"th" | "td"} tag
   * @param {string} label Etiqueta que se muestra en la vista de tarjetas (móvil).
   */
  #buildCell(tag, label) {
    const cell = document.createElement(tag);
    cell.dataset.label = label;
    return cell;
  }

  /** @param {string[]} permissions */
  #buildPermissions(permissions) {
    if (permissions.length === 0) {
      const empty = document.createElement("span");
      empty.className = "permissions__empty";
      empty.textContent = TEXT.noPermissions;
      return empty;
    }

    const details = document.createElement("details");
    details.className = "permissions";

    const summary = document.createElement("summary");
    summary.className = "permissions__summary";
    summary.textContent = TEXT.permissionsSummary(permissions.length);

    const list = document.createElement("ul");
    list.className = "permissions__list";
    list.append(
      ...permissions.map((permission) => {
        const item = document.createElement("li");
        item.className = "permissions__item";
        item.textContent = permission;
        return item;
      }),
    );

    details.append(summary, list);
    return details;
  }
}

customElements.define("cd-user-list", UserList);
