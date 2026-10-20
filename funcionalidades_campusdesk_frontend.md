# 💻 CampusDesk — Especificación Técnica y Funcionalidades del Frontend

---

## 🏛️ 1. ARQUITECTURA Y ESTADO ACTUAL DEL FRONTEND

El frontend de **CampusDesk** es una aplicación web desacoplada tipo Single/Multi-Page basada en estándares web nativos modernos:
* **Lenguaje:** Vanilla JavaScript (ES6+ Modules, sin frameworks como React, Angular o Vue).
* **Componentización:** **Web Components nativos** (`Custom Elements v1`) con encapsulación estricta mediante **Shadow DOM** (`this.attachShadow({ mode: 'open' })`).
* **Comunicación HTTP:** Cliente centralizado [`http-client.js`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/Frontend/Scripts/config/http-client.js) basado en Fetch API nativa con inyección de Bearer Token desde `sessionStorage` y tipado de errores con `ApiError`.
* **Estilos:** Vanilla CSS con variables CSS personalizadas (*Custom Properties*), diseño modular (*BEM naming*), y diseño responsive.
* **Organización de Código:** Separación en capas: `components/`, `services/`, `config/`, `constants/`, `mappers/`, `modules/`, `pages/`, `utils/`, `validators/`.

```text
Frontend/
├── Assets/                                        # Iconos SVG, logotipos e ilustraciones
│   └── Icons/ (dashboard.svg, menu.svg, user.svg, assign.svg, search.svg, etc.)
├── stylesheet/                                    # Estilos globales y tokens de diseño
│   └── main.css
├── index.html                                     # Acceso y autenticación
├── user.html                                      # Dashboard portal de solicitante
├── create-ticket.html                             # Formulario de creación de ticket
├── my-tickets.html                                # Listado y seguimiento de tickets del usuario
├── technician.html                                # Dashboard operativo del técnico
├── superadmin.html                                # Panel administrativo principal
├── assignation.html                               # Asignación manual y masiva de tickets
├── techguys.html                                  # Directorio y disponibilidad de técnicos
├── ticketlist.html                                # Monitoreo global de tickets
├── userlist.html                                  # Directorio y gestión de usuarios
├── newuser.html                                   # Alta de nuevos usuarios
├── roleselector.html                              # Asignador de roles
└── Scripts/
    ├── config/
    │   ├── api.config.js                          # URL base del backend y claves de almacenamiento
    │   └── http-client.js                         # Wrapper de fetch, headers JWT y manejo de ApiError
    ├── constants/
    │   ├── messages.es.js                         # Diccionario de mensajes y errores por código HTTP
    │   ├── messages.tickets.es.js                 # Textos de ciclo de vida de tickets
    │   ├── messages.technicians.es.js             # Textos de técnicos
    │   └── messages.users.es.js                   # Textos de usuarios y roles
    ├── services/                                  # Capa de comunicación con los endpoints REST
    │   ├── ticket.service.js
    │   ├── technician.service.js
    │   ├── user.service.js
    │   ├── role.service.js
    │   ├── permission.service.js
    │   └── access.service.js
    ├── mappers/                                   # Adaptadores entre DTOs backend y estado de vista
    │   └── user-view.mapper.js
    ├── modules/                                   # Utilidades de DOM global (ej. sidebar-toggle.js)
    ├── pages/                                     # Scripts orquestadores por página HTML
    └── components/                                # Web Components nativos con Shadow DOM
        ├── user-portal.js                         # <cd-user-portal>
        ├── assigned-tickets.js                    # <cd-assigned-tickets>
        ├── technician-dashboard.js                # <cd-technician-dashboard>
        ├── ticket-list.js                         # <cd-ticket-list>
        ├── ticket-assign.js                       # <cd-ticket-assign>
        ├── technician-directory.js                # <cd-technician-directory>
        ├── user-dashboard.js                      # <cd-user-dashboard>
        ├── user-directory.js                      # <cd-user-directory>
        ├── user-list.js                           # <cd-user-list>
        ├── user-form-modal.js                     # <cd-user-form-modal>
        ├── role-assignment.js                     # <cd-role-assignment>
        ├── change-password-modal.js               # <cd-change-password-modal>
        └── revenue-chart.js                       # <cd-revenue-chart>
```

---

## 🎯 2. MATRIZ DE INTEGRACIÓN FRONTEND PARA LAS 90 FUNCIONALIDADES

A continuación se detalla cómo se traduce cada funcionalidad en elementos tangibles del DOM, componentes y servicios:

### 2.1. Funcionalidades de Solicitante (`USER`)

