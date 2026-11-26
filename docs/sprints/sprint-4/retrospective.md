# Retrospectiva — Sprint 4 — AgroValle Connect

**Fecha de elaboración:** 26 de noviembre de 2026.
**Periodo analizado:** del 24 al 26 de noviembre de 2026.
**Base de análisis:** [planificación](planning.md), [Sprint Review](review.md), [bitácora](../../bitacora-daily-scrum.md#sprint-4--24-al-26-de-noviembre-de-2026), tarjetas del Kanban y revisiones de los PR.

## Contexto

El equipo comprometió 8 SP (HU-12 y HU-08) en 13 tareas con 24 horas estimadas, y las cerró en dos días y medio. Ángel cerró 6 tareas, Juanito 4, Sebastián 2 y Joel 1. El tiempo promedio entre la apertura de un PR y su merge fue de 3,5 horas (mediana de 1,1 horas).

## Evaluación del plan de mejora del Sprint 3

| ID | Acción | Resultado en el Sprint 4 |
|---|---|---|
| MEJ-07 | Revisar que la última revisión sea *Approved* antes de fusionar. | Se cumplió en 11 de 13 PR. #227 entró sin revisión y #224 con una revisión en *Changes requested*. |
| MEJ-08 | Títulos en Conventional Commits. | Se cumplió en los 13 PR del sprint. |
| MEJ-09 | Anotar las horas reales. | No se cumplió de forma pareja; no todas las tarjetas tienen horas reales. |
| MEJ-10 | Incluir tareas de CD o de frontend. | No se cumplió. El sprint fue solo backend y documentación. |
| MEJ-11 | Issue para cada contradicción del backlog. | Parcial. HU-12 y HU-08 se diseñaron con la reserva al crear el pedido, pero la fila de HU-07 en `BACKLOG.md` sigue sin corregirse. |

## CONTINUE — Prácticas que conviene mantener

| Práctica | Resultado observado | Evidencia |
|---|---|---|
| Escribir en las tarjetas las decisiones de diseño durante la planificación. | Los cinco autores trabajaron con las mismas reglas (sin carrito, flete con Strategy, reserva en la misma transacción). | [Planificación](planning.md#3-decisiones-de-diseño-tomadas-en-la-planificación) |
| Enlazar las dependencias con `#N` en la sección "Depende de". | Quien iba a empezar veía en GitHub si la tarea previa ya estaba cerrada. | Tarjetas #200 a #212 |
| Pedir cambios por los finales de línea y por ramas atrasadas. | Se evitó reescribir `trazabilidad-bdd.md` completo y fusionar con el CI en rojo. | [#226](https://github.com/Angel-uniajc/AgrovalleConnect/pull/226), [#224](https://github.com/Angel-uniajc/AgrovalleConnect/pull/224) |
| Fusionar con merge commit los PR con TDD. | La evidencia Rojo → Verde → Refactor de T05-HU12 quedó en `develop`. | [#220](https://github.com/Angel-uniajc/AgrovalleConnect/pull/220) |

## STOP — Situaciones que deben corregirse

| Situación observada | Consecuencia | Evidencia |
|---|---|---|
| Cerrar una tarjeta antes de que su PR esté en `develop`. | La tarea siguiente se hizo sobre código que todavía no existía y duplicó el cambio. | #210 con #224 y #225 |
| Fusionar con una revisión en *Changes requested* o sin revisión. | El DoD no se cumple y los comentarios del revisor pueden quedar sin atender. | #224 y #227 |
| Trabajar y fusionar de madrugada para alcanzar el cierre. | Diez de los 13 PR se fusionaron entre las 18:00 del 25/11 y las 09:30 del 26/11, cuatro de ellos entre la medianoche y las 2:00. Las revisiones se hicieron con poco margen. | Cronología de la [bitácora](../../bitacora-daily-scrum.md#sprint-4--24-al-26-de-noviembre-de-2026) |
| Dejar sin actualizar la tarjeta cuando otra persona toma la tarea. | El tablero muestra responsables que no hicieron la tarea, y las métricas por persona quedan mal. | #203, #210 y #211 |

## START — Plan de mejora para los sprints siguientes

| ID | Acción | Responsable | Frecuencia | Indicador |
|---|---|---|---|---|
| MEJ-12 | Mover una tarjeta a *Done* solo cuando su PR aparezca como fusionado en *Development*. | Responsable de la tarea | Cada tarea | Cero tarjetas cerradas sin PR fusionado. |
| MEJ-13 | Si alguien toma la tarea de otro, cambiar el responsable de la tarjeta ese mismo día. | Quien toma la tarea | Cada cambio | El responsable de cada tarjeta coincide con el autor del PR. |
| MEJ-14 | Planear el despliegue continuo como historia técnica con tareas (perfil de staging, `Dockerfile`, `GET /api/v1/estado`, `cd.yml` y secrets). | Scrum Master y equipo | Próxima planificación | Staging responde `200` en `/api/v1/estado` con evidencia en la review. |
| MEJ-15 | Abrir un issue para la concurrencia en la reserva de stock. | Equipo | Antes del siguiente sprint con pedidos | Issue creado y priorizado en el backlog. |
| MEJ-16 | Reservar el último día del sprint para revisar y documentar, sin funcionalidades nuevas. | Equipo | Cada cierre | La review y la retro quedan en `develop` antes de la release. |

MEJ-09 (horas reales) y MEJ-10 (tareas de frontend) siguen vigentes del Sprint 3.
