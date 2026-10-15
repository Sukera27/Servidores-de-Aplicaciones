<div align="center">

# 🚀 Tema 2 · Servidores de Aplicaciones

### Tomcat en Ubuntu (Azure) + despliegue de una API Spring Boot con H2

![Ubuntu 24.04](https://img.shields.io/badge/Ubuntu_24.04-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)
![Azure](https://img.shields.io/badge/Azure-0078D4?style=for-the-badge&logo=microsoftazure&logoColor=white)
![Tomcat 10](https://img.shields.io/badge/Tomcat_10-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=black)
![Spring Boot 3.5](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java 17](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2_Database-0F4C81?style=for-the-badge)

*Escuela Profesional Vedruna Sevilla · 2º DAW · Despliegue de Aplicaciones Web*

</div>

---

Apuntes prácticos de la asignatura **Despliegue de Aplicaciones Web (DAW)**. Este documento recoge, en orden cronológico, todos los comandos necesarios para instalar Apache Tomcat (con `apt`) en la misma VM Ubuntu de Azure del Tema 1 y desplegar en él la API de ejemplo `pokemon-api` (tabla Pokémon: `id`, `nombre`, `tipo`; base de datos **H2 embebida**, sin Docker ni MySQL) empaquetada como `.war`.

Los apuntes en formato web están en [`index.html`](./index.html). El código de la API está en [`pokemon-api/`](./pokemon-api).

## 📖 Índice

- [Contenido de esta carpeta](#contenido-de-esta-carpeta)
- [Requisitos previos](#requisitos-previos)
- [0. Conectarse a la VM del Tema 1](#0-conectarse-a-la-vm-del-tema-1)
- [1. Comprobar la versión de Ubuntu y qué Tomcat ofrece `apt`](#1-comprobar-la-versión-de-ubuntu-y-qué-tomcat-ofrece-apt)
- [2. Instalar Tomcat](#2-instalar-tomcat)
- [3. Configurar el firewall (UFW) y el NSG de Azure — puerto 8080](#3-configurar-el-firewall-ufw-y-el-nsg-de-azure--puerto-8080)
- [4. Rutas de configuración que hay que conocer](#4-rutas-de-configuración-que-hay-que-conocer)
- [5. La API `pokemon-api`: cómo está preparada para desplegarse como WAR](#5-la-api-pokemon-api-cómo-está-preparada-para-desplegarse-como-war)
- [6. Desplegar el WAR en Tomcat](#6-desplegar-el-war-en-tomcat)
- [7. Comandos de gestión — referencia rápida](#7-comandos-de-gestión--referencia-rápida)
- [Solución de problemas habituales](#solución-de-problemas-habituales)
- [Tarea extra: acceder al Manager App](#tarea-extra-acceder-al-manager-app)

---

## Contenido de esta carpeta

| Archivo / carpeta | Descripción |
|---|---|
| `index.html` | Apuntes del Tema 2 (servidor de aplicaciones vs servidor web, Tomcat y alternativas, instalación, firewall, despliegue de un WAR) |
| `README_2.md` | Este archivo: comandos en orden cronológico |
| `pokemon-api/` | Proyecto Spring Boot completo (WAR + H2) usado en la práctica |

## Requisitos previos

- La VM Ubuntu **24.04** de Azure del [Tema 1](../TEMA%201-Servidores%20WEB/README_1.md), con acceso SSH.
- Java 17 o superior y Maven instalados en tu ordenador para compilar `pokemon-api` (la base de datos va embebida, no hace falta nada más).

---

## 0. Conectarse a la VM del Tema 1

```bash
ssh usuario@<IP_PUBLICA_VM>
```

No hace falta crear una VM nueva: Tomcat se instala junto al Apache2 del Tema 1, cada uno en su puerto.

## 1. Comprobar la versión de Ubuntu y qué Tomcat ofrece `apt`

```bash
lsb_release -ds
apt-cache policy tomcat10 tomcat11
```

El paquete de Tomcat disponible depende de la versión de Ubuntu, y **tiene que encajar con la versión de Spring Boot** (cada Spring Boot exige una versión mínima de la API Servlet):

| Spring Boot | API Servlet | Tomcat | Paquete `apt` |
|---|---|---|---|
| 2.x (`javax.*`) | 4.0 | 9 | `tomcat9` (Ubuntu 22.04) |
| 3.x (`jakarta.*`) | 6.0 | 10.1 | `tomcat10` (Ubuntu 24.04) |
| 4.x | 6.1 | 11 | `tomcat11` (Ubuntu 25.10 / 26.04) |

> Nuestra VM es Ubuntu 24.04 → **`tomcat10`**, y por eso `pokemon-api` usa **Spring Boot 3.5**. Si el `.war` está hecho con una versión de Spring Boot que exige más Servlet de la que da Tomcat, se despliega pero la aplicación no arranca.

## 2. Instalar Tomcat

```bash
sudo apt update
sudo apt install tomcat10 -y
```

`apt` instala también Java si hace falta, crea el usuario `tomcat`, registra el servicio de `systemd` y lo arranca y habilita. No incluye las aplicaciones de ejemplo ni el Manager (son paquetes aparte; el Manager se ve en la tarea extra del final).

```bash
sudo systemctl status tomcat10     # debe poner: active (running)
curl http://localhost:8080         # prueba en local, sin depender del firewall
```

## 3. Configurar el firewall (UFW) y el NSG de Azure — puerto 8080

```bash
sudo ufw allow 8080/tcp
sudo ufw status
```

Portal de Azure → recurso de la VM → **Redes (Networking)** → **Reglas de puertos de entrada** → **Agregar regla de puerto de entrada**:

- Puerto: `8080`
- Protocolo: `TCP`
- Acción: `Permitir`

Comprobar desde el navegador: `http://<IP_PUBLICA_VM>:8080`

## 4. Rutas de configuración que hay que conocer

| Ruta | Qué es |
|---|---|
| `/var/lib/tomcat10/webapps/` | Carpeta de despliegue: aquí van los `.war` |
| `/etc/tomcat10/server.xml` | Configuración principal (Connector del puerto 8080) |
| `/etc/default/tomcat10` | Variables del servicio (`JAVA_HOME`, `JAVA_OPTS`: memoria de la JVM) |
| `/var/log/tomcat10/catalina.out` | Log de arranque de Tomcat y de cada app desplegada |
| `/usr/share/tomcat10/` | Binarios de Tomcat (`CATALINA_HOME`) |
| `/var/lib/tomcat10/` | Datos de la instancia (`CATALINA_BASE`) |

## 5. La API `pokemon-api`: cómo está preparada para desplegarse como WAR

El proyecto ya está en la carpeta [`pokemon-api/`](./pokemon-api), listo para usar. Por defecto Spring Boot empaqueta un `.jar` con un Tomcat *embebido*; este proyecto está preparado para generar además un `.war` que arranca sobre nuestro Tomcat externo:

### 5.1 Versión de Spring Boot y empaquetado en `pom.xml`

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.16</version>   <!-- Servlet 6.0 = Tomcat 10.1 -->
</parent>

<packaging>war</packaging>
```

### 5.2 Starter de Tomcat como `provided`, y H2 como `runtime`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

`provided` hace que compile contra las clases de Servlet sin empaquetar un Tomcat embebido dentro del `.war`. H2 va como `runtime`: es una base de datos en memoria, no hace falta instalar ni levantar nada aparte.

### 5.3 La clase principal extiende `SpringBootServletInitializer`

`pokemon-api/src/main/java/com/vedruna/pokemonapi/PokemonApiApplication.java`:

```java
package com.vedruna.pokemonapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PokemonApiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PokemonApiApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PokemonApiApplication.class);
    }
}
```

Esto no rompe `mvn spring-boot:run` en local: sigue arrancando igual, con Tomcat embebido.

### 5.4 Probar en local (opcional pero recomendable)

```bash
cd pokemon-api
mvn spring-boot:run
```

`http://localhost:8080/api/v1/pokemon` debería devolver ya 4 pokémon de ejemplo (los crea un `DataLoader` al arrancar: Pikachu, Charmander, Squirtle y Bulbasaur).

### 5.5 Empaquetar

```bash
mvn clean package
```

El `pom.xml` fija `<finalName>pokemon-api</finalName>`, así que siempre genera `target/pokemon-api.war` (sin sufijos de versión que renombrar a mano).

## 6. Desplegar el WAR en Tomcat

### 6.1 Copiar el archivo a la VM

```bash
scp target/pokemon-api.war usuario@<IP_PUBLICA_VM>:/tmp/
```

### 6.2 Copiarlo a `webapps/`

```bash
ssh usuario@<IP_PUBLICA_VM>
sudo cp /tmp/pokemon-api.war /var/lib/tomcat10/webapps/
sudo chown tomcat:tomcat /var/lib/tomcat10/webapps/pokemon-api.war
```

Tomcat lo detecta, lo descomprime en `webapps/pokemon-api/` y lo despliega en el contexto `/pokemon-api`. No hay que reiniciar nada.

> **Gotcha del context path:** al desplegar en un Tomcat externo, la ruta base pasa a ser **el nombre del `.war`** (no lo que diga `application.properties`): `pokemon-api.war` → `/pokemon-api/`; `ROOT.war` → raíz (`/`).

### 6.3 Verificar

```bash
sudo tail -f /var/log/tomcat10/catalina.out
```

(Sal con `Ctrl+C` cuando aparezca `Deployment of web application archive ... has finished`.)

```
http://<IP_PUBLICA_VM>:8080/pokemon-api/api/v1/pokemon
```

Debería devolver el JSON con los 4 pokémon de ejemplo. La consola de H2 también queda accesible para ver la tabla:

```
http://<IP_PUBLICA_VM>:8080/pokemon-api/h2-console
# JDBC URL: jdbc:h2:mem:pokemondb · usuario: sa · contraseña: (en blanco)
```

> **Por qué H2 en memoria simplifica esto:** al no depender de MySQL ni de Docker, no hay que preocuparse de si la VM tiene red hasta una base de datos externa — vive dentro del propio proceso de Tomcat. A cambio, los datos se pierden si se reinicia Tomcat (el `DataLoader` los vuelve a crear en el siguiente arranque).

### 6.4 Redesplegar tras un cambio en el código

```bash
sudo systemctl stop tomcat10
sudo rm -rf /var/lib/tomcat10/webapps/pokemon-api /var/lib/tomcat10/webapps/pokemon-api.war
sudo cp /tmp/pokemon-api.war /var/lib/tomcat10/webapps/
sudo chown tomcat:tomcat /var/lib/tomcat10/webapps/pokemon-api.war
sudo systemctl start tomcat10
```

---

## 7. Comandos de gestión — referencia rápida

```bash
sudo systemctl start|stop|restart tomcat10    # gestionar el servicio
sudo systemctl status tomcat10                # ver estado
sudo tail -f /var/log/tomcat10/catalina.out   # ver arranque / errores en tiempo real
ls /var/lib/tomcat10/webapps                  # apps desplegadas
mvn clean package                             # generar el .war
sudo ufw status                               # ver puertos permitidos
```

## Solución de problemas habituales

- **El `.war` se despliega pero la app no arranca y en `catalina.out` hay errores de `ClassNotFoundException` / `NoSuchMethodError` de `jakarta.servlet`**: la versión de Spring Boot no encaja con la de Tomcat (ver tabla del punto 1). Con Tomcat 10.1 hace falta Spring Boot 3.x; Spring Boot 4 exige Tomcat 11.
- **`404` al acceder a la app**: revisar el nombre del `.war` desplegado — el context path es ese nombre (sin `.war`), no el que aparece en `application.properties`.
- **La carpeta `webapps/pokemon-api/` no aparece tras copiar el `.war`**: mirar `sudo tail -n 50 /var/log/tomcat10/catalina.out` por si hubo un error al desplegar, y comprobar que el archivo es legible por Tomcat (`ls -l /var/lib/tomcat10/webapps`).
- **Los datos desaparecen tras reiniciar Tomcat**: es el comportamiento normal de H2 en memoria (`jdbc:h2:mem:pokemondb`); el `DataLoader` los vuelve a crear en cada arranque.
- **`OutOfMemoryError` en el log (VM con poca RAM)**: subir la memoria de la JVM en `/etc/default/tomcat10`, por ejemplo `JAVA_OPTS="-Djava.awt.headless=true -Xmx512m"`, y `sudo systemctl restart tomcat10`.
- **`Address already in use` en el puerto 8080**: hay otro proceso escuchando ahí; comprobar con `sudo ss -tlnp | grep 8080`.

## Tarea extra: acceder al Manager App

El **Manager App** es la interfaz web de Tomcat para ver, parar, arrancar y desplegar aplicaciones sin usar SSH. En Ubuntu viene en un paquete aparte (`tomcat10-admin`), no trae ningún usuario creado y, por seguridad, su configuración solo deja entrar a conexiones que vengan de la propia VM. Esta tarea consiste en instalarlo, crear un usuario, abrirlo a conexiones externas y entrar desde el navegador.

### 1. Instalar el paquete del Manager

```bash
sudo apt install tomcat10-admin -y
```

### 2. Crear un usuario con el rol `manager-gui`

Los usuarios se definen en `tomcat-users.xml`, dentro de la carpeta de configuración de Tomcat. Entramos en ella y abrimos el archivo con `nano`:

```bash
cd /etc/tomcat10
sudo nano tomcat-users.xml
```

Baja hasta el final del archivo y añade estas dos líneas **justo antes de** `</tomcat-users>` (después del último `-->`), cambiando la contraseña por la tuya:

```xml
<role rolename="manager-gui"/>
<user username="admin" password="ClaveTomcat2026" roles="manager-gui"/>
```

Te tiene que quedar así:

```xml
-->
  <role rolename="manager-gui"/>
  <user username="admin" password="ClaveTomcat2026" roles="manager-gui"/>
</tomcat-users>
```

Guarda con `Ctrl+O`, `Enter` y sal con `Ctrl+X`. El reinicio lo haremos al final del paso 3.

> **Ojo con el comentario y con el pegado:** el archivo trae usuarios de ejemplo (`admin`, `robot`, `tomcat`...) con la contraseña `<must-be-changed>`, pero están dentro de un comentario `<!-- ... -->`: no existen de verdad, y si añades tus líneas dentro de un comentario Tomcat las ignora y el login falla. Escríbelas **fuera** de cualquier comentario y, mejor, **a mano**: al pegar en `nano` a veces se pierde el primer carácter.

### 3. Configurar el contexto del Manager (`manager.xml`) y reiniciar

El archivo `/etc/tomcat10/Catalina/localhost/manager.xml` le dice a Tomcat dos cosas: **dónde está** la aplicación del Manager (`docBase`) y **qué IPs pueden entrar** (la `Valve`). Lo reescribimos con la ruta correcta y con la restricción de IP comentada:

```bash
sudo tee /etc/tomcat10/Catalina/localhost/manager.xml > /dev/null <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<Context docBase="/usr/share/tomcat10-admin/manager" antiResourceLocking="false" privileged="true">
  <!--
  <Valve className="org.apache.catalina.valves.RemoteAddrValve"
         allow="127\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1" />
  -->
</Context>
EOF
sudo systemctl restart tomcat10
```

- Sin `docBase`, Tomcat busca el Manager en `webapps/manager`, que no existe, y da **404**.
- Con la `Valve` activa, solo se puede entrar desde la propia VM; desde otra IP da **403** sin llegar a pedir la contraseña. Al comentarla, se puede entrar con el navegador por la IP pública.

### 4. Comprobar desde la VM

```bash
sleep 10
curl -s -o /dev/null -w "%{http_code}\n" -u admin:ClaveTomcat2026 http://localhost:8080/manager/html
```

Debe imprimir `200`. Si sale `401`, el usuario o la contraseña no coinciden con los de `tomcat-users.xml`.

> No uses `curl -I` para esta prueba: manda una petición `HEAD` y el Manager responde `403` por su filtro anti-CSRF aunque todo esté bien.

### 5. Entrar en el Manager desde el navegador

```
http://<IP_PUBLICA_VM>:8080/manager/html
```

El navegador muestra un recuadro de usuario y contraseña: `admin` y la contraseña del paso 2. Verás las aplicaciones desplegadas con los botones *Start*, *Stop*, *Reload* y *Undeploy*. El puerto 8080 ya está abierto desde el apartado 3.

### 6. Probar los botones y redesplegar desde el Manager

Prueba *Stop* y *Start* sobre `/pokemon-api` y comprueba en `http://<IP_PUBLICA_VM>:8080/pokemon-api/api/v1/pokemon` cómo deja de responder y vuelve. Para desplegar desde aquí: *Undeploy* de `/pokemon-api` y, en *WAR file to deploy*, subir de nuevo `pokemon-api.war` y pulsar *Deploy*.

> **Límite de 50 MB en el Manager:** rechaza subidas de más de 50 MB con un *403 Access Denied* que despista (parece un problema de permisos y no lo es). Si el `.war` pesa más, despliégalo copiándolo a `webapps/` como en el apartado 6. Comprobar el tamaño: `ls -lh target/pokemon-api.war`.

> **Seguridad: el Manager queda abierto a todo internet.** Con la `Valve` comentada, cualquiera que conozca la IP puede intentar entrar; solo lo protegen el usuario y la contraseña. Es cómodo para practicar, pero usa una contraseña propia (no la de estos apuntes) y no lo dejes así en un entorno real. Para eso está la alternativa del túnel SSH.

### Alternativa más segura: túnel SSH

Se deja la `Valve` activa, de modo que el Manager solo acepta conexiones de la propia VM, y se entra mediante un túnel SSH que hace que tu navegador "parezca" estar dentro de ella, sin abrir nada al exterior. Primero se vuelve a escribir `manager.xml` con la `Valve` sin comentar:

```bash
sudo tee /etc/tomcat10/Catalina/localhost/manager.xml > /dev/null <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<Context docBase="/usr/share/tomcat10-admin/manager" antiResourceLocking="false" privileged="true">
  <Valve className="org.apache.catalina.valves.RemoteAddrValve"
         allow="127\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1" />
</Context>
EOF
sudo systemctl restart tomcat10
```

Después, en una terminal de **tu ordenador** (no de la VM), dejándola abierta mientras uses el Manager:

```bash
ssh -L 8081:localhost:8080 usuario@<IP_PUBLICA_VM>
```

Y en el navegador, `http://localhost:8081/manager/html`: el puerto `8081` de tu ordenador queda conectado con el `8080` de la VM.

### Si algo falla

| Síntoma | Causa | Solución |
|---|---|---|
| `404` en `/manager/html` | Falta el `docBase` en `manager.xml` | Repetir el paso 3 |
| `403` y no aparece el recuadro de contraseña | La `Valve` está activa y entras desde otra IP | Comentar la `Valve` (paso 3) o usar el túnel SSH |
| Aparece el recuadro y el login falla (`401`) | Usuario o contraseña incorrectos, o escritos dentro del comentario | Revisar el paso 2 y reiniciar Tomcat |
| `403` con `curl -I` | `HEAD` no pasa el filtro anti-CSRF; no es un fallo real | Probar con el `curl` del paso 4 o con el navegador |

---

A medida que se añadan nuevos temas, se enlazarán desde el menú lateral de `index.html`, igual que el Tema 1 enlaza ya a este Tema 2.

---

<div align="center">

Hecho con ☕ y 💚 en **Vedruna Sevilla**

</div>