| ID | Funcionalidad | Pantalla / Archivo HTML | Componente Web Component | Elementos del DOM / Interacción | Servicio Invocado |
|:---|:---|:---|:---|:---|:---|
| **U-01** | Selección guiada por catálogo de servicios | `create-ticket.html` | `<cd-user-portal>` | Cards interactivas con iconos de servicios (Hardware, Red, Correo, etc.) que pre-llenan categoría y SLA estimado. | `serviceCatalog.service.js` |
| **U-02** | Carga de adjuntos y capturas | `create-ticket.html` | `<cd-attachment-uploader>` | Zona Drag & Drop con `<input type="file" multiple>`, preview en miniatura y barra de progreso. | `attachment.service.js` |
| **U-03** | Ubicación física detallada | `create-ticket.html` | `<cd-user-portal>` | Selectores en cascada: Campus (`<select id="campus">`) → Edificio (`<select id="building">`) → Aula (`<input id="room">`). | `ticket.service.js` |
| **U-04** | Cancelar ticket en estado OPEN | `my-tickets.html` | `<cd-user-portal>` | Botón `<button class="btn btn--danger">Cancelar</button>` visible solo en tickets `OPEN`, modal de confirmación de motivo. | `ticket.service.js` (`cancelTicket`) |
| **U-05** | Reabrir ticket resuelto | `my-tickets.html` | `<cd-user-portal>` | Botón `<button class="btn btn--warning">Reabrir</button>` habilitado si `status === 'RESOLVED'` y dentro de los 7 días de gracia. | `ticket.service.js` (`reopenTicket`) |
| **U-06** | Confirmar solución y Encuesta CSAT | `my-tickets.html` | `<cd-csat-modal>` | Botón *"Aceptar Solución"*; despliega modal `<dialog>` con 5 estrellas interactivas (⭐) y textarea de retroalimentación. | `satisfaction.service.js` |
| **U-07** | Rechazar resolución técnica | `my-tickets.html` | `<cd-user-portal>` | Botón *"Rechazar Solución"*; despliega prompt de justificación obligatoria que regresa el ticket a `IN_PROGRESS`. | `ticket.service.js` (`transitionTicket`) |
| **U-08** | Sugerencia automática de artículos KB | `create-ticket.html` | `<cd-kb-suggestions>` | Al escribir en el input de título (`input` event con debounce), despliega lista de artículos que podrían resolver el problema. | `knowledgebase.service.js` |
| **U-09** | Portal de Base de Conocimiento (FAQ) | `knowledge-base.html` | `<cd-knowledge-base>` | Buscador por texto, acordiones con categorías de autoservicio y contador de votos útiles ("¿Te sirvió? Sí / No"). | `knowledgebase.service.js` |
| **U-10** | Centro de Notificaciones In-App | Header (`app-header`) | `<cd-notification-center>` | Campana con badge numérico no leídos (`#count`); dropdown con hilos de cambios de estado y comentarios. | `notification.service.js` |
| **U-11** | Preferencias de Notificación | `user-profile.html` | `<cd-notification-preferences>` | Toggles tipo switch para activar/desactivar notificaciones por canal (In-App y Email) por tipo de evento. | `user.service.js` |
| **U-12** | Barra visual de cuenta regresiva SLA | `my-tickets.html` | `<cd-sla-indicator>` | Barra de progreso con color dinámico (Verde: > 50% tiempo restante; Amarillo: < 25%; Rojo: Incumplido). | Integrado en datos de ticket |
| **U-13** | Timeline interactivo del historial | Detalle de ticket | `<cd-ticket-timeline>` | Línea de tiempo visual vertical que renderiza los eventos de `StatusHistory` con avatar del actor y fecha formateada. | `ticket.service.js` (`getTicketHistory`) |
| **U-14** | Suscripción / Observadores (Watchers) | Detalle de ticket | `<cd-ticket-watchers>` | Botón *"Seguir Incidencia"* (`★ Seguir`); lista de avatares de compañeros suscritos al ticket. | `ticket.service.js` |
| **U-15** | Edición de perfil de usuario | `user-profile.html` | `<cd-user-profile>` | Formulario con inputs para teléfono, extensión de campus, departamento y avatar. | `user.service.js` (`updateProfile`) |
| **U-16** | Banner de Avisos y Mantenimiento | Header global | `<cd-announcement-banner>` | Banner superior con alerta (`bg-amber` o `bg-red`) con mensaje de caída de red o mantenimiento de campus. | `announcement.service.js` |
| **U-17** | Filtros avanzados y ordenamiento | `my-tickets.html` | `<cd-ticket-filters>` | Barra de filtrado con selector múltiple de estados, rango de fechas (desde/hasta) y orden por prioridad/fecha. | `ticket.service.js` (`listTickets`) |
| **U-18** | Vista tipo tarjetas (Kanban / Cards) | `my-tickets.html` | `<cd-user-portal>` | Selector de vista `[ ☷ Tarjetas ]` o `[ ☰ Tabla ]` para ver los tickets según preferencia. | Estado local del componente |
| **U-19** | Descarga de comprobante en PDF | Detalle de ticket | `<cd-user-portal>` | Botón *"Descargar Comprobante"* (`icon-pdf.svg`); invoca descarga de archivo binario con el radicado del ticket. | `ticket.service.js` (`downloadPdf`) |
| **U-20** | Comentarios con adjuntos inline | Detalle de ticket | `<cd-comment-thread>` | Hilo de chat con imágenes clicables (LightBox modal para zoom) y enlaces para descargar archivos PDF/DOCX. | `ticket.service.js` / `attachment.service.js` |
| **U-21** | Feedback de utilidad en artículos KB | `knowledge-base.html` | `<cd-knowledge-base>` | Botones interactivos `[ 👍 Me sirvió ]` / `[ 👎 No me sirvió ]` con actualización instantánea de contadores. | `knowledgebase.service.js` |
| **U-22** | Visualización de etiquetas (Tags) | `my-tickets.html` | `<cd-ticket-list>` | Píldoras de colores (`badge--tag`) con las etiquetas asociadas (ej. `#laboratorio`, `#wifi`). | Renderizado en tabla |
| **U-23** | Indicador de técnico escribiendo / en línea | Detalle de ticket | `<cd-comment-thread>` | Punto verde en el avatar del técnico asignado indicando disponibilidad actual. | `technician.service.js` |
| **U-24** | Botón de escalamiento por demora | Detalle de ticket | `<cd-user-portal>` | Botón *"Solicitar Escalamiento"* habilitado cuando el ticket lleva más de 24 horas sin respuesta técnica. | `ticket.service.js` (`escalateTicket`) |
| **U-25** | Detección de tickets similares al crear | `create-ticket.html` | `<cd-duplicate-alert>` | Alerta flotante si ya existe una incidencia abierta del mismo solicitante en la misma aula/área. | `ticket.service.js` |
| **U-26** | Formulario dinámico por tipo de servicio | `create-ticket.html` | `<cd-dynamic-form>` | Renderiza campos específicos según el servicio (ej. si es "Software": Nombre del programa, versión y licencia). | `serviceCatalog.service.js` |
| **U-27** | Búsqueda global de tickets por texto | `my-tickets.html` | `<cd-search-bar>` | Input de búsqueda textual (`input` con icono de lupa) que filtra por palabra clave en título y cuerpo. | `ticket.service.js` |
| **U-28** | Solicitud de cambio de prioridad | Detalle de ticket | `<cd-user-portal>` | Enlace *"Solicitar aumento de prioridad"* con selector y justificación de impacto académico. | `ticket.service.js` |
| **U-29** | Contador de tiempo transcurrido | `my-tickets.html` | `<cd-user-portal>` | Etiqueta dinámica: *"Creado hace 2 horas"* / *"Resuelto en 45 min"*. | `utils/date-formatter.js` |
| **U-30** | Historial de encuestas respondidas | `user-profile.html` | `<cd-user-profile>` | Pestaña *"Mis Calificaciones"* que lista las notas de satisfacción enviadas a los técnicos. | `satisfaction.service.js` |

