/**
 * Login form behavior.
 * Validates presence of credentials and emits a clean payload.
 * Auth transport stays out of this module (Open/Closed for future API wiring).
 */

/**
 * @typedef {{ username: string, password: string }} LoginCredentials
 */

/**
 * @param {HTMLFormElement} form
 * @param {{ onSubmit?: (credentials: LoginCredentials) => void | Promise<void> }} [options]
 */
export function bindLoginForm(form, options = {}) {
  if (!(form instanceof HTMLFormElement)) {
    return;
  }

  const usernameInput = form.querySelector("#username");
  const passwordInput = form.querySelector("#password");
  const submitButton = form.querySelector('[type="submit"]');

  if (
    !(usernameInput instanceof HTMLInputElement) ||
    !(passwordInput instanceof HTMLInputElement)
  ) {
    return;
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const credentials = {
      username: usernameInput.value.trim(),
      password: passwordInput.value,
    };

    if (!credentials.username || !credentials.password) {
      form.reportValidity();
      return;
    }

    if (submitButton instanceof HTMLButtonElement) {
      submitButton.disabled = true;
    }

    try {
      if (typeof options.onSubmit === "function") {
        await options.onSubmit(credentials);
      }
    } finally {
      if (submitButton instanceof HTMLButtonElement) {
        submitButton.disabled = false;
      }
    }
  });
}
