# Mockups de baja fidelidad - AgroValle Connect

Artefacto de la tarea **T07-DIS (#80)**, Sprint de Diseño (#73). Wireframes en escala de grises, formato móvil de 390 px de ancho. Los nombres, cantidades y precios son datos de ejemplo. Los recuadros punteados representan mensajes y estados de respuesta del BACKLOG.

Fuente: `BACKLOG.md` (17 historias de usuario) y los DTO del código (`RegistroAgricultorRequest`, `LoginRequest`).

## Trazabilidad pantalla - historia

| # | Pantalla | Historias | Estados representados |
|---|---|---|---|
| 01 | Inicio de sesión | HU-16 | 200, 400, 401 con mensaje genérico |
| 02 | Registro de agricultor | HU-01 | 201, 400, 409 |
| 03 | Registro de comprador | HU-17 | 201, 400 |
| 04 | Registro de finca | HU-14 | 201, 400, 401 |
| 05 | Inicio del agricultor | Soporte | Sin historia propia |
| 06 | Publicar cosecha | HU-02 | 201, 400, 401 |
| 07 | Pedidos recibidos | HU-15 | 200, 400 |
| 08 | Asignar ruta de envío | HU-06 | 200, 400 |
| 09 | Catálogo y filtros | HU-04 | 200, 200 con lista vacía |
| 10 | Detalle de producto | HU-12, HU-05 | Producto agotado |
| 11 | Precios regionales | HU-03 | 200, 200 sin datos suficientes |
| 12 | Contacto directo | HU-05 | 200, 401, 404 |
| 13 | Carrito y pedido | HU-12, HU-08 | 201, 400 por stock, carrito vacío, producto agotado |
| 14 | Pago | HU-07 | Pago aprobado, pago rechazado |
| 15 | Seguimiento de ruta | HU-13 | 200, 404 |
| 16 | Pedido entregado | HU-11, HU-09, HU-10 | 400, 403, 404 |

Las 17 historias del BACKLOG están cubiertas por al menos una pantalla.

## Supuestos

- Las cantidades se expresan en kg en todas las pantallas; el BACKLOG (HU-08) habla de "unidades".
- El campo de precio por kg en la pantalla 06 no está en el criterio de HU-02, pero el catálogo y los pedidos lo necesitan.
- Las categorías del catálogo, los métodos de pago y el formato del código de seguimiento son ejemplos.
- Los campos de la finca (pantalla 04) están por confirmar: el BACKLOG solo dice "datos de la finca".
- El formato móvil asume una web responsiva.

## Puntos por aclarar con el equipo

- HU-17 indica que un correo duplicado responde 400, mientras que el registro de agricultor (HU-01) responde 409 con el código actual.
- HU-12 crea la orden en "Pendiente de pago" antes de pagar, pero HU-07 dice que un pago rechazado no crea orden.