---

### 2.2. Funcionalidades de Técnico (`TECHNICIAN`)

| ID | Funcionalidad | Pantalla / Archivo HTML | Componente Web Component | Elementos del DOM / Interacción | Servicio Invocado |
|:---|:---|:---|:---|:---|:---|
| **T-01** | Selector de Disponibilidad en Header | Header técnico | `<cd-technician-status>` | Desplegable en el avatar: 🟢 Disponible, 🔴 Ocupado, 🟡 En Pausa, ⚫ Fuera de Turno. | `technician.service.js` |
| **T-02** | Widget de Cronómetro (Time Tracking) | Detalle de ticket | `<cd-time-tracker>` | Widget flotante con cronómetro digital `[ 00:24:15 ]`, botón Start/Pause y guardado en minutos. | `timetracking.service.js` |
| **T-03** | Notas Internas Técnicas (Color diferenciado) | Detalle de ticket | `<cd-comment-thread>` | Checkbox *"Nota interna (solo soporte)"*; en el chat se renderiza con fondo amarillo y candado `🔒`. | `ticket.service.js` (`addTicketComment`) |
| **T-04** | Menú de Respuestas Rápidas (Canned) | Detalle de ticket | `<cd-canned-selector>` | Botón `[ ⚡ Respuestas ]` que abre menú emergente para insertar plantillas pre-escritas en el editor. | `cannedresponse.service.js` |
| **T-05** | Ficha del Solicitante (User Popover) | Detalle de ticket | `<cd-user-popover>` | Al posar el mouse sobre el solicitante, modal flotante con: Teléfono, Aula habitual, Tickets abiertos. | `user.service.js` |
| **T-06** | Crear Sub-tarea / Tarea Hija | Detalle de ticket | `<cd-subtasks-manager>` | Botón `[ + Agregar Sub-tarea ]`; tabla anidada de actividades pendientes con checks de completitud. | `ticket.service.js` |
| **T-07** | Vincular incidencias relacionadas | Detalle de ticket | `<cd-linked-tickets>` | Input autocompletable para enlazar incidencias duplicadas o bloqueantes (`Relacionado con #104`). | `ticket.service.js` |
| **T-08** | Carga masiva de notas de resolución | Detalle de ticket | `<cd-resolution-form>` | Modal al presionar *"Resolver"*: campos obligatorios de Causa Raíz y Solución Aplicada. | `ticket.service.js` (`transitionTicket`) |
| **T-09** | Botón de Pausa con justificación (PENDING) | Detalle de ticket | `<cd-assigned-tickets>` | Botón *"Pausar Ticket"*; selector de motivo (Esperando repuesto / Esperando respuesta de usuario). | `ticket.service.js` (`transitionTicket`) |
| **T-10** | Mini-Dashboard de Métricas Técnicas | `technician.html` | `<cd-technician-dashboard>` | Tarjetas KPI: Tickets resueltos hoy, promedio de tiempo de atención, tickets en riesgo de SLA. | `technician.service.js` / `dashboard.service.js` |
| **T-11** | Galería de evidencias técnicas | Detalle de ticket | `<cd-attachment-uploader>` | Botón para adjuntar fotos de cableado, pantallas de error o actas firmadas de entrega de equipo. | `attachment.service.js` |
| **T-12** | Convertir resolución en artículo KB | Detalle de ticket | `<cd-assigned-tickets>` | Botón *"Publicar en Base de Conocimiento"*; pre-llena un borrador de artículo con la solución del ticket. | `knowledgebase.service.js` |
| **T-13** | Re-categorización y cambio de prioridad | Detalle de ticket | `<cd-ticket-editor>` | Selectores inline en la barra lateral del ticket para corregir categoría o severidad. | `ticket.service.js` (`updateTicket`) |
| **T-14** | Checklist de procedimientos de soporte | Detalle de ticket | `<cd-checklist>` | Lista de verificación interactiva (ej. `[x] Verificó conexión [ ] Reinició router`). | `ticket.service.js` |
| **T-15** | Alerta visual de incumplimiento SLA | `technician.html` | `<cd-assigned-tickets>` | Fila de la tabla parpadea o se resalta con borde rojo si el ticket está a menos de 30 minutos de vencer. | Estado en render |
| **T-16** | Vista de Cola de Tickets sin Asignar | `technician.html` | `<cd-unassigned-pool>` | Pestaña *"Tickets Disponibles"*; lista con botón `[ ✋ Tomar este ticket ]` para autoasignación. | `ticket.service.js` (`claimTicket`) |
| **T-17** | Filtros rápidos de cola (Mis tickets / Vencidos) | `technician.html` | `<cd-assigned-tickets>` | Botones de filtro rápido tipo píldoras: `[ Todos ]`, `[ Críticos ]`, `[ En Pausa ]`, `[ SLA en Riesgo ]`. | Filtro local / API |
| **T-18** | Solicitud de reasignación a otro técnico | Detalle de ticket | `<cd-assigned-tickets>` | Botón *"Transferir Ticket"*; selector de técnico colega y campo de motivo de la transferencia. | `ticket.service.js` |
| **T-19** | Historial de intervenciones anteriores | Detalle de ticket | `<cd-equipment-history>` | Pestaña que muestra todos los tickets previos reportados en esa misma sala o equipo. | `ticket.service.js` |
| **T-20** | Visualizador de firmas / aceptación de entrega | Detalle de ticket | `<cd-signature-pad>` | Canvas interactivo en pantalla para captura de firma táctil del usuario al entregar equipo reparado. | `attachment.service.js` |
| **T-21** | Atajos de teclado en el detalle | Detalle de ticket | `<cd-assigned-tickets>` | Shortcuts (ej. `Ctrl+Enter` para enviar comentario, `Esc` para cerrar modales). | Listener `keydown` |
| **T-22** | Previsualización de adjuntos sin descargar | Detalle de ticket | `<cd-file-previewer>` | Modal embebido con visor de PDF y visualizador de imágenes para no saturar descargas. | `components/file-previewer.js` |
| **T-23** | Asociación con Activo IT / Computador | Detalle de ticket | `<cd-asset-tagger>` | Selector con autocompletado por serial o placa de inventario de la universidad. | `asset.service.js` |
| **T-24** | Plantilla estructurada de diagnóstico | Detalle de ticket | `<cd-resolution-form>` | Botón que inyecta en el textarea la estructura: `**Síntoma:** / **Causa:** / **Solución:**`. | Manipulación del DOM |
| **T-25** | Gestión personal de respuestas rápidas | `technician.html` | `<cd-canned-manager>` | Modal para crear, editar o eliminar las plantillas de texto personales del técnico. | `cannedresponse.service.js` |
| **T-26** | Indicador de tiempo en pausa acumulado | Detalle de ticket | `<cd-assigned-tickets>` | Badge que muestra: *"Tiempo pausado por usuario: 2h 15m (Descontado de SLA)"*. | Renderizado en DOM |
| **T-27** | Crear ticket en nombre del solicitante | `technician.html` | `<cd-user-portal mode="technician">` | Formulario con campo de búsqueda de usuario (`onBehalfOf`) para tickets reportados por teléfono. | `ticket.service.js` |
| **T-28** | Fusión de tickets duplicados (Merge) | Detalle de ticket | `<cd-merge-modal>` | Modal para buscar otro ticket y fusionar comentarios, cerrando el ticket secundario. | `ticket.service.js` (`mergeTickets`) |
| **T-29** | Reporte personal de productividad | `technician.html` | `<cd-technician-performance>` | Gráficos de barras con rendimiento semanal, tickets cerrados y valoración promedio recibida. | `technician.service.js` |
| **T-30** | Marcado de resolución parcial | Detalle de ticket | `<cd-assigned-tickets>` | Check para indicar si la solución es provisional o definitiva. | `ticket.service.js` |

