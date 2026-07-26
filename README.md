# API RESTful - Gestión de Solicitudes de Soporte Técnico

## Descripción del Proyecto
Esta es una API RESTful desarrollada en Java con Spring Boot para la gestión de solicitudes de soporte técnico. El sistema permite a una empresa de servicios tecnológicos centralizar la información, registrando y administrando el ciclo de vida de cada solicitud de soporte desde su creación hasta su cierre, evitando la pérdida de información por el uso de registros manuales o correos desordenados.

## Tecnologías Utilizadas
El proyecto hace uso de las siguientes herramientas y dependencias:
* **Java 17:** Lenguaje principal de desarrollo.
* **Spring Boot (3.2.5):** Framework principal para la creación de la API REST.
* **Spring Data JPA:** Para la capa de acceso a datos.
* **H2 Database:** Base de datos en memoria para la persistencia temporal de los datos durante la ejecución.
* **Bean Validation:** Para asegurar la integridad y validación de los datos de entrada (ej. `@NotNull`, `@NotBlank`).
* **Maven:** Gestor de dependencias y construcción del proyecto.
* **Swagger / OpenAPI:** Para la documentación interactiva de los endpoints.

## Arquitectura del Sistema
El proyecto sigue una **arquitectura por capas** para asegurar la escalabilidad y separación de responsabilidades:
* **`controller`:** Controladores REST que exponen los endpoints y manejan las peticiones HTTP.
* **`service`:** Capa que contiene la lógica de negocio y las validaciones principales.
* **`repository`:** Interfaces de Spring Data JPA para la interacción con la base de datos H2.
* **`model`:** Entidades de dominio (ej. `SolicitudSoporte`) que se mapean a la base de datos.
* **`dto`:** Objetos de Transferencia de Datos (`RequestDTO` y `ResponseDTO`) para estructurar las entradas y salidas.
* **`exception`:** Manejo centralizado de errores usando `@RestControllerAdvice`.

## Instalación y Ejecución

### Prerrequisitos
* Tener instalado **Java JDK 17** o superior.
* Tener instalado **Maven**.

### Pasos para ejecutar localmente
1. Clonar el repositorio en tu máquina local:
   ```bash
   git clone https://github.com/casiano-reyes/soporte-tecnico-api.git
Navegar al directorio del proyecto:
Ejecutar la aplicación utilizando Maven:
Una vez que la aplicación compile e inicie, el servidor local estará corriendo en el puerto 8080.
🌐 Endpoints Principales
La URL base de la API es: http://localhost:8080/api/v1/solicitudes-soporte
Método
Ruta
Descripción
GET
/
Lista todas las solicitudes de soporte registradas.
POST
/
Registra una nueva solicitud de soporte técnico.
GET
/{id}
Busca y devuelve los detalles de una solicitud específica por su ID.
PUT
/{id}
Actualiza la información completa de una solicitud existente.
PATCH
/{id}/estado?estado=EN_PROCESO
Modifica únicamente el estado de la solicitud.
DELETE
/{id}
Elimina una solicitud del sistema.
📚 Documentación Interactiva (Swagger)
Para explorar y probar la API de forma interactiva a través de una interfaz gráfica, una vez que el proyecto esté corriendo, abre tu navegador y ve: 👉 http://localhost:8080/api/swagger-ui/index.html

## 🔒 Actualizaciones de Arquitectura (Seguridad, Paginación y Microservicios)

En las últimas fases se han implementado mejoras arquitectónicas clave para la robustez, seguridad y rendimiento del sistema:

### 1. Persistencia de Datos con MySQL/MariaDB
Se migró la base de datos de H2 (en memoria) a **MySQL/MariaDB** local. 
- Base de datos principal de soporte: `soportedb`
- Base de datos de usuarios: `usuariosdb`
- Mapeo automático de esquemas a través de **Hibernate/JPA** con soporte del dialecto de MariaDB.

### 2. Estandarización y Paginación de Endpoints
Se modificó el listado principal de solicitudes para no comprometer el rendimiento con consultas masivas:
- **Paginación (`PageResponse`):** El listado ahora acepta los parámetros opcionales `page` (por defecto 0) y `size` (por defecto 10).
- **Estructura Estándar de Respuesta (`ResponseDTO`):** Todas las respuestas devuelven un formato JSON uniforme con `responseCode`, `responseMessage` y el objeto `data`.

Ejemplo de endpoint paginado:
`GET http://localhost:8080/api/v1/solicitudes-soporte?page=0&size=5`

### 3. Microservicio de Usuarios (`usuarios-api`)
Se creó un nuevo microservicio dedicado a la autenticación en el puerto **`8083`**:
- Endpoint de Autenticación: `POST http://localhost:8083/api/v1/auth/login?username=...&password=...`
- Credenciales estáticas de prototipado rápido:
  - `admin` / `1234` (Rol: **`ADMIN`**)
  - `tecnico` / `1234` (Rol: **`TECNICO`**)
- Retorna un token **JWT** firmado con una clave simétrica secreta compartida válida por 1 hora.

### 4. Seguridad de la API (`soporte-tecnico-api`)
Se integró **Spring Security** en el microservicio de soporte técnico para validar los tokens JWT entrantes:
- **JwtFilter:** Intercepta todas las peticiones protegidas y valida la firma y vigencia de la cabecera `Authorization: Bearer <token>`.
- **Rutas Públicas:** Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`) y la consola H2 (`/h2-console/**`).
- **Rutas Protegidas:** Todo el resto de la API requiere autenticación.
- **Autorización por Rol (`@PreAuthorize`):** El endpoint de eliminación de solicitudes (`DELETE /v1/solicitudes-soporte/{id}`) está estrictamente restringido a usuarios con rol **`ADMIN`**.