# 📌 Sistema de Soporte TI - Gestión de Usuarios e Incidencias (Microservicios & SPA)

Este repositorio contiene la solución empresarial unificada para la gestión de incidencias de Soporte TI y administración de usuarios, estructurada en una arquitectura desacoplada de microservicios y una interfaz web moderna de tipo Single Page Application (SPA).

---

## 🏗️ Componentes del Sistema

El proyecto consta de tres componentes principales:

1. **`usuarios-api`** (Puerto `8083`): Microservicio Spring Boot encargado de la autenticación JWT, registro de personal (ADMIN, TECNICO, CLIENTE) y administración de accesos (listado, edición y eliminación de usuarios).
2. **`soporte-tecnico-api`** (Puerto `8080` / Context Path `/api`): Microservicio Spring Boot encargado de la lógica de negocio de tickets (creación, asignación de técnicos especialistas, cambios de estado, historial de auditoría y borrado).
3. **`frontend`** (Web SPA / Vanilla CSS & JS): Interfaz gráfica de usuario premium, totalmente responsiva y reactiva que se comunica de forma asíncrona con ambos microservicios.

---

## ⚙️ Arquitectura y Seguridad JWT

El sistema implementa una **arquitectura sin estado (Stateless)** basada en **JWT (JSON Web Tokens)**:

```
  [ Cliente SPA ] --(1) POST /api/v1/auth/login ---------> [ usuarios-api (8083) ]
        |                                                              |
        |<------------(2) Retorna JWT Token con Rol (JWT) -------------|
        |
  [ Cliente SPA ] --(3) Peticiones CRUD (Bearer JWT) ----> [ soporte-tecnico-api (8080) ]
                                                                       |
                                                           (Valida firma localmente)
                                                                       |
                                                          <--- (4) Retorna JSON ---
```

* **Clave Compartida Simétrica**: Ambos microservicios validan localmente la firma del token JWT utilizando la clave compartida:
  `EstaEsUnaClaveSuperSecretaParaZegel2026Backend`
* **Exclusiones de Seguridad (CORS)**: Configurado para admitir llamadas cruzadas (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`) provenientes de la interfaz frontend.

---

## 💾 Configuración de la Base de Datos (MySQL)

El sistema sigue el patrón de **base de datos por servicio** en MySQL/MariaDB (puerto `3306`):
* **usuarios-api**: base de datos `usuariosdb`
* **soporte-tecnico-api**: base de datos `soportedb`

### Opción A: Levantar con Docker (Recomendada 🐳)
Inicia la base de datos MySQL y la estructura inicial automáticamente con un solo comando desde la raíz:
```bash
docker compose up -d
```
*Esto levantará un contenedor MySQL en el puerto 3306, configurará la clave en `12345` y cargará el script `db-init/init.sql` inicializando ambas bases de datos.*

### Opción B: Instalación Local Tradicional
1. Asegúrate de tener el motor de base de datos MySQL corriendo en el puerto `3306`.
2. Crea manualmente las bases de datos vacías en tu gestor:
   ```sql
   CREATE DATABASE usuariosdb;
   CREATE DATABASE soportedb;
   ```
3. El usuario configurado por defecto es `root` con contraseña `12345` (puedes cambiarlo en los archivos `application.properties` de cada microservicio).

---

## 🚀 Instrucciones de Inicio y Ejecución

### Prerrequisitos:
* **Java JDK 17** o superior.
* **Docker y Docker Compose** (opcional, para base de datos).
* Navegador web moderno.

### 1. Levantar el Backend:

#### A. Iniciar Microservicio de Usuarios (`usuarios-api`):
Navega a la carpeta `usuarios-api` y ejecuta:
```bash
# Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```
*El servicio se iniciará en:* `http://localhost:8083`

#### B. Iniciar Microservicio de Soporte (`soporte-tecnico-api`):
Navega a la carpeta `soporte-tecnico-api` y ejecuta:
```bash
# Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```
*El servicio se iniciará en:* `http://localhost:8080/api`

---

### 2. Levantar el Frontend:
Navega a la carpeta `frontend/` y abre el archivo **`index.html`** directamente en tu navegador. 

Para un mejor comportamiento de rutas locales, se recomienda levantarlo usando un servidor web de desarrollo local como *Live Server* en VSCode, o mediante Node.js:
```bash
# Instalar un servidor estático rápido
npm install -g serve

# Servir la carpeta frontend en el puerto 5000 o similar
serve -l 5000 frontend
```
Abre la dirección indicada en tu navegador (ej. `http://localhost:5000`).

---

## 🔑 Credenciales y Cuentas de Prueba

Inicia sesión en la pantalla de acceso con cualquiera de las siguientes cuentas pre-sembradas en el sistema:

| Usuario | Contraseña | Rol / Permisos | Acciones Disponibles |
| :--- | :--- | :--- | :--- |
| **`admin`** | `1234` | **ADMIN** | Registro/edición/borrado de usuarios, asignación de técnicos, cambio de estados, estadísticas, borrado e historial de tickets. |
| **`tecnico`** | `1234` | **TECNICO** | Ver solicitudes asignadas en su panel, cambiar estado a "En Proceso" o "Resuelto". |
| **`luis`** | `1234` | **TECNICO** | Técnico especialista (exclusivo para casos complejos de soporte). |
| **`cliente`** | `1234` | **CLIENTE** | Registrar nuevas solicitudes (vinculación automática de nombre) y ver su propio historial. |

---

## 🌐 Endpoints y Documentación Swagger UI

### 📄 soporte-tecnico-api
* **Swagger UI (Pruebas Interactivas)**: [http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html)
* **Endpoints Clave**:
  * `GET /api/v1/solicitudes-soporte` (Listado paginado general - ADMIN)
  * `GET /api/v1/solicitudes-soporte/mis-solicitudes` (Listado propio - CLIENTE)
  * `GET /api/v1/solicitudes-soporte/mis-asignaciones` (Listado propio - TECNICO)
  * `POST /api/v1/solicitudes-soporte` (Creación de incidencia)
  * `PATCH /api/v1/solicitudes-soporte/{id}/asignar-tecnico?tecnico={username}` (Asignar técnico - ADMIN)
  * `DELETE /api/v1/solicitudes-soporte/{id}` (Eliminar incidencia - ADMIN)

### 📄 usuarios-api
* **Endpoints Clave**:
  * `POST /api/v1/auth/login` (Autenticación e inicio de sesión)
  * `POST /api/v1/auth/register` (Registro de personal)
  * `GET /api/v1/auth/users` (Ver lista de personal - ADMIN)
  * `PUT /api/v1/auth/users/{username}` (Editar rol y contraseña - ADMIN)
  * `DELETE /api/v1/auth/users/{username}` (Eliminar personal - ADMIN)

---

## 📦 Cómo Subir el Proyecto a tu Repositorio de GitHub

Sigue estos comandos en tu terminal local para inicializar el repositorio y subir el código a tu cuenta de GitHub:

```bash
# 1. Inicializar el repositorio Git local en la raíz del proyecto
git init

# 2. Agregar todos los archivos al área de preparación (stage)
git add .

# 3. Guardar el estado inicial (Commit)
git commit -m "feat: implementacion de panel reactivo de soporte TI y gestion de usuarios"

# 4. Crear una rama principal llamada 'main'
git branch -M main

# 5. Vincular el repositorio local con tu repositorio remoto de GitHub
# (Reemplaza la URL por la de tu repositorio remoto de GitHub)
git remote add origin https://github.com/TU_USUARIO/TU_REPOSITORIO.git

# 6. Empujar el código a la rama principal de GitHub
git push -u origin main
```