#  Planificación del Sprint 1 — AgroValle Connect

**Ubicación en el repo:** `docs/sprint-1-planning.md`
**Metodología:** Scrum · Planning Poker con Escala Fibonacci (1, 2, 3, 5, 8, 13) · Priorización MoSCoW
**Stack Tecnológico:** Java 17 / Spring Boot, Spring Data JPA, PostgreSQL, Spring Security + JJWT, JUnit 5 + MockMvc
**Calidad:** Norma ISO/IEC 25010 · Checkstyle (Google Java Style) · Husky

---

## 1.  Equipo y Roles

| Integrante | Rol | Responsabilidad principal en el Sprint 1 |
| :--- | :---: | :--- |
| **Angel Isaac Castillo Cuesta** | Scrum Master | Facilitar la planificación, mantener el tablero Kanban y remover impedimentos del equipo. |
| **Sebastian Martinez Rodriguez** | Backend / DBA | Entidades JPA, repositorios, servicios y configuración de PostgreSQL. |
| **Juan David Taborda Cruz** | Frontend | Consumo de los endpoints `/api/v1/auth/register` y `/api/v1/auth/login`. |
| **Joel Mina Orejuela** | QA / Pruebas | Pruebas JUnit 5 + MockMvc y traducción de escenarios BDD a pruebas automatizadas. |

---

## 2.  Sprint Goal

> **Habilitar el registro inicial de agricultores del Valle del Cauca y su inicio de sesión seguro en la plataforma AgroValle Connect, validando la persistencia en PostgreSQL, la unicidad de los datos de identidad y la arquitectura REST en capas (Controller → Service → Repository), de modo que exista un usuario autenticado con token JWT que habilite las historias protegidas de los siguientes sprints.**

| # | Criterio de cumplimiento del Sprint Goal | Historia |
| :---: | :--- | :---: |
| 1 | Un agricultor se registra mediante `POST /api/v1/auth/register` y recibe `201 Created`. | **HU-01** |
| 2 | El registro persiste en PostgreSQL y **no** se permite duplicar la cédula. | **HU-01** |
| 3 | Un usuario registrado inicia sesión mediante `POST /api/v1/auth/login` y recibe un token JWT firmado (HS256). | **HU-16** |
| 4 | Las credenciales inválidas responden `401 Unauthorized` con un mensaje genérico. | **HU-16** |
| 5 | Los escenarios BDD de ambas historias están traducidos a pruebas JUnit 5 que pasan en verde. | **HU-01 / HU-16** |

---

## 3.  Capacidad del Sprint

| Concepto | Story Points | Porcentaje | Detalle |
| :--- | :---: | :---: | :--- |
| **Capacidad total del equipo** | **10 Points** | 100 % | Definida por la docente para el Sprint 1. |
| **Comprometido** | **8 Points** | 80 % | HU-01 (5 Points) + HU-16 (3 Points). |
| **Colchón** | **2 Points** | 20 % | Curva de aprendizaje (Spring Boot, JPA, JWT), *code review* y correcciones de Checkstyle. |

---

## 4.  Historias Seleccionadas para el Sprint 1

| ID | Historia de Usuario | Priorización (MoSCoW) | Estimación (Story Points) | Criterio de Aceptación | ¿Por qué entra en el Sprint 1? |
| :---: | :--- | :---: | :---: | :--- | :--- |
| **HU-01** | **Registro de Agricultores:** Como Agricultor, quiero registrarme en la plataforma para ofrecer mis productos. | **M** (Must Have) | **5 Points** | **Given** que el usuario ingresa a `/api/v1/auth/register`, **When** envía un JSON con `nombre`, `ubicacion_valle` y `cedula` válida, **Then** el sistema responde status `201 Created` y el registro persiste en PostgreSQL. | Es la puerta de entrada al sistema. Construye los cimientos: tabla en PostgreSQL, `@Entity`, `@Repository`, `@Service` y `@RestController`. |
| **HU-16** | **Inicio de Sesión:** Como Usuario, quiero iniciar sesión en la plataforma para acceder a mis funcionalidades según mi rol. | **M** (Must Have) | **3 Points** | **Escenario Principal (200 OK):** **Given** un usuario registrado con credenciales válidas, **When** envía `POST /api/v1/auth/login` con `email` y `password`, **Then** el sistema responde `200 OK` con el token JWT. **Alternativo 1 (401):** **Given** un email registrado con contraseña incorrecta, o un email no existente, **When** envía `POST /api/v1/auth/login`, **Then** responde `401 Unauthorized` con un mensaje genérico. **Alternativo 2 (400):** **Given** que el request no incluye `email` o `password`, **When** se envía `POST /api/v1/auth/login`, **Then** responde `400 Bad Request`. | Es el complemento natural del registro y prerrequisito de toda historia que exija "autenticado con token JWT" (HU-02, HU-05, HU-07, HU-08, HU-14, etc.). |
| | **TOTAL DEL SPRINT 1** | | **8 Points** | | |

