/**
 * <cd-role-assignment>: cambia el rol de un usuario y gestiona sus permisos individuales.
 *
 * Flujo: elegir usuario -> elegir rol -> ajustar permisos individuales -> guardar.
 *  - El superadmin (rol protegido) no aparece en la lista de usuarios ni en la de roles.
 *  - La lista de permisos muestra el catálogo completo: los que trae el rol van fijos y
 *    los demás se pueden asignar o retirar de forma individual.
 *  - Guardar aplica solo la diferencia contra el estado que tiene el backend.
 *  - Cambiar de usuario con cambios sin guardar pide confirmación antes de descartarlos.
 *
 * Los textos son constantes de MESSAGES; los datos de la API se insertan con textContent.
 */
import { MESSAGES } from "../constants/messages.es.js";
import { listUsers } from "../services/user.service.js";
import { listAssignableRoles, isProtectedRole } from "../services/role.service.js";
import { listPermissions } from "../services/permission.service.js";
import { applyAccessChanges } from "../services/access.service.js";
import { diffAccess } from "../utils/access-diff.js";
import { attachStyles } from "../utils/shadow-styles.js";

const TEXT = MESSAGES.access;
const ROLE_LABELS = MESSAGES.users.roleLabels;
const NOTICE_DURATION_MS = 6000;

export class RoleAssignment extends HTMLElement {
  #root;
  #refs = null;
  #initialization = null;
  #users = [];
  #roles = [];
  #catalog = [];
  #selectedUser = null;
  #currentUsername = "";
  #pendingUsername = "";
  #baseline = null;
  #draft = null;
  #saving = false;
  #loadId = 0;
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

  async #initialize() {
    await attachStyles(this.#root, "role-assignment.css");
    this.#root.append(this.#buildSkeleton());
    this.#refs = this.#collectRefs();
    this.#bindEvents();
    await this.#loadAll();
  }

  #buildSkeleton() {
    const template = document.createElement("template");
    const { fields, permissions, unsaved } = TEXT;

