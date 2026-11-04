# Retrospectiva — Sprint 2 — AgroValle Connect

**Fecha de elaboración:** 4 de noviembre de 2026.
**Corte funcional analizado:** cierre de HU-02 del 3 de noviembre de 2026.
**Tarea:** [DOC #135](https://github.com/Angel-uniajc/AgrovalleConnect/issues/135).
**Base de análisis:** [planificación](sprint-2-planning.md), [Sprint Review](sprint-2-review.md), [bitácora](bitacora-daily-scrum.md) y revisiones de código.

## Contexto y alcance

El equipo cerró el flujo de registro de fincas HU-14 y publicación de cosechas HU-02 sobre el registro y login ya existentes. También integró artefactos de diseño, refactorizaciones del registro, el patrón Observer y la verificación de cobertura en CI.

Esta retrospectiva analiza las entregas y los comentarios de los PR. El plan de mejora recoge propuestas del representante para organizar el siguiente ciclo de trabajo. La fecha de elaboración identifica el documento y las acciones se presentan como propuestas para su adopción por el equipo.

## CONTINUE — Prácticas que conviene mantener

| Práctica | Resultado observado | Evidencia |
|---|---|---|
| Separar entidades, DTO, servicio, endpoint y pruebas en tareas con dependencias. | La revisión pudo contrastar cada entrega con su tarjeta y comprobar el flujo completo. | [Planificación](sprint-2-planning.md), [endpoint de fincas #120](https://github.com/Angel-uniajc/AgrovalleConnect/pull/120), [endpoint de cosechas #131](https://github.com/Angel-uniajc/AgrovalleConnect/pull/131) |
| Revisar persistencia y autorización además del estado HTTP. | Los rechazos comprueban que la operación conserva la cantidad de productos. | [Integración de fincas #121](https://github.com/Angel-uniajc/AgrovalleConnect/pull/121), [integración de cosechas #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) |
| Atender los comentarios antes de integrar. | La suite de cosechas incorporó el caso COMPRADOR, una fecha compartida por prueba y municipio propio. | [Revisión y respuesta #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) |
| Documentar los límites de las decisiones de diseño. | El documento de Observer explica la ejecución síncrona, el rollback y el tratamiento futuro de efectos externos. | [Observer #130](https://github.com/Angel-uniajc/AgrovalleConnect/pull/130) |
| Mantener Checkstyle, pruebas y cobertura en la verificación. | La comprobación del 4 de noviembre terminó con 113 pruebas sin fallos. | [Evidencia de verificación](evidencias/sprint2-verificacion.txt) |

## STOP — Situaciones que deben corregirse

| Situación observada | Consecuencia | Evidencia |
|---|---|---|
| Acumular integración y pruebas del flujo de cosechas cerca del cierre. | Queda menos margen para resolver incidencias de extremo a extremo. El servicio se integró el 30 de octubre y el endpoint y la suite se integraron el 3 de noviembre. | [Servicio #122](https://github.com/Angel-uniajc/AgrovalleConnect/pull/122), [endpoint #131](https://github.com/Angel-uniajc/AgrovalleConnect/pull/131), [suite #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) |
| Tratar la integración funcional como cierre de toda la entrega académica. | IEEE 29148 y la Sprint Review requirieron su integración documental el 4 de noviembre. | [IEEE 29148 #137](https://github.com/Angel-uniajc/AgrovalleConnect/pull/137), [Sprint Review #138](https://github.com/Angel-uniajc/AgrovalleConnect/pull/138) |
| Preparar pruebas dependientes de datos iniciales. | El cargador queda acoplado a escenarios que pueden preparar sus propios datos. | [Revisiones #130](https://github.com/Angel-uniajc/AgrovalleConnect/pull/130) y [#132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) |
| Equiparar la duración de un PR con esfuerzo efectivo. | Las esperas y revisiones distorsionan la estimación. | [Cronología de la bitácora](bitacora-daily-scrum.md) |

## START — Plan de mejora propuesto

| ID | Acción | Responsable propuesto | Inicio o frecuencia | Indicador verificable |
|---|---|---|---|---|
| MEJ-01 | Reservar dos horas diarias de trabajo compartido en días activos. Dedicar 15 minutos al avance, siguiente paso e impedimentos y el resto a coordinación, revisión o trabajo en pareja. | Facilitador y todos los integrantes | Desde la primera jornada del siguiente sprint. El equipo coordina la franja según disponibilidad. | Cada jornada deja un registro breve con avance, siguiente paso y bloqueos. |
| MEJ-02 | Revisar el tablero al comenzar y terminar la jornada y avisar por el canal del equipo cuando una dependencia impida continuar. | Responsable de cada tarea | Dos revisiones por jornada | In Progress ≤ 3 y Code Review ≤ 2. Cada bloqueo tiene nota, responsable de desbloqueo y siguiente acción. |
| MEJ-03 | Asignar un revisor al abrir cada PR y coordinar un relevo si no puede atenderlo al siguiente día hábil. | Autor del PR y revisor | Cada PR | La primera revisión ocurre al siguiente día hábil o queda registrada la causa de espera. |
| MEJ-04 | Actualizar la rama de trabajo desde develop antes de iniciar una tarea y ejecutar la verificación antes de solicitar revisión. | Autor de cada tarea | Cada tarea | El PR contiene únicamente su alcance y presenta CI satisfactorio. |
| MEJ-05 | Incorporar documentación, trazabilidad y evidencias al criterio de terminado de cada tarea. | Autor y revisor | Cada entrega | La tarjeta enlaza el PR, las pruebas y el documento o evidencia aplicable. |
| MEJ-06 | Reservar la última jornada planificada para revisar la rama de entrega, las reglas de calidad y la exportación del PDF. | Representante y equipo | Cada cierre de corte | Las capturas de CI y cobertura corresponden al SHA entregado. |

Las responsabilidades se asignan por rol para que el reparto definitivo se concrete al planificar el siguiente sprint. La próxima retrospectiva evaluará los indicadores de MEJ-01 a MEJ-06.

## Gestión de cambios de tiempo

La secuencia de integración permite identificar concentración de trabajo al cierre. El historial consultado carece de fechas comprometidas para cada tarea que permitan cuantificar un aplazamiento del sprint. Por ello, el equipo debe registrar en adelante fecha prevista, fecha real, causa confirmada, dependencia afectada y medida tomada cuando reprograme una entrega.

La disponibilidad de tiempo se tratará mediante la franja compartida, los avisos de bloqueo y la revisión de dependencias. Estas medidas convierten el seguimiento en acciones concretas sin atribuir causas a partir del intervalo entre commits.
