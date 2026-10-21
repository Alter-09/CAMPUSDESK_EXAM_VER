# 🎫 CampusDesk — Análisis de Funcionalidades Faltantes

## 📊 Estado Actual del Proyecto

El proyecto **CampusDesk** es un sistema de gestión de tickets (helpdesk) universitario construido con **Spring Boot 4.1.1**, **Spring Security**, **JWT**, **PostgreSQL**, **MapStruct** y **Swagger/OpenAPI**. Opera en entorno local de desarrollo.

### Módulos Implementados Actualmente

| Módulo | Funcionalidades existentes |
|:---|:---|
| **Ticket** | Crear ticket (USER), listar con filtros, ver por ID, actualizar ticket (solo si OPEN y sin técnico) |
| **Assignment** | Asignar técnico a ticket (ADMIN), reasignar con justificación (ADMIN) |
| **Lifecycle** | Transiciones de estado: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED |
| **Comment** | Crear comentario en ticket, listar comentarios con visibilidad por rol |
| **StatusHistory** | Historial de cambios de estado (solo lectura, inmutable) |
| **Dashboard** | Métricas globales (ADMIN), por técnico (TECHNICIAN), por solicitante (USER) |
| **Technician** | Listar todos los técnicos (ADMIN), listar disponibles ordenados por carga (ADMIN) |
| **User Management** | CRUD usuarios, asignar roles, asignar/revocar permisos, reset password |

### Roles del Sistema

| Rol | Descripción |
|:---|:---|
| `USER` | Solicitante/Estudiante que crea y da seguimiento a tickets |
| `TECHNICIAN` | Técnico que atiende y resuelve tickets asignados |
| `ADMIN` | Administrador que gestiona todo el sistema, usuarios y asignaciones |

---

## 🔍 Investigación Realizada

Plataformas analizadas como referencia:

| Plataforma | Tipo | Fortalezas clave identificadas |
|:---|:---|:---|
| **Freshdesk** | SaaS comercial | Automatización, SLA, templates, colisión de agentes, time tracking |
| **Zendesk** | SaaS comercial | Formularios dinámicos, macros, vistas personalizadas, Problem/Incident linking |
| **Jira Service Management** | SaaS comercial | Cambios/releases, CMDB, SLA con calendarios, automatización no-code |
| **ServiceNow** | SaaS enterprise | Catálogo de servicios, flujos de aprobación, analítica avanzada, escalaciones |
| **GLPI** | Open-source | Gestión de activos, CMDB, entidades multi-sitio, calendario de intervenciones |
| **osTicket** | Open-source | Respuestas predefinidas, tareas dentro de tickets, SLA básico, LDAP |
| **ManageEngine** | Comercial | Encuestas CSAT, base de conocimiento, descubrimiento de activos, control remoto |

---

## 📋 Funcionalidades Faltantes — 30 por Rol (90 Total)

> [!IMPORTANT]
> Cada funcionalidad fue validada contra lo que **ya existe** en el proyecto para evitar redundancias. Ninguna de las 90 funcionalidades listadas está implementada actualmente en el código fuente analizado.

---

### 🟢 ROL: USER (Solicitante / Estudiante) — 30 Funcionalidades

| # | ID | Funcionalidad | Descripción | Inspirada en |
|:---:|:---|:---|:---|:---|
| 1 | U-01 | Cancelar ticket propio | Permitir al USER cancelar un ticket que aún esté en estado OPEN (antes de asignación) | Freshdesk, Zendesk |
| 2 | U-02 | Adjuntar archivos al ticket | Subir capturas de pantalla, documentos o logs al crear o comentar un ticket | Zendesk, Jira SM |
| 3 | U-03 | Ver historial de todos sus tickets | Panel con listado completo de tickets creados, con paginación y búsqueda | ServiceNow, GLPI |
| 4 | U-04 | Confirmar resolución del ticket | Aceptar o rechazar la resolución propuesta por el técnico (RESOLVED → CLOSED o reabrir) | Freshdesk, ServiceNow |
| 5 | U-05 | Reabrir ticket cerrado | Solicitar reapertura de un ticket ya cerrado dentro de un periodo de gracia (ej: 7 días) | Zendesk, osTicket |
| 6 | U-06 | Encuesta de satisfacción (CSAT) | Calificar el servicio recibido (1-5 estrellas) al cerrarse un ticket | Freshdesk, ManageEngine |
| 7 | U-07 | Comentario con feedback de rechazo | Al rechazar una resolución, obligar al usuario a explicar por qué (texto obligatorio) | Jira SM, ServiceNow |
| 8 | U-08 | Ver base de conocimiento pública | Acceder a artículos de autoayuda organizados por categoría antes de crear un ticket | Zendesk, Freshdesk |
| 9 | U-09 | Buscar en la base de conocimiento | Buscador con palabras clave que sugiere artículos relevantes mientras escribe | ServiceNow, Freshdesk |
| 10 | U-10 | Notificación por email en cambios de estado | Recibir email automático cuando su ticket cambia de estado | Todas las plataformas |
| 11 | U-11 | Notificación cuando un técnico comenta | Alerta por email cuando el técnico agrega un comentario público al ticket | Zendesk, Freshdesk |
| 12 | U-12 | Ver tiempo estimado de resolución | Visualizar el SLA aplicable a su ticket y el tiempo restante estimado | ServiceNow, ManageEngine |
| 13 | U-13 | Duplicar ticket como plantilla | Crear un nuevo ticket basándose en los datos de uno existente propio | Jira SM |
| 14 | U-14 | Agregar observadores a su ticket | Invitar a otro usuario (ej: compañero) para que reciba notificaciones del ticket | Zendesk, Jira SM |
| 15 | U-15 | Editar perfil propio | Actualizar datos personales (nombre, teléfono, departamento) sin intervención de admin | GLPI, osTicket |
| 16 | U-16 | Ver anuncios del sistema | Visualizar avisos de mantenimiento, interrupciones de servicio o novedades publicados por el admin | ManageEngine, ServiceNow |
| 17 | U-17 | Filtrar tickets por rango de fechas | Agregar filtro de fecha de creación (desde/hasta) al listado de tickets propios | Zendesk, Freshdesk |
| 18 | U-18 | Ordenar tickets por múltiples criterios | Ordenar por prioridad, estado, categoría o fecha de actualización | GLPI, Jira SM |
| 19 | U-19 | Exportar tickets propios a CSV | Descargar un listado de sus tickets en formato CSV/Excel para seguimiento personal | ManageEngine, GLPI |
| 20 | U-20 | Recibir sugerencias de artículos al crear ticket | Al escribir el título, sugerir artículos de KB que podrían resolver su duda sin crear ticket | Zendesk, ServiceNow |
| 21 | U-21 | Votar artículos de conocimiento | Marcar artículos como útiles o no útiles (thumbs up/down) para mejorar la KB | Freshdesk, Zendesk |
| 22 | U-22 | Agregar etiquetas/tags al ticket | Añadir tags descriptivos a su ticket para facilitar la organización | Freshdesk, Zendesk |
| 23 | U-23 | Ver progreso visual del ticket | Barra o stepper visual que muestra la etapa actual del ticket en el flujo de vida | ServiceNow, ManageEngine |
| 24 | U-24 | Solicitar escalación del ticket | Botón para solicitar que su ticket sea escalado si lleva mucho tiempo sin resolverse | ManageEngine, ServiceNow |
| 25 | U-25 | Panel de dashboard personal | Vista resumida con conteo de tickets por estado y gráficos simples del usuario | Freshdesk, Jira SM |
| 26 | U-26 | Catálogo de servicios disponibles | Navegar un catálogo organizado de servicios IT que puede solicitar (software, accesos, hardware) | ServiceNow, Jira SM |
| 27 | U-27 | Formularios dinámicos por categoría | Campos del formulario de ticket que cambian según la categoría seleccionada | ServiceNow, Zendesk |
| 28 | U-28 | Marcar ticket como urgente con justificación | Solicitar cambio de prioridad a CRÍTICA proporcionando una razón (sujeto a aprobación admin) | GLPI, ManageEngine |
| 29 | U-29 | Recibir notificación de ticket próximo a vencer SLA | Alerta cuando el SLA de su ticket está por incumplirse | ServiceNow, Freshdesk |
| 30 | U-30 | Ver FAQ / Preguntas frecuentes | Sección de preguntas frecuentes categorizada para resolver dudas comunes sin crear ticket | Freshdesk, Zendesk |

---

### 🔵 ROL: TECHNICIAN (Técnico de Soporte) — 30 Funcionalidades

| # | ID | Funcionalidad | Descripción | Inspirada en |
|:---:|:---|:---|:---|:---|
| 1 | T-01 | Tomar ticket de cola (auto-asignarse) | El técnico selecciona un ticket no asignado de la cola y se lo asigna a sí mismo | osTicket, Freshdesk |
| 2 | T-02 | Registrar tiempo trabajado por ticket | Cronómetro o registro manual de horas dedicadas a cada ticket | Freshdesk, GLPI |
| 3 | T-03 | Notas internas privadas | Agregar notas que solo sean visibles para técnicos y admins, no para el solicitante | Zendesk, Freshdesk |
| 4 | T-04 | Respuestas predefinidas (canned responses) | Biblioteca de respuestas pre-escritas para problemas recurrentes (ej: "reinicie su equipo") | osTicket, Zendesk |
| 5 | T-05 | Ver perfil del solicitante con historial | Al abrir un ticket, ver la info del usuario y su historial de tickets previos | Zendesk, ServiceNow |
| 6 | T-06 | Crear sub-tareas dentro del ticket | Descomponer un ticket complejo en sub-tareas independientes con seguimiento propio | Jira SM, ServiceNow |
| 7 | T-07 | Vincular tickets relacionados | Enlazar tickets que están relacionados o que reportan el mismo problema | Zendesk, Jira SM |
| 8 | T-08 | Detección de colisión de agentes | Aviso en tiempo real si otro técnico está viendo o editando el mismo ticket | Freshdesk, Zendesk |
| 9 | T-09 | Solicitar información adicional al usuario | Cambiar ticket a estado PENDING (esperando info del usuario) con notificación automática | Zendesk, Freshdesk |
| 10 | T-10 | Dashboard personal del técnico | Panel con KPIs propios: tickets activos, tiempo promedio de resolución, carga actual | Zendesk, ManageEngine |
| 11 | T-11 | Adjuntar archivos a comentarios | Subir archivos, capturas o documentación técnica en las respuestas | Todas las plataformas |
| 12 | T-12 | Convertir resolución en artículo de KB | Transformar la solución de un ticket resuelto en un artículo de base de conocimiento | Jira SM, ServiceNow |
| 13 | T-13 | Cambiar prioridad del ticket | Ajustar la prioridad de un ticket asignado según evaluación técnica | Freshdesk, GLPI |
| 14 | T-14 | Cambiar categoría del ticket | Reclasificar un ticket si fue categorizado incorrectamente por el usuario | Freshdesk, osTicket |
| 15 | T-15 | Ver SLA activo y tiempo restante | Visualizar el temporizador SLA con alertas de proximidad de vencimiento | ServiceNow, ManageEngine |
| 16 | T-16 | Filtrar cola de tickets por especialidad | Filtrar tickets por categorías que coincidan con su especialidad técnica | GLPI, ManageEngine |
| 17 | T-17 | Recibir notificación de ticket nuevo asignado | Email/alerta al recibir un nuevo ticket asignado | Todas las plataformas |
| 18 | T-18 | Recibir notificación cuando usuario comenta | Alerta cuando el solicitante responde o agrega información al ticket | Zendesk, Freshdesk |
| 19 | T-19 | Actualizar su estado de disponibilidad | Cambiar manualmente entre AVAILABLE, BUSY, ON_LEAVE (disponible, ocupado, de licencia) | Freshdesk, ManageEngine |
| 20 | T-20 | Transferir ticket a otro técnico | Proponer reasignación del ticket a un colega con justificación (requiere aprobación admin) | osTicket, GLPI |
| 21 | T-21 | Calendario de intervenciones | Vista de calendario con tickets programados, fechas de SLA y citas agendadas | GLPI, Jira SM |
| 22 | T-22 | Buscar tickets por palabra clave | Buscar en título, descripción y comentarios de tickets asignados | Zendesk, ServiceNow |
| 23 | T-23 | Ver inventario/activos del usuario | Consultar los activos asignados al solicitante (PC, monitor, periféricos) vinculados al ticket | GLPI, ManageEngine |
| 24 | T-24 | Registrar diagnóstico técnico | Campo estructurado para documentar causa raíz, componente afectado y solución aplicada | ServiceNow, ManageEngine |
| 25 | T-25 | Plantillas de resolución por categoría | Templates de resolución pre-configurados por tipo de problema (hardware, software, red) | Freshdesk, osTicket |
| 26 | T-26 | Pausar temporizador SLA | Pausar el conteo del SLA cuando el ticket está en estado PENDING (esperando al usuario) | ServiceNow, Freshdesk |
| 27 | T-27 | Crear ticket a nombre de un usuario | Registrar un ticket en nombre de un usuario que reporta por teléfono o en persona | osTicket, GLPI |
| 28 | T-28 | Fusionar tickets duplicados | Combinar dos o más tickets que reportan el mismo problema en uno solo | Freshdesk, Zendesk |
| 29 | T-29 | Ver estadísticas de su desempeño mensual | Reporte personal con tendencias: tickets resueltos, tiempo promedio, satisfacción recibida | ManageEngine, Jira SM |
| 30 | T-30 | Checklist de pasos para resolución | Lista de verificación (checklist) de pasos a completar antes de marcar un ticket como resuelto | Zendesk, Jira SM |

---

### 🔴 ROL: ADMIN (Administrador del Sistema) — 30 Funcionalidades

