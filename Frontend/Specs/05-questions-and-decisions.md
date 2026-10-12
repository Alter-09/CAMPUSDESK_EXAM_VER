# 05 · Preguntas Abiertas y Registro de Decisiones

| Campo | Valor |
|---|---|
| Versión | 0.1.0 |
| Fecha | 2026-10-08 |
| Uso | **Consultar antes de empezar cualquier tarea** (Protocolo Cero Suposiciones, `00` §3) |
| Prioridad | 🔴 bloquea una fase temprana · 🟡 importante · 🟢 menor |
| Estado | ⬜ Abierta · ✅ Respondida (se anota la respuesta y se propaga a los documentos) |

> Las **"Propuestas"** son solo ayuda para decidir: **no están asumidas** y nada se implementa sobre ellas hasta que pasen a ✅.

---

## 1. Decisiones confirmadas (D-xx)

| ID | Decisión | Fuente |
|---|---|---|
| D-01 | Solo FrontEnd; stack único: HTML + CSS + JavaScript vanilla; sin frameworks, librerías ni nada externo de FrontEnd | Sebbs |
| D-02 | `index.html` es **solo login**, sin registro; el superadmin crea usuarios y asigna permisos desde la interfaz | Sebbs |
| D-03 | Estructura modular: HTML, CSS y JS en archivos separados; SOLID y DRY | Sebbs |
| D-04 | Todo el código en inglés; comentarios en español; interfaz en español | Sebbs |
| D-05 | JavaScript con `async/await` y *webhooks* de prueba/placeholders hasta el Swagger final (interpretación: `Q-10`) | Sebbs |
| D-06 | Crear, modificar y visualizar tiquetes con Web Components + Shadow DOM | Sebbs |
| D-07 | Paginación variada y páginas específicas para errores HTTP personalizados | Sebbs |
| D-08 | Validación en JS antes de la API en login y creación de tiquetes, con indicación explícita de lo que falta/está mal | Sebbs |
| D-09 | Enfoque UX + UI y optimización del consumo de API | Sebbs |
| D-10 | Cero suposiciones: ante cualquier duda, preguntar antes de escribir código | Sebbs |
| D-11 | Desarrollo por fases con auditoría de calidad al cierre de cada una | Sebbs |
| D-12 | Specs en Markdown, modificables fase a fase | Sebbs |
| D-13 | `DESIGN.md` "Titan Meridian" es la identidad raíz (diseño y tipografía); Figma aporta pantallas, estructura y assets | Sebbs |
| D-14 | Breakpoints y grilla: ≥1280 (12 col) · 768–1279 (8 col) · <768 (4 col) | `DESIGN.md` |
| D-15 | Tipografías: Hanken Grotesk, Inter, JetBrains Mono (autoalojadas) | `DESIGN.md` + C-01 |
| D-16 | El backend es la autoridad de reglas y permisos; la UI solo guía | Enunciado RF-04 / backend |
| D-17 | Sin datos simulados en la entrega final | Enunciado, restricción 11 |

---

## 2. Preguntas abiertas (Q-xx)

### 🔴 Roles, usuarios y autenticación

**Q-01 · ¿"Superadmin" es un rol nuevo o es el ADMIN?** — *Responde: Sebbs + Backend · Bloquea: F3 (redirección por rol), F8*
Contexto: el backend solo define `ADMIN`, `TECHNICIAN`, `USER`. · Opciones: A) rol nuevo `SUPERADMIN`; B) el ADMIN actual cumple ese papel. · Propuesta: ninguna hasta hablar con backend.
Estado: ⬜ · Respuesta: —

**Q-02 · "Asignar permisos": ¿es elegir entre los 3 roles o permisos granulares?** — *Sebbs + Backend · F8*
Contexto: RF-01 y la propuesta backend solo manejan roles. · Opciones: A) selector de rol; B) permisos finos (requiere modelo nuevo en backend). Estado: ⬜ · Respuesta: —

**Q-03 · ¿Qué endpoints existirán para gestionar usuarios?** — *Backend · F8*
Necesarios: crear, listar (RF-01: ADMIN consulta usuarios), editar rol, activar/desactivar, restablecer contraseña. **No aparecen en la propuesta.** Estado: ⬜ · Respuesta: —

**Q-04 · Contrato de autenticación** — *Backend · F3 (y mocks de F1)*
Faltan: ruta de login · cuerpo (¿`email`+`password`?) · respuesta (campo del token, expiración, datos del usuario/rol) · cómo conocer el rol (¿viene en la respuesta o se decodifica el JWT?) · formato de la cabecera · ¿existe `/me`? · ¿refresh token? · ¿logout en servidor?
Estado: ⬜ · Respuesta: —

