# 01 · Especificación Funcional — CampusDesk Frontend (QUÉ)

| Campo | Valor |
|---|---|
| Versión | 0.1.0 |
| Fecha | 2026-10-08 |
| Depende de | `00-constitution.md` · `03-api-contract.md` · `06-design-system.md` |
| Marcas | ✅ confirmado · 🔶 propuesta no confirmada · ❓ pendiente (`Q-xx`) · 🔒 bloqueado |

---

## 1. Alcance

**Dentro:** interfaz, validación en cliente, consumo de API, sesión en cliente, navegación por rol, manejo de errores HTTP, UX/accesibilidad, entrega del frontend (README + evidencias).

**Fuera:** lógica de negocio, persistencia, emisión de JWT, Swagger, seguridad del servidor, recuperación de contraseña (no figura en el enunciado ni en la propuesta backend).

## 2. Roles

| Rol | Fuente | Qué ve/hace en la UI |
|---|---|---|
| **SUPERADMIN** ❓ | Sebbs | Único que crea usuarios y asigna permisos desde la interfaz. **No existe en el backend** (solo ADMIN/TECHNICIAN/USER) → `Q-01`, `Q-02` |
| **ADMIN** ✅ | RF-01 | Todos los tiquetes, técnicos, asignar/reasignar, consultar usuarios, estadísticas globales, historial de cualquier tiquete, comentar tiquetes activos |
| **TECHNICIAN** ✅ | RF-01 | Tiquetes asignados, detalle, cambiar estado (EN_PROCESO / RESUELTA), comentar, historial |
| **USER** ✅ | RF-01 | Crear tiquetes, ver solo los propios, editar mientras estén ABIERTA y sin asignar, comentar activos, ver estado/historial, cerrar cuando estén RESUELTA |

> El alcance de **datos** por rol lo filtra el backend. La UI solo decide qué **controles** mostrar.

## 3. Matriz estado × rol (guía de UX — la verdad la tiene el backend, `Q-19`)

Ciclo: **ABIERTA → ASIGNADA → EN_PROCESO → RESUELTA → CERRADA** (RF-04).

| Estado | USER (solicitante) | TECHNICIAN (asignado) | ADMIN |
|---|---|---|---|
| ABIERTA | Editar · Comentar | — | Asignar · Comentar |
| ASIGNADA | Comentar | **Iniciar** (→EN_PROCESO) · Comentar | Reasignar · Comentar |
| EN_PROCESO | Comentar | **Resolver** (→RESUELTA) · Comentar | Comentar · Reasignar ❓ |
| RESUELTA | **Confirmar cierre** (→CERRADA) · Comentar | Comentar | Comentar |
| CERRADA | Solo lectura | Solo lectura | Solo lectura |

Regla UI ✅: en CERRADA no hay edición ni formulario de comentarios (el backend responde 409 si se fuerza).

## 4. Mapa del sitio (MPA 🔶 `Q-12`)

| ID | Archivo | Acceso | Pantalla del enunciado (RT-06) | Fase |
|---|---|---|---|---|
| S-01 | `index.html` | Público | Pantalla 1 — Autenticación (**solo login**; el registro público queda fuera por decisión de Sebbs → `Q-05`) | F3 |
| S-02 | `pages/dashboard.html` | Autenticado | Pantalla 2 — Dashboard | F4 |
| S-03 | `pages/tickets.html` | Autenticado | Pantalla 3 — Gestión de tiquetes | F5 |
| S-04 | *Componente* `cd-ticket-form` (crear/editar) | Según rol | Pantalla 3 | F6 |
| S-05 | `pages/ticket-detail.html?id=` | Autenticado | Pantalla 4 — Detalle | F6–F7 |
| S-06 | `pages/admin.html` | ADMIN | Pantalla 5 — Administración | F7–F8 |
| S-07 | `pages/users.html` 🔒 | SUPERADMIN ❓ | (Requisito de Sebbs) | F8 |
| S-08 | `pages/errors/*.html` | Público | (Requisito de Sebbs) | F2 |
| S-09 | Shell/navegación (`cd-app-shell`) | Autenticado | Navegación por rol (Pantalla 2) | F4 |

