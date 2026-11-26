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
| 04/11 | El equipo integró IEEE 29148, la Sprint Review, la retrospectiva y la bitácora. También exigió cobertura de ramas (#141) y llevó la versión 0.2.0 a main (#142). La comprobación complementaria ejecutó 113 pruebas y registró cinco respuestas HTTP. | La entrega en main dependía de la regla de ramas y de la documentación final. | Entregar el segundo corte y planear el Sprint 3. |

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
- La [Sprint Review](sprints/sprint-2/review.md) registra el 3 de noviembre como fecha de la sesión y la participación de todos los integrantes, según lo confirmado por el representante. Sus evidencias complementarias fueron obtenidas el 4 de noviembre.
- La especificación IEEE 29148 se integró el 4 de noviembre a las 17:20 y la Sprint Review a las 17:25.
- La regla de cobertura de ramas se integró el 4 de noviembre a las 18:07 (PR #141) y la versión 0.2.0 llegó a `main` a las 18:35 (PR #142).

### Integración, capacidad y mejora

Entre el merge del servicio de cosechas y el del endpoint transcurrieron casi cuatro días calendario, incluido el fin de semana. La secuencia registra concentración de integración y pruebas cerca del cierre. La estimación de esfuerzo de una tarjeta se interpreta junto con dependencias, esperas y revisiones.

La cronología consultada carece de las fechas previstas necesarias para afirmar un desplazamiento formal de un sprint. El siguiente ciclo incorporará, como propuesta de mejora, el registro de fecha prevista y real, impedimento confirmado, responsable y próxima acción. La [tarea de retrospectiva #135](https://github.com/Angel-uniajc/AgrovalleConnect/issues/135) recoge el plan de dos horas diarias de coordinación y trabajo compartido, revisión de WIP y atención de PR.

---

## Sprint 3 — 17 al 24 de noviembre de 2026

**Sprint Goal:** permitir que un comprador se registre e inicie sesión, encuentre cosechas disponibles por municipio y categoría y contacte al agricultor con un mensaje asociado a la oferta.
**Comprometido:** 11 SP (HU-17, HU-04 y HU-05), 20 tareas, 49 horas estimadas.

Este seguimiento se reconstruyó el 26 de noviembre a partir de los PR, las revisiones y las tarjetas del Kanban. En cada fila, "qué hizo" es lo que el integrante fusionó ese día, "qué hará" es su siguiente tarjeta y "impedimentos" recoge lo que quedó registrado en las revisiones. Las horas son de Colombia.

### Daily — 18/11/2026

| Integrante | ¿Qué hizo? | Evidencia | ¿Qué hará? | Impedimentos |
|---|---|---|---|---|
| **Angel Castillo** | Definió el contrato del catálogo y sus filtros (T01-HU04). | PR #173 | T02-HU04: consulta de ofertas | Ninguno |
| **Sebastian Martinez** | Alineó el contrato de registro del comprador y sus escenarios BDD (T01-HU17). | PR #172 | T03-HU17: DTO del registro | La revisión pidió unificar los duplicados en `409`. |
| **Juan Taborda** | Definió el contrato del contacto directo (T01-HU05). | PR #174 | T03-HU04: DTO y servicio del catálogo | El PR se abrió sin conectarlo con su tarjeta. |
| **Joel Mina** | Abrió el perfil `Comprador` y su repositorio (T02-HU17). | PR #175 (abierto) | Terminar T02-HU17 | Esperaba la revisión. |

### Daily — 19/11/2026

| Integrante | ¿Qué hizo? | Evidencia | ¿Qué hará? | Impedimentos |
|---|---|---|---|---|
| **Angel Castillo** | Implementó la consulta de ofertas por municipio y categoría (T02-HU04) y el registro público por rol (T05-HU17). | PR #177, #180 | Diagramas de la sesión 13 (T13 a T16-DIS) | El PR #180 se fusionó sin revisión registrada. |
| **Sebastian Martinez** | Creó los DTO del registro por rol (T03-HU17) y la entidad `MensajeContacto` (T02-HU05). | PR #176, #181 | T03-HU05: servicio de contacto | El revisor pidió cambios en #181. |
| **Juan Taborda** | Creó el DTO y el servicio de consulta del catálogo (T03-HU04). | PR #179 | T06-HU17: JWT del comprador | Ninguno |
| **Joel Mina** | Terminó el perfil `Comprador` (T02-HU17) y el registro de compradores con TDD (T04-HU17). | PR #175, #178 | T04-HU04: endpoint del catálogo | Hubo que distinguir los duplicados de otros errores de integridad (commit `fix` de Ángel en #178). |

### Daily — 20/11/2026

| Integrante | ¿Qué hizo? | Evidencia | ¿Qué hará? | Impedimentos |
|---|---|---|---|---|
| **Angel Castillo** | Consolidó secuencias y componentes y diagramó la colaboración del pedido, del alistamiento y el despliegue de staging (T13 a T16-DIS). | PR #188, #190, #193, #194 | T06-HU04 y T06-HU05: documentación | Ninguno |
| **Sebastian Martinez** | Implementó el servicio de contacto con TDD (T03-HU05) y verificó el catálogo con BDD (T05-HU04). | PR #192, #196 | T05-HU05: BDD del contacto | Ninguno |
| **Juan Taborda** | Emitió el JWT con el perfil del comprador (T06-HU17) y verificó los escenarios BDD del registro (T07-HU17). | PR #187, #191 | T08-HU17: documentación del flujo | La tarjeta decía `400` para el correo repetido y el contrato decía `409`; se corrigió la tarjeta. |
| **Joel Mina** | Expuso el GET del catálogo (T04-HU04) y el contacto exclusivo para compradores (T04-HU05). | PR #189, #195 | Apoyar las revisiones | Los dos PR entraron con el nombre de la rama como título. |

### Daily — 24/11/2026

| Integrante | ¿Qué hizo? | Evidencia | ¿Qué hará? | Impedimentos |
|---|---|---|---|---|
| **Angel Castillo** | Documentó el catálogo (T06-HU04) y el contacto con su demostración (T06-HU05). | PR #197, #213 | Planear el Sprint 4 | Ninguno |
| **Sebastian Martinez** | Verificó la persistencia y la seguridad de los mensajes con BDD (T05-HU05). | PR #209 | T03-HU12 | Ninguno |
| **Juan Taborda** | Documentó el flujo del comprador y su diagrama de secuencia (T08-HU17). | PR #198 | T04-HU12 y T01-HU08 | Ninguno |
| **Joel Mina** | Revisó la documentación del contacto. | Revisión de #213 | T02-HU12 | Ninguno |

**Cierre:** HU-04, HU-17 y HU-05 se cerraron el 24/11 entre las 15:55 y las 17:31. El 21, 22 y 23 de noviembre no hubo merges.

---

## Sprint 4 — 24 al 26 de noviembre de 2026

**Sprint Goal:** permitir que un comprador autenticado cree un pedido con flete por municipio y stock reservado en la misma transacción, y rechazar con `400` las cantidades que superan el inventario.
**Comprometido:** 8 SP (HU-12 y HU-08), 13 tareas, 24 horas estimadas.

Seguimiento reconstruido con el mismo criterio del Sprint 3.

### Daily — 25/11/2026

| Integrante | ¿Qué hizo? | Evidencia | ¿Qué hará? | Impedimentos |
|---|---|---|---|---|
| **Angel Castillo** | Definió el contrato de pedidos (T01-HU12) e implementó con TDD el servicio que crea el pedido y reserva el stock (T05-HU12). | PR #215, #220 | T06-HU12 y T08-HU12 | La revisión de #215 pidió cubrir `AGOTADO` y validar todo antes de reservar. |
| **Sebastian Martinez** | Creó los DTO del pedido y sus validaciones (T03-HU12). | PR #217 | T07-HU12: BDD del pedido | El revisor pidió cambios antes de aprobar. |
| **Juan Taborda** | Implementó el Strategy del flete (T04-HU12), el contrato de stock (T01-HU08) y las excepciones de stock con su `400` en JSON (T02-HU08). | PR #218, #219, #221 | T05-HU08: diagrama del rechazo | Ninguno |
| **Joel Mina** | Implementó las entidades `Pedido` y `DetallePedido` (T02-HU12). | PR #216 | Revisar los PR de HU-08 | El PR quedó conectado a la tarjeta #203 en vez de la #200. |

### Daily — 26/11/2026

| Integrante | ¿Qué hizo? | Evidencia | ¿Qué hará? | Impedimentos |
|---|---|---|---|---|
| **Angel Castillo** | Expuso `POST /api/v1/pedidos` (T06-HU12), actualizó los diagramas del pedido (T08-HU12), validó el stock en el servicio (T03-HU08) y verificó STK-01 a STK-11 (T04-HU08). Abrió la release `0.3.0` a `main`. | PR #222, #223, #225, #224, #228 | Despliegue continuo | El #224 tenía la rama atrasada y el CI en rojo, y traía el mismo cambio que #225. |
| **Sebastian Martinez** | Verificó los escenarios BDD del pedido (T07-HU12). | PR #226 | Despliegue continuo | El PR pasó `trazabilidad-bdd.md` completo a CRLF; se corrigió en la misma rama. |
| **Juan Taborda** | Diagramó el rechazo por stock insuficiente (T05-HU08) y revisó #220 a #226. | PR #227 | Documentación de cierre | El PR se fusionó sin revisión registrada. |
| **Joel Mina** | Aprobó el #224 y fusionó el #224 y el #227. | Revisión de #224 | Despliegue continuo | Ninguno |

**Cierre:** HU-12 y HU-08 se cerraron el 26/11 a las 09:41. El mismo día, el PR #228 llevó el incremento a `main`.
