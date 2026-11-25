# Contrato de pedidos — HU-12

Contrato funcional y BDD para `POST /api/v1/pedidos`. Esta especificación define la creación atómica de un pedido pendiente de pago y reemplaza el carrito persistente por una lista de productos enviada en el request.

## 1. Alcance y decisiones

- Solo puede crear pedidos un usuario autenticado con rol `COMPRADOR` y perfil comprador.
- El comprador se identifica desde el JWT; si el JSON incluye `comprador_id`, se ignora, igual que en HU-05. Nunca sustituye al comprador del token ni provoca un 400 por sí solo.
- El request contiene `items`, una lista no vacía de productos y cantidades. La lista representa el carrito de esta operación; no se persiste una tabla de carrito en esta entrega.
- El pedido se crea con estado `PENDIENTE_PAGO`. La reserva descuenta `cantidad_disponible` en la misma transacción que guarda el pedido y sus detalles.
- Si una línea llega exactamente a cero, el producto cambia a `AGOTADO`. Ningún rechazo deja descuentos parciales ni crea el pedido.
- El precio unitario se copia al detalle al momento de la compra. El subtotal es la suma de `cantidad × precio_unitario`; el total es `subtotal + costo_flete`.
- Se publica `PedidoCreadoEvent` después de guardar el pedido dentro de la transacción. Los efectos externos deberán ejecutarse después del commit.
- El carrito persistente y sus operaciones de agregar/quitar productos quedan como mejora futura; no forman parte de HU-12.

## 2. Endpoint

`POST /api/v1/pedidos`

Headers:

```http
Authorization: Bearer <token-del-comprador>
Content-Type: application/json
```

Request mínimo:

```json
{
  "items": [
    { "id_producto": 12, "cantidad": 2.00 },
    { "id_producto": 18, "cantidad": 1.50 }
  ]
}
```

Reglas del request:

| Campo | Regla |
|---|---|
| `items` | Obligatorio, no nulo y con al menos un elemento. |
| `items[].id_producto` | Obligatorio, entero positivo y producto existente. |
| `items[].cantidad` | Obligatoria, positiva y con hasta dos decimales. |
| `comprador_id` | Se ignora si se envía; el perfil siempre se obtiene del token. |

Respuesta exitosa (`201 Created`):

```json
{
  "id": 41,
  "estado": "PENDIENTE_PAGO",
  "items": [
    { "id_producto": 12, "cantidad": 2.00, "precio_unitario": 3200.00 }
  ],
  "subtotal": 6400.00,
  "costo_flete": 10000.00,
  "total": 16400.00
}
```

La respuesta no incluye contraseña, hash ni datos privados del comprador.

## 3. Tarifas de flete del Sprint 4

Son valores fijos de negocio para esta entrega y se calculan según el municipio de la finca de cada producto. La implementación posterior debe usar Strategy y conservar estos valores como unidades monetarias en pesos colombianos:

| Municipio | `costo_flete` |
|---|---:|
| Dagua | 12 000.00 |
| Palmira | 10 000.00 |
| Buga | 15 000.00 |

Si un pedido contiene productos de varios municipios, el costo de flete es la suma de la tarifa aplicable a cada municipio distinto. Si alguno no tiene tarifa configurada, responde `400 Bad Request` con `{"error":"Municipio sin tarifa de flete"}`; no se reserva stock, no se crea el pedido y no se publica el evento.

## 4. Escenarios BDD

### PED-01 — Crear pedido válido

**Given** un comprador autenticado con productos `ACTIVO` y stock suficiente  
**When** envía `POST /api/v1/pedidos` con una lista `items` válida  
**Then** responde `201 Created`, crea el pedido en `PENDIENTE_PAGO`, guarda los detalles, calcula subtotal/flete/total y descuenta las cantidades reservadas.

### PED-02 — Sin token

