# Retrospectiva — Sprint 3 — AgroValle Connect

**Fecha de elaboración:** 26 de noviembre de 2026.
**Periodo analizado:** del 17 al 24 de noviembre de 2026.
**Base de análisis:** [planificación](planning.md), [Sprint Review](review.md), [bitácora](../../bitacora-daily-scrum.md#sprint-3--17-al-24-de-noviembre-de-2026), tarjetas del Kanban y revisiones de los PR.

## Contexto

El equipo comprometió 11 SP (HU-17, HU-04 y HU-05) divididos en 20 tareas con 49 horas estimadas, y las cerró todas. Al mismo tiempo terminó los diagramas del Sprint de diseño (T10 a T12-DIS) y los de la sesión 13 (T13 a T16-DIS). Cada integrante cerró entre 4 y 6 tareas: Sebastián 6, Ángel 5, Juanito 5 y Joel 4.

## CONTINUE — Prácticas que conviene mantener

| Práctica | Resultado observado | Evidencia |
|---|---|---|
| Empezar cada historia con una tarea T01 de contrato y escenarios BDD. | Las tareas siguientes de cada HU tuvieron un criterio común. Las discusiones de diseño (como `400` contra `409`) se resolvieron antes de escribir código. | [#172](https://github.com/Angel-uniajc/AgrovalleConnect/pull/172), [#173](https://github.com/Angel-uniajc/AgrovalleConnect/pull/173), [#174](https://github.com/Angel-uniajc/AgrovalleConnect/pull/174) |
| Incluir el diagrama de secuencia como una tarea de cada historia. | El diseño se actualizó con cada HU, sin dejarlo para el final. | T08-HU17, T06-HU04 y T06-HU05 |
| Preparar datos propios en cada prueba de integración. | Las suites no dependen del orden de ejecución ni de los cargadores. | [#196](https://github.com/Angel-uniajc/AgrovalleConnect/pull/196), [#209](https://github.com/Angel-uniajc/AgrovalleConnect/pull/209) |
| Pedir cambios cuando la tarjeta y el código no coinciden. | La tarjeta de T07-HU17 se corrigió antes de aprobar. | [#191](https://github.com/Angel-uniajc/AgrovalleConnect/pull/191) |

## STOP — Situaciones que deben corregirse

| Situación observada | Consecuencia | Evidencia |
|---|---|---|
| Fusionar un PR sin aprobación registrada, o con una revisión que es solo un comentario. | El DoD pide *Peer Review* aprobado. En cuatro PR no queda constancia. | #172, #175, #176 y #180 |
| Dejar como título del PR el nombre de la rama. | El commit en `develop` queda sin Conventional Commits. | #189 y #195 |
| Mover las tareas sin mover la historia. | HU-17 y HU-04 siguieron en *Backlog* con tareas en revisión, y el tablero no mostraba el avance real. | Tablero del 18 y 19/11 |
| Concentrar la documentación al final. | Cuatro de las cinco tareas pendientes se cerraron el último día (24/11). | #165, #159, #170 y #171 |

## START — Plan de mejora

| ID | Acción | Responsable | Frecuencia | Indicador |
|---|---|---|---|---|
| MEJ-07 | Antes de fusionar, revisar en *Reviews* que la última revisión de cada revisor sea *Approved*. | Quien fusiona | Cada PR | Cero PR fusionados sin aprobación. |
| MEJ-08 | Editar el título del PR a Conventional Commits con el código de la tarea antes del merge. | Quien fusiona | Cada PR | Todos los commits de `develop` con `tipo(TXX-HUXX):`. |
| MEJ-09 | Anotar las horas reales en la tarjeta al pasarla a *Done*. | Responsable de la tarea | Cada tarea | 100 % de las tarjetas cerradas con horas reales. |
| MEJ-10 | Incluir en la planificación tareas de despliegue continuo y de frontend. | Scrum Master y equipo | Cada planificación | El sprint tiene al menos una tarea de CD o de frontend. |
| MEJ-11 | Convertir en issue cada contradicción del backlog apenas se detecte (por ejemplo, HU-07 contra HU-12). | Quien la detecta | Continuo | Cada contradicción tiene su issue antes de la planificación siguiente. |

La retrospectiva del Sprint 4 evaluará estos indicadores.