| # | ID | Funcionalidad | Descripción | Inspirada en |
|:---:|:---|:---|:---|:---|
| 1 | A-01 | Configurar reglas de SLA | Definir políticas de SLA por prioridad y categoría (tiempo de respuesta y resolución) | ServiceNow, Zendesk |
| 2 | A-02 | Configurar reglas de escalación automática | Definir acciones automáticas cuando un SLA está por vencer o se incumple | ServiceNow, ManageEngine |
| 3 | A-03 | Gestionar base de conocimiento | CRUD completo de artículos de conocimiento: crear, editar, publicar, archivar | Freshdesk, Jira SM |
| 4 | A-04 | Categorizar artículos de conocimiento | Organizar artículos por categorías y subcategorías con orden de visualización | Zendesk, Freshdesk |
| 5 | A-05 | Gestionar anuncios del sistema | Crear, editar y programar avisos de mantenimiento o interrupciones visibles para usuarios | ManageEngine, ServiceNow |
| 6 | A-06 | Configurar plantillas de email/notificación | Personalizar los templates de notificaciones por email para cada evento del sistema | Freshdesk, Zendesk |
| 7 | A-07 | Panel de métricas avanzado con gráficos | Dashboard ejecutivo con gráficos de tendencias, volumen de tickets, y distribución por categoría | Todas las plataformas |
| 8 | A-08 | Reporte de cumplimiento de SLA | Informe detallado mostrando porcentaje de tickets dentro/fuera de SLA por periodo | ServiceNow, ManageEngine |
| 9 | A-09 | Reporte de desempeño por técnico | Comparativa de rendimiento entre técnicos: carga, tiempo medio de resolución, CSAT | Zendesk, Freshdesk |
| 10 | A-10 | Reporte de tickets por categoría y periodo | Análisis de volumen de tickets por categoría/prioridad en rangos de tiempo configurable | ManageEngine, Jira SM |
| 11 | A-11 | Exportar reportes a CSV/PDF | Descargar cualquier reporte del sistema en formato CSV o PDF | GLPI, ManageEngine |
| 12 | A-12 | Programar reportes automáticos por email | Configurar envío periódico de reportes (diario, semanal, mensual) por correo | Freshdesk, ServiceNow |
| 13 | A-13 | Gestionar catálogo de servicios | CRUD del catálogo de servicios IT que los usuarios pueden solicitar | ServiceNow, Jira SM |
| 14 | A-14 | Gestionar categorías y subcategorías | Administrar el catálogo de categorías de tickets (agregar, editar, desactivar) | Freshdesk, GLPI |
| 15 | A-15 | Configurar formularios dinámicos por categoría | Definir qué campos se muestran según la categoría del ticket | ServiceNow, Zendesk |
| 16 | A-16 | Reglas de asignación automática | Configurar auto-asignación basada en categoría, prioridad o carga de trabajo del técnico | Freshdesk, ServiceNow |
| 17 | A-17 | Gestionar horarios laborales | Definir horarios de operación, días festivos y calendario laboral para cálculos de SLA | Jira SM, ManageEngine |
| 18 | A-18 | Log de auditoría del sistema | Registro inmutable de todas las acciones administrativas (quién hizo qué, cuándo) | ServiceNow, Zendesk |
| 19 | A-19 | Gestionar respuestas predefinidas globales | CRUD de la biblioteca de canned responses disponibles para todos los técnicos | osTicket, Freshdesk |
| 20 | A-20 | Configurar encuestas de satisfacción | Personalizar preguntas, escala y momento de envío de las encuestas CSAT | ManageEngine, Freshdesk |
| 21 | A-21 | Reporte de satisfacción del usuario (CSAT) | Dashboard con análisis de resultados de encuestas por técnico, categoría y periodo | Freshdesk, Zendesk |
| 22 | A-22 | Bloquear/desbloquear usuarios masivamente | Operaciones en lote sobre cuentas de usuario (activar, desactivar, bloquear) | GLPI, ManageEngine |
| 23 | A-23 | Gestionar etiquetas/tags del sistema | CRUD del catálogo de tags disponibles para clasificación de tickets | Freshdesk, Zendesk |
| 24 | A-24 | Configurar reglas de notificación | Definir qué eventos disparan notificaciones y a qué roles o usuarios | Zendesk, ServiceNow |
| 25 | A-25 | Ver cola global de tickets sin asignar | Vista dedicada de todos los tickets OPEN sin técnico asignado, con alertas por antigüedad | osTicket, GLPI |
| 26 | A-26 | Reasignación masiva de tickets | Reasignar múltiples tickets de un técnico a otro (ej: cuando un técnico entra en licencia) | ManageEngine, Freshdesk |
| 27 | A-27 | Gestionar inventario de activos básico | Registro de equipos/activos IT asociables a usuarios y tickets (PC, impresoras, etc.) | GLPI, ManageEngine |
| 28 | A-28 | Configurar prioridades personalizadas | Definir niveles de prioridad adicionales o modificar los existentes con colores y tiempos SLA | ServiceNow, Jira SM |
| 29 | A-29 | Cerrar tickets inactivos automáticamente | Regla que cierra tickets en RESOLVED sin confirmación del usuario tras N días | Freshdesk, Zendesk |
| 30 | A-30 | Dashboard en tiempo real de operación | Monitor live con tickets entrantes, en progreso, y técnicos activos actualizado en tiempo real | ServiceNow, ManageEngine |

---

## 📈 Resumen de Cobertura

| Área funcional | Existente | Faltante (propuesto) |
|:---|:---:|:---:|
| Gestión de tickets (CRUD básico) | ✅ | — |
| Asignación de técnicos | ✅ | — |
| Transiciones de estado | ✅ | — |
| Comentarios | ✅ | — |
| Historial de estados | ✅ | — |
| Dashboard básico | ✅ | — |
| **SLA Management** | ❌ | A-01, A-02, T-15, T-26, U-12, U-29 |
| **Notificaciones por email** | ❌ | U-10, U-11, T-17, T-18, A-06, A-24 |
| **Base de conocimiento** | ❌ | U-08, U-09, U-20, U-21, U-30, T-12, A-03, A-04 |
| **Adjuntar archivos** | ❌ | U-02, T-11 |
| **Encuestas de satisfacción** | ❌ | U-06, A-20, A-21 |
| **Reportes avanzados/Exportación** | ❌ | U-19, T-29, A-07 a A-12 |
| **Catálogo de servicios** | ❌ | U-26, A-13 |
| **Gestión de activos** | ❌ | T-23, A-27 |
| **Automatización/Reglas** | ❌ | A-16, A-29 |
| **Auditoría** | ❌ | A-18 |
| **Formularios dinámicos** | ❌ | U-27, A-15 |
| **Time tracking** | ❌ | T-02 |
| **Escalaciones** | ❌ | U-24, A-02 |
| **Respuestas predefinidas** | ❌ | T-04, T-25, A-19 |

---

> [!NOTE]
> **Fuentes de la investigación**: Freshdesk (freshworks.com), Zendesk (zendesk.com), Jira Service Management (atlassian.com), ServiceNow (servicenow.com), GLPI (glpi-project.org), osTicket (osticket.com), ManageEngine ServiceDesk Plus (manageengine.com).

> [!TIP]
> Se recomienda implementar en orden de impacto: **SLA → Notificaciones → Adjuntar archivos → Base de conocimiento → Reportes avanzados → Encuestas CSAT** ya que estas funcionalidades ofrecen el mayor valor agregado al sistema existente.

---

## 🔧 Ampliaciones Necesarias a Estructuras Existentes

> [!IMPORTANT]
> Esta sección detalla **exactamente** qué se debe agregar o modificar en los enums, entidades, DTOs, authorities, reglas de lifecycle y propiedades ya existentes para soportar las 90 funcionalidades propuestas.

---

### 1. ENUMS — Valores Nuevos a Agregar

#### 1.1 `TicketStatus` — [TicketStatus.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/enums/TicketStatus.java)

**Estado actual:** `OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

| Valor nuevo | Justificación | Funcionalidades que lo requieren |
|:---|:---|:---|
| `PENDING` | Ticket en espera de respuesta del usuario (T-09, T-26) | T-09, T-26, A-29 |
| `CANCELLED` | Ticket cancelado por el solicitante antes de asignación (U-01) | U-01 |
| `REOPENED` | Ticket reabierto tras haber sido cerrado (U-05, U-07) | U-05, U-07 |
| `ESCALATED` | Ticket que ha sido escalado por incumplimiento de SLA o solicitud del usuario (U-24) | U-24, A-02 |

#### 1.2 `TransitionAction` — [TransitionAction.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/enums/TransitionAction.java)

**Estado actual:** `ASSIGN`, `REASSIGN`, `START_PROGRESS`, `RESOLVE`, `CONFIRM_CLOSE`

| Valor nuevo | Justificación | Funcionalidades que lo requieren |
|:---|:---|:---|
| `CANCEL` | El solicitante cancela su ticket abierto | U-01 |
| `REOPEN` | El solicitante reabre un ticket cerrado | U-05 |
| `REJECT_RESOLUTION` | El solicitante rechaza la resolución propuesta | U-04, U-07 |
| `PAUSE` | El técnico pone el ticket en espera (PENDING) | T-09 |
| `RESUME` | El técnico o usuario saca el ticket de PENDING | T-09 |
| `ESCALATE` | El usuario solicita escalación o el sistema la aplica por SLA | U-24, A-02 |
| `AUTO_CLOSE` | Cierre automático por inactividad configurable | A-29 |

#### 1.3 `StatusHistoryAction` — [StatusHistoryAction.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/enums/StatusHistoryAction.java)

**Estado actual:** `CREATION`, `ASSIGNMENT`, `STATUS_TRANSITION`, `CLOSURE`

| Valor nuevo | Justificación | Funcionalidades que lo requieren |
|:---|:---|:---|
| `CANCELLATION` | Registrar que el ticket fue cancelado | U-01 |
| `REOPENING` | Registrar la reapertura del ticket | U-05 |
| `REJECTION` | Registrar que la resolución fue rechazada | U-04, U-07 |
| `ESCALATION` | Registrar la escalación del ticket | U-24, A-02 |
| `PAUSE` | Registrar que el ticket fue puesto en espera | T-09 |
| `AUTO_CLOSURE` | Registrar cierre automático por inactividad | A-29 |
| `PRIORITY_CHANGE` | Registrar cambio de prioridad | T-13, U-28 |
| `CATEGORY_CHANGE` | Registrar cambio de categoría | T-14 |
| `MERGE` | Registrar fusión de tickets | T-28 |
| `SELF_ASSIGNMENT` | Registrar que un técnico tomó un ticket de la cola | T-01 |

#### 1.4 `CommentVisibility` — [CommentVisibility.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/enums/CommentVisibility.java)

**Estado actual:** `PUBLIC`, `INTERNAL`

| Valor nuevo | Justificación | Funcionalidades que lo requieren |
|:---|:---|:---|
| `TECHNICIAN_ONLY` | Notas visibles solo entre técnicos (no admin) para discusión entre pares | T-03 |

> [!NOTE]
> `INTERNAL` cubre admin+técnico. `TECHNICIAN_ONLY` añadiría granularidad extra. Evaluar si `INTERNAL` es suficiente o si se necesita esta distinción.

#### 1.5 `AssignmentType` — [AssignmentType.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/assignment/enums/AssignmentType.java)

**Estado actual:** `INITIAL`, `REASSIGNMENT`

| Valor nuevo | Justificación | Funcionalidades que lo requieren |
|:---|:---|:---|
| `SELF_ASSIGNMENT` | El técnico toma un ticket de la cola sin intervención del admin | T-01 |
| `AUTO_ASSIGNMENT` | Asignación automática por reglas del sistema (categoría, carga, especialidad) | A-16 |
| `BULK_REASSIGNMENT` | Reasignación masiva de tickets de un técnico a otro | A-26 |
| `TRANSFER_REQUEST` | Solicitud de transferencia hecha por un técnico (requiere aprobación) | T-20 |

#### 1.6 `TechnicianAvailabilityStatus` — [TechnicianAvailabilityStatus.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/enums/TechnicianAvailabilityStatus.java)

**Estado actual:** `AVAILABLE`, `BUSY`, `ON_LEAVE`, `OFFLINE`

| Valor nuevo | Justificación | Funcionalidades que lo requieren |
|:---|:---|:---|
| `IN_TRAINING` | Para técnicos en capacitación que no deben recibir tickets | T-19 |
| `PARTIALLY_AVAILABLE` | Técnico con carga parcial, acepta tickets de baja prioridad solamente | T-19, A-16 |

#### 1.7 `MetricPeriod` — [MetricPeriod.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/enums/MetricPeriod.java)

**Estado actual:** `TODAY`, `THIS_WEEK`, `THIS_MONTH`, `ALL_TIME`

| Valor nuevo | Justificación | Funcionalidades que lo requieren |
|:---|:---|:---|
| `LAST_7_DAYS` | Rango móvil de última semana para reportes | A-07, A-10 |
| `LAST_30_DAYS` | Rango móvil de último mes para reportes | A-07, A-10 |
| `LAST_90_DAYS` | Rango trimestral para análisis de tendencias | A-10 |
| `CUSTOM_RANGE` | Rango de fechas personalizado (desde/hasta) | U-17, A-10 |

#### 1.8 `TicketCategory` — [TicketCategory.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/enums/TicketCategory.java)

**Estado actual:** `HARDWARE`, `SOFTWARE`, `REDES`, `ACCESOS`, `OTROS`, `NETWORK`, `ACCESS`, `OTHER`

| Valor nuevo | Justificación | Funcionalidades que lo requieren |
|:---|:---|:---|
| `PRINTING` | Impresoras y periféricos de impresión | A-14, U-27 |
| `EMAIL_SYSTEMS` | Problemas con correo electrónico | A-14, U-27 |
| `SECURITY` | Incidentes de seguridad informática | A-14, U-27 |
| `TELEPHONY` | Telefonía IP y comunicaciones | A-14, U-27 |
| `DATABASE` | Bases de datos y almacenamiento | A-14, U-27 |
| `CLOUD_SERVICES` | Servicios en la nube | A-14, U-27 |

> [!TIP]
> A futuro, considerar migrar `TicketCategory` de un enum estático a una **tabla de base de datos** (`ticket_categories`) para permitir CRUD dinámico por el ADMIN sin redeploy (A-14).

#### 1.9 `TicketPriority` — [TicketPriority.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/enums/TicketPriority.java)

**Estado actual:** `BAJA`, `MEDIA`, `ALTA`, `CRITICA`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

> Sin valores nuevos necesarios. La prioridad actual cubre los 4 niveles estándar. Para A-28 (prioridades personalizadas), considerar migrar a tabla de BD similar a categorías.

---

### 2. NUEVOS ENUMS A CREAR

| Enum nuevo | Paquete canonical | Valores | Funcionalidades |
|:---|:---|:---|:---|
| `SlaStatus` | `com.roles.usermanagement.modules.sla.enums` | `WITHIN_SLA`, `WARNING`, `BREACHED`, `PAUSED` | A-01, T-15, T-26, U-12 |
| `NotificationEventType` | `com.roles.usermanagement.modules.notification.enums` | `TICKET_CREATED`, `TICKET_ASSIGNED`, `STATUS_CHANGED`, `COMMENT_ADDED`, `SLA_WARNING`, `SLA_BREACHED`, `TICKET_RESOLVED`, `TICKET_CLOSED`, `ESCALATION` | U-10, U-11, T-17, T-18, A-06, A-24 |
| `NotificationChannel` | `com.roles.usermanagement.modules.notification.enums` | `EMAIL`, `IN_APP` | U-10, U-11, A-06 |
| `NotificationStatus` | `com.roles.usermanagement.modules.notification.enums` | `PENDING`, `SENT`, `FAILED`, `READ` | A-06, A-24 |
| `ArticleStatus` | `com.roles.usermanagement.modules.knowledgebase.enums` | `DRAFT`, `PUBLISHED`, `ARCHIVED` | A-03, A-04 |
| `SatisfactionRating` | `com.roles.usermanagement.modules.satisfaction.enums` | `VERY_UNSATISFIED`, `UNSATISFIED`, `NEUTRAL`, `SATISFIED`, `VERY_SATISFIED` | U-06, A-20, A-21 |
| `AnnouncementStatus` | `com.roles.usermanagement.modules.announcement.enums` | `DRAFT`, `ACTIVE`, `EXPIRED`, `ARCHIVED` | A-05, U-16 |
| `FileAttachmentContext` | `com.roles.usermanagement.modules.attachment.enums` | `TICKET_CREATION`, `COMMENT`, `RESOLUTION`, `KNOWLEDGE_ARTICLE` | U-02, T-11 |
| `AuditActionType` | `com.roles.usermanagement.modules.audit.enums` | `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, `ROLE_CHANGED`, `PERMISSION_CHANGED`, `SLA_CONFIGURED`, `CATEGORY_CHANGED`, `BULK_OPERATION`, `SYSTEM_CONFIG_CHANGED` | A-18 |
| `EscalationTrigger` | `com.roles.usermanagement.modules.sla.enums` | `SLA_WARNING`, `SLA_BREACH`, `USER_REQUEST`, `INACTIVITY` | U-24, A-02 |
| `TimeEntryType` | `com.roles.usermanagement.modules.timetracking.enums` | `MANUAL`, `TIMER` | T-02 |
| `TicketRelationType` | `com.roles.usermanagement.modules.ticket.enums` | `RELATED`, `DUPLICATE`, `PARENT`, `CHILD`, `BLOCKS`, `BLOCKED_BY` | T-07, T-06, T-28 |
| `ExportFormat` | `com.roles.usermanagement.modules.report.enums` | `CSV`, `PDF` | U-19, A-11 |
| `ReportScheduleFrequency` | `com.roles.usermanagement.modules.report.enums` | `DAILY`, `WEEKLY`, `MONTHLY` | A-12 |
| `CannedResponseScope` | `com.roles.usermanagement.modules.cannedresponse.enums` | `GLOBAL`, `PERSONAL`, `CATEGORY_SPECIFIC` | T-04, T-25, A-19 |

