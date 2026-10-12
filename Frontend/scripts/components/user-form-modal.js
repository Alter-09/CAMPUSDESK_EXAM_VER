/**
 * <cd-user-form-modal>: ventana para registrar un usuario y darle permisos.
 *
 * Contrato:
 *  - open(): abre el modal, reinicia el formulario y carga roles y permisos.
 *  - close(): lo cierra (no hace nada mientras se está enviando).
 *  - Emite "user-created" con detail.username cuando la API crea el usuario.
 *
 * Usa <dialog> nativo: Escape para cerrar, foco atrapado y capa superior sin código extra.
 */
import { MESSAGES } from "../constants/messages.es.js";
import { createUser } from "../services/user.service.js";
import { listAssignableRoles } from "../services/role.service.js";
import { listPermissions } from "../services/permission.service.js";
import { validateNewUser } from "../validators/new-user.validator.js";
import { attachStyles } from "../utils/shadow-styles.js";

const TEXT = MESSAGES.users.modal;
const ROLE_LABELS = MESSAGES.users.roleLabels;
const CONFLICT_STATUS = 409;
const FIELD_NAMES = ["username", "email", "role", "password"];

export const USER_CREATED_EVENT = "user-created";

/**
 * @param {{ name: "username" | "email" | "password", type: string, autocomplete: string }} field
 * @returns {string}
 */
function textFieldMarkup({ name, type, autocomplete }) {
  const { label, placeholder } = TEXT.fields[name];

  return `
    <div class="field">
      <label class="field__label" for="${name}">${label}</label>
      <div class="field__control">
        <input id="${name}" name="${name}" class="field__input" type="${type}"
          autocomplete="${autocomplete}" placeholder="${placeholder}" />
      </div>
      <p class="modal__error" id="${name}-error"></p>
    </div>
  `;
}

export class UserFormModal extends HTMLElement {
  #root;
  #refs = null;
  #initialization = null;
  #roles = [];
  #permissionCatalog = [];
  #catalogRequestId = 0;
  #catalogReady = false;
  #submitting = false;

  constructor() {
    super();
    this.#root = this.attachShadow({ mode: "open" });
  }

  connectedCallback() {
    this.#initialization ??= this.#initialize();
  }

  /** Abre el modal con el formulario limpio. Requiere que el elemento esté en el DOM. */
  async open() {
    await this.#initialization;

    const { dialog, inputs } = this.#refs;
    if (dialog.open) return;

    this.#resetForm();
    dialog.showModal();
    inputs.username.focus();

    await this.#loadCatalogs();
  }

  close() {
    if (this.#submitting) return;
    this.#refs.dialog.close();
  }

  async #initialize() {
    await attachStyles(this.#root, "user-form-modal.css");
    this.#root.append(this.#buildSkeleton());
    this.#refs = this.#collectRefs();
    this.#bindEvents();
  }

  #buildSkeleton() {
    const template = document.createElement("template");
    const { permissions } = TEXT.fields;

    template.innerHTML = `
      <dialog class="modal" aria-labelledby="modal-title">
        <form class="modal__form" novalidate>
          <header class="modal__header">
            <h2 id="modal-title" class="modal__title">${TEXT.title}</h2>
            <button class="modal__close" type="button" aria-label="${TEXT.close}">×</button>
          </header>

          <div class="modal__catalog" hidden>
            <p class="modal__catalog-message" role="status"></p>
            <button class="button modal__retry" type="button" hidden>
              <span class="button__label">${TEXT.retry}</span>
            </button>
          </div>

          <p class="modal__summary" role="alert" hidden></p>

          <div class="modal__fields">
            ${textFieldMarkup({ name: "username", type: "text", autocomplete: "off" })}
            ${textFieldMarkup({ name: "email", type: "email", autocomplete: "off" })}

            <div class="field">
              <label class="field__label" for="role">${TEXT.fields.role.label}</label>
              <div class="field__control">
                <select id="role" name="role" class="field__input modal__select" disabled>
                  <option value="" disabled selected>${TEXT.fields.role.placeholder}</option>
                </select>
              </div>
              <p class="modal__error" id="role-error"></p>
            </div>

            ${textFieldMarkup({ name: "password", type: "password", autocomplete: "new-password" })}

            <details class="picker">
              <summary class="picker__summary">
                <span class="picker__label">${permissions.label}</span>
                <span class="picker__count"></span>
              </summary>
              <p class="picker__hint"></p>
              <div class="picker__grid" role="group" aria-label="${permissions.label}"></div>
            </details>
          </div>

          <footer class="modal__actions">
            <button class="button modal__cancel" type="button">
              <span class="button__label">${TEXT.cancel}</span>
            </button>
            <button class="button button--primary modal__submit" type="submit" disabled>
              <span class="button__label">${TEXT.submit}</span>
            </button>
          </footer>
        </form>
      </dialog>
    `;

    return template.content;
  }

