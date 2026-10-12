# 03 · Contrato de API (Frontend ↔ Backend) — PROVISIONAL

| Campo | Valor |
|---|---|
| Versión | 0.1.0 |
| Fecha | 2026-10-08 |
| Fuente | `CAMPUSDESK_BACKEND.md` (propuesta del equipo backend) + `CAMPUSDESK_SPRINGBOOT.md` (enunciado) |
| Estado | ⚠️ **Provisional hasta el Swagger final.** Los endpoints pueden variar |
| Regla | Solo se registra aquí lo que **el backend documentó**. Lo no documentado se marca ❓, **no se infiere** |

Leyenda de columna *Estado*: ✅ documentado por backend · ❓ falta definir · 🔄 cambió respecto al Swagger (se anota en el registro de cambios).

---

## 1. Convenciones generales

| Aspecto | Valor | Estado |
|---|---|---|
| URL base / versionado | — | ❓ `Q-08` |
| Autenticación | JWT con expiración (RT-05); cabecera esperada `Authorization: Bearer <token>` | ❓ formato exacto `Q-04` |
| Formato de datos | JSON | ✅ (RT-03: "estructura JSON consistente") |
| Formato de error | JSON limpio vía `ApiExceptionHandler`, sin trazas internas | ✅ existe · ❓ campos `Q-07` |
| Formato de fechas / zona horaria | — | ❓ `Q-18` |
| Paginación | Existe en `GET /api/tickets`; forma de la respuesta y parámetros | ❓ `Q-14` |
| CORS | Solo orígenes necesarios (RT-05) | ❓ origen del front `Q-08` |

## 2. Endpoints

| Módulo | Método | Ruta | Roles (según backend) | Usado en | Estado |
|---|---|---|---|---|---|
| auth | POST | *login* | Público | S-01 | ❓ `Q-04` |
| auth | — | *registro* | Público | (descartado por Sebbs) | ❓ `Q-05` |
| users | — | *crear/listar/editar usuarios y permisos* | SUPERADMIN ❓ / ADMIN (consultar) | S-07 | ❓ `Q-03` |
| ticket | GET | `/api/tickets` (filtros `category`, `priority`, `status` + paginación) | Todos (alcance filtrado por rol) | S-03, S-06 | ✅ |
| ticket | GET | `/api/tickets/{id}` | Dueño / técnico asignado / ADMIN | S-05 | ✅ |
| ticket | POST | `/api/tickets` | Solicitante autenticado | S-04 | ✅ (quién: `Q-25`) |
| ticket | PUT | `/api/tickets/{id}` | Solicitante, solo ABIERTA y sin asignar | S-04, S-05 | ✅ |
| lifecycle | PATCH | `/api/tickets/{id}/status` | Según transición (técnico asignado / solicitante) | S-05 | ✅ |
| assignment | PATCH | `/api/tickets/{id}/assign` | Solo ADMIN | S-05, S-06 | ✅ |
| comment | GET | `/api/tickets/{id}/comments` | Dueño / técnico asignado / ADMIN | S-05 | ✅ |
| comment | POST | `/api/tickets/{id}/comments` | Dueño / técnico asignado / ADMIN (no en CERRADA) | S-05 | ✅ |
| statushistory | GET | `/api/tickets/{id}/history` | Según visibilidad del tiquete | S-05 | ✅ |
| dashboard | GET | `/api/reports/summary` | Todos (alcance por rol) | S-02 | ✅ (params `Q-23`) |
| technician | GET | `/api/users/technicians` | ADMIN (selector de asignación) | S-06 | ✅ |

## 3. DTOs conocidos

> Nombres de campo **tal cual** aparecen en `CAMPUSDESK_BACKEND.md`. "—" = el backend no los especificó.

### Ticket
| DTO | Campos documentados | Faltantes |
|---|---|---|
| `TicketCreateRequest` | Valida con `@NotBlank`, `@Size`, `@NotNull` | Lista de campos y límites ❓ `Q-16` (la entidad tiene `title, description, category, priority`; **no** se asume que sean los del request) |
| `TicketUpdateRequest` | "Campos editables por el solicitante original" | Campos ❓ `Q-16` |
| `TicketFilterRequest` | `category`, `priority`, `status` | Paginación/orden ❓ `Q-14` |
| `TicketSummaryResponse` | "DTO compacto para tablas y listas" | Campos ❓ `Q-18` |
| `TicketDetailResponse` | "Información completa de solicitante, técnico, fechas y estado" | Campos ❓ `Q-18` |
| Entidad `Ticket` (referencia) | `id, title, description, category, priority, status, applicant_id, technician_id, created_at, updated_at` | — |

### Lifecycle / Assignment
| DTO | Campos documentados | Faltantes |
|---|---|---|
| `StateTransitionRequest` | `targetStatus` + comentarios de justificación | Nombre del campo de comentario y si es obligatorio ❓ `Q-21` |
| `StateTransitionResponse` | Estado anterior, nuevo estado, timestamp | Nombres exactos ❓ |
| `TicketAssignRequest` | `technicianId` | — |
| `TicketReassignRequest` | `newTechnicianId` + justificación | Cómo distingue el backend asignación vs reasignación en un mismo `PATCH` ❓ `Q-20` |
| `TicketAssignmentResponse` | Datos del técnico y nuevo estado | Nombres exactos ❓ |

