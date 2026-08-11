# 📌 Sistema de Soporte Técnico Veterinario (Arquitectura de Microservicios)

Este repositorio contiene la documentación unificada y las instrucciones esenciales de funcionamiento para los dos microservicios que componen el sistema:

1. **`usuarios-api`** (Puerto `8083`): Microservicio encargado de la autenticación y emisión de tokens JWT.
2. **`soporte-tecnico-api`** (Puerto `8080` / Context Path `/api`): Microservicio encargado del core del negocio (gestión de solicitudes de soporte).

---

## 🛠️ Arquitectura y Flujo de Autenticación

El sistema implementa una **arquitectura desacoplada sin estado (Stateless)** basada en **JWT (JSON Web Tokens)** con clave compartida simétrica:

```
  [ Cliente ] --(1) POST /api/v1/auth/login --> [ usuarios-api (8083) ]
       |                                                 |
       |<------------(2) Retorna JWT Token --------------|
       |
  [ Cliente ] --(3) GET /api/v1/solicitudes (Bearer JWT) -> [ soporte-tecnico-api (8080) ]
                                                                 |
                                                     (Valida firma localmente)
                                                                 |
                                                    <--- (4) Retorna datos ---
```

- **Firma Secreta Compartida**: Ambos microservicios usan exactamente la misma firma en su clase `JwtUtil`:
  `EstaEsUnaClaveSuperSecretaParaZegel2026Backend`
- **Validez del Token**: 1 hora.

---

## 💾 Configuración de Base de Datos (MySQL)

Ambos microservicios utilizan MySQL/MariaDB en el puerto `3306` siguiendo el patrón de **base de datos por servicio**:
* **usuarios-api**: base de datos `usuariosdb`
* **soporte-tecnico-api**: base de datos `soportedb`

### Opción A: Levantar Base de Datos con Docker (Recomendada 🐳)
Si tienes **Docker** y **Docker Compose** instalados, puedes iniciar la base de datos y crear las estructuras necesarias automáticamente con un solo comando en la raíz del proyecto:
```bash
docker compose up -d
```
*Esto iniciará un contenedor MySQL en el puerto 3306, configurará la contraseña en `12345` y ejecutará el script `db-init/init.sql` para crear de forma automática las bases de datos `usuariosdb` y `soportedb`.*

### Opción B: Instalación Local Tradicional
Si prefieres usar un motor MySQL instalado localmente en tu sistema operativo:
1. Asegúrate de tener el servicio MySQL corriendo en el puerto `3306`.
2. Crea manualmente las bases de datos vacías en tu motor SQL:
   ```sql
   CREATE DATABASE usuariosdb;
   CREATE DATABASE soportedb;
   ```
3. El usuario por defecto configurado es `root` con contraseña `12345` (puedes modificar estas credenciales en los archivos `application.properties` de cada proyecto si tienes una contraseña distinta).

> [!NOTE]
> Las tablas de las bases de datos se autogeneran automáticamente al iniciar los proyectos gracias a la configuración de Hibernate.

---

## 🚀 Instrucciones de Inicio y Ejecución

### Prerrequisitos:
- **Java JDK 17** o superior instalado.
- **Docker** y **Docker Compose** (si usas la Opción A para la base de datos).
- *(Opcional)* Maven instalado globalmente (no es necesario, ya que ambos proyectos incluyen su propio compilador embebido `mvnw`).

### Ejecutar los microservicios:

#### 1. Iniciar Microservicio de Usuarios (`usuarios-api`):
Navega a la carpeta `usuarios-api` y ejecuta:
```bash
# En Linux/macOS
./mvnw spring-boot:run

# En Windows
mvnw.cmd spring-boot:run
```
*El servicio levantará en:* `http://localhost:8083`

#### 2. Iniciar Microservicio de Soporte (`soporte-tecnico-api`):
Navega a la carpeta `soporte-tecnico-api` y ejecuta:
```bash
# En Linux/macOS
./mvnw spring-boot:run

# En Windows
mvnw.cmd spring-boot:run
```
*El servicio levantará en:* `http://localhost:8080/api`

---

## 🧪 Pruebas Rápidas con Postman

Hemos preparado una colección de Postman pre-configurada para facilitar las pruebas de desarrollo:

1. Importa el archivo **`Soporte_Tecnico_Veterinario.postman_collection.json`** (ubicado en la raíz de este repositorio) a tu aplicación Postman.
2. Abre la carpeta `1. Autenticación` y ejecuta la petición **`Login Administrador (ADMIN)`**. Esto obtendrá el JWT y guardará automáticamente el token en una variable global llamada `{{jwt_token}}`.
3. Ya puedes ejecutar libremente cualquiera de las peticiones en la carpeta `2. Solicitudes de Soporte` (Crear, Listar, Actualizar, Consultar por ID, Cambiar Estado, Eliminar o provocar una Validación Incorrecta).

---

## 🔑 Credenciales de Prueba (Prototipado Rápido)

Para iniciar sesión y obtener el token JWT, realiza una petición `POST` al endpoint de `usuarios-api`:
- **URL**: `POST http://localhost:8083/api/v1/auth/login`
- **Parámetros** (Query o Form URL Encoded): `username` y `password`.

### Cuentas disponibles:
| Usuario | Contraseña | Rol / Permisos |
| :--- | :--- | :--- |
| **`admin`** | `1234` | **ADMIN** (Acceso completo, incluyendo eliminación de solicitudes) |
| **`tecnico`** | `1234` | **TECNICO** (Acceso de lectura y actualización, no puede eliminar) |

*Respuesta esperada:*
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## 🌐 Endpoints y Documentación Interactiva (Swagger UI)

Una vez iniciados los servicios, puedes consultar y probar los endpoints directamente desde tu navegador:

### 📄 soporte-tecnico-api (Puerto 8080 con Context Path `/api`)
* **Swagger UI (Pruebas Interactivas)**: 👉 [http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html)
* **OpenAPI Docs (JSON)**: `http://localhost:8080/api/v3/api-docs`

#### Endpoints Principales:
* **Listado Paginado**: `GET /api/v1/solicitudes-soporte?page=0&size=10` (Público)
* **Crear Solicitud**: `POST /api/v1/solicitudes-soporte` (Requiere Token Bearer)
* **Obtener por ID**: `GET /api/v1/solicitudes-soporte/{id}` (Requiere Token Bearer)
* **Actualizar Completa**: `PUT /api/v1/solicitudes-soporte/{id}` (Requiere Token Bearer)
* **Actualizar Estado**: `PATCH /api/v1/solicitudes-soporte/{id}/estado?estado=EN_PROCESO` (Requiere Token Bearer)
* **Eliminar Solicitud**: `DELETE /api/v1/solicitudes-soporte/{id}` (Requiere Token Bearer con rol **ADMIN**)

---

### 📄 usuarios-api (Puerto 8083)
* **Login**: `POST /api/v1/auth/login` (Público)
* **Swagger UI (Si se habilita dependencia)**: 👉 [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
* **OpenAPI Docs (JSON)**: `http://localhost:8083/v3/api-docs`