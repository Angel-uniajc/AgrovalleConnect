![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.x-brightgreen)

# AgroValle Connect

Plataforma web que conecta directamente a los productores agrícolas del Valle del Cauca con la demanda comercial urbana, eliminando la intermediación innecesaria en la cadena de comercialización.

## Declaración de Visión del Producto

> Para **productores agrícolas del Valle del Cauca**, que **necesitan vender directamente a comerciantes y restaurantes sin intermediarios**, **AgroValle Connect** es **una plataforma web desarrollada en Java 17 / Spring Boot**, que **conecta la oferta agrícola con la demanda comercial urbana a precio justo y en tiempo real**. A diferencia de **los intermediarios tradicionales y las cadenas de comercialización largas**, nuestro producto **garantiza trazabilidad logística, transparencia de precios y contratos de API abiertos entre productores y compradores**.

## Integrantes del equipo

| Nombre completo | Rol |
|---|---|
| _Angel Isaac Castillo Cuesta_ | Scrum Master |
| _Sebastian Martinez Rodriguez_ | Backend / DBA |
| _Juan David Taborda Cruz_ | Frontend |
| _Joel Mina Orejuela_ | QA / Pruebas |

## Estrategia de Control de Versiones: GitFlow

El equipo adoptó **GitFlow** como estrategia de branching. Se eligió sobre Trunk-Based Development porque el proyecto contempla entregas versionadas por sprint (incrementos funcionales evaluados en cortes académicos), y GitFlow permite mantener una rama `develop` estable para integración continua del equipo mientras se preparan `release/` específicos, sin exponer `main` a código en progreso. Esto reduce los tiempos de espera al aislar el trabajo individual en `feature/` de corta duración por Historia de Usuario, y previene conflictos de fusión extensos al integrar seguido contra `develop` en lugar de acumular cambios.

### Sprint 0 — Configuración inicial

```mermaid
gitGraph
    commit id: "chore: inicializar proyecto"
    branch develop
    checkout develop
    branch feature/configuracion-husky
    checkout feature/configuracion-husky
    commit id: "chore: instalar paquetes husky"
    commit id: "chore: configurar BD H2 para husky"
    checkout develop
    merge feature/configuracion-husky tag: "PR#1"
    branch feature/creacion-gitignore
    checkout feature/creacion-gitignore
    commit id: "chore: configuracion .gitignore"
    checkout develop
    merge feature/creacion-gitignore tag: "PR#2"
    branch feature/docs/contrato-calidad
    checkout feature/docs/contrato-calidad
    commit id: "docs(dod): checklist DoD firmado"
    checkout develop
    merge feature/docs/contrato-calidad tag: "PR#3"
    branch feature/creacion-README
    checkout feature/creacion-README
    commit id: "docs(backlog): 15 HU con moscow y bdd"
    checkout develop
    merge feature/creacion-README tag: "PR#4"
    branch feature/backlog
    checkout feature/backlog
    commit id: "fix: extension BACKLOG.dm a .md"
    checkout develop
    merge feature/backlog tag: "PR#5"
    commit id: "Delete BACKLOG.dm"
    checkout main
    merge develop tag: "PR#6"
```

### Sprint 1 — Registro de agricultores e inicio de sesión (HU-01 y HU-16)

Una rama `feature/` por tarea, integrada a `develop` mediante Pull Request revisado por un compañero.

