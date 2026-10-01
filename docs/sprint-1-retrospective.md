#  Retrospectiva — Sprint 1 — AgroValle Connect

**Fecha:** *30/09/2026* · **Facilitador:** Scrum Master · **Participantes:** Angel Castillo, Sebastian Martinez, Juan Taborda y Joel Mina

---

## 1.  Contexto

El equipo trabajó el Sprint 1 con una capacidad de 10 Story Points y se comprometió con 8 (HU-01 y HU-16). Entre el 26 y el 30 de septiembre de 2026 se fusionaron 8 Pull Requests a `develop` bajo GitFlow. La HU-01 quedó completa y la HU-16 quedó con su primera tarea terminada.

---

## 2.  CONTINUE — Lo que el equipo debe seguir haciendo

| # | Práctica | Evidencia |
| :---: | :--- | :--- |
| 1 | Trabajar con una rama `feature/` por tarea e integrar por Pull Request. | PR #53, #55, #67, #68, #69, #70 |
| 2 | Que un compañero revise y fusione los PR de otro (*Peer Review*). | Fusiones realizadas por Taborda, Sebastian y Joel |
| 3 | Mantener la calidad automatizada: GitHub Actions, Checkstyle y Husky (`checkstyle:check` + `test`). | PR #51, `.husky/pre-commit` |
| 4 | Escribir las pruebas junto con el código. | 27 métodos `@Test` para HU-01 y seguridad |
| 5 | Corregir los bloqueos apenas aparecen (ej.: Spring Security bloqueando `/api/v1/auth/**`). | Commit `fix(auth): permitir acceso publico` |

---

## 3.  STOP — Lo que el equipo debe dejar de hacer

| # | Práctica a eliminar | Impacto | Causa probable |
| :---: | :--- | :---: | :--- |
| 1 | Dejar las historias complejas para el final del sprint: la primera tarea de HU-16 se fusionó recién el 30/09. | **Alto** | Planificación del orden de trabajo  |
| 2 | Concentrar las tareas técnicas en una sola persona (37 de 51 commits). | **Medio** | Reparto desigual de tareas  |
| 3 | Hacer commits o fusiones directas desde la web de GitHub sin Conventional Commits ni PR (`T02-HU01`, `Delete ...`). | **Bajo** | Atajos para corregir rápido |
| 4 | Reorganizar el proyecto a mitad del trabajo por errores de estructura de paquetes y de Lombok. | **Medio** | Curva de aprendizaje en Spring Boot y Maven |
| 5 | Dejar el Kanban y la bitácora sin actualizar el mismo día del avance. | **Medio** | "" |

---

## 4. 🔵 START — Lo que el equipo debe empezar a hacer

| # | Nueva práctica | Responsable | Plazo | Indicador de éxito |
| :---: | :--- | :---: | :---: | :--- |
| 1 | Iniciar las historias más complejas en los primeros dos días del sprint. | Scrum Master | Sprint 2, día 1 | Primera tarea fusionada en ≤ 2 días |
| 2 | Repartir las tareas de forma equilibrada entre los cuatro integrantes. | Equipo | Planning | Ningún integrante con más del 40 % de las tareas |
| 3 | Hacer programación en parejas para temas nuevos (JWT, relaciones JPA). | QA + Backend | Sprint 2 | Sin bloqueos de más de un día |
| 4 | Respetar los límites WIP del Kanban (`In progress` ≤ 3, `In review` ≤ 2). | Equipo | Continuo | Ninguna columna excede su límite |
| 5 | Actualizar el Kanban y la bitácora el mismo día del avance. | Equipo | Continuo | Tablero coherente con GitHub en cada daily |

---

## 5. 💡 Aprendizajes del equipo

| Tema | Aprendizaje |
| :--- | :--- |
| **JPA** | Se comprendió cómo una entidad se mapea a una tabla y cómo `JpaRepository` evita escribir SQL básico. |
| **Seguridad** | Se aprendió que agregar Spring Security protege todas las rutas por defecto y debe configurarse explícitamente. |
| **BDD → JUnit** | Se practicó convertir Given/When/Then en pruebas con MockMvc. |
| **GitFlow** | Ramas cortas por tarea reducen los conflictos al integrar. |
