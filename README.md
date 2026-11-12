# Academy Security API

API REST educativa para administrar estudiantes, cursos y matrículas. Está construida con Java y Spring Boot e incluye persistencia en MySQL, validación de datos y autenticación stateless mediante JWT y roles.

## Contenido

- [Requisitos](#requisitos)
- [Inicio rápido](#inicio-rápido)
- [Conectar MySQL Workbench](#conectar-mysql-workbench)
- [Configuración](#configuración)
- [Autenticación](#autenticación)
- [Endpoints](#endpoints)
- [Ejemplos](#ejemplos)
- [Paginación](#paginación)
- [Pruebas](#pruebas)
- [Estructura](#estructura)

## Requisitos

- Java 21
- Maven 3.9 o posterior
- Docker con Docker Compose

## Inicio rápido

Desde la carpeta del proyecto, inicia MySQL:

```bash
docker compose up -d
docker compose ps
```

Inicia la API en otra terminal:

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Compose inicia únicamente la base de datos; la aplicación Java se ejecuta con Maven en el equipo anfitrión.

Al terminar, detén el contenedor sin borrar los datos:

```bash
docker compose down
```

> **Importante:** `docker compose down -v` también elimina el volumen de MySQL y todos los datos guardados en él.

## Conectar MySQL Workbench

Con el contenedor iniciado, crea una conexión en MySQL Workbench con estos valores de desarrollo:

| Campo | Valor |
|---|---|
| Hostname | `127.0.0.1` |
| Port | `3307` |
| Username | `root` |
| Password | `root` |
| Default Schema | `academia` |

El puerto del equipo anfitrión es `3307`; dentro de Docker, MySQL escucha en `3306`. La base de datos se crea automáticamente al iniciar el servicio. Estos valores son solo para desarrollo local y no deben usarse en producción.

## Configuración

La configuración local predeterminada está en `src/main/resources/application.properties`. Se puede sobrescribir mediante variables de entorno de Spring Boot:

| Variable | Uso |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de MySQL. Para Compose local: `jdbc:mysql://127.0.0.1:3307/academia?useSSL=false&serverTimezone=UTC`. |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos; en Compose local es `root`. |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos; en Compose local es `root`. |
| `APP_JWT_SECRET` | Clave de firma JWT; usa una clave aleatoria de al menos 32 bytes. |

Por ejemplo, para configurar la clave JWT en una terminal Bash antes de iniciar la API:

```bash
export APP_JWT_SECRET="$(openssl rand -base64 48)"
mvn spring-boot:run
```

La base de datos local está configurada para crear/actualizar el esquema automáticamente. No uses las credenciales ni la configuración de desarrollo para desplegar la aplicación en un entorno real.

## Autenticación

La API usa tokens JWT tipo Bearer. Obtén un token enviando las credenciales de una cuenta de práctica:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

La respuesta incluye `token`, `tokenType` (`Bearer`) y `expiresIn` (segundos). Envía el token en las rutas protegidas:

```bash
curl http://localhost:8080/api/v1/auth/me \
  -H 'Authorization: Bearer <TOKEN>'
```

Cuentas creadas automáticamente para practicar (no usar en producción):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `ana` | `ana123` | `USER` |

El inicio de sesión, la verificación de salud y Swagger son públicos. Las demás rutas requieren autenticación, excepto que la creación de cursos y las rutas `/internal/**` requieren el rol `ADMIN`.

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Público | Inicia sesión y entrega un JWT. |
| `GET` | `/api/v1/auth/me` | Autenticado | Devuelve el usuario y su rol. |
| `GET` | `/api/v1/students` | Autenticado | Lista estudiantes paginados. |
| `GET` | `/api/v1/students/{id}` | Autenticado | Busca un estudiante por ID. |
| `GET` | `/api/v1/students/by-email?email=...` | Autenticado | Busca un estudiante por correo. |
| `POST` | `/api/v1/students` | Autenticado | Crea un estudiante. |
| `GET` | `/api/v1/courses` | Autenticado | Lista cursos paginados. |
| `GET` | `/api/v1/courses/{id}` | Autenticado | Busca un curso por ID. |
| `GET` | `/api/v1/courses/{id}/enrolled` | Autenticado | Consulta cuántos estudiantes están matriculados. |
| `POST` | `/api/v1/courses` | `ADMIN` | Crea un curso. |
| `GET` | `/api/v1/enrollments/{id}` | Autenticado | Busca una matrícula por ID. |
| `POST` | `/api/v1/enrollments` | Autenticado | Matricula un estudiante en un curso. |
| `GET` | `/internal/health` | Público | Comprueba que la API está disponible. |
| `POST` | `/internal/seed` | `ADMIN` | Carga datos de ejemplo. |

Los datos de entrada se validan. Por ejemplo, el estudiante debe tener nombre y correo válidos, y edad entre 16 y 99 años; el curso requiere precio positivo y cupo mínimo de 1. No se permiten correos duplicados, matrículas duplicadas ni superar el cupo de un curso.

## Ejemplos

### Cargar datos de práctica

Después de obtener un JWT de administrador, reemplaza `<TOKEN>`:

```bash
curl -X POST http://localhost:8080/internal/seed \
  -H 'Authorization: Bearer <TOKEN>'
```

La respuesta informa cuántos estudiantes y cursos se crearon. La operación puede repetirse: no duplica estudiantes de ejemplo ni vuelve a crear cursos si ya hay cursos registrados.

### Crear un estudiante

```bash
curl -X POST http://localhost:8080/api/v1/students \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Carlos Ruiz","age":20,"email":"carlos@example.com"}'
```

### Crear un curso

Esta ruta requiere un token con rol `ADMIN`:

```bash
curl -X POST http://localhost:8080/api/v1/courses \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Java avanzado","price":150000,"maxCapacity":25}'
```

### Matricular un estudiante

Usa los IDs existentes de estudiante y curso:

```bash
curl -X POST http://localhost:8080/api/v1/enrollments \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"studentId":1,"courseId":1}'
```

### Documentación interactiva

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

En Swagger UI, usa **Authorize** para proporcionar un JWT en las operaciones protegidas.

## Paginación

Los listados de estudiantes y cursos aceptan `page`, `size` y `sort`. La primera página es `0`, el tamaño predeterminado es `10` y el máximo permitido es `50`.

```text
GET /api/v1/students?page=0&size=10&sort=name,asc
```

La respuesta incluye `content`, `page`, `size`, `totalElements` y `totalPages`.

## Pruebas

Ejecuta las pruebas automatizadas:

```bash
mvn test
```

Las pruebas usan una base H2 en memoria; no requieren iniciar MySQL.

## Estructura

```text
.
├── compose.yaml
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/academy/
│   │   │   ├── AcademyApplication.java
│   │   │   ├── business/
│   │   │   │   ├── exception/       # Excepciones de negocio
│   │   │   │   └── service/         # Lógica de estudiantes, cursos y matrículas
│   │   │   ├── persistence/
│   │   │   │   ├── entity/          # Entidades JPA
│   │   │   │   └── repository/      # Repositorios Spring Data
│   │   │   ├── presentation/
│   │   │   │   ├── config/         # Configuración de OpenAPI
│   │   │   │   ├── controller/     # Endpoints REST
│   │   │   │   ├── dto/            # Solicitudes y respuestas de la API
│   │   │   │   ├── exception/      # Manejo global de errores HTTP
│   │   │   │   └── mapper/         # Conversión entre entidades y DTO
│   │   │   └── security/           # Login, JWT y autorización
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/academy/
│       │   └── AcademyApiIntegrationTest.java
│       └── resources/
│           └── application-test.properties
└── README.md
```
