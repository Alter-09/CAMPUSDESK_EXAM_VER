# 04 · Fases, Puertas de Calidad y Auditorías

| Campo | Valor |
|---|---|
| Versión | 0.1.0 |
| Fecha | 2026-10-08 |
| Depende de | `00` · `01` · `02` · `03` · `05` · `06` |
| Principio | **Ninguna fase se cierra con hallazgos S1/S2 abiertos.** Cerrar bien una fase cuesta menos que arrastrar deuda a la siguiente |

---

## 1. Tracker de fases

| Fase | Nombre | Pantallas | Estado | Preguntas que la bloquean | Cierre (fecha) |
|---|---|---|---|---|---|
| **F0** | Descubrimiento y cierre de decisiones | — | ⬜ | Q-05 · Q-08 · Q-09 · Q-10 · Q-11 · Q-12 · Q-26 · Q-28 · Q-31 · Q-33 · Q-34 · Q-35 · Q-39 | — |
| **F1** | Fundaciones (infraestructura + sistema de diseño) | — | ⬜ | Q-06 · Q-07 · Q-27 · Q-30 | — |
| **F2** | Errores y feedback | S-08 | ⬜ | Q-13 · Q-36 | — |
| **F3** | Autenticación | S-01 | ⬜ | Q-04 · Q-06 · Q-32 | — |
| **F4** | Shell y Dashboard | S-09 · S-02 | ⬜ | Q-23 · Q-38 | — |
| **F5** | Listado de tiquetes | S-03 | ⬜ | Q-14 · Q-15 · Q-18 · Q-25 | — |
| **F6** | Web Components de tiquetes (crear/editar/ver) | S-04 · S-05 (parcial) | ⬜ | Q-16 · Q-29 · Q-37 | — |
| **F7** | Ciclo de vida, comentarios, historial y asignación | S-05 · S-06 (parcial) | ⬜ | Q-19 · Q-20 · Q-21 · Q-22 · Q-24 | — |
| **F8** | Administración y gestión de usuarios | S-06 · S-07 | 🔒 | Q-01 · Q-02 · Q-03 · Q-17 | — |
| **F9** | Integración real, endurecimiento y entrega | Todas | ⬜ | Swagger final | — |

## 2. Reglas de puerta (gate)

**Severidades**

| Nivel | Definición | Ejemplos |
|---|---|---|
| **S1 — Bloqueante** | Rompe un flujo, la seguridad o una regla de la Constitución (C-01, C-02, C-03, C-08, C-10) | Librería externa, CSS inline, `innerHTML` con datos de usuario, mock en entrega |
| **S2 — Mayor** | Bug funcional, violación de DRY/SOLID, fallo de accesibilidad en un flujo crítico, criterio de aceptación incumplido | Regla de validación duplicada, formulario sin mensajes por campo |
| **S3 — Menor** | Cosmético o mejora sin impacto funcional | Espaciado fuera de Figma por pocos px |

**Umbral para cerrar** 🔶 (ajustable): S1 = 0 · S2 = 0 · S3 ≤ 3, todos registrados en la bitácora de deuda con dueño y fase destino, y **ninguno arrastrado más de una fase**.

**Procedimiento de cierre**
1. Congelar alcance de la fase (no entran tareas nuevas).
2. Auto-chequeo con la **DoD** (`00` §4).
3. Ejecutar la **auditoría común** (§3) + la **específica** de la fase.
4. Registrar hallazgos (§5); corregir S1/S2.
5. Re-auditar lo corregido + **regresión rápida** de fases anteriores.
6. Si el Swagger ya cubre los endpoints de la fase → **punto de integración**: probarlos en `API_MODE='real'` antes de cerrar.
7. Actualizar documentos (spec, contrato, preguntas) + historiales.
8. **Aprobación de Sebbs** → cambiar estado a ✔️ y completar el tracker.

## 3. Auditoría común (se ejecuta en TODAS las fases)

