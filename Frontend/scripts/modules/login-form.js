/**
 * Comportamiento del formulario de login.
 * Valida presencia y emite credenciales limpias (S-01).
 * El transporte HTTP queda fuera (OCP para cablear AuthService después).
 */

/**
 * @typedef {{ username: string, password: string }} LoginCredentials
 */

/**
 * @param {HTMLFormElement} form
 * @param {{
 *   onSubmit?: (credentials: LoginCredentials) => void | Promise<void>,
 *   messages?: {
 *     usernameRequired: string,
 *     passwordRequired: string,
 *     summaryPrefix: string,
 *     submitting: string,
 *     submit: string,
 *   },
 * }} [options]
 */
export function bindLoginForm(form, options = {}) {
  if (!(form instanceof HTMLFormElement)) {
    return;
  }

  const usernameInput = form.querySelector("#username");
  const passwordInput = form.querySelector("#password");
  const submitButton = form.querySelector('[type="submit"]');
  const summary = form.querySelector("#login-form-summary");
  const messages = options.messages;

  if (!(usernameInput instanceof HTMLInputElement) || !(passwordInput instanceof HTMLInputElement)) {
    return;
  }

  /**
   * @param {HTMLInputElement} input
   * @param {string} message
   */
  const setFieldError = (input, message) => {
    const errorId = `${input.id}-error`;
    const errorNode = form.querySelector(`#${errorId}`);
    input.setAttribute("aria-invalid", message ? "true" : "false");

    if (errorNode instanceof HTMLElement) {
      errorNode.textContent = message;
    }

    if (message) {
      input.setAttribute("aria-describedby", errorId);
    } else {
      input.removeAttribute("aria-describedby");
    }
  };

  const clearErrors = () => {
    setFieldError(usernameInput, "");
    setFieldError(passwordInput, "");
    if (summary instanceof HTMLElement) {
      summary.textContent = "";
      summary.classList.remove("is-visible");
    }
  };

  /**
   * @returns {{ valid: boolean, credentials: LoginCredentials }}
   */
  const validate = () => {
    clearErrors();

    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    /** @type {string[]} */
    const issues = [];

    if (!username) {
      const message = messages?.usernameRequired ?? "El usuario es obligatorio.";
      setFieldError(usernameInput, message);
      issues.push(message);
    }

    if (!password) {
      const message = messages?.passwordRequired ?? "La contraseña es obligatoria.";
      setFieldError(passwordInput, message);
      issues.push(message);
    }

    if (issues.length > 0 && summary instanceof HTMLElement) {
      const prefix = messages?.summaryPrefix ?? "Revisa los siguientes campos:";
      summary.textContent = `${prefix} ${issues.join(" ")}`;
      summary.classList.add("is-visible");
    }

    return {
      valid: issues.length === 0,
      credentials: { username, password },
    };
  };

  usernameInput.addEventListener("input", () => {
    if (usernameInput.getAttribute("aria-invalid") === "true") {
      setFieldError(usernameInput, "");
    }
  });

  passwordInput.addEventListener("input", () => {
    if (passwordInput.getAttribute("aria-invalid") === "true") {
      setFieldError(passwordInput, "");
    }
  });

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const { valid, credentials } = validate();
    if (!valid) {
      const firstInvalid = form.querySelector('[aria-invalid="true"]');
      if (firstInvalid instanceof HTMLElement) {
        firstInvalid.focus();
      }
      return;
    }

    const label = submitButton?.querySelector(".button__label");
    const idleLabel = messages?.submit ?? label?.textContent ?? "Ingresar";

    if (submitButton instanceof HTMLButtonElement) {
      submitButton.disabled = true;
      submitButton.setAttribute("aria-busy", "true");
      if (label) {
        label.textContent = messages?.submitting ?? "Ingresando…";
      }
    }

    try {
      if (typeof options.onSubmit === "function") {
        await options.onSubmit(credentials);
      }
    } finally {
      if (submitButton instanceof HTMLButtonElement) {
        submitButton.disabled = false;
        submitButton.removeAttribute("aria-busy");
        if (label) {
          label.textContent = idleLabel;
        }
      }
    }
  });
}