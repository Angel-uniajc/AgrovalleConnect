# Sprint 4 Review · AgroValle Connect

**Periodo del sprint:** del 24 al 26 de noviembre de 2026.
**Fecha de elaboración:** 26 de noviembre de 2026 (sesión 15, cierre del proyecto).
**Línea base revisada:** `develop` en `a4879eb`. El incremento llegó a `main` el 26 de noviembre con el PR [#228](https://github.com/Angel-uniajc/AgrovalleConnect/pull/228) (merge `3b2252e`, versión Maven `0.3.0`).
**Base de la revisión:** [planificación](planning.md), tarjetas del Kanban, PR fusionados y sus revisiones.

## Sprint Goal y resultado

> Permitir que un comprador autenticado cree un pedido con varios productos, con el flete calculado según el municipio de cada finca y el stock reservado en la misma transacción, y que la plataforma rechace con `400` las cantidades que superan el inventario sin crear el pedido ni descontar unidades.

El objetivo se cumplió. Las 13 tareas y las dos historias quedaron cerradas el 26 de noviembre a las 09:41 (hora de Colombia).

| Historia | Story Points | Tareas | Horas estimadas | Estado |
|---|---:|---:|---:|---|
| [HU-12](https://github.com/Angel-uniajc/AgrovalleConnect/issues/27) · Realizar pedido | 5 | 8 | 16 | Hecha |
| [HU-08](https://github.com/Angel-uniajc/AgrovalleConnect/issues/19) · Cantidad superior al inventario | 3 | 5 | 8 | Hecha |
| **Total** | **8 de 8** | **13 de 13** | **24** | |

Velocidad del sprint: **8 SP completados de 8 comprometidos**.

## Incremento entregado

| Entrega | Qué hace | Evidencia |
|---|---|---|
| Pedido | `POST /api/v1/pedidos`, solo para compradores, recibe la lista `items`. Crea el pedido `PENDIENTE_PAGO` con sus detalles, el subtotal, el flete y el total, y descuenta el stock en la misma transacción. Responde `201`. | [Contrato](../../contrato-pedido-hu12.md), [#216](https://github.com/Angel-uniajc/AgrovalleConnect/pull/216), [#217](https://github.com/Angel-uniajc/AgrovalleConnect/pull/217), [#220](https://github.com/Angel-uniajc/AgrovalleConnect/pull/220), [#222](https://github.com/Angel-uniajc/AgrovalleConnect/pull/222) |
| Patrón Strategy | `CalculadoraFlete` elige la estrategia según el municipio de cada finca (`FleteDagua`, `FletePalmira`, `FleteBuga`). Un municipio nuevo se agrega con una clase nueva, sin modificar la calculadora. Un municipio sin tarifa responde `400` en JSON. | [#218](https://github.com/Angel-uniajc/AgrovalleConnect/pull/218), paquete `service/flete` |
| Patrón Observer | Después de guardar, `PedidoService` publica `PedidoCreadoEvent` y `PedidoCreadoListener` registra el aviso para cada agricultor del pedido. | [#220](https://github.com/Angel-uniajc/AgrovalleConnect/pull/220) |
| Validación de stock | Antes de reservar se revisan todas las líneas, en este orden: producto inexistente (`404`), agotado, no disponible y cantidad mayor que el stock (`400` en JSON con `id_producto`, `cantidad_solicitada` y `stock_disponible`). Un producto repetido se valida con la suma de sus cantidades. | [Contrato](../../contrato-stock-hu08.md), [#221](https://github.com/Angel-uniajc/AgrovalleConnect/pull/221), [#225](https://github.com/Angel-uniajc/AgrovalleConnect/pull/225) |
| Pruebas | `PedidoControllerIntegrationTest` cubre PED-01, 02, 03, 05, 06 y 07. `StockPedidoIntegrationTest` cubre STK-01 a STK-11, una prueba por escenario. En los rechazos se comprueba que no hay pedido ni detalle, que el stock no cambió y que no se publicó el evento. | [#226](https://github.com/Angel-uniajc/AgrovalleConnect/pull/226), [#224](https://github.com/Angel-uniajc/AgrovalleConnect/pull/224), [matriz BDD](../../trazabilidad-bdd.md) |
| Diseño | Secuencia y colaboración del pedido, y secuencia del rechazo por stock. | [#223](https://github.com/Angel-uniajc/AgrovalleConnect/pull/223), [#227](https://github.com/Angel-uniajc/AgrovalleConnect/pull/227) |

## Calidad verificada

- Hubo 43 ejecuciones del CI desde el 24 de noviembre y solo una falló: la [#171](https://github.com/Angel-uniajc/AgrovalleConnect/actions), sobre el PR #224, porque la rama estaba atrasada frente a `develop`. Se corrigió actualizando la rama y la siguiente ejecución quedó en verde.
- El hook de Husky ejecutó 370 pruebas sin fallos el 26/11. La release a `main` reporta 381 pruebas sin fallos, con Checkstyle y JaCoCo en verde.
- La cobertura medida está en el [reporte de métricas](../../metrics/sprint-metrics-corte3.md).

## Feedback de las revisiones de código

| Hallazgo | Respuesta incorporada | PR |
|---|---|---|
| El contrato debía cubrir `AGOTADO` y stock cero como "Producto agotado", aclarar que se valida todo antes de reservar y que `comprador_id` se ignora. | Ángel corrigió el contrato en dos commits y la segunda revisión lo aprobó. | [#215](https://github.com/Angel-uniajc/AgrovalleConnect/pull/215) |
| El PR de entidades estaba conectado a la tarjeta equivocada (#203), y la descripción decía `costo_flete > 0` cuando el esquema permite `>= 0`. | Se conectó con #200 y la tarjeta #203 volvió a su columna. | [#216](https://github.com/Angel-uniajc/AgrovalleConnect/pull/216) |
| `trazabilidad-bdd.md` mezcla finales de línea, y el PR lo pasó completo a CRLF (+322/−27 cuando el cambio real era +305/−10). | Sebastián devolvió las líneas a su formato en un commit nuevo. | [#226](https://github.com/Angel-uniajc/AgrovalleConnect/pull/226) |
| La rama de T04-HU08 no tenía los cambios de T03-HU08 ni de T07-HU12, y el CI estaba en rojo. | Ángel actualizó la rama con `develop` y el CI pasó. | [#224](https://github.com/Angel-uniajc/AgrovalleConnect/pull/224) |
| En el diagrama del rechazo, quien invoca `ManejadorErrores` es Spring MVC, no el controlador. | Queda como corrección pequeña pendiente. | [#227](https://github.com/Angel-uniajc/AgrovalleConnect/pull/227) |

## Lo que quedó pendiente

- **Peer Review:** [#227](https://github.com/Angel-uniajc/AgrovalleConnect/pull/227) se fusionó sin revisión registrada, y [#224](https://github.com/Angel-uniajc/AgrovalleConnect/pull/224) se fusionó con la aprobación de un revisor mientras la revisión de otro seguía en *Changes requested*.
- **Orden de las tareas de HU-08:** la tarjeta de T03-HU08 (#210) se cerró antes de que su PR estuviera fusionado. El PR de T04 (#224) trajo el mismo cambio al servicio que T03 (#225). El equipo lo resolvió fusionando primero #225 y actualizando #224, pero la fase roja del TDD de T03 solo quedó en el historial del #224.
- **Responsables en las tarjetas:** #203 y #210 dicen Joel y #211 dice Sebastián, pero las hizo Ángel.
- **Concurrencia:** dos compradores que piden el mismo producto al mismo tiempo podrían reservar las mismas unidades, porque `Producto` no tiene `@Version` ni bloqueo. Queda como mejora documentada, fuera del alcance del sprint.
- **Despliegue continuo:** igual que en el Sprint 3, staging no está en servicio.
- **Frontend:** el incremento se demuestra con Postman. El equipo reconoce que la planificación debe mejorar y que los sprints deben incluir tareas de frontend para entregar funcionalidad completa al cliente.
