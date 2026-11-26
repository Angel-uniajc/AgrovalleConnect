# Contrato de validación de stock — HU-08

Contrato funcional y BDD de la validación de stock que ocurre dentro de `POST /api/v1/pedidos`. Complementa el [contrato de pedidos de HU-12](contrato-pedido-hu12.md) (escenarios PED-07, PED-09 y PED-10) y define el cuerpo exacto de los rechazos por stock.

## 1. Alcance y decisiones

- La validación de stock ocurre **al crear el pedido**, no en el pago. En esta entrega no existe pasarela de pagos: el pago (HU-07) partirá de un pedido que ya pasó esta validación.
- No hay endpoint propio. Las reglas se aplican en `POST /api/v1/pedidos`, con la autenticación, los roles, el request y la respuesta exitosa que define HU-12.
- **Todas las líneas se validan antes de reservar.** Si una línea falla, no se descuenta stock de ninguna, no se guarda el pedido ni sus detalles y no se publica `PedidoCreadoEvent`.
- **Se reporta solo el primer rechazo**, en el orden en que llegan las líneas en `items`.
- **Un producto repetido en varias líneas se valida con la suma de sus cantidades.** El rechazo se reporta en la posición de la primera línea de ese producto y `cantidad_solicitada` es la suma.
- Un producto está **agotado** cuando su estado es `AGOTADO` o su `cantidad_disponible` es cero, sin importar la cantidad pedida.
- Un producto `INACTIVO` con stock positivo no es un caso de HU-08: responde 400 "Producto no disponible" según PED-07 de HU-12.
- Las cantidades se comparan como valores decimales con dos cifras (`NUMERIC(12,2)`): 10.01 supera a 10.00.

## 2. Orden de evaluación

1. `401` sin token o token inválido, y `403` por rol o comprador sin perfil (HU-12).
2. `400` por JSON o campos inválidos y lista `items` vacía (HU-12).
3. Por cada producto, en el orden de `items`:
   1. `404` si el producto no existe (PED-06).
   2. `400` "Producto agotado" si su estado es `AGOTADO` o su stock es cero.
   3. `400` "Producto no disponible" si está `INACTIVO` (PED-07).
   4. `400` "Stock insuficiente" si la cantidad solicitada (sumada si el producto se repite) supera `cantidad_disponible`.
4. Cálculo del flete, reserva, guardado del pedido y evento (HU-12).

## 3. Cuerpos de error

Stock insuficiente (`400 Bad Request`):

```json
{
  "error": "Stock insuficiente",
  "id_producto": 12,
  "cantidad_solicitada": 16.00,
  "stock_disponible": 10.00
}
```

Producto agotado (`400 Bad Request`):

```json
{
  "error": "Producto agotado",
  "id_producto": 12
}
```

| Campo | Significado |
|---|---|
| `error` | Mensaje fijo en español: `Stock insuficiente` o `Producto agotado`. |
| `id_producto` | Producto de la primera línea rechazada. |
| `cantidad_solicitada` | Cantidad pedida de ese producto; si se repite en varias líneas, la suma. |
| `stock_disponible` | `cantidad_disponible` del producto al momento de validar. |

Las claves van en *snake_case*, como el resto del JSON del API. Los mapeos existentes de `ManejadorErrores` no cambian.

## 4. Escenarios BDD

### STK-01 — Cantidad superior al stock

**Given** un comprador autenticado y un producto `ACTIVO` con 10.00 unidades disponibles  
**When** envía un pedido con 16.00 unidades de ese producto  
**Then** responde `400 Bad Request` con el cuerpo de stock insuficiente (`cantidad_solicitada` 16.00, `stock_disponible` 10.00); el stock sigue en 10.00, no se crea el pedido y no se publica el evento.

### STK-02 — Cantidad igual al stock

**Given** un producto `ACTIVO` con 10.00 unidades disponibles  
**When** el comprador pide 10.00 unidades  
**Then** pasa la validación de stock y el pedido se crea según PED-08: `201 Created`, stock en cero y producto `AGOTADO`.

### STK-03 — Cantidad menor al stock