---

## 5.  Desglose Técnico: HU-01 — Registro de Agricultores

| ID Tarea | Descripción Técnica | Componente / Tecnología | Atributo ISO 25010 | Dependencia |
| :---: | :--- | :---: | :---: | :---: |
| **T01-HU01** | Crear la clase `@Entity` **Agricultor** mapeada a PostgreSQL y definir la interfaz `@Repository` extendiendo `JpaRepository`. | JPA / PostgreSQL | Mantenibilidad | — |
| **T02-HU01** | Crear el DTO de registro y configurar las anotaciones de validación de entrada (`@NotBlank`, `@Size`) para los datos del agricultor. | Jakarta Bean Validation / DTO | Adecuación Funcional | T01 |
| **T03-HU01** | Implementar la clase `@Service` para encapsular la lógica de negocio y verificar la **unicidad de la cédula** antes de guardar. | Spring `@Service` | Adecuación Funcional | T01, T02 |
| **T04-HU01** | Diseñar el controlador `@RestController` en `/api/v1/auth/register`, procesando la solicitud HTTP `POST` y retornando la respuesta `201 Created`. | Spring Web REST | Compatibilidad | T03 |
| **T05-HU01** | Construir las pruebas unitarias e integración con **JUnit 5 y MockMvc** para validar la persistencia y la respuesta HTTP `201 Created` (incluye la traducción BDD → JUnit, ver sección 7). | JUnit 5 / MockMvc | Fiabilidad | T04 |

---

## 6.  Desglose Técnico: HU-16 — Inicio de Sesión

| ID Tarea | Descripción Técnica | Componente / Tecnología | Atributo ISO 25010 | Dependencia |
| :---: | :--- | :---: | :---: | :---: |
| **T01-HU16** | Configurar la clase `@Configuration` de seguridad definiendo el `PasswordEncoder` (**BCrypt**) y el filtro de autenticación JWT. | Spring Security | Seguridad | HU-01 |
| **T02-HU16** | Crear el DTO de login (email, contraseña) y configurar las anotaciones de validación de entrada (`@NotBlank`, `@Email`) sobre los datos de acceso. | Jakarta Bean Validation / DTO | Adecuación Funcional | — |
| **T03-HU16** | Implementar la capa `@Service` que localiza al Usuario por email, compara la contraseña con `PasswordEncoder.matches()` y genera el token JWT (HS256) incluyendo `rol` y `perfil_id`, con expiración (`exp`). | Spring Security / JJWT | Seguridad | T01, T02 |
| **T04-HU16** | Diseñar el controlador `@RestController` en `POST /api/v1/auth/login` procesando las credenciales y retornando el token JWT con status `200`, o `401 Unauthorized` con mensaje genérico si son inválidas. | Spring Web REST | Compatibilidad | T03 |
| **T05-HU16** | Construir las pruebas unitarias e integración con JUnit 5 y MockMvc para validar el login exitoso (`200` + token), las credenciales inválidas (`401`) y que el mensaje de error **no revele cuál dato falló**. | JUnit 5 / MockMvc | Fiabilidad | T04 |
| **T06-HU16** | Traducir los escenarios BDD de HU-01 y HU-16 a pruebas automatizadas y verificar su trazabilidad (ver sección 7). | JUnit 5 | Fiabilidad | T05-HU01, T05 |

---

## 7.  Tarea Obligatoria: Traducción BDD → Pruebas Automatizadas JUnit 5

**Idea central:** cada escenario *Given / When / Then* del backlog se convierte **uno a uno** en un método de prueba, de modo que los criterios de aceptación pasan de ser texto a ser una verificación automática.