**Q-05 · Registro público vs. enunciado** — *Sebbs (+ trainer) · F0*
Contexto: el enunciado exige "Formulario de registro" (Pantalla 1) y casos CP-01/CP-02 de registro; Sebbs decidió que **no** habrá registro (software privado). · Riesgo: evaluación. · Opciones: A) mantener solo login y cubrir CP-01/02 con el formulario de creación de usuario del superadmin; B) pedir al trainer aprobar la excepción por escrito. · Propuesta: A + B. Estado: ⬜ · Respuesta: —

**Q-06 · ¿Dónde se guarda el token en el navegador?** — *Sebbs · F1/F3*
Contexto: con MPA la memoria se pierde en cada navegación. · Opciones: A) `sessionStorage` (por pestaña; se pierde al cerrar); B) `localStorage` (persiste y se comparte entre pestañas; mayor exposición ante XSS); C) cookie `HttpOnly` (requiere cambios en backend/CORS). · Propuesta: A. Estado: ⬜ · Respuesta: —

**Q-07 · Forma del JSON de error del backend (`ApiExceptionHandler`)** — *Backend · F1/F2*
Faltan: nombres de campos (mensaje, código, ruta, marca de tiempo) y **cómo vienen los errores por campo** en los 400 (para mapearlos a inputs). Estado: ⬜ · Respuesta: —

### 🔴 Entorno, diseño y arquitectura

**Q-08 · Servidor estático y origen CORS** — *Sebbs + Backend · F0*
Contexto: los módulos ES no funcionan desde `file://`; el CORS del backend debe permitir el origen exacto (CP-15). · Pregunta: ¿qué servidor usarás (extensión Live Server, `python -m http.server`, otro) y en qué puerto? ¿cuál será la URL base de la API? ¿cómo responde el servidor a rutas inexistentes (afecta la página 404)? Estado: ⬜ · Respuesta: —

**Q-09 · Entregables de Figma** — *Sebbs · F0/F1*
Lista de `02` §12: frames por pantalla (desktop/tablet/móvil), estados de componentes, logo, favicon, iconos, contenedor de crear/editar, textos. ¿Qué nombre de marca va en pantalla: CampusDesk, TechNova o ambos? Estado: ⬜ · Respuesta: —

**Q-10 · ¿Qué significan "webhooks de prueba/placeholders"?** — *Sebbs · F1*
Contexto: un *webhook* técnicamente es una llamada del servidor al cliente; aquí parece referirse a **endpoints de prueba** que `fetch` consume mientras no exista el backend. · Opciones: A) solo URLs/constantes placeholder (las llamadas fallan hasta que el backend responda); B) **capa mock local** con respuestas simuladas (`MockTransport`) y escenarios de error; C) servicio externo de mocks. · Propuesta: B (permite auditar estados y páginas de error sin backend; se retira en F9 por D-17). Estado: ⬜ · Respuesta: —

**Q-11 · Estrategia de archivos de los Web Components** — *Sebbs · F1*
Opciones A/B/C de `02` §8. · Propuesta: A (cumple literalmente C-02). Estado: ⬜ · Respuesta: —

**Q-12 · MPA vs. SPA, páginas y destino post-login** — *Sebbs · F0*
Propuesta: MPA con módulos ES (`02` §1 A-1). Preguntas: ¿se confirma el mapa de `01` §4? ¿adónde aterriza cada rol tras el login? ¿se acepta usar un Web Component también para el *shell* (`cd-app-shell`) y los componentes de apoyo (toasts, diálogo, paginación), más allá de los de tiquetes? Estado: ⬜ · Respuesta: —

**Q-13 · Páginas de error HTTP** — *Sebbs · F2*
Entendido: "errores HTTPS" = **códigos de estado HTTP** (confirmar). · Propuesta: página para 401, 403, 404, 500 y sin conexión/timeout; 400 y 409 inline. ¿401 es página o redirección al login con aviso? ¿Algún otro código? Estado: ⬜ · Respuesta: —

### 🔴 Listados y paginación

**Q-14 · Contrato de paginación** — *Backend · F5*
Faltan: parámetros (`page`, `size`, `sort`…), base 0 o 1, forma de la respuesta (¿`Page` de Spring con `content/totalElements/totalPages/number/size` u otra?), campos ordenables. Estado: ⬜ · Respuesta: —

