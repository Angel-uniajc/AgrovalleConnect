# Diagrama de despliegue - AgroValle Connect

Documento de diseño asociado a la tarea **T04-DIS (#77)**, parte del Sprint de Diseño (#73).

## 1. Contexto y objetivo

AgroValle Connect es un backend **Java 17 + Spring Boot** que expone una API REST (`/api/v1`) y persiste en **PostgreSQL**. Este documento describe dónde se ejecuta cada componente del sistema, qué artefactos se despliegan en cada nodo, cómo se comunican y cómo llega el código desde el equipo de desarrollo hasta producción.

Para distinguir lo que ya existe de lo que es una decisión de diseño, el documento usa dos etiquetas:

- **Actual:** verificable en el repositorio (artefacto JAR, Java 17, PostgreSQL, H2 en pruebas, Checkstyle y Husky).
- **Propuesto:** decisión de diseño para el despliegue en producción, aún no implementada.

**Alcance:** el backend y su base de datos. El despliegue del cliente web o móvil queda fuera de este diagrama; el cliente aparece solo como consumidor de la API.

## 2. Nodos y artefactos

| Nodo | Tipo | Entorno de ejecución | Artefactos | Estado |
|---|---|---|---|---|
| Dispositivo del usuario | Dispositivo | Navegador web o app móvil | Aplicación cliente (fuera de alcance) | Externo |
| Nodo de entrada | Nodo de ejecución | Proxy inverso (Nginx o el del proveedor de despliegue) | Certificado TLS y configuración del proxy | Propuesto |
| Servidor de aplicación | Contenedor Docker | JRE 17 con Tomcat embebido de Spring Boot | `agrovalle-connect-0.0.1-SNAPSHOT.jar` | JAR actual, contenedor propuesto |
| Servidor de base de datos | Nodo de datos | PostgreSQL | Esquema con las tablas `usuario` y `agricultor` | PostgreSQL actual, servidor propuesto |

## 3. Diagrama de despliegue (producción)

```mermaid
flowchart LR
    subgraph EXTERNO ["Dispositivo del usuario"]
        APP(["Cliente Web / Móvil"])
    end

    subgraph NUBE ["Infraestructura de Producción"]
        subgraph ENTRADA ["Nodo de Entrada (Propuesto)"]
            PROXY{{"Proxy Inverso\n[Terminación TLS]"}}
        end

        subgraph DOCKER ["Servidor de aplicación (Contenedor Docker)"]
            JRE[["JRE 17"]]
            JAR("agrovalle-connect-0.0.1-SNAPSHOT.jar\n[Spring Boot + Tomcat :8080]")
            JRE -.->|ejecuta| JAR
        end

        subgraph DB_SERVER ["Servidor de base de datos (Propuesto)"]
            PG[("PostgreSQL :5432\n[tablas: usuario y agricultor]")]
        end
        
        PROXY ==>|"HTTP :8080\n(Red Privada)"| JAR
        JAR ==>|"JDBC/TCP :5432\n(Red Privada)"| PG
    end

    APP ===>|"HTTPS :443\n(Payload JSON)"| PROXY
    
    style EXTERNO fill:#f9f9f9,stroke:#333,stroke-width:2px
    style NUBE fill:#e1f5fe,stroke:#0288d1,stroke-width:2px,stroke-dasharray: 5 5
    style ENTRADA fill:#ffffff,stroke:#666
    style DOCKER fill:#ffffff,stroke:#666
    style DB_SERVER fill:#ffffff,stroke:#666
```

### 3.1 Comunicación entre nodos

| Origen | Destino | Protocolo y puerto | Exposición |
|---|---|---|---|
| Cliente | Proxy inverso | HTTPS, 443 | Pública |
| Proxy inverso | Servidor de aplicación | HTTP, 8080 | Red privada |
| Servidor de aplicación | PostgreSQL | JDBC sobre TCP, 5432 | Red privada, sin acceso desde Internet |

El tráfico viaja cifrado entre el cliente y el proxy (TLS). Los puertos 8080 y 5432 no se publican hacia Internet.

## 4. Entornos

| Entorno | Dónde corre | Base de datos | Estado |
|---|---|---|---|
| Desarrollo local | Máquina del desarrollador (VS Code, JDK 17, Maven Wrapper `mvnw`) | PostgreSQL | Actual |
| Pruebas automatizadas | Build local y pipeline de integración continua | H2 en memoria (`jdbc:h2:mem:testdb`, `create-drop`) | Actual |
| Producción | Contenedor Docker detrás de un proxy inverso | PostgreSQL en un servidor separado | Propuesto |

Las pruebas usan H2 para no depender de un servidor externo; el comportamiento específico de PostgreSQL se valida en desarrollo y producción.

## 5. Construcción y entrega (CI/CD)

```mermaid
flowchart TD
    subgraph LOCAL ["Entorno Local (Actual)"]
        DEV(("Desarrollador\n[VS Code, JDK 17, mvnw]"))
        HUSKY{"Husky pre-commit\n[Checkstyle + Pruebas]"}
    end

    subgraph VCS ["Control de Versiones (Actual)"]
        GH[("GitHub\n[Ramas: feature, develop, main]")]
        PR[["Pull Request\n[Revisión por pares]"]]
    end

    subgraph CICD ["Pipeline CI/CD (Propuesto)"]
        CI["Pipeline CI\n[./mvnw clean verify]"]
        IMG{{"Imagen Docker"}}
        REG[("Registro de Imágenes")]
    end

    subgraph PROD ["Despliegue"]
        SERVER(("Servidor de Aplicación\n[Producción]"))
    end

    DEV -->|"1. git commit"| HUSKY
    HUSKY -->|"2. git push"| GH
    GH -->|"3. abre PR"| PR
    PR -->|"4. merge a main"| CI
    CI -->|"5. build & test"| IMG
    IMG -->|"6. publica imagen"| REG
    REG -->|"7. despliegue"| SERVER
    
    style LOCAL fill:#f1f8e9,stroke:#689f38,stroke-width:2px
    style VCS fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    style CICD fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    style PROD fill:#fce4ec,stroke:#c2185b,stroke-width:2px
```

| Etapa | Qué verifica o produce | Estado |
|---|---|---|
| Hook de Husky | Impide el commit si falla Checkstyle o las pruebas (Definition of Done del equipo) | Actual |
| Pull Request | Revisión y aprobación de al menos un compañero | Actual |
| Pipeline CI | `./mvnw clean verify`: compila, ejecuta Checkstyle y las pruebas con los mismos criterios que el hook local | Propuesto |
| Construcción de imagen | Empaqueta el JAR en una imagen Docker y la publica en un registro | Propuesto |
| Despliegue | Reemplaza el contenedor en producción con la nueva imagen | Propuesto |

El pipeline propuesto ejecuta la verificación en cada PR y construye y despliega la imagen únicamente al fusionar a `main`.

## 6. Configuración y secretos

La configuración se externaliza mediante variables de entorno, de modo que la **misma imagen** sirve para cualquier entorno:

| Variable | Propósito |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring activo (por ejemplo `prod`) |

Reglas de configuración:

- Ninguna credencial se guarda en el repositorio; se inyectan como secretos del entorno de despliegue.
- En producción, `spring.jpa.hibernate.ddl-auto` debe ser `validate` o `none`. El valor `create-drop` se usa solo en las pruebas con H2, porque borra el esquema al cerrar la aplicación.

## 7. Seguridad del despliegue

- TLS terminado en el proxy inverso; el servidor de aplicación no se expone directamente.
- La base de datos vive en una red privada y solo acepta conexiones del servidor de aplicación.
- Usuario de base de datos con los privilegios mínimos necesarios para la aplicación.
- Contenedor ejecutado como usuario sin privilegios de administrador.
- Las contraseñas de los usuarios se almacenan únicamente como hash BCrypt (`passwordHash`).

## 8. Relación con atributos de calidad (ISO 25010)

| Atributo | Cómo lo respalda el despliegue |
|---|---|
| Portabilidad | Una imagen Docker con el JAR y el JRE corre igual en cualquier entorno compatible; la configuración cambia solo por variables de entorno |
| Seguridad | TLS en el borde, red privada para la base de datos y secretos fuera del repositorio |
| Fiabilidad | Aplicación sin estado separada de la base de datos: el contenedor se puede reemplazar sin perder datos |
| Mantenibilidad | El mismo proceso de build y las mismas puertas de calidad en local y en integración continua |

## 9. Decisiones de diseño

- **JAR ejecutable con servidor embebido** en lugar de un WAR sobre un servidor externo: menos piezas que instalar y mantener.
- **Contenedor Docker** como unidad de despliegue, para obtener entornos reproducibles.
- **Aplicación sin estado** separada de la base de datos, lo que permite reemplazar o escalar el contenedor de la aplicación sin tocar los datos.
- **Configuración externalizada** mediante variables de entorno, sin credenciales en el código.
- **Mismas puertas de calidad** en el equipo y en el pipeline: lo que bloquea un commit local también bloquea el merge.

## 10. Pendientes y riesgos

| Tema | Detalle |
|---|---|
| Proveedor de despliegue | El equipo debe elegir dónde se aloja el contenedor y la base de datos; el diagrama es válido para cualquier proveedor que ofrezca contenedores y PostgreSQL |
| Migraciones de esquema | Para producción conviene adoptar una herramienta de migraciones (Flyway o Liquibase) en lugar de depender de `ddl-auto` |
| Respaldos | Definir la frecuencia de copias de seguridad de PostgreSQL y probar la restauración |
| Monitoreo | Un endpoint de salud (requiere Spring Boot Actuator) permitiría que el proveedor verifique que el contenedor responde |
| Cliente | El despliegue del frontend queda pendiente de un diagrama propio |

## Anexo A. Dockerfile de referencia (propuesto)

Esta propuesta no está incluida en el repositorio.

```dockerfile
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/agrovalle-connect-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Anexo B. Docker Compose de referencia para desarrollo (propuesto)

Esta propuesta no está incluida en el repositorio. La contraseña se lee desde un archivo `.env` que no se versiona.

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: agrovalle
      POSTGRES_USER: agrovalle
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/agrovalle
      SPRING_DATASOURCE_USERNAME: agrovalle
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    depends_on:
      - db

volumes:
  pgdata:
```
