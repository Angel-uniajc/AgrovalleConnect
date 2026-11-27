# Contrato de pago — HU-07

Contrato planificado para T01-HU07 ([#232](https://github.com/Angel-uniajc/AgrovalleConnect/issues/232)). Describe el comportamiento que implementarán las tareas siguientes; **ningún endpoint de pago está implementado todavía** en esta línea base de `develop` (`873b973`).

## 1. Decisión y relación con HU-12 y HU-08

`POST /api/v1/pedidos` ya crea un pedido `PENDIENTE_PAGO`, guarda sus detalles y **descuenta `cantidad_disponible` para reservar las unidades**. HU-08 valida el stock antes de esa reserva. Por tanto, pagar no crea otro pedido, no repite la validación de disponibilidad contra el stock ya reducido y **no descuenta las mismas unidades por segunda vez**. La aprobación cambia el pedido existente a `PAGADO`, valor que ya existe en `Pedido.Estado` y en `schema.sql`.

El criterio original «si el pago es rechazado no se crea orden ni se descuenta inventario» se interpreta desde el inicio del flujo: no queda **un pedido confirmado** ni un **descuento neto** después del rechazo. El pedido `PENDIENTE_PAGO` sí existió; pasa a `CANCELADO` y se restituyen exactamente sus unidades reservadas. No se borra el pedido ni sus detalles: conservan la trazabilidad del intento.

| Situación | Pedido | Pago | Inventario | Aviso al agricultor |
|---|---|---|---|---|
| Después de HU-12, antes de pagar | `PENDIENTE_PAGO` | Ninguno | Reservado una vez | Ningún aviso de pago |
| Solicitud enviada a la pasarela | `PENDIENTE_PAGO` | `PENDIENTE` | Sigue reservado | Ninguno |
| Aprobación definitiva | `PAGADO` | `APROBADO` | Sigue con la reserva original; sin segundo descuento | Uno por agricultor distinto, después del commit |
| Rechazo definitivo | `CANCELADO` | `RECHAZADO` | Restituido una sola vez | Ninguno |
| Vencimiento sin aprobación | `CANCELADO` | `EXPIRADO` o ninguno si no hubo intento | Restituido una sola vez | Ninguno |
| Error o timeout sin resultado definitivo | `PENDIENTE_PAGO` | `PENDIENTE` | Sigue reservado hasta reconciliar | Ninguno |

La reserva vence **30 minutos después de `pedido.fecha_pedido`**. Un proceso periódico revisa los pedidos vencidos y cancela los que no tengan pago aprobado. Si la pasarela dejó un intento con resultado incierto, primero consulta su estado con la misma clave de idempotencia. No libera stock mientras pueda existir una aprobación no reconciliada. Un intento tardío no debe convertir automáticamente un pedido ya `CANCELADO` en `PAGADO`: se registra para conciliación manual.

La restitución suma al producto la cantidad de cada detalle una sola vez, dentro de una transacción que bloquea el pedido y los productos afectados o usa una protección equivalente contra actualizaciones concurrentes. Si el producto quedó `AGOTADO` por llegar a cero y ahora tiene unidades, vuelve a `ACTIVO`; si un administrador lo dejó `INACTIVO`, conserva ese estado. El pedido `CANCELADO` no puede pagarse ni alistarse. Tras un rechazo definitivo, un nuevo intento de compra requiere **crear otro pedido**. Un error temporal permite reintentar el mismo pedido con la misma clave, después de consultar el resultado anterior.

El `400` de un rechazo **no debe deshacer** el registro del pago `RECHAZADO`, la cancelación ni la restitución. La lógica transaccional guarda ese resultado y la capa HTTP construye el `400` después de confirmar la transacción; lanzar una excepción de ejecución que provoque rollback dejaría la reserva retenida.

## 2. Pasarela de este sprint y seguridad

Este sprint usará un **simulador de pasarela** detrás de una interfaz `PasarelaPago`, activo solo en perfiles de desarrollo, prueba y staging. El adaptador debe poder devolver aprobación, rechazo y respuesta incierta de forma determinista en pruebas; la decisión del resultado de prueba se configura del lado del servidor, no mediante un campo que permita a un cliente de producción aprobar su propio pago. El despliegue de producción no puede habilitar este adaptador: una pasarela real, sus credenciales y la conciliación operativa requieren otra integración. La API jamás recibe ni guarda número de tarjeta, CVV o clave bancaria. El token JWT identifica al comprador; el servidor calcula el importe desde los detalles y el flete guardados del pedido.

El adaptador envía a la pasarela una **clave de idempotencia** y puede consultar el resultado por esa misma clave. Una respuesta del navegador o del cliente no basta para marcar `PAGADO`: el servidor solo acepta la aprobación comunicada o confirmada por el adaptador. El identificador del comprador se obtiene del JWT; si el JSON trae `comprador_id` o `monto`, se ignoran y nunca sustituyen los valores del servidor, como en HU-12. Los errores públicos no incluyen datos del proveedor ni secretos.

## 3. API propuesta

### Iniciar o reintentar el pago

`POST /api/v1/pedidos/{id}/pago`

```http
Authorization: Bearer <token-del-comprador>
Content-Type: application/json
X-Idempotency-Key: <uuid-estable-para-este-intento>
```

```json
{ "metodo": "SIMULADOR" }
```

`id` es el ID del pedido existente. `metodo` es obligatorio y, en este sprint, solo acepta `SIMULADOR`. La clave es un UUID obligatorio generado por el cliente y usado de nuevo si repite la solicitud tras un timeout. El servidor almacena la clave antes de invocar al adaptador; una clave no puede asociarse a otro pedido ni a otro importe. Un segundo intento con otra clave mientras el primero esté pendiente o después de un resultado definitivo responde `409`.

El comprador debe tener perfil, ser dueño del pedido y el pedido debe estar `PENDIENTE_PAGO` y dentro del plazo. Si el pago se aprueba, responde `200 OK` con una **confirmación digital**:

```json
{
  "id_pedido": 41,
  "estado_pedido": "PAGADO",
  "estado_pago": "APROBADO",
  "metodo": "SIMULADOR",
  "monto": 16400.00,
  "referencia_pago": "SIM-41-001",
  "fecha_pago": "2026-11-26T15:30:00"
}
```

`monto` es `subtotal + costo_flete` del pedido; no lo decide el cliente. `referencia_pago` identifica de forma única la operación confirmada y constituye el comprobante consultable de esta entrega. **HU-11** se encargará del recibo PDF descargable. Una petición idéntica con la misma clave devuelve el mismo resultado HTTP y la misma referencia, sin llamar otra vez a la pasarela, cambiar stock o duplicar avisos.

### Consultar la confirmación

`GET /api/v1/pedidos/{id}/pago` requiere el mismo comprador autenticado. Devuelve `200` con los campos de la confirmación si existe un pago definitivo; para un rechazo, `estado_pago` es `RECHAZADO`, `estado_pedido` es `CANCELADO` y no se ofrece comprobante de aprobación. Si aún no existe resultado definitivo, devuelve `404` con `{"error":"Pago no confirmado"}`. El GET nunca modifica el pedido ni el stock.

### Errores y persistencia

| Caso | HTTP y cuerpo público | Efecto |
|---|---|---|
| Sin token o token inválido | `401 Unauthorized` | Sin llamada a pasarela ni cambios. |
| Rol `AGRICULTOR`, comprador sin perfil o pedido ajeno | `403 Forbidden` | Sin llamada ni cambios. |
| Pedido inexistente | `404 Not Found`, `{"error":"Pedido no encontrado"}` | Sin llamada ni cambios. |
| JSON ilegible, `metodo` no admitido o clave ausente/no UUID | `400 Bad Request`, error de campo | Sin llamada ni cambios. |
| Pago rechazado definitivamente | `400 Bad Request`, `{"error":"Pago rechazado","id_pedido":41,"estado_pedido":"CANCELADO"}` | Se registra `RECHAZADO`, se cancela el pedido y se restituye la reserva una vez. |
| Pedido ya `PAGADO`, `CANCELADO`, `ALISTADO` o vencido; clave distinta para operación pendiente o finalizada | `409 Conflict`, `{"error":"El pedido no admite este pago"}` | Sin segundo cobro ni cambio de stock. Si está vencido, se ejecuta antes la política de expiración. |
| Pasarela no disponible o resultado incierto | `503 Service Unavailable`, `{"error":"No se pudo confirmar el pago"}` | Pedido e intento siguen pendientes; sin liberación ni aviso hasta conciliación. |
| Pago aprobado | `200 OK`, confirmación digital | `PAGADO`; reserva original intacta; aviso después del commit. |

Una repetición con la misma clave tras un rechazo devuelve el mismo `400` y no vuelve a restituir unidades. La clave se revisa **antes** de rechazar por estado `CANCELADO` para poder reproducir ese resultado. Un pedido vencido sin intento pendiente se cancela y libera en la misma transacción de control; la respuesta al nuevo intento es `409`.

Si la pasarela aprobó pero falla el guardado local, el resultado se trata como **incierto**: se consulta por la misma clave antes de cualquier reintento, cancelación o liberación. No se ejecuta una segunda operación de cobro.

## 4. Modelo que debe implementar T02-HU07

La tabla `pago` de `docs/diseno/schema.sql` es todavía una propuesta con estados `APROBADO`/`RECHAZADO`, `pedido_id UNIQUE` y sin clave ni referencia. **No cumple aún este contrato**. T02-HU07 ([#233](https://github.com/Angel-uniajc/AgrovalleConnect/issues/233)) debe alinear entidad, repositorio y esquema: mantener un pago por pedido, admitir `PENDIENTE`, `APROBADO`, `RECHAZADO` y `EXPIRADO`, persistir clave de idempotencia única, referencia de proveedor única cuando exista, importe calculado y fecha del resultado (nula mientras esté pendiente). El pedido conserva sus detalles y estados existentes. La protección de concurrencia de pedido/productos y la migración de una base existente forman parte del diseño de persistencia; no basta editar el `CREATE TABLE` de ejemplo.

## 5. Escenarios BDD planificados

### PAG-01 — Aprobación sin segundo descuento

**Given** un comprador autenticado con pedido propio `PENDIENTE_PAGO` y stock ya reservado por HU-12

**When** paga con el simulador y este aprueba

**Then** obtiene `200` con `PAGADO`, referencia y monto del servidor; el stock queda exactamente como después de HU-12 y se notifica una vez a cada agricultor tras el commit.

### PAG-02 — Rechazo definitivo

**Given** un pedido pendiente con una o varias líneas reservadas

**When** la pasarela rechaza el pago

**Then** responde `400`, persiste `RECHAZADO`, cambia el pedido a `CANCELADO`, restituye todas las unidades una vez y no avisa a ningún agricultor.

### PAG-03 — Vencimiento sin aprobación

**Given** un pedido que supera los 30 minutos sin pago aprobado ni resultado incierto por reconciliar

**When** el proceso de expiración lo revisa

**Then** lo cancela, restituye las unidades una sola vez y rechaza pagos posteriores con `409`.

### PAG-04 — Respuesta incierta de la pasarela

**Given** un intento pendiente registrado con clave de idempotencia

**When** la pasarela no responde con resultado definitivo

**Then** responde `503`, mantiene pedido y stock reservados y no notifica; consulta el resultado antes de reintentar o expirar.

### PAG-05 — Repetición de aprobación

**Given** un pago ya aprobado

**When** se repite la petición con la misma clave

**Then** devuelve la misma confirmación `200` y referencia, sin segunda operación, descuento ni aviso.

### PAG-06 — Repetición de rechazo

**Given** un pago ya rechazado

**When** se repite con la misma clave

**Then** devuelve el mismo `400`, conserva `CANCELADO` y no restituye unidades por segunda vez.

### PAG-07 — Conflicto de estado o clave

**Given** un pedido con operación pendiente o definitiva

**When** el comprador usa otra clave o intenta pagar un pedido `PAGADO`, `CANCELADO` o `ALISTADO`

**Then** responde `409` sin invocar una segunda operación.

### PAG-08 — Sin autenticación

**Given** una petición sin JWT válido

**When** invoca POST o GET de pago

**Then** responde `401` sin consultar ni alterar pagos, pedidos o stock.

### PAG-09 — Rol, perfil o propiedad incorrectos

**Given** un agricultor, un comprador sin perfil o un comprador diferente del dueño

**When** invoca POST o GET de pago

**Then** responde `403` y no revela la confirmación ni llama a la pasarela.

### PAG-10 — Pedido inexistente

**Given** un comprador autenticado

**When** paga o consulta un ID de pedido inexistente

**Then** responde `404` sin cambios.

### PAG-11 — Entrada inválida

**Given** JSON ilegible, método inválido o clave de idempotencia ausente o mal formada

**When** intenta pagar

**Then** responde `400` sin crear un intento ni cambiar la reserva.

### PAG-12 — Consulta del comprobante

**Given** un comprador con pago aprobado propio

**When** consulta `GET /api/v1/pedidos/{id}/pago`

**Then** responde `200` con la misma referencia, importe y fecha; otro comprador recibe `403`.

### PAG-13 — Aprobación tardía tras cancelación

**Given** un pedido ya cancelado y con stock liberado

**When** llega una aprobación tardía del proveedor

**Then** no lo convierte en `PAGADO`, no vuelve a descontar stock y deja la anomalía registrada para conciliación.

## 6. Trazabilidad

PAG-01 a PAG-13 son criterios **planificados**, aún sin pruebas ni endpoints. T02-HU07 modela la persistencia; T03-HU07 implementa el adaptador; T04-HU07 aplica las transiciones; T05-HU07 notifica después del commit; T06-HU07 expone POST/GET y seguridad; T07-HU07 implementa las pruebas; T08-HU07 registra diagramas y demostración. La [matriz BDD](trazabilidad-bdd.md) deberá cambiar el estado de cada escenario a verificado solo cuando su prueba esté integrada en `develop`.
