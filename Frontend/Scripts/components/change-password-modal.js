import { API_BASE_URL, TOKEN_STORAGE_KEY } from "../config/api.config.js";
import { attachStyles } from "../utils/shadow-styles.js";

const PASSWORD_REGEX = /^(?=.*[A-Z])(?=.*[^a-zA-Z0-9\s]).{6,}$/;

export class ChangePasswordModal extends HTMLElement {
  #root;
  #dialog = null;
  #form = null;
  #currentInput = null;
  #newInput = null;
  #confirmInput = null;
  #summaryEl = null;
  #submitBtn = null;
  #token = "";
  #resolvePromise = null;
  #initialized = null;

  constructor() {
    super();
    this.#root = this.attachShadow({ mode: "open" });
  }

  connectedCallback() {
    this.#initialized ??= this.#initialize();
  }

  async #initialize() {
    await attachStyles(this.#root, "change-password-modal.css");

    const template = document.createElement("template");
    template.innerHTML = `
      <dialog class="modal" id="change-pwd-dialog" aria-modal="true" aria-labelledby="modal-title">
        <form class="modal__form" method="dialog">
          <header class="modal__header">
            <span class="modal__badge">Primer Ingreso</span>
            <h2 class="modal__title" id="modal-title">Actualizar Contraseña</h2>
            <p class="modal__description">
              Por políticas de seguridad institucional, debes cambiar tu contraseña provisional antes de continuar a tu panel.
            </p>
          </header>

          <p class="modal__summary" id="modal-summary" role="alert"></p>

          <div class="modal__fields">
            <div class="field">
              <label class="field__label" for="current-password">Contraseña actual / provisional</label>
              <div class="field__control">
                <input id="current-password" name="currentPassword" type="password" class="field__input"
                  autocomplete="current-password" required placeholder="Ingresa tu clave actual" />
              </div>
              <p class="modal__error" id="current-error"></p>
            </div>

            <div class="field">
              <label class="field__label" for="new-password">Nueva contraseña</label>
              <div class="field__control">
                <input id="new-password" name="newPassword" type="password" class="field__input"
                  autocomplete="new-password" required placeholder="Mínimo 6 caracteres" />
              </div>
              <p class="field__hint">Debe incluir al menos una letra mayúscula y un carácter especial.</p>
              <p class="modal__error" id="new-error"></p>
            </div>

            <div class="field">
              <label class="field__label" for="confirm-password">Confirmar nueva contraseña</label>
              <div class="field__control">
                <input id="confirm-password" name="confirmPassword" type="password" class="field__input"
                  autocomplete="new-password" required placeholder="Repite la nueva contraseña" />
              </div>
              <p class="modal__error" id="confirm-error"></p>
            </div>
          </div>

          <div class="modal__actions">
            <button type="submit" class="modal__submit" id="submit-btn">
              <span>Guardar y Continuar</span>
            </button>
            <button type="button" class="modal__logout" id="logout-btn">
              Cancelar y cerrar sesión
            </button>
          </div>
        </form>
      </dialog>
    `;

    this.#root.appendChild(template.content.cloneNode(true));

    this.#dialog = this.#root.querySelector("#change-pwd-dialog");
    this.#form = this.#root.querySelector(".modal__form");
    this.#currentInput = this.#root.querySelector("#current-password");
    this.#newInput = this.#root.querySelector("#new-password");
    this.#confirmInput = this.#root.querySelector("#confirm-password");
    this.#summaryEl = this.#root.querySelector("#modal-summary");
    this.#submitBtn = this.#root.querySelector("#submit-btn");
    this.#dialog.addEventListener("cancel", (event) => {
      event.preventDefault();
    });

    this.#form.addEventListener("submit", (event) => {
      event.preventDefault();
      this.#handleSubmit();
    });

    const logoutBtn = this.#root.querySelector("#logout-btn");
    logoutBtn.addEventListener("click", () => {
      sessionStorage.removeItem(TOKEN_STORAGE_KEY);
      sessionStorage.removeItem("userRole");
      sessionStorage.removeItem("currentUser");
      sessionStorage.removeItem("username");
      window.location.reload();
    });
  }


  async promptChangePassword({ token, username, currentPassword = "" }) {
    await this.#initialized;
    this.#token = token;

    this.#currentInput.value = currentPassword;
    this.#newInput.value = "";
    this.#confirmInput.value = "";
    this.#summaryEl.textContent = "";
    this.#clearErrors();

    this.#dialog.showModal();

    if (currentPassword) {
      this.#newInput.focus();
    } else {
      this.#currentInput.focus();
    }

    return new Promise((resolve) => {
      this.#resolvePromise = resolve;
    });
  }

  #clearErrors() {
    this.#root.querySelectorAll(".modal__error").forEach((el) => (el.textContent = ""));
    this.#root.querySelectorAll(".field__input").forEach((el) => el.removeAttribute("aria-invalid"));
  }

  async #handleSubmit() {
    this.#clearErrors();
    this.#summaryEl.textContent = "";

    const currentPassword = this.#currentInput.value;
    const newPassword = this.#newInput.value;
    const confirmPassword = this.#confirmInput.value;

    let hasError = false;

    if (!currentPassword) {
      this.#setError(this.#currentInput, "current-error", "La contraseña actual es obligatoria.");
      hasError = true;
    }

    if (!newPassword) {
      this.#setError(this.#newInput, "new-error", "La nueva contraseña es obligatoria.");
      hasError = true;
    } else if (newPassword.length < 6) {
      this.#setError(this.#newInput, "new-error", "Debe tener al menos 6 caracteres.");
      hasError = true;
    } else if (!PASSWORD_REGEX.test(newPassword)) {
      this.#setError(this.#newInput, "new-error", "Debe incluir al menos una mayúscula y un carácter especial.");
      hasError = true;
    } else if (newPassword === currentPassword) {
      this.#setError(this.#newInput, "new-error", "La nueva contraseña debe ser diferente a la actual.");
      hasError = true;
    }

    if (!confirmPassword) {
      this.#setError(this.#confirmInput, "confirm-error", "Confirma tu nueva contraseña.");
      hasError = true;
    } else if (confirmPassword !== newPassword) {
      this.#setError(this.#confirmInput, "confirm-error", "Las contraseñas no coinciden.");
      hasError = true;
    }

    if (hasError) return;

    this.#setSubmitting(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/change-password`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${this.#token}`
        },
        body: JSON.stringify({
          currentPassword,
          newPassword
        })
      });

      if (!response.ok) {
        let errorMsg = "No se pudo actualizar la contraseña.";
        try {
          const errData = await response.json();
          if (errData?.error) errorMsg = errData.error;
          else if (errData?.message) errorMsg = errData.message;
        } catch {}
        this.#summaryEl.textContent = errorMsg;
        this.#setSubmitting(false);
        return;
      }

      // Success: close dialog and resolve promise to continue navigation
      this.#dialog.close();
      if (this.#resolvePromise) {
        this.#resolvePromise();
      }
    } catch (err) {
      console.error("Error al cambiar contraseña:", err);
      this.#summaryEl.textContent = "Error de conexión con el servidor. Inténtalo de nuevo.";
      this.#setSubmitting(false);
    }
  }

  #setError(input, errorId, message) {
    input.setAttribute("aria-invalid", "true");
    const errEl = this.#root.querySelector(`#${errorId}`);
    if (errEl) errEl.textContent = message;
  }

  #setSubmitting(isSubmitting) {
    this.#submitBtn.disabled = isSubmitting;
    this.#submitBtn.querySelector("span").textContent = isSubmitting
      ? "Guardando..."
      : "Guardar y Continuar";
  }
}

if (!customElements.get("cd-change-password-modal")) {
  customElements.define("cd-change-password-modal", ChangePasswordModal);
}