---

### 2.3. Funcionalidades de Administrador (`ADMIN`)

| ID | Funcionalidad | Pantalla / Archivo HTML | Componente Web Component | Elementos del DOM / Interacción | Servicio Invocado |
|:---|:---|:---|:---|:---|:---|
| **A-01** | Editor de Políticas de SLA | `superadmin.html` | `<cd-sla-manager>` | Tabla de prioridades con inputs numéricos para minutos de primera respuesta y resolución. | `sla.service.js` |
| **A-02** | Reglas automáticas de escalamiento | `superadmin.html` | `<cd-sla-manager>` | Configurador: *"Si consume 75% del tiempo → Notificar a supervisor"*. | `sla.service.js` |
| **A-03** | Moderación de Artículos KB | `superadmin.html` | `<cd-kb-moderator>` | Bandeja de artículos en estado `DRAFT` con botones de *"Aprobar y Publicar"* o *"Rechazar"*. | `knowledgebase.service.js` |
| **A-04** | Gestor de Categorías de KB | `superadmin.html` | `<cd-kb-category-tree>` | Árbol jerárquico drag & drop para ordenar categorías y subcategorías de ayuda. | `knowledgebase.service.js` |
| **A-05** | Publicador de Anuncios y Avisos | `superadmin.html` | `<cd-announcement-editor>` | Formulario con selector de vigencia (fecha inicio/fin), nivel de severidad y campus afectado. | `announcement.service.js` |
| **A-06** | Editor de Plantillas de Correo | `superadmin.html` | `<cd-email-templates>` | Editor de texto con variables dinámicas (`{{ticketId}}`, `{{applicantName}}`, `{{status}}`). | `notification.service.js` |
| **A-07** | Dashboard Ejecutivo en Tiempo Real | `superadmin.html` | `<cd-admin-kpis>` | Gráficos de dona y barras: Cumplimiento de SLA (%), MTTR global y volumen por sede. | `dashboard.service.js` |
| **A-08** | Matriz de control de brechas de SLA | `superadmin.html` | `<cd-sla-breaches-table>` | Tabla con las incidencias vencidas, técnico a cargo y minutos de sobrepaso. | `sla.service.js` |
| **A-09** | Monitor de Carga de Técnicos en Vivo | `superadmin.html` | `<cd-technician-workload>` | Barras de progreso de carga de cada técnico (`Tickets activos / Capacidad máxima`). | `technician.service.js` |
| **A-10** | Generador de Reportes Personalizados | `superadmin.html` | `<cd-report-generator>` | Creador de reportes con filtros multidimensionales (fechas, campus, categoría, técnico). | `report.service.js` |
| **A-11** | Exportación tabular completa (Excel/CSV) | `superadmin.html` | `<cd-report-generator>` | Botón `[ 📊 Exportar Excel ]` con descarga instantánea de datos tabulados. | `report.service.js` |
| **A-12** | Programación de reportes automáticos | `superadmin.html` | `<cd-report-scheduler>` | Selector de periodicidad (Semanal/Mensual) y lista de correos de decanos/directores. | `report.service.js` |
| **A-13** | Administrador del Catálogo de Servicios | `superadmin.html` | `<cd-catalog-builder>` | Constructor drag & drop de tipos de solicitud y formularios dinámicos. | `serviceCatalog.service.js` |
| **A-14** | Acciones Masivas en Lote (Bulk Actions) | `ticketlist.html` | `<cd-batch-actions-bar>` | Barra flotante que aparece al seleccionar checkboxes en la tabla: *"Asignar a..."*, *"Cerrar"*. | `ticket.service.js` (`batchUpdate`) |
| **A-15** | Reasignación masiva por técnico ausente | `superadmin.html` | `<cd-bulk-reassign-modal>` | Modal para transferir todas las incidencias de un técnico incapacitado hacia otro técnico. | `assignment.service.js` |
| **A-16** | Configuración de Capacidad Máxima | `techguys.html` | `<cd-technician-directory>` | Input numérico en la fila de cada técnico para definir su límite de tickets simultáneos. | `technician.service.js` |
| **A-17** | Horarios Laborales y Festivos de Campus | `superadmin.html` | `<cd-business-hours-picker>` | Calendario interactivo para marcar feriados universitarios donde se pausa el reloj de SLA. | `sla.service.js` |
| **A-18** | Visor de Auditoría de Seguridad | `superadmin.html` | `<cd-audit-viewer>` | Tabla inmutable con buscador por usuario, IP, tipo de acción y fecha exacta. | `audit.service.js` |
| **A-19** | Repositorio Global de Respuestas Rápidas | `superadmin.html` | `<cd-canned-admin>` | CRUD de plantillas institucionales predeterminadas disponibles para todo el equipo. | `cannedresponse.service.js` |
| **A-20** | Configuración de Encuestas CSAT | `superadmin.html` | `<cd-csat-config>` | Toggle de activación de encuestas, días de gracia para responder y escalas (1-5 estrellas). | `satisfaction.service.js` |
| **A-21** | Reporte de Satisfacción por Técnico | `superadmin.html` | `<cd-csat-analytics>` | Ranking de técnicos según calificación promedio otorgada por la comunidad estudiantil. | `satisfaction.service.js` |
| **A-22** | Asistente de Configuración Inicial (Wizard)| `superadmin.html` | `<cd-setup-wizard>` | Guía paso a paso para poblar campus, categorías y primeras políticas SLA en instalaciones nuevas.| Estado local |
| **A-23** | Gestor de Etiquetas Globales (Tags) | `superadmin.html` | `<cd-tag-manager>` | Tabla con paleta de colores para crear etiquetas institucionales (ej. `#prioridad-rectoria`).| `tag.service.js` |
| **A-24** | Matriz de Notificaciones del Sistema | `superadmin.html` | `<cd-notification-matrix>` | Tabla de doble entrada (Evento vs Rol) para definir qué notificaciones se emiten por defecto. | `notification.service.js` |
| **A-25** | Simulador de Cálculo de SLA | `superadmin.html` | `<cd-sla-simulator>` | Calculadora donde se selecciona prioridad y fecha/hora para previsualizar cuándo vencería el ticket.| Cálculo cliente |
| **A-26** | Balanceador automático de carga | `superadmin.html` | `<cd-assignment-settings>` | Toggle para activar el algoritmo Round-Robin / Menor Carga en tickets nuevos. | `assignment.service.js` |
| **A-27** | Catálogo e Inventario de Activos IT | `superadmin.html` | `<cd-asset-manager>` | CRUD de computadores, proyectores y switchs con asignación a dependencias. | `asset.service.js` |
| **A-28** | Administrador de Categorías Dinámicas | `superadmin.html` | `<cd-category-admin>` | Pantalla para agregar nuevas categorías sin modificar código Java. | `ticket.service.js` |
| **A-29** | Configuración de Auto-cierre de Tickets | `superadmin.html` | `<cd-autoclose-settings>` | Input para fijar el plazo de cierre definitivo tras resolución (defecto: 5 días). | `config.service.js` |
| **A-30** | Copia de seguridad y exportación de datos | `superadmin.html` | `<cd-backup-manager>` | Botón de descarga de respaldo de incidencias en formato JSON/SQL para archivo histórico. | `report.service.js` |

