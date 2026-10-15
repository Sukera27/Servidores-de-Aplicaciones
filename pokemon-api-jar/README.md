<div align="center">

# ☕ pokemon-api-jar

### La misma API de Pokémon, pero como `.jar` con Tomcat embebido

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Tomcat](https://img.shields.io/badge/Tomcat-embebido-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=black)
![H2](https://img.shields.io/badge/H2-Database-0F4C81?style=for-the-badge)
![Puerto](https://img.shields.io/badge/Puerto-8081-2ea44f?style=for-the-badge)

*Escuela Profesional Vedruna Sevilla · 2º DAW · Despliegue de Aplicaciones Web*

</div>

---

Es la misma API que [`pokemon-api`](../pokemon-api) (tabla `Pokemon`: `id`, `nombre`, `tipo`; base de datos **H2** en memoria; 4 pokémon de ejemplo al arrancar), pero empaquetada de otra forma: en vez de un `.war` que necesita un Tomcat instalado en la máquina, genera un **`.jar` ejecutable que lleva su propio Tomcat dentro** y escucha en el puerto **8081**.

## 🔀 `.war` vs `.jar`

| | `pokemon-api` (`.war`) | `pokemon-api-jar` (`.jar`) |
|---|---|---|
| Empaquetado (`pom.xml`) | `<packaging>war</packaging>` | `<packaging>jar</packaging>` |
| Tomcat | El de la VM (`tomcat10`), la app se copia a `webapps/` | **Embebido** dentro del jar (`spring-boot-starter-web`) |
| Cómo se ejecuta | La arranca Tomcat al detectar el `.war` | `java -jar pokemon-api-jar.jar` |
| Puerto | 8080 (el de Tomcat) | **8081** (`server.port` en `application.properties`) |
| URL de la API | `http://<IP>:8080/pokemon-api/api/v1/pokemon` | `http://<IP>:8081/api/v1/pokemon` |
| Clase principal | Extiende `SpringBootServletInitializer` | Solo `SpringApplication.run(...)` |
| Qué necesita la máquina | Tomcat + Java | **Solo Java** |

> 💡 Fíjate en la URL: con el `.war` la app cuelga de un **contexto** (`/pokemon-api`) que decide Tomcat según el nombre del archivo. Con el `.jar` la app es la dueña del servidor y se sirve directamente desde `/`.

---

## 🛠️ Compilar

Necesitas **JDK 17 o superior** y Maven en tu ordenador:

```bash
cd pokemon-api-jar
mvn clean package
```

Se genera `target/pokemon-api-jar.jar` (un jar "gordo" con todas las dependencias y el Tomcat dentro).

> ⚠️ Compila con un JDK **igual o más antiguo** que el Java de la VM. El `pom.xml` ya fuerza Java 17, que funciona en Ubuntu 24.04 (que trae Java 21). Si un JDK más nuevo genera clases que el Java de la VM no entiende, verás `UnsupportedClassVersionError`.

## ▶️ Probar en local

```bash
java -jar target/pokemon-api-jar.jar
```

Verás en la consola algo como `Tomcat started on port 8081 (http)`. Después:

```bash
curl http://localhost:8081/api/v1/pokemon
```

Para pararla: `Ctrl + C`.

---

## ☁️ Desplegar en la VM de Azure

No hace falta instalar ni tocar el Tomcat de la VM (que sigue en el 8080).

### 1️⃣ Subir el jar

```bash
scp target/pokemon-api-jar.jar usuario@<IP_PUBLICA_VM>:/tmp/
```

### 2️⃣ Comprobar que hay Java

```bash
ssh usuario@<IP_PUBLICA_VM>
java -version                    # debe ser 17 o superior
```

Si no está: `sudo apt install openjdk-17-jre-headless -y`. (Si ya instalaste `tomcat10` con `apt`, Java ya está.)

### 3️⃣ Abrir el puerto 8081

```bash
sudo ufw allow 8081/tcp
sudo ufw status
```

Y en Azure: **VM → Redes → Reglas de puerto de entrada → Agregar** una regla para el puerto `8081`, protocolo TCP.

### 4️⃣ Ejecutarlo

```bash
sudo mkdir -p /opt/pokemon-api-jar
sudo mv /tmp/pokemon-api-jar.jar /opt/pokemon-api-jar/
java -jar /opt/pokemon-api-jar/pokemon-api-jar.jar
```

Prueba desde la VM con `curl http://localhost:8081/api/v1/pokemon` y desde tu ordenador en el navegador:

```
http://<IP_PUBLICA_VM>:8081/api/v1/pokemon
```

De esta forma la aplicación se para al cerrar la sesión SSH. Para dejarla corriendo, mira el siguiente apartado.

### 5️⃣ Dejarla funcionando en segundo plano (servicio `systemd`)

Crea el archivo de servicio:

```bash
sudo nano /etc/systemd/system/pokemon-api-jar.service
```

```ini
[Unit]
Description=Pokemon API (jar con Tomcat embebido)
After=network.target

[Service]
User=usuario
ExecStart=/usr/bin/java -jar /opt/pokemon-api-jar/pokemon-api-jar.jar
SuccessExitStatus=143
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

> ✏️ Cambia `usuario` por tu usuario de la VM.

Actívalo:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now pokemon-api-jar
sudo systemctl status pokemon-api-jar      # debe poner: active (running)
```

Ver el log en directo: `sudo journalctl -u pokemon-api-jar -f`.

Para actualizar la app: sube el jar nuevo, `sudo mv` a `/opt/pokemon-api-jar/` y `sudo systemctl restart pokemon-api-jar`.

---

## 🧪 Endpoints

| Método | Endpoint | Descripción |
|:------:|----------|-------------|
| ![GET](https://img.shields.io/badge/GET-2ea44f?style=flat-square) | `/api/v1/pokemon` | Lista todos los pokémon |
| ![GET](https://img.shields.io/badge/GET-2ea44f?style=flat-square) | `/api/v1/pokemon/{id}` | Devuelve uno por id (`404` si no existe) |
| ![POST](https://img.shields.io/badge/POST-0969da?style=flat-square) | `/api/v1/pokemon` | Crea uno nuevo (`201 Created`) |
| ![PUT](https://img.shields.io/badge/PUT-bf8700?style=flat-square) | `/api/v1/pokemon/{id}` | Modifica `nombre` y `tipo` |
| ![DELETE](https://img.shields.io/badge/DELETE-cf222e?style=flat-square) | `/api/v1/pokemon/{id}` | Borra uno (`204 No Content`) |

```bash
curl -X POST http://localhost:8081/api/v1/pokemon \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Eevee", "tipo": "Normal"}'
```

### 🗄️ Consola de H2

`http://<IP>:8081/h2-console` — JDBC URL `jdbc:h2:mem:pokemondb`, usuario `sa`, contraseña vacía.

> ℹ️ La base de datos vive **en memoria**: al reiniciar la aplicación se pierden los cambios y vuelven los 4 pokémon de ejemplo.

---

## 🩹 Solución de problemas

- **`UnsupportedClassVersionError ... class file version 69.0`**: el jar se compiló con un JDK más nuevo que el Java de la VM. Recompila con `mvn clean package` usando JDK 17 (o el mismo que la VM).
- **`Port 8081 was already in use`**: ya hay algo en ese puerto (¿otra copia de la app?). Comprueba con `sudo ss -tlnp | grep 8081`.
- **No responde desde fuera pero sí con `curl localhost`**: falta abrir el 8081 en `ufw` o en el NSG de Azure.
- **`java: command not found`**: instala Java con `sudo apt install openjdk-17-jre-headless -y`.

---

<div align="center">

Hecho con ☕ y 💚 en **Vedruna Sevilla**

</div>
