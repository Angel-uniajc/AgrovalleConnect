# Planificación del Sprint 4

**Fecha de la planificación:** 24 de noviembre de 2026.
**Duración:** del 24 al 26 de noviembre de 2026 (cierre del proyecto en la sesión 15).
**Participantes:** Angel Castillo (Scrum Master), Sebastian Martinez, Juan Taborda y Joel Mina.

## 1. Sprint Goal

> Permitir que un comprador autenticado cree un pedido con varios productos, con el flete calculado según el municipio de cada finca y el stock reservado en la misma transacción, y que la plataforma rechace con `400` las cantidades que superan el inventario sin crear el pedido ni descontar unidades.

## 2. Alcance y capacidad

| Historia | Prioridad | Story Points | Por qué entra en este sprint |
|---|---|---:|---|
| [HU-12](https://github.com/Angel-uniajc/AgrovalleConnect/issues/27) · Realizar pedido | Must | 5 | Cierra el recorrido de la demo final: registro → login → publicar → consultar → **pedido**. |
| [HU-08](https://github.com/Angel-uniajc/AgrovalleConnect/issues/19) · Cantidad solicitada superior al inventario | Must | 3 | La validación del stock ocurre al crear el pedido, así que comparte el mismo endpoint y la misma transacción. |
| **Total** | | **8** | La materia permite hasta 12 SP por sprint. El equipo comprometió 8 porque el sprint dura tres días. |

El sprint también responde a la retroalimentación del segundo corte: incorpora el patrón **Strategy** (flete por municipio), que el proyecto todavía no tenía, y un segundo uso del **Observer** (`PedidoCreadoEvent`).

## 3. Decisiones de diseño tomadas en la planificación

Las decisiones quedaron escritas en las tarjetas el 24 de noviembre:

1. **Sin tabla de carrito.** El cuerpo de `POST /api/v1/pedidos` trae la lista `items` (`id_producto`, `cantidad`). El carrito persistente queda como mejora futura. Esto reemplaza el `CarritoRepository` que proponía el diagrama de T14-DIS (#190).
2. **El flete se guarda en `pedido.costo_flete`** y se calcula con Strategy según el municipio de la finca: Dagua $12.000, Palmira $10.000 y Buga $15.000. Un pedido con fincas de varios municipios suma una tarifa por municipio. Un municipio sin tarifa responde `400`.
3. **La reserva descuenta `cantidad_disponible` en la misma transacción** que crea el pedido `PENDIENTE_PAGO`. Si una línea llega a cero, el producto pasa a `AGOTADO`. Después de guardar se publica `PedidoCreadoEvent`.
4. **Se validan todas las líneas antes de reservar.** Si una falla, no se reserva ninguna. Un producto repetido se valida con la suma de sus cantidades.

## 4. Desglose en tareas

Las horas son las estimaciones registradas en el Kanban. Cada tarea es un sub-issue de su historia, y en su sección "Depende de" enlaza el número de la tarea previa.

| Tarjeta | Tarea | Responsable | Depende de | Horas |
|---|---|---|---|---:|
| [#199](https://github.com/Angel-uniajc/AgrovalleConnect/issues/199) | T01-HU12 — Definir el contrato de `POST /api/v1/pedidos` y los escenarios PED-01 a PED-12 | Ángel | — | 1 |
| [#200](https://github.com/Angel-uniajc/AgrovalleConnect/issues/200) | T02-HU12 — Implementar las entidades `Pedido` y `DetallePedido` y sus repositorios | Joel | #199 | 2 |
| [#201](https://github.com/Angel-uniajc/AgrovalleConnect/issues/201) | T03-HU12 — Crear los DTO del pedido y sus validaciones | Sebastián | #199 | 2 |
| [#202](https://github.com/Angel-uniajc/AgrovalleConnect/issues/202) | T04-HU12 — Implementar el Strategy de cálculo de flete por municipio | Juanito | #199 | 2 |
| [#203](https://github.com/Angel-uniajc/AgrovalleConnect/issues/203) | T05-HU12 — Implementar con TDD el servicio que crea el pedido y reserva el stock | Ángel | #200, #201, #202 | 3 |
| [#204](https://github.com/Angel-uniajc/AgrovalleConnect/issues/204) | T06-HU12 — Exponer `POST /api/v1/pedidos` solo para compradores | Ángel | #203 | 2 |
| [#205](https://github.com/Angel-uniajc/AgrovalleConnect/issues/205) | T07-HU12 — Verificar los escenarios BDD del pedido con pruebas de integración | Sebastián | #204 | 2 |
| [#206](https://github.com/Angel-uniajc/AgrovalleConnect/issues/206) | T08-HU12 — Actualizar los diagramas de secuencia y de colaboración del pedido | Ángel | #204 | 2 |
| [#207](https://github.com/Angel-uniajc/AgrovalleConnect/issues/207) | T01-HU08 — Definir el contrato de la validación de stock (STK-01 a STK-11) | Juanito | #199 | 1 |
| [#208](https://github.com/Angel-uniajc/AgrovalleConnect/issues/208) | T02-HU08 — Crear las excepciones de stock y su respuesta `400` en JSON | Juanito | #207 | 2 |
| [#210](https://github.com/Angel-uniajc/AgrovalleConnect/issues/210) | T03-HU08 — Validar el stock en el servicio de pedidos antes de reservar | Ángel | #203, #208 | 2 |
| [#211](https://github.com/Angel-uniajc/AgrovalleConnect/issues/211) | T04-HU08 — Verificar los escenarios de stock con pruebas de integración | Ángel | #210 | 2 |
| [#212](https://github.com/Angel-uniajc/AgrovalleConnect/issues/212) | T05-HU08 — Diagramar el rechazo por stock insuficiente | Juanito | #210 | 1 |
| **Total** | **13 tareas** | | | **24** |

Por historia: HU-12 suma 16 horas y HU-08 suma 8 horas.

En la columna de responsables aparece quien terminó cada tarea. Al planear, #203 y #210 estaban a nombre de Joel, y #211 a nombre de Sebastián. Esas tareas las tomó Ángel durante el sprint, y la [Sprint Review](review.md) registra el cambio.

## 5. Atributos de calidad (ISO/IEC 25010)

| Atributo | Cómo lo atiende el sprint |
|---|---|
| Adecuación funcional | Los escenarios PED-01 a PED-12 y STK-01 a STK-11 se traducen a pruebas JUnit con nombres trazables. |
| Fiabilidad | El pedido, los detalles y la reserva se guardan en una sola transacción. Si algo falla, no queda nada a medias. |
| Seguridad | Solo `ROLE_COMPRADOR` crea pedidos. El comprador sale del token, nunca del JSON. |
| Mantenibilidad | El Strategy permite agregar un municipio con una clase nueva, sin tocar la calculadora (principio abierto/cerrado). |

## 6. Fuera de alcance

Quedan fuera la pasarela de pago (HU-07), la liberación de reservas, el carrito persistente, el frontend y el control de concurrencia entre dos compradores que piden el mismo producto al mismo tiempo.

[Contrato HU-12](../../contrato-pedido-hu12.md) · [Contrato HU-08](../../contrato-stock-hu08.md) · [Matriz BDD](../../trazabilidad-bdd.md)