---

## 🧩 3. ESPECIFICACIÓN DE NUEVOS WEB COMPONENTS (NATIVOS CON SHADOW DOM)

Todos los nuevos componentes se desarrollan como **Custom Elements nativos** siguiendo las reglas de oro de la arquitectura frontend de CampusDesk:
1. Heredan de `HTMLElement`.
2. Encapsulan su marcado y CSS con `this.attachShadow({ mode: 'open' })`.
3. Inyectan estilos mediante `<style>` dentro del Shadow Root consumiendo las variables CSS de [`main.css`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/Frontend/stylesheet/main.css).
4. No utilizan librerías externas: manipulan el DOM directamente con selectores `this.shadowRoot.querySelector`.
5. Se registran canónicamente con `customElements.define('cd-<nombre>', ComponentClass)`.

### 3.1. `<cd-csat-modal>` — Modal de Encuesta de Satisfacción
* **Archivo:** `Frontend/Scripts/components/csat-modal.js`
* **Uso:** `<cd-csat-modal ticket-id="105"></cd-csat-modal>`
* **Estructura Interna del Shadow DOM:**
  ```html
  <dialog class="csat-dialog">
      <div class="csat-card">
          <h2 class="csat-title">¿Cómo calificarías la atención recibida?</h2>
          <p class="csat-subtitle">Tu opinión ayuda a mejorar el servicio técnico del campus.</p>
          <div class="star-rating" role="radiogroup" aria-label="Calificación de 1 a 5 estrellas">
              <button class="star-btn" data-value="1" aria-label="1 estrella">★</button>
              <button class="star-btn" data-value="2" aria-label="2 estrellas">★</button>
              <button class="star-btn" data-value="3" aria-label="3 estrellas">★</button>
              <button class="star-btn" data-value="4" aria-label="4 estrellas">★</button>
              <button class="star-btn" data-value="5" aria-label="5 estrellas">★</button>
          </div>
          <textarea class="csat-feedback" placeholder="Comentarios adicionales sobre el servicio (opcional)..." rows="3"></textarea>
          <div class="csat-actions">
              <button class="btn btn--secondary" id="btn-skip">Omitir</button>
              <button class="btn btn--primary" id="btn-submit" disabled>Enviar Calificación</button>
          </div>
      </div>
  </dialog>
  ```