---

### 3. ENTIDADES EXISTENTES — Campos Nuevos a Agregar

#### 3.1 `Ticket` — [Ticket.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/entity/Ticket.java)

| Campo nuevo | Tipo | Justificación | Funcionalidades |
|:---|:---|:---|:---|
| `closedAt` | `LocalDateTime` | Fecha real de cierre para calcular SLA de resolución | A-01, T-15 |
| `firstResponseAt` | `LocalDateTime` | Fecha del primer comentario del técnico (SLA de respuesta) | A-01, A-08 |
| `slaDeadlineAt` | `LocalDateTime` | Deadline de resolución según el SLA configurado | T-15, U-12, U-29 |
| `slaResponseDeadlineAt` | `LocalDateTime` | Deadline de primera respuesta según SLA | A-01, A-08 |
| `slaBreached` | `Boolean` | Flag que indica si el SLA fue incumplido | A-08, T-15 |
| `slaPausedAt` | `LocalDateTime` | Momento en que el SLA fue pausado (estado PENDING) | T-26 |
| `slaPausedDuration` | `Long` | Duración acumulada de pausas en minutos | T-26 |
| `parentTicketId` | `Long` (FK nullable) | Referencia al ticket padre para sub-tareas | T-06 |
| `mergedIntoId` | `Long` (FK nullable) | Si fue fusionado, referencia al ticket destino | T-28 |
| `tags` | `String` (comma-separated o relación M-N) | Tags del ticket para clasificación extra | U-22, A-23 |
| `resolutionNotes` | `String` (TEXT) | Diagnóstico técnico y causa raíz documentada | T-24 |
| `cancellationReason` | `String` | Razón de cancelación (cuando estado = CANCELLED) | U-01 |
| `reopenCount` | `Integer` (default 0) | Conteo de veces que el ticket ha sido reabierto | U-05 |
| `escalationReason` | `String` | Razón de la escalación (usuario o SLA) | U-24, A-02 |

#### 3.2 `Comment` — [Comment.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/entity/Comment.java)

| Campo nuevo | Tipo | Justificación | Funcionalidades |
|:---|:---|:---|:---|
| `hasAttachments` | `Boolean` (default false) | Flag rápido para saber si el comentario tiene archivos | U-02, T-11 |
| `isCannedResponse` | `Boolean` (default false) | Marcar si el comentario fue generado desde una respuesta predefinida | T-04 |

#### 3.3 `UserEntity` — [UserEntity.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/entity/UserEntity.java)

| Campo nuevo | Tipo | Justificación | Funcionalidades |
|:---|:---|:---|:---|
| `fullName` | `String` (length 100) | Nombre completo para mostrar en UI y reportes | U-15, T-05 |
| `phone` | `String` (length 20) | Teléfono de contacto | U-15 |
| `department` | `String` (length 80) | Departamento o dependencia universitaria | U-15, T-05 |
| `profileUpdatedAt` | `LocalDateTime` | Última vez que el usuario actualizó su perfil | U-15 |
| `notificationsEnabled` | `Boolean` (default true) | Preferencia de notificaciones del usuario | U-10, U-11 |

#### 3.4 `TechnicianProfile` — [TechnicianProfile.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/entity/TechnicianProfile.java)

| Campo nuevo | Tipo | Justificación | Funcionalidades |
|:---|:---|:---|:---|
| `totalResolvedTickets` | `Integer` (default 0) | Contador acumulado para métricas de rendimiento | T-29, A-09 |
| `avgResolutionMinutes` | `Double` | Promedio móvil de tiempo de resolución | T-29, A-09 |
| `avgSatisfactionRating` | `Double` | Promedio de calificación CSAT recibida | T-29, A-09, A-21 |
| `maxConcurrentTickets` | `Integer` (default 10) | Límite máximo de tickets simultáneos para auto-asignación | T-01, A-16 |

#### 3.5 `DashboardMetrics` / `DashboardMetricsResponse`

| Campo nuevo | Tipo | Justificación | Funcionalidades |
|:---|:---|:---|:---|
| `pendingTickets` | `long` | Conteo de tickets en estado PENDING | A-07, T-10 |
| `cancelledTickets` | `long` | Conteo de tickets cancelados | A-07 |
| `escalatedTickets` | `long` | Conteo de tickets escalados | A-07 |
| `avgResolutionTimeHours` | `double` | Promedio de horas de resolución | A-07, T-10, A-09 |
| `slaCompliancePercent` | `double` | Porcentaje de cumplimiento de SLA | A-07, A-08 |
| `avgSatisfactionRating` | `double` | Promedio general de satisfacción | A-07, A-21 |
| `ticketsByCategory` | `Map<String, Long>` | Distribución de tickets por categoría | A-07, A-10 |
| `ticketsByPriority` | `Map<String, Long>` | Distribución de tickets por prioridad | A-07, A-10 |

#### 3.6 `StatusHistory` — [StatusHistory.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/entity/StatusHistory.java)

| Campo nuevo | Tipo | Justificación | Funcionalidades |
|:---|:---|:---|:---|
| `comment` | `String` (TEXT, nullable) | Nota/justificación del cambio (ej: razón de rechazo, razón de escalación) | U-07, U-24, T-20 |

---

### 4. NUEVAS ENTIDADES A CREAR
| Entidad | Paquete canonical | Descripción | Funcionalidades |
|:---|:---|:---|:---|
| `SlaPolicy` | `com.roles.usermanagement.modules.sla.entity` | Define políticas SLA por prioridad/categoría (responseTimeMinutes, resolutionTimeMinutes, businessHoursOnly) | A-01, A-17 |
| `EscalationRule` | `com.roles.usermanagement.modules.sla.entity` | Reglas de escalación: % de SLA consumido, acción a tomar, destinatario de la alerta | A-02 |
| `Notification` | `com.roles.usermanagement.modules.notification.entity` | Registro de notificaciones: recipientUsername, eventType, channel, status, ticketId, sentAt | U-10, U-11, T-17, T-18, A-24 |
| `NotificationTemplate` | `com.roles.usermanagement.modules.notification.entity` | Plantillas de email por eventType: subject, bodyTemplate, enabled | A-06 |
| `NotificationPreference` | `com.roles.usermanagement.modules.notification.entity` | Preferencias por usuario: eventType → habilitado/deshabilitado | A-24 |
| `FileAttachment` | `com.roles.usermanagement.modules.attachment.entity` | Archivos adjuntos: fileName, contentType, size, storagePath, ticketId, commentId, uploadedBy | U-02, T-11 |
| `KnowledgeArticle` | `com.roles.usermanagement.modules.knowledgebase.entity` | Artículos KB: title, content, category, status, authorUsername, viewCount, helpfulCount, notHelpfulCount | A-03, U-08 |
| `KnowledgeCategory` | `com.roles.usermanagement.modules.knowledgebase.entity` | Categorías de KB: name, parentCategoryId, displayOrder | A-04, U-08 |
| `SatisfactionSurvey` | `com.roles.usermanagement.modules.satisfaction.entity` | Encuesta: ticketId, respondentUsername, rating, comment, submittedAt | U-06, A-20, A-21 |
| `SatisfactionConfig` | `com.roles.usermanagement.modules.satisfaction.entity` | Configuración de encuestas: enabled, scaleType, autoSendOnClose, gracePeriodDays | A-20 |
| `Announcement` | `com.roles.usermanagement.modules.announcement.entity` | Avisos: title, content, status, startDate, endDate, createdBy | A-05, U-16 |
| `Tag` | `com.roles.usermanagement.modules.tag.entity` | Tags del sistema: name, color, description | A-23, U-22 |
| `TicketTag` | `com.roles.usermanagement.modules.tag.entity` | Relación M-N: ticketId, tagId | A-23, U-22 |
| `TicketWatcher` | `com.roles.usermanagement.modules.ticket.entity` | Observadores: ticketId, watcherUsername, addedAt | U-14 |
| `TicketRelation` | `com.roles.usermanagement.modules.ticket.entity` | Vínculos entre tickets: sourceTicketId, targetTicketId, relationType | T-07, T-06 |
| `TimeEntry` | `com.roles.usermanagement.modules.timetracking.entity` | Registro de tiempo: ticketId, technicianUsername, startedAt, endedAt, durationMinutes, description, entryType | T-02 |
| `CannedResponse` | `com.roles.usermanagement.modules.cannedresponse.entity` | Respuestas pre-escritas: title, content, category, scope, createdByUsername | T-04, T-25, A-19 |
| `AuditLog` | `com.roles.usermanagement.modules.audit.entity` | Log de auditoría: actionType, performedByUsername, targetEntity, targetId, details, timestamp | A-18 |
| `BusinessHours` | `com.roles.usermanagement.modules.sla.entity` | Horarios laborales: dayOfWeek, startTime, endTime, isHoliday, holidayDate, description | A-17 |
| `ReportSchedule` | `com.roles.usermanagement.modules.report.entity` | Programación de reportes: reportType, frequency, recipientEmails, lastSentAt, enabled | A-12 |
| `TicketChecklist` | `com.roles.usermanagement.modules.ticket.entity` | Checklist de resolución: ticketId, itemText, completed, completedAt, orderIndex | T-30 |
| `AssetItem` | `com.roles.usermanagement.modules.asset.entity` | Activos IT: serialNumber, name, type, assignedToUsername, status, purchaseDate | A-27, T-23 |
| `ServiceCatalogItem` | `com.roles.usermanagement.modules.servicecatalog.entity` | Catálogo de servicios: name, description, category, estimatedResolutionTime, formFields, enabled | A-13, U-26 |

---

### 5. AUTHORITIES (Permisos) — Valores Nuevos en `UserRoles.Authority`

**Archivo:** [UserRoles.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/service/UserRoles.java)

**Estado actual (18 permisos):**
```
USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE, ROLE_ASSIGN, PERMISSION_ASSIGN,
ROLE_MANAGE, PERMISSION_MANAGE, TICKET_CREATE, TICKET_READ_OWN, TICKET_UPDATE_OWN,
TICKET_CLOSE, TICKET_READ_ASSIGNED, TICKET_STATUS_UPDATE, TICKET_READ_ALL,
TICKET_ASSIGN, REPORT_READ, COMMENT_CREATE, COMMENT_READ, HISTORY_READ
```

| Authority nuevo | Rol(es) asignado(s) | Funcionalidades |
|:---|:---|:---|
| `TICKET_CANCEL_OWN` | USER | U-01 |
| `TICKET_REOPEN` | USER | U-05 |
| `TICKET_ESCALATION_REQUEST` | USER | U-24 |
| `TICKET_SELF_ASSIGN` | TECHNICIAN | T-01 |
| `TICKET_CHANGE_PRIORITY` | TECHNICIAN, ADMIN | T-13 |
| `TICKET_CHANGE_CATEGORY` | TECHNICIAN, ADMIN | T-14 |
| `TICKET_MERGE` | TECHNICIAN, ADMIN | T-28 |
| `TICKET_TRANSFER_REQUEST` | TECHNICIAN | T-20 |
| `TICKET_CREATE_ON_BEHALF` | TECHNICIAN | T-27 |
| `TICKET_LINK` | TECHNICIAN, ADMIN | T-07, T-06 |
| `ATTACHMENT_UPLOAD` | USER, TECHNICIAN | U-02, T-11 |
| `ATTACHMENT_DOWNLOAD` | USER, TECHNICIAN, ADMIN | U-02, T-11 |
| `KB_READ` | USER, TECHNICIAN, ADMIN | U-08, U-09, U-20 |
| `KB_CREATE` | TECHNICIAN, ADMIN | T-12, A-03 |
| `KB_UPDATE` | ADMIN | A-03 |
| `KB_DELETE` | ADMIN | A-03 |
| `KB_VOTE` | USER | U-21 |
| `SURVEY_SUBMIT` | USER | U-06 |
| `SURVEY_MANAGE` | ADMIN | A-20 |
| `SLA_MANAGE` | ADMIN | A-01, A-02 |
| `SLA_VIEW` | TECHNICIAN, ADMIN | T-15 |
| `NOTIFICATION_MANAGE` | ADMIN | A-06, A-24 |
| `ANNOUNCEMENT_MANAGE` | ADMIN | A-05 |
| `ANNOUNCEMENT_READ` | USER, TECHNICIAN, ADMIN | U-16 |
| `TAG_MANAGE` | ADMIN | A-23 |
| `TAG_ASSIGN` | USER, TECHNICIAN | U-22 |
| `TIME_ENTRY_CREATE` | TECHNICIAN | T-02 |
| `TIME_ENTRY_READ` | TECHNICIAN, ADMIN | T-02, A-09 |
| `CANNED_RESPONSE_MANAGE` | ADMIN | A-19 |
| `CANNED_RESPONSE_USE` | TECHNICIAN | T-04, T-25 |
| `REPORT_EXPORT` | ADMIN | A-11, A-12 |
| `REPORT_ADVANCED` | ADMIN | A-07, A-08, A-09, A-10 |
| `AUDIT_READ` | ADMIN | A-18 |
| `ASSET_MANAGE` | ADMIN | A-27 |
| `ASSET_READ` | TECHNICIAN, ADMIN | T-23 |
| `SERVICE_CATALOG_MANAGE` | ADMIN | A-13 |
| `SERVICE_CATALOG_READ` | USER, TECHNICIAN, ADMIN | U-26 |
| `BULK_OPERATION` | ADMIN | A-22, A-26 |
| `BUSINESS_HOURS_MANAGE` | ADMIN | A-17 |
| `CHECKLIST_MANAGE` | TECHNICIAN | T-30 |
| `PROFILE_UPDATE_OWN` | USER, TECHNICIAN | U-15 |
| `DASHBOARD_PERSONAL` | USER, TECHNICIAN | U-25, T-10 |
| `TICKET_EXPORT_OWN` | USER | U-19 |

