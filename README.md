# ECIWISE Study

Proyecto Spring Boot preparado con PostgreSQL, JPA, validación y una API base para estudiantes.

Este microservicio NO autentica usuarios por email/password. La autenticación se hace en el microservicio de auth y aquí solo se valida el JWT recibido.

## Requisitos

- Java 21
- Maven 3.9+
- Docker y Docker Compose

## Base de datos

Levanta PostgreSQL con:

```bash
docker compose up -d
```

La aplicación usa por defecto estas credenciales:

- URL: `jdbc:postgresql://localhost:5432/eciwise_study`
- Usuario: `postgres`
- Contraseña: `postgres`

Puedes sobrescribirlas con variables de entorno:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## JWT esperado

Variable requerida para validar firma:

- `JWT_SECRET`

El token debe llegar en `Authorization: Bearer <token>` y contener estos claims:

```json
{
	"sub": "<id-usuario>",
	"email": "usuario@dominio.com",
	"nombre": "Nombre",
	"apellido": "Apellido",
	"rol": "estudiante"
}
```

Roles soportados:

- `estudiante`
- `tutor`
- `admin`

## Ejecutar

```bash
mvn spring-boot:run
```

## Endpoints base

- `GET /api/students`
- `POST /api/students`

Reglas de acceso:

- `GET /api/students`: `estudiante`, `tutor`, `admin`
- `POST /api/students`: `tutor`, `admin`