* **Eventos Emitidos:** `CustomEvent('csat-submitted', { detail: { ticketId, rating, feedback } })`.

### 3.2. `<cd-announcement-banner>` — Alerta Global de Mantenimiento
* **Archivo:** `Frontend/Scripts/components/announcement-banner.js`
* **Uso:** `<cd-announcement-banner></cd-announcement-banner>`
* **Estructura Interna del Shadow DOM:**
  ```html
  <div class="announcement-bar announcement-bar--warning" role="alert">
      <img src="./Assets/Icons/warning.svg" class="announcement-icon" alt="" />
      <div class="announcement-content">
          <strong class="announcement-title">Aviso de Mantenimiento:</strong>
          <span class="announcement-text">Interrupción programada de Wi-Fi en Edificio Central de 14:00 a 16:00.</span>
      </div>
      <button class="announcement-close" aria-label="Cerrar aviso">&times;</button>
  </div>
  ```

### 3.3. `<cd-time-tracker>` — Cronómetro y Registro de Tiempo para Técnicos
* **Archivo:** `Frontend/Scripts/components/time-tracker.js`
* **Uso:** `<cd-time-tracker ticket-id="105"></cd-time-tracker>`
* **Estructura Interna del Shadow DOM:**
  ```html
  <div class="tracker-card">
      <span class="tracker-label">Tiempo Invertido:</span>
      <span class="tracker-display" id="time-display">00:15:32</span>
      <div class="tracker-buttons">
          <button class="btn-icon btn--play" id="btn-toggle-timer" title="Iniciar/Pausar">▶</button>
          <button class="btn-icon btn--save" id="btn-save-time" title="Registrar Tiempo">💾</button>
      </div>
      <span class="tracker-total">Total acumulado: 45 min</span>
  </div>
  ```

### 3.4. `<cd-pagination>` — Componente Reutilizable de Paginación
* **Archivo:** `Frontend/Scripts/components/pagination.js`
* **Uso:** `<cd-pagination total-pages="10" current-page="1" page-size="20"></cd-pagination>`
* **Estructura Interna del Shadow DOM:**
  ```html
  <nav class="pagination-nav" aria-label="Navegación de páginas">
      <div class="pagination-info">Mostrando página <span id="cur">1</span> de <span id="tot">10</span></div>
      <div class="pagination-controls">
          <button class="page-btn" id="first" title="Primera página">&laquo;</button>
          <button class="page-btn" id="prev" title="Página anterior">&lsaquo;</button>
          <span class="page-current" id="page-num">1</span>
          <button class="page-btn" id="next" title="Página siguiente">&rsaquo;</button>
          <button class="page-btn" id="last" title="Última página">&raquo;</button>
      </div>
      <select class="page-size-selector" id="size-select">
          <option value="10">10 / pág</option>
          <option value="20" selected>20 / pág</option>
          <option value="50">50 / pág</option>
      </select>
  </nav>
  ```
* **Eventos Emitidos:** `CustomEvent('page-changed', { detail: { page, size } })`.

### 3.5. `<cd-attachment-uploader>` — Subida de Archivos con Drag & Drop
* **Archivo:** `Frontend/Scripts/components/attachment-uploader.js`
* **Uso:** `<cd-attachment-uploader max-size-mb="10" multiple></cd-attachment-uploader>`
* **Estructura Interna del Shadow DOM:**
  ```html
  <div class="uploader-dropzone" id="dropzone">
      <input type="file" id="file-input" hidden multiple accept=".jpg,.png,.pdf,.docx,.txt" />
      <img src="./Assets/Icons/upload.svg" class="upload-icon" alt="" />
      <p class="dropzone-text">Arrastra capturas o documentos aquí, o <button type="button" class="btn-browse" id="browse-btn">explora tus archivos</button></p>
      <span class="dropzone-hint">Formatos: JPG, PNG, PDF, DOCX (Máx. 10MB)</span>
      <ul class="file-list" id="file-list"></ul>
  </div>
  ```

