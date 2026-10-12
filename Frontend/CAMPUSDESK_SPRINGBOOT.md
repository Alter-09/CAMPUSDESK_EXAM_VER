## **Contexto empresarial**

La empresa ficticia **TechNova Solutions** cuenta con más de 200 colaboradores distribuidos en diferentes áreas. Diariamente, su departamento de tecnología recibe solicitudes relacionadas con fallas de equipos, problemas de conectividad, errores de software y dificultades de acceso a los sistemas corporativos.

Actualmente, estas solicitudes se gestionan mediante correos electrónicos, llamadas y mensajes, generando dificultades como:

- Pérdida de solicitudes y falta de seguimiento.
- Asignación desorganizada de técnicos.
- Ausencia de un historial de atención.
- Desconocimiento del estado de cada incidencia.
- Dificultad para medir el desempeño del departamento tecnológico.
- Acceso no controlado a información de otros colaboradores.

Debido a esta situación, la empresa ha decidido contratar un equipo de desarrollo para implementar una plataforma que centralice las solicitudes de soporte y permita gestionar su ciclo de vida de manera organizada y segura.



## **Objetivo general**

Desarrollar una aplicación web de gestión de incidencias tecnológicas utilizando Spring Boot, PostgreSQL y JavaScript, aplicando arquitectura por capas, inyección de dependencias, persistencia mediante JPA, construcción de APIs REST, documentación Swagger, manejo de excepciones y mecanismos de seguridad basados en Spring Security y JWT.



### **Objetivos específicos**

1. Diseñar una base de datos relacional que represente los procesos del sistema.
2. Implementar una arquitectura backend organizada por responsabilidades.
3. Aplicar Java Beans, inyección de dependencias y principios de programación orientada a objetos.
4. Gestionar la persistencia mediante Spring Data JPA.
5. Construir una API REST que implemente las operaciones y reglas del negocio.
6. Documentar y probar los endpoints mediante Swagger.
7. Implementar validaciones y manejo centralizado de excepciones.
8. Proteger las funcionalidades mediante autenticación JWT y autorización por roles.
9. Configurar CORS para permitir la comunicación entre el frontend y el backend.
10. Desarrollar una interfaz web funcional que consuma los servicios implementados.





# **Requerimientos funcionales**



## **RF-01. Gestión de usuarios**

El sistema deberá manejar tres roles:

**Administrador (ADMIN)**

- Consultar todos los tickets registrados.
- Visualizar los técnicos disponibles.
- Asignar y reasignar técnicos a los tickets.
- Consultar los usuarios registrados.
- Consultar estadísticas generales de las incidencias.
- Visualizar el historial de cualquier ticket.

**Técnico (TECHNICIAN)**

- Consultar los tickets asignados a su cuenta.
- Visualizar los detalles de las incidencias asignadas.
- Cambiar el estado de los tickets según las reglas establecidas.
- Agregar comentarios sobre el proceso de atención.
- Consultar el historial de las solicitudes asignadas.

**Usuario (USER)**

- Registrarse e iniciar sesión.
- Crear solicitudes de soporte tecnológico.
- Consultar únicamente sus propias solicitudes.
- Modificar una solicitud mientras permanezca abierta y sin asignar.
- Agregar comentarios a sus solicitudes activas.
- Consultar el estado y el historial de sus tickets.
- Cerrar un ticket cuando haya sido resuelto.

**Condición obligatoria:** Los usuarios no podrán asignarse roles privilegiados durante el registro. Los administradores y técnicos deberán ser creados mediante un mecanismo de inicialización o aprovisionamiento seguro.



## **RF-02. Autenticación y autorización**

Implementar un sistema de autenticación que permita:

- Registrar nuevos usuarios.
- Iniciar sesión mediante correo electrónico y contraseña.
- Generar un token JWT cuando la autenticación sea correcta.
- Validar el token en las solicitudes a endpoints protegidos.
- Restringir el acceso de acuerdo con el rol.
- Rechazar solicitudes con tokens ausentes, inválidos o expirados.
- Proteger los recursos según su propietario o técnico asignado.

Las contraseñas deberán almacenarse utilizando un algoritmo de hash seguro, como BCrypt.

