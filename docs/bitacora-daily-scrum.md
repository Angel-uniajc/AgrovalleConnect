# Bitácora de Daily Scrums — AgroValle Connect

**Sprint Goal:** Habilitar el registro inicial de agricultores del Valle del Cauca y su inicio de sesión seguro, validando la persistencia en PostgreSQL y la arquitectura REST.
**Capacidad / Comprometido:** 10 Story Points / 8 Story Points (HU-01 + HU-16)
**Facilitador:** Angel Isaac Castillo Cuesta (Scrum Master)

---

## Formato de cada Daily

Cada integrante responde tres preguntas: **¿Qué hice desde la última daily?**, **¿Qué haré hasta la próxima?** y **¿Qué impedimentos tengo?**

---

##  Daily 1 — 26/09/2026

| Integrante | ¿Qué hizo? | Evidencia en GitHub | Impedimentos | Plan siguiente |
| :--- | :--- | :--- | :--- | :---: |
| **Angel Castillo** | Creó las entidades `Usuario` y `Agricultor` y sus repositorios JPA; agregó Lombok; reorganizó la estructura del proyecto. | PR #53 (`feature/T01-HU01`), 11 commits | Errores por estructura de paquetes en Spring Boot (corregidos con `refactor`).  | T02-HU01 |
| **Juan Taborda** | Creó el DTO de registro con getters y setters. | Commits `chore: Creación DTO` y `Getters y Setters agregados` | "" | T03-HU01 |
| **Sebastian Martinez** | Ayuda complementaria | "" | PR #53 (`feature/T01-HU01`), 11 commits | Apoyar T03-HU01 | 
| **Joel Mina** | Ayuda complementaria | "" | "" | Apoyar T03-HU01 |

