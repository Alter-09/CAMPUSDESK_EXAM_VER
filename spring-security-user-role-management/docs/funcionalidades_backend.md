# 📖 Catálogo Maestro de Funcionalidades del Backend — CampusDesk (Spring Boot & Spring Security)

---

## 🏛️ 1. Arquitectura Técnica y Flujo de Ejecución del Backend

El backend de **CampusDesk** está desarrollado sobre el ecosistema **Spring Boot 3.x** con **Java 17+** y **Spring Security 6**, implementando una arquitectura en capas desacoplada orientada al dominio (*Domain-Driven Design* simplificado) con persistencia relacional mediante **Spring Data JPA** / **Hibernate**.

```text
                                        [ CLIENTE HTTP / FRONTEND ]
                                                     │ (Bearer JWT / JSON)
                                                     ▼
                                      ┌───────────────────────────────┐
                                      │   SecurityFilterChain         │
                                      │   (CorsConfig & JwtFilter)    │
                                      └──────────────┬────────────────┘
                                                     │ Valida firma, tiempo y Subject
                                                     ▼
                                      ┌───────────────────────────────┐
                                      │   UserSecurityService         │
                                      │   (Roles + Permisos Dinámicos)│
                                      └──────────────┬────────────────┘
                                                     │ Establece SecurityContextHolder
                                                     ▼
                                      ┌───────────────────────────────┐
                                      │   @RestController             │
                                      │   (@PreAuthorize / Validación)│
                                      └──────────────┬────────────────┘
                                                     │ DTOs (Request / Records)
                                                     ▼
                                      ┌───────────────────────────────┐
                                      │   @Service (Lógica de Negocio)│
                                      │   (@Transactional / Políticas)│
                                      └──────┬──────────────────┬─────┘
                                             │                  │
                   Disparo de Eventos/SLA    │                  │ Mapeo con DTOs
                                             ▼                  ▼
                                ┌─────────────────┐    ┌─────────────────┐
                                │ Events / SLA /  │    │ MapStruct /     │
                                │ StatusHistory   │    │ Custom Mappers  │
                                └─────────────────┘    └─────────────────┘
                                             │                  │
                                             └────────┬─────────┘
                                                      │ Entidades JPA
                                                      ▼
                                      ┌───────────────────────────────┐
                                      │   JpaRepository / CrudRepo    │
                                      │   (Locks pesimistas / Specs)  │
                                      └──────────────┬────────────────┘
                                                     │ SQL Dialect
                                                     ▼
                                      ┌───────────────────────────────┐
                                      │   Base de Datos Relacional    │
                                      │   (PostgreSQL / MySQL / H2)   │
                                      └───────────────────────────────┘
```

### 📋 Orden Lógico de Capas por Funcionalidad
Cada funcionalidad en el backend sigue una secuencia estructurada y predecible:
1. **Entidad (`@Entity`) & Enumeraciones (`enum`)**: Representación tabular relacional en JPA con anotaciones de validación y auditoría (`@Id`, `@Column`, `@ManyToOne`, `@Enumerated`, `@CreationTimestamp`).
2. **Repositorio (`@Repository`)**: Interfaz que extiende de `JpaRepository` o `CrudRepository`, con consultas derivadas, consultas nativas/JPQL (`@Query`), soporte de `JpaSpecificationExecutor` para filtros dinámicos, y bloqueos de concurrencia pesimistas (`LockModeType.PESSIMISTIC_WRITE`).
3. **DTOs (Data Transfer Objects / Records)**: Clases inmutables o `record` de Java con anotaciones Jakarta Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Positive`) que definen el contrato exacto de entrada y salida desacoplado de la base de datos.
4. **Mapper (`*Mapper`)**: Componente encargado de la transformación bidireccional segura entre Entidades JPA y DTOs públicos, evitando la exposición de contraseñas o detalles internos.
5. **Servicio (`@Service`)**: Orquestador de la lógica de negocio gobernado por demarcación transaccional (`@Transactional`), control de políticas de acceso (`TicketAccessPolicy`), cálculo de plazos SLA, emisión de eventos de aplicación (`ApplicationEventPublisher`), y auditoría inmutable.
6. **Controlador (`@RestController`)**: Punto de entrada HTTP expuesto bajo una ruta base (`@RequestMapping`), mapeando peticiones (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`), parseando parámetros y retornando códigos de estado HTTP semánticos (200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict).
7. **Seguridad & Autorización (`Spring Security`)**: Validación perimetral mediante `SecurityConfig`, filtro `JwtFilter`, y seguridad a nivel de método con `@PreAuthorize("hasRole(...)")` o `@PreAuthorize("hasAuthority(...)")`.

---

# PARTE I: MÓDULO DE SEGURIDAD Y CONTROL DE ACCESO (SPRING SECURITY)

Este módulo abarca la autenticación basada en JWT, la gestión de usuarios, la administración jerárquica de roles y la asignación granular de permisos individuales dinámicos.

---

### 1.1 Autenticación y Sesión

