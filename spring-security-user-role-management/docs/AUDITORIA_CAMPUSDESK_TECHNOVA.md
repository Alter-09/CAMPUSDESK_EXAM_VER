# INFORME DE AUDITORÍA INTEGRAL DE PROYECTO: CAMPUSDESK (RE-AUDITORÍA V2)
**Cliente ficticio:** TechNova Solutions  
**Plataforma auditada:** CAMPUSDESK (Backend Spring Boot + Frontend Vanilla Web)  
**Fecha de auditoría:** Octubre 2026 (Versión 2 - Post Sincronización de Contratos y Corrección de Tests)  
**Alcance:** Backend, Frontend, Persistencia PostgreSQL, Seguridad Spring Security + JWT, API REST, Manejo de Errores, Documentación Swagger y Casos de Prueba.

---

## 1. RESUMEN EJECUTIVO Y CALIFICACIÓN GLOBAL ACTUALIZADA

Tras las correcciones técnicas aplicadas en el backend (alineación de endpoints del contrato RT-03, soporte completo de enums en español, normalización de respuestas de error JSON y reconstrucción de la suite de pruebas unitarias/integración), se ha re-evaluado el proyecto en su totalidad.

### Calificación Cualitativa General: **85 / 100 (Notable / Aprobado Alto)**
*(Incremento significativo desde la evaluación inicial de 72/100)*

| Dimensión | Ponderación | Estado Inicial | Estado Actual | Veredicto |
|---|:---:|:---:|:---:|:---:|
| **Arquitectura Backend & Clean Code (RT-01, RT-02)** | 20% | 18% | **20%** | 🟢 **100% Cumplido** |
| **Seguridad, Roles y JWT (RF-01, RF-02, RT-05)** | 20% | 16% | **18%** | 🟢 **Sólido** |
| **Ciclo de Vida, Reglas de Negocio & Tickets (RF-03 a RF-06)** | 20% | 17% | **20%** | 🟢 **100% Cumplido** |
| **Contrato REST & Manejo de Errores (RT-03, RT-04)** | 15% | 10% | **15%** | 🟢 **100% Cumplido** |
| **Pruebas Automatizadas, DB & Empaquetado (CP-01..16, RT-02)** | 10% | 7% | **8%** | 🟢 **Tests en Verde (18/18)** |
| **Frontend Web & Consumo de API (RT-06, CP-15)** | 15% | 4% | **4%** | 🔴 **Pendiente Conexión Dinámica** |
| **TOTAL** | **100%** | **72 / 100** | **85 / 100** | **Aprobado con Alta Calidad en Backend** |

---

## 2. ESTADO ACTUAL DE LOS 16 CASOS DE PRUEBA OBLIGATORIOS (CP-01 a CP-16)

| Código | Escenario Obligatorio | Resultado Esperado | Comprobación Técnica en Ejecución | Dictamen |
|:---:|---|---|---|:---:|
| **CP-01** | Registrar un usuario válido | Cuenta creada con rol USER | Valida en `POST /api/auth/register`, retorna 201 y asigna `USER`. | 🟢 **APROBADO** |
| **CP-02** | Registrar un correo duplicado | Rechazo de la operación | Lanza `ResponseStatusException(409 CONFLICT)`. | 🟢 **APROBADO** |
| **CP-03** | Iniciar sesión correctamente | Se recibe un JWT válido | Retorna token firmado HMAC256 con tiempo de expiración. | 🟢 **APROBADO** |
| **CP-04** | Acceder a endpoint protegido sin token | HTTP 401 | Interceptado por Spring Security. | 🟢 **APROBADO** |
| **CP-05** | Crear un ticket | Registrado en estado ABIERTA | Nace en estado `OPEN` (ABIERTA) asociado al solicitante. | 🟢 **APROBADO** |
| **CP-06** | Consultar ticket ajeno como USER | HTTP 403 o 404 | Rechazado con `403 FORBIDDEN` por `TicketAccessPolicy`. | 🟢 **APROBADO** |
| **CP-07** | Asignar un técnico como ADMIN | Pasa a ASIGNADA | Endpoint `PATCH /api/tickets/{id}/assign` cambia estado a `ASSIGNED`. | 🟢 **APROBADO** |
| **CP-08** | Intentar asignar como USER | HTTP 403 | Rechazado con `403 FORBIDDEN` vía `@PreAuthorize("hasRole('ADMIN')")`. | 🟢 **APROBADO** |
| **CP-09** | Modificar ticket ajeno como técnico | Operación rechazada | Rechazado con `403 FORBIDDEN` por no ser el solicitante. | 🟢 **APROBADO** |
| **CP-10** | Resolver ticket sin pasar por EN_PROCESO | HTTP 409 | Rechazado con `409 CONFLICT` por la máquina de reglas. | 🟢 **APROBADO** |
| **CP-11** | Registrar un cambio de estado | Registro histórico creado | Transaccionalmente guardado en entidad `StatusHistory`. | 🟢 **APROBADO** |
| **CP-12** | Cerrar ticket RESUELTA como solicitante | Estado CERRADA | Pasa a estado `CLOSED` (CERRADA) por el solicitante. | 🟢 **APROBADO** |
| **CP-13** | Comentar un ticket CERRADA | Operación rechazada | Rechazado con `400 BAD_REQUEST` al detectar ticket cerrado. | 🟢 **APROBADO** |
| **CP-14** | Consultar estadísticas | Coinciden con PostgreSQL | Conteo exacto en `GET /api/reports/summary` y `/dashboard/stats`. | 🟢 **APROBADO** |
| **CP-15** | Consumir API desde frontend autorizado | Comunicación exitosa | Parcial: Login conecta, pero tickets en UI aún no consumen API. | 🔴 **PENDIENTE EN UI** |
| **CP-16** | Utilizar un JWT inválido o expirado | HTTP 401 | Limpia contexto de seguridad y rechaza solicitud. | 🟢 **APROBADO** |