| ID | Control | Cómo se verifica | Regla |
|---|---|---|---|
| AUD-01 | Sin recursos externos | `grep -rEn "https?://" --include=*.html --include=*.css --include=*.js --exclude-dir=docs .` (solo se permite la URL de la API en configuración); sin `<script src="http`, sin `fonts.googleapis`; Network sin dominios ajenos | C-01 |
| AUD-02 | Separación de archivos | `grep -rEn "<style\|style=\"\|\bon[a-z]+=\"" --include=*.html .` vacío; sin `<script>` con código en HTML; plantillas/estilos de componentes en sus archivos | C-02 |
| AUD-03 | Idioma | `grep -rEn "[áéíóúñ¿¡]" js components` solo en comentarios y `messages.es.js`; revisión de muestra de nombres | C-03 |
| AUD-04 | SOLID / DRY | Revisión de responsabilidades por archivo; búsqueda de duplicados (endpoints, enums, reglas, errores, matriz de roles); dependencias en la dirección correcta (`02` §3) | C-04–C-06 |
| AUD-05 | Calidad de código | `grep -rEn "console\.\|\bvar\b\|\.then\(\|[^=!]==[^=]" js components` vacío; `fetch(` solo en `fetch-transport.js`; sin `TODO` huérfanos; consola del navegador **sin errores ni advertencias** | C-06, C-07 |
| AUD-06 | Estados de UI | Cada vista con datos remotos probada en carga · vacío · error · éxito (usar escenarios del mock) | C-12 |
| AUD-07 | Formularios | Matriz de datos inválidos por campo; **0 peticiones** con datos inválidos (Network); mensajes por campo + resumen + foco | C-11 |
| AUD-08 | Errores y autoridad del backend | Forzar 400/401/403/404/409/500/red/timeout (mock y, si hay, real); la UI se comporta según `01` §7 sin filtrar datos internos | C-09 |
| AUD-09 | Accesibilidad | Recorrido **solo con teclado**; foco visible y con orden lógico; labels; errores anunciados; `prefers-reduced-motion`; verificación de contraste de lo nuevo contra `06` §8 | C-13 |
| AUD-10 | Consumo de API | Network: sin peticiones duplicadas ni innecesarias; las obsoletas se cancelan; la caché respeta TTL e invalidación | C-14 |
| AUD-11 | Responsive y roles | Revisar en **<768 · 768–1279 · ≥1280**; probar con **cada rol** aplicable (USER, TECHNICIAN, ADMIN, SUPERADMIN ❓) | `06` §4 |
| AUD-12 | Seguridad cliente | Pegar `<img src=x onerror=alert(1)>` y `<script>` en todos los campos de texto → se muestran como texto; `grep -rn "innerHTML\|outerHTML\|insertAdjacentHTML"` con cada hallazgo justificado; token ausente de URL y logs | C-08 |
| AUD-13 | Mocks | Antes de F9: confirmar que están aislados en `mocks/` y tras `API_MODE`. En F9: `API_MODE='real'` por defecto y **cero** referencias a fixtures | C-10 |
| AUD-14 | Fidelidad de diseño | Hash de `DESIGN.md`; ningún valor de color fuera de `tokens.css` (`06` §10); revisión visual contra Figma | C-15 |
| AUD-15 | Regresión y documentación | Humo rápido de las fases ya cerradas; docs actualizados con historial | C-16 |

