# 02 · Plan Técnico — CampusDesk Frontend (CÓMO)

| Campo | Valor |
|---|---|
| Versión | 0.1.0 |
| Fecha | 2026-10-08 |
| Depende de | `00-constitution.md` · `01-spec.md` · `03-api-contract.md` · `06-design-system.md` |
| Estado | Todo lo marcado 🔶 requiere confirmación antes de implementarse |

---

## 1. Decisiones de arquitectura y su razonamiento

| # | Decisión | Por qué | Estado |
|---|---|---|---|
| A-1 | **MPA** (varias páginas HTML) con **módulos ES**; sin router | Sebbs describe `index.html` como login y páginas específicas de error → encaja con páginas reales. Cada página importa solo lo que usa (carga perezosa natural). Costo: el "shell" se repetiría en cada HTML → se resuelve con un Web Component (`cd-app-shell`) | 🔶 `Q-12` |
| A-2 | **Capa HTTP con transporte inyectable** (`FetchTransport` / `MockTransport`) | Los placeholders de hoy y el Swagger real de mañana se intercambian **sin tocar servicios ni páginas** (DIP/OCP) | 🔶 `Q-10` |
| A-3 | **Servicios por módulo del backend** (ticket, lifecycle, assignment, comment, statushistory, dashboard, technician + auth/users) | Espejo 1:1 del backend: un cambio en un módulo toca un solo servicio (SRP) | ✅ derivado de `CAMPUSDESK_BACKEND.md` |
| A-4 | **Mappers DTO → modelo de UI** en un único lugar | El Swagger final puede renombrar campos; solo cambian los mappers | 🔶 |
| A-5 | **Componentes presentacionales vs. páginas orquestadoras** | Los Web Components reciben datos y emiten eventos; las páginas llaman a los servicios. Componentes reutilizables y probables aislados | 🔶 |
| A-6 | **Composition root** (`js/core/container.js`) | Único lugar donde se instancian y conectan dependencias (inyección sin framework) | 🔶 |
| A-7 | **Login y páginas "normales" en Light DOM; Shadow DOM solo en componentes de tiquetes (+ los de apoyo que se confirmen)** | Los gestores de contraseñas y el autocompletado funcionan mejor en Light DOM; Sebbs pidió Shadow DOM para tiquetes | ✅ tiquetes · 🔶 resto |

## 2. Estructura de carpetas 🔶

```text
campusdesk-frontend/
├── index.html                      # Login (único punto de entrada público)
├── pages/
│   ├── dashboard.html
│   ├── tickets.html
│   ├── ticket-detail.html
│   ├── admin.html
│   ├── users.html                  # 🔒 SUPERADMIN (Q-01..Q-03)
│   └── errors/                     # 401 · 403 · 404 · 500 · offline (Q-13)
├── assets/
│   ├── fonts/                      # woff2 autoalojadas (Hanken Grotesk, Inter, JetBrains Mono)
│   ├── icons/                      # SVG locales
│   └── images/                     # logo, favicon (Figma)
├── css/
│   ├── base/                       # tokens.css · fonts.css · reset.css · typography.css · utilities.css
│   ├── layout/                     # shell.css · grid.css
│   ├── components/                 # button.css · form-field.css · badge.css · table.css · ...
│   └── pages/                      # login.css · dashboard.css · tickets.css · ...
├── components/                     # Web Components (colocados por componente)
│   ├── base/                       # base-component.js · component-loader.js
│   ├── ticket-form/                # ticket-form.js · ticket-form.html · ticket-form.css
│   ├── ticket-detail/ · ticket-list/ · comment-thread/ · status-timeline/
│   ├── pagination/ · toast-container/ · confirm-dialog/ · app-shell/ · kpi-card/
├── js/
│   ├── config/                     # app.config.js · endpoints.js · environment.js
│   ├── constants/                  # ticket-status.js · ticket-priority.js · ticket-category.js
│   │                               # roles.js · http-status.js · storage-keys.js · messages.es.js
│   ├── core/
│   │   ├── container.js            # composition root
│   │   ├── http/                   # http-client.js · fetch-transport.js · mock-transport.js · api-error.js
│   │   ├── auth/                   # session-manager.js · page-guard.js
│   │   └── errors/                 # error-presenter.js · error-navigator.js
│   ├── services/                   # auth · ticket · lifecycle · assignment · comment · status-history
│   │   │                           # dashboard · technician · user  (*.service.js)
│   │   └── mappers/                # un mapper por módulo (DTO → modelo de UI)
│   ├── validators/                 # validator.js · rules.js · login.validator.js · ticket.validator.js · ...
│   ├── pages/                      # login.page.js · dashboard.page.js · tickets.page.js · ...
│   └── utils/                      # dom.js · debounce.js · date-format.js · query-string.js · memory-cache.js
├── mocks/                          # fixtures + handlers del MockTransport (solo desarrollo, C-10)
├── dev/                            # 🔶 playground de infraestructura (solo desarrollo; Q-28)
└── docs/
    ├── spec/                       # este paquete de especificaciones
    └── design/DESIGN.md            # fuente de identidad (no se edita a mano)
```