**Q-15 · "Manejo variado de paginación": ¿qué variantes?** — *Sebbs · F5*
Opciones (combinables por vista): numerada con anterior/siguiente · selector de tamaño de página · "cargar más" · scroll infinito. ¿Qué vista usa cuál (tiquetes, comentarios, historial, técnicos, usuarios)? Estado: ⬜ · Respuesta: —

### 🟡 Datos y reglas de negocio

**Q-16 · Campos y restricciones de `TicketCreateRequest` / `TicketUpdateRequest`** — *Backend · F6*
Faltan: lista de campos, longitudes mín/máx de título y descripción, campos editables, quién fija la prioridad. Estado: ⬜ · Respuesta: —

**Q-17 · Política de contraseñas** — *Backend · F8* · RT-04 exige "requisitos mínimos" sin detallarlos. Estado: ⬜ · Respuesta: —

**Q-18 · Campos de `TicketSummaryResponse` / `TicketDetailResponse` y formato de fechas** — *Backend (+ Sebbs para locale) · F5/F6*
Faltan: nombres de campos (¿nombre del solicitante/técnico o solo ids?), formato de fecha (¿ISO-8601? ¿UTC?), y qué *locale*/zona horaria muestra la UI. Estado: ⬜ · Respuesta: —

**Q-19 · Acciones permitidas: ¿las calcula el backend o el front?** — *Backend · F7*
Opciones: A) el detalle trae `allowedActions` (el front no duplica reglas de negocio, mejor DRY); B) el front deriva por rol+estado (matriz de `01` §3, duplica reglas). Además: ¿el ADMIN puede reasignar en EN_PROCESO? (RF-04 solo menciona ASIGNADA). Propuesta: A. Estado: ⬜ · Respuesta: —

**Q-20 · Asignación vs. reasignación en `PATCH …/assign`** — *Backend · F7* · Un endpoint, dos DTOs: ¿cómo se distinguen, cómo se llama el campo de justificación y es obligatorio? Estado: ⬜ · Respuesta: —

**Q-21 · Cambio de estado: cuerpo de `PATCH …/status`** — *Backend · F7* · ¿Se envía `targetStatus` o `TransitionAction`? ¿Nombre del campo de comentario? ¿Es obligatorio? Estado: ⬜ · Respuesta: —

**Q-22 · Comentarios: visibilidad, límites y paginación** — *Backend · F7* · `CommentVisibility` (PUBLIC/INTERNAL): ¿quién ve INTERNAL y la UI debe elegirla? ¿Longitud máxima? ¿Se pagina el listado? Estado: ⬜ · Respuesta: —

**Q-23 · Dashboard: ASIGNADA, periodo y parámetros** — *Backend · F4* · El resumen no trae ASIGNADA (el total ≠ suma). ¿Se cuenta dentro de "abiertos"? ¿Cómo se envía `MetricPeriod` y el filtro por departamento? Estado: ⬜ · Respuesta: —

**Q-24 · Técnicos: disponibilidad y carga** — *Backend · F7/F8* · ¿Hay endpoint propio para `TechnicianAvailabilityResponse`? ¿El selector debe mostrar carga (`activeTicketsAssigned`)? Estado: ⬜ · Respuesta: —

**Q-25 · ¿Quién puede crear tiquetes?** — *Backend + Sebbs · F5/F6* · RF-01 lo asigna a USER; ¿ADMIN/TECHNICIAN también o no ven el botón? Estado: ⬜ · Respuesta: —

### 🟡 Calidad y herramientas

**Q-26 · Navegadores objetivo** — *Sebbs · F0* · (Los breakpoints ya están en D-14.) `adoptedStyleSheets` y Shadow DOM exigen navegadores modernos: ¿versiones mínimas a soportar? Estado: ⬜ · Respuesta: —

**Q-27 · Nivel de accesibilidad objetivo** — *Sebbs · F1* · `DESIGN.md` promete AAA para métricas críticas; ¿el objetivo global es WCAG 2.1 AA, con AAA solo en métricas? (ver `06` §8). Estado: ⬜ · Respuesta: —

**Q-28 · Herramientas de desarrollo permitidas** — *Sebbs (+ trainer) · F0* · "Nada externo" ¿incluye herramientas que no se entregan (ESLint, Prettier, Lighthouse, Live Server, Playwright)? Propuesta: permitidas solo en desarrollo y fuera del repo entregado. Estado: ⬜ · Respuesta: —

### 🟢 Detalles

