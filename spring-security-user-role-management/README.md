# CAMPUSDESK - Sistema de Gestion de Soporte y Mesa de Ayuda

Backend desarrollado en Java 21 con Spring Boot y PostgreSQL para la gestion centralizada de incidencias, tickets de soporte, asignacion de tecnicos, seguimiento de estados y control de acceso basado en roles (RBAC) con politicas avanzadas de contraseñas.

---

## Tabla de Contenido

- [Informacion del Proyecto](#informacion-del-proyecto)
- [Integrantes del Equipo](#integrantes-del-equipo)
- [Creditos y Reconocimientos](#creditos-y-reconocimientos)
- [Objetivo y Alcance](#objetivo-y-alcance)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Modulos del Backend](#modulos-del-backend)
- [Politicas de Seguridad y Control de Acceso](#politicas-de-seguridad-y-control-de-acceso)
  - [Modelo RBAC y Permisos Individuales](#modelo-rbac-y-permisos-individuales)
  - [Autenticacion con JWT](#autenticacion-con-jwt)
  - [Ciclo de Vida de Contraseñas](#ciclo-de-vida-de-contraseñas)
- [Configuracion y Perfiles](#configuracion-y-perfiles)
- [Requisitos Previos e Instalacion](#requisitos-previos-e-instalacion)
- [Instrucciones de Ejecucion](#instrucciones-de-ejecucion)
- [Catalogo de Endpoints REST](#catalogo-de-endpoints-rest)
- [Estructura del Repositorio](#estructura-del-repositorio)
- [Documentacion Interactiva (Swagger/OpenAPI)](#documentacion-interactiva-swaggeropenapi)

---

## Informacion del Proyecto

- **Nombre del Proyecto:** CAMPUSDESK
- **Tipo de Aplicacion:** API RESTful Backend
- **Version:** 0.0.1-SNAPSHOT
- **Lenguaje Base:** Java 21 LTS
- **Framework:** Spring Boot 4.1.1
- **Base de Datos:** PostgreSQL
- **Seguridad:** Spring Security con JSON Web Tokens (HMAC256)

---

## Integrantes del Equipo

Proyecto desarrollado y mantenido por los siguientes integrantes:

- **Sergio Aparicio**
- **Jhon Reyes**
- **Sebastian Ayala**
- **Juan Diego Leon**

---

## Creditos y Reconocimientos

Este proyecto toma como base arquitectonica y modular la plantilla **NEXO / spring-security-user-role-management**, la cual provee la estructura inicial para:

- Gestion desacoplada de seguridad con Spring Security y filtros de autenticacion sin estado (Stateless).
- Implementacion de tokens JWT mediante la libreria Auth0 JWT con algoritmo HMAC256.
- Manejo integral de usuarios, asignacion de un rol unico y extension dinamica de permisos individuales.
- Inicializacion automatica del catalogo de permisos y roles a traves del servicio de bootstrap (`SecurityBootstrapService`).
- Capa de persistencia relacional con Spring Data JPA y soporte para palabras reservadas en PostgreSQL.

A partir de dicha plantilla, el equipo extendio la solucion para incorporar los modulos de negocio propios de **CAMPUSDESK** (tickets, tecnicos, asignaciones, historial de estados, ciclo de vida y panel de control), ademas de incorporar politicas de primer ingreso con cambio forzado de clave y restablecimiento administrativo.

---

## Objetivo y Alcance

CAMPUSDESK fue concebido como una plataforma institucional para gestionar incidentes y requerimientos tecnicos dentro de un campus universitario o corporativo. 

### Principales capacidades del sistema:
1. **Mesa de Ayuda y Tickets:** Registro, clasificacion, seguimiento y resolucion de solicitudes tecnicas.
2. **Asignacion de Personal:** Vinculacion de incidencias a tecnicos especializados con base en disponibilidad y categoria.
3. **Trazabilidad y Ciclo de Vida:** Registro cronologico de transiciones de estado (Abierto, En Progreso, Resuelto, Cerrado, Cancelado) y bitacora de comentarios.
4. **Control de Acceso Estricto:** Restriccion de endpoints segun roles institucionales (Administrador, Tecnico, Usuario/Solicitante) y validacion dinamica de autoridades.
5. **Seguridad de Cuentas:** Gestion controlada por el Super Administrador con activacion de contraseñas temporales y reseteo asistido.

---

## Tecnologias Utilizadas

### Nucleo y Frameworks
- **Java 21 LTS:** Lenguaje principal de programacion, aprovechando caracteristicas modernas de sintaxis y rendimiento.
- **Spring Boot 4.1.1:** Ecosistema para configuracion automatica, inyeccion de dependencias y despliegue del servidor embebido.
- **Spring Security:** Framework para autorizacion, autenticacion declarativa con `@PreAuthorize` y cadena de filtros.
- **Spring Data JPA / Hibernate:** Abstraccion de la capa de persistencia relacional con generacion de esquema `ddl-auto=update`.

### Persistencia y Seguridad
- **PostgreSQL Driver:** Controlador JDBC para comunicacion con la base de datos relacional PostgreSQL.
- **Java JWT (com.auth0:java-jwt):** Generacion, firma HMAC256 y validacion criptografica de tokens Bearer.
- **BCrypt Password Encoder:** Hashing criptografico unidireccional para contraseñas de usuarios.

### Herramientas de Desarrollo y Productividad
- **MapStruct:** Mapeo de alto rendimiento en tiempo de compilacion entre Entidades JPA y DTOs.
- **Lombok:** Generacion automatica de constructores, getters, setters y metodos de datos para reducir codigo boilerplate.
- **Gradle Wrapper 8.14.3:** Gestion de dependencias y automatizacion de tareas de construccion (build).
- **Springdoc OpenAPI 3:** Generacion automatica de la especificacion OpenAPI y visualizacion interactiva en Swagger UI.

---

## Arquitectura del Sistema

El proyecto sigue una arquitectura en capas basada en principios de diseño limpio y separacion de responsabilidades:

```text
[ Cliente Web / Postman / Swagger UI ]
                 │
                 ▼
       ┌──────────────────┐
       │   JwtFilter      │  (Filtro de seguridad por peticion)
       └─────────┬────────┘
                 │
                 ▼
       ┌──────────────────┐
       │  Capa Web        │  (Controladores REST, DTOs, OpenAPI)
       │  (Controllers)   │
       └─────────┬────────┘
                 │
                 ▼
       ┌──────────────────┐
       │  Capa de Dominio │  (Servicios de negocio, validaciones)
       │  (Services)      │
       └─────────┬────────┘
                 │
                 ▼
       ┌──────────────────┐
       │ Capa Persistencia│  (Entidades JPA, CrudRepositories, Mappers)
       │ (Repositories)   │
       └─────────┬────────┘
                 │
                 ▼
       ┌──────────────────┐
       │ Base de Datos    │  (PostgreSQL - Tablas relacionales)
       └──────────────────┘
```

---

## Modulos del Backend

El codigo fuente se organiza en dos nucleos principales:

### 1. Nucleo de Seguridad y Administracion (`com.roles.usermanagement`)
- **Autenticacion (`web/controller/AuthController`):** Login, emision de JWT, cambio obligatorio/voluntario de contraseña y consulta de sesion actual (`/me`).
- **Gestion de Usuarios (`web/controller/UserController`):** Altas, modificaciones, eliminaciones, asignacion de rol unico y consulta de permisos efectivos.
- **Catalogos de Seguridad (`web/controller/SecurityCatalogController`):** Administracion del catalogo dinamico de roles y permisos del sistema.
- **Bootstrap (`domain/service/SecurityBootstrapService`):** Creacion inicial automatica de permisos base y de la cuenta `superadmin` al iniciar la aplicacion.

### 2. Modulos de Mesa de Ayuda (`com.roles.usermanagement.modules`)
- **Ticket (`modules/ticket`):** Entidad central de incidencia con titulo, descripcion, prioridad, estado y solicitante.
- **Assignment (`modules/assignment`):** Vinculacion entre un ticket y el tecnico encargado de su gestion.
- **Technician (`modules/technician`):** Perfil de los operadores tecnicos, especialidad y carga de trabajo.
- **Comment (`modules/comment`):** Historial de notas publicas y privadas dentro de un ticket.
- **Status History (`modules/statushistory`):** Auditoria cronologica de los cambios de estado de cada ticket.
- **Lifecycle (`modules/lifecycle`):** Maquina de estados que valida que transiciones de estado son validas.
- **Dashboard (`modules/dashboard`):** Consultas agregadas y estadisticas para administradores y coordinadores.

---

## Politicas de Seguridad y Control de Acceso

### Modelo RBAC y Permisos Individuales

El sistema implementa una combinacion hibrida de control de acceso:
- Cada usuario posee **exactamente un rol principal** asignado (por ejemplo, `ADMIN`, `TECHNICIAN`, `CUSTOMER`).
- Un rol hereda una coleccion predeterminada de permisos desde la tabla `role_permission`.
- De manera complementaria, el administrador puede conceder o revocar **permisos individuales adicionales** directamente a un usuario.
- **Permisos Efectivos:** Se calculan dinamicamente en cada peticion como la union de los permisos heredados del rol mas los permisos individuales asignados:
  ```text
  Permisos Efectivos = Permisos del Rol U Permisos Individuales
  ```

### Autenticacion con JWT

- **Tipo de Token:** Bearer Token bajo el estandar RFC 7519.
- **Algoritmo:** HMAC256 con clave secreta configurable (`security.jwt.secret`).
- **Expiracion Predeterminada:** 15 dias en entorno de desarrollo (`application-dev.properties`), configurable mediante la propiedad `security.jwt.expiration`.
- **Estructura:** El token almacena el nombre de usuario (`subject`), fecha de emision y fecha de expiracion.
- **Validacion:** En cada peticion entrante, `JwtFilter` valida la firma del token y recarga el estado del usuario en PostgreSQL. Si la cuenta fue bloqueada o deshabilitada posteriormente, el acceso se interrumpe de inmediato.

### Ciclo de Vida de Contraseñas

Con el objetivo de garantizar la seguridad en entornos academicos y productivos sin requerir servidores de correo electronico externos, se aplico el siguiente ciclo de vida:

1. **Creacion por Super Admin:**
   - El administrador crea la cuenta mediante `POST /api/user/add` especificando datos basicos y una contraseña temporal.
   - El sistema guarda la cuenta con la bandera `mustChangePassword = true`.
2. **Primer Ingreso:**
   - El usuario inicia sesion en `POST /api/auth/login`.
   - La respuesta devuelve un objeto JSON estructurado:
     ```json
     {
       "token": "eyJhbGciOi...",
       "username": "usuario_ejemplo",
       "mustChangePassword": true
     }
     ```
   - El frontend detecta `mustChangePassword: true` y restringe la navegacion, obligando al usuario a redirigirse a la vista de actualizacion de clave.
3. **Cambio Obligatorio:**
   - El usuario consume `POST /api/auth/change-password` enviando su contraseña actual y la nueva contraseña definitiva (minimo 6 caracteres y distinta a la anterior).
   - El backend valida la clave con BCrypt, actualiza el registro y apaga la bandera (`mustChangePassword = false`).
4. **Restablecimiento Asistido por el Super Admin:**
   - En caso de olvido o perdida de acceso, el Super Admin ejecuta `POST /api/user/{username}/reset-password`.
   - El endpoint asigna una nueva contraseña temporal, desbloquea la cuenta si estaba bloqueada y reactiva `mustChangePassword = true`.

---

## Configuracion y Perfiles

La aplicacion maneja su configuracion mediante archivos de propiedades de Spring Boot:

| Archivo | Proposito |
|---|---|
| `src/main/resources/application.properties` | Define el perfil activo (`spring.profiles.active=dev`). |
| `src/main/resources/application-dev.properties` | Configuracion principal para desarrollo local (PostgreSQL, puerto 8050, JWT y bootstrap). |
| `src/test/resources/application-test.properties` | Configuracion independiente con base de datos H2 en memoria para pruebas unitarias. |

### Variables de Entorno Soportadas

Las siguientes variables de entorno permiten sobreescribir la configuracion sin modificar el codigo fuente:

| Variable | Descripcion | Valor Predeterminado en Dev |
|---|---|---|
| `DB_URL` | URL de conexion JDBC a PostgreSQL | `jdbc:postgresql://localhost:5432/sistema_gestion_usuarios` |
| `DB_USERNAME` | Usuario de PostgreSQL | `admin` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `admin123` |
| `JWT_SECRET` | Clave secreta para firma HMAC256 | `User_M4n4gement` |
| `JWT_ISSUER` | Emisor registrado en el token JWT | `User_R0les_M4n4gement` |
| `JWT_EXPIRATION` | Vigencia del token (`30m`, `8h`, `15d`) | `15d` |
| `ADMIN_USERNAME` | Nombre de usuario del Super Admin inicial | `superadmin` |
| `ADMIN_EMAIL` | Correo electronico del Super Admin inicial | `superadmin@localhost` |
| `ADMIN_PASSWORD` | Contraseña inicial del Super Admin | `SuperAdmin123!` |
| `BOOTSTRAP_ADMIN_ENABLED` | Habilita/deshabilita la creacion del Super Admin | `true` |

---

## Requisitos Previos e Instalacion

### Requisitos de Software
- **Java Development Kit (JDK):** Version 21 o superior instalada y configurada en la variable `JAVA_HOME`.
- **PostgreSQL:** Servidor activo en el puerto 5432 (o el configurado en tu entorno).
- **Git:** Para control de versiones.
- **Terminal:** PowerShell (Windows) o Bash (Linux / macOS).

### 1. Clonar el Repositorio
```bash
git clone <URL_DEL_REPOSITORIO>
cd spring-security-user-role-management
```

### 2. Crear la Base de Datos en PostgreSQL
Accede a tu cliente de PostgreSQL (`psql` o pgAdmin) y ejecuta:

```sql
CREATE DATABASE sistema_gestion_usuarios;
```

---

## Instrucciones de Ejecucion

### Ejecucion en Modo Desarrollo (PowerShell)

Asegurate de que las credenciales de conexion coincidan con las de tu PostgreSQL:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/sistema_gestion_usuarios"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "tu_password_de_postgresql"

.\gradlew.bat bootRun
```

### Ejecucion en Entornos Linux / macOS (Bash)

```bash
export DB_URL="jdbc:postgresql://localhost:5432/sistema_gestion_usuarios"
export DB_USERNAME="postgres"
export DB_PASSWORD="tu_password_de_postgresql"

./gradlew bootRun
```

El servidor iniciara por defecto en el puerto **8050**:
```text
http://localhost:8050
```

### Generacion del Archivo Ejecutable (JAR)

Para compilar y empaquetar el proyecto en un archivo ejecutable:

```powershell
.\gradlew.bat clean test bootJar
java -jar build/libs/usermanagement-0.0.1-SNAPSHOT.jar
```

---

## Catalogo de Endpoints REST

A continuacion se presenta la relacion de endpoints principales disponibles en la aplicacion:

### 1. Modulo de Autenticacion (`/api/auth`)

| Metodo | Ruta | Acceso / Permiso | Descripcion |
|---|---|---|---|
| `POST` | `/api/auth/login` | Publico | Autentica credenciales y devuelve el token JWT con la bandera `mustChangePassword`. |
| `POST` | `/api/auth/change-password` | Usuario Autenticado | Permite cambiar la contraseña (obligatoria en primer ingreso o voluntaria). |
| `GET` | `/api/auth/me` | Usuario Autenticado | Consulta el rol, permisos heredados, permisos individuales y permisos efectivos de la sesion. |

### 2. Modulo de Administracion de Usuarios (`/api/user`)

| Metodo | Ruta | Acceso / Permiso | Descripcion |
|---|---|---|---|
| `GET` | `/api/user/all` | `USER_READ` | Lista la totalidad de usuarios registrados en el sistema. |
| `POST` | `/api/user/add` | `USER_CREATE` | Registra un nuevo usuario. Por defecto se marca con `mustChangePassword = true`. |
| `PUT` | `/api/user/update` | `USER_UPDATE` | Actualiza atributos del usuario (correo, estado locked, disabled o rol). |
| `DELETE` | `/api/user/delete/{name}` | `USER_DELETE` | Elimina fisicamente una cuenta de usuario. |
| `POST` | `/api/user/{username}/reset-password` | `USER_UPDATE` o `ADMIN` | Restablece la contraseña de un usuario con una clave temporal y reactiva el cambio obligatorio. |
| `POST` | `/api/user/assignRole` | `ROLE_ASSIGN` | Modifica el rol unico del usuario. |
| `POST` | `/api/user/assignPermission` | `PERMISSION_ASSIGN` | Concede un permiso individual adicional al usuario. |
| `DELETE` | `/api/user/{username}/permissions/{permission}` | `PERMISSION_ASSIGN` | Revoca un permiso individual otorgado a la cuenta. |
| `GET` | `/api/user/{username}/permissions` | `USER_READ` | Obtiene el detalle de permisos heredados y efectivos de un usuario especifico. |
| `GET` | `/api/user/permissions` | `PERMISSION_ASSIGN` | Lista el catalogo completo de nombres de permisos disponibles. |

### 3. Modulo de Catalogos de Seguridad (`/api`)

| Metodo | Ruta | Acceso / Permiso | Descripcion |
|---|---|---|---|
| `GET` | `/api/roles` | `ROLE_MANAGE` | Lista todos los roles existentes y sus permisos asignados. |
| `POST` | `/api/roles` | `ROLE_MANAGE` | Crea un nuevo rol en el sistema con su lista inicial de permisos. |
| `GET` | `/api/permissions` | `PERMISSION_MANAGE` | Consulta la lista de todas las autoridades base y personalizadas. |
| `POST` | `/api/permissions` | `PERMISSION_MANAGE` | Da de alta una nueva autoridad/permiso en el catalogo. |
| `PUT` | `/api/roles/{role}/permissions/{permission}` | `ROLE_MANAGE` y `PERMISSION_MANAGE` | Asocia un permiso existente a un rol. |
| `DELETE` | `/api/roles/{role}/permissions/{permission}` | `ROLE_MANAGE` y `PERMISSION_MANAGE` | Desvincula un permiso de un rol determinado. |

---

## Estructura del Repositorio

La distribucion del codigo fuente sigue las convenciones estandar de proyectos Spring Boot:

```text
spring-security-user-role-management/
│
├── .gradle/                                 # Cache y configuraciones internas de Gradle
├── gradle/wrapper/                          # Binarios y propiedades del Gradle Wrapper
├── build.gradle                             # Archivo de dependencias y plugins de Gradle
├── settings.gradle                          # Configuracion del nombre del proyecto
├── gradlew                                  # Script de ejecucion para sistemas Unix/Linux
├── gradlew.bat                              # Script de ejecucion para sistemas Windows
├── README.md                                # Documentacion principal del proyecto
│
└── src/
    ├── main/
    │   ├── java/com/roles/usermanagement/
    │   │   ├── UsermanagementApplication.java   # Clase principal de arranque de Spring Boot
    │   │   │
    │   │   ├── domain/                          # Capa de Dominio (Logica central)
    │   │   │   ├── dto/                         # Data Transfer Objects (Login, User, Roles)
    │   │   │   ├── repository/                  # Interfaces de repositorio de dominio
    │   │   │   └── service/                     # Servicios (UserService, SecurityBootstrap)
    │   │   │
    │   │   ├── modules/                         # Modulos de Negocio CAMPUSDESK
    │   │   │   ├── assignment/                  # Asignacion de tecnicos a incidencias
    │   │   │   ├── comment/                     # Comentarios en los tickets
    │   │   │   ├── dashboard/                   # Metricas y panel administrativo
    │   │   │   ├── lifecycle/                   # Flujo y transiciones de estado
    │   │   │   ├── statushistory/               # Auditoria historica de estados
    │   │   │   ├── technician/                  # Informacion operativa de tecnicos
    │   │   │   └── ticket/                      # Gestion central de tickets de soporte
    │   │   │
    │   │   ├── persistance/                     # Capa de Persistencia
    │   │   │   ├── crud/                        # Interfaces Spring Data CrudRepository
    │   │   │   ├── entity/                      # Entidades JPA (UserEntity, RoleEntity, etc.)
    │   │   │   ├── mapper/                      # Mappers de MapStruct
    │   │   │   └── repository/                  # Implementaciones de repositorios de persistencia
    │   │   │
    │   │   └── web/                             # Capa Web
    │   │       ├── config/                      # Seguridad, JWT, CORS, OpenAPI, Bootstrap
    │   │       └── controller/                  # Controladores REST expuestos
    │   │
    │   └── resources/
    │       ├── application.properties           # Perfil activo
    │       ├── application-dev.properties       # Configuracion de desarrollo y base de datos
    │       └── banner.txt                       # Banner institucional de arranque
    │
    └── test/                                    # Pruebas automatizadas y recursos de test
```

---

## Documentacion Interactiva (Swagger/OpenAPI)

Con la aplicacion en ejecucion, es posible explorar y probar interactivamente cada uno de los endpoints mediante la interfaz grafica de Swagger UI:

- **Swagger UI:**  
  `http://localhost:8050/swagger-ui.html`

- **Especificacion OpenAPI en JSON:**  
  `http://localhost:8050/v3/api-docs`

- **Especificacion OpenAPI en YAML:**  
  `http://localhost:8050/v3/api-docs.yaml`

### Procedimiento para Probar Endpoints Protegidos en Swagger UI

1. Ingresa a la interfaz web de Swagger UI en tu navegador.
2. Localiza el endpoint `POST /api/auth/login` y pulsa sobre **Try it out**.
3. Envia las credenciales de administrador:
   ```json
   {
     "username": "superadmin",
     "password": "SuperAdmin123!"
   }
   ```
4. Copia el token recibido dentro del campo `token` de la respuesta JSON.
5. Sube a la parte superior derecha de la pagina y haz clic en el boton **Authorize** (marcado con un candado).
6. Pega el valor del token en el campo de texto y pulsa **Authorize**.
7. A partir de ese momento, todas las solicitudes ejecutadas desde Swagger incluiran el encabezado `Authorization: Bearer <token>`, permitiendo evaluar las rutas protegidas.

---

## Licencia

Este proyecto fue desarrollado con fines academicos para la asignatura correspondiente. El uso de la plantilla base **NEXO / spring-security-user-role-management** se realiza bajo fines educativos y de integracion de arquitecturas seguras en Java y Spring Boot.