### 3.6. `<cd-notification-center>` — Centro de Notificaciones en el Header
* **Archivo:** `Frontend/Scripts/components/notification-center.js`
* **Uso:** `<cd-notification-center></cd-notification-center>`
* **Estructura Interna del Shadow DOM:**
  ```html
  <div class="notif-wrapper">
      <button class="notif-bell" id="bell-btn" aria-label="Notificaciones">
          <img src="./Assets/Icons/bell.svg" alt="" width="20" height="20" />
          <span class="notif-badge" id="badge">3</span>
      </button>
      <div class="notif-dropdown" id="dropdown" hidden>
          <div class="notif-header">
              <h3>Notificaciones</h3>
              <button class="btn-text" id="mark-all-read">Marcar leídas</button>
          </div>
          <ul class="notif-items" id="items-list">
              <!-- Renderizado dinámico de notificaciones -->
          </ul>
      </div>
  </div>
  ```

---

## 🛠️ 4. MODIFICACIONES A COMPONENTES EXISTENTES

### 4.1. [`user-portal.js`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/Frontend/Scripts/components/user-portal.js) (`<cd-user-portal>`)
* **Ampliación en modo `create-ticket`:**
  * Inclusión del Web Component `<cd-attachment-uploader>` en el formulario.
  * Inserción de campos de Campus, Edificio y Aula.
  * Integración de `<cd-kb-suggestions>` para mostrar artículos coincidentes en tiempo real según el título ingresado.
* **Ampliación en modo `my-tickets`:**
  * Incorporación de botones de acción por estado:
    * Si `status === 'OPEN'`: Inyectar botón de cancelación con prompt de confirmación.
    * Si `status === 'RESOLVED'`: Inyectar botones *"Confirmar Solución"* (que abre `<cd-csat-modal>`) y *"Reabrir"*.

### 4.2. [`ticket-list.js`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/Frontend/Scripts/components/ticket-list.js) (`<cd-ticket-list>`)
* **Adopción de Paginación Backend:**
  * Reemplazar la llamada estática de array por la estructura paginada `PageResponse<TicketResponse>`.
  * Integrar al pie de la tabla el componente `<cd-pagination>`.
* **Nuevos Badges de Estado y SLA:**
  * Incorporación de estilos CSS para los nuevos estados: `.status-badge--pending`, `.status-badge--cancelled`, `.status-badge--reopened`, `.status-badge--escalated`.
  * Columna visual para el estado de SLA con indicador de cuenta regresiva.
  * Columna de Etiquetas (Tags) renderizadas como píldoras de colores.

### 4.3. [`assigned-tickets.js`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/Frontend/Scripts/components/assigned-tickets.js) (`<cd-assigned-tickets>`)
* **Botones de Control Técnico:**
  * Botón *"Pausar (Esperando Usuario)"* que ejecuta la transición a `PENDING`.
  * Botón *"Reanudar"* para regresar de `PENDING` a `IN_PROGRESS`.
  * Integración del componente de cronómetro `<cd-time-tracker>`.
  * Botón *"Reclamar Ticket"* cuando se visualiza el pool general de tickets abiertos sin asignar.

### 4.4. [`technician-dashboard.js`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/Frontend/Scripts/components/technician-dashboard.js) (`<cd-technician-dashboard>`)
* **Selector de Disponibilidad en el Header:**
  * Dropdown interactivo para actualizar `availabilityStatus` (`AVAILABLE`, `BUSY`, `ON_BREAK`, `OFF_DUTY`).
* **KPIs Extendidos:**
  * Indicador de tiempo medio de resolución (MTTR), cumplimiento de SLA personal (%) y promedio de calificación por estrellas.

---

## 🌐 5. NUEVOS SERVICIOS FRONTEND (`Frontend/Scripts/services/`)

Cada nuevo módulo de negocio del backend cuenta con su correspondiente servicio frontend que consume [`http-client.js`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/Frontend/Scripts/config/http-client.js):

| Archivo de Servicio | Funciones Exportadas | Endpoints Backend Consumidos |
|:---|:---|:---|
| `sla.service.js` | `getSlaPolicies()`, `updateSlaPolicy(id, data)`, `getSlaBreaches()`, `simulateSla(priority)` | `GET /api/sla/policies`, `PUT /api/sla/policies/{id}` |
| `notification.service.js` | `getMyNotifications(page)`, `markAsRead(id)`, `markAllAsRead()`, `getUnreadCount()` | `GET /api/notifications`, `PATCH /api/notifications/{id}/read` |
| `attachment.service.js` | `uploadAttachment(ticketId, file)`, `getTicketAttachments(ticketId)`, `downloadAttachment(id)` | `POST /api/tickets/{id}/attachments`, `GET /api/tickets/{id}/attachments/{attId}` |
| `knowledgebase.service.js` | `searchArticles(query)`, `getArticleById(id)`, `voteArticle(id, helpful)`, `createArticle(data)` | `GET /api/kb/articles?query=...`, `POST /api/kb/articles/{id}/vote` |
| `satisfaction.service.js` | `submitSurvey(ticketId, rating, comment)`, `getCsatReport(since)`, `getTechnicianRatings()` | `POST /api/tickets/{id}/satisfaction`, `GET /api/reports/satisfaction` |
| `announcement.service.js` | `getActiveAnnouncements()`, `createAnnouncement(data)`, `deleteAnnouncement(id)` | `GET /api/announcements/active`, `POST /api/announcements` |
| `tag.service.js` | `getAllTags()`, `createTag(name, color)`, `addTagToTicket(ticketId, tagId)` | `GET /api/tags`, `POST /api/tags` |
| `timetracking.service.js` | `logTime(ticketId, minutes, description)`, `getTicketTimeEntries(ticketId)` | `POST /api/tickets/{id}/time-entries`, `GET /api/tickets/{id}/time-entries` |
| `cannedresponse.service.js` | `getCannedResponses(category)`, `createCannedResponse(data)` | `GET /api/canned-responses`, `POST /api/canned-responses` |
| `audit.service.js` | `getAuditLogs(filters, page)` | `GET /api/audit/logs` |
| `report.service.js` | `getSlaReport(period)`, `getWorkloadReport()`, `exportExcel(filters)`, `exportPdf(ticketId)` | `GET /api/reports/sla-compliance`, `GET /api/reports/export/excel` |
| `servicecatalog.service.js`| `getCatalogItems()`, `getItemFields(itemId)` | `GET /api/service-catalog` |