### Comment / History
| DTO | Campos documentados | Faltantes |
|---|---|---|
| `CommentCreateRequest` | `content` (`@NotBlank`) | Longitud máxima ❓ `Q-22` |
| `CommentResponse` | "ID, autor, fecha de publicación, rol del autor y mensaje" | Nombres exactos ❓ |
| `StatusHistoryResponse` | `id, ticketId, previousStatus, newStatus, changedByUsername, changedAt` | — |
| Enum `CommentVisibility` | `PUBLIC`, `INTERNAL` | Si la UI lo elige/filtra ❓ `Q-22` |

### Dashboard / Technician
| DTO | Campos documentados | Faltantes |
|---|---|---|
| `DashboardMetricsResponse` | `totalTickets, openTickets, inProgressTickets, resolvedTickets, closedTickets` | Contador ASIGNADA; tendencia/series ❓ `Q-23`, `Q-38` |
| `MetricFilterRequest` / `MetricPeriod` | `TODAY, WEEK, MONTH, TOTAL` ("filtro temporal o por departamento") | Parámetros y alcance ❓ `Q-23` |
| `TechnicianResponse` | `id, fullName, email, activeTicketsAssigned` | — |
| `TechnicianAvailabilityResponse` | "Estatus operativo y disponibilidad de carga" | Endpoint propio ❓ `Q-24` |

## 4. Enumeraciones y etiquetas de UI

Los **valores** son del backend (✅). Las **etiquetas** son propuesta 🔶 (copy definitivo: `Q-30`) y se centralizan en `messages.es.js`.

| Enum | Valores (API) | Etiqueta UI 🔶 |
|---|---|---|
| `TicketStatus` | `ABIERTA` · `ASIGNADA` · `EN_PROCESO` · `RESUELTA` · `CERRADA` | Abierta · Asignada · En proceso · Resuelta · Cerrada |
| `TicketPriority` | `BAJA` · `MEDIA` · `ALTA` · `CRITICA` | Baja · Media · Alta · Crítica |
| `TicketCategory` | `HARDWARE` · `SOFTWARE` · `REDES` · `ACCESOS` · `OTROS` | Hardware · Software · Redes · Accesos · Otros |
| `TransitionAction` | `START_PROGRESS` · `RESOLVE` · `CONFIRM_CLOSE` | Iniciar atención · Marcar como resuelta · Confirmar cierre |
| `AssignmentType` | `INITIAL` · `REASSIGNMENT` | Asignación inicial · Reasignación |
| `TechnicianAvailabilityStatus` | `AVAILABLE` · `BUSY` · `OFFLINE` | Disponible · Ocupado · Desconectado |
| `MetricPeriod` | `TODAY` · `WEEK` · `MONTH` · `TOTAL` | Hoy · Semana · Mes · Total |
| `StatusHistoryAction` | `STATUS_CHANGE` · `AUTO_INITIALIZED` | Cambio de estado · Creación |
| `CommentVisibility` | `PUBLIC` · `INTERNAL` | Pública · Interna |

Roles de seguridad: `ADMIN`, `TECHNICIAN`, `USER` ✅ · `SUPERADMIN` ❓ `Q-01`.

## 5. Códigos HTTP documentados por backend (✅)

| Código | Cuándo | Tratamiento en UI |
|---|---|---|
| 400 | Campos vacíos, email inválido, asignar a usuario sin rol técnico | Ver `01-spec.md` §7 |
| 401 | Credenciales incorrectas; token ausente/expirado/adulterado | Ver §7 |
| 403 | Acceder o alterar tiquetes de otro colaborador | Ver §7 |
| 404 | ID de tiquete inexistente | Ver §7 |
| 409 | Transición inválida; comentar tiquete CERRADA | Ver §7 |
| 500 | Error imprevisto con mensaje genérico | Ver §7 |

## 6. Matriz Pantalla → Endpoints

| Pantalla | Endpoints |
|---|---|
| S-01 Login | login ❓ |
| S-02 Dashboard | `GET /api/reports/summary` |
| S-03 Listado | `GET /api/tickets` |
| S-04 Formulario | `POST /api/tickets` · `PUT /api/tickets/{id}` |
| S-05 Detalle | `GET /api/tickets/{id}` · `…/comments` (GET, POST) · `…/history` · `PATCH …/status` · `PATCH …/assign` |
| S-06 Administración | `GET /api/tickets` · `GET /api/users/technicians` · `PATCH …/assign` |
| S-07 Usuarios | endpoints ❓ `Q-03` |

## 7. Protocolo al recibir el Swagger final

1. Guardar copia del Swagger/OpenAPI en `docs/spec/` (o su enlace + fecha).
2. Recorrer **§2 y §3** fila por fila: marcar ✅/🔄 y completar los ❓.
3. Resolver las `Q-xx` de backend que queden cubiertas (registrar en `05`).
4. Actualizar `endpoints.js`, mappers y fixtures de `mocks/` (nunca los servicios).
5. Probar módulo por módulo con `API_MODE='real'` y anotar diferencias.
6. Registrar el cambio abajo y subir la versión.

## 8. Registro de cambios del contrato

| Versión | Fecha | Cambio | Impacto en el front |
|---|---|---|---|
| 0.1.0 | 2026-10-08 | Versión inicial desde `CAMPUSDESK_BACKEND.md` | — |