    template.innerHTML = `
      <section class="assignment" aria-labelledby="assignment-title">
        <header class="assignment__header">
          <p class="assignment__eyebrow">${TEXT.eyebrow}</p>
          <h1 id="assignment-title" class="assignment__title">${TEXT.title}</h1>
        </header>

        <p class="assignment__notice" role="status" hidden></p>
        <p class="assignment__message" role="status"></p>
        <button class="button assignment__retry" type="button" hidden>
          <span class="button__label">${TEXT.retry}</span>
        </button>

        <form class="assignment__form" novalidate hidden>
          <p class="assignment__summary" role="alert" hidden></p>

          <div class="field">
            <label class="field__label" for="user">${fields.user.label}</label>
            <div class="field__control">
              <select id="user" class="field__input assignment__select"></select>
            </div>
          </div>

          <div class="field">
            <label class="field__label" for="role">${fields.role.label}</label>
            <div class="field__control">
              <select id="role" class="field__input assignment__select" disabled></select>
            </div>
          </div>

          <section class="permissions-card" aria-labelledby="permissions-title">
            <div class="permissions-card__header">
              <h2 id="permissions-title" class="permissions-card__title">${permissions.title}</h2>
              <span class="permissions-card__count"></span>
            </div>
            <p class="permissions-card__hint"></p>
            <ul class="permissions-card__list"></ul>
          </section>

          <div class="assignment__actions">
            <p class="assignment__changes" aria-live="polite"></p>
            <button class="button button--primary assignment__save" type="submit" disabled>
              <span class="button__label">${TEXT.save}</span>
            </button>
          </div>
        </form>

        <dialog class="confirm" aria-labelledby="confirm-title" aria-describedby="confirm-text">
          <div class="confirm__form">
            <h2 id="confirm-title" class="confirm__title">${unsaved.title}</h2>
            <p id="confirm-text" class="confirm__text"></p>
            <div class="confirm__actions">
              <button class="button confirm__discard" type="button">
                <span class="button__label">${unsaved.discard}</span>
              </button>
              <button class="button button--primary confirm__keep" type="button" autofocus>
                <span class="button__label">${unsaved.keep}</span>
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
      notice: find(".assignment__notice"),
      message: find(".assignment__message"),
      retryButton: find(".assignment__retry"),
      form: find(".assignment__form"),
      summary: find(".assignment__summary"),
      userSelect: find("#user"),
      roleSelect: find("#role"),
      count: find(".permissions-card__count"),
      hint: find(".permissions-card__hint"),
      list: find(".permissions-card__list"),
      changes: find(".assignment__changes"),
      saveButton: find(".assignment__save"),
      saveLabel: find(".assignment__save .button__label"),
      confirmDialog: find(".confirm"),
      confirmText: find(".confirm__text"),
      keepButton: find(".confirm__keep"),
      discardButton: find(".confirm__discard"),
    };
  }

  #bindEvents() {
    const { retryButton, form, userSelect, roleSelect, list, confirmDialog, keepButton, discardButton } = this.#refs;

    retryButton.addEventListener("click", () => this.#loadAll());
    form.addEventListener("submit", (event) => this.#handleSubmit(event));
    userSelect.addEventListener("change", () => this.#handleUserChange());
    roleSelect.addEventListener("change", () => this.#handleRoleChange());
    list.addEventListener("change", (event) => this.#handlePermissionToggle(event));

    keepButton.addEventListener("click", () => confirmDialog.close());
    discardButton.addEventListener("click", () => this.#discardChanges());
    // Escape o "Seguir editando": no hay usuario pendiente de cambiar.
    confirmDialog.addEventListener("close", () => {
      this.#pendingUsername = "";
    });
  }

  /* ---------- Carga ---------- */

  async #loadAll() {
    const loadId = ++this.#loadId;
    this.#showMessage(TEXT.loading);

    try {
      const [users, roles, catalog] = await Promise.all([listUsers(), listAssignableRoles(), listPermissions()]);
      if (loadId !== this.#loadId) return;

      this.#roles = roles;
      this.#catalog = catalog;
      this.#users = users.filter((user) => !isProtectedRole(user.role));

      if (this.#users.length === 0) {
        this.#showMessage(TEXT.noUsers);
        return;
      }

      this.#fillRoleOptions();
      this.#fillUserOptions();
      this.#selectUser("");
      this.#showForm();
    } catch (error) {
      if (loadId !== this.#loadId) return;

      const detail = error?.message ? ` ${error.message}` : "";
      this.#showMessage(`${TEXT.loadError}${detail}`, { retry: true });
    }
  }

  /**
   * @param {string} text
   * @param {{ retry?: boolean }} [options]
   */
  #showMessage(text, { retry = false } = {}) {
    const { message, retryButton, form } = this.#refs;

    message.textContent = text;
    message.setAttribute("role", retry ? "alert" : "status");
    message.hidden = false;
    retryButton.hidden = !retry;
    form.hidden = true;
  }

  #showForm() {
    this.#refs.message.hidden = true;
    this.#refs.retryButton.hidden = true;
    this.#refs.form.hidden = false;
  }

  /** @param {string} [selectedUsername] */
  #fillUserOptions(selectedUsername = "") {
    const placeholder = new Option(TEXT.fields.user.placeholder, "", true, true);
    placeholder.disabled = true;

    const options = this.#users.map((user) => new Option(`${user.username} (${user.email})`, user.username));
    this.#refs.userSelect.replaceChildren(placeholder, ...options);
    this.#refs.userSelect.value = selectedUsername;
  }

  #fillRoleOptions() {
    const placeholder = new Option(TEXT.fields.role.placeholder, "", true, true);
    placeholder.disabled = true;

    const options = this.#roles.map((role) => new Option(ROLE_LABELS[role.name] ?? role.name, role.name));
    this.#refs.roleSelect.replaceChildren(placeholder, ...options);
  }

  /* ---------- Selección y borrador ---------- */

  /** @param {string} username Vacío limpia la selección. */
  #selectUser(username) {
    const { roleSelect } = this.#refs;
    const user = this.#users.find((item) => item.username === username) ?? null;

    this.#selectedUser = user;
    this.#currentUsername = user?.username ?? "";
    this.#showSummary("");

    if (!user) {
      this.#baseline = null;
      this.#draft = null;
      roleSelect.value = "";
      roleSelect.disabled = true;
    } else {
      const additional = [...(user.additionalPermissions ?? [])];

      this.#baseline = { role: user.role, additionalPermissions: additional };
      this.#draft = { role: user.role, additionalPermissions: new Set(additional) };
      roleSelect.value = user.role;
      roleSelect.disabled = this.#saving;
    }

    this.#renderPermissions();
    this.#updateSummary();
  }

  /** Si hay cambios sin guardar, vuelve al usuario actual y pide confirmación. */
  #handleUserChange() {
    const { userSelect, confirmDialog, confirmText } = this.#refs;
    const nextUsername = userSelect.value;

    if (!this.#hasPendingChanges()) {
      this.#selectUser(nextUsername);
      return;
    }

    userSelect.value = this.#currentUsername;
    this.#pendingUsername = nextUsername;
    confirmText.textContent = TEXT.unsaved.text(this.#currentUsername);
    confirmDialog.showModal();
  }

  #discardChanges() {
    const username = this.#pendingUsername;

    this.#pendingUsername = "";
    this.#refs.confirmDialog.close();
    this.#refs.userSelect.value = username;
    this.#selectUser(username);
  }

  /** @returns {boolean} */
  #hasPendingChanges() {
    return Boolean(this.#baseline && this.#draft && diffAccess(this.#baseline, this.#draft).hasChanges);
  }

  #handleRoleChange() {
    if (!this.#draft) return;

    this.#draft.role = this.#refs.roleSelect.value || this.#draft.role;
    this.#showSummary("");
    this.#renderPermissions();
    this.#updateSummary();
  }

  /** @param {Event} event */
  #handlePermissionToggle(event) {
    const checkbox = event.target;
    if (!(checkbox instanceof HTMLInputElement) || !this.#draft) return;

    if (checkbox.checked) {
      this.#draft.additionalPermissions.add(checkbox.value);
    } else {
      this.#draft.additionalPermissions.delete(checkbox.value);
    }

    // El estado visual del permiso se actualiza en sitio para no perder el foco.
    const row = checkbox.closest(".perm");
    if (row instanceof HTMLElement) {
      row.dataset.effective = String(row.dataset.fromRole === "true" || checkbox.checked);
    }

    this.#showSummary("");
    this.#updateSummary();
  }

  /** @returns {Set<string>} Permisos que trae el rol del borrador. */
  #inheritedPermissions() {
    const role = this.#roles.find((item) => item.name === this.#draft?.role);
    return new Set(role?.permissions ?? []);
  }

  /* ---------- Lista de permisos ---------- */

  #renderPermissions() {
    const { list, hint } = this.#refs;

    list.replaceChildren();

    if (!this.#draft) {
      hint.textContent = TEXT.permissions.pickUser;
      this.#updateCount();
      return;
    }

    const inherited = this.#inheritedPermissions();
    hint.textContent = TEXT.permissions.hint;
    list.append(
      ...this.#catalog.map((permission) =>
        this.#buildPermissionRow(permission, inherited.has(permission), this.#draft.additionalPermissions.has(permission)),
      ),
    );
    this.#updateCount();
  }

  /**
   * Casos de una fila:
   *  - solo del rol: fija, sin control.
   *  - del rol y también individual: casilla marcada para poder retirar la asignación sobrante.
   *  - no del rol: casilla para asignarla o retirarla de forma individual.
   * @param {string} permission
   * @param {boolean} fromRole
   * @param {boolean} individual
   */
  #buildPermissionRow(permission, fromRole, individual) {
    const row = document.createElement("li");
    row.className = "perm";
    row.dataset.fromRole = String(fromRole);
    row.dataset.effective = String(fromRole || individual);

    const mark = document.createElement("span");
    mark.className = "perm__mark";
    mark.setAttribute("aria-hidden", "true");

    const body = document.createElement("div");
    body.className = "perm__body";

    const code = document.createElement("span");
    code.className = "perm__code";
    code.textContent = permission;
    body.append(code);

    if (fromRole) {
      const note = document.createElement("span");
      note.className = "perm__note";
      note.textContent = TEXT.permissions.fromRole;
      body.append(note);
    }

    if (!fromRole || individual) {
      const toggleText = fromRole ? TEXT.permissions.toggleRedundant : TEXT.permissions.toggleIndividual;

      const toggle = document.createElement("label");
      toggle.className = "perm__toggle";

      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.value = permission;
      checkbox.checked = individual;
      checkbox.disabled = this.#saving;
      checkbox.setAttribute("aria-label", `${toggleText}: ${permission}`);

      const text = document.createElement("span");
      text.textContent = toggleText;

      toggle.append(checkbox, text);
      body.append(toggle);
    }

    row.append(mark, body);
    return row;
  }

  #updateCount() {
    const { count } = this.#refs;

    if (!this.#draft) {
      count.textContent = "";
      return;
    }

    const inherited = this.#inheritedPermissions();
    const active = this.#catalog.filter(
      (permission) => inherited.has(permission) || this.#draft.additionalPermissions.has(permission),
    ).length;

    count.textContent = TEXT.permissions.count(active, this.#catalog.length);
  }

  /* ---------- Resumen de cambios y guardado ---------- */

  #updateSummary() {
    const { changes, saveButton } = this.#refs;

    if (!this.#draft || !this.#baseline) {
      changes.textContent = "";
      saveButton.disabled = true;
      this.#updateCount();
      return;
    }

    const diff = diffAccess(this.#baseline, this.#draft);
    const parts = [];

    if (diff.roleChanged) parts.push(TEXT.changes.role(ROLE_LABELS[diff.role] ?? diff.role));
    if (diff.added.length > 0) parts.push(TEXT.changes.assign(diff.added.length));
    if (diff.removed.length > 0) parts.push(TEXT.changes.remove(diff.removed.length));

    changes.textContent = diff.hasChanges ? TEXT.changes.summary(parts) : TEXT.changes.none;
    saveButton.disabled = this.#saving || !diff.hasChanges;
    this.#updateCount();
  }

  /** @param {SubmitEvent} event */
  async #handleSubmit(event) {
    event.preventDefault();
    if (this.#saving || !this.#selectedUser || !this.#baseline || !this.#draft) return;

    const diff = diffAccess(this.#baseline, this.#draft);
    if (!diff.hasChanges) return;

    const { username } = this.#selectedUser;
    this.#showSummary("");
    this.#setSaving(true);

    try {
      const { failures } = await applyAccessChanges({
        username,
        role: diff.roleChanged ? diff.role : "",
        assign: diff.added,
        remove: diff.removed,
      });

      const refreshed = await this.#refreshUsers(username);

      if (failures.length > 0) {
        this.#showSummary(TEXT.partialFailure(username, failures));
      } else if (!refreshed) {
        this.#showSummary(TEXT.refreshFailed);
      } else {
        this.#notify(TEXT.saved(username));
      }
    } catch (error) {
      // Falló el cambio de rol: no se aplicó nada.
      this.#showSummary(error?.message || MESSAGES.errors.unknown);
    } finally {
      this.#setSaving(false);
    }
  }

  /**
   * Relee los usuarios para que el estado guardado refleje lo que tiene el backend.
   * @param {string} username Usuario que se mantiene seleccionado.
   * @returns {Promise<boolean>}
   */
  async #refreshUsers(username) {
    try {
      const users = await listUsers();
      this.#users = users.filter((user) => !isProtectedRole(user.role));
      this.#fillUserOptions(username);
      this.#selectUser(username);
      return true;
    } catch {
      return false;
    }
  }

  /** @param {boolean} saving */
  #setSaving(saving) {
    const { userSelect, roleSelect, saveLabel, list } = this.#refs;

    this.#saving = saving;
    saveLabel.textContent = saving ? TEXT.saving : TEXT.save;
    userSelect.disabled = saving;
    roleSelect.disabled = saving || !this.#selectedUser;

    for (const checkbox of list.querySelectorAll("input[type='checkbox']")) {
      checkbox.disabled = saving;
    }

    this.#updateSummary();
  }

  /* ---------- Avisos ---------- */

  /** @param {string} message Vacío oculta el resumen de error. */
  #showSummary(message) {
    this.#refs.summary.textContent = message;
    this.#refs.summary.hidden = !message;
  }

  /** @param {string} message */
  #notify(message) {
    const { notice } = this.#refs;

    notice.textContent = message;
    notice.hidden = false;

    clearTimeout(this.#noticeTimer);
    this.#noticeTimer = setTimeout(() => {
      notice.hidden = true;
    }, NOTICE_DURATION_MS);
  }
}

customElements.define("cd-role-assignment", RoleAssignment);