---

## 5. Especificación por pantalla

### S-01 · Login (`index.html`)

- **Acceso:** público. Si ya hay sesión válida → redirige al destino por rol (`Q-12`).
- **Contenido:** campo correo, campo contraseña (con botón mostrar/ocultar accesible), botón "Ingresar". **Sin registro.** Sin "olvidé mi contraseña" (no contemplado en el enunciado ni en la propuesta backend; confirmar si se necesita).
- **Validación en cliente (antes de llamar a la API):** correo → obligatorio + formato; contraseña → obligatoria. *No* se valida complejidad en login (la política es del backend, `Q-17`).
- **Feedback explícito:** mensaje bajo cada campo inválido + resumen superior con enlaces a los campos; foco al primer inválido; `aria-invalid` + `aria-describedby`; el error de un campo se limpia al corregirlo.
- **Envío:** botón en estado "cargando" y deshabilitado (evita doble envío).
- **Respuestas:** éxito → guardar sesión → redirigir · 401 → mensaje **genérico** de credenciales incorrectas (no revelar si el correo existe), conservar correo, limpiar contraseña 🔶 · 400 → errores de campo si el servidor los envía · 5xx/red → mensaje de servicio no disponible sin perder lo digitado.
- **Sesión expirada:** si se llegó por token vencido/inválido, mostrar aviso "tu sesión expiró" (CP-16).

**Criterios de aceptación**
- [ ] S01-AC1 Login válido genera sesión y redirige (CP-03).
- [ ] S01-AC2 Campos vacíos → mensajes por campo **sin** petición en Network.
- [ ] S01-AC3 Correo con formato inválido → mensaje específico.
- [ ] S01-AC4 Credenciales incorrectas → mensaje genérico; foco gestionado.
- [ ] S01-AC5 Con sesión válida no se muestra el login.
- [ ] S01-AC6 No existe enlace ni formulario de registro.
- [ ] S01-AC7 Operable solo con teclado; labels leídos por lector de pantalla.
- [ ] S01-AC8 Token expirado/ inválido → llega aquí con aviso (CP-16).

### S-02 · Dashboard

- **Roles:** todos. El backend entrega los datos ya acotados (ADMIN global · TECHNICIAN asignados · USER propios).
- **Endpoint:** `GET /api/reports/summary` → `totalTickets, openTickets, inProgressTickets, resolvedTickets, closedTickets`.
- **UI:** tarjetas KPI por indicador; clic en una tarjeta → `tickets.html` filtrado por ese estado 🔶.
- **Gaps detectados (no implementar hasta resolver):**
  - No existe contador de **ASIGNADA** → el total no es la suma de las 4 tarjetas (`Q-23`).
  - `DESIGN.md` define selector de rango (segmented control) → podría mapear a `MetricPeriod` (TODAY/WEEK/MONTH/TOTAL) pero faltan los query params (`Q-23`).
  - `DESIGN.md` define **badge de tendencia (%)** y **sparkline** en el KPI → la API **no** entrega series ni comparación de periodos (`Q-38`).
- **Estados:** skeleton · error con "Reintentar" · vacío (todos en 0).
- **Optimización:** caché TTL corto + invalidación tras mutaciones; sin polling.

**Criterios de aceptación**
- [ ] S02-AC1 Los valores provienen de la API, nunca estáticos; coinciden con PostgreSQL (CP-14).
- [ ] S02-AC2 Un usuario USER/TECHNICIAN no ve valores globales (verifica el backend; la UI no recalcula).
- [ ] S02-AC3 Estados skeleton/error/vacío implementados.
- [ ] S02-AC4 Navegar de ida y vuelta no repite la petición dentro del TTL.

### S-03 · Listado de tiquetes