---

### 6. REGLAS DE LIFECYCLE — Nuevas Reglas para `LifecycleRuleInitializer`

**Archivo:** [LifecycleRuleInitializer.java](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/config/LifecycleRuleInitializer.java)

**Reglas actuales (5):**
1. OPEN → ASSIGNED (ASSIGN, ADMIN)
2. ASSIGNED → ASSIGNED (REASSIGN, ADMIN)
3. ASSIGNED → IN_PROGRESS (START_PROGRESS, TECHNICIAN, mustBeAssigned)
4. IN_PROGRESS → RESOLVED (RESOLVE, TECHNICIAN, mustBeAssigned)
5. RESOLVED → CLOSED (CONFIRM_CLOSE, USER, mustBeApplicant)

**Reglas nuevas a agregar:**

| # | From | To | Action | Required Role | mustBeApplicant | mustBeAssigned | Funcionalidad |
|:---:|:---|:---|:---|:---|:---:|:---:|:---|
| 6 | OPEN | CANCELLED | CANCEL | ROLE_USER | ✅ | ❌ | U-01 |
| 7 | CLOSED | REOPENED | REOPEN | ROLE_USER | ✅ | ❌ | U-05 |
| 8 | REOPENED | ASSIGNED | ASSIGN | ROLE_ADMIN | ❌ | ❌ | U-05 (reasignar tras reabrir) |
| 9 | RESOLVED | IN_PROGRESS | REJECT_RESOLUTION | ROLE_USER | ✅ | ❌ | U-04, U-07 |
| 10 | IN_PROGRESS | PENDING | PAUSE | ROLE_TECHNICIAN | ❌ | ✅ | T-09 |
| 11 | PENDING | IN_PROGRESS | RESUME | ROLE_TECHNICIAN | ❌ | ✅ | T-09 |
| 12 | ASSIGNED | ESCALATED | ESCALATE | ROLE_USER | ✅ | ❌ | U-24 |
| 13 | IN_PROGRESS | ESCALATED | ESCALATE | ROLE_ADMIN | ❌ | ❌ | A-02 |
| 14 | ESCALATED | ASSIGNED | ASSIGN | ROLE_ADMIN | ❌ | ❌ | A-02 (reasignar tras escalar) |
| 15 | RESOLVED | CLOSED | AUTO_CLOSE | ROLE_ADMIN | ❌ | ❌ | A-29 |

---

### 7. DTOs EXISTENTES — Campos Nuevos

#### 7.1 `TicketCreateRequest` → agregar campos opcionales

| Campo | Tipo | Funcionalidad |
|:---|:---|:---|
| `tags` | `List<String>` (nullable) | U-22 |
| `onBehalfOfUsername` | `String` (nullable) | T-27 |
| `serviceCatalogItemId` | `Long` (nullable) | U-26 |

#### 7.2 `TicketResponse` → agregar campos

| Campo | Tipo | Funcionalidad |
|:---|:---|:---|
| `tags` | `List<String>` | U-22 |
| `closedAt` | `LocalDateTime` | A-01 |
| `slaDeadlineAt` | `LocalDateTime` | U-12, T-15 |
| `slaBreached` | `Boolean` | T-15, A-08 |
| `parentTicketId` | `Long` | T-06 |
| `reopenCount` | `Integer` | U-05 |
| `hasAttachments` | `Boolean` | U-02 |
| `watcherCount` | `Integer` | U-14 |
| `resolutionNotes` | `String` | T-24 |

#### 7.3 `StateTransitionRequest` → agregar campo

| Campo | Tipo | Funcionalidad |
|:---|:---|:---|
| `reason` | `String` (nullable) | U-07 (razón de rechazo), U-01 (razón de cancelación), U-24 (razón de escalación) |

#### 7.4 `CommentCreateRequest` → agregar campo

| Campo | Tipo | Funcionalidad |
|:---|:---|:---|
| `attachmentIds` | `List<Long>` (nullable) | T-11, U-02 |
| `cannedResponseId` | `Long` (nullable) | T-04 |

#### 7.5 `TechnicianResponse` → agregar campos

| Campo | Tipo | Funcionalidad |
|:---|:---|:---|
| `totalResolvedTickets` | `Integer` | A-09, T-29 |
| `avgResolutionMinutes` | `Double` | A-09, T-29 |
| `avgSatisfactionRating` | `Double` | A-09, A-21 |
| `maxConcurrentTickets` | `Integer` | A-16 |

---

### 8. PROPIEDADES DE CONFIGURACIÓN — Nuevas para `application-dev.properties`

| Propiedad | Valor sugerido | Funcionalidad |
|:---|:---|:---|
| `app.sla.enabled` | `true` | A-01 |
| `app.sla.default-response-minutes` | `60` | A-01 |
| `app.sla.default-resolution-minutes` | `480` | A-01 |
| `app.notification.enabled` | `true` | U-10, U-11, T-17, T-18 |
| `app.notification.from-email` | `noreply@campusdesk.local` | A-06 |
| `app.attachment.enabled` | `true` | U-02, T-11 |
| `app.attachment.max-file-size-mb` | `10` | U-02, T-11 |
| `app.attachment.allowed-extensions` | `jpg,png,pdf,doc,docx,xlsx,txt,log,zip` | U-02, T-11 |
| `app.attachment.storage-path` | `./uploads/attachments` | U-02, T-11 |
| `app.survey.enabled` | `true` | U-06, A-20 |
| `app.survey.auto-send-on-close` | `true` | A-20 |
| `app.survey.grace-period-days` | `7` | A-20 |
| `app.ticket.reopen-grace-days` | `7` | U-05 |
| `app.ticket.auto-close-after-days` | `5` | A-29 |
| `app.ticket.max-reopens` | `3` | U-05 |
| `app.kb.enabled` | `true` | U-08, A-03 |
| `app.export.enabled` | `true` | U-19, A-11 |
| `app.business-hours.start` | `08:00` | A-17 |
| `app.business-hours.end` | `18:00` | A-17 |
| `app.business-hours.timezone` | `America/Bogota` | A-17 |
| `spring.servlet.multipart.max-file-size` | `10MB` | U-02, T-11 |
| `spring.servlet.multipart.max-request-size` | `50MB` | U-02, T-11 |

---

### 9. NUEVOS MÓDULOS A CREAR (Estructura de Paquetes Homogénea)

Para **mantener la arquitectura del proyecto intacta** como ya está organizada, los nuevos módulos de negocio deben replicar exactamente la estructura interna uniforme que tienen los módulos existentes (`ticket`, `comment`, `technician`, `assignment`, `lifecycle`, `statushistory`, `dashboard`).

Cada nuevo módulo dentro de `com.roles.usermanagement.modules.<nombre>` contendrá estrictamente sus 7 subpaquetes estándar:
* `controller/`: Controladores REST anotados con `@RestController` y `@RequestMapping`.
* `dto/`: Records y clases de transferencia de datos de entrada/salida (Requests y Responses).
* `entity/`: Entidades JPA anotadas con `@Entity`, `@Table` e índices.
* `enums/`: Enumeraciones específicas del módulo.
* `mapper/`: Componentes `@Component` de transformación Entidad ↔ DTO.
* `repository/`: Interfaces Spring Data JPA (`JpaRepository`, `JpaSpecificationExecutor`).
* `service/`: Servicios con lógica de negocio `@Service` y `@Transactional`.

| Módulo | Paquete Raíz Canónico | Subpaquetes Estándar Obligatorios | Funcionalidades cubiertas |
|:---|:---|:---|:---|
| `sla` | `com.roles.usermanagement.modules.sla` | `controller, dto, entity, enums, mapper, repository, service` | A-01, A-02, A-17, T-15, T-26, U-12, U-29 |
| `notification` | `com.roles.usermanagement.modules.notification` | `controller, dto, entity, enums, mapper, repository, service` | U-10, U-11, T-17, T-18, A-06, A-24 |
| `attachment` | `com.roles.usermanagement.modules.attachment` | `controller, dto, entity, enums, mapper, repository, service` | U-02, T-11 |
| `knowledgebase` | `com.roles.usermanagement.modules.knowledgebase` | `controller, dto, entity, enums, mapper, repository, service` | U-08, U-09, U-20, U-21, U-30, T-12, A-03, A-04 |
| `satisfaction` | `com.roles.usermanagement.modules.satisfaction` | `controller, dto, entity, enums, mapper, repository, service` | U-06, A-20, A-21 |
| `announcement` | `com.roles.usermanagement.modules.announcement` | `controller, dto, entity, enums, mapper, repository, service` | A-05, U-16 |
| `tag` | `com.roles.usermanagement.modules.tag` | `controller, dto, entity, enums, mapper, repository, service` | A-23, U-22 |
| `timetracking` | `com.roles.usermanagement.modules.timetracking` | `controller, dto, entity, enums, mapper, repository, service` | T-02 |
| `cannedresponse` | `com.roles.usermanagement.modules.cannedresponse` | `controller, dto, entity, enums, mapper, repository, service` | T-04, T-25, A-19 |
| `audit` | `com.roles.usermanagement.modules.audit` | `controller, dto, entity, enums, mapper, repository, service` | A-18 |
| `asset` | `com.roles.usermanagement.modules.asset` | `controller, dto, entity, enums, mapper, repository, service` | A-27, T-23 |
| `servicecatalog` | `com.roles.usermanagement.modules.servicecatalog` | `controller, dto, entity, enums, mapper, repository, service` | A-13, U-26 |
| `report` | `com.roles.usermanagement.modules.report` | `controller, dto, entity, enums, mapper, repository, service` | A-07 a A-12, U-19, T-29 |

> [!IMPORTANT]
> **Preservación del Módulo de Perfil de Usuario (U-15):**
> Para no fragmentar la arquitectura de usuarios ni crear paquetes huérfanos, la funcionalidad **U-15** (actualización de perfil, nombre, teléfono, departamento y preferencias) **no** se implementa como un módulo separado en `modules.profile`. Se extiende de forma natural en la arquitectura Core de Usuario ya existente:
> * **Entidad:** `com.roles.usermanagement.persistance.entity.UserEntity`
> * **DTO:** `com.roles.usermanagement.domain.dto.UserProfileUpdateRequest`
> * **Servicio:** `com.roles.usermanagement.domain.service.UserService`
> * **Repositorio:** `com.roles.usermanagement.persistance.repository.UserRepository`
> * **Controlador:** `com.roles.usermanagement.web.controller.UserController` (`PUT /api/user/profile`)

---

### 10. RESUMEN CUANTITATIVO DE AMPLIACIONES

| Tipo de estructura | Existentes | Valores/Items nuevos | Total tras ampliación |
|:---:|:---:|:---:|:---:|
| **Valores en enums existentes** | 33 | 27 | 60 |
| **Enums nuevos** | 0 | 15 | 15 |
| **Campos en entidades existentes** | ~45 | 30 | ~75 |
| **Entidades nuevas** | 0 | 22 | 22 |
| **Authorities** | 20 | 43 | 63 |
| **Lifecycle rules** | 5 | 10 | 15 |
| **Campos en DTOs existentes** | ~35 | 21 | ~56 |
| **Properties de configuración** | ~12 | 22 | ~34 |
| **Módulos nuevos de negocio** | 0 | 13 | 13 |

---

## 🏛️ PARTE 3: ANÁLISIS DE PERSISTENCIA JPA Y GAPS TÉCNICOS GLOBALES

---

### 11. MODIFICACIONES Y EXTENSIONES A REPOSITORIOS JPA EXISTENTES

A continuación se detalla el diagnóstico exacto de los repositorios JPA actuales y los métodos que **deben agregarse** para que el proyecto pueda soportar las 90 funcionalidades y operar sin errores de concurrencia o rendimiento.

#### 11.1. [`TicketRepository`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/repository/TicketRepository.java)
* **Diagnóstico actual:** Solo extiende de `JpaRepository<Ticket, Long>` y `JpaSpecificationExecutor<Ticket>`, con un único método `@Lock(LockModeType.PESSIMISTIC_WRITE) findForUpdate(@Param("id") Long id)`.
* **Limitaciones:** Carece de búsquedas directas por solicitante/técnico con paginación, conteos por rango de fechas, detección de tickets vencidos (SLA) o próximos a vencer, y actualización en bloque.
* **Métodos a agregar:**

```java
package com.roles.usermanagement.modules.ticket.repository;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findForUpdate(@Param("id") Long id);

    // Búsquedas paginadas directas por usuario / técnico
    Page<Ticket> findByApplicant_UsernameOrderByCreatedAtDesc(String applicantUsername, Pageable pageable);
    Page<Ticket> findByTechnician_UsernameOrderByCreatedAtDesc(String technicianUsername, Pageable pageable);

    // Consulta del pool general de tickets sin asignar (para técnicos / autoasignación)
    Page<Ticket> findByStatusAndTechnicianIsNullOrderByCreatedAtAsc(TicketStatus status, Pageable pageable);

    // Tickets vencidos o próximos a vencer de acuerdo con SLA
    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') AND t.slaDueAt < :threshold")
    List<Ticket> findPendingBreachingSla(@Param("threshold") LocalDateTime threshold);

    // Búsqueda de tickets candidatos para auto-cierre tras N días de inactividad en estado RESOLVED
    @Query("SELECT t FROM Ticket t WHERE t.status = 'RESOLVED' AND t.updatedAt <= :inactivityLimit")
    List<Ticket> findResolvedTicketsReadyForAutoClose(@Param("inactivityLimit") LocalDateTime inactivityLimit);

    // Sub-tickets e incidencias relacionadas (Jira / ServiceNow hierarchy)
    List<Ticket> findByParentTicketId(Long parentTicketId);

    // Detección de posibles duplicados (misma categoría, misma ubicación o solicitante, en estado abierto)
    @Query("SELECT t FROM Ticket t WHERE t.applicant.username = :applicantUsername " +
           "AND t.category = :category AND t.status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS') AND t.id <> :excludeId")
    List<Ticket> findPotentialDuplicates(@Param("applicantUsername") String applicantUsername,
                                         @Param("category") TicketCategory category,
                                         @Param("excludeId") Long excludeId);

    // Actualización masiva de estado (para operaciones por lote del ADMIN)
    @Modifying
    @Query("UPDATE Ticket t SET t.status = :newStatus, t.updatedAt = :now WHERE t.id IN :ticketIds")
    int updateStatusInBatch(@Param("ticketIds") Collection<Long> ticketIds,
                            @Param("newStatus") TicketStatus newStatus,
                            @Param("now") LocalDateTime now);

    // Conteos rápidos para validación de cuotas o dashboard
    long countByApplicant_UsernameAndStatusIn(String applicantUsername, Collection<TicketStatus> statuses);
    long countByTechnician_UsernameAndStatusIn(String technicianUsername, Collection<TicketStatus> statuses);
}
```