  #collectRefs() {
    const find = (selector) => this.#root.querySelector(selector);

    return {
      dialog: find(".modal"),
      form: find(".modal__form"),
      closeButton: find(".modal__close"),
      cancelButton: find(".modal__cancel"),
      submitButton: find(".modal__submit"),
      submitLabel: find(".modal__submit .button__label"),
      summary: find(".modal__summary"),
      catalog: find(".modal__catalog"),
      catalogMessage: find(".modal__catalog-message"),
      retryButton: find(".modal__retry"),
      picker: find(".picker"),
      pickerCount: find(".picker__count"),
      pickerHint: find(".picker__hint"),
      pickerGrid: find(".picker__grid"),
      inputs: Object.fromEntries(FIELD_NAMES.map((name) => [name, find(`#${name}`)])),
      errors: Object.fromEntries(FIELD_NAMES.map((name) => [name, find(`#${name}-error`)])),
    };
  }

  #bindEvents() {
    const { dialog, form, closeButton, cancelButton, retryButton, pickerGrid, inputs } = this.#refs;

    closeButton.addEventListener("click", () => this.close());
    cancelButton.addEventListener("click", () => this.close());
    retryButton.addEventListener("click", () => this.#loadCatalogs());

    // Escape no debe cerrar el modal mientras se envía el formulario.
    dialog.addEventListener("cancel", (event) => {
      if (this.#submitting) event.preventDefault();
    });

    form.addEventListener("submit", (event) => this.#handleSubmit(event));

    for (const name of FIELD_NAMES) {
      inputs[name].addEventListener("input", () => this.#clearFieldError(name));
    }

    inputs.role.addEventListener("change", () => this.#renderPermissionOptions());
    pickerGrid.addEventListener("change", () => this.#updatePickerCount());
  }

  /* ---------- Carga de roles y permisos ---------- */

  async #loadCatalogs() {
    const requestId = ++this.#catalogRequestId;

    this.#catalogReady = false;
    this.#syncControls();
    this.#showCatalogStatus(TEXT.loading);

    try {
      const [roles, permissions] = await Promise.all([listAssignableRoles(), listPermissions()]);
      if (requestId !== this.#catalogRequestId) return;

      this.#roles = roles;
      this.#permissionCatalog = permissions;
      this.#fillRoleOptions();
      this.#renderPermissionOptions();
      this.#catalogReady = true;
      this.#showCatalogStatus("");
    } catch (error) {
      if (requestId !== this.#catalogRequestId) return;

      const detail = error?.message ? ` ${error.message}` : "";
      this.#showCatalogStatus(`${TEXT.loadError}${detail}`, { retry: true });
    } finally {
      if (requestId === this.#catalogRequestId) this.#syncControls();
    }
  }

  /**
   * @param {string} message Vacío oculta el aviso.
   * @param {{ retry?: boolean }} [options]
   */
  #showCatalogStatus(message, { retry = false } = {}) {
    const { catalog, catalogMessage, retryButton } = this.#refs;

    catalogMessage.textContent = message;
    catalog.hidden = !message;
    retryButton.hidden = !retry;
  }

  #fillRoleOptions() {
    const placeholder = new Option(TEXT.fields.role.placeholder, "", true, true);
    placeholder.disabled = true;

    const options = this.#roles.map((role) => new Option(ROLE_LABELS[role.name] ?? role.name, role.name));
    this.#refs.inputs.role.replaceChildren(placeholder, ...options);
  }

  /* ---------- Permisos adicionales ---------- */

  /** Solo se ofrecen los permisos que el rol elegido todavía no trae. */
  #renderPermissionOptions() {
    const { pickerGrid, pickerHint, inputs } = this.#refs;
    const { permissions: text } = TEXT.fields;

    this.#clearFieldError("role");

    const stillSelected = new Set(this.#checkedPermissions());
    const role = this.#roles.find((item) => item.name === inputs.role.value);

    pickerGrid.replaceChildren();

    if (!role) {
      pickerHint.textContent = text.chooseRole;
      this.#updatePickerCount();
      return;
    }

    const inherited = new Set(role.permissions);
    const available = this.#permissionCatalog.filter((permission) => !inherited.has(permission));

    pickerHint.textContent = available.length > 0 ? text.hint : text.none;
    pickerGrid.append(...available.map((permission) => this.#buildPermissionOption(permission, stillSelected.has(permission))));
    this.#updatePickerCount();
  }

  /**
   * @param {string} permission
   * @param {boolean} checked
   */
  #buildPermissionOption(permission, checked) {
    const label = document.createElement("label");
    label.className = "permission-option";

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.name = "additionalPermissions";
    checkbox.value = permission;
    checkbox.checked = checked;

    const name = document.createElement("span");
    name.textContent = permission;

    label.append(checkbox, name);
    return label;
  }

  /** @returns {string[]} */
  #checkedPermissions() {
    return [...this.#refs.pickerGrid.querySelectorAll("input:checked")].map((input) => input.value);
  }

  #updatePickerCount() {
    const count = this.#checkedPermissions().length;
    this.#refs.pickerCount.textContent = count > 0 ? TEXT.fields.permissions.selected(count) : "";
  }

  /* ---------- Envío ---------- */

  /** @param {SubmitEvent} event */
  async #handleSubmit(event) {
    event.preventDefault();
    if (this.#submitting || !this.#catalogReady) return;

    const { inputs } = this.#refs;
    const { values, errors } = validateNewUser(
      {
        username: inputs.username.value,
        email: inputs.email.value,
        role: inputs.role.value,
        password: inputs.password.value,
      },
      { assignableRoleNames: this.#roles.map((role) => role.name) },
      TEXT.validation,
    );

    this.#renderErrors(errors);

    const firstInvalid = FIELD_NAMES.find((name) => errors[name]);
    if (firstInvalid) {
      inputs[firstInvalid].focus();
      return;
    }

    this.#setSubmitting(true);
    let created = false;

    try {
      await createUser({ ...values, additionalPermissions: this.#checkedPermissions() });
      created = true;
    } catch (error) {
      this.#handleSubmitError(error);
    } finally {
      this.#setSubmitting(false);
    }

    if (created) {
      this.close();
      this.dispatchEvent(
        new CustomEvent(USER_CREATED_EVENT, {
          detail: { username: values.username },
          bubbles: true,
          composed: true,
        }),
      );
    }
  }

  /** @param {{ status?: number, message?: string }} error */
  #handleSubmitError(error) {
    const message = error?.message || MESSAGES.errors.unknown;

    if (error?.status === CONFLICT_STATUS) {
      this.#showSummary("");
      this.#setFieldError("username", message);
      this.#refs.inputs.username.focus();
      return;
    }

    this.#showSummary(message);
  }

  /** @param {boolean} submitting */
  #setSubmitting(submitting) {
    this.#submitting = submitting;
    this.#refs.submitLabel.textContent = submitting ? TEXT.submitting : TEXT.submit;
    if (submitting) {
      this.#refs.submitButton.setAttribute("aria-busy", "true");
    } else {
      this.#refs.submitButton.removeAttribute("aria-busy");
    }
    this.#syncControls();
  }

  /** Habilita o bloquea los controles según haya catálogos cargados o un envío en curso. */
  #syncControls() {
    const { submitButton, cancelButton, closeButton, inputs } = this.#refs;

    submitButton.disabled = this.#submitting || !this.#catalogReady;
    inputs.role.disabled = this.#submitting || !this.#catalogReady;
    cancelButton.disabled = this.#submitting;
    closeButton.disabled = this.#submitting;
  }

  /* ---------- Errores y reinicio (mismo patrón que login-form.js) ---------- */

  /**
   * @param {string} name
   * @param {string} message Vacío limpia el error.
   */
  #setFieldError(name, message) {
    const input = this.#refs.inputs[name];
    const errorNode = this.#refs.errors[name];

    input.setAttribute("aria-invalid", message ? "true" : "false");
    errorNode.textContent = message;

    if (message) {
      input.setAttribute("aria-describedby", errorNode.id);
    } else {
      input.removeAttribute("aria-describedby");
    }
  }

  /** @param {string} name */
  #clearFieldError(name) {
    if (this.#refs.inputs[name].getAttribute("aria-invalid") === "true") {
      this.#setFieldError(name, "");
    }
  }

  /** @param {Partial<Record<string, string>>} errors */
  #renderErrors(errors) {
    for (const name of FIELD_NAMES) {
      this.#setFieldError(name, errors[name] ?? "");
    }

    const issues = FIELD_NAMES.map((name) => errors[name]).filter(Boolean);
    this.#showSummary(issues.length > 0 ? `${TEXT.validation.summaryPrefix} ${issues.join(" ")}` : "");
  }

  /** @param {string} message Vacío oculta el resumen. */
  #showSummary(message) {
    this.#refs.summary.textContent = message;
    this.#refs.summary.hidden = !message;
  }

  #resetForm() {
    this.#refs.form.reset();

    for (const name of FIELD_NAMES) {
      this.#setFieldError(name, "");
    }

    this.#showSummary("");
    this.#refs.picker.open = false;
    this.#renderPermissionOptions();
  }
}

customElements.define("cd-user-form-modal", UserFormModal);