- **Endpoint:** `GET /api/tickets` con filtros (`status`, `priority`, `category`) y paginación (`Q-14`).
- **Filtros:** estado, prioridad, categoría; combinables; se procesan **en el servidor**; botón "Limpiar filtros".
- **URL como estado** 🔶: los filtros y la página viven en el query string (compartible, botón "atrás" funciona).
- **Paginación:** 100 % servidor; reiniciar a página 1 al cambiar filtros; indicador "Mostrando X–Y de N"; variantes ❓ `Q-15` (el diseño final puede combinar varias).
- **Listado:** columnas según Figma (`Q-09`); la fila es un enlace real `<a>` al detalle; acciones rápidas visibles también con foco de teclado y en táctil (no solo en hover).
- **Crear:** botón "Nuevo tiquete" según rol (`Q-25`) → abre `cd-ticket-form` (contenedor según Figma, `Q-29`).
- **Estados:** skeleton · vacío **diferenciado** ("aún no hay tiquetes" vs "ningún resultado para estos filtros") · error + reintentar.

**Criterios de aceptación**
- [ ] S03-AC1 Cada cambio de filtro dispara una sola petición vigente (las obsoletas se cancelan).
- [ ] S03-AC2 Recargar la página conserva filtros y página.
- [ ] S03-AC3 USER nunca ve tiquetes ajenos (CP-06, lo garantiza el backend).
- [ ] S03-AC4 Paginación coherente con la respuesta del backend (total, página, tamaño).

### S-04 · Formulario de tiquete — `cd-ticket-form` (Web Component + Shadow DOM)

- **Modos:** `create` y `edit`.
- **Campos 🔶/❓ (`Q-16`):** título, descripción, categoría (HARDWARE, SOFTWARE, REDES, ACCESOS, OTROS), prioridad (BAJA, MEDIA, ALTA, CRITICA).
- **Reglas de validación (límites pendientes):**

| Campo | Reglas | Límites |
|---|---|---|
| Título | obligatorio (sin solo espacios) · mín/máx | ❓ `Q-16` |
| Descripción | obligatoria (sin solo espacios) · máx | ❓ `Q-16` |
| Categoría | obligatoria · valor ∈ enum | ✅ enum |
| Prioridad | obligatoria · valor ∈ enum | ✅ enum |

- **Edición:** solo si el tiquete está ABIERTA, sin asignar y el usuario es el solicitante (RF-03). Si el servidor responde 403/409 → mensaje + refrescar el tiquete.
- **Comportamiento:** el componente valida y **emite un evento** con el payload ya validado; la **página** llama al servicio (el componente no hace `fetch`).
- **Mejoras UX 🔶:** no enviar si en edición no hubo cambios; confirmar al cerrar con cambios sin guardar.

**Criterios de aceptación**
- [ ] S04-AC1 Cada campo inválido muestra qué falta o qué está mal, bajo el campo y en el resumen.
- [ ] S04-AC2 Con datos inválidos no se envía ninguna petición.
- [ ] S04-AC3 Crear exitoso → tiquete queda ABIERTA (CP-05), feedback de éxito y lista actualizada.
- [ ] S04-AC4 Errores de campo del servidor se mapean al campo correspondiente.
- [ ] S04-AC5 El componente funciona aislado (Shadow DOM) sin romper estilos de la página ni heredar los suyos por accidente.

### S-05 · Detalle del tiquete

- **Endpoints (en paralelo):** `GET /api/tickets/{id}` · `/comments` · `/history`; acciones: `PATCH …/status`, `PATCH …/assign`, `PUT …`.
- **Secciones:** encabezado (id, título, estado, prioridad, categoría) · información (solicitante, técnico, fechas) · descripción · acciones por rol/estado · comentarios (cronológicos + formulario) · historial (línea de tiempo).
- **Acciones:** Editar · Iniciar · Resolver · **Confirmar cierre** (con diálogo de confirmación) · Asignar/Reasignar (ADMIN).
- **Fallo parcial:** si comentarios o historial fallan, el detalle sigue visible y esa sección muestra error con "Reintentar".
- **Concurrencia:** 409 → mensaje claro + recarga del estado actual (la UI puede estar desactualizada).
- **Comentarios:** obligatorio (sin solo espacios), máximo ❓ `Q-22`; se renderizan como **texto** (nunca HTML).
- **Recurso ajeno o inexistente:** 403/404 → página de error correspondiente (CP-06).