| Paso BDD | Se traduce a… | Herramienta en Spring Boot |
| :---: | :--- | :--- |
| **Given** | Preparación del estado inicial (*arrange*): datos de prueba o usuario guardado en H2. | `@BeforeEach`, repositorio JPA |
| **When** | Ejecución de la petición HTTP (*act*). | `mockMvc.perform(post(...))` |
| **Then** | Verificación del estado y del cuerpo de la respuesta (*assert*). | `andExpect(status()...)`, `assertEquals` |

### Matriz de Trazabilidad BDD → JUnit 5

| Historia | Escenario BDD | Given | When | Then | Método de prueba propuesto |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **HU-01** | Registro válido | El usuario ingresa a `/api/v1/auth/register`. | Envía JSON con `nombre`, `ubicacion_valle` y `cedula` válida. | Responde `201 Created` y el registro persiste. | `registrarAgricultor_conDatosValidos_retorna201` |
| **HU-01** | Cédula duplicada | Ya existe un agricultor con esa cédula. | Envía el mismo JSON de registro. | Se rechaza el registro y no se duplica. | `registrarAgricultor_conCedulaDuplicada_rechazaRegistro` |
| **HU-01** | Campos inválidos | El usuario ingresa a `/api/v1/auth/register`. | Envía `nombre` o `cedula` vacíos. | Responde `400 Bad Request`. | `registrarAgricultor_conCamposVacios_retorna400` |
| **HU-16** | Principal (200) | Existe un usuario registrado con credenciales válidas. | Envía `POST /api/v1/auth/login` con `email` y `password`. | Responde `200 OK` con token JWT. | `login_conCredencialesValidas_retorna200YToken` |
| **HU-16** | Alternativo 1 (401) — contraseña incorrecta | Existe un email registrado. | Envía el email correcto y una contraseña errónea. | Responde `401 Unauthorized` con mensaje genérico. | `login_conPasswordIncorrecta_retorna401` |
| **HU-16** | Alternativo 1 (401) — email inexistente | El email no existe en el sistema. | Envía un email no registrado. | Responde `401` con el **mismo** mensaje genérico. | `login_conEmailInexistente_retorna401` |
| **HU-16** | Alternativo 2 (400) | El request no incluye `email` o `password`. | Envía `POST /api/v1/auth/login` incompleto. | Responde `400 Bad Request`. | `login_conCamposFaltantes_retorna400` |

>  **Convención de nombres:** `accion_condicion_resultadoEsperado`.
>  **Base de datos de pruebas:** H2 en memoria (configurada en `src/test/.../resources/application.properties`), para que las pruebas corran igual en el computador de cada integrante sin depender de PostgreSQL.

---

## 8.  Trazabilidad ISO/IEC 25010

| Característica de Calidad | Qué significa en AgroValle Connect | Tareas asociadas |
| :--- | :--- | :---: |
| **Adecuación Funcional** | El sistema hace lo que la historia pide: valida datos y aplica reglas de negocio (cédula única). | T02-HU01, T03-HU01, T02-HU16 |
| **Mantenibilidad** | Código organizado en capas (`Entity`, `Repository`, `Service`, `Controller`), fácil de modificar. | T01-HU01 |
| **Compatibilidad** | La API REST usa contratos JSON y códigos HTTP estándar que cualquier cliente puede consumir. | T04-HU01, T04-HU16 |
| **Seguridad** | Contraseñas cifradas con BCrypt, tokens JWT firmados y errores que no filtran información. | T01-HU16, T03-HU16 |
| **Fiabilidad** | Pruebas automatizadas que garantizan el comportamiento ante casos válidos e inválidos. | T05-HU01, T05-HU16, T06-HU16 |

---

## 9.  Flujo de Trabajo: GitFlow + Kanban

**Tablero:** GitHub Projects — *Agrovalle-Connect-Kanban*

**Límites WIP (Work In Progress):** `In progress` ≤ **3** tareas · `In review` (Code Review) ≤ **2** tareas. Si una columna llega a su límite, el equipo deja de iniciar trabajo nuevo y ayuda a terminar lo que ya está en curso.

