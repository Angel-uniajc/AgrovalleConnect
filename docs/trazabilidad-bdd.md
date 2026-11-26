# Trazabilidad BDD → JUnit 5 — AgroValle Connect

Cada escenario Given/When/Then del backlog se verifica con al menos una prueba automatizada.
Los nombres propuestos en `sprint-1-planning.md` (sección 7) se relacionan con las pruebas reales. La matriz de HU-14 está en [la planificación del Sprint 2](sprint-2-planning.md#3-matriz-de-trazabilidad-bdd--junit-5--hu-14).

## HU-01 — Registro de Agricultores

| Escenario BDD | Resultado esperado | Nombre propuesto (planning) | Prueba real |
| :--- | :---: | :--- | :--- |
| Registro válido | `201 Created` y persiste con hash | `registrarAgricultor_conDatosValidos_retorna201` | `AuthControllerIntegrationTest#registroExitoso_persisteYGuardaHashNoTextoPlano` |
| Cédula duplicada | `409 Conflict`, no se duplica | `registrarAgricultor_conCedulaDuplicada_rechazaRegistro` | `AuthControllerIntegrationTest#rechazaCedulaDuplicada`, `AgricultorRegisterServiceTest#registrar_cedulaDuplicada_lanzaExcepcionYNoGuardaNada` |
| Campos vacíos | `400 Bad Request`, no se guarda | `registrarAgricultor_conCamposVacios_retorna400` | `AuthControllerIntegrationTest#registrarAgricultor_conCamposVacios_retorna400` |
| Cédula inválida | `400 Bad Request`, no se guarda | — | `AuthControllerIntegrationTest#registrarAgricultor_conCedulaInvalida_retorna400`, `RegistroAgricultorRequestTest` |
| Correo duplicado (adicional) | `409 Conflict` | — | `AuthControllerIntegrationTest#rechazaCorreoDuplicado` |
| Contraseña débil (adicional) | `400 Bad Request`, no se guarda | — | `AuthControllerIntegrationTest#contrasenaDebil_devuelve400YNoGuardaNada` |

## HU-04 — Catálogo y filtros

El [diagrama de secuencia de HU-04](diseno/diagrama-secuencia.md#10-hu-04--consulta-del-catálogo) muestra dónde se valida el JWT y cómo se construye la respuesta que comprueban los escenarios siguientes.

Contrato definido en [contrato-catalogo-hu04.md](contrato-catalogo-hu04.md#4-escenarios-bdd-y-nombres-de-prueba-propuestos), asociado a T01-HU04 (#160). Los Given/When/Then completos están allí. Los escenarios se verifican en T05-HU04 (#164) con `CatalogoIntegrationTest`: un comprador real se registra y autentica por HU-17, y la prueba siembra sus propias fincas y ofertas en Dagua, Palmira y Buga, con productos `INACTIVO`, `AGOTADO` y stock cero. Cada prueba se deshace al terminar (`@Transactional`), y después de cada consulta se comprueba que no cambiaron ni la cantidad de productos ni el stock, y que no se emitió `CosechaPublicadaEvent`.

| ID BDD | Consulta o condición | Resultado esperado | Prueba real |
|---|---|---|---|
| CAT-01 | Consulta `?municipio=Dagua&categoria=Frutas` | `200`; solo Dagua/Frutas, con los ocho campos públicos | `CatalogoIntegrationTest#consultarCatalogo_conAmbosFiltros_retornaSoloCoincidencias` |
| CAT-02 | Consulta ambos filtros | `200` y `[]`, sin relajar ninguno de los filtros | `CatalogoIntegrationTest#consultarCatalogo_sinCoincidencias_retornaArregloVacio` |
| CAT-03 | Consulta sin parámetros | `200`; todas las ofertas disponibles | `CatalogoIntegrationTest#consultarCatalogo_sinFiltros_retornaOfertasDisponibles` |
| CAT-04 | Consulta solo `?municipio=Dagua` | `200`; todas las categorías de Dagua y ningún otro municipio | `CatalogoIntegrationTest#consultarCatalogo_soloMunicipio_filtraPorMunicipio` |
| CAT-05 | Consulta solo `?categoria=Frutas` | `200`; Frutas de todos los municipios y ninguna otra categoría | `CatalogoIntegrationTest#consultarCatalogo_soloCategoria_filtraPorCategoria` |
| CAT-06 | Consulta `?municipio=pALmIrA&categoria=fRuTaS` | Mismo contenido que con los nombres canónicos | `CatalogoIntegrationTest#consultarCatalogo_conMayusculasMixtas_ignoraMayusculas` |
| CAT-07 | Consulta `?municipio=%20Palmira%20&categoria=%20Frutas%20` | Mismo contenido que sin espacios periféricos | `CatalogoIntegrationTest#consultarCatalogo_conEspaciosPerifericos_normalizaFiltros` |
| CAT-08 | Consulta filtros vacíos o solo espacios, incluidos uno vacío y otro informado | Los vacíos se omiten; el filtro informado se conserva; ambos vacíos equivalen a no filtrar | `CatalogoIntegrationTest#consultarCatalogo_conFiltrosVacios_losTrataComoAusentes` |
| CAT-09 | Consulta municipio o categoría desconocidos, incluso junto a otro filtro válido | `200` y `[]` | `CatalogoIntegrationTest#consultarCatalogo_conFiltroDesconocido_retornaArregloVacio` |
| CAT-10 | Consulta con y sin filtros | Solo productos `ACTIVO` con stock mayor que cero; si no queda ninguno, `[]` | `CatalogoIntegrationTest#consultarCatalogo_conOfertasNoDisponibles_lasExcluye` |
| CAT-11 | Consulta el catálogo | IDs en orden ascendente, sin duplicados ni campos privados | `CatalogoIntegrationTest#consultarCatalogo_conVariasOfertas_ordenaPorIdYRespetaContrato` |
| CAT-12 | Consulta el catálogo sin token | `401`, sin ejecutar la consulta del servicio | `CatalogoIntegrationTest#consultarCatalogo_sinToken_retorna401` |
| CAT-13 | Consulta con token inválido, de otra clave o expirado | `401`, sin ejecutar la consulta del servicio | `CatalogoIntegrationTest#consultarCatalogo_conTokenInvalidoOExpirado_retorna401` |
| CAT-14 | Agricultor autenticado consulta el catálogo | `200` con las mismas reglas de selección; no recibe `403` por su rol | `CatalogoIntegrationTest#consultarCatalogo_conRolAgricultor_retorna200` |
| CAT-15 | Consulta nombres parciales, sin la tilde requerida, o con `%`/`_` | No se amplía la coincidencia literal de nombre completo; `[]` si no existe ese nombre | `CatalogoIntegrationTest#consultarCatalogo_conNombreNoExacto_noAmpliaLaBusqueda` |

## HU-05 — Contacto directo (implementado y verificado)

Contrato definido en [contrato-contacto-hu05.md](contrato-contacto-hu05.md#5-escenarios-bdd-y-nombres-de-prueba-propuestos), asociado a T01-HU05 (#166). Los Given/When/Then completos están allí. **El endpoint `POST /api/v1/contacto/mensaje`, la entidad `MensajeContacto` y `ContactoService` están implementados y verificados** con persistencia real en H2, comprador registrado mediante HU-17 y producto publicado por un agricultor. T05-HU05 (#170) completó la verificación con [`ContactoControllerIntegrationTest`](../src/test/java/com/agrovalleconnect/agrovalle_connect/controller/ContactoControllerIntegrationTest.java): cada fila enlaza su prueba real. Complementan la cobertura `ContactoServiceTest` y `ContactoControllerWebTest`. T06-HU05 (#171) documenta la demostración.

| ID BDD | Consulta o condición | Resultado esperado | Prueba real | Estado |
|---|---|---|---|---|
| CON-01 | Comprador envía `id_producto` y mensaje válidos | `200`; se guarda un mensaje con el comprador del token, el producto y la fecha del servidor | `ContactoControllerIntegrationTest#enviarContacto_conDatosValidos_retorna200YPersiste` | Verificado: T05-HU05 (#170) |
| CON-02 | Envía un `id_producto` que no existe | `404`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conProductoInexistente_retorna404` | Verificado: T05-HU05 (#170) |
| CON-03 | Producto `INACTIVO` | `400`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conProductoInactivo_retorna400` | Verificado: T05-HU05 (#170) |
| CON-04 | Producto `AGOTADO` | `400`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conProductoAgotado_retorna400` | Verificado: T05-HU05 (#170) |
| CON-05 | Producto `ACTIVO` con stock 0 | `400`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conStockCero_retorna400` | Verificado: T05-HU05 (#170) |
| CON-06 | `mensaje` nulo, vacío o solo espacios | `400`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conMensajeVacio_retorna400` | Verificado: T05-HU05 (#170) |
| CON-07 | Mensaje de 2000 y de 2001 caracteres | El de 2000 responde `200`; el de 2001 responde `400` | `ContactoControllerIntegrationTest#enviarContacto_conMensajeEnElLimite_aceptaHasta2000` | Verificado: T05-HU05 (#170) |
| CON-08 | `id_producto` nulo, 0 o negativo | `400`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conIdProductoInvalido_retorna400` | Verificado: T05-HU05 (#170) |
| CON-09 | Petición sin token | `401`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_sinToken_retorna401` | Verificado: T05-HU05 (#170) |
| CON-10 | Token inválido o expirado | `401`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conTokenInvalido_retorna401` | Verificado: T05-HU05 (#170) |
| CON-11 | Agricultor autenticado | `403`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conRolAgricultor_retorna403` | Verificado: T05-HU05 (#170) |
| CON-12 | Cuerpo con `comprador_id` de otro comprador | `200`; el mensaje queda a nombre del comprador del token | `ContactoControllerIntegrationTest#enviarContacto_conCompradorIdEnElCuerpo_loIgnora` | Verificado: T05-HU05 (#170) |
| CON-13 | Token de rol `COMPRADOR` sin perfil de comprador | `403` con mensaje genérico; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conCompradorSinPerfil_retorna403` | Verificado: T05-HU05 (#170) |
| CON-14 | Mismo mensaje enviado dos veces | Dos `200` y dos registros distintos | `ContactoControllerIntegrationTest#enviarContacto_conMensajeRepetido_guardaDosRegistros` | Verificado: T05-HU05 (#170) |
| CON-15 | JSON mal formado o `id_producto` no numérico | `400` con el mapa `{"json": ...}`; no se guarda nada | `ContactoControllerIntegrationTest#enviarContacto_conJsonMalFormado_retorna400` | Verificado: T05-HU05 (#170) |

## HU-08 — Validación de stock (planificado)

El contrato completo está en [contrato-stock-hu08.md](contrato-stock-hu08.md) y corresponde a T01-HU08 (#207). La validación ocurre al crear el pedido en `POST /api/v1/pedidos` (HU-12), antes de reservar y antes de cualquier pago. Los escenarios STK-01 a STK-11 quedan planificados para T03-HU08 (#210, pruebas unitarias) y T04-HU08 (#211, pruebas de integración); todavía no se afirma cobertura automatizada.

| ID BDD | Condición | Resultado esperado | Estado |
|---|---|---|---|
| STK-01 | Cantidad superior al stock (16 con 10 disponibles) | `400` "Stock insuficiente" con `id_producto`, `cantidad_solicitada` y `stock_disponible`; stock sin cambios | Planificado |
| STK-02–STK-03 | Cantidad igual o menor al stock | Pasa la validación; `201` según PED-08 y PED-01 | Planificado |
| STK-04–STK-06 | Stock en cero, estado `AGOTADO` o `INACTIVO` con stock en cero | `400` "Producto agotado" con `id_producto` | Planificado |
| STK-07 | Pedido mixto: una línea válida y otra insuficiente | `400` por la línea insuficiente; ninguna reserva | Planificado |
| STK-08 | Varias líneas rechazadas | `400` solo por la primera en el orden de `items` | Planificado |
| STK-09–STK-10 | Producto repetido en varias líneas | Se valida la suma: `400` si la supera, `201` si la alcanza | Planificado |
| STK-11 | Frontera decimal (10.01 con 10.00 disponibles) | `400` "Stock insuficiente" | Planificado |

## HU-12 — Realizar pedido

El contrato completo está en [contrato-pedido-hu12.md](contrato-pedido-hu12.md) y corresponde a T01-HU12 (#199). La decisión del sprint es enviar la lista `items` directamente, sin tabla de carrito persistente, guardar `costo_flete` en `pedido`, reservar stock dentro de la transacción y publicar `PedidoCreadoEvent` después del guardado.

T07-HU12 (#205) verifica los escenarios de `PedidoControllerIntegrationTest` con MockMvc y H2. Cada prueba crea su agricultor, finca en Palmira, cosecha y comprador con los endpoints reales (el comprador con el flujo de HU-17), y en cada rechazo comprueba que no se crea el pedido y que el stock no cambia.

| ID BDD | Condición | Resultado esperado | Prueba real | Estado |
|---|---|---|---|---|
| PED-01 | Comprador con items válidos | `201`, pedido `PENDIENTE_PAGO`, detalles, subtotal, flete de Palmira (10 000.00), total y stock descontado | `PedidoControllerIntegrationTest#ped01_crearPedidoValido_retorna201ConFleteYDescuentaStock` | Verificado: T07-HU12 (#205) |
| PED-02 | Petición sin token | `401`, sin pedido ni cambio de stock | `PedidoControllerIntegrationTest#ped02_crearPedidoSinToken_retorna401` | Verificado: T07-HU12 (#205) |
| PED-03 | Token de agricultor | `403`, sin pedido ni cambio de stock | `PedidoControllerIntegrationTest#ped03_crearPedidoConTokenDeAgricultor_retorna403` | Verificado: T07-HU12 (#205) |
| PED-04 | Comprador sin perfil | `403`, sin cambios | `PedidoControllerWebTest#compradorSinPerfil_retorna403` (servicio simulado) | Verificado a nivel web |
| PED-05 | Lista `items` vacía | `400` "El pedido debe incluir al menos un producto", sin pedido | `PedidoControllerIntegrationTest#ped05_crearPedidoConListaVacia_retorna400` | Verificado: T07-HU12 (#205) |
| PED-06 | Producto inexistente | `404` "Producto no encontrado", sin reservas ni pedido | `PedidoControllerIntegrationTest#ped06_crearPedidoConProductoInexistente_retorna404` | Verificado: T07-HU12 (#205) |
| PED-07 | Producto `INACTIVO` con stock positivo | `400` "Producto no disponible", sin reservas ni pedido | `PedidoControllerIntegrationTest#ped07_crearPedidoConProductoInactivo_retorna400` | Verificado: T07-HU12 (#205) |
| PED-08 | Reserva exacta | `201` con `AGOTADO` al llegar a cero | `PedidoServiceIntegrationTest#crear_confirmaPedidoDetallesStockYActivaListener` (servicio) | Verificado a nivel de servicio |
| PED-09–PED-10 | Stock insuficiente en una o varias líneas | `400`, validación de todas las líneas antes de reservar; ninguna reserva si una falla | — | Planificado (T04-HU08, #211) |
| PED-11 | Evento de creación | Evento único tras guardar | `PedidoServiceIntegrationTest#crear_confirmaPedidoDetallesStockYActivaListener` (servicio) | Verificado a nivel de servicio |
| PED-12 | JSON y campos inválidos | `400` sin reserva | `PedidoControllerWebTest#camposInvalidos_retorna400SinServicio`, `PedidoControllerWebTest#jsonMalFormado_retorna400SinServicio` | Verificado a nivel web |

`./mvnw test -Dtest=PedidoControllerIntegrationTest` ejecuta los escenarios verificados de esta tabla.

## HU-16 — Inicio de Sesión

| Escenario BDD | Resultado esperado | Prueba real (nombre = matriz) |
| :--- | :---: | :--- |
| Credenciales válidas | `200 OK` con token JWT | `AuthLoginIntegrationTest#login_conCredencialesValidas_retorna200YToken` |
| Contraseña incorrecta | `401` con mensaje genérico | `AuthLoginIntegrationTest#login_conPasswordIncorrecta_retorna401` |
| Email inexistente | `401` con el mismo mensaje | `AuthLoginIntegrationTest#login_conEmailInexistente_retorna401` |
| Campos faltantes | `400 Bad Request` | `AuthLoginIntegrationTest#login_conCamposFaltantes_retorna400` |
| Mensaje no revela cuál dato falló (adicional) | Mismo cuerpo en ambos 401 | `AuthLoginIntegrationTest#login_emailInexistenteYPasswordIncorrecta_respondenElMismoMensaje` |
| Endpoint protegido sin token (adicional) | `401` | `AccesoProtegidoIntegrationTest#accesoProtegido_sinToken_retorna401` |
| Endpoint protegido con token válido (adicional) | Pasa la seguridad | `AccesoProtegidoIntegrationTest#accesoProtegido_conTokenValido_pasaLaSeguridad` |
| Endpoint protegido con token inválido (adicional) | `401` | `AccesoProtegidoIntegrationTest#accesoProtegido_conTokenInvalido_retorna401` |

## HU-17 — Registro de Comprador

Contrato y decisiones en [contrato-registro-hu17.md](contrato-registro-hu17.md). Las pruebas de integración están en `AuthControllerIntegrationTest`, `CompradorRegisterServiceIntegrationTest` y `AuthLoginIntegrationTest`; la fila de formato de nombre y correo se verifica a nivel de DTO en `RegistroCompradorRequestTest`.

| Escenario BDD | Resultado esperado | Prueba real |
| :--- | :---: | :--- |
| Registro válido de comprador | `201 Created`, persiste, sin password ni hash en la respuesta | `AuthControllerIntegrationTest.registrarComprador_valido_retorna201ConLosCuatroCamposPublicos`, `AuthControllerIntegrationTest.registrarComprador_valido_respuestaNoExponePasswordNiHash`, `CompradorRegisterServiceIntegrationTest.registrar_guardaUsuarioYCompradorConHashBcrypt` |
| Correo duplicado (entre compradores o con un agricultor) | `409 Conflict`, no se guarda | `AuthControllerIntegrationTest.registrarComprador_correoODocumentoDuplicado_retorna409`, `AuthControllerIntegrationTest.registrarComprador_conCorreoDeAgricultor_retorna409`, `CompradorRegisterServiceIntegrationTest.correoDeAgricultor_noPuedeUsarseParaUnComprador` |
| `tipo_comprador` inválido | `400 Bad Request`, no se guarda | `AuthControllerIntegrationTest.registrarComprador_tipoInvalido_retorna400` |
| `tipo_comprador` en mayúsculas o mixtas | `201 Created`, se guarda en MAYÚSCULAS | `AuthControllerIntegrationTest.registrarComprador_conTipoEnMayusculasOMixtas_seNormalizaYRetorna201` |
| Documento duplicado | `409 Conflict`, no se guarda | `AuthControllerIntegrationTest.registrarComprador_correoODocumentoDuplicado_retorna409`, `CompradorRegisterServiceIntegrationTest.documentoDuplicadoQueEsquivaLaVerificacion_loFrenaLaBaseYSeDeshaceTodo` |
| Documento igual a la cédula de un agricultor | `201 Created` (la unicidad es solo entre compradores) | `AuthControllerIntegrationTest.registrarComprador_conDocumentoIgualACedulaDeAgricultor_retorna201` |
| Campos faltantes | `400 Bad Request`, no se guarda | `AuthControllerIntegrationTest.registrarComprador_conCamposFaltantes_retorna400` |
| Contraseña inválida | `400 Bad Request`, no se guarda | `AuthControllerIntegrationTest.registrarComprador_conPasswordInvalida_retorna400` |
| Documento inválido (corto, largo, con letras o con dígito de verificación) | `400 Bad Request`, no se guarda | `AuthControllerIntegrationTest.registrarComprador_conDocumentoInvalido_retorna400` |
| Formato de nombre y correo (correo sin formato válido, nombre o correo de más de 50 caracteres) | `400 Bad Request`, no se guarda | `RegistroCompradorRequestTest.correoSinFormato_esRechazado`, `RegistroCompradorRequestTest.nombreMayorA50Caracteres_esRechazado`, `RegistroCompradorRequestTest.correoMayorA50Caracteres_esRechazado` |
| Rol inválido o JSON mal formado | `400 Bad Request`, no se guarda | `AuthControllerIntegrationTest.registrar_rolDesconocido_retorna400`, `AuthControllerIntegrationTest.registrar_jsonIncompleto_retornaSoloMensajePublico` |
| Atomicidad: si falla el perfil, se deshace el usuario | no queda ni `Usuario` ni `Comprador` | `CompradorRegisterServiceIntegrationTest.falloAlCrearElPerfil_deshaceTambienElUsuario` |
| Login real del comprador | `200 OK`, token con rol y perfil del comprador | `AuthLoginIntegrationTest.login_compradorRegistrado_retornaTokenConRolYPerfilDelComprador` |
| Compatibilidad HU-01 (JSON sin `rol`) | `201 Created` como AGRICULTOR | `AuthControllerIntegrationTest.registroExitoso_persisteYGuardaHashNoTextoPlano`, `AuthControllerIntegrationTest.registrarAgricultor_conRolExplicito_persistePerfilDeAgricultor` |

Los nombres de estas pruebas también están agrupados por clase en la [planificación del Sprint 3](sprint-3-planning.md#hu-17-lo-ejecutado-y-lo-pendiente), junto con el estado de las tareas de HU-17.

## Cómo verificar

HU-14 se verifica con `FincaControllerIntegrationTest`: registro válido, municipio no permitido, campos inválidos, ausencia de token y rol comprador; además cubre token inválido y municipio en minúsculas con campos opcionales ausentes. Los Given/When/Then y los nombres exactos están en la matriz del Sprint 2.

HU-17 se verifica con las pruebas de la tabla anterior: en las de integración, cada rechazo comprueba con `verificarConteos` que no se crearon ni `Usuario` ni `Comprador`; la fila de formato de nombre y correo se verifica con pruebas unitarias del DTO, que no tocan la base de datos.

`./mvnw test` ejecuta todas las pruebas listadas. Un escenario sin prueba en esta tabla es una brecha de trazabilidad.

HU-04 se verifica con `CatalogoIntegrationTest` (`./mvnw test -Dtest=CatalogoIntegrationTest`). HU-05 se verifica con `ContactoControllerIntegrationTest` (`./mvnw test -Dtest=ContactoControllerIntegrationTest`), que cubre CON-01 a CON-15 con persistencia real, y se complementa con `ContactoServiceTest` y `ContactoControllerWebTest`.