> En los comandos, `\|` es solo el escape de Markdown dentro de tablas: copiando desde la vista renderizada se obtiene `|`; si copias del texto crudo, quita la `\`.

---

## 4. Fases

> Cada tarea tiene ID `Fx-Txx`. Las tareas marcadas con ❓ **no se empiezan** hasta cerrar su pregunta.

### F0 — Descubrimiento y cierre de decisiones
**Objetivo:** que nadie escriba código sobre supuestos.
**Tareas**
- F0-T01 Responder las preguntas 🔴 listadas en el tracker (registro en `05`).
- F0-T02 Recibir insumos de Figma y confirmar la jerarquía de tokens (`02` §12, `06` §9).
- F0-T03 Crear repositorio/rama y estructura de carpetas vacía (`02` §2); copiar `DESIGN.md` a `docs/design/`.
- F0-T04 Elegir y verificar el servidor estático HTTP (módulos ES funcionando) y confirmar con backend el origen CORS.
- F0-T05 Congelar el paquete de specs en **v1.0** (sin 🔶 en lo necesario para F1–F3).

**Gate F0:** [ ] preguntas del tracker respondidas · [ ] Figma entregado y revisado · [ ] servidor sirve módulos ES · [ ] origen CORS acordado · [ ] specs v1.0 · [ ] decisión registrada sobre desviaciones de contraste (`Q-34`).

### F1 — Fundaciones
**Objetivo:** la base técnica y visual sobre la que se apoya todo; **sin pantallas aún**.
**Tareas**
- F1-T01 `config/` (`environment`, `app.config`, `endpoints`) y `constants/` (enums, roles, http-status, storage-keys, `messages.es.js` mínimo).
- F1-T02 `utils/` (dom, debounce, query-string, date-format con `Intl`, memory-cache).
- F1-T03 `core/http`: `HttpClient`, `FetchTransport`, `MockTransport`, `ApiError` (timeout, abort, dedupe, normalización).
- F1-T04 `mocks/` base con **escenarios de error** conmutables (`02` §14).
- F1-T05 `container.js` (composition root).
- F1-T06 `tokens.css`, `reset.css`, `typography.css` desde `DESIGN.md` (`06` §10).
- F1-T07 Fuentes autoalojadas + `fonts.css` + `preload` (`06` §6).
- F1-T08 `BaseComponent` + `ComponentLoader` (según `Q-11`).
- F1-T09 `Validator` + `rules.js` + `FormErrorPresenter`.
- F1-T10 🔶 `dev/playground.html` para probar infraestructura (según `Q-28`).

**Aceptación:** HttpClient probado con éxito, 400/401/403/404/409/500, timeout, offline, abort y dedupe · tipografías cargan **sin red** · cero valores de color fuera de `tokens.css` · `fetch(` solo en `fetch-transport.js`.
**Auditoría específica:** AUD-01–05, 14 con rigor máximo (aquí se fija la calidad de todo lo demás).
**Riesgos:** diseñar sin saber la forma del error del backend (`Q-07`) → se encapsula en `ApiError` y se ajusta en un solo punto.

### F2 — Errores y feedback (S-08)
**Tareas:** F2-T01 `ErrorPresenter` + tabla `status → estrategia` · F2-T02 `ErrorNavigator` (`returnUrl`, anti-bucle) · F2-T03 páginas de error según `Q-13` y Figma ❓ · F2-T04 `cd-toast-container` · F2-T05 `cd-confirm-dialog` · F2-T06 estado "sin conexión" · F2-T07 pruebas con cada escenario del mock.
**Aceptación:** S08-AC1 · cada escenario del mock produce el comportamiento de `01` §7 · ninguna pantalla expone trazas ni mensajes técnicos.
**Auditoría específica:** AUD-08 y AUD-09 (diálogos y toasts accesibles, foco atrapado/restaurado).

### F3 — Autenticación (S-01)
**Tareas:** F3-T01 `login.validator` · F3-T02 `index.html` + CSS según Figma ❓ · F3-T03 `AuthService` + mapper de sesión ❓ `Q-04` · F3-T04 `SessionManager` ❓ `Q-06` (limpia caché al salir) · F3-T05 `pageGuard` + interceptor 401 · F3-T06 `login.page.js` (estados, redirección por rol) · F3-T07 logout y aviso de sesión expirada · F3-T08 módulo `role-permissions`.
**Aceptación:** S01-AC1…AC8 · CP-03, CP-04, CP-16.
**Punto de integración:** si el login ya está en el Swagger, probarlo real.
**Auditoría específica:** AUD-07 (matriz de inválidos), AUD-12 (el token no queda en URL ni consola).

### F4 — Shell y Dashboard (S-09, S-02)
**Tareas:** F4-T01 `cd-app-shell` (riel 64/260, drawer, móvil) · F4-T02 navegación por rol · F4-T03 `cd-kpi-card` · F4-T04 `DashboardService` + mapper · F4-T05 `dashboard.page.js` + caché TTL · F4-T06 skeleton/error/vacío · F4-T07 🔶 clic en KPI → listado filtrado.
**Aceptación:** S02-AC1…AC4 · S09-AC1 · CP-14.
**Auditoría específica:** AUD-10 (volver a la página no repite petición dentro del TTL), AUD-11 (3 breakpoints).

### F5 — Listado de tiquetes (S-03)
**Tareas:** F5-T01 `TicketService.list` + mapper ❓ `Q-14/18` · F5-T02 `cd-ticket-list` · F5-T03 filtros + estado en URL · F5-T04 `cd-pagination` con variantes ❓ `Q-15` · F5-T05 cancelación/dedupe · F5-T06 estados (vacío diferenciado) · F5-T07 botón "Nuevo" según rol ❓ `Q-25`.
**Aceptación:** S03-AC1…AC4 · CP-06 (visibilidad).
**Auditoría específica:** AUD-10 (una sola petición vigente por cambio de filtro).

### F6 — Web Components de tiquetes: crear, editar, ver (S-04, S-05 parcial)
**Tareas:** F6-T01 `cd-ticket-form` (modo create) · F6-T02 modo edit con regla ABIERTA/sin asignar · F6-T03 `ticket.validator` ❓ `Q-16` · F6-T04 `TicketService.create/update/getById` · F6-T05 `cd-ticket-detail` (lectura) + badges ❓ `Q-37` · F6-T06 `ticket-detail.page.js` · F6-T07 mapeo de errores de servidor a campos · F6-T08 pruebas de Shadow DOM (aislamiento, teclado, lector).
**Aceptación:** S04-AC1…AC5 · CP-05.
**Auditoría específica:** AUD-07 y AUD-09 con foco en Shadow DOM (labels dentro de la misma raíz, `delegatesFocus`).

### F7 — Ciclo de vida, comentarios, historial y asignación (S-05, S-06 parcial)
**Tareas:** F7-T01 `LifecycleService` + acciones por rol/estado ❓ `Q-19/21` · F7-T02 diálogo de cierre · F7-T03 `CommentService` + `cd-comment-thread` ❓ `Q-22` · F7-T04 `StatusHistoryService` + `cd-status-timeline` · F7-T05 carga paralela con fallo parcial · F7-T06 `AssignmentService` + `TechnicianService` + selector ❓ `Q-20/24` · F7-T07 manejo 409/403 + recarga · F7-T08 invalidación de caché.
**Aceptación:** S05-AC1…AC6 · S06-AC2/AC3 · CP-07, CP-10, CP-11, CP-12, CP-13.
**Auditoría específica:** AUD-08 (409 por estado desactualizado con dos sesiones) y AUD-12 (XSS en comentarios).

### F8 — Administración y usuarios (S-06, S-07) 🔒
**Tareas:** F8-T01 `admin.page.js` (seguimiento, técnicos) · F8-T02 `users.page.js` listar/crear/editar ❓ `Q-01–03` · F8-T03 `user.validator` ❓ `Q-17` · F8-T04 guarda SUPERADMIN · F8-T05 duplicado de correo → error en campo (CP-02).
**Aceptación:** S06-AC1 · S07 según respuestas.
**Contingencia:** si backend no entrega endpoints de usuarios a tiempo, F8 se marca ⛔ y se registra como **deuda formal acordada con Sebbs** antes de entrar a F9 (no se improvisan endpoints).

### F9 — Integración real, endurecimiento y entrega
**Tareas:** F9-T01 diff con el Swagger (`03` §7) · F9-T02 `API_MODE='real'` por defecto · F9-T03 retirar `mocks/` y `dev/` de la entrega · F9-T04 CORS end-to-end (CP-15) · F9-T05 ejecutar **CP-01…CP-16** con evidencia · F9-T06 auditoría completa (AUD-01…15) en 3 breakpoints y navegadores de `Q-26` · F9-T07 rendimiento (Lighthouse si `Q-28` lo permite; si no, DevTools Network/Performance) · F9-T08 accesibilidad final (teclado + lector) · F9-T09 `README.md` del frontend (nombre y descripción, integrantes, tecnologías, requisitos previos, instalación, configuración por variables/entorno, ejecución, roles, enlace local a Swagger, evidencias) · F9-T10 capturas (login, gestión de tiquetes, asignación, cambios de estado, historial y comentarios, dashboard, Swagger) · F9-T11 ensayo de la demo completa (creación → cierre) con una cuenta por rol · F9-T12 congelar versión.
**Gate final:** S1 = 0, S2 = 0, **S3 = 0 sin justificación**, AUD-13 en verde, todos los CP demostrables, README y evidencias completos.

---

## 5. Bitácora de hallazgos (copiar una fila por hallazgo)

| ID | Fase | AUD | Severidad | Descripción | Estado | Responsable | Resuelto en |
|---|---|---|---|---|---|---|---|
| — | — | — | — | — | — | — | — |

## 6. Bitácora de deuda técnica (solo S3 o desviaciones acordadas)

| ID | Descripción | Origen (`H-xx`/hallazgo) | Dueño | Fase destino | Aprobado por Sebbs |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

## 7. Plantilla de informe de auditoría (copiar al cerrar cada fase)

```markdown
### Informe de auditoría — F<n> · <fecha>
- Alcance auditado: <tareas / pantallas>
- DoD (00 §4): ✅/❌ por ítem
- Auditoría común: AUD-01 ✅ … AUD-15 ✅/❌ (adjuntar evidencia de los ❌)
- Auditoría específica: <resultado>
- Punto de integración: <endpoints probados en real / no aplica>
- Hallazgos: S1: n · S2: n · S3: n (ver bitácora)
- Documentos actualizados: <archivos + versión>
- Decisión del gate: CERRADA / NO CERRADA (motivo)
- Aprobación de Sebbs: <sí/no · fecha>
```

---

## Historial de cambios

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1.0 | 2026-10-08 | Versión inicial |