**Given** una petición sin credenciales  
**When** invoca el endpoint  
**Then** responde `401 Unauthorized` y no crea pedido ni modifica stock.

### PED-03 — Token de agricultor

**Given** un usuario autenticado con rol `AGRICULTOR`  
**When** invoca el endpoint  
**Then** responde `403 Forbidden` y no crea pedido ni modifica stock.

### PED-04 — Comprador sin perfil

**Given** un token con rol comprador cuyo perfil no existe  
**When** invoca el endpoint  
**Then** responde `403 Forbidden` y no crea pedido ni modifica stock.

### PED-05 — Lista vacía

**Given** un comprador autenticado  
**When** envía `items: []`  
**Then** responde `400 Bad Request` indicando que debe incluir al menos un producto, sin crear pedido.

### PED-06 — Producto inexistente

**Given** una lista con un `id_producto` que no existe  
**When** crea el pedido  
**Then** responde `404 Not Found` durante la validación previa, sin reservar ninguna línea ni crear pedido.

### PED-07 — Producto no disponible

**Given** un producto `INACTIVO`, `AGOTADO` o con `cantidad_disponible` igual a cero  
**When** lo incluye en el pedido  
**Then** responde `400 Bad Request`; para `AGOTADO` o stock cero el mensaje es "Producto agotado", conforme a HU-08; para `INACTIVO` con stock positivo indica "Producto no disponible". No se reserva ninguna línea, no se crea pedido ni se publica evento.

### PED-08 — Reserva exacta

**Given** un producto `ACTIVO` con stock igual a la cantidad solicitada  
**When** crea el pedido  
**Then** responde `201 Created`, deja `cantidad_disponible` en cero y cambia el producto a `AGOTADO`.

### PED-09 — Cantidad superior al stock

**Given** una línea cuya cantidad supera `cantidad_disponible`  
**When** crea el pedido  
**Then** responde `400 Bad Request` con `id_producto`, cantidad solicitada y stock disponible; no crea pedido ni descuenta ninguna línea.

### PED-10 — Fallo atómico en pedido con varias líneas

**Given** dos líneas donde la primera tiene stock suficiente y la segunda no  
**When** crea el pedido  
**Then** valida todas las líneas antes de reservar y responde `400 Bad Request` por la línea sin disponibilidad; no reserva ninguna, conserva el stock original de ambas y no crea detalles ni pedido ni publica evento.

### PED-11 — Evento de pedido creado

**Given** un pedido válido  
**When** termina el guardado transaccional  
**Then** publica `PedidoCreadoEvent` con el identificador del pedido y del comprador exactamente una vez.

### PED-12 — JSON y campos inválidos

**Given** un JSON mal formado, un `id_producto` nulo/no positivo o una `cantidad` nula/no positiva  
**When** envía la petición  
**Then** responde `400 Bad Request` con errores por campo y no ejecuta la reserva.

## 5. Códigos de error

| Situación | HTTP | Regla de persistencia |
|---|---:|---|
| Sin token o token inválido | 401 | No se ejecuta el servicio. |
| Rol no autorizado o comprador sin perfil | 403 | No se crea pedido. |
| JSON/campos inválidos, lista vacía, stock insuficiente o producto no disponible | 400 | Se conserva todo el stock y no se publica evento. |
| Municipio sin tarifa configurada | 400 | Mensaje "Municipio sin tarifa de flete"; sin reservas, pedido ni evento. |
| Producto inexistente | 404 | Se revierte la transacción completa. |
| Pedido válido | 201 | Se guardan pedido y detalles, se reserva stock y se publica el evento. |

## 6. Trazabilidad

Los escenarios PED-01 a PED-12 se convierten en pruebas de integración de `PedidoController` y pruebas unitarias de `CalculadoraFlete`/`PedidoService` en las tareas posteriores de HU-12. La matriz de [trazabilidad BDD](trazabilidad-bdd.md) marca esta cobertura como planificada hasta que esas pruebas se fusionen.