```mermaid
gitGraph
    commit id: "estado inicial (Sprint 0)"
    branch develop
    checkout develop

    branch feature/workflow-configuracion
    checkout feature/workflow-configuracion
    commit id: "chore: GitHub Actions pipeline"
    checkout develop
    merge feature/workflow-configuracion tag: "PR#51"

    branch docs/BACKLOG-README
    checkout docs/BACKLOG-README
    commit id: "docs: actualizar BACKLOG y README"
    commit id: "docs: ajuste por cambio estructural"
    checkout develop
    merge docs/BACKLOG-README tag: "PR#52"

    branch feature/T01-HU01
    checkout feature/T01-HU01
    commit id: "chore: entidad Usuario + Lombok"
    commit id: "chore: entidad Agricultor"
    commit id: "chore: repositorios JPA"
    commit id: "fix: correccion de entidades"
    commit id: "refactor: reorganizar estructura"
    commit id: "fix: limpiar comentarios"
    checkout develop
    merge feature/T01-HU01 tag: "PR#53"

    branch feature/T02-HU01
    checkout feature/T02-HU01
    commit id: "chore: creacion DTO"
    commit id: "chore: getters y setters"
    checkout develop
    merge feature/T02-HU01 tag: "T02-HU01"

    branch feature/T03-HU01-registro-agricultor
    checkout feature/T03-HU01-registro-agricultor
    commit id: "chore: dependencia spring-security"
    commit id: "feat: PasswordEncoder BCrypt"
    commit id: "feat: service de registro"
    checkout develop
    merge feature/T03-HU01-registro-agricultor tag: "PR#55"

    branch feature/pruebas-registro
    checkout feature/pruebas-registro
    commit id: "fix: Lombok en Maven"
    commit id: "test: entidades y relaciones"
    commit id: "test: validacion del DTO"
    commit id: "test: servicio de registro"
    commit id: "fix: redundancia en pruebas"
    checkout develop
    merge feature/pruebas-registro tag: "PR#67"

    branch feature/T04-HU01
    checkout feature/T04-HU01
    commit id: "chore: DTO request"
    commit id: "chore: DTO response"
    commit id: "chore: RestController"
    commit id: "fix: test y controller"
    commit id: "chore: eliminar test duplicado"
    checkout develop
    merge feature/T04-HU01 tag: "PR#68"

    branch feature/T05-HU01-pruebas-registro
    checkout feature/T05-HU01-pruebas-registro
    commit id: "fix(auth): acceso publico y 409"
    commit id: "chore: dependencia de cifrado"
    commit id: "test(auth): integracion MockMvc"
    checkout develop
    merge feature/T05-HU01-pruebas-registro tag: "PR#69"

    branch feature/T01-HU16-security-config
    checkout feature/T01-HU16-security-config
    commit id: "feat: dependencia security-test"
    commit id: "feat(security): SecurityFilterChain"
    commit id: "test: SecurityConfig"
    checkout develop
    merge feature/T01-HU16-security-config tag: "PR#70"

    %% Al cerrar el sprint, descomentar para fusionar a main:
    %% checkout main
    %% merge develop tag: "Release Sprint 1"
```

## Stack Tecnológico

- **Lenguaje:** Java 17
- **Framework:** Spring Boot
- **Gestor de dependencias:** Maven
- **Base de datos:** PostgreSQL
- **Calidad de código:** Checkstyle (Google Java Style)
- **Automatización de commits:** Husky (pre-commit hooks)
- **Pruebas:** JUnit 5 + MockMvc (medición de cobertura con JaCoCo planificada)

## Cómo levantar el proyecto localmente

```bash
git clone git@github.com:usuario-o-organizacion/agrovalle-connect.git
cd agrovalle-connect
./mvnw spring-boot:run
```

Configura tu conexión local a PostgreSQL en `src/main/resources/application.properties` antes de ejecutar.

## Convención de Commits

Este repositorio sigue el estándar de **Conventional Commits**:

- `feat:` nueva funcionalidad
- `fix:` corrección de error
- `docs:` cambios en documentación
- `test:` pruebas
- `chore:` mantenimiento/configuración
- `refactor:` reorganización de código sin cambio de comportamiento

## Documentación relacionada

- [`BACKLOG.md`](./BACKLOG.md) — Product Backlog: 17 Historias de Usuario priorizadas con MoSCoW, especificadas en BDD y estimadas con Story Points (Fibonacci).
- [`docs/dod.md`](./docs/dod.md) — Definition of Done, firmado por el equipo.
- [`docs/sprint-1-planning.md`](./docs/sprint-1-planning.md) — Planificación del Sprint 1: Sprint Goal, desglose técnico e ISO/IEC 25010.
- [`docs/bitacora-daily-scrum.md`](./docs/bitacora-daily-scrum.md) — Bitácora de Daily Scrums del Sprint 1.
- [`docs/sprint-1-review.md`](./docs/sprint-1-review.md) — Sprint Review: incremento, evidencias de demo e historial GitFlow.
- [`docs/sprint-1-retrospective.md`](./docs/sprint-1-retrospective.md) — Retrospectiva del Sprint 1 (Start-Stop-Continue).
