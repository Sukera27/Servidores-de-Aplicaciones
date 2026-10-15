# pokemon-api

API de ejemplo para el Tema 2 (Servidores de Aplicaciones) de la asignatura **Despliegue de Aplicaciones Web**. Una única tabla, `Pokemon` (`id`, `nombre`, `tipo`), con base de datos **H2 embebida en memoria**: no necesita Docker, MySQL ni ningún servicio externo. Los pasos completos para instalar Tomcat y desplegar esta API están en `../README_2.md` y `../index.html`.

Requisitos para compilarla: **Java 17 o superior** y **Maven**.

> Usa **Spring Boot 3.5** (API Servlet 6.0), que encaja con el `tomcat10` de Ubuntu 24.04. Spring Boot 4 exigiría Tomcat 11.

## Ejecutar en local

```bash
mvn spring-boot:run
```

Al arrancar carga automáticamente 4 pokémon de ejemplo (Pikachu, Charmander, Squirtle, Bulbasaur).

- API: `http://localhost:8080/api/v1/pokemon`
- Consola H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:pokemondb`, usuario `sa`, contraseña en blanco)

## Endpoints

| Método | Ruta | Qué hace |
|---|---|---|
| `GET` | `/api/v1/pokemon` | Lista todos los pokémon |
| `GET` | `/api/v1/pokemon/{id}` | Devuelve uno por id |
| `POST` | `/api/v1/pokemon` | Crea uno nuevo (`{"nombre": "...", "tipo": "..."}`) |
| `PUT` | `/api/v1/pokemon/{id}` | Actualiza nombre y tipo |
| `DELETE` | `/api/v1/pokemon/{id}` | Borra uno |

## Generar el .war para desplegar en Tomcat

```bash
mvn clean package
```

Genera `target/pokemon-api.war` (el nombre lo fija `<finalName>` en `pom.xml`, sin sufijo de versión). El proceso de despliegue en Tomcat está detallado en `../README_2.md` (apartados 5 y 6).