**Criterios de aceptación**
- [ ] S05-AC1 Las 3 cargas son paralelas y una falla no tumba las demás.
- [ ] S05-AC2 Solo se ofrecen las acciones válidas para rol + estado (matriz §3).
- [ ] S05-AC3 Cambio de estado refleja el historial inmediatamente después (CP-11).
- [ ] S05-AC4 Cerrar tiquete pide confirmación (CP-12).
- [ ] S05-AC5 En CERRADA no hay acciones ni formulario de comentario (CP-13).
- [ ] S05-AC6 Un `<script>`/`<img onerror>` escrito en un comentario se muestra como texto inofensivo.

### S-06 · Administración (ADMIN)

- Seguimiento de tiquetes (reutiliza el listado S-03; vista "sin asignar" ❓) · directorio de técnicos (`GET /api/users/technicians`: `id, fullName, email, activeTicketsAssigned`) · asignar/reasignar (`PATCH …/assign`).
- Selector de técnico con carga de trabajo visible 🔶 (`Q-24`); reasignación con justificación ❓ (`Q-20`).
- **Criterios:** [ ] S06-AC1 Solo ADMIN accede (guarda de página + 403 del backend) · [ ] S06-AC2 Asignar deja el tiquete ASIGNADA (CP-07) · [ ] S06-AC3 Tras asignar se invalidan caché de técnicos, listado y dashboard.

### S-07 · Gestión de usuarios (SUPERADMIN) 🔒

Según Sebbs: único que crea usuarios y asigna permisos **desde la interfaz**. Todo lo demás depende de `Q-01`, `Q-02`, `Q-03`, `Q-17`: listado, creación (nombre, correo, contraseña, rol/permisos), edición, activar/desactivar. Los correos duplicados deben mostrar error claro en el campo (CP-02).

### S-08 · Páginas de error HTTP personalizadas

- Propuesta 🔶 (`Q-13`): páginas dedicadas para **401, 403, 404, 500** y **sin conexión/timeout**; **400 y 409 no** son página (se muestran inline/toast).
- Cada página: código, título y explicación en español, acción principal (inicio / login / reintentar) y secundaria; **sin** trazas ni datos técnicos; usa tokens de `06`; accesible.
- Navegación a error con `returnUrl` cuando aplica; protección contra bucles de redirección (p. ej. 401 estando ya en login).
- URLs inexistentes del propio sitio dependen de la configuración del servidor estático (`Q-08`).
- **Criterio:** [ ] S08-AC1 Cada código mapeado muestra su página y nunca expone información interna.

### S-09 · Shell y navegación

- Encabezado con nombre/rol del usuario (fuente: `Q-04`), navegación **según rol** (una sola matriz rol→enlaces), página activa con `aria-current`, cerrar sesión, contenedor de notificaciones (toasts).
- Responsive según `DESIGN.md`: ≥1280 riel lateral 64 px/260 px colapsable · 768–1279 drawer off-canvas · <768 columna única (detalle del menú móvil: Figma, `Q-36`).
- **Criterio:** [ ] S09-AC1 Un USER no ve enlaces de ADMIN y, aun escribiendo la URL, la guarda de página lo bloquea.

---

## 6. Validación de formularios — reglas transversales

| Aspecto | Regla |
|---|---|
| **Cuándo** | Al enviar (todos los campos) · al salir de un campo (blur) · al escribir **solo** si el campo ya mostró error |
| **Dónde** | Mensaje bajo el campo + resumen superior con enlaces + foco al primer inválido |
| **Cómo** | `aria-invalid`, `aria-describedby`, resumen en región `aria-live`; el color nunca es el único indicador |
| **Texto** | Específico ("El título es obligatorio", no "Campo inválido"); definido en `messages.es.js` (`Q-30`) |
| **Datos** | `trim()` antes de validar; el valor enviado es el normalizado |
| **Servidor** | Los errores de campo del 400 se mapean al input correspondiente (`Q-07`); si no hay mapeo, mensaje general del formulario |
| **Estado** | Se conserva lo digitado ante cualquier error (excepto contraseña tras 401) |

