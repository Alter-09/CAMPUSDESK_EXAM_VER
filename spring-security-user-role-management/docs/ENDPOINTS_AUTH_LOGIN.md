# Guía de Endpoints de Autenticación y Login (CAMPUSDESK)

Este documento detalla todos los endpoints relacionados con el ciclo de vida de autenticación, login, registro, cambio y restablecimiento de contraseñas en el backend.

---

## Estado de Consumo en el Frontend (Auth & Sesión)

A continuación se muestra el estado de integración de cada endpoint de autenticación con la interfaz del Frontend (`Frontend/`):

| Método | Endpoint | Estado Frontend | Archivo / Componente Frontend | Observación / Pendiente |
| :--- | :--- | :---: | :--- | :--- |
| **POST** | `/api/auth/login` | 🟢 **CONSUMIDO** | `scripts/main.js` (L-112) | Envía identificador y contraseña desde el formulario principal. |
| **GET** | `/api/auth/me` | 🟢 **CONSUMIDO** | `scripts/main.js` (L-164) | Obtiene rol y permisos del usuario tras autenticarse con éxito. |
| **POST** | `/api/auth/register` | 🔴 **NO CONSUMIDO** | *Pendiente* | El Frontend aún no tiene vista o modal de registro para auto-crear cuentas `USER`. |
| **POST** | `/api/auth/change-password` | 🔴 **NO CONSUMIDO** | *Pendiente* | Falta la pantalla/modal para cambio de clave obligatorio (`mustChangePassword`) o voluntario. |
| **POST** | `/api/user/{username}/reset-password` | 🔴 **NO CONSUMIDO** | *Pendiente* | Falta el botón o acción en la vista de administración para resetear claves de usuarios. |

---

## Tabla Resumen de Todos los Endpoints del Backend vs Frontend

| Módulo | Método | Endpoint | ¿Consumido por Frontend? | Dónde se consume en el Frontend |
| :--- | :--- | :--- | :---: | :--- |
| **Auth** | `POST` | `/api/auth/login` | 🟢 Sí | `scripts/main.js` |
| **Auth** | `GET` | `/api/auth/me` | 🟢 Sí | `scripts/main.js` |
| **Auth** | `POST` | `/api/auth/register` | 🔴 No | *Sin interfaz visual aún* |
| **Auth** | `POST` | `/api/auth/change-password` | 🔴 No | *Sin interfaz visual aún* |
| **User** | `GET` | `/api/user/all` | 🟢 Sí | `user.service.js`, `ticket-assign.js` |
| **User** | `POST` | `/api/user/add` | 🟢 Sí | `user.service.js` |
| **User** | `PUT` | `/api/user/update` | 🔴 No | *Gestión completa por micro-acciones* |
| **User** | `DELETE` | `/api/user/delete/{name}` | 🟢 Sí | `user.service.js` |
| **User** | `POST` | `/api/user/assignRole` | 🟢 Sí | `access.service.js` |
| **User** | `POST` | `/api/user/assignPermission` | 🟢 Sí | `access.service.js` |
| **User** | `DELETE` | `/api/user/{user}/permissions/{perm}` | 🟢 Sí | `access.service.js` |
| **User** | `POST` | `/api/user/{user}/reset-password` | 🔴 No | *Sin botón de reseteo en UI* |
| **Catalog** | `GET` | `/api/roles` | 🟢 Sí | `role.service.js` |
| **Catalog** | `GET` | `/api/permissions` | 🟢 Sí | `permission.service.js` |
| **Tickets** | `GET` | `/api/tickets` | 🟢 Sí | `ticket.service.js`, `ticket-list.js`, `ticket-assign.js` |
| **Tickets** | `POST` | `/api/tickets` | 🟢 Sí | `ticket.service.js`, `create-ticket.html` |
| **Tickets** | `GET` | `/api/tickets/{id}` | 🟢 Sí | `ticket.service.js` |
| **Tickets** | `PUT` | `/api/tickets/{id}` | 🟢 Sí | `ticket.service.js` |
| **Tickets** | `PATCH` | `/api/tickets/{id}/assign` | 🟢 Sí | `ticket.service.js`, `ticket-assign.js` |
| **Lifecycle** | `PATCH` | `/api/tickets/{id}/status` | 🟢 Sí | `ticket.service.js` |
| **Lifecycle** | `POST` | `/api/tickets/{id}/transitions`| 🟢 Sí | `ticket-assign.js` |
| **Comments**| `GET` | `/api/tickets/{id}/comments` | 🟢 Sí | `ticket.service.js` |
| **Comments**| `POST` | `/api/tickets/{id}/comments` | 🟢 Sí | `ticket.service.js` |
| **History** | `GET` | `/api/tickets/{id}/history` | 🟢 Sí | `ticket.service.js` |
| **Dashboard**| `GET` | `/api/dashboard/stats` | 🟢 Sí | `revenue-chart.js` |
| **Dashboard**| `GET` | `/api/reports/summary` | 🟢 Sí | `revenue-chart.js`, `Specs/` |

---

## Detalle de Endpoints

### 1. Iniciar Sesión (Login)
* **URL:** `/api/auth/login`
* **Método:** `POST`
* **Acceso:** Público (Sin token)
* **Descripción:** Valida credenciales contra la base de datos y genera un token JWT con vigencia configurada.

#### Payload de Entrada (Request Body)
El backend es flexible y acepta el identificador en cualquiera de los siguientes campos: `username`, `email` o `identifier`:

```json
{
  "username": "usuario_o_correo@technova.com",
  "password": "Password123!"
}
```