#### SEC-01: Registro de Usuarios
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite el auto-registro público de solicitantes con asignación por defecto del rol `USER`. Valida el formato del correo institucional y la robustez de la contraseña. |
| **API Endpoint** | `POST /api/auth/register`<br>• **Headers:** `Content-Type: application/json`<br>• **Códigos HTTP:** `201 Created`, `400 Bad Request`, `409 Conflict` |
| **Roles & Permisos** | **Público** (`permitAll()` en `SecurityConfig`). No requiere token. |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`UserEntity.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/entity/UserEntity.java)<br>2. **Repositorio:** [`UserCrudRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/crud/UserCrudRepository.java) & [`UserRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/repository/UserRepository.java)<br>3. **DTOs:** [`RegisterDto.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/dto/RegisterDto.java) & [`UserDto.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/dto/UserDto.java)<br>4. **Mapper:** [`UserMapper.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/mapper/UserMapper.java)<br>5. **Servicio:** [`UserService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/service/UserService.java)<br>6. **Controlador:** [`AuthController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/controller/AuthController.java)<br>7. **Seguridad:** [`SecurityConfig.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/config/SecurityConfig.java) (`/api/auth/**` en `permitAll()`) |
| **Payload Entrada (Request)** | ```json
{
  "username": "sergio.aparicio",
  "email": "sergio@campusdesk.edu.co",
  "password": "Password123!"
}
``` |
| **Payload Salida (Response)** | ```json
{
  "message": "Usuario registrado exitosamente con rol USER",
  "username": "sergio.aparicio",
  "email": "sergio@campusdesk.edu.co",
  "role": "USER"
}
``` |
| **Reglas de Negocio & Validaciones** | • Contraseña obligatoria con mínimo 6 caracteres, al menos 1 letra mayúscula y 1 carácter especial (`^(?=.*[A-Z])(?=.*[^a-zA-Z0-9\s]).{6,}$`).<br>• Validación de sintaxis de correo electrónico con regex.<br>• Verificación de unicidad de `username` y `email` en la base de datos.<br>• Encriptación unidireccional de contraseña mediante `BCryptPasswordEncoder`.<br>• Asignación automática de rol `USER` y estado inicial no bloqueado (`locked = false`). |

---

#### SEC-02: Inicio de Sesión y Autenticación JWT
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Autentica a los usuarios mediante nombre de usuario o dirección de correo electrónico junto con su contraseña. Emite un token JWT firmado y reporta si el usuario debe cambiar su contraseña obligatoriamente. |
| **API Endpoint** | `POST /api/auth/login`<br>• **Headers:** `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `401 Unauthorized` |
| **Roles & Permisos** | **Público** (`permitAll()`). |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`UserEntity.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/entity/UserEntity.java)<br>2. **Repositorio:** [`UserCrudRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/crud/UserCrudRepository.java)<br>3. **DTOs:** [`LoginDto.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/dto/LoginDto.java) & [`LoginResponseDto.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/dto/LoginResponseDto.java)<br>4. **Servicio:** [`UserSecurityService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/service/UserSecurityService.java)<br>5. **Seguridad / Utilidad JWT:** [`JwtUtil.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/config/JwtUtil.java)<br>6. **Controlador:** [`AuthController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/controller/AuthController.java) |
| **Payload Entrada (Request)** | ```json
{
  "identifier": "sergio.aparicio",
  "password": "Password123!"
}
``` *(identifier soporta tanto username como correo electrónico)* |
| **Payload Salida (Response)** | ```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzZXJnaW8uYXBh...",
  "username": "sergio.aparicio",
  "mustChangePassword": false
}
``` |
| **Reglas de Negocio & Validaciones** | • Delega la validación de credenciales en `AuthenticationManager.authenticate(...)`.<br>• `UserSecurityService` resuelve el usuario por `username` o por `email`.<br>• Si la cuenta está bloqueada (`locked = true`) o inactiva, deniega el acceso con 401.<br>• Genera token JWT firmado con HMAC-SHA256 con fecha de emisión y expiración (según `JwtProperties`).<br>• Inspecciona el indicador `mustChangePassword`: si es `true`, el cliente web redirige al flujo de cambio de clave forzado. |

---

#### SEC-03: Cambio de Contraseña Voluntario o Primer Ingreso
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite al usuario autenticado cambiar su contraseña actual por una nueva. Desactiva la bandera `mustChangePassword` si estaba activa tras un restablecimiento administrativo. |
| **API Endpoint** | `POST /api/auth/change-password`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `401 Unauthorized` |
| **Roles & Permisos** | **Autenticado** (`@PreAuthorize("isAuthenticated()")`). |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`UserEntity.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/entity/UserEntity.java)<br>2. **Repositorio:** [`UserRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/repository/UserRepository.java)<br>3. **DTO:** [`ChangePasswordDto.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/dto/ChangePasswordDto.java)<br>4. **Servicio:** [`UserService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/service/UserService.java)<br>5. **Controlador:** [`AuthController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/controller/AuthController.java) |
| **Payload Entrada (Request)** | ```json
{
  "currentPassword": "Temporal123!",
  "newPassword": "MiNuevaPasswordSegura2026!"
}
``` |
| **Payload Salida (Response)** | ```json
{
  "message": "Contraseña actualizada exitosamente. Ya puedes utilizar la aplicación."
}
``` |
| **Reglas de Negocio & Validaciones** | • Valida que `currentPassword` coincida con el hash BCrypt almacenado.<br>• Valida complejidad de `newPassword` (6+ chars, mayúscula, símbolo).<br>• La nueva contraseña no puede ser idéntica a la anterior.<br>• Coloca `mustChangePassword = false` en la entidad del usuario. |

---

#### SEC-04: Consulta de Identidad, Rol y Permisos de Sesión
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Retorna la identidad del usuario actual, su rol activo, los permisos heredados del rol, los permisos individuales directos y el consolidado de permisos efectivos. Utilizado por el frontend para renderizar menús y botones según privilegios. |
| **API Endpoint** | `GET /api/auth/me`<br>• **Headers:** `Authorization: Bearer <TOKEN>`<br>• **Códigos HTTP:** `200 OK`, `401 Unauthorized` |
| **Roles & Permisos** | **Autenticado** (`@PreAuthorize("isAuthenticated()")`). |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`UserEntity.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/entity/UserEntity.java)<br>2. **Repositorio:** [`UserRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/repository/UserRepository.java)<br>3. **DTO:** [`UserPermissionsDto.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/dto/UserPermissionsDto.java)<br>4. **Controlador:** [`AuthController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/controller/AuthController.java) |
| **Payload Entrada (Request)** | Ninguno (toma el `Principal` del contexto de seguridad). |
| **Payload Salida (Response)** | ```json
{
  "username": "admin",
  "role": "ADMIN",
  "rolePermissions": ["USER_READ", "ROLE_ASSIGN", "TICKET_DELETE"],
  "individualPermissions": ["AUDIT_READ"],
  "effectivePermissions": ["USER_READ", "ROLE_ASSIGN", "TICKET_DELETE", "AUDIT_READ"]
}
``` |
| **Reglas de Negocio & Validaciones** | • Resuelve el usuario a partir del token JWT validado en `JwtFilter`.<br>• Realiza la unión matemática (conjunto sin duplicados) entre los permisos del rol asignado y los permisos adicionales específicos asignados al usuario. |

---

### 1.2 Gestión de Usuarios y Perfiles

#### SEC-05: Listar Todos los Usuarios
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Consulta el listado completo de usuarios registrados en el sistema, incluyendo sus correos, roles asignados, permisos individuales y estado de bloqueo. |
| **API Endpoint** | `GET /api/user/all`<br>• **Headers:** `Authorization: Bearer <TOKEN>`<br>• **Códigos HTTP:** `200 OK`, `401 Unauthorized`, `403 Forbidden` |
| **Roles & Permisos** | Permiso específico `@PreAuthorize("hasAuthority('USER_READ')")`. |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`UserEntity.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/entity/UserEntity.java)<br>2. **Repositorio:** [`UserRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/repository/UserRepository.java)<br>3. **DTO:** [`UserDto.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/dto/UserDto.java)<br>4. **Mapper:** [`UserMapper.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/mapper/UserMapper.java)<br>5. **Servicio:** [`UserService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/service/UserService.java)<br>6. **Controlador:** [`UserController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/controller/UserController.java) |
| **Payload Entrada (Request)** | Ninguno. |
| **Payload Salida (Response)** | Array JSON de `UserDto` con `username`, `email`, `role`, `roles`, `locked`, `disabled`, `additionalPermissions`. |
| **Reglas de Negocio & Validaciones** | Las contraseñas se filtran y nunca son expuestas en el DTO de respuesta. |

---

#### SEC-06: Crear Usuario Administrativo
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a un administrador crear un nuevo usuario con rol o permisos personalizados. Si se omiten roles, asigna el rol por defecto. |
| **API Endpoint** | `POST /api/user/add`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `403 Forbidden`, `409 Conflict` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('USER_CREATE') and ((#dto.role == null and (#dto.roles == null or #dto.roles.isEmpty())) or hasAuthority('ROLE_ASSIGN')) and (#dto.additionalPermissions == null or #dto.additionalPermissions.isEmpty() or hasAuthority('PERMISSION_ASSIGN'))")` |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`UserEntity.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/entity/UserEntity.java)<br>2. **Repositorio:** [`UserRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/repository/UserRepository.java)<br>3. **DTO:** [`UserDto.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/dto/UserDto.java)<br>4. **Mapper:** [`UserMapper.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/mapper/UserMapper.java)<br>5. **Servicio:** [`UserService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/service/UserService.java)<br>6. **Controlador:** [`UserController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/controller/UserController.java) |
| **Payload Entrada (Request)** | ```json
{
  "username": "carlos.tecnico",
  "email": "carlos@campusdesk.edu.co",
  "password": "Password123!",
  "role": "TECHNICIAN",
  "additionalPermissions": []
}
``` |
| **Payload Salida (Response)** | Objeto `UserDto` con los datos del usuario registrado. |
| **Reglas de Negocio & Validaciones** | Control de autorización multi-etapa: requiere `USER_CREATE`, pero si el request especifica rol se evalúa `ROLE_ASSIGN`, y si incluye permisos individuales evalúa `PERMISSION_ASSIGN`. |

---

#### SEC-07: Actualizar Datos de Usuario
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Modifica información de un usuario existente. Los campos no enviados se conservan. Permite reasignar el rol único o actualizar permisos adicionales. |
| **API Endpoint** | `PUT /api/user/update`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('USER_UPDATE') and ((#dto.role == null and #dto.roles == null) or hasAuthority('ROLE_ASSIGN')) and (#dto.additionalPermissions == null or hasAuthority('PERMISSION_ASSIGN'))")` |
| **Orden Lógico de Archivos** | `UserEntity` → `UserRepository` → `UserDto` → `UserMapper` → `UserService` → `UserController` |
| **Reglas de Negocio & Validaciones** | Si se envía `password` en texto claro, se vuelve a encriptar con BCrypt antes de persistir. |

---

#### SEC-08: Asignar Rol a Usuario
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Reemplaza el rol único del usuario especificado por el nuevo rol provisto. Conserva intactos los permisos individuales previamente concedidos. |
| **API Endpoint** | `POST /api/user/assignRole`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('ROLE_ASSIGN')")` |
| **Orden Lógico de Archivos** | `UserRoleEntity`, `UserRoleId` → `UserRoleCrudRepository` & `UserRoleRepository` → `UserRoleDto` → `UserRoleMapper` → `UserService` → `UserController` |
| **Payload Entrada (Request)** | ```json
{
  "username": "carlos.tecnico",
  "role": "TECHNICIAN"
}
``` |

---

#### SEC-09: Asignar Permiso Individual a Usuario
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Concede un permiso puntual a un usuario, sumándolo a los permisos que ya hereda de su rol. Si ya lo posee, la operación es idempotente (no duplica). |
| **API Endpoint** | `POST /api/user/assignPermission`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('PERMISSION_ASSIGN')")` |
| **Orden Lógico de Archivos** | `UserEntity`, `PermissionEntity` → `UserRepository` → `UserPermissionDto`, `UserDto` → `UserService` → `UserController` |
| **Payload Entrada (Request)** | ```json
{
  "username": "carlos.tecnico",
  "permission": "AUDIT_READ"
}
``` |

---

#### SEC-10: Revocar Permiso Individual de Usuario
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Retira una concesión individual directa de un usuario. Los permisos provenientes del rol base permanecen activos. |
| **API Endpoint** | `DELETE /api/user/{username}/permissions/{permission}`<br>• **Headers:** `Authorization: Bearer <TOKEN>`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('PERMISSION_ASSIGN')")` |
| **Orden Lógico de Archivos** | `UserEntity`, `PermissionEntity` → `UserRepository` → `UserService` → `UserController` |

---

#### SEC-11: Consultar Permisos de un Usuario
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a los administradores auditar los permisos de un usuario específico: permisos del rol, permisos directos y efectivos. |
| **API Endpoint** | `GET /api/user/{username}/permissions`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('USER_READ')")` |
| **Orden Lógico de Archivos** | `UserEntity` → `UserRepository` → `UserPermissionsDto` → `UserService` → `UserController` |

---

#### SEC-12: Catálogo General de Permisos
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Retorna la lista con los nombres de todos los permisos existentes en la base de datos para poblar selectores de administración. |
| **API Endpoint** | `GET /api/user/permissions`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('PERMISSION_ASSIGN')")` |
| **Orden Lógico de Archivos** | `PermissionEntity` → `PermissionCrudRepository` → `UserRepository` → `UserService` → `UserController` |

---

#### SEC-13: Eliminar Usuario Definitivamente
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Elimina permanentemente un usuario de la base de datos previa verificación de existencia. |
| **API Endpoint** | `DELETE /api/user/delete/{name}`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('USER_DELETE')")` |
| **Orden Lógico de Archivos** | `UserEntity` → `UserCrudRepository` & `UserRepository` → `UserService` → `UserController` |

---

#### SEC-14: Restablecimiento Administrativo de Contraseña
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a Super Admin o administradores asignar una contraseña temporal a un usuario bloqueado u olvidado, activando la obligación de cambiarla en su siguiente acceso. |
| **API Endpoint** | `POST /api/user/{username}/reset-password`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('ADMIN')")` |
| **Orden Lógico de Archivos** | `UserEntity` → `UserCrudRepository` & `UserRepository` → `UserService` → `UserController` |
| **Payload Entrada (Request)** | ```json
{
  "temporaryPassword": "Temporal123!"
}
``` *(Opcional; si se omite usa el valor por defecto seguro)* |
| **Reglas de Negocio & Validaciones** | Encripta la clave temporal con BCrypt y setea `mustChangePassword = true` en la base de datos. |

---

#### SEC-15: Consultar Perfil Propio
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Devuelve los datos de contacto y perfil del usuario autenticado actual (teléfono, departamento, email, username). |
| **API Endpoint** | `GET /api/user/me/profile`<br>• **Códigos HTTP:** `200 OK`, `401 Unauthorized` |
| **Roles & Permisos** | Autenticado (`Authentication auth`). |
| **Orden Lógico de Archivos** | `UserEntity` → `UserCrudRepository` → `UserProfileDto` → `UserService` → `UserController` |

---

#### SEC-16: Actualizar Perfil Propio
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite al usuario en sesión actualizar su número telefónico y departamento institucional. |
| **API Endpoint** | `PUT /api/user/me/profile`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request` |
| **Roles & Permisos** | Autenticado (`Authentication auth`). |
| **Orden Lógico de Archivos** | `UserEntity` → `UserCrudRepository` → `UserProfileUpdateRequest`, `UserProfileDto` → `UserService` → `UserController` |
| **Payload Entrada (Request)** | ```json
{
  "phoneNumber": "+57 310 1234567",
  "department": "Ingeniería de Sistemas"
}
``` |

---

#### SEC-17: Bloqueo y Desbloqueo Masivo de Usuarios
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite bloquear o desbloquear de forma masiva una lista de usuarios en una sola operación transaccional. |
| **API Endpoint** | `POST /api/user/bulk-lock?locked=true`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasRole('ADMIN')")` |
| **Orden Lógico de Archivos** | `UserEntity` → `UserCrudRepository` → `UserService` → `UserController` |
| **Payload Entrada (Request)** | `["usuario1", "usuario2", "usuario3"]` |
| **Payload Salida (Response)** | `{"updatedCount": 3, "locked": true}` |

---

### 1.3 Catálogo de Roles y Permisos Dinámicos

#### SEC-18: Listar Roles y Permisos Asociados
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Consulta el catálogo completo de roles creados en el sistema junto con los permisos que tiene vinculados cada uno. |
| **API Endpoint** | `GET /api/roles`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('ROLE_MANAGE')")` |
| **Orden Lógico de Archivos** | `RoleEntity`, `PermissionEntity` → `RoleCrudRepository` → `RoleCatalogDto` → `SecurityCatalogService` → `SecurityCatalogController` |

---

#### SEC-19: Crear Nuevo Rol en el Catálogo
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Registra un nuevo rol en la base de datos con una lista opcional de permisos válidos existentes. |
| **API Endpoint** | `POST /api/roles`<br>• **Códigos HTTP:** `201 Created`, `400 Bad Request`, `403 Forbidden`, `409 Conflict` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('ROLE_MANAGE') and (#dto.permissions == null or #dto.permissions.isEmpty() or hasAuthority('PERMISSION_MANAGE'))")` |
| **Orden Lógico de Archivos** | `RoleEntity`, `PermissionEntity` → `RoleCrudRepository`, `PermissionCrudRepository` → `RoleCatalogDto` → `SecurityCatalogService` → `SecurityCatalogController` |
| **Payload Entrada (Request)** | ```json
{
  "name": "AUDITOR_EXTERNO",
  "permissions": ["USER_READ", "AUDIT_READ"]
}
``` |

---

#### SEC-20: Listar y Crear Permisos Maestros
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite auditar el inventario de permisos maestros del sistema y registrar nuevos permisos para futuros módulos. |
| **API Endpoints** | • `GET /api/permissions`<br>• `POST /api/permissions` (`201 Created`) |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")` |
| **Orden Lógico de Archivos** | `PermissionEntity` → `PermissionCrudRepository` → `PermissionCatalogDto` → `SecurityCatalogService` → `SecurityCatalogController` |

---

#### SEC-21: Vincular y Desvincular Permiso de un Rol
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Asocia o retira un permiso de un rol. El cambio impacta inmediatamente a todos los usuarios que posean ese rol. Protege los permisos esenciales de `ADMIN`, `SUPERADMIN` y `CUSTOMER`. |
| **API Endpoints** | • `PUT /api/roles/{role}/permissions/{permission}` (Vincular)<br>• `DELETE /api/roles/{role}/permissions/{permission}` (Desvincular) |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('ROLE_MANAGE') and hasAuthority('PERMISSION_MANAGE')")` |
| **Orden Lógico de Archivos** | `RoleEntity` (`findForUpdate`), `PermissionEntity` → `RoleCrudRepository` → `RoleCatalogDto` → `SecurityCatalogService` → `SecurityCatalogController` |
| **Reglas de Negocio & Validaciones** | Bloqueo pesimista en la entidad Rol para evitar condiciones de carrera durante la edición concurrente de permisos. |

---

### 1.4 Infraestructura de Seguridad y Filtros JWT

| Archivo / Componente | Responsabilidad Técnica |
| :--- | :--- |
| [`SecurityConfig.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/config/SecurityConfig.java) | Configura el `SecurityFilterChain`: define `SessionCreationPolicy.STATELESS`, desactiva CSRF para API REST, habilita CORS predeterminado, declara rutas públicas (`/api/auth/**`, `/api/v1/announcements/active`, `/api/v1/kb/articles/**`, `/v3/api-docs/**`) y antepone `JwtFilter` a `UsernamePasswordAuthenticationFilter`. Habilita seguridad por anotación con `@EnableMethodSecurity(securedEnabled = true)`. |
| [`JwtFilter.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/config/JwtFilter.java) | Intercepta cada solicitud HTTP: extrae el encabezado `Authorization`, valida el prefijo `Bearer `, verifica la firma HMAC y expiración con `JwtUtil`, carga las autoridades con `UserSecurityService` y puebla el `SecurityContextHolder` con un `UsernamePasswordAuthenticationToken`. |
| [`JwtUtil.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/config/JwtUtil.java) | Utilidad para creación y verificación criptográfica de tokens JWT usando algoritmo HMAC-SHA256 y clave secreta configurada en `JwtProperties`. Extrae el `subject` (username). |
| [`UserSecurityService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/service/UserSecurityService.java) | Implementa `UserDetailsService` de Spring Security. Construye el objeto `UserDetails` inyectando tanto el prefijo `ROLE_<ROL>` como las autoridades directas (`USER_READ`, etc.) obtenidas de la base de datos. |
| [`SecurityBootstrapService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/service/SecurityBootstrapService.java) | Se ejecuta al arrancar el contexto de Spring: asegura la existencia de roles (`ADMIN`, `USER`, `TECHNICIAN`, `SUPERADMIN`), permisos fundamentales y el usuario administrador por defecto si la base de datos está vacía. |
| [`ApiExceptionHandler.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/web/controller/ApiExceptionHandler.java) | `@ControllerAdvice` global que captura `AccessDeniedException` (403), `BadCredentialsException` (401), `MethodArgumentNotValidException` (400) y `ResponseStatusException`, devolviendo respuestas JSON estandarizadas. |

---

# PARTE II: MÓDULOS DE NEGOCIO (CAMPUSDESK)

A continuación se detallan todas las funcionalidades pertenecientes a los 20 submódulos de negocio de CampusDesk, organizadas por dominio.

---

## 2.1 Módulo 1: Gestión de Incidencias / Tickets (`modules/ticket`)

### MOD-01: Crear Ticket / Incidencia Propia
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Crea una nueva incidencia tecnológica en estado inicial `OPEN` vinculada al solicitante autenticado. Calcula automáticamente las fechas límite de SLA (primera respuesta y resolución), genera un registro inmutable en el historial y emite el evento `TicketCreatedEvent`. |
| **API Endpoint** | `POST /api/tickets`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden` |
| **Roles & Permisos** | Exclusivo solicitantes: `@PreAuthorize("hasRole('USER')")`. |
| **Orden Lógico de Archivos** | 1. **Entidades & Enums:** [`Ticket.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/entity/Ticket.java), [`TicketPriority.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/enums/TicketPriority.java), [`TicketCategory.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/enums/TicketCategory.java), [`TicketStatus.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/enums/TicketStatus.java)<br>2. **Repositorios:** [`TicketRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/repository/TicketRepository.java) & [`UserCrudRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/persistance/crud/UserCrudRepository.java)<br>3. **DTOs:** [`TicketCreateRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/dto/TicketCreateRequest.java) & [`TicketResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/dto/TicketResponse.java)<br>4. **Mapper:** [`TicketMapper.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/mapper/TicketMapper.java)<br>5. **Servicios & Eventos:** [`TicketService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/service/TicketService.java), [`SlaService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/service/SlaService.java), [`StatusHistoryService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/service/StatusHistoryService.java), [`TicketCreatedEvent.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/domain/event/TicketCreatedEvent.java)<br>6. **Controlador:** [`TicketController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/controller/TicketController.java)<br>7. **Seguridad:** `@PreAuthorize("hasRole('USER')")` |
| **Payload Entrada (Request)** | ```json
{
  "title": "Fallo en proyector Sala 302",
  "description": "El proyector no enciende la lámpara ni recibe señal HDMI",
  "category": "HARDWARE",
  "priority": "HIGH",
  "serviceCatalogId": 1,
  "urgentReason": "Clase magistral programada"
}
``` |
| **Payload Salida (Response)** | Retorna `TicketResponse` completo con `id`, `title`, `status: OPEN`, `applicantUsername`, `slaDueDate`, `firstResponseDueDate`, `createdAt`, etc. |
| **Reglas de Negocio & Validaciones** | • Título y descripción obligatorios no vacíos.<br>• Consulta las políticas SLA vigentes para la prioridad seleccionada y programa las fechas de vencimiento.<br>• Registra en la tabla `status_history` la acción `CREATION`.<br>• Publica de manera asíncrona el evento `TicketCreatedEvent`. |

---

### MOD-02: Crear Ticket en Nombre de un Usuario (On-Behalf)
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a técnicos o administradores registrar una incidencia reportada presencialmente o por llamada telefónica en nombre de otro usuario solicitante. |
| **API Endpoint** | `POST /api/tickets/on-behalf?applicantUsername=estudiante1`<br>• **Códigos HTTP:** `201 Created`, `404 Not Found`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")` |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `TicketCreateRequest`, `TicketResponse` → `TicketMapper` → `TicketService` (con `SlaService` y `StatusHistoryService`) → `TicketController` |
| **Reglas de Negocio & Validaciones** | Valida la existencia del solicitante en `UserCrudRepository`. Asigna el ticket al solicitante pero registra en el historial que el autor físico de la creación fue el técnico o admin en sesión. |

---

### MOD-03: Listar Tickets con Filtros (Visibilidad Restringida)
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Consulta el listado de tickets aplicando los filtros opcionales de estado, prioridad y categoría, respetando la política estricta de aislamiento de datos según el rol. |
| **API Endpoint** | `GET /api/tickets?status=OPEN&priority=HIGH&category=HARDWARE`<br>• **Códigos HTTP:** `200 OK`, `401 Unauthorized` |
| **Roles & Permisos** | Autenticado (`USER`, `TECHNICIAN`, `ADMIN`, `SUPERADMIN`). |
| **Orden Lógico de Archivos** | `Ticket` → `TicketSpecifications` & `TicketRepository` → `TicketResponse` → `TicketMapper` → `TicketAccessPolicy` & `TicketService` → `TicketController` |
| **Reglas de Negocio & Validaciones** | **Reglas de Alcance / Visibilidad:**<br>• **ADMIN / SUPERADMIN:** Visualiza todas las incidencias de la institución.<br>• **TECHNICIAN:** Visualiza únicamente las incidencias que tiene asignadas (`technician.username = :me`).<br>• **USER:** Visualiza únicamente las incidencias creadas por él (`applicant.username = :me`).<br>• Combina los filtros dinámicos con `Specification.allOf(...)` en una sola consulta SQL optimizada con orden descendente por fecha. |

---

### MOD-04: Listado Paginado de Incidencias
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Consulta paginada de tickets con tamaño de página y filtros dinámicos. Ideal para tablas con grandes volúmenes de datos. |
| **API Endpoint** | `GET /api/tickets/paginated?page=0&size=10&status=ASSIGNED`<br>• **Códigos HTTP:** `200 OK`, `401 Unauthorized` |
| **Roles & Permisos** | Autenticado. Aplica la misma regla de visibilidad de `TicketAccessPolicy`. |
| **Orden Lógico de Archivos** | `Ticket` → `TicketSpecifications`, `TicketRepository` → `PageResponse<TicketResponse>` → `TicketMapper` → `TicketService` → `TicketController` |

---

### MOD-05: Consultar Detalle de Incidencia por ID
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Retorna la ficha completa de un ticket específico. Si el usuario no tiene permisos de visualización sobre este ticket, deniega la consulta con `403 Forbidden` para evitar fuga de metadatos. |
| **API Endpoint** | `GET /api/tickets/{id}`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | Autenticado (verificado por `TicketAccessPolicy.assertCanView(ticket, auth)`). |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `TicketResponse` → `TicketMapper` → `TicketAccessPolicy` & `TicketService` → `TicketController` |

---

### MOD-06: Modificar Incidencia Propia (Solo en Estado OPEN)
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite al solicitante corregir el título, descripción, prioridad o categoría de su incidencia antes de que un técnico haya sido asignado. |
| **API Endpoint** | `PUT /api/tickets/{id}`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `403 Forbidden`, `404 Not Found`, `409 Conflict` |
| **Roles & Permisos** | Exclusivo solicitante o admin (`@PreAuthorize("hasRole('USER')")` + comprobación de propiedad). |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `TicketUpdateRequest`, `TicketResponse` → `TicketMapper` → `TicketAccessPolicy` & `TicketService` → `TicketController` |
| **Reglas de Negocio & Validaciones** | Solo modificable si `status == OPEN` y `technician == null`. Si ya fue asignada o está en progreso, retorna `409 Conflict`. |

---

### MOD-07: Cancelar Incidencia Propia
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite al solicitante desistir o cancelar una incidencia que aún se encuentra en estado `OPEN`. |
| **API Endpoint** | `POST /api/tickets/{id}/cancel`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found`, `409 Conflict` |
| **Roles & Permisos** | Solicitante propietario o Admin. |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `TicketResponse` → `StatusHistoryService` → `TicketService` → `TicketController` |
| **Reglas de Negocio & Validaciones** | Cambia el estado a `CANCELLED`. Registra la acción `CLOSURE` en el historial de estados. |

---

### MOD-08: Solicitar Reapertura de Ticket Resuelto
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite reabrir un ticket cuando la solución brindada fue insatisfactoria o la falla reapareció. |
| **API Endpoint** | `POST /api/tickets/{id}/reopen?reason=El+problema+persiste`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found`, `409 Conflict` |
| **Roles & Permisos** | Solicitante propietario o Admin. |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `TicketResponse` → `StatusHistoryService` → `TicketService` → `TicketController` |
| **Reglas de Negocio & Validaciones** | Solo tickets en estado `RESOLVED` o `CLOSED` pueden ser reabiertos. Incrementa el contador `reopenedCount` y cambia el estado a `IN_PROGRESS`. |

---

### MOD-09: Clonar Incidencia como Plantilla
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Duplica los datos base de un ticket existente para reportar una incidencia idéntica rápidamente sin escribir todo desde cero. |
| **API Endpoint** | `POST /api/tickets/{id}/clone`<br>• **Códigos HTTP:** `201 Created`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | Autenticado con visibilidad sobre el ticket original. |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `TicketCreateRequest` → `TicketService.create(...)` → `TicketController` |

---

### MOD-10: Solicitar Escalación Técnica Prioritaria
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Marca una incidencia como escalada (`isEscalated = true`, `status = ESCALATED`) para atención de nivel superior. |
| **API Endpoint** | `POST /api/tickets/{id}/escalate?reason=Bloqueo+critico`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | Autenticado con permisos de visualización. |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `TicketResponse` → `TicketService` → `TicketController` |

---

### MOD-11: Eliminar Incidencia Definitivamente (SuperAdmin)
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Elimina físicamente el registro de la incidencia de la base de datos. Acción irreversible reservada a SuperAdmin. |
| **API Endpoint** | `DELETE /api/tickets/{id}`<br>• **Códigos HTTP:** `204 No Content`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('TICKET_DELETE') or hasRole('SUPERADMIN')")` |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `TicketService` → `TicketController` |

---

### MOD-12: Gestión de Subtareas de Incidencias
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite dividir incidencias complejas en ítems o tareas secundarias que se completan progresivamente. |
| **Métodos & Archivos** | • **Crear Subtarea:** `createSubtask(ticketId, request)`<br>• **Marcar Completada:** `toggleCompleted(subtaskId)`<br>• **Listar Subtareas:** `getSubtasksByTicket(ticketId)`<br>• **Archivos:** [`TicketSubtask.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/entity/TicketSubtask.java), [`TicketSubtaskRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/repository/TicketSubtaskRepository.java), [`SubtaskRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/dto/SubtaskRequest.java), [`SubtaskResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/dto/SubtaskResponse.java), [`TicketSubtaskService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/service/TicketSubtaskService.java) |

---

## 2.2 Módulo 2: Ciclo de Vida y Transición de Estados (`modules/lifecycle`)

### MOD-13: Transición de Estado de Incidencia (Máquina de Estados)
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Gobierna el ciclo de vida del ticket mediante una máquina de estados finita validada en base de datos (`LifecycleRule`). Ejecuta transiciones como `START_PROGRESS`, `RESOLVE`, `CONFIRM_CLOSE`, `CANCEL`, `REOPEN`, `ESCALATE`. Utiliza bloqueo pesimista y actualiza el historial en la misma transacción atómica. |
| **API Endpoints** | • `POST /api/tickets/{ticketId}/transitions`<br>• `PATCH /api/tickets/{ticketId}/status`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `403 Forbidden`, `404 Not Found`, `409 Conflict` |
| **Roles & Permisos** | Depende de la acción solicitada:<br>• `START_PROGRESS`, `RESOLVE`: Técnico asignado.<br>• `CONFIRM_CLOSE`: Solicitante propietario.<br>• `ASSIGN`, `REASSIGN`: Administrador. |
| **Orden Lógico de Archivos** | 1. **Entidades & Enums:** [`Ticket.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/entity/Ticket.java), [`LifecycleRule.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/entity/LifecycleRule.java), [`TicketStatus.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/enums/TicketStatus.java), [`TransitionAction.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/enums/TransitionAction.java)<br>2. **Repositorios:** [`TicketRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/ticket/repository/TicketRepository.java) (`findForUpdate`), [`LifecycleRuleRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/repository/LifecycleRuleRepository.java)<br>3. **DTOs:** [`StateTransitionRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/dto/StateTransitionRequest.java) & [`StateTransitionResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/dto/StateTransitionResponse.java)<br>4. **Servicios:** [`TicketLifecycleService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/service/TicketLifecycleService.java), [`StatusHistoryService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/service/StatusHistoryService.java)<br>5. **Controlador:** [`TicketLifecycleController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/lifecycle/controller/TicketLifecycleController.java) |
| **Payload Entrada (Request)** | ```json
{
  "action": "START_PROGRESS",
  "comment": "Iniciando diagnóstico en sitio de la red"
}
``` |
| **Payload Salida (Response)** | Retorna `StateTransitionResponse` con `ticketId`, `previousStatus`, `newStatus: IN_PROGRESS`, `changedBy`, `changedAt`. |
| **Reglas de Negocio & Validaciones** | • Obtiene el ticket con bloqueo pesimista en base de datos (`SELECT FOR UPDATE`).<br>• Si la regla de transición no existe en `LifecycleRule`, arroja `409 Conflict`.<br>• Verifica rigurosamente que el actor que ejecuta la transición cumpla con el rol requerido por la regla. |

---

## 2.3 Módulo 3: Asignación y Reasignación de Técnicos (`modules/assignment`)

### MOD-14: Asignación Inicial de Técnico por Administrador
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Asigna por primera vez un técnico a un ticket en estado `OPEN`, cambiando su estado a `ASSIGNED` y guardando la traza en `TicketAssignmentLog`. |
| **API Endpoint** | `PUT` o `PATCH /api/tickets/{ticketId}/assign`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | Exclusivo administrador: `@PreAuthorize("hasRole('ADMIN')")`. |
| **Orden Lógico de Archivos** | `TicketAssignmentLog`, `AssignmentType` → `TicketAssignmentLogRepository`, `TicketRepository` → `TicketAssignRequest`, `TicketAssignmentResponse` → `TicketAssignmentService` → `TicketAssignmentController` |
| **Payload Entrada (Request)** | `{"technicianUsername": "carlos.tecnico"}` |
| **Reglas de Negocio & Validaciones** | Bloqueo pesimista `PESSIMISTIC_WRITE`. El ticket debe estar en `OPEN` y el usuario objetivo debe tener el rol `TECHNICIAN`. |

---

### MOD-15: Reasignación de Técnico con Justificación Obligatoria
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Reasigna un ticket previamente asignado a otro técnico diferente, exigiendo un texto de justificación administrativa para fines de auditoría. |
| **API Endpoint** | `PUT /api/tickets/{ticketId}/reassign`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasRole('ADMIN')")` |
| **Orden Lógico de Archivos** | `TicketAssignmentLog` → `TicketAssignmentLogRepository` → `TicketReassignRequest`, `TicketAssignmentResponse` → `TicketAssignmentService` → `TicketAssignmentController` |
| **Payload Entrada (Request)** | ```json
{
  "technicianUsername": "nuevo.tecnico",
  "justification": "Técnico anterior asignado a guardia externa de emergencia"
}
``` |
| **Reglas de Negocio & Validaciones** | El ticket debe estar en estado `ASSIGNED` y no puede reasignarse al mismo técnico que ya lo tiene. |

---

### MOD-16: Auto-Asignación de Incidencia por Técnico
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a un técnico tomar directamente un ticket libre de la cola en estado `OPEN` sin esperar intervención de un administrador. |
| **API Endpoint** | `POST /api/tickets/{ticketId}/self-assign`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `403 Forbidden` |
| **Roles & Permisos** | Exclusivo técnico: `@PreAuthorize("hasRole('TECHNICIAN')")`. |
| **Orden Lógico de Archivos** | `TicketAssignmentLog` → `TicketAssignmentLogRepository` → `TicketAssignmentResponse` → `TicketAssignmentService` → `TicketAssignmentController` |

---

### MOD-17: Asignación Automática Inteligente por Balanceo de Carga
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Selecciona automáticamente de la base de datos al técnico disponible con la menor cantidad de incidencias activas en curso y le adjudica el ticket. |
| **API Endpoint** | `POST /api/tickets/{ticketId}/auto-assign`<br>• **Códigos HTTP:** `200 OK`, `400 Bad Request`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")` |
| **Orden Lógico de Archivos** | `TechnicianProfile` → `TechnicianRepository` (`findAvailableTechniciansWithCapacity`) → `TicketAssignmentLog` → `TicketAssignmentService` → `TicketAssignmentController` |
| **Reglas de Negocio & Validaciones** | Incrementa el contador de carga activa `activeTicketsCount` del técnico seleccionado e inserta la justificación `"Auto-asignado por balanceo de carga"`. |

---

### MOD-18: Reasignación Masiva de Incidencias
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Transfiere en bloque todos los tickets activos (`ASSIGNED`, `IN_PROGRESS`) de un técnico saliente o incapacitado hacia un nuevo técnico receptor. |
| **API Endpoint** | `POST /api/tickets/bulk-reassign?fromTechnician=tecnico1&toTechnician=tecnico2`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasRole('ADMIN')")` |
| **Orden Lógico de Archivos** | `Ticket`, `TicketAssignmentLog` → `TicketAssignmentService` → `TicketAssignmentController` |
| **Payload Salida (Response)** | `{"reassignedCount": 5, "success": true}` |

---

## 2.4 Módulo 4: Comentarios y Notas Internas (`modules/comment`)

### MOD-19: Agregar Comentario Público o Nota Interna
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Añade un mensaje al hilo del ticket. Si es una nota técnica interna (`isInternal = true` o `visibility = INTERNAL`), solo será visible para técnicos y administradores. |
| **API Endpoint** | `POST /api/tickets/{ticketId}/comments`<br>• **Headers:** `Authorization: Bearer <TOKEN>`, `Content-Type: application/json`<br>• **Códigos HTTP:** `201 Created`, `400 Bad Request`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("isAuthenticated()")` (Verifica que sea el solicitante, el técnico asignado o admin). |
| **Orden Lógico de Archivos** | 1. **Entidad & Enums:** [`Comment.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/entity/Comment.java), [`CommentVisibility.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/enums/CommentVisibility.java)<br>2. **Repositorio:** [`CommentRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/repository/CommentRepository.java)<br>3. **DTOs:** [`CommentCreateRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/dto/CommentCreateRequest.java), [`CommentResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/dto/CommentResponse.java)<br>4. **Servicio:** [`CommentService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/service/CommentService.java)<br>5. **Controlador:** [`CommentController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/comment/controller/CommentController.java) |
| **Payload Entrada (Request)** | ```json
{
  "content": "Se verificó cableado del switch, requiere cambio de patchcord",
  "visibility": "INTERNAL",
  "isInternal": true
}
``` |
| **Reglas de Negocio & Validaciones** | No se pueden añadir comentarios si la incidencia está `CLOSED`. Si un usuario solicitante intenta marcar `INTERNAL`, se rechaza con `403 Forbidden`. |

---

### MOD-20: Consultar Comentarios de un Ticket
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Devuelve los comentarios del ticket ordenados cronológicamente. Si el consultante es el solicitante, los comentarios internos son filtrados automáticamente en la consulta. |
| **API Endpoint** | `GET /api/tickets/{ticketId}/comments`<br>• **Códigos HTTP:** `200 OK`, `401 Unauthorized`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("isAuthenticated()")`. |
| **Orden Lógico de Archivos** | `Comment` → `CommentRepository` → `CommentResponse` → `CommentService` → `CommentController` |

---

## 2.5 Módulo 5: Gestión de Archivos Adjuntos (`modules/attachment`)

### MOD-21: Cargar Metadatos de Archivo Adjunto
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Registra la referencia y metadatos de un archivo cargado (capturas de pantalla, logs, PDFs) vinculado a una incidencia o a un comentario específico. |
| **API Endpoint** | `POST /api/v1/attachments/ticket/{ticketId}`<br>• **Códigos HTTP:** `201 Created`, `400 Bad Request` |
| **Roles & Permisos** | Autenticado con visibilidad sobre el ticket. |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`TicketAttachment.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/attachment/entity/TicketAttachment.java)<br>2. **Repositorio:** [`AttachmentRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/attachment/repository/AttachmentRepository.java)<br>3. **DTO:** [`AttachmentResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/attachment/dto/AttachmentResponse.java)<br>4. **Servicio:** [`AttachmentService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/attachment/service/AttachmentService.java)<br>5. **Controlador:** [`AttachmentController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/attachment/controller/AttachmentController.java) |
| **Payload Entrada (Request)** | ```json
{
  "fileName": "error_pantalla.png",
  "fileType": "image/png",
  "fileSize": 204850,
  "storagePath": "/uploads/tickets/error_pantalla.png"
}
``` |

---

### MOD-22: Listar Adjuntos de un Ticket
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Retorna la lista de archivos adjuntos asociados a una incidencia. |
| **API Endpoint** | `GET /api/v1/attachments/ticket/{ticketId}`<br>• **Códigos HTTP:** `200 OK` |
| **Roles & Permisos** | Autenticado. |
| **Orden Lógico de Archivos** | `TicketAttachment` → `AttachmentRepository` → `AttachmentResponse` → `AttachmentService` → `AttachmentController` |

---

## 2.6 Módulo 6: Directorio y Disponibilidad de Técnicos (`modules/technician`)

### MOD-23: Listar Todos los Técnicos
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Consulta el directorio global de perfiles técnicos, especialidades, carga activa y estado de disponibilidad. |
| **API Endpoints** | `GET /api/technicians` y `GET /api/users/technicians`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasRole('ADMIN')")` |
| **Orden Lógico de Archivos** | 1. **Entidad & Enums:** [`TechnicianProfile.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/entity/TechnicianProfile.java), [`TechnicianAvailabilityStatus.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/enums/TechnicianAvailabilityStatus.java)<br>2. **Repositorio:** [`TechnicianRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/repository/TechnicianRepository.java)<br>3. **DTO:** [`TechnicianResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/dto/TechnicianResponse.java)<br>4. **Mapper:** [`TechnicianMapper.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/mapper/TechnicianMapper.java)<br>5. **Servicio:** [`TechnicianService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/service/TechnicianService.java)<br>6. **Controlador:** [`TechnicianController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/technician/controller/TechnicianController.java) |

---

### MOD-24: Listar Técnicos Disponibles por Menor Carga
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Retorna únicamente los técnicos que están en estado `AVAILABLE`, ordenados de forma ascendente por el número de tickets activos asignados. |
| **API Endpoint** | `GET /api/technicians/available`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasRole('ADMIN')")` |
| **Orden Lógico de Archivos** | `TechnicianProfile` → `TechnicianRepository` (`findByAvailabilityStatusOrderByActiveTicketsCountAsc`) → `TechnicianAvailabilityResponse` → `TechnicianMapper` → `TechnicianService` → `TechnicianController` |

---

## 2.7 Módulo 7: Catálogo de Servicios IT (`modules/servicecatalog`)

### MOD-25: Consultar Ítems Activos del Catálogo de Servicios
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Consulta el catálogo de servicios IT institucionales disponibles para solicitud (Hardware, Cuentas, Software, Red). |
| **API Endpoints** | • `GET /api/v1/service-catalog/items`<br>• `GET /api/v1/service-catalog/items/category/{category}`<br>• **Códigos HTTP:** `200 OK` |
| **Roles & Permisos** | Autenticado (`.requestMatchers("/api/v1/service-catalog/items/**").authenticated()`). |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`ServiceCatalogItem.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/servicecatalog/entity/ServiceCatalogItem.java)<br>2. **Repositorio:** [`ServiceCatalogItemRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/servicecatalog/repository/ServiceCatalogItemRepository.java)<br>3. **DTO:** [`CatalogItemResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/servicecatalog/dto/CatalogItemResponse.java)<br>4. **Servicio:** [`ServiceCatalogService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/servicecatalog/service/ServiceCatalogService.java)<br>5. **Controlador:** [`ServiceCatalogController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/servicecatalog/controller/ServiceCatalogController.java) |

---

### MOD-26: Crear Nuevo Servicio en el Catálogo
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a los administradores registrar un nuevo servicio parametrizable con su SLA estimado y categoría. |
| **API Endpoint** | `POST /api/v1/service-catalog/items`<br>• **Códigos HTTP:** `201 Created`, `400 Bad Request`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasRole('ADMIN')")` |
| **Orden Lógico de Archivos** | `ServiceCatalogItem` → `ServiceCatalogItemRepository` → `CatalogItemRequest`, `CatalogItemResponse` → `ServiceCatalogService` → `ServiceCatalogController` |
| **Payload Entrada (Request)** | ```json
{
  "name": "Configuración de VPN Institucional",
  "description": "Acceso remoto a la red académica",
  "category": "NETWORK",
  "estimatedHours": 12,
  "iconName": "vpn-lock"
}
``` |

---

## 2.8 Módulo 8: Base de Conocimiento y Autoservicio FAQ (`modules/knowledgebase`)

### MOD-27: Artículos Públicos, Búsqueda y Sugerencias en Vivo
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Motor de autoservicio para reducir la apertura de tickets repetitivos:<br>1. Lista artículos publicados paginados.<br>2. Búsqueda por coincidencia de palabras clave.<br>3. Sugerencia en vivo (*Live Suggestions*) mientras el usuario redacta el título de su ticket. |
| **API Endpoints** | • `GET /api/v1/kb/articles?page=0&size=10`<br>• `GET /api/v1/kb/articles/search?query=wifi`<br>• `GET /api/v1/kb/articles/suggest?title=conectar+red`<br>• `GET /api/v1/kb/articles/{id}` (incrementa vista)<br>• `GET /api/v1/kb/categories`<br>• **Códigos HTTP:** `200 OK` |
| **Roles & Permisos** | **Público** (`permitAll()` en `SecurityConfig`). |
| **Orden Lógico de Archivos** | 1. **Entidades:** [`KbArticle.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/knowledgebase/entity/KbArticle.java), [`KbCategory.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/knowledgebase/entity/KbCategory.java)<br>2. **Repositorios:** [`KbArticleRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/knowledgebase/repository/KbArticleRepository.java), [`KbCategoryRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/knowledgebase/repository/KbCategoryRepository.java)<br>3. **DTOs:** [`KbArticleResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/knowledgebase/dto/KbArticleResponse.java), [`KbCategoryResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/knowledgebase/dto/KbCategoryResponse.java)<br>4. **Servicio:** [`KnowledgeBaseService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/knowledgebase/service/KnowledgeBaseService.java)<br>5. **Controlador:** [`KnowledgeBaseController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/knowledgebase/controller/KnowledgeBaseController.java) |

---

### MOD-28: Votación de Utilidad de Artículos KB
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite registrar feedback sobre un artículo ("¿Te sirvió? Sí/No"), incrementando `helpfulCount` o `unhelpfulCount`. |
| **API Endpoint** | `POST /api/v1/kb/articles/{id}/vote?helpful=true`<br>• **Códigos HTTP:** `204 No Content` |
| **Roles & Permisos** | Público / Autenticado. |
| **Orden Lógico de Archivos** | `KbArticle` → `KbArticleRepository` → `KnowledgeBaseService` → `KnowledgeBaseController` |

---

### MOD-29: Crear Artículo en Base de Conocimiento
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a técnicos o administradores redactar y publicar guías de solución y manuales paso a paso. |
| **API Endpoint** | `POST /api/v1/kb/articles`<br>• **Códigos HTTP:** `201 Created`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")` |
| **Orden Lógico de Archivos** | `KbArticle`, `KbCategory` → `KbArticleRepository` → `KbArticleRequest`, `KbArticleResponse` → `KnowledgeBaseService` → `KnowledgeBaseController` |
| **Payload Entrada (Request)** | ```json
{
  "title": "Cómo conectarse a Eduroam",
  "content": "Pasos para autenticarse con credenciales institucionales...",
  "categoryId": 2,
  "isPublished": true
}
``` |

---

## 2.9 Módulo 9: Anuncios y Avisos Globales (`modules/announcement`)

### MOD-30: Consultar Avisos Activos del Sistema
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Retorna la lista de alertas institucionales vigentes (mantenimientos programados, caídas de red, interrupciones de servicios). |
| **API Endpoint** | `GET /api/v1/announcements/active`<br>• **Códigos HTTP:** `200 OK` |
| **Roles & Permisos** | **Público** (`permitAll()` en `SecurityConfig`). |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`Announcement.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/announcement/entity/Announcement.java)<br>2. **Repositorio:** [`AnnouncementRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/announcement/repository/AnnouncementRepository.java)<br>3. **DTO:** [`AnnouncementResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/announcement/dto/AnnouncementResponse.java)<br>4. **Servicio:** [`AnnouncementService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/announcement/service/AnnouncementService.java)<br>5. **Controlador:** [`AnnouncementController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/announcement/controller/AnnouncementController.java) |

---

### MOD-31: Crear Anuncio Institucional
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a administradores publicar un banner informativo con rango de fechas de expiración y criticidad. |
| **API Endpoint** | `POST /api/v1/announcements`<br>• **Códigos HTTP:** `201 Created`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasRole('ADMIN')")` |
| **Orden Lógico de Archivos** | `Announcement` → `AnnouncementRepository` → `AnnouncementRequest`, `AnnouncementResponse` → `AnnouncementService` → `AnnouncementController` |
| **Payload Entrada (Request)** | ```json
{
  "title": "Mantenimiento Servidor Campus",
  "content": "Corte programado este sábado de 22:00 a 02:00",
  "severity": "CRITICAL",
  "validUntil": "2026-10-25T23:59:59"
}
``` |

---

## 2.10 Módulo 10: Encuestas de Satisfacción CSAT (`modules/satisfaction`)

### MOD-32: Calificación de Satisfacción CSAT
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite al solicitante calificar el servicio técnico recibido tras el cierre o resolución de su ticket (escala 1 a 5 estrellas) con comentarios de retroalimentación. |
| **API Endpoints** | • `POST /api/v1/satisfaction/tickets/{ticketId}` (`201 Created`)<br>• `GET /api/v1/satisfaction/tickets/{ticketId}` (`200 OK`)<br>• `GET /api/v1/satisfaction/average` (Promedio institucional global) |
| **Roles & Permisos** | Envío: Solicitante autenticado. Promedio: `@PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")`. |
| **Orden Lógico de Archivos** | 1. **Entidad & Enums:** [`TicketSatisfaction.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/satisfaction/entity/TicketSatisfaction.java), [`CsatScore.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/satisfaction/enums/CsatScore.java)<br>2. **Repositorio:** [`TicketSatisfactionRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/satisfaction/repository/TicketSatisfactionRepository.java)<br>3. **DTOs:** [`CsatRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/satisfaction/dto/CsatRequest.java), [`CsatResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/satisfaction/dto/CsatResponse.java)<br>4. **Servicio:** [`SatisfactionService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/satisfaction/service/SatisfactionService.java)<br>5. **Controlador:** [`SatisfactionController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/satisfaction/controller/SatisfactionController.java) |
| **Payload Entrada (Request)** | ```json
{
  "rating": 5,
  "comment": "Excelente atención y rapidez en la solución."
}
``` |

---

## 2.11 Módulo 11: Control y Registro de Tiempos / Time Tracking (`modules/timetracking`)

### MOD-33: Imputar Minutos de Trabajo en una Incidencia
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a técnicos y administradores imputar minutos dedicados a la resolución de un ticket con una breve descripción de la tarea efectuada. |
| **API Endpoints** | • `POST /api/v1/timetracking/tickets/{ticketId}/logs` (`201 Created`)<br>• `GET /api/v1/timetracking/tickets/{ticketId}/logs`<br>• `GET /api/v1/timetracking/tickets/{ticketId}/total-minutes` |
| **Roles & Permisos** | Imputación: `@PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")`. Consulta: Autenticado. |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`TicketTimeLog.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/timetracking/entity/TicketTimeLog.java)<br>2. **Repositorio:** [`TicketTimeLogRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/timetracking/repository/TicketTimeLogRepository.java)<br>3. **DTOs:** [`TimeLogRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/timetracking/dto/TimeLogRequest.java), [`TimeLogResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/timetracking/dto/TimeLogResponse.java)<br>4. **Servicio:** [`TimeTrackingService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/timetracking/service/TimeTrackingService.java)<br>5. **Controlador:** [`TimeTrackingController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/timetracking/controller/TimeTrackingController.java) |
| **Payload Entrada (Request)** | ```json
{
  "minutes": 45,
  "description": "Desinstalación de drivers corruptos y actualización de firmware"
}
``` |

---

## 2.12 Módulo 12: Gestión de Activos e Inventario de Hardware (`modules/asset`)

### MOD-34: Consulta, Alta y Vinculación de Activos IT
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Administra el inventario de hardware institucional (computadores, impresoras, switches):<br>1. Consulta activos asignados a un usuario.<br>2. Consulta activos afectados por un ticket.<br>3. Alta de un nuevo equipo en inventario.<br>4. Enlace directo entre un equipo del inventario y la incidencia en trámite. |
| **API Endpoints** | • `GET /api/v1/assets/user/{username}`<br>• `GET /api/v1/assets/ticket/{ticketId}`<br>• `POST /api/v1/assets` (`hasRole('ADMIN')`)<br>• `POST /api/v1/assets/ticket/{ticketId}/link/{assetId}` (`hasAnyRole('TECHNICIAN', 'ADMIN')`) |
| **Orden Lógico de Archivos** | 1. **Entidades:** [`Asset.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/asset/entity/Asset.java), [`TicketAssetRelation.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/asset/entity/TicketAssetRelation.java)<br>2. **Repositorios:** [`AssetRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/asset/repository/AssetRepository.java), [`TicketAssetRelationRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/asset/repository/TicketAssetRelationRepository.java)<br>3. **DTOs:** [`AssetRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/asset/dto/AssetRequest.java), [`AssetResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/asset/dto/AssetResponse.java)<br>4. **Servicio:** [`AssetService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/asset/service/AssetService.java)<br>5. **Controlador:** [`AssetController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/asset/controller/AssetController.java) |

---

## 2.13 Módulo 13: Etiquetas Taxonómicas / Tags (`modules/tag`)

### MOD-35: Gestión y Asignación de Etiquetas a Tickets
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite etiquetar incidencias con píldoras de colores (`#wifi`, `#bloqueB`, `#urgente`, `#redes`) para facilitar su agrupación temática. |
| **API Endpoints** | • `GET /api/v1/tags`<br>• `POST /api/v1/tags` (`hasAnyRole('ADMIN', 'TECHNICIAN')`)<br>• `POST /api/v1/tags/ticket/{ticketId}/assign/{tagId}`<br>• `GET /api/v1/tags/ticket/{ticketId}` |
| **Orden Lógico de Archivos** | 1. **Entidades:** [`TicketTag.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/tag/entity/TicketTag.java), [`TicketTagAssignment.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/tag/entity/TicketTagAssignment.java)<br>2. **Repositorios:** [`TicketTagRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/tag/repository/TicketTagRepository.java), [`TicketTagAssignmentRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/tag/repository/TicketTagAssignmentRepository.java)<br>3. **DTOs:** [`TagRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/tag/dto/TagRequest.java), [`TagResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/tag/dto/TagResponse.java)<br>4. **Servicio:** [`TicketTagService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/tag/service/TicketTagService.java)<br>5. **Controlador:** [`TagController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/tag/controller/TagController.java) |

---

## 2.14 Módulo 14: Acuerdos de Nivel de Servicio / SLA (`modules/sla`)

### MOD-36: Políticas SLA, Cálculo de Plazos y Detección de Infracciones
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Administra los tiempos máximos permitidos de primera respuesta y resolución según prioridad (Baja, Media, Alta, Crítica). Incluye un planificador de fondo (`SlaBreachScheduler`) que audita cada hora las incidencias abiertas y registra infracciones si se superan los límites comprometidos. |
| **API Endpoints** | • `GET /api/v1/sla/policies`<br>• `POST /api/v1/sla/policies` (`@PreAuthorize("hasRole('ADMIN')")`) |
| **Planificador Programado** | `@Scheduled(fixedRate = 3600000)` en `SlaBreachScheduler` consulta `findBreachedResolutionTickets()` y `findBreachedFirstResponseTickets()`, registrando `SlaViolation` y enviando notificaciones. |
| **Orden Lógico de Archivos** | 1. **Entidades & Enums:** [`SlaPolicy.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/entity/SlaPolicy.java), [`SlaViolation.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/entity/SlaViolation.java), [`SlaPriority.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/enums/SlaPriority.java), [`SlaBreachType.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/enums/SlaBreachType.java)<br>2. **Repositorios:** [`SlaPolicyRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/repository/SlaPolicyRepository.java), [`SlaViolationRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/repository/SlaViolationRepository.java)<br>3. **DTOs:** [`SlaPolicyRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/dto/SlaPolicyRequest.java), [`SlaPolicyResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/dto/SlaPolicyResponse.java)<br>4. **Servicios & Schedulers:** [`SlaService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/service/SlaService.java), [`SlaBreachScheduler.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/service/SlaBreachScheduler.java)<br>5. **Controlador:** [`SlaController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/sla/controller/SlaController.java) |

---

## 2.15 Módulo 15: Auditoría Global del Sistema (`modules/audit`)

### MOD-37: Consulta de Trazas Inmutables de Auditoría
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Consulta el registro inmutable de acciones administrativas y de seguridad (quién hizo qué, sobre qué entidad, cuándo y desde qué IP). |
| **API Endpoints** | • `GET /api/v1/audit?page=0&size=20`<br>• `GET /api/v1/audit/entity/{entityName}/{entityId}?page=0&size=20`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("hasAuthority('AUDIT_READ') or hasRole('SUPERADMIN')")` |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`AuditLog.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/audit/entity/AuditLog.java)<br>2. **Repositorio:** [`AuditLogRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/audit/repository/AuditLogRepository.java)<br>3. **DTO:** [`AuditLogResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/audit/dto/AuditLogResponse.java)<br>4. **Servicio:** [`AuditService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/audit/service/AuditService.java)<br>5. **Controlador:** [`AuditController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/audit/controller/AuditController.java) |

---

## 2.16 Módulo 16: Respuestas Rápidas / Canned Responses (`modules/cannedresponse`)

### MOD-38: Plantillas de Respuestas Predefinidas
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Permite a técnicos y administradores crear y recuperar respuestas estandarizadas frecuentes (ej. "Reinicio de router completado", "Favor enviar captura del error") para agilizar la interacción en los comentarios. |
| **API Endpoints** | • `GET /api/v1/canned-responses`<br>• `POST /api/v1/canned-responses` (`201 Created`) |
| **Roles & Permisos** | `@PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")` |
| **Orden Lógico de Archivos** | 1. **Entidad:** [`CannedResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/cannedresponse/entity/CannedResponse.java)<br>2. **Repositorio:** [`CannedResponseRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/cannedresponse/repository/CannedResponseRepository.java)<br>3. **DTOs:** [`CannedResponseRequest.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/cannedresponse/dto/CannedResponseRequest.java), [`CannedResponseResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/cannedresponse/dto/CannedResponseResponse.java)<br>4. **Servicio:** [`CannedResponseService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/cannedresponse/service/CannedResponseService.java)<br>5. **Controlador:** [`CannedResponseController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/cannedresponse/controller/CannedResponseController.java) |

---

## 2.17 Módulo 17: Dashboard y Métricas de Rendimiento (`modules/dashboard`)

### MOD-39: Estadísticas y Métricas Operativas por Rol
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Genera contadores de tickets discriminados por estado (`OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`), filtrados automáticamente según el rol del usuario que realiza la consulta. |
| **API Endpoints** | `GET /api/dashboard/stats` y `GET /api/reports/summary`<br>• **Códigos HTTP:** `200 OK`, `401 Unauthorized`, `403 Forbidden` |
| **Roles & Permisos** | `@PreAuthorize("isAuthenticated()")` |
| **Orden Lógico de Archivos** | 1. **Modelo Interno:** [`DashboardMetrics.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/model/DashboardMetrics.java)<br>2. **Repositorio:** [`DashboardReportRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/repository/DashboardReportRepository.java)<br>3. **DTO:** [`DashboardMetricsResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/dto/DashboardMetricsResponse.java)<br>4. **Mapper:** [`DashboardMapper.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/mapper/DashboardMapper.java)<br>5. **Servicio:** [`DashboardService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/service/DashboardService.java)<br>6. **Controlador:** [`DashboardController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/dashboard/controller/DashboardController.java) |
| **Reglas de Negocio & Validaciones** | • **ADMIN / SUPERADMIN:** Contadores globales institucionales de todo el sistema.<br>• **TECHNICIAN:** Contadores exclusivos de los tickets donde es el técnico asignado.<br>• **USER:** Contadores exclusivos de los tickets creados por él. |

---

## 2.18 Módulo 18: Generación de Reportes y Exportación CSV (`modules/report`)

### MOD-40: Exportación de Incidencias a CSV
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Genera un archivo descargable con formato CSV (`text/csv`) con el historial de incidencias del solicitante para auditoría o archivo personal. |
| **API Endpoint** | `GET /api/v1/reports/my-tickets/export`<br>• **Headers:** Retorna cabecera `Content-Disposition: attachment; filename="mis_tickets.csv"`<br>• **Códigos HTTP:** `200 OK` |
| **Roles & Permisos** | Autenticado (`Authentication auth`). |
| **Orden Lógico de Archivos** | `Ticket` → `TicketRepository` → `ReportService.java` → `ReportController.java` |

---

## 2.19 Módulo 19: Historial Inmutable de Estados / Status History (`modules/statushistory`)

### MOD-41: Línea de Tiempo del Ciclo de Vida del Ticket
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Devuelve el historial inmutable de cambios de estado y asignaciones que ha atravesado una incidencia desde su creación hasta su cierre definitivo, ordenado cronológicamente. |
| **API Endpoint** | `GET /api/tickets/{ticketId}/history`<br>• **Códigos HTTP:** `200 OK`, `403 Forbidden`, `404 Not Found` |
| **Roles & Permisos** | Autenticado con permisos de visualización sobre el ticket (valida con `TicketAccessPolicy`). |
| **Orden Lógico de Archivos** | 1. **Entidad & Enums:** [`StatusHistory.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/entity/StatusHistory.java), [`StatusHistoryAction.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/enums/StatusHistoryAction.java)<br>2. **Repositorio:** [`StatusHistoryRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/repository/StatusHistoryRepository.java)<br>3. **DTO:** [`StatusHistoryResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/dto/StatusHistoryResponse.java)<br>4. **Mapper:** [`StatusHistoryMapper.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/mapper/StatusHistoryMapper.java)<br>5. **Servicio:** [`StatusHistoryService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/service/StatusHistoryService.java)<br>6. **Controlador:** [`StatusHistoryController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/statushistory/controller/StatusHistoryController.java) |
| **Reglas de Negocio & Validaciones** | Este endpoint es de estricta solo lectura. No existen operaciones `PUT` ni `DELETE` sobre el historial para garantizar su inmutabilidad y valor de prueba legal. |

---

## 2.20 Módulo 20: Centro de Notificaciones In-App (`modules/notification`)

### MOD-42: Centro de Notificaciones y Lectura
| Atributo | Detalle |
| :--- | :--- |
| **¿Qué hace / Propósito?** | Gestiona los avisos y notificaciones internas del usuario (asignación de ticket, nuevo comentario, vencimiento de SLA, resolución). Permite listar no leídas, paginar historial, consultar badge numérico y marcar lectura individual o masiva. |
| **API Endpoints** | • `GET /api/v1/notifications/unread`<br>• `GET /api/v1/notifications?page=0&size=10`<br>• `GET /api/v1/notifications/count`<br>• `PATCH /api/v1/notifications/{id}/read`<br>• `PATCH /api/v1/notifications/read-all` |
| **Roles & Permisos** | Autenticado (`Authentication auth`). |
| **Orden Lógico de Archivos** | 1. **Entidad & Enums:** [`Notification.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/notification/entity/Notification.java), [`NotificationType.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/notification/enums/NotificationType.java), [`NotificationChannel.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/notification/enums/NotificationChannel.java)<br>2. **Repositorio:** [`NotificationRepository.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/notification/repository/NotificationRepository.java)<br>3. **DTO:** [`NotificationResponse.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/notification/dto/NotificationResponse.java)<br>4. **Servicio:** [`NotificationService.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/notification/service/NotificationService.java)<br>5. **Controlador:** [`NotificationController.java`](file:///c:/Users/sergi/OneDrive/Desktop/springboot/PROYECTO/CAMPUSDESK/spring-security-user-role-management/src/main/java/com/roles/usermanagement/modules/notification/controller/NotificationController.java) |

---

# 📊 3. MATRIZ CONSOLIDADA DE ENDPOINTS Y FUNCIONALIDADES

| Código | Módulo | Funcionalidad | Método | Ruta / Path | Rol / Autorización Requerida | Entidad Clave | Servicio Involucrado |
| :--- | :--- | :--- | :---: | :--- | :--- | :--- | :--- |
| **SEC-01** | Seguridad | Registro de Usuario | `POST` | `/api/auth/register` | `permitAll()` (Público) | `UserEntity` | `UserService` |
| **SEC-02** | Seguridad | Inicio de Sesión y JWT | `POST` | `/api/auth/login` | `permitAll()` (Público) | `UserEntity` | `UserSecurityService` / `JwtUtil` |
| **SEC-03** | Seguridad | Cambio de Contraseña | `POST` | `/api/auth/change-password` | Autenticado | `UserEntity` | `UserService` |
| **SEC-04** | Seguridad | Sesión Actual y Permisos | `GET` | `/api/auth/me` | Autenticado | `UserEntity` | `UserRepository` |
| **SEC-05** | Seguridad | Listar Usuarios | `GET` | `/api/user/all` | `USER_READ` | `UserEntity` | `UserService` |
| **SEC-06** | Seguridad | Crear Usuario | `POST` | `/api/user/add` | `USER_CREATE` + `ROLE_ASSIGN` | `UserEntity` | `UserService` |
| **SEC-07** | Seguridad | Actualizar Usuario | `PUT` | `/api/user/update` | `USER_UPDATE` + `ROLE_ASSIGN` | `UserEntity` | `UserService` |
| **SEC-08** | Seguridad | Asignar Rol Único | `POST` | `/api/user/assignRole` | `ROLE_ASSIGN` | `UserRoleEntity` | `UserService` |
| **SEC-09** | Seguridad | Conceder Permiso | `POST` | `/api/user/assignPermission` | `PERMISSION_ASSIGN` | `PermissionEntity` | `UserService` |
| **SEC-10** | Seguridad | Revocar Permiso | `DELETE`| `/api/user/{username}/permissions/{p}`| `PERMISSION_ASSIGN` | `PermissionEntity` | `UserService` |
| **SEC-11** | Seguridad | Permisos de Usuario | `GET` | `/api/user/{username}/permissions` | `USER_READ` | `UserEntity` | `UserService` |
| **SEC-12** | Seguridad | Catálogo de Permisos | `GET` | `/api/user/permissions` | `PERMISSION_ASSIGN` | `PermissionEntity` | `UserService` |
| **SEC-13** | Seguridad | Eliminar Usuario | `DELETE`| `/api/user/delete/{name}` | `USER_DELETE` | `UserEntity` | `UserService` |
| **SEC-14** | Seguridad | Restablecer Password Admin | `POST` | `/api/user/{username}/reset-password` | `USER_UPDATE` o `ROLE_ADMIN` | `UserEntity` | `UserService` |
| **SEC-15** | Seguridad | Consultar Perfil Propio | `GET` | `/api/user/me/profile` | Autenticado | `UserEntity` | `UserService` |
| **SEC-16** | Seguridad | Actualizar Perfil Propio | `PUT` | `/api/user/me/profile` | Autenticado | `UserEntity` | `UserService` |
| **SEC-17** | Seguridad | Bloqueo Masivo Cuentas | `POST` | `/api/user/bulk-lock` | `ROLE_ADMIN` | `UserEntity` | `UserService` |
| **SEC-18** | Seguridad | Listar Catálogo Roles | `GET` | `/api/roles` | `ROLE_MANAGE` | `RoleEntity` | `SecurityCatalogService` |
| **SEC-19** | Seguridad | Crear Rol en Catálogo | `POST` | `/api/roles` | `ROLE_MANAGE` | `RoleEntity` | `SecurityCatalogService` |
| **SEC-20** | Seguridad | Listar/Crear Permisos | `GET`/`POST`| `/api/permissions` | `PERMISSION_MANAGE` | `PermissionEntity` | `SecurityCatalogService` |
| **SEC-21** | Seguridad | Vincular/Desvincular Permiso|`PUT`/`DEL`| `/api/roles/{r}/permissions/{p}` | `ROLE_MANAGE` + `PERMISSION_MANAGE` | `RoleEntity` | `SecurityCatalogService` |
| **MOD-01** | Tickets | Crear Ticket Propio | `POST` | `/api/tickets` | `ROLE_USER` | `Ticket` | `TicketService` |
| **MOD-02** | Tickets | Crear Ticket On-Behalf | `POST` | `/api/tickets/on-behalf` | `ROLE_TECHNICIAN` o `ROLE_ADMIN` | `Ticket` | `TicketService` |
| **MOD-03** | Tickets | Listar Tickets con Filtros | `GET` | `/api/tickets` | Autenticado (Scope por Rol) | `Ticket` | `TicketService` |
| **MOD-04** | Tickets | Listar Tickets Paginados | `GET` | `/api/tickets/paginated` | Autenticado (Scope por Rol) | `Ticket` | `TicketService` |
| **MOD-05** | Tickets | Ver Detalle de Incidencia | `GET` | `/api/tickets/{id}` | Autenticado (Verifica Acceso) | `Ticket` | `TicketService` |
| **MOD-06** | Tickets | Modificar Incidencia Propia| `PUT` | `/api/tickets/{id}` | `ROLE_USER` (Solo en `OPEN`) | `Ticket` | `TicketService` |
| **MOD-07** | Tickets | Cancelar Ticket Propio | `POST` | `/api/tickets/{id}/cancel` | Solicitante o Admin | `Ticket` | `TicketService` |
| **MOD-08** | Tickets | Reabrir Incidencia | `POST` | `/api/tickets/{id}/reopen` | Solicitante o Admin | `Ticket` | `TicketService` |
| **MOD-09** | Tickets | Clonar Ticket Plantilla | `POST` | `/api/tickets/{id}/clone` | Autenticado con acceso | `Ticket` | `TicketService` |
| **MOD-10** | Tickets | Solicitar Escalación | `POST` | `/api/tickets/{id}/escalate` | Autenticado con acceso | `Ticket` | `TicketService` |
| **MOD-11** | Tickets | Eliminar Incidencia Definitiva|`DELETE`| `/api/tickets/{id}` | `TICKET_DELETE` o `SUPERADMIN` | `Ticket` | `TicketService` |
| **MOD-12** | Tickets | Subtareas | Serv. | Métodos internos de servicio | `TECHNICIAN` / `ADMIN` | `TicketSubtask` | `TicketSubtaskService` |
| **MOD-13** | Ciclo Vida | Transición de Estado | `POST`/`PATCH`| `/api/tickets/{id}/transitions` o `/status`| Dinámico según regla máquina | `LifecycleRule`, `Ticket` | `TicketLifecycleService` |
| **MOD-14** | Asignación| Asignación Inicial Manual | `PUT`/`PATCH`| `/api/tickets/{id}/assign` | `ROLE_ADMIN` | `TicketAssignmentLog` | `TicketAssignmentService` |
| **MOD-15** | Asignación| Reasignación Justificada | `PUT` | `/api/tickets/{id}/reassign` | `ROLE_ADMIN` | `TicketAssignmentLog` | `TicketAssignmentService` |
| **MOD-16** | Asignación| Auto-Asignación Técnico | `POST` | `/api/tickets/{id}/self-assign` | `ROLE_TECHNICIAN` | `TicketAssignmentLog` | `TicketAssignmentService` |
| **MOD-17** | Asignación| Asignación Automática Carga| `POST` | `/api/tickets/{id}/auto-assign` | `ROLE_ADMIN` o `ROLE_TECHNICIAN` | `TechnicianProfile` | `TicketAssignmentService` |
| **MOD-18** | Asignación| Reasignación Masiva | `POST` | `/api/tickets/bulk-reassign` | `ROLE_ADMIN` | `TicketAssignmentLog` | `TicketAssignmentService` |
| **MOD-19** | Comentarios| Crear Comentario / Nota | `POST` | `/api/tickets/{id}/comments` | Autenticado (`INTERNAL` solo Tech/Admin)| `Comment` | `CommentService` |
| **MOD-20** | Comentarios| Listar Comentarios Ticket | `GET` | `/api/tickets/{id}/comments` | Autenticado (Filtra `INTERNAL`) | `Comment` | `CommentService` |
| **MOD-21** | Adjuntos | Cargar Metadatos Adjunto | `POST` | `/api/v1/attachments/ticket/{id}` | Autenticado con acceso | `TicketAttachment` | `AttachmentService` |
| **MOD-22** | Adjuntos | Listar Adjuntos Ticket | `GET` | `/api/v1/attachments/ticket/{id}` | Autenticado con acceso | `TicketAttachment` | `AttachmentService` |
| **MOD-23** | Técnicos | Directorio General | `GET` | `/api/technicians` | `ROLE_ADMIN` | `TechnicianProfile` | `TechnicianService` |
| **MOD-24** | Técnicos | Disponibles por Menor Carga| `GET` | `/api/technicians/available` | `ROLE_ADMIN` | `TechnicianProfile` | `TechnicianService` |
| **MOD-25** | Catálogo | Consultar Catálogo Activo | `GET` | `/api/v1/service-catalog/items` | Autenticado | `ServiceCatalogItem` | `ServiceCatalogService` |
| **MOD-26** | Catálogo | Crear Ítem de Servicio | `POST` | `/api/v1/service-catalog/items` | `ROLE_ADMIN` | `ServiceCatalogItem` | `ServiceCatalogService` |
| **MOD-27** | KB / FAQ | Búsqueda y Sugerencias KB | `GET` | `/api/v1/kb/articles/**` | `permitAll()` (Público) | `KbArticle` | `KnowledgeBaseService` |
| **MOD-28** | KB / FAQ | Votar Utilidad Artículo | `POST` | `/api/v1/kb/articles/{id}/vote` | Público / Autenticado | `KbArticle` | `KnowledgeBaseService` |
| **MOD-29** | KB / FAQ | Crear Artículo de KB | `POST` | `/api/v1/kb/articles` | `ROLE_ADMIN` o `ROLE_TECHNICIAN` | `KbArticle` | `KnowledgeBaseService` |
| **MOD-30** | Anuncios | Ver Avisos Activos | `GET` | `/api/v1/announcements/active` | `permitAll()` (Público) | `Announcement` | `AnnouncementService` |
| **MOD-31** | Anuncios | Publicar Anuncio | `POST` | `/api/v1/announcements` | `ROLE_ADMIN` | `Announcement` | `AnnouncementService` |
| **MOD-32** | Satisfacción| Encuesta CSAT | `POST`/`GET`| `/api/v1/satisfaction/**` | Solicitante (Post), Admin/Tech (Avg) | `TicketSatisfaction` | `SatisfactionService` |
| **MOD-33** | Tiempo | Registro de Horas / Tiempos| `POST`/`GET`| `/api/v1/timetracking/**` | `ROLE_TECHNICIAN` o `ROLE_ADMIN` | `TicketTimeLog` | `TimeTrackingService` |
| **MOD-34** | Activos | Inventario Hardware | `POST`/`GET`| `/api/v1/assets/**` | `ROLE_ADMIN` o `ROLE_TECHNICIAN` | `Asset` | `AssetService` |
| **MOD-35** | Tags | Catálogo y Asignar Tags | `POST`/`GET`| `/api/v1/tags/**` | `ROLE_ADMIN` o `ROLE_TECHNICIAN` | `TicketTag` | `TicketTagService` |
| **MOD-36** | SLA | Políticas y Detección Breach| `POST`/`GET`| `/api/v1/sla/policies` + Scheduler | `ROLE_ADMIN` (Creación) | `SlaPolicy`, `SlaViolation`| `SlaService`, `SlaBreachScheduler`|
| **MOD-37** | Auditoría | Trazas Inmutables | `GET` | `/api/v1/audit/**` | `AUDIT_READ` o `SUPERADMIN` | `AuditLog` | `AuditService` |
| **MOD-38** | Respuestas | Plantillas Predefinidas | `POST`/`GET`| `/api/v1/canned-responses` | `ROLE_TECHNICIAN` o `ROLE_ADMIN` | `CannedResponse` | `CannedResponseService` |
| **MOD-39** | Dashboard | Métricas Operativas | `GET` | `/api/dashboard/stats` | Autenticado (Scope por Rol) | N/A (Consultas agregadas) | `DashboardService` |
| **MOD-40** | Reportes | Exportar CSV de Tickets | `GET` | `/api/v1/reports/my-tickets/export`| Autenticado | `Ticket` | `ReportService` |
| **MOD-41** | Historial | Línea de Tiempo Estados | `GET` | `/api/tickets/{id}/history` | Autenticado con acceso | `StatusHistory` | `StatusHistoryService` |
| **MOD-42** | Notificaciones| Avisos In-App y Lectura | `GET`/`PATCH`| `/api/v1/notifications/**` | Autenticado | `Notification` | `NotificationService` |
