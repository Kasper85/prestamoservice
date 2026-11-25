# PrestaControl API (`prestamoservice`)

**Autor:** Julio Rodriguez  

API REST en Spring Boot 3 + Java 21 protegida con JWT (`jjwt`) y Spring Security stateless.

## Requisitos
- Java 21+
- Maven (o usar `.\mvnw.cmd` en Windows / `./mvnw` en Linux/macOS)
- PostgreSQL (o perfil `local` con H2 en archivo para pruebas rápidas sin PostgreSQL)

## Variables de entorno
Copia `.env.example` y configura:
- `DB_URL` (ej. `jdbc:postgresql://localhost:5432/prestacontrol`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (clave en Base64 de al menos 32 bytes)
- `JWT_EXPIRATION_MS` (default `3600000` = 1 hora)
- `PORT` (default `8080`)

## Ejecución local (Windows)
```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

## Despliegue en Render (Docker + PostgreSQL)
1. Sube este repositorio a GitHub/GitLab.
2. En Render, crea una base de datos PostgreSQL (`prestacontrol-db`) o usa el Blueprint `render.yaml`.
3. Crea un **Web Service** con runtime **Docker** apuntando al `Dockerfile`.
4. Configura `DB_URL` en formato JDBC:
   `jdbc:postgresql://<host>:5432/<database>`
   (Render entrega `postgresql://user:pass@host/db`; extrae `host`, `database`, `DB_USERNAME` y `DB_PASSWORD`).
5. Configura `JWT_SECRET` (Base64 >= 32 bytes) y `JWT_EXPIRATION_MS=3600000`.

## Colección Postman
Importa `postman/PrestaControl.postman_collection.json`. Incluye:
- Variables `{{baseUrl}}` y `{{token}}`
- Guardado automático del token en Register/Login
- Pruebas de 401 sin token y con token alterado