**Q-29 · Contenedor de crear/editar tiquete** — *Sebbs (Figma) · F6* · ¿Modal, drawer o página aparte? Estado: ⬜ · Respuesta: —
**Q-30 · Textos de la interfaz (copy)** — *Sebbs · F1* · ¿Los define Figma/Sebbs o se proponen para aprobación? Estado: ⬜ · Respuesta: —
**Q-31 · Convenciones** — *Sebbs · F0* · Prefijo `cd-`, BEM-like, idioma de los commits y estrategia de ramas (`02` §13). Estado: ⬜ · Respuesta: —
**Q-32 · Sesión: inactividad y varias pestañas** — *Sebbs · F3* · ¿cierre por inactividad? ¿sincronizar logout entre pestañas? Estado: ⬜ · Respuesta: —

### 🔴/🟡 Diseño (a raíz de `DESIGN.md`) — ver `06` §8

**Q-33 · Jerarquía de tokens y valores sin token** — *Sebbs · F0/F1* 🔴
Contexto: `DESIGN.md` trae dos conjuntos de color que se contradicen (roles tipo Material 3 vs. tokens de marca) y ~14 valores en la prosa sin token (`06` §2.1). · Preguntas: ¿los tokens de **marca** son la fuente de verdad y el conjunto B se ignora? ¿se acepta tokenizar los valores sin token con los nombres propuestos? ¿qué color usan los **errores de formulario**? Estado: ⬜ · Respuesta: —

**Q-34 · Decisiones sobre hallazgos de contraste (H-01…H-04)** — *Sebbs · F0* 🔴
Medido: texto blanco sobre botón coral **3.42:1** (hover **2.75:1**); `slate-titanium-500` como texto **2.61–4.05:1**; coral como texto en tarjeta **4.34:1**; borde de input **1.41:1**. · Opciones por hallazgo: A) ajustar el valor en Figma/`DESIGN.md` (p. ej. texto oscuro sobre coral = 5.63:1); B) mantener y registrar desviación aprobada (deuda S3 con dueño); C) corregir solo el tamaño/peso para que cuente como "texto grande". Estado: ⬜ · Respuesta: —

**Q-35 · ¿Tema único oscuro?** — *Sebbs · F0* 🟡 · `DESIGN.md` define solo tema oscuro. ¿No habrá tema claro ni `prefers-color-scheme`? Estado: ⬜ · Respuesta: —

**Q-36 · Componentes no definidos en `DESIGN.md`** — *Sebbs (Figma) · F2–F7* 🟡
Modal/diálogo, toast, paginación, timeline, hilo de comentarios, skeleton, estados vacíos, páginas de error, encabezado/menú de usuario, riel de navegación, menú móvil, estados `error`/`disabled`/`loading` de inputs y botones, `select`, `textarea`, checkbox/radio, **acciones de fila sin hover** (H-08). ¿Vienen en Figma o se derivan (y quién aprueba)? Estado: ⬜ · Respuesta: —

**Q-37 · Mapeo estado / prioridad / categoría → chips** — *Sebbs · F6* 🟡 · Variantes definidas: neutro, crítico, nominal; el ámbar (`data-metric-amber`) no tiene uso. Completar la tabla de `06` §9. Estado: ⬜ · Respuesta: —

**Q-38 · Tendencias, sparklines y gráficas del KPI** — *Sebbs + Backend · F4* 🟡 · El diseño las contempla; la API solo entrega conteos. Opciones: A) omitirlas en la UI; B) pedir al backend series/comparativos (fuera de RF-07); C) calcular un comparativo en cliente (no recomendable: requeriría datos que no llegan). Estado: ⬜ · Respuesta: —

**Q-39 · Archivos de fuentes** — *Sebbs · F0/F1* 🟡 · ¿Quién descarga las fuentes? ¿Variables o estáticas (pesos de `06` §6)? ¿Subconjunto latin + latin-ext? ¿Se confirma la licencia y la compatibilidad de `tnum` en Hanken Grotesk (`H-12`)? Estado: ⬜ · Respuesta: —

---

## 3. Cómo registrar una respuesta

1. Cambiar el estado a ✅ y escribir la **respuesta literal** + fecha + quién respondió.
2. Si genera una decisión estable, añadir una fila `D-xx` en §1.
3. Propagar a `01` / `02` / `03` / `06` (cambiar 🔶/❓ por ✅) y anotar el cambio en cada historial.
4. Desbloquear las tareas afectadas en `04`.

---

## Historial de cambios

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1.0 | 2026-10-08 | Versión inicial: 17 decisiones y 39 preguntas |