---

## 🎨 6. ENCAPSULACIÓN EN SHADOW DOM Y DISEÑO RESPONSIVE

### 6.1. Penetración de Tokens de Diseño Globales
Para mantener la coherencia visual con [`main.css`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/Frontend/stylesheet/main.css) sin violar el encapsulamiento de Shadow DOM, todos los Web Components heredan las variables CSS declaradas en `:root`:

```css
/* Dentro del método getStyles() de cada componente */
:host {
    display: block;
    font-family: inherit;
    --primary: var(--color-primary, #2563eb);
    --primary-hover: var(--color-primary-dark, #1d4ed8);
    --surface: var(--color-surface, #ffffff);
    --border: var(--color-border, #e2e8f0);
    --text-main: var(--color-text, #1e293b);
    --text-muted: var(--color-muted, #64748b);
    --danger: var(--color-danger, #ef4444);
    --warning: var(--color-warning, #f59e0b);
    --success: var(--color-success, #10b981);
}
```

### 6.2. Adaptabilidad Móvil (Responsive Web Design)
Cada componente incluye sus propias Media Queries encapsuladas dentro del Shadow DOM:
* En pantallas `< 768px`: Las tablas (`ticket-table`) activan desplazamiento horizontal suave (`overflow-x: auto`), los modales pasan a ocupar pantalla completa tipo *sheet*, y los cronómetros flotantes se fijan en la parte inferior de la ventana (*bottom dock*).

---

## ⚠️ 7. MANEJO DE ERRORES Y SISTEMA DE TOASTS EN EL DOM

Se incorpora el componente `<cd-toast-manager>` (registrado en el `<body>` de cada página HTML) que escucha eventos globales de error emitidos por los servicios:

```javascript
// En Scripts/config/http-client.js o mediante CustomEvent global:
window.dispatchEvent(new CustomEvent('app-toast', {
    detail: {
        type: 'error', // 'success', 'warning', 'info'
        title: 'Error de Servidor',
        message: apiError.message // Extraído del JSON uniforme del backend
    }
}));
```

El `<cd-toast-manager>` renderiza dinámicamente tarjetas flotantes temporales en la esquina superior derecha (`z-index: 9999`) que desaparecen automáticamente a los 4 segundos, garantizando retroalimentación visual clara sin interrumpir el flujo del usuario.

---

## 🗺️ 8. MAPA COMPLETO DE ARCHIVOS FRONTEND A IMPLEMENTAR

```text
Frontend/
├── knowledge-base.html                            # [NUEVA PÁGINA] Portal de preguntas y FAQ
├── user-profile.html                              # [NUEVA PÁGINA] Edición de perfil y preferencias (U-15)
│
├── Scripts/
│   ├── components/
│   │   ├── csat-modal.js                          # [NUEVO] <cd-csat-modal> (Encuestas CSAT)
│   │   ├── announcement-banner.js                 # [NUEVO] <cd-announcement-banner> (Avisos globales)
│   │   ├── time-tracker.js                        # [NUEVO] <cd-time-tracker> (Cronómetro de horas)
│   │   ├── pagination.js                          # [NUEVO] <cd-pagination> (Paginador universal)
│   │   ├── attachment-uploader.js                 # [NUEVO] <cd-attachment-uploader> (Drag & Drop)
│   │   ├── notification-center.js                 # [NUEVO] <cd-notification-center> (Campana de avisos)
│   │   ├── knowledge-base.js                      # [NUEVO] <cd-knowledge-base> (Buscador KB)
│   │   ├── toast-manager.js                       # [NUEVO] <cd-toast-manager> (Alertas visuales flotantes)
│   │   ├── user-portal.js                         # [MODIFICADO] Adjuntos, campus, aula y acciones
│   │   ├── ticket-list.js                         # [MODIFICADO] Paginación, SLA y tags
│   │   ├── assigned-tickets.js                    # [MODIFICADO] Transiciones dinámicas y time tracking
│   │   └── technician-dashboard.js                # [MODIFICADO] Switch de disponibilidad y métricas MTTR
│   │
│   ├── services/
│   │   ├── ticket.service.js                      # [MODIFICADO: cancel, reopen, batch, claim]
│   │   ├── technician.service.js                  # [MODIFICADO: updateAvailability, getPerformance]
│   │   ├── user.service.js                        # [MODIFICADO: updateProfile, notificationsPreferences]
│   │   ├── sla.service.js                         # [NUEVO]
│   │   ├── notification.service.js                # [NUEVO]
│   │   ├── attachment.service.js                  # [NUEVO]
│   │   ├── knowledgebase.service.js               # [NUEVO]
│   │   ├── satisfaction.service.js                # [NUEVO]
│   │   ├── announcement.service.js                # [NUEVO]
│   │   ├── tag.service.js                         # [NUEVO]
│   │   ├── timetracking.service.js                # [NUEVO]
│   │   ├── cannedresponse.service.js              # [NUEVO]
│   │   ├── audit.service.js                       # [NUEVO]
│   │   ├── report.service.js                      # [NUEVO]
│   │   └── servicecatalog.service.js              # [NUEVO]
│   │
│   └── constants/
│       ├── messages.tickets.es.js                 # [MODIFICADO: textos de SLA, pausas y reaperturas]
│       ├── messages.sla.es.js                     # [NUEVO: etiquetas de plazos y vencimientos]
│       └── messages.notifications.es.js           # [NUEVO: plantillas de mensajes in-app]
```