| Formulario | Campos y reglas |
|---|---|
| Login | correo: obligatorio + formato · contraseña: obligatoria |
| Tiquete | ver S-04 |
| Comentario | contenido obligatorio (trim) · máx ❓ `Q-22` |
| Asignación | técnico obligatorio · justificación en reasignación ❓ `Q-20` |
| Cambio de estado | comentario/justificación ❓ `Q-21` |
| Usuario (S-07) | nombre · correo · contraseña (política `Q-17`) · rol/permisos (`Q-02`) |

## 7. Manejo de errores HTTP en la UI 🔶 (`Q-13`)

| Status | Origen (backend) | Comportamiento propuesto |
|---|---|---|
| 400 | Validación / asignación inválida | Errores inline en campos o mensaje de formulario. **No** página |
| 401 | Credenciales / token ausente, expirado o adulterado | En login: mensaje de credenciales. Resto: limpiar sesión → login con aviso de sesión expirada |
| 403 | Sin permiso | Página 403 si es navegación a un recurso; toast/inline si es acción puntual |
| 404 | Recurso inexistente | Página 404 si es el recurso principal; mensaje inline si es secundario |
| 409 | Transición inválida / tiquete cerrado | Mensaje inline + recargar estado del tiquete. **No** página |
| 500 | Error del servidor | Página 500 si falla la carga principal; toast con "Reintentar" si es una acción |
| Red / timeout / offline | Sin respuesta | Estado/página "sin conexión" con reintento |
| Respuesta inesperada (no JSON) | — | Tratar como 500 genérico |

## 8. Trazabilidad CP → Frontend

| CP | Cómo se demuestra desde el frontend |
|---|---|
| CP-01 / 02 | ❓ El registro público no existe (`Q-05`). Se cubriría con el formulario de creación de usuario de S-07 (duplicado → error en el campo) |
| CP-03 | S-01 con credenciales válidas |
| CP-04 | Borrar el token y abrir una página protegida → redirección a login (+ 401 manejado) |
| CP-05 | S-04 crear → aparece ABIERTA en S-03 |
| CP-06 | Como USER abrir `ticket-detail.html?id=<ajeno>` → página 403/404 |
| CP-07 | S-06/S-05 asignar técnico → ASIGNADA |
| CP-08 | La UI **oculta** el control; el 403 se demuestra forzando la llamada (consola/Swagger) y verificando que la UI lo maneja |
| CP-09 | Igual que CP-08 (la UI no ofrece la acción; se maneja el 403) |
| CP-10 | La UI no ofrece el salto; el 409 se demuestra con Swagger y, en la UI, con estado desactualizado (dos sesiones) |
| CP-11 | S-05 historial tras cada cambio de estado |
| CP-12 | S-05 "Confirmar cierre" como solicitante en RESUELTA |
| CP-13 | S-05 en CERRADA: sin formulario; 409 manejado si se fuerza |
| CP-14 | S-02 vs consulta SQL del trainer |
| CP-15 | Toda la app en modo real sin errores CORS |
| CP-16 | Token inválido/expirado → login con aviso |

## 9. Requisitos del enunciado cubiertos (RT-06 y afines)

`fetch()` + `async/await` ✅ (C-07) · errores de comunicación ✅ (§7) · mensajes de éxito ✅ (C-12) · indicadores de carga ✅ (C-12) · sin datos estáticos como sustituto ✅ (C-10) · manejo del token y cierre de sesión ✅ (02 §7) · README del frontend + evidencias (F9).

---

## Historial de cambios

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1.0 | 2026-10-08 | Versión inicial |
