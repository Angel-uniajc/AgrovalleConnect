# Planificación del Sprint 3

**Sprint Goal.** Permitir que un comprador se registre e inicie sesión, encuentre cosechas disponibles por municipio y categoría y contacte al agricultor con un mensaje que quede asociado a la oferta.

El compromiso suma **11 puntos de historia**: HU-17 (registro de comprador, 5), HU-04 (catálogo, 3) y HU-05 (contacto, 3). Las horas de la tabla son estimaciones por tarea registradas en el Kanban.

| Tarjeta | Trabajo | Horas estimadas |
|---|---|---:|
| [#152](https://github.com/Angel-uniajc/AgrovalleConnect/issues/152) | T01-HU17 — Alinear el contrato de registro y los escenarios BDD | 2 |
| [#153](https://github.com/Angel-uniajc/AgrovalleConnect/issues/153) | T02-HU17 — Implementar el perfil Comprador y su repositorio | 2 |
| [#154](https://github.com/Angel-uniajc/AgrovalleConnect/issues/154) | T03-HU17 — Crear DTOs y validación del registro por rol | 2 |
| [#155](https://github.com/Angel-uniajc/AgrovalleConnect/issues/155) | T04-HU17 — Registrar compradores con TDD y transacción atómica | 4 |
| [#156](https://github.com/Angel-uniajc/AgrovalleConnect/issues/156) | T05-HU17 — Integrar el registro de comprador en AuthController | 3 |
| [#157](https://github.com/Angel-uniajc/AgrovalleConnect/issues/157) | T06-HU17 — Emitir JWT con el perfil del comprador al iniciar sesión | 2 |
| [#158](https://github.com/Angel-uniajc/AgrovalleConnect/issues/158) | T07-HU17 — Verificar los escenarios BDD y la regresión de autenticación | 4 |
| [#159](https://github.com/Angel-uniajc/AgrovalleConnect/issues/159) | T08-HU17 — Documentar el flujo y el diagrama de secuencia de compradores | 2 |
| [#160](https://github.com/Angel-uniajc/AgrovalleConnect/issues/160) | T01-HU04 — Precisar el contrato del catálogo y sus filtros | 2 |
| [#161](https://github.com/Angel-uniajc/AgrovalleConnect/issues/161) | T02-HU04 — Consultar ofertas activas por municipio y categoría | 3 |
| [#162](https://github.com/Angel-uniajc/AgrovalleConnect/issues/162) | T03-HU04 — Crear DTO y servicio de consulta del catálogo | 3 |
| [#163](https://github.com/Angel-uniajc/AgrovalleConnect/issues/163) | T04-HU04 — Exponer GET de productos y conservar la seguridad existente | 2 |
| [#164](https://github.com/Angel-uniajc/AgrovalleConnect/issues/164) | T05-HU04 — Verificar BDD del catálogo con datos aislados | 4 |
| [#165](https://github.com/Angel-uniajc/AgrovalleConnect/issues/165) | T06-HU04 — Documentar el catálogo y su diagrama de secuencia | 2 |
| [#166](https://github.com/Angel-uniajc/AgrovalleConnect/issues/166) | T01-HU05 — Definir el contrato y los escenarios de contacto | 1 |
| [#167](https://github.com/Angel-uniajc/AgrovalleConnect/issues/167) | T02-HU05 — Crear MensajeContacto, repositorio y DTOs | 2 |
| [#168](https://github.com/Angel-uniajc/AgrovalleConnect/issues/168) | T03-HU05 — Implementar el servicio transaccional de contacto con TDD | 3 |
| [#169](https://github.com/Angel-uniajc/AgrovalleConnect/issues/169) | T04-HU05 — Exponer el contacto exclusivo para compradores | 2 |
| [#170](https://github.com/Angel-uniajc/AgrovalleConnect/issues/170) | T05-HU05 — Verificar la persistencia y seguridad de mensajes con BDD | 3 |
| [#171](https://github.com/Angel-uniajc/AgrovalleConnect/issues/171) | T06-HU05 — Documentar el contacto y la demostración del Sprint 3 | 1 |

## Recorrido de demostración del catálogo

1. En un entorno de prueba, iniciar sesión como agricultor con `POST /api/v1/auth/login`. Con su token, registrar una finca en Dagua mediante `POST /api/v1/fincas` y conservar el ID devuelto.
2. Publicar mango mediante `POST /api/v1/productos` con ese ID, el ID real de la categoría Frutas, cantidad y precio positivos y la fecha de hoy. Conservar el ID recibido en el `201`.
3. Registrar un comprador de prueba con `POST /api/v1/auth/register`, rol `COMPRADOR`, nombre, correo, contraseña, documento y `tipo_comprador` válido. Iniciar sesión con su correo en el campo `email` y su contraseña en `password`.
4. Usar el token del comprador para consultar `GET /api/v1/productos?municipio=Dagua&categoria=Frutas`. Buscar en el arreglo el ID de la publicación del paso 2. Repetir con un municipio inexistente para comprobar el arreglo vacío y sin token para comprobar el `401`.

[Ejemplos de consulta](../README.md) · [Contrato HU-04](contrato-catalogo-hu04.md) · [Matriz BDD](trazabilidad-bdd.md)