**Por qué `components/` aparte de `js/`:** cada Web Component necesita su JS, su plantilla y sus estilos juntos (alta cohesión). Mantenerlos colocados evita saltar entre tres árboles para tocar un componente.

## 3. Capas y regla de dependencia

```text
pages/*.page.js ─────► components/*  ─────► validators · utils · constants
      │                                       ▲
      └───────────► services/* ─► core/http ──┘
                                      │
                       config · constants (hojas del grafo)
```

**Reglas:** las dependencias solo apuntan hacia abajo · los **servicios nunca tocan el DOM** · los **componentes nunca llaman a `fetch`/servicios** · los **validadores son funciones puras** · sin dependencias circulares · `config` y `constants` no importan nada.

## 4. SOLID aplicado

| Principio | Decisión concreta en este proyecto | Cómo se audita (AUD-04) |
|---|---|---|
| **S** — Responsabilidad única | Un servicio por módulo backend; validación separada de su presentación (`Validator` calcula, `FormErrorPresenter` pinta); `HttpClient` solo transporta/normaliza | Cada archivo se describe en una frase sin "y" |
| **O** — Abierto/Cerrado | Reglas de validación como funciones componibles; nuevos estados/errores se registran en tablas (`ErrorPresenter`, `role-permissions`) sin editar la lógica | Añadir una regla/rol/estado no requiere modificar el motor |
| **L** — Sustitución de Liskov | `FetchTransport` y `MockTransport` cumplen el mismo contrato; todo componente respeta el contrato de `BaseComponent` | Cambiar `API_MODE` no cambia ningún servicio |
| **I** — Segregación de interfaces | Servicios pequeños y específicos; las páginas importan solo los que usan | Ninguna página importa un servicio que no invoca |
| **D** — Inversión de dependencias | Servicios dependen de `HttpClient` (abstracción) inyectado; `HttpClient` depende de un `Transport` inyectado; todo se cablea en `container.js` | Ningún servicio hace `new HttpClient()` ni usa `fetch` |

## 5. Capa HTTP

| Pieza | Responsabilidad |
|---|---|
| `endpoints.js` | **Única** fuente de rutas (función por endpoint). Cambia al llegar el Swagger |
| `HttpClient` | Añade `Authorization: Bearer …` (formato por confirmar, `Q-04`), serializa JSON, aplica timeout (`AbortController`), normaliza respuesta/errores, ejecuta el interceptor 401 |
| `Transport` (contrato) | `request({ method, url, headers, body, signal }) → { status, headers, data }` |
| `FetchTransport` | Implementación real con `fetch()` |
| `MockTransport` | Responde desde `mocks/` con latencia configurable y **escenarios de error** (ver §14) |
| `ApiError` | `{ status, code, message, fieldErrors, isNetworkError, isTimeout }` (forma real: `Q-07`) |
| `environment.js` | `API_BASE_URL` y `API_MODE` (`'mock'` \| `'real'`) — única configuración que cambia entre entornos |

