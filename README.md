# ECIWISE Study
 
Proyecto Spring Boot preparado con PostgreSQL, JPA, validación y una API para el módulo de estudio de ECIWise.
 
Este microservicio NO autentica usuarios por email/password. La autenticación se hace en el microservicio de auth y aquí solo se valida el JWT recibido.
 
---
 
## Requisitos
 
- Java 21
- Maven 3.9+
- Docker y Docker Compose
---
 
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
---
 
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
- `monitor`
- `admin`
---
 
## Ejecutar
 
```bash
mvn spring-boot:run
```
 
---
 
## Usuarios
 
Este servicio no gestiona usuarios manualmente. Cada usuario se crea automáticamente (just-in-time) a partir de los claims del JWT la primera vez que realiza una acción.
 
---
 
## Endpoints
 
### Materias (`/api/subjects`)
 
- `GET /api/subjects` — lista todas las materias (cualquier usuario autenticado)
- `GET /api/subjects/{id}` — obtiene una materia
- `POST /api/subjects` — crea una materia (`name`, `code`) — solo admin y monitor
- `PUT /api/subjects/{id}` — actualiza una materia — solo admin y monitor
- `DELETE /api/subjects/{id}` — elimina una materia — solo admin y monitor
### Colecciones (`/api/collections`)
 
- `GET /api/collections` — lista las colecciones visibles para el usuario (públicas + propias; admin y monitor ven todas)
- `GET /api/collections/mine` — lista las colecciones propias del usuario (filtros opcionales: `subjectId`, `corte`)
- `GET /api/collections/public` — lista las colecciones públicas de otros usuarios (filtros opcionales: `subjectId`, `corte`)
- `GET /api/collections/{id}` — obtiene una colección accesible
- `POST /api/collections` — crea una colección (`name`, `visibility`, `subjectId`, `corte`)
- `PUT /api/collections/{id}` — actualiza una colección
- `DELETE /api/collections/{id}` — elimina una colección
### Flashcards
 
- `GET /api/collections/{collectionId}/flashcards` — lista las tarjetas de una colección accesible
- `POST /api/collections/{collectionId}/flashcards` — crea una tarjeta (`title`, `description`, `question`, `answer`)
- `GET /api/flashcards/{id}` — obtiene una tarjeta
- `PUT /api/flashcards/{id}` — actualiza una tarjeta
- `DELETE /api/flashcards/{id}` — elimina una tarjeta
### Preguntas formales (`/api/questions`)
 
- `GET /api/collections/{collectionId}/questions` — lista las preguntas de una colección
- `POST /api/collections/{collectionId}/questions` — crea una pregunta — solo admin y monitor
- `GET /api/questions/{id}` — obtiene una pregunta
- `PUT /api/questions/{id}` — actualiza una pregunta — solo admin y monitor
- `DELETE /api/questions/{id}` — elimina una pregunta — solo admin y monitor
Tipos de pregunta soportados (`type`):
 
- `TRUE_FALSE` — verdadero o falso
- `OPEN` — respuesta abierta (evaluada por similitud de texto)
- `CLOSED` — selección múltiple (mín 2, máx 5 opciones)
### Sesiones de estudio (`/api/study`)
 
- `POST /api/study/parcial` — inicia una sesión modo Parcial (`subjectId`, `corte`, `examDate`, `targetGrade`)
- `POST /api/study/repaso` — inicia una sesión modo Repaso (`subjectId`, `corte`, `questionType`)
- `GET /api/study/sessions/{id}` — obtiene el detalle de una sesión
- `POST /api/study/sessions/{id}/answer` — responde una pregunta de la sesión (`sessionQuestionId`, `answer`)
- `POST /api/study/sessions/{id}/complete` — completa la sesión y calcula el score
- `GET /api/study/history` — historial de sesiones completadas (filtros opcionales: `subjectId`, `corte`, `mode`)
### Uso / historial de flashcards
 
- `POST /api/flashcards/{id}/use` — registra el uso de una tarjeta
- `GET /api/usage/me` — historial de uso del usuario actual
---
 
## Reglas de acceso
 
- Todos los endpoints requieren un JWT válido.
- `estudiante` solo puede crear colecciones **privadas** con flashcards. No puede crear preguntas formales (T/F, Abierta, Cerrada).
- `monitor` y `admin` pueden crear colecciones públicas o privadas, gestionar materias y crear/editar/eliminar preguntas formales en cualquier colección.
- Las colecciones públicas son visibles para cualquier usuario autenticado; las privadas solo para su autor, monitor y admin.
- El score del Parcial y del Repaso se calcula sobre **5.0**.
---
