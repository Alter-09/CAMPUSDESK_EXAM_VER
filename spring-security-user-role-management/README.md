# CAMPUSDESK - Plataforma de Soporte Tecnico y Mesa de Ayuda

Backend robusto desarrollado en Java 21 y Spring Boot para la administracion integral de incidentes, mesa de ayuda, asignacion de personal tecnico y control de acceso institucional en entornos de campus universitario o corporativo.

---

## Ficha del Proyecto

- **Nombre del Sistema:** CAMPUSDESK
- **Modulo:** API REST Backend
- **Estado:** En desarrollo activo
- **Lenguaje Base:** Java 21 LTS
- **Framework:** Spring Boot 4.1.1
- **Base de Datos:** PostgreSQL
- **Seguridad y Tokens:** Spring Security 6 con JWT (HMAC256)
- **Documentacion Interactiva:** Swagger UI / OpenAPI 3

---

## Creadores e Integrantes del Equipo

Sistema desarrollado por:

- **Sergio Aparicio**
- **Jhon Reyes**
- **Sebastian Ayala**
- **Juan Diego Leon**

---

## Tabla de Contenido

- [Presentacion de CAMPUSDESK](#presentacion-de-campusdesk)
- [Funcionalidades Implementadas](#funcionalidades-implementadas)
- [Modulos del Sistema](#modulos-del-sistema)
- [Flujo de Seguridad y Ciclo de Vida de Contraseñas](#flujo-de-seguridad-y-ciclo-de-vida-de-contraseñas)
- [Arquitectura de Software](#arquitectura-de-software)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Catalogo de Endpoints REST](#catalogo-de-endpoints-rest)
- [Configuracion y Variables de Entorno](#configuracion-y-variables-de-entorno)
- [Requisitos Previos e Instalacion](#requisitos-previos-e-instalacion)
- [Instrucciones de Ejecucion](#instrucciones-de-ejecucion)
- [Estructura del Repositorio](#estructura-del-repositorio)
- [Documentacion Interactiva (Swagger UI)](#documentacion-interactiva-swagger-ui)
- [Creditos y Reconocimientos](#creditos-y-reconocimientos)

---

## Presentacion de CAMPUSDESK

CAMPUSDESK surge como una respuesta a la necesidad de canalizar, priorizar y resolver incidencias tecnicas dentro de una institucion educativa o corporativa. El sistema permite conectar de forma fluida a tres actores principales:

1. **Usuarios Solicitantes (Estudiantes, Docentes, Personal Administrativo):** Reportan requerimientos de hardware, software, red o infraestructura mediante tickets de servicio.
2. **Personal Tecnico (Operadores de Soporte):** Reciben tickets asignados segun su especialidad y disponibilidad, registran avances, documentan soluciones y gestionan el cambio de estados.
3. **Super Administradores y Coordinadores:** Supervisan la mesa de ayuda, crean y habilitan usuarios, asignan roles, administran permisos del sistema y auditan metricas de atencion.

---

## Funcionalidades Implementadas

La version actual del backend de CAMPUSDESK cuenta con las siguientes capacidades implementadas y operativas:

### 1. Mesa de Ayuda y Gestion de Tickets
- Creacion y recepcion de solicitudes tecnicas con descripcion detallada, categorizacion y prioridad (Baja, Media, Alta, Critica).
- Clasificacion de incidencias segun el tipo de servicio requerido.
- Trazabilidad y consulta de tickets en curso e historicos.

### 2. Gestion de Operadores Tecnicos y Asignaciones
- Registro de perfiles tecnicos con definicion de especialidades.
- Asignacion formal de tickets a operadores responsables.
- Modulo preparado para evitar sobrecarga de trabajo y balancear la atencion de casos.

### 3. Trazabilidad, Comentarios e Historial de Estados
- Bitacora de comentarios publicos e internos asociados a cada ticket.
- Registro historico e inmutable de cambios de estado para auditoria de tiempos de respuesta.
- Ciclo de vida estricto para asegurar que un ticket no salte de estados de forma inconsistente.

### 4. Ciclo de Vida de Contraseñas y Gestion de Usuarios
- **Creacion Asistida por el Super Admin:** Los usuarios son dados de alta por el administrador con una contraseña temporal elegida por este.
- **Forzado de Cambio en Primer Ingreso (`mustChangePassword`):** Toda cuenta nueva nace con la bandera activada. Al autenticarse, la respuesta de login notifica al cliente que debe forzar la pantalla de cambio de clave antes de ingresar al sistema.
- **Cambio de Contraseña Autenticado (`/api/auth/change-password`):** El usuario ingresa su clave temporal y define su nueva clave definitiva (minimo 6 caracteres, diferente a la temporal). Al completarse, se apaga la bandera.
- **Restablecimiento Administrativo de Claves (`/api/user/{username}/reset-password`):** Si un usuario pierde u olvida su contraseña, el Super Admin puede restablecerla asignando una nueva clave provisional y reactivando la obligatoriedad de cambio, sin requerir servidores de correo electronico externos.

### 5. Control de Acceso Basado en Roles (RBAC) y Permisos Dinamicos
- Cada usuario posee exactamente un rol principal (`ADMIN`, `CUSTOMER`, roles tecnicos personalizados).
- Soporte para otorgar permisos individuales adicionales que complementan los heredados por el rol.
- Evaluacion de seguridad dinamica: los permisos efectivos se recalculan en cada peticion contra PostgreSQL.

---

## Modulos del Sistema

El codigo de CAMPUSDESK se estructura modularmente para desacoplar el dominio del negocio de la infraestructura:

```text
com.roles.usermanagement/
│
├── modules/                         # Dominio central de CAMPUSDESK
│   ├── ticket/                      # Gestion central de incidencias y tickets
│   ├── assignment/                  # Asignacion de tecnicos a tickets
│   ├── technician/                  # Catalogo de operadores de soporte
│   ├── comment/                     # Notas y comentarios por ticket
│   ├── statushistory/               # Auditoria de transiciones de estado
│   ├── lifecycle/                   # Maquina de estados del ticket
│   └── dashboard/                   # Estadisticas y panel de control
│
├── domain/                          # Modelos, DTOs y servicios de negocio transversales
├── persistance/                     # Entidades JPA, repositorios CRUD y mappers
└── web/                             # Controladores REST, filtros JWT y configuracion de seguridad
```

---

## Flujo de Seguridad y Ciclo de Vida de Contraseñas

A continuacion se describe el flujo paso a paso del ingreso y gestion de credenciales en CAMPUSDESK:

```text
PASO 1: SUPER ADMIN CREA EL USUARIO
POST /api/user/add
- El Super Admin envia: username, email, role y una password temporal.
- El backend guarda al usuario con mustChangePassword = true.
- El Super Admin entrega la contraseña temporal al usuario.

                  │
                  ▼

PASO 2: PRIMER INGRESO DEL USUARIO
POST /api/auth/login
- El usuario ingresa con sus credenciales temporales.
- El backend responde:
  {
    "token": "eyJhbGciOi...",
    "username": "operador01",
    "mustChangePassword": true
  }
- El frontend identifica mustChangePassword: true y bloquea el acceso al panel,
  redirigiendo a la vista obligatoria de cambio de clave.

                  │
                  ▼

PASO 3: CAMBIO OBLIGATORIO DE CONTRASEÑA
POST /api/auth/change-password
- Encabezado: Authorization: Bearer <token>
- Cuerpo: { "currentPassword": "temporal...", "newPassword": "definitiva..." }
- El servidor valida la clave actual, encripta la nueva con BCrypt y cambia
  mustChangePassword a false.
- A partir de este momento, el usuario accede normalmente al dashboard.

                  │
                  ▼

PASO 4 (CASO DE OLVIDO): RESTABLECIMIENTO POR SUPER ADMIN
POST /api/user/{username}/reset-password
- El Super Admin asigna una nueva contraseña temporal.
- La cuenta se desbloquea automaticamente y se reactiva mustChangePassword = true.
```

---

## Arquitectura de Software

La aplicacion adopta una arquitectura multicapa desacoplada y sin estado (Stateless):

```text
[ Cliente Web / Aplicacion Frontend / Swagger UI ]
                        │
                        ▼ (HTTP REST + Bearer Token)
            ┌───────────────────────┐
            │       JwtFilter       │ ── Valida firma HMAC256 y vigencia
            └───────────┬───────────┘
                        │
                        ▼
            ┌───────────────────────┐
            │   Controladores REST  │ ── Endpoints de Auth, User, Tickets
            └───────────┬───────────┘
                        │
                        ▼
            ┌───────────────────────┐
            │   Capa de Servicios   │ ── Reglas de negocio y validaciones
            └───────────┬───────────┘
                        │
                        ▼
            ┌───────────────────────┐
            │ Capa de Persistencia  │ ── Spring Data JPA y Mappers
            └───────────┬───────────┘
                        │
                        ▼
            ┌───────────────────────┐
            │ Base de Datos Postgres│ ── Tablas normalizadas
            └───────────────────────┘
```

---

## Tecnologias Utilizadas

- **Java 21 LTS:** Lenguaje principal con caracteristicas avanzadas de tipado y colecciones inmutables.
- **Spring Boot 4.1.1:** Nucleo del servidor web, inversion de control e inyeccion de dependencias.
- **Spring Security 6:** Filtros de autorizacion basados en expresiones de seguridad (`@PreAuthorize`).
- **PostgreSQL:** Sistema de gestion de bases de datos relacional transaccional.
- **Hibernate / Spring Data JPA:** Mapeo objeto-relacional con creacion de esquema automatica (`ddl-auto=update`).
- **Auth0 Java JWT:** Firma y verificacion de tokens bajo el algoritmo simetrico HMAC256.
- **BCrypt:** Codificacion de contraseñas con sal aleatoria (salt).
- **MapStruct:** Mapeo eficiente de objetos entre entidades JPA y DTOs en tiempo de compilacion.
- **Lombok:** Generacion automatica de metodos accesores y constructores.
- **Gradle Wrapper 8.14.3:** Automatizacion de compilacion, empaquetado y ejecucion.
- **Springdoc OpenAPI 3:** Documentacion de la API en formato Swagger UI.

---

## Catalogo de Endpoints REST

### 1. Autenticacion y Sesion (`/api/auth`)

| Metodo | Ruta | Autorizacion | Descripcion |
|---|---|---|---|
| `POST` | `/api/auth/login` | Publico | Autentica con usuario y contraseña; retorna JWT y el indicador `mustChangePassword`. |
| `POST` | `/api/auth/change-password` | Autenticado | Actualiza la contraseña actual por una nueva y desactiva el cambio obligatorio. |
| `GET` | `/api/auth/me` | Autenticado | Devuelve el rol actual, permisos individuales y el consolidado de permisos efectivos. |

### 2. Gestion de Usuarios (`/api/user`)

| Metodo | Ruta | Autorizacion Requerida | Descripcion |
|---|---|---|---|
| `GET` | `/api/user/all` | `USER_READ` | Lista todos los usuarios registrados. |
| `POST` | `/api/user/add` | `USER_CREATE` | Registra una cuenta nueva con `mustChangePassword = true` por defecto. |
| `PUT` | `/api/user/update` | `USER_UPDATE` | Actualiza correo, bloqueo o estado de habilitacion de un usuario. |
| `DELETE` | `/api/user/delete/{name}` | `USER_DELETE` | Elimina definitivamente la cuenta de un usuario. |
| `POST` | `/api/user/{username}/reset-password` | `USER_UPDATE` o `ADMIN` | Asigna clave temporal y obliga al usuario al cambio de contraseña en su siguiente acceso. |
| `POST` | `/api/user/assignRole` | `ROLE_ASSIGN` | Reemplaza el rol unico asignado al usuario. |
| `POST` | `/api/user/assignPermission` | `PERMISSION_ASSIGN` | Agrega un permiso individual especifico a un usuario. |
| `DELETE` | `/api/user/{username}/permissions/{perm}` | `PERMISSION_ASSIGN` | Revoca un permiso individual otorgado a la cuenta. |
| `GET` | `/api/user/{username}/permissions` | `USER_READ` | Consulta los permisos individuales, heredados y efectivos del usuario. |
| `GET` | `/api/user/permissions` | `PERMISSION_ASSIGN` | Retorna la lista de todas las autoridades existentes. |

### 3. Catalogos de Seguridad (`/api`)

| Metodo | Ruta | Autorizacion Requerida | Descripcion |
|---|---|---|---|
| `GET` | `/api/roles` | `ROLE_MANAGE` | Lista los roles registrados y sus permisos asociados. |
| `POST` | `/api/roles` | `ROLE_MANAGE` | Registra un nuevo rol asignando su conjunto de permisos. |
| `GET` | `/api/permissions` | `PERMISSION_MANAGE` | Consulta todas las autoridades base y dinamicas. |
| `POST` | `/api/permissions` | `PERMISSION_MANAGE` | Da de alta una nueva autoridad en el catalogo. |
| `PUT` | `/api/roles/{role}/permissions/{perm}` | `ROLE_MANAGE` y `PERMISSION_MANAGE` | Vincula un permiso existente a un rol. |
| `DELETE` | `/api/roles/{role}/permissions/{perm}` | `ROLE_MANAGE` y `PERMISSION_MANAGE` | Desvincula un permiso de un rol determinado. |

---

## Configuracion y Variables de Entorno

El archivo base de desarrollo se encuentra en `src/main/resources/application-dev.properties`. Puedes parametrizar la conexion mediante las siguientes variables de entorno:

| Variable | Descripcion | Valor Predeterminado en Dev |
|---|---|---|
| `DB_URL` | URL JDBC de conexion a PostgreSQL | `jdbc:postgresql://localhost:5432/sistema_gestion_usuarios` |
| `DB_USERNAME` | Usuario de base de datos | `admin` |
| `DB_PASSWORD` | Contraseña de base de datos | `admin123` |
| `JWT_SECRET` | Clave secreta para firma HMAC256 | `User_M4n4gement` |
| `JWT_ISSUER` | Emisor registrado del token JWT | `User_R0les_M4n4gement` |
| `JWT_EXPIRATION` | Tiempo de vida del token (`30m`, `8h`, `15d`) | `15d` |
| `ADMIN_USERNAME` | Usuario administrador del bootstrap | `superadmin` |
| `ADMIN_EMAIL` | Correo del administrador del bootstrap | `superadmin@localhost` |
| `ADMIN_PASSWORD` | Contraseña inicial del administrador | `SuperAdmin123!` |
| `BOOTSTRAP_ADMIN_ENABLED` | Control de creacion de cuenta inicial | `true` |

---

## Requisitos Previos e Instalacion

### Requisitos Minimos
- JDK 21 (configurado en la variable de entorno `JAVA_HOME`).
- Servidor PostgreSQL 14 o superior en ejecucion.
- Git instalado.
- Terminal compatible con PowerShell (Windows) o Bash (Linux / macOS).

### 1. Clonar el Proyecto
```bash
git clone <URL_DEL_REPOSITORIO>
cd spring-security-user-role-management
```

### 2. Crear la Base de Datos
Desde tu cliente PostgreSQL (`psql` o pgAdmin), crea la base de datos requerida:

```sql
CREATE DATABASE sistema_gestion_usuarios;
```

---

## Instrucciones de Ejecucion

### Ejecucion con Gradle Wrapper en Windows (PowerShell)

Define las variables de conexion correspondientes a tu instalacion de PostgreSQL:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/sistema_gestion_usuarios"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "tu_password_de_postgres"

.\gradlew.bat bootRun
```

### Ejecucion en Linux o macOS (Bash)

```bash
export DB_URL="jdbc:postgresql://localhost:5432/sistema_gestion_usuarios"
export DB_USERNAME="postgres"
export DB_PASSWORD="tu_password_de_postgres"

./gradlew bootRun
```

La aplicacion quedara disponible en el puerto **8050**:
```text
http://localhost:8050
```

### Generacion del Archivo JAR Ejecutable

```powershell
.\gradlew.bat clean test bootJar
java -jar build/libs/usermanagement-0.0.1-SNAPSHOT.jar
```

---

## Estructura del Repositorio

```text
spring-security-user-role-management/
│
├── build.gradle                             # Configuracion de dependencias y plugins
├── settings.gradle                          # Definicion del proyecto
├── gradlew / gradlew.bat                    # Scripts ejecutables de Gradle Wrapper
├── README.md                                # Documentacion tecnica principal
│
└── src/
    ├── main/
    │   ├── java/com/roles/usermanagement/
    │   │   ├── UsermanagementApplication.java
    │   │   │
    │   │   ├── modules/                     # Modulos de soporte CAMPUSDESK
    │   │   │   ├── ticket/                  # Incidencias y requerimientos
    │   │   │   ├── assignment/              # Asignaciones tecnicas
    │   │   │   ├── technician/              # Operadores tecnicos
    │   │   │   ├── comment/                 # Comentarios en tickets
    │   │   │   ├── statushistory/           # Historial cronologico de estados
    │   │   │   ├── lifecycle/               # Maquina de estados del ticket
    │   │   │   └── dashboard/               # Indicadores y metricas
    │   │   │
    │   │   ├── domain/                      # DTOs y logica de dominio transversal
    │   │   ├── persistance/                 # Entidades, repositorios y mappers
    │   │   └── web/                         # Controladores REST y filtros de seguridad
    │   │
    │   └── resources/
    │       ├── application.properties       # Perfil activo (dev)
    │       ├── application-dev.properties   # Parametros de base de datos y JWT
    │       └── banner.txt                   # Banner de arranque CAMPUSDESK
    │
    └── test/                                # Pruebas unitarias e integracion
```

---

## Documentacion Interactiva (Swagger UI)

Una vez iniciado el servidor, puedes probar visualmente todas las rutas y metodos en:

- **Swagger UI:**  
  `http://localhost:8050/swagger-ui.html`

- **Especificacion OpenAPI (JSON):**  
  `http://localhost:8050/v3/api-docs`

### Procedimiento para Autenticarse en Swagger UI

1. Ingresa a `http://localhost:8050/swagger-ui.html`.
2. Ejecuta `POST /api/auth/login` con:
   ```json
   {
     "username": "superadmin",
     "password": "SuperAdmin123!"
   }
   ```
3. Copia el token recibido en el campo `token` de la respuesta JSON.
4. En la parte superior derecha de la pagina, haz clic en el boton **Authorize** (marcado con el icono de candado).
5. Escribe o pega el token en el campo de texto y haz clic en **Authorize**.
6. Todas las consultas subsecuentes desde Swagger enviaran el token en la cabecera `Authorization: Bearer <token>`.

---

## Creditos y Reconocimientos

Este proyecto adopto como punto de partida la plantilla de codigo abierto **spring-security-user-role-management**, reutilizando sus fundamentos tecnicos para:
- La configuracion base de Spring Security con filtros JWT stateless y algoritmo HMAC256.
- El modelo relacional inicial de roles unicos y extension dinamica de permisos individuales en PostgreSQL.
- El servicio de bootstrap para la inicializacion automatica de catalogos del sistema.

Sobre dicha base, los creadores de este proyecto diseñaron, desarrollaron e integraron toda la logica de negocio de **CAMPUSDESK**: la gestion de tickets de soporte tecnico, operadores tecnicos, asignaciones, historial de estados, y la politica de seguridad para el ciclo de vida de contraseñas (contraseñas temporales, forzado de cambio en primer ingreso y restablecimiento administrativo).