---

## 3. COMPARATIVA Y ESTADO DEL CONTRATO DE API REST (RT-03)

Todos los endpoints oficiales exigidos en el documento ahora se encuentran operativos:

| Endpoint Mínimo Requerido (RT-03) | Método | Implementación en Backend | Estado |
|---|:---:|---|:---:|
| `/api/auth/register` | `POST` | `AuthController.register()` | 🟢 **100% Operativo** |
| `/api/auth/login` | `POST` | `AuthController.login()` | 🟢 **100% Operativo** |
| `/api/users/technicians` | `GET` | `TechnicianController.listAll()` (responde en `/api/users/technicians` y `/api/technicians`) | 🟢 **100% Operativo** |
| `/api/tickets` | `GET` | `TicketController.list()` (con filtros de estado, prioridad y categoría) | 🟢 **100% Operativo** |
| `/api/tickets/{id}` | `GET` | `TicketController.getById()` | 🟢 **100% Operativo** |
| `/api/tickets` | `POST` | `TicketController.create()` | 🟢 **100% Operativo** |
| `/api/tickets/{id}` | `PUT` | `TicketController.update()` | 🟢 **100% Operativo** |
| `/api/tickets/{id}/assign` | `PATCH` | `TicketAssignmentController.assign()` (admite `PATCH` y `PUT`) | 🟢 **100% Operativo** |
| `/api/tickets/{id}/status` | `PATCH` | `TicketLifecycleController.transition()` (admite `PATCH` y `POST` con `status` o `action`) | 🟢 **100% Operativo** |
| `/api/tickets/{id}/comments` | `GET` | `CommentController.getCommentsByTicket()` | 🟢 **100% Operativo** |
| `/api/tickets/{id}/comments` | `POST` | `CommentController.createComment()` | 🟢 **100% Operativo** |
| `/api/tickets/{id}/history` | `GET` | `StatusHistoryController.getHistory()` | 🟢 **100% Operativo** |
| `/api/reports/summary` | `GET` | `DashboardController.getDashboardStats()` (responde en `/api/reports/summary` y `/api/dashboard/stats`) | 🟢 **100% Operativo** |

---

## 4. ESTADO DE PRUEBAS AUTOMATIZADAS Y COMPILACIÓN (GRADLE)

* **Compilación de código fuente:**
  `.\gradlew.bat compileJava` → 🟢 **BUILD SUCCESSFUL (0 warnings, 0 errores)**
* **Ejecución de suite de tests:**
  `.\gradlew.bat test` → 🟢 **BUILD SUCCESSFUL (18 pruebas ejecutadas, 0 fallidas)**
* **Empaquetado JAR de producción:**
  `.\gradlew.bat bootJar` → 🟢 **BUILD SUCCESSFUL (usermanagement-0.0.1-SNAPSHOT.jar generado)**

---

## 5. DIAGNÓSTICO RESTANTE PARA ALCANZAR EL 100/100

Para lograr la máxima nota en la sustentación final ante el evaluador/trainer, resta abordar los siguientes puntos en el Frontend y en la entrega:

1. **Conexión Dinámica de Tickets en Frontend (`ticketlist.html`):**
   - Actualmente las filas de tickets en `ticketlist.html` están estáticas en el HTML.
   - **Acción requerida:** Crear un script Vanilla JS (`ticket-list.js`) que al cargar la página realice `fetch('http://localhost:8051/api/tickets', { headers: { 'Authorization': 'Bearer ' + token } })` e inyecte dinámicamente las filas de tickets de PostgreSQL.

2. **Formulario de Registro en Frontend (`index.html`):**
   - La pantalla de login `index.html` necesita la opción visual de registrarse consumiendo `POST /api/auth/register`.

3. **Restringir Orígenes CORS (`CorsConfig.java`):**
   - Cambiar `allowedOrigins("*")` para especificar los orígenes exactos del frontend (e.g. `http://localhost:5500`, `http://127.0.0.1:5500`, `http://localhost:3000`).

4. **Script SQL de Datos de Prueba (Entrega):**
   - Crear un archivo `schema-and-seed.sql` con la definición DDL de PostgreSQL y datos de prueba precargados (técnicos, usuarios y tickets de ejemplo) para cumplir el punto 3 de la entrega (página 12).
