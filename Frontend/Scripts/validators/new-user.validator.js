/**
 * Reglas de validación del formulario "Registro de Nuevo Usuario".
 * Lógica pura: no toca el DOM ni la red.
 * El backend no valida la longitud de la contraseña (aceptó "123" en las pruebas),
 * por eso las reglas mínimas viven aquí y se ajustan en un solo lugar.
 */

export const USERNAME_MIN_LENGTH = 3;
export const PASSWORD_MIN_LENGTH = 8;

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const WHITESPACE_PATTERN = /\s/;
const PASSWORD_COMPLEXITY_PATTERN = /^(?=.*[A-Z])(?=.*[^a-zA-Z0-9\s])/;

/**
 * @typedef {{ username: string, email: string, role: string, password: string }} NewUserValues
 * @typedef {Partial<Record<keyof NewUserValues, string>>} NewUserErrors
 */

/**
 * @param {NewUserValues} rawValues Valores tal como están en el formulario.
 * @param {{ assignableRoleNames: string[] }} context
 * @param {{
 *   usernameRequired: string, usernameSpaces: string, usernameShort: (min: number) => string,
 *   emailRequired: string, emailInvalid: string,
 *   roleRequired: string, roleInvalid: string,
 *   passwordRequired: string, passwordShort: (min: number) => string,
 * }} messages
 * @returns {{ values: NewUserValues, errors: NewUserErrors }}
 */
export function validateNewUser(rawValues, { assignableRoleNames }, messages) {
  const values = {
    username: rawValues.username.trim(),
    email: rawValues.email.trim(),
    role: rawValues.role,
    password: rawValues.password,
  };

  /** @type {NewUserErrors} */
  const errors = {};

  if (!values.username) {
    errors.username = messages.usernameRequired;
  } else if (WHITESPACE_PATTERN.test(values.username)) {
    errors.username = messages.usernameSpaces;
  } else if (values.username.length < USERNAME_MIN_LENGTH) {
    errors.username = messages.usernameShort(USERNAME_MIN_LENGTH);
  }

  if (!values.email) {
    errors.email = messages.emailRequired;
  } else if (!EMAIL_PATTERN.test(values.email)) {
    errors.email = messages.emailInvalid;
  }

  if (!values.role) {
    errors.role = messages.roleRequired;
  } else if (!assignableRoleNames.includes(values.role)) {
    errors.role = messages.roleInvalid;
  }

  if (!values.password) {
    errors.password = messages.passwordRequired;
  } else if (values.password.length < PASSWORD_MIN_LENGTH) {
    errors.password = messages.passwordShort(PASSWORD_MIN_LENGTH);
  } else if (!PASSWORD_COMPLEXITY_PATTERN.test(values.password)) {
    errors.password = 'La contraseña debe contener al menos una letra mayúscula y un carácter especial.';
  }

  return { values, errors };
}