| Columna | Significado | Criterio para pasar a la siguiente |
| :---: | :--- | :--- |
| 📥 **Backlog** | Historias de usuario aún sin planificar. | Seleccionada en el Sprint Planning. |
| 🟦 **Ready** | Tareas del Sprint 1 listas para tomarse. | Un integrante se la asigna. |
| 🟧 **In progress** | Tarea asignada con rama `feature/` activa. **Límite WIP: 3.** | Pull Request abierto hacia `develop`. |
| 🟪 **In review** | Pull Request esperando revisión de un compañero. **Límite WIP: 2.** | Aprobación de al menos 1 integrante. |
| ✅ **Done** | PR fusionado en `develop` cumpliendo el DoD. | — |

| Regla | Detalle |
| :--- | :--- |
| **Ramas por tarea** | Una rama `feature/` de corta duración por tarea, creada desde `develop`. Ej.: `feature/hu01-t01-entidad-agricultor`. |
| **Integración** | Todo cambio entra a `develop` mediante Pull Request con al menos **1 aprobación**. |
| **Releases** | `main` solo recibe código estable al cierre del sprint (merge desde `develop`). |
| **Commits** | Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `chore:`. Ej.: `feat(hu01): crear entidad Agricultor y repositorio`. |

---

## 10.  Definition of Done Aplicable al Sprint

Checklist obligatorio definido en [`docs/dod.md`](./dod.md):

| Criterio | Descripción | Cumple |
| :--- | :--- | :---: |
| **Build Local** | El proyecto compila sin errores (Java 17 / Spring Boot). | ☐ |
| **Linter Pass** | Checkstyle (Google Java Style) con cero advertencias. | ☐ |
| **Functional Correctness** | El 100 % de las pruebas unitarias pasan. | ☐ |
| **Peer Review** | Pull Request aprobado por al menos un compañero. | ☐ |
| **Documentation** | `README.md` y `/docs` actualizados. | ☐ |
| **Commits** | Historial con Conventional Commits. | ☐ |
| **Automatización** | Hook de Husky (`.husky/pre-commit`) activo. | ☐ |

---

## 11.  Riesgos y Supuestos

| Riesgo / Supuesto | Impacto | Mitigación |
| :--- | :---: | :--- |
| Curva de aprendizaje en JPA, Spring Security y JWT. | **Medio** | Colchón de 2 Points; programación en parejas en T01-HU16 y T03-HU16. |
| Errores de Checkstyle que bloquean el commit por Husky. | **Bajo** | Ejecutar `./mvnw checkstyle:check` antes de cada commit. |
| Conflictos de fusión al integrar en `develop`. | **Medio** | Ramas cortas por tarea e integración frecuente. |
| Configuración distinta de PostgreSQL por integrante. | **Medio** | Usar `application-local.properties` (ignorado en `.gitignore`) y H2 para pruebas. |
| **Supuesto:** el login depende de que HU-01 esté en `develop`. | **Alto** | Priorizar T01–T03 de HU-01 al inicio del sprint. |

---

## 12.  Historias para el Sprint 2

**Sprint Goal (borrador):** *Habilitar la publicación de cosechas asociadas a las fincas registradas y la consulta filtrada del catálogo por municipio y categoría, completando el ciclo de oferta y visibilidad en PostgreSQL y la arquitectura REST.*

| ID | Historia de Usuario | Priorización (MoSCoW) | Estimación (Story Points) | Criterio de Aceptación |
| :---: | :--- | :---: | :---: | :--- |
| **HU-02** | **Publicación de Cosechas:** Como Agricultor, quiero publicar mis cosechas para que sean visibles. | **M** (Must Have) | **5 Points** | **Given** un agricultor autenticado con token JWT, **When** publica un producto con `tipo`, `cantidad` y `fecha_cosecha`, **Then** el sistema valida que la fecha no sea anterior a hoy y retorna un ID de producto único. |
| **HU-04** | **Filtro de Categorías y Municipios:** Como Comprador, quiero filtrar las cosechas por municipio (Dagua, Palmira, Buga) y categoría para localizar recursos rápidamente. | **M** (Must Have) | **3 Points** | **Given** productos registrados en PostgreSQL bajo municipio "Dagua" y categoría "Frutas", **When** realiza GET a `/api/v1/productos?municipio=Dagua&categoria=Frutas`, **Then** responde status `200 OK` con el arreglo JSON de ofertas activas. |
| | **TOTAL DEL SPRINT 2** | | **8 Points** | |

---

*Documento elaborado por el equipo AgroValle Connect · Sprint 1*