No se permite almacenar contraseñas en texto plano.



## **RF-03. Gestión de tickets**

Cada ticket deberá contener, como mínimo:

![img](https://khc-sistema-v2.s3.amazonaws.com/editor/17913425915826cca75043ca5b/attachment-1.png.png)



**Categorías permitidas:**

- HARDWARE
- SOFTWARE
- REDES
- ACCESOS
- OTROS

**Prioridades permitidas:**

- BAJA
- MEDIA
- ALTA
- CRITICA

El sistema deberá permitir crear, consultar, modificar cuando corresponda y administrar solicitudes de acuerdo con los permisos de cada rol.

Los tickets deberán almacenarse de manera permanente en PostgreSQL.



## **RF-04. Gestión del ciclo de vida**

Todo ticket deberá atravesar un flujo de atención controlado:

**ABIERTA → ASIGNADA → EN_PROCESO → RESUELTA → CERRADA**

Reglas:

1. Un ticket nuevo inicia automáticamente en estado ABIERTA.
2. Solo un administrador puede asignar un técnico.
3. Al asignar un técnico, el ticket pasa a ASIGNADA.
4. El técnico responsable puede cambiarlo a EN_PROCESO.
5. Un ticket solo puede marcarse RESUELTA si previamente estuvo EN_PROCESO.
6. Solo el solicitante puede confirmar el cierre de su ticket RESUELTA.
7. Un ticket CERRADA no puede modificarse ni recibir comentarios.
8. No se permiten transiciones de estado diferentes a las establecidas.
9. La reasignación de un ticket ASIGNADA solo podrá realizarla un administrador.
10. Cada cambio de estado deberá registrarse en el historial.

El backend será responsable de validar estas reglas; no es suficiente con restringir las acciones desde la interfaz.



## **RF-05. Sistema de comentarios**

Cada incidencia contará con un apartado para registrar comentarios.

Los comentarios deberán almacenar:

- Identificador.
- Contenido.
- Autor.
- Ticket relacionado.
- Fecha de creación.

Los usuarios podrán comentar sus propias solicitudes activas y los técnicos podrán comentar los tickets asignados.

Los administradores podrán agregar comentarios a cualquier ticket activo.

Los comentarios deberán mostrarse cronológicamente.



## **RF-06. Historial de cambios**

El sistema deberá registrar automáticamente los cambios de estado de cada ticket.

Cada registro debe contener:

- Ticket relacionado.
- Estado anterior.
- Estado nuevo.
- Usuario responsable del cambio.
- Fecha y hora del cambio.

El historial no podrá modificarse manualmente desde la interfaz.

La creación del historial y la actualización del estado deberán ejecutarse como una operación consistente.



## **RF-07. Panel de indicadores**

Desarrollar un dashboard con los siguientes indicadores:

- Total de tickets registrados.
- Cantidad de tickets abiertos.
- Cantidad de tickets en proceso.
- Cantidad de tickets resueltos.
- Cantidad de tickets cerrados.

Las estadísticas deberán obtenerse desde PostgreSQL mediante la API REST.

El administrador visualizará estadísticas generales. Los demás roles únicamente podrán visualizar información correspondiente a sus tickets permitidos, si se les ofrece un dashboard.



## **RF-08. Búsqueda y filtros**

Implementar filtros para consultar tickets por:

- Estado.
- Prioridad.
- Categoría.

Los filtros deberán procesarse en el backend, respetando los permisos del usuario autenticado.



# Requerimientos técnicos

## RT-01. Arquitectura

El backend deberá utilizar una arquitectura por capas.

Como mínimo se deben identificar las siguientes responsabilidades:

- **Controller:** Recepción y respuesta de solicitudes HTTP.
- **Service:** Implementación de lógica y reglas del negocio.
- **Repository:** Acceso a datos mediante Spring Data JPA.
- **Entity:** Representación del modelo persistente.
- **DTO:** Estructuras para recibir y devolver información.
- **Security:** Configuración de autenticación y autorización.
- **Exception:** Manejo de errores y excepciones.

No se permite implementar toda la lógica del negocio directamente en los controladores.

Se deberá utilizar inyección de dependencias administrada por Spring.



## RT-02. Persistencia

La base de datos deberá desarrollarse en PostgreSQL.

Se requiere implementar, como mínimo, las siguientes entidades:

![img](https://khc-sistema-v2.s3.amazonaws.com/editor/17913425915826cca75043ca5b/attachment-2.png.png)



Deben implementarse relaciones mediante JPA, identificadores, claves foráneas y restricciones de integridad.

Las consultas deberán realizarse mediante Spring Data JPA.

No se permite utilizar colecciones en memoria como mecanismo principal de persistencia.



## RT-03. API REST

La aplicación deberá exponer una API REST que permita realizar las operaciones del sistema.

Como referencia, se establece el siguiente contrato mínimo:

![img](https://khc-sistema-v2.s3.amazonaws.com/editor/17913425915826cca75043ca5b/attachment-3.png.png)



Podrán agregarse endpoints adicionales si son necesarios para completar los requerimientos.

Las respuestas deberán utilizar códigos HTTP adecuados y una estructura JSON consistente.

## RT-04. Validaciones y excepciones

Implementar validaciones para evitar, entre otros casos:

- Campos obligatorios vacíos.
- Correos electrónicos con formato incorrecto.
- Correos duplicados.
- Contraseñas que incumplan los requisitos mínimos de seguridad.
- Tickets con título o descripción inválidos.
- Prioridades, categorías o estados inexistentes.
- Asignaciones a usuarios que no sean técnicos.
- Cambios de estado no permitidos.
- Consultas a recursos inexistentes.
- Accesos a información sin autorización.

Se deberá implementar manejo global de excepciones.

Códigos HTTP mínimos esperados:

![img](https://khc-sistema-v2.s3.amazonaws.com/editor/17913425915826cca75043ca5b/attachment-4.png.png)



Los errores no deberán exponer contraseñas, tokens, trazas internas ni información sensible.



## RT-05. Seguridad

Implementar Spring Security con JWT.

Se deberá garantizar que:

- Los endpoints privados requieran autenticación.
- Los permisos se validen desde el backend.
- Un usuario no pueda consultar tickets ajenos.
- Un técnico no pueda modificar tickets que no tenga asignados.
- Solo el administrador pueda realizar asignaciones.
- El token tenga un tiempo de expiración.
- Las credenciales sensibles no estén escritas directamente en el código fuente.
- La configuración CORS permita únicamente los orígenes necesarios para el frontend.

Swagger deberá permitir documentar y probar las operaciones protegidas.



## RT-06. Frontend

La interfaz deberá desarrollarse exclusivamente con:

**HTML5, CSS3 y JavaScript Vanilla.**

No se permite utilizar:

- React.
- Angular.
- Vue.
- Bootstrap.
- Tailwind CSS.
- jQuery.
- Frameworks frontend equivalentes.

El frontend deberá incluir, como mínimo:

**Pantalla 1. Autenticación**

- Formulario de registro.
- Formulario de inicio de sesión.
- Mensajes de validación.
- Manejo del token y cierre de sesión.

**Pantalla 2. Dashboard**

- Indicadores obtenidos de la API.
- Navegación según el rol.
- Resumen general de las solicitudes autorizadas.

**Pantalla 3. Gestión de tickets**

- Listado de tickets.
- Creación de solicitudes.
- Filtros por estado, prioridad y categoría.
- Acceso al detalle de cada solicitud.

**Pantalla 4. Detalle del ticket**

- Información completa.
- Técnico asignado.
- Comentarios.
- Historial de cambios.
- Acciones permitidas según rol y estado.

**Pantalla 5. Administración**

- Consulta de solicitudes.
- Listado de técnicos.
- Asignación y reasignación permitida.
- Seguimiento de incidencias.

La aplicación deberá consumir la API mediante fetch() y async/await.

Se deberá implementar manejo de errores de comunicación, mensajes de operación exitosa e indicadores de carga cuando corresponda.

No se aceptarán interfaces que presenten datos estáticos como sustituto de los datos reales almacenados en PostgreSQL.



# Casos de prueba obligatorios

Antes de realizar la entrega, el equipo deberá verificar los siguientes escenarios:

![img](https://khc-sistema-v2.s3.amazonaws.com/editor/17913425915826cca75043ca5b/attachment-5.png.png)



Todos los casos deben poder demostrarse durante la presentación del proyecto.



# Restricciones generales

1. El backend debe desarrollarse obligatoriamente en Spring Boot.
2. PostgreSQL es la única base de datos permitida para la persistencia principal.
3. El frontend debe utilizar exclusivamente HTML, CSS y JavaScript.
4. Se debe respetar la arquitectura por capas.
5. Se debe utilizar Spring Data JPA para la persistencia.
6. La lógica del negocio debe implementarse en los servicios.
7. Los endpoints protegidos deben utilizar Spring Security y JWT.
8. Las respuestas y errores deben utilizar códigos HTTP apropiados.
9. La API debe estar documentada con Swagger.
10. El código debe estar organizado, ser legible y utilizar nombres significativos.
11. No se permite utilizar datos simulados para demostrar funcionalidades que requieren persistencia.
12. No se permite publicar contraseñas reales, secretos JWT ni credenciales de base de datos en el repositorio.
13. Todas las operaciones deben respetar las autorizaciones definidas.
14. No se permite entregar únicamente el backend o únicamente el frontend.



Resultado esperado

La entrega se realizará mediante un repositorio privado de GitHub, salvo indicación diferente del trainer.



El repositorio deberá incluir:

**1. Código fuente del backend**

Proyecto Spring Boot con las dependencias y configuraciones necesarias.



**2. Código fuente del frontend**

Archivos HTML, CSS y JavaScript utilizados para la interfaz.



**3. Base de datos**

Script SQL o respaldo PostgreSQL que permita preparar la estructura y disponer de información de prueba.



**4. Documentación**

Archivo README.md que contenga:

- Nombre y descripción del proyecto.
- Integrantes, cuando corresponda.
- Tecnologías utilizadas.
- Requisitos previos.
- Instrucciones de instalación.
- Configuración mediante variables de entorno.
- Procedimiento de ejecución del backend y frontend.
- Instrucciones para preparar PostgreSQL.
- Descripción de roles.
- Enlace local para consultar Swagger.
- Evidencias de funcionamiento.



**5. Evidencias**

Capturas de pantalla que demuestren:

- Inicio de sesión.
- Gestión de tickets.
- Asignación de técnicos.
- Cambios de estado.
- Historial y comentarios.
- Panel de estadísticas.
- Documentación Swagger.



**6. Presentación funcional**

Demostración del flujo completo desde la creación de una incidencia hasta su cierre, utilizando las cuentas correspondientes a cada rol.



# Retos adicionales

Los siguientes desafíos son opcionales y podrán desarrollarse únicamente después de completar los requerimientos obligatorios:

**Reto 1 — Paginación**

Incorporar paginación y ordenamiento de tickets desde la API.



**Reto 2 — Estadísticas avanzadas**

Calcular tiempos promedio de resolución y cantidad de tickets atendidos por técnico.



**Reto 3 — Auditoría**

Registrar cambios de asignación y otras operaciones administrativas relevantes.



**Reto 4 — Pruebas automatizadas**

Implementar pruebas unitarias y de integración utilizando JUnit, Mockito y las herramientas de pruebas de Spring Boot.



**Reto 5 — Experiencia de usuario**

Agregar notificaciones visuales, filtros combinados y mejoras de accesibilidad sin incorporar frameworks frontend.



Los retos opcionales no sustituyen ningún requisito obligatorio.



# Condiciones de entrega y sustentación

Al finalizar las 25 horas, cada estudiante o equipo deberá presentar el enlace de su repositorio y demostrar el funcionamiento de la aplicación.

Durante la sustentación se deberán ejecutar operaciones reales contra el backend y PostgreSQL.

El trainer podrá solicitar pruebas de autorización, uso de Swagger, consultas a la base de datos y explicación del código implementado.

Se valorará la capacidad para explicar las decisiones de arquitectura, las relaciones JPA, el funcionamiento de JWT, el manejo de excepciones y la comunicación entre el frontend y la API.