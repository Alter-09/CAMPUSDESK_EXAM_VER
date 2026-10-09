/**
 * Password visibility toggle.
 * Single responsibility: sync button pressed state with an input type.
 */

/**
 * @param {HTMLButtonElement} button
 * @param {HTMLInputElement} input
 */
export function bindPasswordToggle(button, input) {
  if (!(button instanceof HTMLButtonElement) || !(input instanceof HTMLInputElement)) {
    return;
  }

  const revealLabel = button.dataset.labelShow ?? "Mostrar contraseña";
  const hideLabel = button.dataset.labelHide ?? "Ocultar contraseña";

  const sync = () => {
    const isVisible = input.type === "text";
    button.setAttribute("aria-pressed", String(isVisible));
    button.setAttribute("aria-label", isVisible ? hideLabel : revealLabel);
  };

  button.addEventListener("click", () => {
    input.type = input.type === "password" ? "text" : "password";
    sync();
  });

  sync();
}
