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
| **Total** | **HU-17: 21 h · HU-04: 16 h · HU-05: 12 h** | **49** |

En la línea base `caf1151`, HU-05 ya incluye el endpoint de contacto. Sus pruebas BDD (#170) y la documentación final (#171) siguen pendientes en esa línea base.

## HU-17: lo ejecutado y lo pendiente

Corte del 24 de noviembre de 2026, sobre `develop` en `62f037a`. Las horas son las estimadas en el Kanban; las reales se registran en cada tarjeta y se comparan en la Sprint Review.

| Tarea | Responsable | PR | Estado | Horas estimadas |
|---|---|---|---|---:|
| T01-HU17 (#152) | Sebastián | [#172](https://github.com/Angel-uniajc/AgrovalleConnect/pull/172) | Integrada en `develop` | 2 |
| T02-HU17 (#153) | Joel | [#175](https://github.com/Angel-uniajc/AgrovalleConnect/pull/175) | Integrada en `develop` | 2 |
| T03-HU17 (#154) | Sebastián | [#176](https://github.com/Angel-uniajc/AgrovalleConnect/pull/176) | Integrada en `develop` | 2 |
| T04-HU17 (#155) | Joel | [#178](https://github.com/Angel-uniajc/AgrovalleConnect/pull/178) | Integrada en `develop` | 4 |
| T05-HU17 (#156) | Ángel | [#180](https://github.com/Angel-uniajc/AgrovalleConnect/pull/180) | Integrada en `develop` | 3 |
| T06-HU17 (#157) | Juanito | [#187](https://github.com/Angel-uniajc/AgrovalleConnect/pull/187) | Integrada en `develop` | 2 |
| T07-HU17 (#158) | Juanito | [#191](https://github.com/Angel-uniajc/AgrovalleConnect/pull/191) | Integrada en `develop` | 4 |
| T08-HU17 (#159) | Juanito | Este PR | En revisión | 2 |
| **Total** | | | **Ejecutado: 7 de 8 tareas (19 h) · Pendiente: 1 tarea (2 h)** | **21** |

**Qué quedó funcionando:** `POST /api/v1/auth/register` registra compradores con `rol` igual a `COMPRADOR` (y sigue registrando agricultores sin `rol`), guarda `Usuario` y `Comprador` en una sola transacción y responde 201, 400 o 409 según el [contrato](contrato-registro-hu17.md). `POST /api/v1/auth/login` entrega al comprador un JWT con su rol y su `perfil_id`. La secuencia está en [secuencia-registro-comprador-hu17.md](architecture/secuencia-registro-comprador-hu17.md), y los pasos 5 y 6 del recorrido de demostración muestran el registro y el login de un comprador.

**Qué no incluye:** pantallas de frontend ni edición o eliminación de la cuenta del comprador.

### Pruebas que verifican HU-17

Cada escenario BDD y su prueba están en la [matriz de trazabilidad](trazabilidad-bdd.md#hu-17--registro-de-comprador). Agrupadas por clase:

| Clase | Tipo | Pruebas |
|---|---|---|
| `RegistroCompradorRequestTest` | Unitaria del DTO | `correoSinFormato_esRechazado`, `nombreMayorA50Caracteres_esRechazado`, `correoMayorA50Caracteres_esRechazado` |
| `CompradorRegisterServiceIntegrationTest` | Integración del servicio | `registrar_guardaUsuarioYCompradorConHashBcrypt`, `correoDeAgricultor_noPuedeUsarseParaUnComprador`, `documentoDuplicadoQueEsquivaLaVerificacion_loFrenaLaBaseYSeDeshaceTodo`, `falloAlCrearElPerfil_deshaceTambienElUsuario` |
| `AuthControllerIntegrationTest` | Integración HTTP (MockMvc) | `registrarComprador_valido_retorna201ConLosCuatroCamposPublicos`, `registrarComprador_valido_respuestaNoExponePasswordNiHash`, `registrarComprador_correoODocumentoDuplicado_retorna409`, `registrarComprador_conCorreoDeAgricultor_retorna409`, `registrarComprador_tipoInvalido_retorna400`, `registrarComprador_conTipoEnMayusculasOMixtas_seNormalizaYRetorna201`, `registrarComprador_conDocumentoIgualACedulaDeAgricultor_retorna201`, `registrarComprador_conCamposFaltantes_retorna400`, `registrarComprador_conPasswordInvalida_retorna400`, `registrarComprador_conDocumentoInvalido_retorna400`, `registrar_rolDesconocido_retorna400`, `registrar_jsonIncompleto_retornaSoloMensajePublico`, `registroExitoso_persisteYGuardaHashNoTextoPlano`, `registrarAgricultor_conRolExplicito_persistePerfilDeAgricultor` |
| `AuthLoginIntegrationTest` | Integración HTTP del login | `login_compradorRegistrado_retornaTokenConRolYPerfilDelComprador` |

## Recorrido de demostración del catálogo

Recorrido verificado por HTTP con Java 25 y H2 en memoria el 24 de noviembre de 2026. [Registro de las nueve respuestas](evidencias/sprint3-catalogo-http.json). Iniciar el backend con una base de prueba inicializada. En Postman usar cuerpos JSON y, para fincas, publicación y consulta, seleccionar **Bearer Token** con el token del rol indicado. Los datos siguientes son ficticios.

1. Registrar al agricultor con `POST /api/v1/auth/register` y `{"nombre":"Ana Demo","ubicacion_valle":"Dagua","cedula":"12345678","correo":"ana@example.org","password":"Demo@1234"}`. Esperar `201`.
2. Iniciar sesión con `POST /api/v1/auth/login` y `{"email":"ana@example.org","password":"Demo@1234"}`. Esperar `200` y guardar el campo `token`.
3. Con el token del agricultor, enviar `POST /api/v1/fincas` con `{"nombre":"Finca Demo","municipio":"Dagua"}`. Esperar `201` y guardar el ID de la finca.
4. En la base del backend ejecutar `SELECT id FROM categoria WHERE nombre = 'Frutas';`. Con el mismo token, enviar `POST /api/v1/productos` con `finca_id` y `categoria_id` numéricos obtenidos en estos pasos, `tipo` igual a `Mango`, `cantidad` igual a 25, `precio_unitario` igual a 3200 y `fecha_cosecha` igual a la fecha de hoy en formato `AAAA-MM-DD`. Esperar `201` y guardar el ID del producto.
5. Registrar al comprador con `POST /api/v1/auth/register` y `{"nombre":"Luis Demo","rol":"COMPRADOR","tipo_comprador":"comerciante","documento":"87654321","correo":"luis@example.org","password":"Demo@1234"}`. Esperar `201`. El documento admite de 6 a 10 dígitos y el tipo admite `individual`, `comerciante` o `restaurante`. La contraseña requiere al menos ocho caracteres con mayúscula, minúscula, número y un símbolo de `@$!%*?&`.
6. Enviar `POST /api/v1/auth/login` con `{"email":"luis@example.org","password":"Demo@1234"}`. Esperar `200` y usar el nuevo `token`. El registro recibe `correo` y el login recibe `email`.
7. Consultar `GET /api/v1/productos?municipio=Dagua&categoria=Frutas` con el token del comprador. Esperar `200` y localizar el ID del producto del paso 4. Con un municipio inexistente se espera `200` y `[]`. Al quitar el token se espera `401`.

[Ejemplos de consulta](../README.md) · [Contrato HU-04](contrato-catalogo-hu04.md) · [Matriz BDD](trazabilidad-bdd.md)
