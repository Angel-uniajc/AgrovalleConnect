# Contrato del contacto directo — HU-05

Documento de T01-HU05 ([#166](https://github.com/Angel-uniajc/AgrovalleConnect/issues/166)), parte de [HU-05 (#20)](https://github.com/Angel-uniajc/AgrovalleConnect/issues/20). Define el comportamiento que implementarán T02 a T05 de HU-05. **El endpoint, la entidad y sus pruebas todavía están pendientes**: este contrato no afirma que ya existan en `develop`.

## 1. Operación y acceso

`POST /api/v1/contacto/mensaje` registra la intención de contacto de un comprador hacia el agricultor que publicó un producto. Requiere `Authorization: Bearer <token>` válido.

- Solo el rol `COMPRADOR` puede usarlo. La regla `hasRole("COMPRADOR")` se agrega en `SecurityConfig` junto a las reglas existentes, antes de `anyRequest().authenticated()`, sin cambiarlas.
- Sin token, con token inválido o expirado: `401 Unauthorized`, antes de leer el cuerpo. No se exige un cuerpo de error concreto para este rechazo de Spring Security.
- Con token válido de rol `AGRICULTOR`: `403 Forbidden`.
- El comprador que envía el mensaje sale siempre del correo autenticado (`Authentication.getName()`), nunca del JSON.

## 2. Petición

| Campo JSON | Tipo | Obligatorio | Regla |
|---|---|---|---|
| `id_producto` | Entero | Sí | Positivo (mayor que 0) |
| `mensaje` | Texto | Sí | No vacío ni compuesto solo de espacios; máximo 2000 caracteres |

Reglas que deberá cumplir la implementación:

1. Un mensaje de exactamente 2000 caracteres es válido; uno de 2001 responde `400`.
2. El texto se guarda tal como llega. Solo se rechaza si es nulo, vacío o solo espacios.
3. Cualquier campo adicional (`comprador_id`, `agricultor_id`, `fecha_envio`) se ignora. El cuerpo no puede suplantar al comprador ni cambiar el destinatario.
4. El destinatario es el agricultor dueño de la finca del producto. No se envía en el JSON.
5. La fecha de envío la asigna el servidor.
6. Se pueden enviar varios mensajes sobre el mismo producto: no hay restricción de unicidad.

```http
POST /api/v1/contacto/mensaje
Authorization: Bearer <token_comprador>
Content-Type: application/json
```

```json
{
  "id_producto": 12,
  "mensaje": "Buenas tardes, me interesan 20 kg de mango. ¿Podemos acordar la entrega en Palmira?"
}
```

## 3. Respuesta

Un envío válido responde `200 OK`, `Content-Type: application/json`:

| Campo JSON | Tipo | Origen |
|---|---|---|
| `id` | Entero | Identificador del mensaje guardado |
| `id_producto` | Entero | Producto contactado |
| `fecha_envio` | Texto | Fecha y hora ISO asignadas por el servidor |

```json
{
  "id": 7,
  "id_producto": 12,
  "fecha_envio": "2026-11-20T15:30:00"
}
```

No se devuelven entidades JPA, correo, documento, contraseña, hash ni datos del agricultor.

## 4. Errores

| Situación | Código | Cuerpo |
|---|---|---|
| `id_producto` nulo, cero o negativo | 400 | Mapa campo → mensaje, como en el resto de la API |
| `mensaje` nulo, vacío, solo espacios o de más de 2000 caracteres | 400 | Mapa campo → mensaje |
| JSON mal formado, `id_producto` no numérico o con tipo incorrecto | 400 | Mapa con una sola entrada: `{"json": "El cuerpo de la solicitud no es un JSON válido"}`. Lo responde un nuevo `@ExceptionHandler(HttpMessageNotReadableException.class)` en `ManejadorErrores`, a cargo de T04-HU05 (#169) |
| El producto no existe | 404 | Mensaje de `RecursoNoEncontradoException` |
| Producto con estado distinto de `ACTIVO` o con `cantidad_disponible` igual a 0 | 400 | Mensaje de una excepción específica |
| Sin token, token inválido o expirado | 401 | Sin cuerpo exigido |
| Usuario con rol `AGRICULTOR` | 403 | Respuesta por defecto de Spring Security |
| Token de rol `COMPRADOR` sin perfil de comprador en la base | 403 | Mensaje genérico, sin revelar detalles |

Orden de evaluación: autenticación (401), rol (403), validación del cuerpo (400), perfil del comprador (403), existencia del producto (404) y disponibilidad (400).

## 5. Escenarios BDD y nombres de prueba propuestos

Las pruebas son **propuestas pendientes** para T03-HU05 ([#168](https://github.com/Angel-uniajc/AgrovalleConnect/issues/168)), T04-HU05 ([#169](https://github.com/Angel-uniajc/AgrovalleConnect/issues/169)) y T05-HU05 ([#170](https://github.com/Angel-uniajc/AgrovalleConnect/issues/170)), no evidencia de ejecución. Los datos de prueba deben crear sus propios productos con distinto estado y stock.

| ID | Given (Dado) | When (Cuando) | Then (Entonces) | Prueba propuesta |
|---|---|---|---|---|
| CON-01 | Comprador autenticado y producto `ACTIVO` con stock mayor que 0 | Envía `id_producto` y un mensaje válido | `200`; se guarda un mensaje con el comprador del token, el producto y la fecha del servidor | `enviarContacto_conDatosValidos_retorna200YPersiste` |
| CON-02 | Comprador autenticado | Envía un `id_producto` que no existe | `404`; no se guarda nada | `enviarContacto_conProductoInexistente_retorna404` |
| CON-03 | Comprador autenticado y producto `INACTIVO` | Envía un mensaje válido | `400`; no se guarda nada | `enviarContacto_conProductoInactivo_retorna400` |
| CON-04 | Comprador autenticado y producto `AGOTADO` | Envía un mensaje válido | `400`; no se guarda nada | `enviarContacto_conProductoAgotado_retorna400` |
| CON-05 | Comprador autenticado y producto `ACTIVO` con stock 0 | Envía un mensaje válido | `400`; no se guarda nada | `enviarContacto_conStockCero_retorna400` |
| CON-06 | Comprador autenticado y producto disponible | Envía `mensaje` nulo, vacío o solo espacios | `400`; no se guarda nada | `enviarContacto_conMensajeVacio_retorna400` |
| CON-07 | Comprador autenticado y producto disponible | Envía un mensaje de 2000 y otro de 2001 caracteres | El de 2000 responde `200`; el de 2001 responde `400` | `enviarContacto_conMensajeEnElLimite_aceptaHasta2000` |
| CON-08 | Comprador autenticado | Envía `id_producto` nulo, 0 o negativo | `400`; no se guarda nada | `enviarContacto_conIdProductoInvalido_retorna400` |
| CON-09 | Petición sin token | Envía un mensaje válido | `401`; no se guarda nada | `enviarContacto_sinToken_retorna401` |
| CON-10 | Petición con token inválido o expirado | Envía un mensaje válido | `401`; no se guarda nada | `enviarContacto_conTokenInvalido_retorna401` |
| CON-11 | Agricultor autenticado | Envía un mensaje válido | `403`; no se guarda nada | `enviarContacto_conRolAgricultor_retorna403` |
| CON-12 | Comprador autenticado y producto disponible | Envía además `comprador_id` de otro comprador | `200`; el mensaje queda a nombre del comprador del token | `enviarContacto_conCompradorIdEnElCuerpo_loIgnora` |
| CON-13 | Token de rol `COMPRADOR` sin perfil de comprador | Envía un mensaje válido | `403` con mensaje genérico; no se guarda nada | `enviarContacto_conCompradorSinPerfil_retorna403` |
| CON-14 | Comprador autenticado y producto disponible | Envía dos veces el mismo mensaje | Dos `200` y dos registros distintos | `enviarContacto_conMensajeRepetido_guardaDosRegistros` |
| CON-15 | Comprador autenticado | Envía un JSON mal formado o `"id_producto": "abc"` | `400` con el mapa `{"json": ...}`; no se guarda nada | `enviarContacto_conJsonMalFormado_retorna400` |

En todos los rechazos no se crea ningún mensaje. En ningún caso cambia el stock, se crea un pedido ni se emite un evento.

## 6. Alcance

Este contrato **no** incluye envío de correo ni notificación al agricultor, chat en tiempo real, consulta o listado de mensajes, pedidos, pagos, descuento o reserva de stock, ni frontend. El mensaje solo queda guardado para que el contacto se pueda continuar en una historia posterior.

## 7. Datos y esquema

La tabla `mensaje_contacto` ya está definida en [schema.sql](diseno/schema.sql) con `id`, `comprador_id`, `producto_id`, `mensaje` (`TEXT`) y `fecha_envio` (valor por defecto `CURRENT_TIMESTAMP`). No se prevén cambios de estructura: T02-HU05 solo debe pasar la tabla de `[DISEÑO]` a `[IMPLEMENTADA]`. El límite de 2000 caracteres se valida en el DTO, no en la base.

Depende de la tabla `comprador` y del perfil Comprador de HU-17, que todavía no existen como entidad en `develop`.

## 8. Entrega por tareas y verificación

- **T01-HU05 (#166):** este contrato, criterios de HU-05 en [BACKLOG.md](../BACKLOG.md) y matriz de [trazabilidad BDD](trazabilidad-bdd.md#hu-05--contacto-directo-planificado).
- **T02-HU05 (#167):** entidad `MensajeContacto`, repositorio y DTOs, con pruebas de relaciones y de validación.
- **T03-HU05 (#168):** servicio transaccional de contacto con TDD.
- **T04-HU05 (#169):** `ContactoController`, regla de rol en `SecurityConfig`, mapeos de error en `ManejadorErrores` y el manejador de `HttpMessageNotReadableException` (JSON mal formado → 400, escenario CON-15). Hoy ninguna tarea de HU-17 lo incluye; si alguna lo integra antes en `develop`, T04-HU05 lo reutiliza y conserva el formato de mapa.
- **T05-HU05 (#170):** escenarios CON-01 a CON-15 con comprador real registrado mediante HU-17; completar la matriz con pruebas reales.
- **T06-HU05 (#171):** documentación del endpoint implementado, diagrama de secuencia y demostración.

Los consumidores no deben interpretar este documento como evidencia de que el endpoint esté implementado.

## 9. Fuentes contrastadas en develop

Línea base: `cea148f`, posterior al PR #172 de HU-17.

- [Esquema SQL](diseno/schema.sql): tablas `mensaje_contacto` y `comprador`.
- [Producto](../src/main/java/com/agrovalleconnect/agrovalle_connect/model/Producto.java): estado y `cantidadDisponible`.
- [SecurityConfig](../src/main/java/com/agrovalleconnect/agrovalle_connect/config/SecurityConfig.java): reglas por rol existentes.
- [ManejadorErrores](../src/main/java/com/agrovalleconnect/agrovalle_connect/exception/ManejadorErrores.java): mapeos 400, 403 y 404.
- [Contrato de registro HU-17](contrato-registro-hu17.md) y [contrato del catálogo HU-04](contrato-catalogo-hu04.md): formato y reglas compartidas.