Forma del registro de endpoints (rutas tomadas de `CAMPUSDESK_BACKEND.md`; **auth y users sin definir**):

```js
// Registro único de endpoints. Solo se ajusta aquí cuando llegue el Swagger.
export const ENDPOINTS = Object.freeze({
  tickets: {
    list: () => '/api/tickets',
    byId: (id) => `/api/tickets/${id}`,
    status: (id) => `/api/tickets/${id}/status`,
    assign: (id) => `/api/tickets/${id}/assign`,
    comments: (id) => `/api/tickets/${id}/comments`,
    history: (id) => `/api/tickets/${id}/history`,
  },
  reports: { summary: () => '/api/reports/summary' },
  technicians: { list: () => '/api/users/technicians' },
  // auth: PENDIENTE (Q-04) · users: PENDIENTE (Q-03)
});
```

## 6. Servicios y mappers

| Servicio | Endpoints | Métodos (nombres de diseño) |
|---|---|---|
| `AuthService` ❓ | login (Q-04) | `login(credentials)` |
| `TicketService` | `GET/POST /tickets` · `GET/PUT /tickets/{id}` | `list(filters, page)` · `getById` · `create` · `update` |
| `LifecycleService` | `PATCH …/status` | `changeStatus(id, request)` |
| `AssignmentService` | `PATCH …/assign` | `assign` · `reassign` |
| `CommentService` | `GET/POST …/comments` | `listByTicket` · `create` |
| `StatusHistoryService` | `GET …/history` | `listByTicket` |
| `DashboardService` | `GET /reports/summary` | `getSummary(filter?)` |
| `TechnicianService` | `GET /users/technicians` | `list()` |
| `UserService` 🔒 | (Q-03) | — |

Cada servicio recibe `HttpClient` por constructor, usa `ENDPOINTS` y devuelve **modelos de UI** (vía mappers), nunca el DTO crudo.

## 7. Sesión y guardas

- `SessionManager` es el **único** que lee/escribe el token y los datos de sesión (almacenamiento ❓ `Q-06`; con MPA la memoria se pierde al navegar, por lo que en la práctica es `sessionStorage` o `localStorage`).
- `pageGuard({ allowedRoles })` se ejecuta **antes de renderizar** en cada página protegida:
  1. Sin sesión → `index.html`.
  2. Token vencido (si el `exp` es legible, `Q-04`) → `index.html` con aviso de sesión expirada.
  3. Rol no permitido → página 403.
- Interceptor 401 en `HttpClient`: limpiar sesión + caché → login con aviso. **Evitar bucle** si ya se está en login.
- Logout: borrar sesión **y caché en memoria** (datos acotados por usuario no deben sobrevivir al cambio de cuenta).
- Matriz rol → enlaces/acciones en **un solo módulo** (`role-permissions`), consumida por shell, páginas y componentes.

## 8. Web Components y Shadow DOM

**Contrato de `BaseComponent`:** crea `attachShadow({ mode: 'open', delegatesFocus: true })` · adopta hojas de estilo compartidas · clona la plantilla · ciclo de vida (`connectedCallback`/`disconnectedCallback` limpian listeners) · datos de entrada por **propiedades/atributos** · salida por **`CustomEvent`** (`bubbles: true, composed: true`) · prefijo `cd-` en todos los nombres.

**Catálogo inicial 🔶**

| Componente | Responsabilidad | Entradas | Eventos | Fase |
|---|---|---|---|---|
| `cd-ticket-form` ✅ | Crear/editar tiquete con validación | `mode`, `ticket`, `disabled` | `ticket-submit`, `ticket-cancel` | F6 |
| `cd-ticket-detail` ✅ | Vista del detalle | `ticket`, `allowedActions` | `action-request` | F6 |
| `cd-ticket-list` | Tabla/lista de resumen | `tickets`, `loading` | `ticket-select` | F5 |
| `cd-comment-thread` | Comentarios + formulario | `comments`, `readonly` | `comment-submit` | F7 |
| `cd-status-timeline` | Historial | `entries` | — | F7 |
| `cd-pagination` | Paginación (variantes `Q-15`) | `page`, `size`, `total` | `page-change` | F5 |
| `cd-kpi-card` | Tarjeta de indicador | `label`, `value` | `kpi-select` | F4 |
| `cd-toast-container` · `cd-confirm-dialog` | Feedback y confirmaciones | — | — | F2 |
| `cd-app-shell` | Encabezado + navegación por rol | `user` | `logout` | F4 |

