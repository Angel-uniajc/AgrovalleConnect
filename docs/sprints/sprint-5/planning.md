# Planificación del Sprint 5

**Fecha de planificación:** 26 de noviembre de 2026.

**Periodo de ejecución:** pendiente de acordar con el equipo y la docente.

**Participantes:** Ángel Isaac Castillo Cuesta, Juan David Taborda Cruz, Sebastián Martínez Rodríguez y Joel Mina Orejuela.
**Línea base:** `develop` en `873b973`.

## 1. Sprint Goal

> Permitir que un comprador pague un pedido ya creado sin descontar dos veces el inventario, reciba una confirmación verificable y se notifique al agricultor; después, permitir que el agricultor marque el pedido pagado como alistado para el siguiente paso logístico.

## 2. Alcance y capacidad

| Historia | Prioridad en el tablero | Story Points | Resultado esperado |
|---|---|---:|---|
| [HU-07 · Pago exitoso](https://github.com/Angel-uniajc/AgrovalleConnect/issues/16) | Must | 8 | Pago seguro de un pedido pendiente, confirmación, notificación y tratamiento explícito del rechazo. |
| [HU-15 · Alistamiento](https://github.com/Angel-uniajc/AgrovalleConnect/issues/24) | Should | 3 | El agricultor autorizado pasa un pedido pagado a `ALISTADO`. |
| **Total comprometido** | | **11** | Dentro del límite de 12 SP usado en el Sprint 4. |

Los puntos corresponden a las historias; las **35 horas** de la tabla siguiente son estimaciones de trabajo por subtarea, no puntos adicionales ni horas ejecutadas. Las 13 subtareas y las dos historias están en **Ready** en el [Kanban](https://github.com/users/Angel-uniajc/projects/2/views/1).

## 3. Decisiones que debe cerrar T01-HU07

En el Sprint 4, `POST /api/v1/pedidos` ya crea el pedido `PENDIENTE_PAGO` y descuenta `cantidad_disponible` para reservar el stock en la misma transacción. El enum actual de `Pedido` tiene `PENDIENTE_PAGO`, `PAGADO`, `ALISTADO`, `EN_CAMINO`, `ENTREGADO` y `CANCELADO`. Por tanto:

1. Un pago aprobado debe cambiar el pedido existente a `PAGADO`, sin crear otro pedido ni volver a descontar el inventario. La frase «Pago aprobado» de HU-07 describe el resultado; el valor persistido es `PAGADO`.
2. El contrato debe fijar qué pasa con el pedido y la reserva si la pasarela rechaza el pago, expira o falla. El criterio original de HU-07 habla de no crear una orden ante rechazo, pero la orden ya existe desde HU-12. La política de cancelación y restitución de unidades requiere una decisión antes de implementar el servicio.
3. El contrato debe fijar un identificador único de operación y el comportamiento ante confirmaciones repetidas para evitar dos aprobaciones, dos avisos o una restitución duplicada.
4. Se usará una pasarela en entorno de prueba. T01 escogerá el proveedor o simulador y definirá sus respuestas. No se manejarán credenciales de producción ni datos de tarjeta en el repositorio.
5. La confirmación de pago debe tener una referencia consultable. El recibo PDF descargable pertenece a HU-11 y queda fuera de este sprint.

T01-HU15 debe precisar la autorización del agricultor cuando un pedido incluya productos de varias fincas y el efecto exacto de «habilitar logística». No se implementará asignación de transporte: eso corresponde a HU-06.

## 4. Desglose, responsables y dependencias

| Tarjeta | Trabajo | Responsable | Revisor | Depende de | Horas |
|---|---|---|---|---|---:|
| [#232](https://github.com/Angel-uniajc/AgrovalleConnect/issues/232) | T01-HU07 · Contrato de pago y reserva de stock | Ángel | Juan | HU-12, HU-08 | 2 |
| [#233](https://github.com/Angel-uniajc/AgrovalleConnect/issues/233) | T02-HU07 · Modelo, estados y persistencia | Joel | Sebastián | #232 | 3 |
| [#234](https://github.com/Angel-uniajc/AgrovalleConnect/issues/234) | T03-HU07 · Interfaz y adaptador de pasarela de prueba | Sebastián | Ángel | #232 | 3 |
| [#235](https://github.com/Angel-uniajc/AgrovalleConnect/issues/235) | T04-HU07 · Servicio de confirmación y rechazo con TDD | Juan | Joel | #233, #234 | 5 |
| [#236](https://github.com/Angel-uniajc/AgrovalleConnect/issues/236) | T05-HU07 · Notificación tras confirmar el pago | Joel | Juan | #235 | 3 |
| [#237](https://github.com/Angel-uniajc/AgrovalleConnect/issues/237) | T06-HU07 · Endpoint seguro y confirmación | Ángel | Sebastián | #235 | 3 |
| [#238](https://github.com/Angel-uniajc/AgrovalleConnect/issues/238) | T07-HU07 · Pruebas BDD de integración | Sebastián | Ángel | #235, #236, #237 | 4 |
| [#239](https://github.com/Angel-uniajc/AgrovalleConnect/issues/239) | T08-HU07 · Secuencia, trazabilidad y demostración | Juan | Joel | #238 | 2 |
| [#240](https://github.com/Angel-uniajc/AgrovalleConnect/issues/240) | T01-HU15 · Contrato de alistamiento | Joel | Ángel | #232 | 1 |
| [#241](https://github.com/Angel-uniajc/AgrovalleConnect/issues/241) | T02-HU15 · Servicio transaccional con TDD | Sebastián | Juan | #240, #235 | 3 |
| [#242](https://github.com/Angel-uniajc/AgrovalleConnect/issues/242) | T03-HU15 · Endpoint y autorización | Juan | Sebastián | #241 | 2 |
| [#243](https://github.com/Angel-uniajc/AgrovalleConnect/issues/243) | T04-HU15 · Pruebas de integración | Ángel | Joel | #242 | 3 |
| [#244](https://github.com/Angel-uniajc/AgrovalleConnect/issues/244) | T05-HU15 · Secuencia y demostración | Joel | Juan | #243 | 1 |
| **Total** | **13 subtareas** | | | | **35** |

HU-07 suma **25 h** y HU-15 **10 h**. Por persona: Ángel 8 h, Juan 9 h, Sebastián 10 h y Joel 8 h. Las dependencias permiten trabajar en paralelo en #233 y #234 después del contrato, y empezar #240 mientras avanza la implementación de HU-07.

## 5. Flujo de trabajo y Definition of Done

El equipo toma las tareas en orden de dependencias desde **Ready**. Se mantienen los límites del tablero: máximo tres tareas en **In progress** y dos en **In review**. Cada PR se dirige a `develop`, se enlaza con su tarjeta en *Development*, pasa compilación y pruebas con Maven Wrapper, Checkstyle sin errores y la regla actual de JaCoCo del POM (**60 % en instrucciones y ramas**), recibe aprobación de una persona distinta de la autora y se integra antes de mover la tarjeta a **Done**. La historia se cierra cuando todas sus subtareas y criterios de aceptación están verificados. Las horas reales y cambios de responsable se anotan en las tarjetas y en la Sprint Review.

La guía de auditoría de la sesión 15 pide además verificar **60 % en líneas e instrucciones** en `target/site/jacoco/index.html`. El POM no bloquea por cobertura de líneas, de modo que esta cifra debe revisarse en el reporte; una regla adicional para líneas requeriría otra tarea. En la revisión de código se comprueban responsabilidades separadas entre controlador, servicio y repositorio; DTOs en las respuestas; dependencias inyectadas; excepciones con respuesta pública explícita; nombres claros, métodos concisos y ausencia de duplicación o bloques `catch` vacíos. El reporte JaCoCo, Checkstyle y la aprobación del PR son evidencias verificables, no casillas dadas por cumplidas en la planificación.

## 6. Verificación y demostración previstas

1. Crear un pedido como comprador y comprobar `PENDIENTE_PAGO` y la reserva de stock.
2. Confirmar el pago en el entorno de prueba; comprobar `PAGADO`, referencia de confirmación, inventario sin segundo descuento y un aviso al agricultor.
3. Repetir la confirmación y comprobar que no duplica el pago ni el aviso.
4. Probar rechazo/expiración y comprobar la política de liberación que quede fijada en #232.
5. Con el agricultor autorizado, llamar `PATCH /api/v1/pedidos/{id}/alistamiento`; comprobar `ALISTADO` en la respuesta y en PostgreSQL. Repetir con un pedido impago y comprobar el rechazo sin cambio de estado.

Las respuestas HTTP, las consultas SQL y los resultados de las pruebas se guardarán en `docs/evidencias/` **cuando se ejecuten**. Esta planificación no constituye evidencia de ejecución.

## 7. Calidad y exclusiones

| Atributo ISO/IEC 25010 | Medida prevista |
|---|---|
| Adecuación funcional | Escenarios BDD de pago y alistamiento trazados a pruebas. |
| Fiabilidad | Transiciones e inventario consistentes; confirmaciones idempotentes. |
| Seguridad | Pago solo del pedido propio; alistamiento solo por agricultor autorizado; credenciales de la pasarela fuera del código. |
| Mantenibilidad | Interfaz de pasarela separada del servicio y secuencias actualizadas con el comportamiento fusionado. |

Quedan fuera HU-06 (asignación de ruta), HU-11 (PDF descargable), frontend y pagos reales. Las pruebas de carga o despliegue a producción requieren planificación aparte.

El material de la sesión 15 también pide auditoría cruzada, backend activo en la PaaS, conexión a PostgreSQL de producción, migraciones con Flyway, documentación consolidada y ensayo de una sustentación de 15 minutos. Son **actividades de cierre del producto**, distintas de los criterios funcionales de HU-07 y HU-15. No se deben marcar como realizadas por terminar estas dos historias. El [ADR-002](../../architecture-decisions/ADR-002-estrategia-despliegue-produccion.md) aún señala como pendientes el perfil de producción, la migración inicial de Flyway, el healthcheck y el despliegue; deben planificarse y verificarse por separado antes de afirmar que producción está lista.

[Sprint 4](../sprint-4/planning.md) · [Contrato de pedido HU-12](../../contrato-pedido-hu12.md) · [Definition of Done](../../dod.md)