**Observaciones del Scrum Master:** Antes del sprint se añadió el pipeline de GitHub Actions (PR #51) y se actualizaron `BACKLOG.md` y `README.md` por el cambio estructural del proyecto (PR #52).

---

##  Daily 2 — 27/09/2026

| Integrante | ¿Qué hizo? | Evidencia en GitHub | Impedimentos | Plan siguiente |
| :--- | :--- | :--- | :--- | :---: |
| **Joel Mina** | Implementó el servicio de registro con hash de contraseña (BCrypt), la validación de unicidad y agregó la dependencia de Spring Security. | PR #55 (`feature/T03-HU01-registro-agricultor`) | N/A | Pruebas |
| **Angel Castillo** | Configuró el procesador de anotaciones de Lombok en Maven y empezó pruebas de las entidades (relación Agricultor-Usuario, cascade, orphanRemoval). | Commits del 27/09 en `feature/pruebas-registro` | Lombok no compilaba con Maven (resuelto con `fix`). | Pruebas de servicio y DTO |


---

##  Daily 3 — 28/09/2026

| Integrante | ¿Qué hizo? | Evidencia en GitHub | Impedimentos | Plan siguiente |
| :--- | :--- | :--- | :--- | :---: |
| **Angel Castillo** | Terminó pruebas de entidad, validación del DTO y servicio de registro (correo duplicado, nombre obligatorio). Creó los DTO de request/response y el `RestController` con sus mapeos. | PR #67 (`feature/pruebas-registro`), commits de `feature/T04-HU01` | Redundancia en el código de pruebas (corregida). | Cerrar T04-HU01 |
| **Juan Taborda** | Revisó y aprobó el PR #67. | PR #67 fusionado | N/A | "" |


---

##  Daily 4 — 29/09/2026

| Integrante | ¿Qué hizo? | Evidencia en GitHub | Impedimentos | Plan siguiente |
| :--- | :--- | :--- | :--- | :---: |
| **Angel Castillo** | Corrigió problemas de pruebas, duplicidad y del controller; sincronizó `develop` en su rama. | PR #68 (`feature/T04-HU01`), fusionado 18:26 | Conflictos con `develop` al integrar. | T01-HU16 |
| **Sebastian Martinez** | Permitió acceso público a `/api/v1/auth/**`, respondió `409` en duplicados, agregó la dependencia de cifrado y escribió las pruebas de integración MockMvc del registro. | PR #69 (`feature/T05-HU01-pruebas-registro`), fusionado 20:31 | Spring Security bloqueaba `/register` por defecto (resuelto). | Apoyar HU-16 |


**Hito:** con el PR #69 queda completa la **HU-01** (T01 a T05).

---

##  Daily 5 — 30/09/2026

| Integrante | ¿Qué hizo? | Evidencia en GitHub | Impedimentos | Plan siguiente |
| :--- | :--- | :--- | :--- | :---: |
| **Angel Castillo** | Configuró `SecurityFilterChain` y `PasswordEncoder` y escribió pruebas de `SecurityConfig`. | PR #70 (`feature/T01-HU16-security-config`), fusionado 16:39 | Dependencia de pruebas de Spring Security (resuelta). | T02-HU16 y T03-HU16 |
| **Sebastian Martinez** | Revisó y fusionó el PR #70. | PR #70 | N/A | N/A |


---

##  Resumen de avance (corte 30/09/2026)

| Historia | Tareas | Completadas | Pendientes | Estado |
| :---: | :---: | :---: | :--- | :---: |
| **HU-01** | 5 | **5** (T01–T05) | — | ✅ Terminada |
| **HU-16** | 6 | **1** (T01) | T02, T03, T04, T05, T06 | 🟧 En progreso |

---

## Sprint 2 — Seguimiento reconstruido de las jornadas

**Tarea de documentación:** [DOC #136](https://github.com/Angel-uniajc/AgrovalleConnect/issues/136).
**Período registrado:** 15 de octubre a 4 de noviembre de 2026.
**Objetivo:** integrar el diseño, completar el flujo de agricultor autenticado → finca propia → cosecha publicada y reunir las evidencias del segundo corte.

Las fechas y horas corresponden a Colombia (UTC−5). El registro se reconstruyó con aperturas y merges de PR y cierres de tarjetas. Cada jornada describe avances observados y el siguiente paso lógico de sus dependencias. Las horas de los PR incluyen espera y revisión. El historial permite fechar las integraciones, mientras la duración y los impedimentos de cada reunión requieren un registro directo.

### Avance y siguiente paso por jornada

| Fecha | Avance observado | Dependencia o dificultad documentada | Siguiente paso derivado del flujo |
|---|---|---|---|
| 15/10 | El equipo incorporó MER y modelo relacional en 3FN. | El backend debía mantener correspondencia con el esquema. | Documentar arquitectura y revisar entidades. |
| 16–17/10 | El equipo incorporó arquitectura de cuatro capas, actualizó Java y alineó modelos y DDL. | El PR #85 registra compatibilidad como motivo de la actualización. | Completar autenticación y diagramas. |
| 19/10 | El equipo integró la utilidad de generación y validación JWT. | El servicio de login dependía de la utilidad. | Integrar el servicio de autenticación. |
| 20/10 | El equipo integró el servicio de autenticación, login HTTP y despliegue. | La protección de rutas dependía del filtro JWT. | Integrar y probar el filtro. |
| 21/10 | El equipo integró el filtro JWT. | La trazabilidad del login requería pruebas y documentación. | Completar la matriz BDD. |
| 22–23/10 | El equipo integró trazabilidad de autenticación, componentes, secuencias y entidades de fincas. | El registro de fincas requería DTO y servicio. | Completar validaciones y registro de fincas. |
| 24/10 | El equipo integró los DTO de fincas. | La publicación del endpoint dependía del servicio. | Completar servicio y controlador. |
| 27/10 | El equipo integró mockups y actualizó el despliegue para Java 25 y JWT. | El documento debía reflejar el entorno implementado. | Verificar el flujo funcional con las capas integradas. |
| 28/10 | El equipo integró servicio, endpoint y pruebas de fincas, además de entidades de productos y categorías. | HU-02 dependía de las fincas y categorías. | Integrar DTO y servicio de cosechas. |
| 29/10 | El equipo incorporó el control de cobertura en JaCoCo y CI. | La verificación debía conservar Checkstyle y pruebas. | Verificar la publicación de cosechas y sus errores. |
| 30/10 | El equipo integró DTO, servicio de cosechas y refactor de excepciones y mapper. | El servicio y el endpoint requerían comprobación conjunta. | Completar Observer y endpoint protegido. |
| 02/11 | El equipo integró el desacoplamiento del controlador de autenticación. | El evento y el endpoint de cosechas seguían su revisión. | Integrar el flujo de publicación y su suite REST. |
| 03/11 | El equipo integró Observer, endpoint y ocho escenarios de productos, incluido COMPRADOR. | La revisión solicitó datos propios y fecha compartida en las pruebas. | Preparar la evidencia documental del segundo corte. |
| 04/11 | El equipo integró IEEE 29148 y la Sprint Review. La comprobación complementaria ejecutó 113 pruebas y registró cinco respuestas HTTP. | La entrega en main y la regla de cobertura de ramas se describen con su estado en la review. | Completar retrospectiva, bitácora y revisión de la versión de entrega. |

### Cronología verificable de PR

| PR | Autor en GitHub | Entrega | Apertura | Merge |
|---|---|---|---|---|
| [#82](https://github.com/Angel-uniajc/AgrovalleConnect/pull/82) | SebashoDev | docs(diseño): agregar MER y modelo relacional en 3FN (T01, T02-DIS) | 2026-10-15 10:01 | 2026-10-15 10:04 |
| [#85](https://github.com/Angel-uniajc/AgrovalleConnect/pull/85) | Angel-uniajc | chore: se actulizó java por temas de compatibilidad | 2026-10-16 23:23 | 2026-10-16 23:30 |
| [#83](https://github.com/Angel-uniajc/AgrovalleConnect/pull/83) | Joel-404 | docs(T03-DIS): documentar arquitectura MVC de 4 capas y patrones GoF | 2026-10-16 22:50 | 2026-10-16 23:33 |
| [#86](https://github.com/Angel-uniajc/AgrovalleConnect/pull/86) | TabordaDev | docs(diseno): alinear schema, DER y MER con entidades JPA | 2026-10-17 00:56 | 2026-10-17 01:00 |
| [#87](https://github.com/Angel-uniajc/AgrovalleConnect/pull/87) | SebashoDev | feat(auth): crear JwtUtil con generacion y validacion de token HS256 | 2026-10-19 13:59 | 2026-10-19 14:15 |
| [#88](https://github.com/Angel-uniajc/AgrovalleConnect/pull/88) | Angel-uniajc | feat: implementar servicio de autenticación con login y generación de JWT | 2026-10-19 15:27 | 2026-10-20 14:39 |
| [#90](https://github.com/Angel-uniajc/AgrovalleConnect/pull/90) | Joel-404 | docs(T04-DIS): documentar diagrama de despliegue | 2026-10-19 15:53 | 2026-10-20 20:12 |
| [#91](https://github.com/Angel-uniajc/AgrovalleConnect/pull/91) | TabordaDev | feat(auth): endpoint POST /api/v1/auth/login (T04-HU16) | 2026-10-20 15:32 | 2026-10-20 20:51 |
| [#92](https://github.com/Angel-uniajc/AgrovalleConnect/pull/92) | SebashoDev | feat(auth): filtro JWT y pruebas de acceso protegido (T05-HU16) | 2026-10-20 21:38 | 2026-10-21 01:17 |
| [#95](https://github.com/Angel-uniajc/AgrovalleConnect/pull/95) | SebashoDev | test(auth): trazabilidad BDD de HU-01 y HU-16 (T06-HU16) | 2026-10-21 23:48 | 2026-10-22 12:00 |
| [#111](https://github.com/Angel-uniajc/AgrovalleConnect/pull/111) | Angel-uniajc | T06-DIS: diagrama de componentes | 2026-10-22 15:11 | 2026-10-22 23:00 |
| [#96](https://github.com/Angel-uniajc/AgrovalleConnect/pull/96) | Joel-404 | docs(T05-DIS): diagramas de secuencia de HU-01 y HU-16 | 2026-10-22 11:36 | 2026-10-23 01:37 |
| [#112](https://github.com/Angel-uniajc/AgrovalleConnect/pull/112) | SebashoDev | feat(hu14): entidades Municipio y Finca con repositorios (T01-HU14) | 2026-10-22 23:43 | 2026-10-23 13:29 |
| [#113](https://github.com/Angel-uniajc/AgrovalleConnect/pull/113) | TabordaDev | feat(T02-HU14): DTOs de registro de finca con validaciones y pruebas | 2026-10-23 18:08 | 2026-10-24 17:23 |
| [#115](https://github.com/Angel-uniajc/AgrovalleConnect/pull/115) | Joel-404 | docs(T07-DIS): mockups baja fidelidad | 2026-10-24 20:17 | 2026-10-27 13:15 |
| [#117](https://github.com/Angel-uniajc/AgrovalleConnect/pull/117) | Angel-uniajc | docs(T09-DIS): alinear despliegue con Java 25 y JWT | 2026-10-27 21:58 | 2026-10-27 22:12 |
| [#119](https://github.com/Angel-uniajc/AgrovalleConnect/pull/119) | SebashoDev | feat(hu14): T03-HU14 FincaService para registrar fincas | 2026-10-28 09:15 | 2026-10-28 12:51 |
| [#120](https://github.com/Angel-uniajc/AgrovalleConnect/pull/120) | TabordaDev | feat(T04-HU14): endpoint POST /api/v1/fincas protegido por rol | 2026-10-28 14:43 | 2026-10-28 16:10 |
| [#121](https://github.com/Angel-uniajc/AgrovalleConnect/pull/121) | Angel-uniajc | test(T05-HU14): pruebas de integración y trazabilidad BDD de fincas | 2026-10-28 16:19 | 2026-10-28 16:42 |
| [#118](https://github.com/Angel-uniajc/AgrovalleConnect/pull/118) | Angel-uniajc | feat(T01-HU02): entidades y repositorios de productos y categorías | 2026-10-27 22:27 | 2026-10-28 17:12 |
| [#124](https://github.com/Angel-uniajc/AgrovalleConnect/pull/124) | Angel-uniajc | chore(T01-TEC): configurar JaCoCo y exigir cobertura mínima del 60 % | 2026-10-29 20:49 | 2026-10-29 23:03 |
| [#127](https://github.com/Angel-uniajc/AgrovalleConnect/pull/127) | TabordaDev | feat(T02-HU02): DTOs de publicación de cosecha con validaciones y pruebas | 2026-10-30 00:05 | 2026-10-30 00:27 |
| [#122](https://github.com/Angel-uniajc/AgrovalleConnect/pull/122) | Joel-404 | feat(T03-HU02): publicar cosecha con TDD | 2026-10-29 12:01 | 2026-10-30 01:04 |
| [#128](https://github.com/Angel-uniajc/AgrovalleConnect/pull/128) | SebashoDev | refactor(T02-TEC): excepciones de dominio y AgricultorMapper | 2026-10-30 01:11 | 2026-10-30 01:32 |
| [#129](https://github.com/Angel-uniajc/AgrovalleConnect/pull/129) | TabordaDev | refactor(T03-TEC): AuthController sin dependencia de entidades del modelo | 2026-11-02 02:18 | 2026-11-02 15:21 |
| [#130](https://github.com/Angel-uniajc/AgrovalleConnect/pull/130) | Angel-uniajc | feat(T04-HU02): evento de cosecha publicada con patrón Observer | 2026-11-02 17:22 | 2026-11-03 01:52 |
| [#131](https://github.com/Angel-uniajc/AgrovalleConnect/pull/131) | Joel-404 | feat(T05-HU02): endpoint POST /api/v1/productos protegido por rol | 2026-11-03 00:55 | 2026-11-03 02:02 |
| [#132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) | TabordaDev | test(T06-HU02): pruebas de integración y trazabilidad BDD de publicación de cosechas | 2026-11-03 02:22 | 2026-11-03 03:21 |
| [#137](https://github.com/Angel-uniajc/AgrovalleConnect/pull/137) | Angel-uniajc | docs(HU-01): especificación y comparación IEEE 29148 | 2026-11-03 21:48 | 2026-11-04 17:20 |
| [#138](https://github.com/Angel-uniajc/AgrovalleConnect/pull/138) | Angel-uniajc | docs(Sprint-2): documentar revisión del incremento | 2026-11-03 21:48 | 2026-11-04 17:25 |

### Hitos de cierre

- La tarea T05-HU14 [#101](https://github.com/Angel-uniajc/AgrovalleConnect/issues/101) se cerró el 28 de octubre a las 16:46, después del merge del PR #121 a las 16:42.
- La tarea T06-HU02 [#107](https://github.com/Angel-uniajc/AgrovalleConnect/issues/107) se cerró el 3 de noviembre a las 03:21. HU-02 [#17](https://github.com/Angel-uniajc/AgrovalleConnect/issues/17) se cerró en el mismo minuto.
- La [Sprint Review](sprint-2-review.md) registra el 3 de noviembre como fecha de la sesión y la participación de todos los integrantes, según lo confirmado por el representante. Sus evidencias complementarias fueron obtenidas el 4 de noviembre.
- La especificación IEEE 29148 se integró el 4 de noviembre a las 17:20 y la Sprint Review a las 17:25.

### Integración, capacidad y mejora

Entre el merge del servicio de cosechas y el del endpoint transcurrieron casi cuatro días calendario, incluido el fin de semana. La secuencia registra concentración de integración y pruebas cerca del cierre. La estimación de esfuerzo de una tarjeta se interpreta junto con dependencias, esperas y revisiones.

La cronología consultada carece de las fechas previstas necesarias para afirmar un desplazamiento formal de un sprint. El siguiente ciclo incorporará, como propuesta de mejora, el registro de fecha prevista y real, impedimento confirmado, responsable y próxima acción. La [tarea de retrospectiva #135](https://github.com/Angel-uniajc/AgrovalleConnect/issues/135) recoge el plan de dos horas diarias de coordinación y trabajo compartido, revisión de WIP y atención de PR.