**Consideraciones del Shadow DOM (riesgos conocidos y su mitigación)**

| Riesgo | Mitigación |
|---|---|
| Los estilos globales no entran | Los **tokens CSS** (custom properties) sí atraviesan el shadow → `tokens.css` es la fuente común; hojas compartidas vía `adoptedStyleSheets` |
| `aria-labelledby`/`for` no cruzan raíces | Etiqueta e input siempre dentro de la **misma** raíz |
| Los inputs del shadow no participan en un `<form>` externo | El componente es dueño de su formulario interno y emite el payload por evento |
| Estilado externo puntual | Exponer `::part()` solo donde el diseño lo requiera |
| Pruebas/depuración | `mode: 'open'` |
| Soporte de `adoptedStyleSheets` | Navegadores objetivo ❓ `Q-26` |

**Estrategia de archivos del componente (C-02) ❓ `Q-11`**

| Opción | Cómo | Pros | Contras |
|---|---|---|---|
| **A 🔶 (propuesta)** | `x.js` + `x.html` + `x.css` cargados por `ComponentLoader` (fetch + caché en memoria, hoja parseada una vez) | Cumple C-02 **literalmente** | Peticiones extra (cacheables), inicialización asíncrona |
| B | `x.js` + `x.template.js` (HTML como string) + `x.css` | Sin fetch de HTML | HTML dentro de JS |
| C | Todo en JS | Simple | **Viola C-02** → descartada |

## 9. Arquitectura de validación

- `Validator.validate(values, schema)` → `{ isValid, errors: { campo: mensaje } }`.
- Reglas = funciones puras componibles (`required`, `email`, `minLength(n)`, `maxLength(n)`, `oneOf(enum)`, `notBlank`) en `rules.js`; cada formulario declara su **schema** (`login.validator.js`, `ticket.validator.js`, …).
- Límites en `validation.config.js` (única fuente; valores ❓ `Q-16`, `Q-17`, `Q-22`).
- Mensajes en `messages.es.js` (`Q-30`).
- `FormErrorPresenter` (DOM) separado del validador: pinta mensajes por campo, resumen, `aria-*` y foco.

## 10. Estrategia de consumo de API (C-14)

| Técnica | Dónde | Razón | Valor |
|---|---|---|---|
| Filtros y paginación en servidor | Listados | Nunca traer todo y filtrar en cliente | — |
| `AbortController` | Filtros/paginación | Descarta respuestas obsoletas y evita condiciones de carrera | — |
| *Debounce* | Entradas de texto (si las hay) | Menos peticiones mientras se escribe; los `select` disparan de inmediato | 🔶 300 ms |
| Deduplicación de GET en vuelo | `HttpClient` | Dos componentes pidiendo lo mismo = 1 petición | — |
| Caché en memoria con TTL | Técnicos, resumen del dashboard | Datos de cambio lento | 🔶 60 s / 30 s |
| Invalidación por mutación | Servicios | Asignar → invalidar técnicos, listado y dashboard; cambiar estado → detalle, listado, dashboard | — |
| Carga en paralelo | Detalle | `Promise.allSettled` de detalle + comentarios + historial (fallo parcial tolerado) | — |
| Anti doble envío | Formularios y acciones | Botón deshabilitado + guard en el controlador | — |
| Timeout + reintento acotado | `HttpClient` | Solo GET y solo errores de red/502/503/504, 1 reintento | 🔶 15 s |
| Sin *polling* | Todo | Se refresca por acción del usuario | — |
| Limpiar caché al cerrar sesión | `SessionManager` | Privacidad entre cuentas | — |

