#  Bitácora de Daily Scrums — Sprint 1 — AgroValle Connect

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