---

#### 11.2. [`TicketSpecifications`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/repository/TicketSpecifications.java)
* **Diagnóstico actual:** Solo cuenta con 5 especificaciones: `hasStatus`, `hasPriority`, `hasCategory`, `appliedBy`, y `assignedTo`.
* **Limitaciones:** No permite búsqueda textual por palabra clave en título/descripción, filtros de fecha (`createdAt` o `dueDate`), filtro de tickets sin asignar (`technician IS NULL`), filtro por ubicación/campus, filtro por estado de SLA, ni filtros de selección múltiple (ej. estado `IN ('OPEN', 'ASSIGNED')`).
* **Especificaciones a agregar:**

```java
package com.roles.usermanagement.modules.ticket.repository;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import com.roles.usermanagement.modules.ticket.enums.TicketCategory;
import com.roles.usermanagement.modules.ticket.enums.TicketPriority;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.Collection;

public final class TicketSpecifications {

    private TicketSpecifications(){}

    // Filtros existentes
    public static Specification<Ticket> hasStatus(TicketStatus status){
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
    public static Specification<Ticket> hasPriority(TicketPriority priority){
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }
    public static Specification<Ticket> hasCategory(TicketCategory category){
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }
    public static Specification<Ticket> appliedBy(String username) {
        return (root, query, cb) -> username == null ? null : cb.equal(root.get("applicant").get("username"), username);
    }
    public static Specification<Ticket> assignedTo(String username) {
        return (root, query, cb) -> username == null ? null : cb.equal(root.get("technician").get("username"), username);
    }

    // --- NUEVOS PREDICADOS REQUERIDOS ---

    // 1. Búsqueda por texto libre (palabras clave en título o descripción)
    public static Specification<Ticket> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    // 2. Filtro de tickets no asignados (pool general)
    public static Specification<Ticket> isUnassigned() {
        return (root, query, cb) -> cb.isNull(root.get("technician"));
    }

    // 3. Filtro por colección múltiple de estados
    public static Specification<Ticket> statusIn(Collection<TicketStatus> statuses) {
        return (root, query, cb) -> (statuses == null || statuses.isEmpty()) ? null : root.get("status").in(statuses);
    }

    // 4. Filtro por rango de fechas de creación
    public static Specification<Ticket> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            return from != null ? cb.greaterThanOrEqualTo(root.get("createdAt"), from)
                                : cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    // 5. Filtro por ubicación física (Campus / Edificio)
    public static Specification<Ticket> hasLocation(String campus, String building) {
        return (root, query, cb) -> {
            if (campus == null && building == null) return null;
            if (campus != null && building != null) {
                return cb.and(cb.equal(root.get("campus"), campus), cb.equal(root.get("building"), building));
            }
            return campus != null ? cb.equal(root.get("campus"), campus) : cb.equal(root.get("building"), building);
        };
    }

    // 6. Filtro por estado de SLA (incumplido o en riesgo)
    public static Specification<Ticket> isSlaBreached(Boolean breached) {
        return (root, query, cb) -> {
            if (breached == null) return null;
            return cb.equal(root.get("slaBreached"), breached);
        };
    }

    // 7. Filtro por etiqueta (Tag)
    public static Specification<Ticket> hasTag(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null || tagName.isBlank()) return null;
            return cb.isMember(tagName, root.get("tags"));
        };
    }
}
```

---

#### 11.3. [`CommentRepository`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/repository/CommentRepository.java)
* **Diagnóstico actual:** Solo tiene `findByTicket_IdOrderByCreatedAtAsc(Long ticketId)`.
* **Limitaciones:** En `CommentService`, los comentarios internos (`INTERNAL`) son filtrados en memoria con Java Streams (`.filter(...)`). Esto carga información privada en memoria, expone riesgo de filtrado incorrecto y no permite paginación en base de datos.
* **Métodos a agregar:**

```java
package com.roles.usermanagement.modules.comment.repository;

import com.roles.usermanagement.modules.comment.entity.Comment;
import com.roles.usermanagement.modules.comment.enums.CommentVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Método original
    List<Comment> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);

    // Consulta filtrada directamente en PostgreSQL por visibilidad (evita leaks de seguridad en memoria)
    List<Comment> findByTicket_IdAndVisibilityInOrderByCreatedAtAsc(Long ticketId, Collection<CommentVisibility> visibilities);

    // Paginación de comentarios para tickets con hilos largos
    Page<Comment> findByTicket_IdAndVisibilityInOrderByCreatedAtAsc(Long ticketId, Collection<CommentVisibility> visibilities, Pageable pageable);

    // Conteo total de comentarios por ticket
    long countByTicket_Id(Long ticketId);

    // Conteo de comentarios públicos (para badges visuales del solicitante)
    long countByTicket_IdAndVisibility(Long ticketId, CommentVisibility visibility);

    // Último comentario emitido en un ticket (para cálculo de primer tiempo de respuesta SLA)
    @Query("SELECT c FROM Comment c WHERE c.ticket.id = :ticketId ORDER BY c.createdAt DESC LIMIT 1")
    Comment findLatestByTicketId(@Param("ticketId") Long ticketId);
}
```

---

#### 11.4. [`TechnicianRepository`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/repository/TechnicianRepository.java)
* **Diagnóstico actual:** Solo cuenta con `findByAvailabilityStatusOrderByActiveTicketsCountAsc(TechnicianAvailabilityStatus status)`.
* **Limitaciones:** No tiene soporte para bloqueo pesimista en asignaciones concurrentes, no permite filtrar por especialidad ni turno, y no posee métodos para actualizar atómicamente la carga de tickets (`activeTicketsCount`).
* **Métodos a agregar:**

```java
package com.roles.usermanagement.modules.technician.repository;

import com.roles.usermanagement.modules.technician.entity.TechnicianProfile;
import com.roles.usermanagement.modules.technician.enums.TechnicianAvailabilityStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TechnicianRepository extends JpaRepository<TechnicianProfile, String> {

    // Método original
    List<TechnicianProfile> findByAvailabilityStatusOrderByActiveTicketsCountAsc(TechnicianAvailabilityStatus status);

    // Bloqueo pesimista para evitar carreras al asignar un ticket a un técnico simultáneamente
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TechnicianProfile t WHERE t.username = :username")
    Optional<TechnicianProfile> findForUpdate(@Param("username") String username);

    // Técnicos disponibles por especialidad con capacidad restante (Round-Robin / Least Busy)
    @Query("SELECT t FROM TechnicianProfile t WHERE t.availabilityStatus = 'AVAILABLE' " +
           "AND (:specialty IS NULL OR t.specialty = :specialty) " +
           "AND t.activeTicketsCount < t.maxConcurrentTickets " +
           "ORDER BY t.activeTicketsCount ASC, t.lastAssignedAt ASC NULLS FIRST")
    List<TechnicianProfile> findEligibleForAssignment(@Param("specialty") String specialty);

    // Incremento atómico del contador de tickets activos
    @Modifying
    @Query("UPDATE TechnicianProfile t SET t.activeTicketsCount = t.activeTicketsCount + 1, " +
           "t.lastAssignedAt = :now WHERE t.username = :username")
    int incrementActiveTickets(@Param("username") String username, @Param("now") LocalDateTime now);

    // Decremento atómico del contador de tickets activos (al resolver o cerrar)
    @Modifying
    @Query("UPDATE TechnicianProfile t SET t.activeTicketsCount = CASE WHEN t.activeTicketsCount > 0 THEN t.activeTicketsCount - 1 ELSE 0 END " +
           "WHERE t.username = :username")
    int decrementActiveTickets(@Param("username") String username);

    // Búsqueda de técnicos por estado de guardia / turno
    List<TechnicianProfile> findByAvailabilityStatusAndShift(TechnicianAvailabilityStatus status, String shift);
}
```

---

#### 11.5. [`TicketAssignmentLogRepository`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/assignment/repository/TicketAssignmentLogRepository.java)
* **Diagnóstico actual:** Interfaz vacía: solo hereda `JpaRepository<TicketAssignmentLog, Long>`.
* **Limitaciones:** No permite consultar el historial de asignaciones de un ticket ni auditar las transferencias realizadas por o hacia un técnico.
* **Métodos a agregar:**

```java
package com.roles.usermanagement.modules.assignment.repository;

import com.roles.usermanagement.modules.assignment.entity.TicketAssignmentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAssignmentLogRepository extends JpaRepository<TicketAssignmentLog, Long> {

    // Historial cronológico de reasignaciones de un ticket específico
    List<TicketAssignmentLog> findByTicket_IdOrderByAssignedAtDesc(Long ticketId);

    // Auditoría de asignaciones asignadas por un administrador específico
    Page<TicketAssignmentLog> findByAssignedBy_UsernameOrderByAssignedAtDesc(String adminUsername, Pageable pageable);

    // Auditoría de incidencias asignadas a un técnico
    Page<TicketAssignmentLog> findByNewTechnician_UsernameOrderByAssignedAtDesc(String technicianUsername, Pageable pageable);

    // Conteo de reasignaciones sufridas por un ticket (métrica de escalamiento / ping-pong)
    long countByTicket_Id(Long ticketId);
}
```

---

#### 11.6. [`StatusHistoryRepository`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/repository/StatusHistoryRepository.java)
* **Diagnóstico actual:** Solo cuenta con `findByTicketIdOrderByChangedAtAscIdAsc(Long ticketId)`.
* **Limitaciones:** No permite consultar la última transición para auditoría rápida, ni obtener los cambios realizados por un usuario o en un rango de fechas.
* **Métodos a agregar:**

```java
package com.roles.usermanagement.modules.statushistory.repository;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.statushistory.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    // Método original
    List<StatusHistory> findByTicketIdOrderByChangedAtAscIdAsc(Long ticketId);

    // Obtener la última transición registrada de un ticket
    Optional<StatusHistory> findTopByTicketIdOrderByChangedAtDescIdDesc(Long ticketId);

    // Obtener la primera transición a un estado determinado (ej. momento exacto en que pasó a RESOLVED o IN_PROGRESS)
    Optional<StatusHistory> findFirstByTicketIdAndNewStatusOrderByChangedAtAsc(Long ticketId, TicketStatus newStatus);

    // Auditoría de transiciones ejecutadas por un usuario/técnico en un periodo
    @Query("SELECT sh FROM StatusHistory sh WHERE sh.changedBy.username = :username AND sh.changedAt BETWEEN :start AND :end ORDER BY sh.changedAt DESC")
    List<StatusHistory> findByActorAndDateRange(@Param("username") String username,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);
}
```

---

#### 11.7. [`DashboardReportRepository`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/repository/DashboardReportRepository.java)
* **Diagnóstico actual:** 5 métodos básicos de conteo por estado y propietario.
* **Limitaciones:** No soporta agregaciones por rangos de fechas (semana/mes), no calcula tiempos promedio de resolución, ni distribuciones por categoría/prioridad.
* **Métodos a agregar:**

```java
package com.roles.usermanagement.modules.dashboard.repository;

import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DashboardReportRepository extends JpaRepository<Ticket, Long> {

    // Métodos existentes
    long countByTechnician_Username(String username);
    long countByTechnician_UsernameAndStatus(String username, TicketStatus status);
    long countByApplicant_Username(String username);
    long countByApplicant_UsernameAndStatus(String username, TicketStatus status);
    long countByStatus(TicketStatus status);

    // Distribución por categoría en periodo (GROUP BY)
    @Query("SELECT t.category AS category, COUNT(t) AS count FROM Ticket t WHERE t.createdAt >= :since GROUP BY t.category")
    List<Map<String, Object>> countTicketsGroupedByCategory(@Param("since") LocalDateTime since);

    // Distribución por prioridad en periodo
    @Query("SELECT t.priority AS priority, COUNT(t) AS count FROM Ticket t WHERE t.createdAt >= :since GROUP BY t.priority")
    List<Map<String, Object>> countTicketsGroupedByPriority(@Param("since") LocalDateTime since);

    // Carga de trabajo actual por técnico (tickets activos en progreso o asignados)
    @Query("SELECT t.technician.username AS technician, COUNT(t) AS activeCount " +
           "FROM Ticket t WHERE t.technician IS NOT NULL AND t.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "GROUP BY t.technician.username ORDER BY activeCount DESC")
    List<Map<String, Object>> countActiveTicketsPerTechnician();

    // Tiempo promedio de resolución (en horas) para tickets cerrados en un periodo
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600.0) " +
                   "FROM tickets WHERE status = 'CLOSED' AND updated_at >= :since", nativeQuery = true)
    Double calculateAverageResolutionTimeHours(@Param("since") LocalDateTime since);

    // Cumplimiento porcentual de SLA
    @Query("SELECT " +
           "SUM(CASE WHEN t.slaBreached = false THEN 1.0 ELSE 0.0 END) * 100.0 / NULLIF(COUNT(t), 0) " +
           "FROM Ticket t WHERE t.status IN ('RESOLVED', 'CLOSED') AND t.updatedAt >= :since")
    Double calculateSlaCompliancePercentage(@Param("since") LocalDateTime since);
}
```

---

#### 11.8. [`LifecycleRuleRepository`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/repository/LifecycleRuleRepository.java)
* **Diagnóstico actual:** Solo cuenta con `findByCurrentStatusAndAction(TicketStatus currentStatus, TransitionAction action)`.
* **Limitaciones:** No permite consultar todas las acciones permitidas desde un estado específico (indispensable para que la UI renderice únicamente los botones de acción válidos).
* **Métodos a agregar:**

```java
package com.roles.usermanagement.modules.lifecycle.repository;

import com.roles.usermanagement.modules.lifecycle.entity.LifecycleRule;
import com.roles.usermanagement.modules.lifecycle.enums.TicketStatus;
import com.roles.usermanagement.modules.lifecycle.enums.TransitionAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LifecycleRuleRepository extends JpaRepository<LifecycleRule, Long> {

    // Método original
    Optional<LifecycleRule> findByCurrentStatusAndAction(TicketStatus currentStatus, TransitionAction action);

    // Obtener todas las transiciones posibles desde un estado actual (para poblar dinámicamente botones en Frontend)
    List<LifecycleRule> findByCurrentStatus(TicketStatus currentStatus);

    // Listar reglas activas filtradas por rol permitido
    List<LifecycleRule> findByCurrentStatusAndRequiredRole(TicketStatus currentStatus, String requiredRole);
}
```

