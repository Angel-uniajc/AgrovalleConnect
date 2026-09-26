# Escenarios Alternativos BDD — AgroValle Connect

**Propósito:** complementar los criterios de aceptación existentes (camino feliz) con los escenarios de error que exige la profe: `400 Bad Request`, `401 Unauthorized` y `404 Not Found`, en formato Gherkin (Given-When-Then).

**Convención usada:**
- `400` → la solicitud llega mal formada o viola una regla de negocio (datos inválidos, duplicados, fuera de rango).
- `401` → falta el token JWT o es inválido/expirado.
- `404` → el recurso referenciado (producto, pedido, finca, etc.) no existe.

Solo se incluyen los códigos que aplican realmente a cada historia — no todas las 17 necesitan los tres.

---

## HU-01 — Registro de Agricultores

**Escenario Alternativo 1 — Email o cédula ya registrados (400)**
- **Given** que el email o la cédula enviados ya existen en la base de datos
- **When** el usuario envía `POST /api/v1/auth/register`
- **Then** el sistema responde `400 Bad Request` con un mensaje indicando el campo duplicado, y no crea ningún registro

**Escenario Alternativo 2 — Datos incompletos o inválidos (400)**
- **Given** que el JSON enviado no incluye `nombre`, `email`, `contraseña` o `cedula`, o la contraseña tiene menos de 8 caracteres
- **When** el usuario envía `POST /api/v1/auth/register`
- **Then** el sistema responde `400 Bad Request` detallando el/los campo(s) inválido(s)

---

## HU-02 — Publicación de Cosechas

**Escenario Alternativo 1 — Sin autenticación (401)**
- **Given** que la solicitud no incluye un token JWT válido en el header `Authorization`
- **When** se envía `POST /api/v1/productos`
- **Then** el sistema responde `401 Unauthorized` y no publica el producto

**Escenario Alternativo 2 — Fecha de cosecha inválida (400)**
- **Given** un agricultor autenticado que envía una `fecha_cosecha` anterior a la fecha actual
- **When** publica el producto
- **Then** el sistema responde `400 Bad Request` indicando que la fecha no puede ser pasada

---

## HU-03 — Visualización de Precios Regionales

**Escenario Alternativo 1 — Categoría sin transacciones registradas (404)**
- **Given** que no existen transacciones registradas para la categoría solicitada
- **When** el usuario consulta el precio promedio de esa categoría
- **Then** el sistema responde `404 Not Found` indicando que no hay datos suficientes para calcular el promedio

---

## HU-04 — Filtro de Categorías y Municipios

**Escenario Alternativo 1 — Municipio fuera del Valle del Cauca (400)**
- **Given** que el parámetro `municipio` no corresponde a un municipio válido del Valle del Cauca
- **When** se realiza `GET /api/v1/productos?municipio={municipio}&categoria={categoria}`
- **Then** el sistema responde `400 Bad Request` indicando que el municipio no es válido

---

## HU-05 — Contacto Directo / Intención de Compra

**Escenario Alternativo 1 — Sin autenticación (401)**
- **Given** que la solicitud no incluye un token JWT válido
- **When** se envía `POST /api/v1/contacto/mensaje`
- **Then** el sistema responde `401 Unauthorized`

**Escenario Alternativo 2 — Producto inexistente (404)**
- **Given** un comprador autenticado que envía un `id_producto` que no existe o ya no está activo
- **When** intenta enviar el mensaje de contacto
- **Then** el sistema responde `404 Not Found`

**Escenario Alternativo 3 — Mensaje vacío (400)**
- **Given** un comprador autenticado con un `id_producto` válido
- **When** envía la solicitud sin contenido en el mensaje
- **Then** el sistema responde `400 Bad Request`

---

## HU-06 — Asignar Ruta de Envío

**Escenario Alternativo 1 — Lote no encontrado o no alistado (404)**
- **Given** un `id` de lote que no existe, o que existe pero no está en estado "Alistado"
- **When** el productor intenta confirmar una ruta de envío
- **Then** el sistema responde `404 Not Found`

**Escenario Alternativo 2 — Fecha/hora de salida inválida (400)**
- **Given** un lote válido en estado "Alistado"
- **When** el productor envía una fecha/hora de salida anterior al momento actual
- **Then** el sistema responde `400 Bad Request`

---

## HU-07 — Pago Exitoso con Producto Disponible

**Escenario Alternativo 1 — Sin autenticación (401)**
- **Given** que la solicitud de pago no incluye un token JWT válido
- **When** se intenta procesar el pago
- **Then** el sistema responde `401 Unauthorized`

**Escenario Alternativo 2 — Producto inexistente (404)**
- **Given** un usuario autenticado que referencia un producto que ya no existe en el catálogo
- **When** intenta confirmar la compra
- **Then** el sistema responde `404 Not Found`

**Escenario Alternativo 3 — Datos de pago inválidos (400)**
- **Given** un usuario autenticado con un producto válido
- **When** los datos de la pasarela de pago (tarjeta, monto) son inválidos o están incompletos
- **Then** el sistema responde `400 Bad Request` y no registra la orden

---

## HU-08 — Cantidad Solicitada Superior al Inventario

