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

## HU-04 — Catálogo y filtros (planificado)

Contrato definido en [contrato-catalogo-hu04.md](contrato-catalogo-hu04.md#4-escenarios-bdd-y-nombres-de-prueba-propuestos), asociado a T01-HU04 (#160). Los Given/When/Then completos están allí. **El endpoint GET y todas las pruebas de esta sección están pendientes de implementación**; los nombres siguientes son propuestas, no pruebas existentes ni resultados en verde.

| ID BDD | Consulta o condición | Resultado esperado | Prueba propuesta | Estado |
|---|---|---|---|---|
| CAT-01 | Consulta `?municipio=Dagua&categoria=Frutas` | `200`; solo Dagua/Frutas, con los ocho campos públicos | `consultarCatalogo_conAmbosFiltros_retornaSoloCoincidencias` | Pendiente: T05-HU04 (#164) |
| CAT-02 | Consulta ambos filtros | `200` y `[]`, sin relajar ninguno de los filtros | `consultarCatalogo_sinCoincidencias_retornaArregloVacio` | Pendiente: T05-HU04 (#164) |
| CAT-03 | Consulta sin parámetros | `200`; todas las ofertas disponibles | `consultarCatalogo_sinFiltros_retornaOfertasDisponibles` | Pendiente: T05-HU04 (#164) |
| CAT-04 | Consulta solo `?municipio=Dagua` | `200`; todas las categorías de Dagua y ningún otro municipio | `consultarCatalogo_soloMunicipio_filtraPorMunicipio` | Pendiente: T05-HU04 (#164) |
| CAT-05 | Consulta solo `?categoria=Frutas` | `200`; Frutas de todos los municipios y ninguna otra categoría | `consultarCatalogo_soloCategoria_filtraPorCategoria` | Pendiente: T05-HU04 (#164) |
| CAT-06 | Consulta `?municipio=pALmIrA&categoria=fRuTaS` | Mismo contenido que con los nombres canónicos | `consultarCatalogo_conMayusculasMixtas_ignoraMayusculas` | Pendiente: T05-HU04 (#164) |
| CAT-07 | Consulta `?municipio=%20Palmira%20&categoria=%20Frutas%20` | Mismo contenido que sin espacios periféricos | `consultarCatalogo_conEspaciosPerifericos_normalizaFiltros` | Pendiente: T05-HU04 (#164) |
| CAT-08 | Consulta filtros vacíos o solo espacios, incluidos uno vacío y otro informado | Los vacíos se omiten; el filtro informado se conserva; ambos vacíos equivalen a no filtrar | `consultarCatalogo_conFiltrosVacios_losTrataComoAusentes` | Pendiente: T05-HU04 (#164) |
| CAT-09 | Consulta municipio o categoría desconocidos, incluso junto a otro filtro válido | `200` y `[]` | `consultarCatalogo_conFiltroDesconocido_retornaArregloVacio` | Pendiente: T05-HU04 (#164) |
| CAT-10 | Consulta con y sin filtros | Solo productos `ACTIVO` con stock mayor que cero; si no queda ninguno, `[]` | `consultarCatalogo_conOfertasNoDisponibles_lasExcluye` | Pendiente: T05-HU04 (#164) |
| CAT-11 | Consulta el catálogo | IDs en orden ascendente, sin duplicados ni campos privados | `consultarCatalogo_conVariasOfertas_ordenaPorIdYRespetaContrato` | Pendiente: T05-HU04 (#164) |
| CAT-12 | Consulta el catálogo | `401`, sin ejecutar la consulta del servicio | `consultarCatalogo_sinToken_retorna401` | Pendiente: T05-HU04 (#164) |
| CAT-13 | Consulta el catálogo | `401`, sin ejecutar la consulta del servicio | `consultarCatalogo_conTokenInvalidoOExpirado_retorna401` | Pendiente: T05-HU04 (#164) |
| CAT-14 | Consulta el catálogo | `200` con las mismas reglas de selección; no recibe `403` por su rol | `consultarCatalogo_conRolAgricultor_retorna200` | Pendiente: T05-HU04 (#164) |
| CAT-15 | Consulta nombres parciales, sin la tilde requerida, o con `%`/`_` | No se amplía la coincidencia literal de nombre completo; `[]` si no existe ese nombre | `consultarCatalogo_conNombreNoExacto_noAmpliaLaBusqueda` | Pendiente: T05-HU04 (#164) |

## HU-05 — Contacto directo (planificado)

Contrato definido en [contrato-contacto-hu05.md](contrato-contacto-hu05.md#5-escenarios-bdd-y-nombres-de-prueba-propuestos), asociado a T01-HU05 (#166). Los Given/When/Then completos están allí. **El endpoint, la entidad y todas las pruebas de esta sección están pendientes de implementación**; los nombres siguientes son propuestas, no pruebas existentes ni resultados en verde. Las pruebas de DTO, servicio y controlador de T02, T03 y T04 darán cobertura parcial; T05-HU05 completa los escenarios con un comprador real.

| ID BDD | Consulta o condición | Resultado esperado | Prueba propuesta | Estado |
|---|---|---|---|---|
| CON-01 | Comprador envía `id_producto` y mensaje válidos | `200`; se guarda un mensaje con el comprador del token, el producto y la fecha del servidor | `enviarContacto_conDatosValidos_retorna200YPersiste` | Pendiente: T05-HU05 (#170) |
| CON-02 | Envía un `id_producto` que no existe | `404`; no se guarda nada | `enviarContacto_conProductoInexistente_retorna404` | Pendiente: T05-HU05 (#170) |
| CON-03 | Producto `INACTIVO` | `400`; no se guarda nada | `enviarContacto_conProductoInactivo_retorna400` | Pendiente: T05-HU05 (#170) |
| CON-04 | Producto `AGOTADO` | `400`; no se guarda nada | `enviarContacto_conProductoAgotado_retorna400` | Pendiente: T05-HU05 (#170) |
| CON-05 | Producto `ACTIVO` con stock 0 | `400`; no se guarda nada | `enviarContacto_conStockCero_retorna400` | Pendiente: T05-HU05 (#170) |
| CON-06 | `mensaje` nulo, vacío o solo espacios | `400`; no se guarda nada | `enviarContacto_conMensajeVacio_retorna400` | Pendiente: T05-HU05 (#170) |
| CON-07 | Mensaje de 2000 y de 2001 caracteres | El de 2000 responde `200`; el de 2001 responde `400` | `enviarContacto_conMensajeEnElLimite_aceptaHasta2000` | Pendiente: T05-HU05 (#170) |
| CON-08 | `id_producto` nulo, 0 o negativo | `400`; no se guarda nada | `enviarContacto_conIdProductoInvalido_retorna400` | Pendiente: T05-HU05 (#170) |
| CON-09 | Petición sin token | `401`; no se guarda nada | `enviarContacto_sinToken_retorna401` | Pendiente: T05-HU05 (#170) |
| CON-10 | Token inválido o expirado | `401`; no se guarda nada | `enviarContacto_conTokenInvalido_retorna401` | Pendiente: T05-HU05 (#170) |
| CON-11 | Agricultor autenticado | `403`; no se guarda nada | `enviarContacto_conRolAgricultor_retorna403` | Pendiente: T05-HU05 (#170) |
| CON-12 | Cuerpo con `comprador_id` de otro comprador | `200`; el mensaje queda a nombre del comprador del token | `enviarContacto_conCompradorIdEnElCuerpo_loIgnora` | Pendiente: T05-HU05 (#170) |
| CON-13 | Token de rol `COMPRADOR` sin perfil de comprador | `403` con mensaje genérico; no se guarda nada | `enviarContacto_conCompradorSinPerfil_retorna403` | Pendiente: T03-HU05 (#168) |
| CON-14 | Mismo mensaje enviado dos veces | Dos `200` y dos registros distintos | `enviarContacto_conMensajeRepetido_guardaDosRegistros` | Pendiente: T05-HU05 (#170) |
| CON-15 | JSON mal formado o `id_producto` no numérico | `400` con el mapa `{"json": ...}`; no se guarda nada | `enviarContacto_conJsonMalFormado_retorna400` | Pendiente: T04-HU05 (#169) |

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

Contrato y decisiones en [contrato-registro-hu17.md](contrato-registro-hu17.md). Las pruebas se escribirán en las tareas de implementación de HU-17.

| Escenario BDD | Resultado esperado | Prueba real |
| :--- | :---: | :--- |
| Registro válido de comprador | `201 Created`, persiste, sin password ni hash en la respuesta | pendiente (implementación HU-17) |
| Correo duplicado | `409 Conflict`, no se guarda | pendiente (implementación HU-17) |
| `tipo_comprador` inválido | `400 Bad Request`, no se guarda | pendiente (implementación HU-17) |
| Documento duplicado | `409 Conflict`, no se guarda | pendiente (implementación HU-17) |
| Campos faltantes, documento o rol inválidos | `400 Bad Request`, no se guarda | pendiente (implementación HU-17) |
| Compatibilidad HU-01 (JSON sin `rol`) | `201 Created` como AGRICULTOR | pendiente (implementación HU-17) |

## Cómo verificar

HU-14 se verifica con `FincaControllerIntegrationTest`: registro válido, municipio no permitido, campos inválidos, ausencia de token y rol comprador; además cubre token inválido y municipio en minúsculas con campos opcionales ausentes. Los Given/When/Then y los nombres exactos están en la matriz del Sprint 2.

`./mvnw test` ejecuta todas las pruebas listadas. Un escenario sin prueba en esta tabla es una brecha de trazabilidad.

Las filas planificadas de HU-04 y HU-05 todavía no se ejecutan con Maven. T05-HU04 y T05-HU05 deberán reemplazar los nombres propuestos por referencias a pruebas reales y registrar su verificación.
