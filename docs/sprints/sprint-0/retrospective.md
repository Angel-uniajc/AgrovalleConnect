# Retrospectiva — Sprint 0 — AgroValle Connect

**Fecha de elaboración:** 26 de noviembre de 2026, a partir del historial de Git y de lo que el equipo vivió en los sprints siguientes.

## CONTINUE — Lo que funcionó

| Práctica | Por qué funcionó |
|---|---|
| Integrar cada configuración por Pull Request desde el primer día. | El equipo practicó el flujo de ramas y revisiones antes de escribir funcionalidades. |
| Automatizar Checkstyle y las pruebas con Husky. | Desde el Sprint 1 ningún commit entra con errores de estilo o pruebas en rojo. |
| Firmar la Definition of Done al comienzo. | El equipo tuvo un criterio común de "terminado" para revisar los PR. |

## STOP — Lo que no debe repetirse

| Situación | Consecuencia |
|---|---|
| Hacer cambios directos desde la web de GitHub (`Delete BACKLOG.dm`). | El cambio no pasó por revisión ni por Husky. |
| Escribir mensajes de commit sin Conventional Commits. | El historial del Sprint 0 es más difícil de leer y de enlazar con las tareas. |
| Dar por terminado el `.gitignore` sin compilar el proyecto. | Hubo que corregirlo dos veces (17 y 24 de septiembre). |

## START — Lo que el equipo empezó a hacer después

| Acción | Cuándo se aplicó |
|---|---|
| Agregar el pipeline de CI para repetir en GitHub lo que Husky revisa en local. | Sprint 1 (PR #51) |
| Planear cada sprint con un Sprint Goal, tareas con horas y responsables en el Kanban. | Desde el Sprint 1 |
| Registrar las decisiones técnicas (como el cambio a Java 25) en la documentación de la entrega. | Segundo corte |
