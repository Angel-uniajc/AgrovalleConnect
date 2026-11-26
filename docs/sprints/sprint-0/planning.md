# Planificación del Sprint 0 — Configuración inicial

**Fecha:** 15 de septiembre de 2026.
**Participantes:** Angel Castillo (Scrum Master), Sebastian Martinez, Juan Taborda y Joel Mina.
**Nota:** este sprint lo guió la docente en clase y no tuvo una planificación previa formal. El documento sigue la estructura que la docente compartió para el Sprint 0, ajustada a lo que hizo el equipo.

## 1. Objetivo del Sprint 0

> Dejar el repositorio listo para trabajar en equipo con calidad desde el primer commit: control de versiones con GitFlow, exclusión de archivos generados, reglas de estilo automáticas, contrato de calidad firmado y Product Backlog inicial priorizado.

El Sprint 0 no entrega funcionalidad al usuario y no tiene Story Points. Su resultado es el **estado de preparación** (*readiness*) del equipo y del repositorio.

## 2. Tareas planeadas

| # | Tarea | Responsable | Entregable |
|---|---|---|---|
| 1 | Crear el proyecto Spring Boot con Maven Wrapper | Angel | `pom.xml`, `mvnw`, clase principal y prueba de contexto |
| 2 | Configurar Husky para ejecutar Checkstyle y las pruebas antes de cada commit | Angel | `.husky/pre-commit` con `./mvnw checkstyle:check && ./mvnw test` |
| 3 | Configurar `.gitignore` para el IDE, `target/`, dependencias de Node y archivos de entorno | Joel | `.gitignore` |
| 4 | Redactar y firmar la Definition of Done | Sebastián | `docs/dod.md` |
| 5 | Escribir el Product Backlog con 15 historias, prioridad MoSCoW, escenarios BDD y Story Points | Juanito | `BACKLOG.md` |
| 6 | Crear el README con la visión, los integrantes y el flujo GitFlow en Mermaid | Angel | `README.md` |
| 7 | Crear las ramas `main` y `develop` e integrar todo por Pull Request | Equipo | PR #1 a #8 |

## 3. Decisiones tomadas

- **GitFlow** como estrategia de ramas: `main` solo recibe entregas, `develop` integra el trabajo y cada tarea va en una rama corta.
- **Conventional Commits** para los mensajes (`feat`, `fix`, `docs`, `test`, `chore`, `refactor`).
- **Checkstyle** con reglas de estilo basadas en Google Java Style, ejecutado por Husky en cada commit y después por el CI.
- **Planning Poker** con la escala de Fibonacci para estimar las historias y **MoSCoW** para priorizarlas.
- La guía de la materia pide Java 17. El equipo empezó así y después pasó a **Java 25** por compatibilidad. La decisión quedó documentada en la entrega del segundo corte.

[Review del Sprint 0](review.md) · [Retrospectiva del Sprint 0](retrospective.md)