**Escenario Alternativo 1 — Producto inexistente (404)**
- **Given** un usuario autenticado que intenta comprar un `id_producto` inexistente
- **When** procesa la compra
- **Then** el sistema responde `404 Not Found`

*(El escenario de stock insuficiente ya está cubierto como camino principal de esta historia — HU-08 es en sí misma el "escenario alternativo" de HU-07, por eso solo se agrega el 404).*

---

## HU-09 — Calificar Producto

**Escenario Alternativo 1 — Pedido no encontrado o no completado (404)**
- **Given** un `id` de pedido que no existe, o que existe pero no está en estado "Completado/Recibido"
- **When** el comprador intenta calificar
- **Then** el sistema responde `404 Not Found`

**Escenario Alternativo 2 — Calificación fuera de rango (400)**
- **Given** un pedido completado y recibido
- **When** el comprador envía una calificación fuera del rango 1 a 5
- **Then** el sistema responde `400 Bad Request`

---

## HU-10 — Calificar Transporte

**Escenario Alternativo 1 — Pedido no entregado (404)**
- **Given** un pedido que no existe o que aún no está marcado como entregado
- **When** el comprador intenta calificar el transporte
- **Then** el sistema responde `404 Not Found`

**Escenario Alternativo 2 — Calificación fuera de rango (400)**
- **Given** un pedido entregado y completado
- **When** el comprador envía una calificación fuera del rango 1 a 5
- **Then** el sistema responde `400 Bad Request`

---

## HU-11 — Descargar Recibo de Compra

**Escenario Alternativo 1 — Sin autenticación (401)**
- **Given** que la solicitud no incluye un token JWT válido
- **When** se intenta descargar el recibo
- **Then** el sistema responde `401 Unauthorized`

**Escenario Alternativo 2 — Compra no encontrada (404)**
- **Given** un comerciante autenticado que referencia un `id` de compra que no existe o no le pertenece
- **When** solicita "Descargar recibo"
- **Then** el sistema responde `404 Not Found`

---

## HU-12 — Realizar Pedido

**Escenario Alternativo 1 — Sin autenticación (401)**
- **Given** que la solicitud no incluye un token JWT válido
- **When** se envía `POST /api/v1/pedidos`
- **Then** el sistema responde `401 Unauthorized`

**Escenario Alternativo 2 — Carrito vacío o producto inexistente (400/404)**
- **Given** un comprador autenticado con el carrito vacío
- **When** intenta confirmar el pedido
- **Then** el sistema responde `400 Bad Request`
- **Given**, alternativamente, que algún producto del carrito ya no existe en el catálogo
- **When** intenta confirmar el pedido
- **Then** el sistema responde `404 Not Found`

---

## HU-13 — Consultar Ruta de Envío

**Escenario Alternativo 1 — Código de seguimiento inexistente (404)**
- **Given** un código de seguimiento que no corresponde a ningún envío registrado
- **When** el usuario consulta el estado en el Módulo Logística
- **Then** el sistema responde `404 Not Found`

---

## HU-14 — Registro de Fincas por Municipio

**Escenario Alternativo 1 — Sin autenticación (401)**
- **Given** que la solicitud no incluye un token JWT válido
- **When** se envía `POST /api/v1/fincas`
- **Then** el sistema responde `401 Unauthorized`

**Escenario Alternativo 2 — Municipio fuera del Valle del Cauca (400)**
- **Given** un agricultor autenticado que envía un municipio que no pertenece al Valle del Cauca
- **When** registra la finca
- **Then** el sistema responde `400 Bad Request`

---

## HU-15 — Confirmación de Alistamiento de Lote

**Escenario Alternativo 1 — Sin autenticación (401)**
- **Given** que la solicitud no incluye un token JWT válido
- **When** se envía `PATCH /api/v1/pedidos/{id}/alistamiento`
- **Then** el sistema responde `401 Unauthorized`

**Escenario Alternativo 2 — Pedido no encontrado o no pagado (404)**
- **Given** un `id` de pedido que no existe, o que existe pero no está en estado "Pago aprobado"
- **When** el productor intenta marcarlo como "Alistado"
- **Then** el sistema responde `404 Not Found`

---

## HU-16 — Inicio de Sesión

**Escenario Alternativo 1 — Credenciales incorrectas (401)**
- **Given** un email registrado con una contraseña incorrecta, o un email que no existe
- **When** el usuario envía `POST /api/v1/auth/login`
- **Then** el sistema responde `401 Unauthorized` con un mensaje genérico (sin revelar si falló el email o la contraseña)

**Escenario Alternativo 2 — Datos faltantes (400)**
- **Given** que el request no incluye `email` o `contraseña`
- **When** se envía `POST /api/v1/auth/login`
- **Then** el sistema responde `400 Bad Request`

---

## HU-17 — Registro de Comprador

**Escenario Alternativo 1 — Email duplicado (400)**
- **Given** que el email enviado ya existe en la base de datos
- **When** el usuario envía `POST /api/v1/auth/register` con `rol=COMPRADOR`
- **Then** el sistema responde `400 Bad Request`

**Escenario Alternativo 2 — Tipo de comprador inválido (400)**
- **Given** un JSON con `tipo_comprador` fuera de los valores permitidos (individual, comerciante, restaurante)
- **When** se envía la solicitud de registro
- **Then** el sistema responde `400 Bad Request`