Los valores 🔶 viven en `app.config.js` (ajustables sin tocar lógica).

## 11. Arquitectura de manejo de errores

`HttpClient` lanza `ApiError` → la página/servicio lo entrega a `ErrorPresenter`, que consulta una **tabla** `status → estrategia` (`inline` · `toast` · `page` · `redirect-login`) definida en un solo módulo (spec §7). `ErrorNavigator` realiza la navegación a las páginas de error con `returnUrl` y protección contra bucles. Cambiar el comportamiento de un código = editar una fila, no la lógica (OCP).

## 12. Integración de diseño (resumen)

Detalle completo en **`06-design-system.md`**. Jerarquía (C-15): `DESIGN.md` → Figma → preguntar.

**Insumos que Sebbs debe entregar de Figma (`Q-09`)** — sin esto la fase de UI correspondiente no arranca:

| Insumo | Para |
|---|---|
| Frames por pantalla (S-01…S-09) en desktop/tablet/móvil | Estructura y maquetación |
| Estados de componentes: hover, focus, disabled, loading, error, vacío | Estilos completos |
| Logo (SVG), favicon, iconografía (SVG) | `assets/` |
| Contenedor de crear/editar tiquete (modal/drawer/página) | `Q-29` |
| Textos de la interfaz (copy), o confirmación de que se proponen y Sebbs los aprueba | `messages.es.js` (`Q-30`) |

## 13. Convenciones 🔶 (`Q-31`)

| Elemento | Convención |
|---|---|
| Archivos y carpetas | `kebab-case` (`ticket-form.js`); sufijos `.page.js`, `.service.js`, `.validator.js` |
| Clases JS / componentes | `PascalCase` (`TicketService`) · etiquetas `cd-kebab-case` |
| Funciones y variables | `camelCase`, verbos para funciones |
| Constantes | `UPPER_SNAKE_CASE` |
| CSS | Prefijo `cd-`, metodología BEM-like, tokens como `--cd-*`; sin `!important` |
| Comentarios | En español; explican el **porqué**, no el qué; JSDoc en funciones públicas |
| HTML | Semántico; sin estilos ni eventos inline; textos en español |

## 14. Estrategia de mocks (placeholders) 🔶 `Q-10`

- `API_MODE = 'mock'` en desarrollo; los servicios no saben qué transporte usan.
- `mocks/` contiene **fixtures por módulo** y *handlers* por endpoint con los DTO **conocidos** del contrato; lo desconocido se marca `// PENDIENTE Qxx`, no se inventa.
- **Escenarios de prueba** conmutables (p. ej. por query string de desarrollo): éxito · latencia alta · 400 con errores de campo · 401 · 403 · 404 · 409 · 500 · red caída. Sirven para auditar páginas de error y estados de la UI **sin depender del backend**.
- Credenciales de mock ficticias, solo en `mocks/`.
- **Plan de retiro:** al llegar el Swagger → actualizar `endpoints.js` + mappers → probar módulo a módulo contra el backend real → F9: `API_MODE='real'` por defecto y `mocks/` fuera de la entrega (C-10).

## 15. Entorno y compatibilidad

- Los módulos ES **no cargan desde `file://`** → se requiere servir por HTTP. El servidor y su origen deben coincidir con el CORS del backend (CP-15) ❓ `Q-08`.
- Funciones usadas: módulos ES, Custom Elements v1, Shadow DOM v1, `adoptedStyleSheets`, `AbortController`, `Intl`. Navegadores objetivo ❓ `Q-26`.
- Fuentes: `@font-face` con `font-display: swap` y `preload` de las críticas (`06` §6).
- Endurecimiento opcional en F9: CSP por `<meta>` (viable porque C-02 prohíbe inline) — se evalúa contra el costo de repetirla en cada HTML.

---

## Historial de cambios

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1.0 | 2026-10-08 | Versión inicial |