**Given** un producto `ACTIVO` con 10.00 unidades disponibles  
**When** el comprador pide 4.00 unidades  
**Then** pasa la validación de stock y el pedido se crea según PED-01, con el stock en 6.00.

### STK-04 — Stock en cero con estado ACTIVO

**Given** un producto `ACTIVO` con `cantidad_disponible` en 0.00  
**When** el comprador lo incluye en el pedido  
**Then** responde `400 Bad Request` con el cuerpo de producto agotado; no se crea el pedido.

### STK-05 — Producto en estado AGOTADO

**Given** un producto en estado `AGOTADO`  
**When** el comprador lo incluye en el pedido con cualquier cantidad  
**Then** responde `400 Bad Request` con el cuerpo de producto agotado; no se crea el pedido.

### STK-06 — Producto INACTIVO con stock en cero

**Given** un producto `INACTIVO` con `cantidad_disponible` en 0.00  
**When** el comprador lo incluye en el pedido  
**Then** responde `400 Bad Request` con el cuerpo de producto agotado, conforme a PED-07.

### STK-07 — Pedido mixto: una línea válida y otra insuficiente

**Given** un producto A con 10.00 disponibles y un producto B con 3.00 disponibles  
**When** el comprador pide 2.00 de A y 5.00 de B en el mismo pedido  
**Then** responde `400 Bad Request` con el cuerpo de stock insuficiente de B; el stock de A sigue en 10.00, el de B en 3.00, y no se guardan pedido, detalles ni evento.

### STK-08 — Varias líneas rechazadas

**Given** un producto A agotado y un producto B con stock insuficiente  
**When** el comprador pide B y luego A, en ese orden  
**Then** responde `400 Bad Request` solo con el cuerpo de stock insuficiente de B (la primera línea rechazada) y no reserva nada.

### STK-09 — Producto repetido cuya suma supera el stock

**Given** un producto con 10.00 unidades disponibles  
**When** el comprador lo incluye en dos líneas de 6.00 unidades  
**Then** responde `400 Bad Request` con `cantidad_solicitada` 12.00 y `stock_disponible` 10.00; el stock no cambia y no se crea el pedido.

### STK-10 — Producto repetido cuya suma alcanza el stock

**Given** un producto con 10.00 unidades disponibles  
**When** el comprador lo incluye en dos líneas de 5.00 unidades  
**Then** pasa la validación de stock y el pedido se crea con el stock en cero y el producto `AGOTADO`.

### STK-11 — Frontera decimal

**Given** un producto con 10.00 unidades disponibles  
**When** el comprador pide 10.01 unidades  
**Then** responde `400 Bad Request` con el cuerpo de stock insuficiente (`cantidad_solicitada` 10.01).

## 5. Reparto entre las tareas de HU-08

| Tarea | Qué toma de este contrato |
|---|---|
| T02-HU08 (#208) | `StockInsuficienteException` y `ProductoAgotadoException` con los datos de la sección 3, y su mapeo a 400 en `ManejadorErrores`. |
| T03-HU08 (#210) | Orden de evaluación (sección 2), suma de productos repetidos y validación de todas las líneas antes de reservar, con pruebas unitarias de STK-01, STK-04, STK-05, STK-07, STK-08 y STK-09. |
| T04-HU08 (#211) | Un test de integración por escenario STK-01 a STK-11 y la matriz BDD de HU-08. |
| T05-HU08 (#212) | Diagrama del rechazo por stock insuficiente con sus dos caminos (`201` y `400`). |

## 6. Fuera de alcance

- Pasarela de pagos y rechazo del pago (HU-07).
- Liberar la reserva cuando un pedido se cancela o el pago falla.
- Reservas concurrentes del mismo producto entre dos compradores.
- Carrito persistente.

## 7. Trazabilidad

T04-HU08 (#211) incorpora un test de integración por escenario STK-01 a STK-11 en `StockPedidoIntegrationTest`. La [trazabilidad BDD](trazabilidad-bdd.md#hu-08--validación-de-stock) enlaza los métodos y describe las comprobaciones de persistencia y eventos.
