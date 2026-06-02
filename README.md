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

## Usuarios

Este servicio no gestiona usuarios manualmente. Cada usuario se crea automaticamente
(just-in-time) a partir de los claims del JWT la primera vez que realiza una accion.

## Endpoints

Colecciones (`/api/collections`):

- `GET /api/collections`: lista las colecciones visibles para el usuario (publicas + propias; el admin ve todas)
- `GET /api/collections/{id}`: obtiene una coleccion accesible
- `POST /api/collections`: crea una coleccion (`name`, `visibility`)
- `PUT /api/collections/{id}`: actualiza una coleccion propia (o cualquiera si es admin)
- `DELETE /api/collections/{id}`: elimina una coleccion propia (o cualquiera si es admin)

Flash cards:

- `GET /api/collections/{collectionId}/flashcards`: lista las tarjetas de una coleccion accesible
- `POST /api/collections/{collectionId}/flashcards`: crea una tarjeta (`title`, `description`, `question`, `answer`)
- `GET /api/flashcards/{id}`: obtiene una tarjeta accesible
- `PUT /api/flashcards/{id}`: actualiza una tarjeta propia (o cualquiera si es admin)
- `DELETE /api/flashcards/{id}`: elimina una tarjeta propia (o cualquiera si es admin)

Uso / historial:

- `POST /api/flashcards/{id}/use`: registra el uso de una tarjeta por el usuario actual
- `GET /api/usage/me`: resumen e historial de uso del usuario actual (base para gamificacion)

Reglas de acceso:

- Todos los endpoints requieren un JWT valido (`estudiante`, `tutor`, `admin`).
- Un `estudiante` solo puede crear colecciones privadas; `tutor` y `admin` pueden crear privadas o publicas.
- Las colecciones publicas son visibles para cualquier usuario autenticado; las privadas solo para su autor y el `admin`.
- Cada usuario solo puede editar/eliminar sus propias colecciones y tarjetas; el `admin` puede sobre todas.
