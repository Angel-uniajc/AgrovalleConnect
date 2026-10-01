#  Sprint Review — Sprint 1 — AgroValle Connect

**Fecha de la revisión:** 30/09/2026
**Participantes:** Angel Castillo (Scrum Master), Sebastian Martinez (Backend/DBA), Juan Taborda (Frontend), Joel Mina (QA) y la docente como *stakeholder*.

---

## 1.  Sprint Goal y resultado

**Habilitar el registro inicial de agricultores del Valle del Cauca y su inicio de sesión seguro en la plataforma AgroValle Connect, validando la persistencia en PostgreSQL, la unicidad de los datos de identidad y la arquitectura REST en capas (Controller → Service → Repository), de modo que exista un usuario autenticado con token JWT que habilite las historias protegidas de los siguientes sprints.**

| Criterio del Sprint Goal | Estado | Evidencia |
| :--- | :---: | :--- |
| Registro de agricultor con `201 Created` | ✅ | `AuthController`, prueba `registroExitoso_persisteYGuardaHashNoTextoPlano` |
| Persistencia y unicidad (correo y cédula) | ✅ | Pruebas `rechazaCorreoDuplicado` y `rechazaCedulaDuplicada` (respuesta `409`) |
| Contraseña guardada con hash, no en texto plano | ✅ | Servicio con BCrypt y su prueba de integración |
| Login con token JWT (HS256) | ✅ | Solo T01-HU16 (config de seguridad) está fusionada |
| Escenarios BDD traducidos a JUnit 5 | ✅ | Cubierto para HU-01; HU-16 pendiente |

---

## 2.  Incremento entregado

| Historia | Story Points | Estado | Observación |
| :---: | :---: | :---: | :--- |
| **HU-01** Registro de Agricultores | 5 | ✅ Hecha | Cumple los 5 criterios del DoD revisados. |
| **HU-16** Inicio de Sesión | 3 | 🟧 Parcial | Base de seguridad lista; faltan servicio JWT, controlador y pruebas. |

**Velocidad:** 5 Story Points completados de 8 comprometidos (capacidad 10).
*(Actualizar si se terminan más tareas de HU-16 antes de la revisión.)*

---

## 3.  Evidencias de la demo

| # | Escenario demostrado | Petición | Resultado esperado | Captura |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Registro exitoso | `POST /api/v1/auth/register` con `nombre`, `ubicacion_valle`, `cedula`, correo y contraseña válidos | `201 Created` + mensaje "Agricultor registrado exitosamente" | ![Texto descriptivo](./evidencias/01-registro-201.png) |
| 2 | Correo duplicado | Mismo JSON por segunda vez | `409 Conflict` | ![Texto descriptivo](./evidencias/02-correo-duplicado-409.png) |
| 3 | Cédula duplicada | Otro correo con la misma cédula | `409 Conflict` | ![Texto descriptivo](./evidencias/03-cedula-duplicada-409.png) |
| 4 | Contraseña débil | Contraseña que no cumple reglas | `400 Bad Request`, no se guarda nada | ![Texto descriptivo](./evidencias/04-password-debil-400.png) |
| 5 | Tablero Kanban | Captura del tablero | T01–T05 de HU-01 en *Done* | *![Texto descriptivo](./05-tablero-Kanban.png) |

---

## 4.  Evidencia de Desarrollo: Historial GitFlow

El equipo trabajó con una rama `feature/` por tarea, integrada a `develop` mediante Pull Request. El siguiente diagrama se generó a partir del historial real del repositorio (26/09 al 30/09/2026).

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

### Resumen de Pull Requests

| PR | Rama | Tarea / Propósito | Autor principal | Fecha | Commits |
| :---: | :--- | :--- | :---: | :---: | :---: |
| **#51** | `feature/workflow-configuracion` | Pipeline de CI con GitHub Actions | Angel Castillo | 26/09 | 1 |
| **#52** | `docs/BACKLOG-README` | Actualizar `BACKLOG.md` y `README.md` | Angel Castillo | 26/09 | 2 |
| **#53** | `feature/T01-HU01` | Entidades `Usuario` y `Agricultor` + repositorios JPA | Angel Castillo | 26/09 | 10 |
| — | `feature/T02-HU01` | DTO de registro *(fusión directa, sin PR)* | Juan Taborda | 26/09 | 2 |
| **#55** | `feature/T03-HU01-registro-agricultor` | Servicio de registro con BCrypt y unicidad | Joel Mina | 27/09 | 3 |
| **#67** | `feature/pruebas-registro` | Pruebas de entidades, DTO y servicio | Angel Castillo | 28/09 | 9 |
| **#68** | `feature/T04-HU01` | `RestController` y DTO de respuesta | Angel Castillo | 29/09 | 5 |
| **#69** | `feature/T05-HU01-pruebas-registro` | Pruebas de integración con MockMvc | Sebastian Martinez | 29/09 | 3 |
| **#70** | `feature/T01-HU16-security-config` | Configuración de Spring Security | Angel Castillo | 30/09 | 3 |

| Elemento del diagrama | Significado |
| :--- | :--- |
| Línea de `main` | Código estable; solo recibe el release al cerrar el sprint. |
| Línea de `develop` | Rama de integración del equipo. |
| Ramas `feature/` | Una por tarea, de corta duración. |
| Etiqueta `PR#n` | Pull Request revisado y fusionado en `develop`. |

---

## 5.  Calidad del incremento

| Indicador | Valor | Fuente |
| :--- | :---: | :--- |
| Pruebas automatizadas | **27** métodos `@Test` | `src/test/java/...` |
| Clases de prueba | 6 | DTO, entidades/repositorios, servicio, controlador, seguridad |
| Pull Requests fusionados en el sprint | **8** (#51, #52, #53, #55, #67, #68, #69, #70) | GitHub |
| Commits (sin merges) | 51 en total del repo | `git log` |
| Pipeline de CI | Activo en cada PR | `.github/workflows/ci.yml` |
| Checkstyle + Husky | Activos en local | `.husky/pre-commit` |

---

## 6. Ajustes al Product Backlog

| Cambio | Motivo | Destino |
| :--- | :--- | :---: |
| Tareas pendientes de HU-16 (T02–T06) | No se alcanzaron a terminar debido al poco tiempo | Sprint 2 (prioridad alta)  |
| HU-02 y HU-04 | Siguen como objetivo del siguiente sprint | Sprint 2 |
