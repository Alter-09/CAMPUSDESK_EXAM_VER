# Guía de Endpoints Pendientes por Consumir en el Frontend (CAMPUSDESK)

Este documento detalla los endpoints del backend que aún no han sido integrados en la interfaz de usuario (`Frontend/`), especificando las vistas HTML, componentes de interfaz (modales, botones) y flujos recomendados para su implementación.

---

## Tabla Resumen de Prioridades

| Prioridad | Endpoint | Ubicación en el Front | Tipo de Componente |
| :---: | :--- | :--- | :--- |
| 🔴 **Alta** | `POST /api/auth/change-password` | `main.js` / Todas las vistas | Modal obligatorio al detectar `mustChangePassword: true` y opción en perfil |
| 🟠 **Media** | `POST /api/auth/register` | `index.html` | Modal o pestaña de auto-registro en la pantalla de Login |
| 🟡 **Media** | `POST /api/user/{user}/reset-password` | `userlist.html` / `superadmin.html` | Botón en tabla de usuarios + modal de confirmación con clave temporal |
| 🟢 **Baja** | `PUT /api/user/update` | `userlist.html` | Modal de edición completa de perfil de usuario |

---

## 1. `POST /api/auth/register` (Auto-registro de Nuevo Usuario)

* **Propósito:** Permite que un visitante cree su propia cuenta con rol `USER` sin intervención de un administrador.
* **Dónde debería implementarse:**
  * **Vista HTML:** `Frontend/index.html` (pantalla de Login).
  * **Script JS:** En un nuevo módulo `Frontend/scripts/modules/register-modal.js` o integrado dentro de `Frontend/scripts/main.js`.
* **Componente de Interfaz Sugerido:**
  * Enlace o botón secundario en el formulario de login: *"¿No tienes cuenta? Regístrate aquí"*.
  * **Modal emergente de Registro** con los siguientes campos:
    1. **Nombre de usuario (`username`):** Campo de texto alfanumérico.
    2. **Correo electrónico (`email`):** Campo tipo email con validación Regex obligatoria (debe incluir `@` y formato de dominio).
    3. **Contraseña (`password`):** Campo tipo password con indicador visual de requisitos (mínimo 6 caracteres, al menos una mayúscula y un carácter especial).
* **Flujo UX/UI:**
  1. El usuario completa los campos del modal y pulsa "Crear Cuenta".
  2. El frontend realiza `fetch` a `POST /api/auth/register`.
  3. **Si responde `201 Created`:** Muestra una notificación (toast/alert) de éxito, cierra el modal y prellena el formulario de login con el usuario para que inicie sesión de inmediato.
  4. **Si responde `400` o `409 Conflict`:** Muestra el mensaje de error específico devuelto por la API (ej. *"El correo electrónico ya se encuentra registrado"*).

---

## 2. `POST /api/auth/change-password` (Cambio de Contraseña)

* **Propósito:** Cubre dos casos de uso fundamentales:
  1. **Obligatorio:** Cuando el usuario inicia sesión y la respuesta de `/api/auth/login` retorna `"mustChangePassword": true` (por primer ingreso tras creación o por reseteo administrativo).
  2. **Voluntario:** Cuando un usuario autenticado desea actualizar su clave desde su perfil.
* **Dónde debería implementarse:**
  * **Flujo Obligatorio:** En `Frontend/scripts/main.js` interceptando la respuesta del login. Si `mustChangePassword === true`, se activa un **modal bloqueante** (que no permite cerrar ni navegar) antes de dar paso a los dashboards (`user.html`, `technician.html`, `superadmin.html`).
  * **Flujo Voluntario:** En la barra superior de navegación o menú de perfil en las vistas protegidas (`user.html`, `technician.html`, `superadmin.html`).
* **Componente de Interfaz Sugerido:**
  * **Modal "Actualizar Contraseña"**:
    1. Contraseña actual (`currentPassword`).
    2. Nueva contraseña (`newPassword`).
    3. Confirmación de nueva contraseña.
* **Flujo UX/UI:**
  1. El usuario ingresa la clave actual y la nueva contraseña (debe cumplir los requisitos de seguridad).
  2. Se envía la petición a `POST /api/auth/change-password` con la cabecera `Authorization: Bearer <token>`.
  3. **Si responde `200 OK`:** 
     * Se actualiza la bandera local a `mustChangePassword = false`.
     * Se desbloquea la interfaz o se cierra el modal.
     * Muestra mensaje: *"Contraseña actualizada exitosamente. Ya puedes utilizar la aplicación."*

---

## 3. `POST /api/user/{username}/reset-password` (Restablecimiento Administrativo)

* **Propósito:** El Super Administrador restablece la contraseña de un usuario bloqueado u olvidado, asignándole una clave temporal (ej. `Temporal123!`) y forzando la bandera `mustChangePassword = true`.
* **Dónde debería implementarse:**
  * **Vista HTML:** `Frontend/userlist.html` o dentro del panel de administración en `Frontend/superadmin.html`.
  * **Script JS:** En `Frontend/scripts/services/user.service.js`.
* **Componente de Interfaz Sugerido:**
  * En cada fila de la tabla de usuarios, junto a los botones de "Eliminar" o "Roles", agregar un botón con icono de llave: **"Restablecer Clave"**.
  * **Modal de Confirmación de Reseteo:**
    * Muestra advertencia: *"¿Deseas restablecer la contraseña de [usuario]?"*.
    * Campo opcional para ingresar una contraseña temporal personalizada o dejar la automática por defecto (`Temporal123!`).
* **Flujo UX/UI:**
  1. El administrador confirma el reseteo.
  2. El frontend ejecuta `POST /api/user/{username}/reset-password` enviando el token de Admin.
  3. **Si responde `200 OK`:** Muestra un modal o diálogo emergente con la clave temporal generada para que el administrador pueda copiarla y facilitársela al usuario.

---

## 4. `PUT /api/user/update` (Actualización de Datos de Usuario)

* **Propósito:** Modificación de datos generales de cuentas existentes (correo institucional, estado de bloqueo `locked`, deshabilitado `disabled` o rol principal).
* **Dónde debería implementarse:**
  * **Vista HTML:** `Frontend/userlist.html`.
  * **Script JS:** En `Frontend/scripts/services/user.service.js`.
* **Componente de Interfaz Sugerido:**
  * Botón **"Editar"** en la fila del usuario que abra un modal prellenado con los valores actuales.
* **Flujo UX/UI:**
  1. El administrador modifica los datos requeridos.
  2. El frontend envía `PUT /api/user/update` con el payload de cambios.
  3. Al recibir `200 OK`, refresca la fila correspondiente en la tabla sin necesidad de recargar toda la página.