---

#### 11.9. [`UserCrudRepository`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/crud/UserCrudRepository.java)
* **Diagnóstico actual:** Contiene `findForUpdate`, `existsByEmailAndUsernameNot`, `existsByEmail`, `findByEmail`.
* **Limitaciones:** No permite listar usuarios por rol (ej. listar todos los técnicos o todos los solicitantes) ni realizar búsquedas por nombre o estado de bloqueo.
* **Métodos a agregar:**

```java
// Búsqueda de usuarios asociados a un rol específico (e.g. 'TECHNICIAN' o 'ADMIN')
@Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.role = :roleName AND u.disabled = false")
List<UserEntity> findActiveUsersByRole(@Param("roleName") String roleName);

// Búsqueda autocompletable por prefijo de username o correo
@Query("SELECT u FROM UserEntity u WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%')) " +
       "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :term, '%'))) AND u.disabled = false")
List<UserEntity> searchActiveUsers(@Param("term") String term);
```

---

### 12. NUEVOS REPOSITORIOS JPA REQUERIDOS (Para las 22 Nuevas Entidades)

A continuación se define la estructura de repositorios para cada uno de los 14 nuevos módulos identificados en la Sección 9:

| # | Módulo | Entidad | Nombre del Repositorio JPA | Paquete Canónico de Ubicación |
|:---:|:---|:---|:---|:---|
| 1 | `sla` | `SlaPolicy` | `SlaPolicyRepository` | `com.roles.usermanagement.modules.sla.repository` |
| 2 | `sla` | `TicketSlaTracking` | `TicketSlaTrackingRepository` | `com.roles.usermanagement.modules.sla.repository` |
| 3 | `notification` | `Notification` | `NotificationRepository` | `com.roles.usermanagement.modules.notification.repository` |
| 4 | `attachment` | `Attachment` | `AttachmentRepository` | `com.roles.usermanagement.modules.attachment.repository` |
| 5 | `knowledgebase` | `KnowledgeArticle` | `KnowledgeArticleRepository` | `com.roles.usermanagement.modules.knowledgebase.repository` |
| 6 | `knowledgebase` | `KnowledgeCategory` | `KnowledgeCategoryRepository` | `com.roles.usermanagement.modules.knowledgebase.repository` |
| 7 | `satisfaction` | `SatisfactionSurvey` | `SatisfactionSurveyRepository` | `com.roles.usermanagement.modules.satisfaction.repository` |
| 8 | `satisfaction` | `SurveyTemplate` | `SurveyTemplateRepository` | `com.roles.usermanagement.modules.satisfaction.repository` |
| 9 | `announcement` | `Announcement` | `AnnouncementRepository` | `com.roles.usermanagement.modules.announcement.repository` |
| 10 | `tag` | `Tag` | `TagRepository` | `com.roles.usermanagement.modules.tag.repository` |
| 11 | `tag` | `TicketTag` | `TicketTagRepository` | `com.roles.usermanagement.modules.tag.repository` |
| 12 | `timetracking` | `TimeEntry` | `TimeTrackingRepository` | `com.roles.usermanagement.modules.timetracking.repository` |
| 13 | `cannedresponse` | `CannedResponse` | `CannedResponseRepository` | `com.roles.usermanagement.modules.cannedresponse.repository` |
| 14 | `audit` | `AuditLog` | `AuditLogRepository` | `com.roles.usermanagement.modules.audit.repository` |
| 15 | `asset` | `Asset` | `AssetRepository` | `com.roles.usermanagement.modules.asset.repository` |
| 16 | `servicecatalog` | `ServiceItem` | `ServiceCatalogRepository` | `com.roles.usermanagement.modules.servicecatalog.repository` |

#### Firmas detalladas de repositorios clave (Ubicados en sus módulos correspondientes):

```java
// 1. com.roles.usermanagement.modules.sla.repository.SlaPolicyRepository
package com.roles.usermanagement.modules.sla.repository;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    Optional<SlaPolicy> findByPriorityAndCategory(TicketPriority priority, TicketCategory category);
    Optional<SlaPolicy> findByPriorityAndCategoryIsNull(TicketPriority priority); // Default fallback
}

// 2. com.roles.usermanagement.modules.sla.repository.TicketSlaTrackingRepository
package com.roles.usermanagement.modules.sla.repository;

public interface TicketSlaTrackingRepository extends JpaRepository<TicketSlaTracking, Long> {
    Optional<TicketSlaTracking> findByTicketId(Long ticketId);
    List<TicketSlaTracking> findByIsBreachedFalseAndResolutionDueAtBefore(LocalDateTime time);
}

// 3. com.roles.usermanagement.modules.notification.repository.NotificationRepository
package com.roles.usermanagement.modules.notification.repository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    long countByRecipientUsernameAndIsReadFalse(String username);
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUsername = :username")
    int markAllAsRead(@Param("username") String username);
}

// 4. com.roles.usermanagement.modules.attachment.repository.AttachmentRepository
package com.roles.usermanagement.modules.attachment.repository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTicketId(Long ticketId);
    List<Attachment> findByCommentId(Long commentId);
    @Query("SELECT COALESCE(SUM(a.fileSizeBytes), 0) FROM Attachment a WHERE a.ticket.id = :ticketId")
    long calculateTotalAttachmentSizeBytes(@Param("ticketId") Long ticketId);
}

// 5. com.roles.usermanagement.modules.knowledgebase.repository.KnowledgeArticleRepository
package com.roles.usermanagement.modules.knowledgebase.repository;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    Page<KnowledgeArticle> findByPublishedTrueOrderByViewCountDesc(Pageable pageable);
    @Query("SELECT a FROM KnowledgeArticle a WHERE a.published = true AND " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<KnowledgeArticle> searchArticles(@Param("query") String query, Pageable pageable);
}

// 6. com.roles.usermanagement.modules.satisfaction.repository.SatisfactionSurveyRepository
package com.roles.usermanagement.modules.satisfaction.repository;

public interface SatisfactionSurveyRepository extends JpaRepository<SatisfactionSurvey, Long> {
    Optional<SatisfactionSurvey> findByTicketId(Long ticketId);
    @Query("SELECT AVG(s.rating) FROM SatisfactionSurvey s WHERE s.createdAt >= :since")
    Double calculateAverageRating(@Param("since") LocalDateTime since);
}

// 7. com.roles.usermanagement.modules.announcement.repository.AnnouncementRepository
package com.roles.usermanagement.modules.announcement.repository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    @Query("SELECT a FROM Announcement a WHERE a.active = true AND " +
           "(a.expiresAt IS NULL OR a.expiresAt > :now) ORDER BY a.createdAt DESC")
    List<Announcement> findActiveAnnouncements(@Param("now") LocalDateTime now);
}

// 8. com.roles.usermanagement.modules.cannedresponse.repository.CannedResponseRepository
package com.roles.usermanagement.modules.cannedresponse.repository;

public interface CannedResponseRepository extends JpaRepository<CannedResponse, Long> {
    List<CannedResponse> findByCreatedByOrIsGlobalTrue(String username);
    List<CannedResponse> findByCategoryAndIsGlobalTrue(TicketCategory category);
}

// 9. com.roles.usermanagement.modules.timetracking.repository.TimeTrackingRepository
package com.roles.usermanagement.modules.timetracking.repository;

public interface TimeTrackingRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByTicketIdOrderByLoggedAtDesc(Long ticketId);
    @Query("SELECT COALESCE(SUM(t.minutesSpent), 0) FROM TimeEntry t WHERE t.ticket.id = :ticketId")
    int calculateTotalMinutesSpent(@Param("ticketId") Long ticketId);
}
```

---

### 13. CORRECCIONES Y MODIFICACIONES EN LA CAPA DE MAPPERS

#### 13.1. ⚠️ Corrección de Bug Crítico en [`CommentMapper`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/mapper/CommentMapper.java)
Durante la inspección del código fuente se detectó una **inversión de argumentos** en el constructor de `CommentResponse`:
* **Definición del Record:** `CommentResponse(Long id, Long ticketId, String content, String authorUsername, CommentVisibility visibility, LocalDateTime createdAt)`.
* **Código actual en `CommentMapper.java`:**
  ```java
  return new CommentResponse(
      com.getId(),
      com.getTicket().getId(),
      com.getAuthor().getUsername(), // <- ERROR: Argumento 3 es asignado al parámetro content!
      com.getContent(),              // <- ERROR: Argumento 4 es asignado al parámetro authorUsername!
      com.getVisibility(),
      com.getCreatedAt()
  );
  ```
* **Consecuencia:** En la respuesta JSON, el campo `content` recibe el nombre de usuario y el campo `authorUsername` recibe el texto del mensaje.
* **Corrección requerida:**
  ```java
  return new CommentResponse(
      com.getId(),
      com.getTicket().getId(),
      com.getContent(),              // Parámetro 3: content
      com.getAuthor().getUsername(), // Parámetro 4: authorUsername
      com.getVisibility(),
      com.getCreatedAt()
  );
  ```

#### 13.2. Actualizaciones en [`TicketMapper`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/mapper/TicketMapper.java)
* Integrar los nuevos campos ampliados del Ticket: `campus`, `building`, `room`, `slaDueAt`, `slaBreached`, `reopenCount`, `closedAt`, `satisfactionRating` y lista de `tags`.
* Generar métodos de mapeo hacia `TicketSummaryResponse` (versión liviana para tablas) y `TicketDetailResponse` (versión completa con comentarios, adjuntos e historial).

#### 13.3. Actualizaciones en [`TechnicianMapper`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/mapper/TechnicianMapper.java)
* Mapear campos nuevos en `TechnicianResponse`: `maxConcurrentTickets`, `shift`, `campusAssigned`, porcentaje de carga actual (`(activeTicketsCount / maxConcurrentTickets) * 100`).

#### 13.4. Actualizaciones en [`DashboardMapper`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/mapper/DashboardMapper.java)
* Adaptar el DTO para incluir métricas de cumplimiento de SLA, tiempo medio de respuesta (MTTR) y gráficos de distribución por categoría.

---

### 14. MODIFICACIONES EN LA CAPA DE SERVICIOS (Service Layer)

#### 14.1. Modificaciones a [`TicketService`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/service/TicketService.java)
1. **Migración a Paginación:** Cambiar `list(...)` de `List<TicketResponse>` a `Page<TicketResponse>` aceptando `Pageable`.
2. **Acceso al Pool de Tickets sin Asignar por parte de Técnicos:** Actualmente, la política obliga a `TicketSpecifications.assignedTo(auth.getName())` para técnicos, impidiéndoles ver tickets abiertos sin asignar. Debe modificarse para permitir `assignedTo(username) OR (isUnassigned() AND status == OPEN)`.
3. **Nuevos Métodos de Flujo de Negocio:**
   * `cancelTicket(Long ticketId, String reason, Authentication auth)` (Funcionalidad U-04).
   * `reopenTicket(Long ticketId, String reason, Authentication auth)` (Funcionalidad U-05, valida plazo de gracia de 7 días y máximo 3 reaperturas).
   * `confirmClose(Long ticketId, Authentication auth)` (Funcionalidad U-06).
   * `executeBatchStatusUpdate(List<Long> ticketIds, TicketStatus status, Authentication auth)` (Funcionalidad A-14).
   * `exportTickets(TicketFilterRequest filters, String format, Authentication auth)` (Funcionalidades U-19, A-11).

#### 14.2. Modificaciones a [`TicketAssignmentService`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/assignment/service/TicketAssignmentService.java)
1. **Sincronización de Carga de Técnicos:**
   * Al asignar un ticket: invocar `technicianRepository.incrementActiveTickets(technicianUsername, LocalDateTime.now())`.
   * Al reasignar: decrementar la carga del `previousTechnician` e incrementar la del `newTechnician`.
   * Al cerrar/resolver el ticket: decrementar la carga del técnico responsable.
2. **Autoasignación por Técnicos:** Permitir que un usuario con rol `TECHNICIAN` se autoasigne tickets del pool abierto si no supera su capacidad máxima (`maxConcurrentTickets`).
3. **Asignación Automática Inteligente (Round-Robin / Least Loaded):**
   * Implementar `autoAssign(Long ticketId)`: busca técnicos en `TechnicianRepository.findEligibleForAssignment(ticket.getCategory().name())` y selecciona al técnico disponible con menor número de incidencias activas.

#### 14.3. Modificaciones a [`CommentService`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/service/CommentService.java)
* Reemplazar la consulta general más filtrado en memoria (`.filter(...)`) por la consulta JPA optimizada `findByTicket_IdAndVisibilityInOrderByCreatedAtAsc`.
* Integrar soporte de archivos adjuntos en comentarios (vincular `Attachment` al persistir el comentario).

#### 14.4. Modificaciones a [`TicketLifecycleService`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/service/TicketLifecycleService.java)
* Ampliar el switch de `toHistoryAction(TransitionAction action)` para mapear las 10 nuevas acciones (`CANCEL`, `REOPEN`, `PUT_ON_HOLD`, `RESUME`, `ESCALATE`, `REQUEST_INFO`, `APPROVE`, `REJECT`, `AUTO_CLOSE`, `MERGE`).
* Desencadenar eventos de dominio (`ApplicationEventPublisher`) tras cada transición exitosa.

---

### 15. ARQUITECTURA DE EVENTOS Y TAREAS PROGRAMADAS (Events & Schedulers)

Para evitar acoplamientos rígidos entre módulos y mantener intacta la arquitectura en capas y por módulos del proyecto, se establece:
* **Eventos de Dominio:** Se ubican en `com.roles.usermanagement.domain.event` y se desacoplan mediante Spring `ApplicationEventPublisher` y `@EventListener`.
* **Servicios de Ejecución Programada (`@Scheduled`):** Residen en la capa `service` del módulo responsable (`com.roles.usermanagement.modules.<modulo>.service`).
* **Habilitación de Tareas Programadas (`@EnableScheduling`):** Se centraliza en la configuración web existente `com.roles.usermanagement.web.config.SchedulingConfig`.

#### 15.1. Catálogo de Eventos de Dominio (`com.roles.usermanagement.domain.event`)
| Evento (Record en `domain.event`) | Publicado Por | Consumido Por (Listeners en `service`) |
|:---|:---|:---|
| `TicketCreatedEvent` | `TicketService.create` | `NotificationService` (aviso a admins), `SlaService` (inicia reloj SLA), `AuditService` |
| `TicketAssignedEvent` | `TicketAssignmentService` | `NotificationService` (aviso al técnico asignado), `SlaService`, `AuditService` |
| `TicketStatusChangedEvent` | `TicketLifecycleService` | `NotificationService` (aviso al usuario y técnico), `SlaService`, `DashboardMetricsCache` |
| `CommentAddedEvent` | `CommentService.addComment` | `NotificationService` (aviso a la contraparte), `SlaService` (registra primera respuesta) |
| `SlaBreachedEvent` | `SlaMonitoringSchedulerService` | `NotificationService` (alerta de escalamiento a ADMIN), `AuditService` |
| `TicketResolvedEvent` | `TicketLifecycleService` | `SurveyService` (envía encuesta CSAT), `TechnicianService` (libera capacidad) |