#### Validaciones Aplicadas:
* **Identificador (`username`/`email`):** Obligatorio (no nulo ni vacío).
* **Contraseña (`password`):** 
  * Obligatoria.
  * Valida mediante **Regex**: mínimo 6 caracteres, al menos una letra mayúscula y al menos un carácter especial (`^(?=.*[A-Z])(?=.*[^a-zA-Z0-9\s]).{6,}$`).

#### Respuestas (Responses)
* **`200 OK`**: Credenciales correctas.
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "usuario_test1",
    "mustChangePassword": false
  }
  ```
* **`400 BAD REQUEST`**: Datos incompletos o contraseña no cumple el formato Regex.
  ```json
  {
    "error": "La contraseña debe tener al menos 6 caracteres, al menos una letra mayúscula y al menos un carácter especial"
  }
  ```
* **`401 UNAUTHORIZED`**: Credenciales incorrectas o cuenta bloqueada/deshabilitada.
  ```json
  {
    "error": "Credenciales invalidas o cuenta no disponible"
  }
  ```

---

### 2. Registro de Usuario (Register)
* **URL:** `/api/auth/register`
* **Método:** `POST`
* **Acceso:** Público
* **Descripción:** Crea un nuevo usuario en el sistema con rol `USER` asignado por defecto.

#### Payload de Entrada (Request Body)
```json
{
  "username": "nuevo_usuario",
  "email": "nuevo_usuario@technova.com",
  "password": "Password123!"
}
```

#### Validaciones Aplicadas:
* **`username`**: Obligatorio, máximo 50 caracteres y no debe existir previamente.
* **`email`**: 
  * Obligatorio y único en el sistema.
  * Valida mediante **Regex**: formato de correo con `@` y dominio válido (`^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`).
* **`password`**: Obligatoria, mínimo 6 caracteres (se almacena encriptada con BCrypt).

#### Respuestas (Responses)
* **`201 CREATED`**: Usuario creado exitosamente.
  ```json
  {
    "message": "Usuario registrado exitosamente con rol USER",
    "username": "nuevo_usuario",
    "email": "nuevo_usuario@technova.com",
    "role": "USER"
  }
  ```
* **`400 BAD REQUEST`**: Campos faltantes o formato de correo inválido.
  ```json
  {
    "error": "El correo electrónico debe tener un formato válido"
  }
  ```
* **`409 CONFLICT`**: El nombre de usuario o correo ya está registrado.
  ```json
  {
    "message": "El nombre de usuario ya se encuentra registrado"
  }
  ```

---

### 3. Cambio de Contraseña (Change Password)
* **URL:** `/api/auth/change-password`
* **Método:** `POST`
* **Acceso:** Autenticado (`Authorization: Bearer <token>`)
* **Descripción:** Utilizado para el cambio voluntario de contraseña o en el primer ingreso si `mustChangePassword` es `true`.

#### Headers Requeridos
```http
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

#### Payload de Entrada (Request Body)
```json
{
  "currentPassword": "Password123!",
  "newPassword": "NuevaPassword456!"
}
```

#### Validaciones:
* Ambas contraseñas son obligatorias.
* La contraseña actual debe coincidir con la registrada en base de datos.
* La nueva contraseña debe tener al menos 6 caracteres y ser diferente a la actual.

#### Respuestas (Responses)
* **`200 OK`**: Contraseña actualizada.
  ```json
  {
    "message": "Contraseña actualizada exitosamente. Ya puedes utilizar la aplicación."
  }
  ```
* **`400 BAD REQUEST`**: Contraseña actual incorrecta o nueva contraseña igual a la actual.
  ```json
  {
    "message": "La contraseña actual es incorrecta"
  }
  ```
* **`401 UNAUTHORIZED`**: Token JWT ausente o inválido.

---

### 4. Consultar Sesión Actual (Me)
* **URL:** `/api/auth/me`
* **Método:** `GET`
* **Acceso:** Autenticado (`Authorization: Bearer <token>`)
* **Descripción:** Retorna la identidad, rol asignado, permisos heredados y permisos efectivos del usuario en sesión.

#### Headers Requeridos
```http
Authorization: Bearer <token_jwt>
```

#### Respuestas (Responses)
* **`200 OK`**:
  ```json
  {
    "username": "usuario_test1",
    "role": "USER",
    "inheritedPermissions": [
      "COMMENT_CREATE",
      "COMMENT_READ",
      "HISTORY_READ",
      "TICKET_CLOSE",
      "TICKET_CREATE",
      "TICKET_READ_OWN",
      "TICKET_UPDATE_OWN"
    ],
    "additionalPermissions": [],
    "effectivePermissions": [
      "COMMENT_CREATE",
      "COMMENT_READ",
      "HISTORY_READ",
      "TICKET_CLOSE",
      "TICKET_CREATE",
      "TICKET_READ_OWN",
      "TICKET_UPDATE_OWN"
    ]
  }
  ```

---

### 5. Restablecimiento Administrativo de Contraseña
* **URL:** `/api/user/{username}/reset-password`
* **Método:** `POST`
* **Acceso:** Requiere rol `ADMIN` o permiso `USER_UPDATE`
* **Descripción:** Un administrador restablece la contraseña de un usuario bloqueado u olvidado, asignando una clave temporal y activando `mustChangePassword: true`.

#### Payload de Entrada (Opcional)
```json
{
  "temporaryPassword": "TemporalSegura123!"
}
```
*(Si no se envía cuerpo, el sistema asigna `Temporal123!` por defecto)*.

#### Respuestas (Responses)
* **`200 OK`**:
  ```json
  {
    "message": "Contraseña restablecida con éxito para el usuario: nombre_usuario",
    "temporaryPassword": "TemporalSegura123!",
    "mustChangePassword": true
  }
  ```