#### 15.2. Tareas Programadas en Background (`@Scheduled`)

```java
// 1. Configuración de scheduling: com.roles.usermanagement.web.config.SchedulingConfig
package com.roles.usermanagement.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
}

// 2. Scheduler de SLA: com.roles.usermanagement.modules.sla.service.SlaMonitoringSchedulerService
package com.roles.usermanagement.modules.sla.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SlaMonitoringSchedulerService {

    private final SlaService slaService;

    public SlaMonitoringSchedulerService(SlaService slaService) {
        this.slaService = slaService;
    }

    // Verificación periódica de vencimiento de SLAs (cada 2 minutos)
    @Scheduled(fixedRate = 120000)
    public void monitorSlaDeadlines() {
        slaService.checkAndEscalateBreachedTickets();
    }
}

// 3. Scheduler de Auto-cierre: com.roles.usermanagement.modules.ticket.service.TicketAutoCloseSchedulerService
package com.roles.usermanagement.modules.ticket.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TicketAutoCloseSchedulerService {

    private final TicketService ticketService;

    public TicketAutoCloseSchedulerService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Auto-cierre de incidencias resueltas inactivas tras N días (diario a las 02:00 AM)
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCloseResolvedTickets() {
        ticketService.autoCloseInactiveResolvedTickets();
    }
}
```

---

### 16. ESPECIFICACIÓN DE NUEVOS ENDPOINTS REST (Controllers)

Para dar cumplimiento a las 90 funcionalidades sin depender de autenticación/login, se requieren los siguientes controladores y rutas:

#### 16.1. Endpoints para el Solicitante (`USER`)
| Método | Ruta | Autorización | Propósito | Funcionalidad |
|:---|:---|:---|:---|:---:|
| `POST` | `/api/tickets/{id}/cancel` | `hasRole('USER')` | Cancelar ticket propio en estado abierto | U-04 |
| `POST` | `/api/tickets/{id}/reopen` | `hasRole('USER')` | Reabrir ticket resuelto dentro del plazo de gracia | U-05 |
| `POST` | `/api/tickets/{id}/confirm-close` | `hasRole('USER')` | Confirmar solución y cerrar ticket | U-06 |
| `POST` | `/api/tickets/{id}/satisfaction` | `hasRole('USER')` | Calificar atención recibida (1-5 estrellas + comentario) | U-06 |
| `POST` | `/api/tickets/{id}/attachments` | `hasRole('USER')` | Subir capturas o archivos adjuntos | U-02 |
| `GET` | `/api/tickets/{id}/attachments/{attId}` | `isAuthenticated()` | Descargar archivo adjunto con control de acceso | U-02, T-11 |
| `GET` | `/api/kb/articles` | `isAuthenticated()` | Consultar base de conocimiento con búsqueda | U-08, U-09 |
| `GET` | `/api/notifications` | `isAuthenticated()` | Consultar bandeja de notificaciones in-app | U-10 |
| `PATCH` | `/api/notifications/{id}/read` | `isAuthenticated()` | Marcar notificación como leída | U-10 |
| `GET` | `/api/announcements/active` | `isAuthenticated()` | Ver comunicados activos y avisos de mantenimiento | U-16 |
| `GET` | `/api/tickets/export/pdf` | `hasRole('USER')` | Exportar comprobante de radicación en PDF | U-19 |

#### 16.2. Endpoints para el Técnico (`TECHNICIAN`)
| Método | Ruta | Autorización | Propósito | Funcionalidad |
|:---|:---|:---|:---|:---:|
| `GET` | `/api/tickets/unassigned` | `hasAnyRole('TECHNICIAN','ADMIN')` | Consultar cola de tickets sin asignar disponibles | T-31 |
| `POST` | `/api/tickets/{id}/claim` | `hasRole('TECHNICIAN')` | Reclamar/autoasignarse un ticket del pool general | T-31 |
| `PATCH` | `/api/technicians/me/availability`| `hasRole('TECHNICIAN')` | Cambiar estado propio (AVAILABLE, BUSY, ON_BREAK, OFF_DUTY) | T-01 |
| `POST` | `/api/tickets/{id}/time-entries` | `hasRole('TECHNICIAN')` | Registrar tiempo invertido en la atención | T-02 |
| `GET` | `/api/canned-responses` | `hasAnyRole('TECHNICIAN','ADMIN')` | Listar plantillas de respuestas predefinidas | T-04 |
| `POST` | `/api/canned-responses` | `hasAnyRole('TECHNICIAN','ADMIN')` | Crear nueva respuesta rápida personal | T-04 |
| `POST` | `/api/tickets/{id}/escalate` | `hasRole('TECHNICIAN')` | Solicitar escalamiento a nivel superior | T-06 |
| `GET` | `/api/technicians/me/performance` | `hasRole('TECHNICIAN')` | Ver métricas personales (MTTR, resolución en plazo) | T-28 |

#### 16.3. Endpoints para el Administrador (`ADMIN`)
| Método | Ruta | Autorización | Propósito | Funcionalidad |
|:---|:---|:---|:---|:---:|
| `POST` | `/api/tickets/batch/status` | `hasRole('ADMIN')` | Cambio masivo de estado de múltiples tickets | A-14 |
| `POST` | `/api/tickets/batch/assign` | `hasRole('ADMIN')` | Asignación masiva a un técnico | A-14 |
| `GET` | `/api/sla/policies` | `hasRole('ADMIN')` | Consultar catálogo de políticas de SLA | A-01 |
| `PUT` | `/api/sla/policies/{id}` | `hasRole('ADMIN')` | Modificar umbrales y tiempos de respuesta SLA | A-01 |
| `POST` | `/api/announcements` | `hasRole('ADMIN')` | Publicar comunicado global de mantenimiento | A-05 |
| `GET` | `/api/audit/logs` | `hasRole('ADMIN')` | Consultar log inmutable de auditoría | A-18 |
| `GET` | `/api/reports/sla-compliance` | `hasRole('ADMIN')` | Generar reporte de cumplimiento de SLA | A-07 |
| `GET` | `/api/reports/technician-workload` | `hasRole('ADMIN')` | Reporte detallado de productividad técnica | A-09 |
| `GET` | `/api/reports/export/excel` | `hasRole('ADMIN')` | Exportación tabular completa de incidencias | A-11 |
| `POST` | `/api/tags` | `hasRole('ADMIN')` | Crear o administrar etiquetas del sistema | A-23 |

---

### 17. ACTUALIZACIONES A SEGURIDAD Y FILTROS WEB ([`SecurityConfig`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/config/SecurityConfig.java))

* **Limpieza de rutas huérfanas:** En `SecurityConfig.java`, eliminar las rutas comentadas de la plantilla original (`/api/pizzas/**`, `/api/orders/**`).
* **Protección granular de endpoints:**
  ```java
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
      .requestMatchers("/api/auth/**").permitAll()
      .requestMatchers(HttpMethod.GET, "/api/announcements/active").permitAll()
      // Tickets y ciclo de vida
      .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("USER")
      .requestMatchers("/api/tickets/unassigned", "/api/tickets/*/claim").hasAnyRole("TECHNICIAN", "ADMIN")
      .requestMatchers("/api/tickets/*/assign", "/api/tickets/*/reassign", "/api/tickets/batch/**").hasRole("ADMIN")
      // Módulos administrativos
      .requestMatchers("/api/sla/**", "/api/audit/**", "/api/reports/**").hasRole("ADMIN")
      // Técnicos
      .requestMatchers("/api/technicians/me/**", "/api/canned-responses/**").hasAnyRole("TECHNICIAN", "ADMIN")
      // Todo lo demás autenticado
      .anyRequest().authenticated()
  )
  ```

---

### 18. MANEJO GLOBAL DE EXCEPCIONES ([`ApiExceptionHandler`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/controller/ApiExceptionHandler.java))

Actualmente solo maneja 4 excepciones: `ResponseStatusException`, `DataIntegrityViolationException`, `MethodArgumentNotValidException` y `HttpMessageNotReadableException`.

Para que el proyecto soporte de manera robusta los nuevos flujos, deben añadirse los siguientes manejadores en `ApiExceptionHandler.java`:

```java
// 1. Errores de Autorización (403 Forbidden) con payload JSON uniforme
@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
public ResponseEntity<Map<String, Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.FORBIDDEN.value());
    body.put("error", "Acceso denegado");
    body.put("message", "No posee los privilegios requeridos para ejecutar esta acción");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
}

// 2. Errores de tamaño de archivo adjunto excedido (413 Payload Too Large)
@ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
public ResponseEntity<Map<String, Object>> handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
    body.put("error", "Archivo demasiado grande");
    body.put("message", "El archivo adjunto supera el límite máximo permitido (10MB)");
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
}

// 3. Conflicto de Concurrencia Pesimista / Bloqueo (409 Conflict)
@ExceptionHandler(jakarta.persistence.PessimisticLockException.class)
public ResponseEntity<Map<String, Object>> handlePessimisticLock(jakarta.persistence.PessimisticLockException e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("error", "Conflicto de concurrencia");
    body.put("message", "El recurso está siendo modificado por otra transacción. Intente nuevamente en unos instantes");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
}

// 4. Manejador genérico para fallos no controlados (500 Internal Server Error)
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Error interno del servidor");
    body.put("message", "Ha ocurrido un error inesperado al procesar la solicitud");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
}
```

---

### 19. ESTÁNDAR DE PAGINACIÓN, ORDENAMIENTO Y DTOs DE RESPUESTA

Para evitar sobrecargar la memoria y la red con listas completas de entidades en bases de datos reales:
1. **Contrato de Paginación Universal:** Reemplazar el retorno de colecciones directas por una estructura estándar `PageResponse<T>`:

```java
package com.roles.usermanagement.domain.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isFirst,
    boolean isLast
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
```

2. **Parámetros por defecto:** Configurar en `PageableDefault`: tamaño de página por defecto = 20, ordenamiento por defecto = `createdAt,DESC`.

---

### 20. ÍNDICES DE BASE DE DATOS, CONSTRAINTS Y RENDIMIENTO (PostgreSQL)

Para garantizar un rendimiento inferior a 100 ms en consultas con miles de incidencias, se deben agregar los siguientes índices en PostgreSQL:

#### Índices en Tablas Existentes:
1. **Tabla `tickets`:**
   * `CREATE INDEX idx_tickets_applicant_status ON tickets(applicant_username, status);` (Acelera listados del usuario).
   * `CREATE INDEX idx_tickets_technician_status ON tickets(technician_username, status);` (Acelera colas del técnico).
   * `CREATE INDEX idx_tickets_sla_breach ON tickets(status, sla_due_at) WHERE status NOT IN ('RESOLVED', 'CLOSED');` (Índice parcial para el scheduler de SLA).
   * `CREATE INDEX idx_tickets_created_at ON tickets(created_at DESC);` (Para ordenamiento cronológico global).
2. **Tabla `comments`:**
   * `CREATE INDEX idx_comments_ticket_visibility ON comments(ticket_id, visibility, created_at ASC);` (Acelera carga del hilo de conversación respetando la privacidad).
3. **Tabla `status_history`:**
   * `CREATE INDEX idx_status_history_ticket_changed ON status_history(ticket_id, changed_at ASC);`
4. **Tabla `technician_profiles`:**
   * `CREATE INDEX idx_technician_avail_load ON technician_profiles(availability_status, active_tickets_count ASC);` (Acelera asignación automática Round-Robin).

#### Constraints Clave:
* `ALTER TABLE technician_profiles ADD CONSTRAINT chk_active_tickets_positive CHECK (active_tickets_count >= 0);`
* `ALTER TABLE tickets ADD CONSTRAINT chk_ticket_title_not_empty CHECK (LENGTH(TRIM(title)) > 0);`
* `ALTER TABLE comments ADD CONSTRAINT chk_comment_content_not_empty CHECK (LENGTH(TRIM(content)) > 0);`

---

### 21. RESUMEN GLOBAL CONSOLIDADO DE COMPONENTES A IMPLEMENTAR

| Capa del Proyecto | Estado Actual | Elementos a Modificar | Elementos Nuevos a Crear | Total Final |
|:---|:---:|:---:|:---:|:---:|
| **Entidades JPA** | 11 | 3 (`Ticket`, `Comment`, `TechnicianProfile`) | 22 nuevas entidades | 33 entidades |
| **Repositorios JPA** | 10 | 9 repositorios ampliados con queries y locks | 16 nuevos repositorios | 26 repositorios |
| **Especificaciones JPA** | 1 clase (5 predicados) | 1 clase ampliada | 7 nuevos predicados de filtro | 12 predicados |
| **Mappers** | 6 componentes | 4 modificados + 1 corrección de bug | 14 nuevos mappers de módulo | 20 mappers |
| **Servicios de Negocio** | 7 servicios | 5 servicios ampliados | 14 nuevos servicios | 21 servicios |
| **Eventos de Dominio** | 0 | 0 | 6 clases de evento + Listeners | 6 eventos |
| **Schedulers / Cron** | 0 | 0 | 3 tareas periódicas programadas | 3 schedulers |
| **Controladores REST** | 7 | 4 ampliados | 12 nuevos controladores | 19 controladores |
| **Endpoints REST** | ~18 | 3 actualizados | 29 nuevos endpoints | 47 endpoints |
| **Manejadores de Excepción** | 4 | 0 | 4 nuevos `@ExceptionHandler` | 8 manejadores |
| **Índices de Base de Datos** | 3 | 0 | 8 nuevos índices compuestos y parciales | 11 índices |

---

### 22. 🏛️ MAPA DE ARQUITECTURA Y UBICACIÓN EXACTA DE ARCHIVOS (BLUEPRINT PARA EL PLAN DE IMPLEMENTACIÓN)

Para garantizar de forma irrestricta que la arquitectura del proyecto se mantenga **completamente intacta** durante la elaboración y ejecución del Plan de Implementación, se establece el siguiente mapa canónico de directorios y reglas de posicionamiento de archivos en `src/main/java/com/roles/usermanagement/`:

```text
src/main/java/com/roles/usermanagement/
│
├── UsermanagementApplication.java                 # Punto de entrada Spring Boot
│
├── domain/                                        # CAPA TRANSVERSAL DE DOMINIO Y USUARIOS
│   ├── dto/                                       # DTOs de autenticación, usuarios y compartidos
│   │   ├── UserDto.java
│   │   ├── RegisterDto.java
│   │   ├── UserProfileUpdateRequest.java          # [U-15] Extensión perfil de usuario
│   │   ├── PageResponse.java                      # [Estándar] DTO universal de paginación
│   │   └── ... (DTOs de catálogo y seguridad)
│   ├── event/                                     # [NUEVO] EVENTOS DE DOMINIO SPRING
│   │   ├── TicketCreatedEvent.java
│   │   ├── TicketAssignedEvent.java
│   │   ├── TicketStatusChangedEvent.java
│   │   ├── CommentAddedEvent.java
│   │   ├── SlaBreachedEvent.java
│   │   └── TicketResolvedEvent.java
│   ├── repository/                                # Interfaces de repositorio del dominio
│   │   └── IUserRepository.java
│   └── service/                                   # Servicios de dominio y catálogo de roles
│       ├── UserService.java                       # [Modificado: incluye updateProfile U-15]
│       ├── UserRoles.java                         # [Modificado: incluye 43 nuevas Authorities]
│       ├── SecurityCatalogService.java
│       └── UserSecurityService.java
│
├── persistance/                                   # CAPA DE PERSISTENCIA CENTRAL (con 'a')
│   ├── crud/                                      # Interfaces Spring Data CrudRepository
│   │   ├── UserCrudRepository.java                # [Modificado: queries de búsqueda de usuarios]
│   │   ├── RoleCrudRepository.java
│   │   ├── PermissionCrudRepository.java
│   │   └── UserRoleCrudRepository.java
│   ├── entity/                                    # Entidades base de autenticación y usuario
│   │   ├── UserEntity.java                        # [Modificado: fullName, phone, department, notificationsEnabled]
│   │   ├── RoleEntity.java
│   │   ├── PermissionEntity.java
│   │   ├── UserRoleEntity.java
│   │   └── UserRoleId.java
│   ├── mapper/                                    # Mappers MapStruct de usuario
│   │   ├── UserMapper.java
│   │   └── UserRoleMapper.java
│   └── repository/                                # Implementación de repositorios de dominio
│       ├── UserRepository.java                    # [Modificado: soporte a updateProfile]
│       └── UserRoleRepository.java
│
├── web/                                           # CAPA WEB Y CONFIGURACIÓN CENTRAL
│   ├── config/                                    # Configuraciones Spring Security y Web
│   │   ├── SecurityConfig.java                    # [Modificado: limpieza de rutas legacy y permisos]
│   │   ├── CorsConfig.java
│   │   ├── SchedulingConfig.java                  # [NUEVO: @EnableScheduling para tareas en background]
│   │   ├── OpenApiConfig.java
│   │   ├── JwtFilter.java
│   │   └── JwtUtil.java
│   └── controller/                                # Controladores web globales y manejo de errores
│       ├── UserController.java                    # [Modificado: PUT /api/user/profile U-15]
│       ├── AuthController.java                    # (Intacto, sin tocar en esta etapa)
│       ├── SecurityCatalogController.java
│       └── ApiExceptionHandler.java               # [Modificado: 4 nuevos manejadores de excepción]
│
└── modules/                                       # MÓDULOS DE NEGOCIO (VERTICAL SLICES)
    │
    │   # === 7 MÓDULOS EXISTENTES (Estructura Intacta) ===
    ├── assignment/                                # Módulo de asignaciones
    │   ├── controller/ (TicketAssignmentController.java)
    │   ├── dto/        (TicketAssignRequest.java, TicketReassignRequest.java, TicketAssignmentResponse.java)
    │   ├── entity/     (TicketAssignmentLog.java)
    │   ├── enums/      (AssignmentType.java)
    │   ├── mapper/     (TicketAssignmentMapper.java)
    │   ├── repository/ (TicketAssignmentLogRepository.java)
    │   └── service/    (TicketAssignmentService.java)
    │
    ├── comment/                                   # Módulo de comentarios
    │   ├── controller/ (CommentController.java)
    │   ├── dto/        (CommentCreateRequest.java, CommentResponse.java)
    │   ├── entity/     (Comment.java)
    │   ├── enums/      (CommentVisibility.java)
    │   ├── mapper/     (CommentMapper.java [Bug Fix args])
    │   ├── repository/ (CommentRepository.java)
    │   └── service/    (CommentService.java)
    │
    ├── dashboard/                                 # Módulo de dashboard y métricas
    │   ├── controller/ (DashboardController.java)
    │   ├── dto/        (DashboardMetricsResponse.java, MetricFilterRequest.java)
    │   ├── entity/     (DashboardCacheMetric.java)
    │   ├── enums/      (MetricPeriod.java)
    │   ├── mapper/     (DashboardMapper.java)
    │   ├── model/      (DashboardMetrics.java)
    │   ├── repository/ (DashboardReportRepository.java)
    │   └── service/    (DashboardService.java)
    │
    ├── lifecycle/                                 # Módulo de ciclo de vida de tickets
    │   ├── controller/ (TicketLifecycleController.java)
    │   ├── dto/        (StateTransitionRequest.java, StateTransitionResponse.java)
    │   ├── entity/     (LifecycleRule.java)
    │   ├── enums/      (TicketStatus.java, TransitionAction.java)
    │   ├── repository/ (LifecycleRuleRepository.java)
    │   └── service/    (TicketLifecycleService.java)
    │
    ├── statushistory/                             # Módulo de historial inmutable de estados
    │   ├── controller/ (StatusHistoryController.java)
    │   ├── dto/        (StatusHistoryResponse.java)
    │   ├── entity/     (StatusHistory.java)
    │   ├── enums/      (StatusHistoryAction.java)
    │   ├── mapper/     (StatusHistoryMapper.java)
    │   ├── repository/ (StatusHistoryRepository.java)
    │   └── service/    (StatusHistoryService.java)
    │
    ├── technician/                                # Módulo de perfiles técnicos
    │   ├── controller/ (TechnicianController.java)
    │   ├── dto/        (TechnicianResponse.java, TechnicianAvailabilityResponse.java)
    │   ├── entity/     (TechnicianProfile.java)
    │   ├── enums/      (TechnicianAvailabilityStatus.java)
    │   ├── mapper/     (TechnicianMapper.java)
    │   ├── repository/ (TechnicianRepository.java)
    │   └── service/    (TechnicianService.java)
    │
    ├── ticket/                                    # Módulo principal de tickets
    │   ├── controller/ (TicketController.java)
    │   ├── dto/        (TicketCreateRequest.java, TicketResponse.java, TicketUpdateRequest.java)
    │   ├── entity/     (Ticket.java)
    │   ├── enums/      (TicketCategory.java, TicketPriority.java)
    │   ├── mapper/     (TicketMapper.java)
    │   ├── repository/ (TicketRepository.java, TicketSpecifications.java)
    │   └── service/    (TicketService.java, TicketAccessPolicy.java, TicketAutoCloseSchedulerService.java)
    │
    │   # === 13 NUEVOS MÓDULOS DE NEGOCIO (Patrón Idéntico de 7 Capas) ===
    ├── sla/                                       # Gestión de Acuerdos de Nivel de Servicio
    │   ├── controller/ (SlaPolicyController.java)
    │   ├── dto/        (SlaPolicyRequest.java, SlaPolicyResponse.java, SlaTrackingResponse.java)
    │   ├── entity/     (SlaPolicy.java, TicketSlaTracking.java, EscalationRule.java, BusinessHours.java)
    │   ├── enums/      (SlaStatus.java, EscalationTrigger.java)
    │   ├── mapper/     (SlaMapper.java)
    │   ├── repository/ (SlaPolicyRepository.java, TicketSlaTrackingRepository.java)
    │   └── service/    (SlaService.java, SlaMonitoringSchedulerService.java)
    │
    ├── notification/                              # Notificaciones in-app y correo
    │   ├── controller/ (NotificationController.java)
    │   ├── dto/        (NotificationResponse.java, NotificationPreferenceRequest.java)
    │   ├── entity/     (Notification.java, NotificationTemplate.java, NotificationPreference.java)
    │   ├── enums/      (NotificationEventType.java, NotificationChannel.java, NotificationStatus.java)
    │   ├── mapper/     (NotificationMapper.java)
    │   ├── repository/ (NotificationRepository.java)
    │   └── service/    (NotificationService.java, NotificationEventListener.java)
    │
    ├── attachment/                                # Archivos adjuntos y almacenamiento
    │   ├── controller/ (AttachmentController.java)
    │   ├── dto/        (AttachmentResponse.java)
    │   ├── entity/     (FileAttachment.java)
    │   ├── enums/      (FileAttachmentContext.java)
    │   ├── mapper/     (AttachmentMapper.java)
    │   ├── repository/ (AttachmentRepository.java)
    │   └── service/    (AttachmentStorageService.java)
    │
    ├── knowledgebase/                             # Base de conocimiento (FAQ / Artículos)
    │   ├── controller/ (KnowledgeBaseController.java)
    │   ├── dto/        (ArticleCreateRequest.java, ArticleResponse.java, ArticleSearchRequest.java)
    │   ├── entity/     (KnowledgeArticle.java, KnowledgeCategory.java)
    │   ├── enums/      (ArticleStatus.java)
    │   ├── mapper/     (KnowledgeBaseMapper.java)
    │   ├── repository/ (KnowledgeArticleRepository.java, KnowledgeCategoryRepository.java)
    │   └── service/    (KnowledgeBaseService.java)
    │
    ├── satisfaction/                              # Encuestas de satisfacción CSAT
    │   ├── controller/ (SatisfactionSurveyController.java)
    │   ├── dto/        (SatisfactionSurveyRequest.java, SatisfactionSurveyResponse.java)
    │   ├── entity/     (SatisfactionSurvey.java, SatisfactionConfig.java)
    │   ├── enums/      (SatisfactionRating.java)
    │   ├── mapper/     (SatisfactionMapper.java)
    │   ├── repository/ (SatisfactionSurveyRepository.java)
    │   └── service/    (SatisfactionService.java)
    │
    ├── announcement/                              # Comunicados globales y avisos de campus
    │   ├── controller/ (AnnouncementController.java)
    │   ├── dto/        (AnnouncementCreateRequest.java, AnnouncementResponse.java)
    │   ├── entity/     (Announcement.java)
    │   ├── enums/      (AnnouncementStatus.java)
    │   ├── mapper/     (AnnouncementMapper.java)
    │   ├── repository/ (AnnouncementRepository.java)
    │   └── service/    (AnnouncementService.java)
    │
    ├── tag/                                       # Catálogo y asignación de etiquetas
    │   ├── controller/ (TagController.java)
    │   ├── dto/        (TagRequest.java, TagResponse.java)
    │   ├── entity/     (Tag.java, TicketTag.java)
    │   ├── enums/      (TagScope.java)
    │   ├── mapper/     (TagMapper.java)
    │   ├── repository/ (TagRepository.java, TicketTagRepository.java)
    │   └── service/    (TagService.java)
    │
    ├── timetracking/                              # Registro de tiempos invertidos por técnicos
    │   ├── controller/ (TimeTrackingController.java)
    │   ├── dto/        (TimeEntryRequest.java, TimeEntryResponse.java)
    │   ├── entity/     (TimeEntry.java)
    │   ├── enums/      (TimeEntryType.java)
    │   ├── mapper/     (TimeTrackingMapper.java)
    │   ├── repository/ (TimeTrackingRepository.java)
    │   └── service/    (TimeTrackingService.java)
    │
    ├── cannedresponse/                            # Plantillas y respuestas predefinidas
    │   ├── controller/ (CannedResponseController.java)
    │   ├── dto/        (CannedResponseRequest.java, CannedResponseResponse.java)
    │   ├── entity/     (CannedResponse.java)
    │   ├── enums/      (CannedResponseScope.java)
    │   ├── mapper/     (CannedResponseMapper.java)
    │   ├── repository/ (CannedResponseRepository.java)
    │   └── service/    (CannedResponseService.java)
    │
    ├── audit/                                     # Log inmutable de auditoría de seguridad
    │   ├── controller/ (AuditLogController.java)
    │   ├── dto/        (AuditLogResponse.java, AuditLogFilterRequest.java)
    │   ├── entity/     (AuditLog.java)
    │   ├── enums/      (AuditActionType.java)
    │   ├── mapper/     (AuditLogMapper.java)
    │   ├── repository/ (AuditLogRepository.java)
    │   └── service/    (AuditLogService.java)
    │
    ├── asset/                                     # Inventario de activos y equipos IT
    │   ├── controller/ (AssetController.java)
    │   ├── dto/        (AssetRequest.java, AssetResponse.java)
    │   ├── entity/     (AssetItem.java)
    │   ├── enums/      (AssetStatus.java, AssetType.java)
    │   ├── mapper/     (AssetMapper.java)
    │   ├── repository/ (AssetRepository.java)
    │   └── service/    (AssetService.java)
    │
    ├── servicecatalog/                            # Catálogo de servicios estandarizados
    │   ├── controller/ (ServiceCatalogController.java)
    │   ├── dto/        (ServiceItemRequest.java, ServiceItemResponse.java)
    │   ├── entity/     (ServiceCatalogItem.java)
    │   ├── enums/      (ServiceCategory.java)
    │   ├── mapper/     (ServiceCatalogMapper.java)
    │   ├── repository/ (ServiceCatalogRepository.java)
    │   └── service/    (ServiceCatalogService.java)
    │
    └── report/                                    # Generación y exportación de reportes
        ├── controller/ (ReportController.java)
        ├── dto/        (ReportFilterRequest.java, ReportExportResponse.java)
        ├── entity/     (ReportSchedule.java)
        ├── enums/      (ExportFormat.java, ReportScheduleFrequency.java)
        ├── mapper/     (ReportMapper.java)
        ├── repository/ (ReportScheduleRepository.java)
        └── service/    (ReportService.java)
```

#### Reglas de Oro de Arquitectura para el Plan de Implementación:
1. **Sin Paquetes Foráneos:** Queda estrictamente prohibido crear carpetas en la raíz de `com.roles.usermanagement` distintas a las 4 canónicas (`domain`, `persistance`, `web`, `modules`). No se crearán paquetes como `common`, `shared` ni `config` en la raíz.
2. **Homogeneidad de Módulos:** Todo nuevo módulo de negocio debe ubicarse obligatoriamente bajo `com.roles.usermanagement.modules.<nombre>` y contar con sus 7 subcarpetas canónicas (`controller`, `dto`, `entity`, `enums`, `mapper`, `repository`, `service`).
3. **Respeto a la Ortografía del Proyecto:** El paquete de persistencia central conserva su ortografía original `persistance` (con 'a').
4. **Preservación del Core de Usuario:** Las extensiones a datos del usuario (teléfono, departamento, preferencias de notificación) deben incorporarse en `persistance.entity.UserEntity`, `domain.service.UserService` y `web.controller.UserController`, sin crear módulos desconectados.
5. **Alineación de Tareas Programadas:** Los métodos `@Scheduled` deben residir en la capa `service` del módulo responsable (ej. `SlaMonitoringSchedulerService` en `modules.sla.service`), y la anotación `@EnableScheduling` se ubicará en `web.config.SchedulingConfig`.
6. **DTOs Compartidos:** Cualquier DTO de uso transversal entre múltiples capas o módulos (como `PageResponse<T>`) pertenece canónicamente a `com.roles.usermanagement.domain.dto`.


