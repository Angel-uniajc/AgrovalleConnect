# Contrato del catálogo de cosechas — HU-04

Documento de T01-HU04 ([#160](https://github.com/Angel-uniajc/AgrovalleConnect/issues/160)), parte de [HU-04 (#18)](https://github.com/Angel-uniajc/AgrovalleConnect/issues/18). Define el comportamiento que implementarán T02 a T05 de HU-04. **El GET y sus pruebas todavía están pendientes**: este contrato no afirma que ya estén disponibles en `develop`.

## 1. Operación y acceso

`GET /api/v1/productos` consulta ofertas disponibles. No recibe cuerpo JSON. Requiere `Authorization: Bearer <token>` válido, igual que las demás rutas autenticadas del proyecto.

- El comprador autenticado puede consultar el catálogo.
- El agricultor autenticado también puede consultarlo. Se conserva la política de `SecurityConfig.anyRequest().authenticated()`; HU-04 no introduce una restricción exclusiva de comprador ni acceso público.
- Sin token, con token inválido o expirado: `401 Unauthorized`, antes de consultar el catálogo. No se exige un cuerpo de error concreto para este rechazo de Spring Security.
- `POST /api/v1/productos` sigue siendo la publicación de HU-02, exclusiva de `AGRICULTOR`, con respuesta `201`. Esta tarea no cambia ese contrato.

## 2. Filtros y selección de ofertas

| Parámetro de consulta | Tipo | Obligatorio | Significado |
|---|---|---|---|
| `municipio` | Texto | No | Nombre del municipio de la finca del producto |
| `categoria` | Texto | No | Nombre de la categoría del producto |

Reglas que deberá cumplir la implementación:

1. Incluir únicamente productos con `estado = ACTIVO` **y** `cantidad_disponible > 0`. Excluir `INACTIVO`, `AGOTADO` y stock cero, incluso si el estado figura como `ACTIVO`.
2. Si se envían ambos filtros, aplicar **AND**: la misma oferta debe coincidir con municipio y categoría. Cada filtro también funciona por separado.
3. Quitar los espacios al inicio y al final del valor recibido y comparar el nombre completo sin distinguir mayúsculas. Por ejemplo, `palmira` y `Palmira` coinciden. No eliminar espacios internos ni tildes: `Tuberculos` no equivale a `Tubérculos` por este contrato.
4. Tratar un parámetro omitido, vacío (`municipio=`) o compuesto solo de espacios como filtro ausente. Sin filtros, devolver todas las ofertas disponibles según la regla 1.
5. Si un nombre no existe en su catálogo, o si los filtros no tienen ofertas en común, devolver `200 OK` con `[]`. Un filtro desconocido no debe ignorarse ni convertirse en `400` o `404`.
6. Los valores son nombres completos, no IDs ni búsquedas parciales. `%` y `_` no tienen significado de comodín: no pueden ampliar una consulta.
7. Ordenar por `Producto.id` ascendente. La respuesta es un arreglo sin envoltorio de paginación; HU-04 no incorpora parámetros de página, tamaño u orden personalizados.
8. No filtrar por fecha de cosecha: una fecha pasada no altera por sí sola el estado o el stock. HU-02 valida la fecha al publicar; HU-04 consulta el estado y la disponibilidad actuales.

Los municipios iniciales de `CargadorMunicipios` son Dagua, Palmira y Buga. Las categorías iniciales de `CargadorCategorias` son Frutas, Hortalizas, Granos y Tubérculos. Se consultan las relaciones persistidas `Producto → Finca → Municipio` y `Producto → Categoria`, sin listas de ofertas codificadas en el controlador.

## 3. Respuesta pública

Una consulta autorizada responde `200 OK`, `Content-Type: application/json`, con cero o más objetos del siguiente contrato. Todos los campos de cada oferta son obligatorios y no nulos:

| Campo JSON | Tipo JSON | Origen y regla |
|---|---|---|
| `id` | Entero | Identificador de `Producto` |
| `tipo` | Texto | Tipo o descripción publicada, hasta 80 caracteres |
| `cantidad_disponible` | Número decimal | Stock actual, mayor que cero; conservar precisión decimal |
| `precio_unitario` | Número decimal | Precio publicado, mayor que cero; conservar precisión decimal |
| `fecha_cosecha` | Texto | Fecha ISO `YYYY-MM-DD` de la cosecha |
| `categoria` | Texto | Nombre de `Categoria`, conservando la escritura del catálogo |
| `finca_id` | Entero | Identificador de la finca asociada |
| `municipio` | Texto | Nombre de `Municipio`, conservando la escritura del catálogo |

Ejemplo ilustrativo con datos ficticios; los IDs y valores dependen de los registros de la base:

```http
GET /api/v1/productos?municipio=Dagua&categoria=Frutas
Authorization: Bearer <token_valido>
```

```json
[
  {
    "id": 12,
    "tipo": "Mango",
    "cantidad_disponible": 25.50,
    "precio_unitario": 3200.00,
    "fecha_cosecha": "2026-11-20",
    "categoria": "Frutas",
    "finca_id": 4,
    "municipio": "Dagua"
  }
]
```

Los decimales se transmiten como números JSON, no como textos; el número de ceros finales no es parte del contrato. La implementación usará `BigDecimal`, de acuerdo con `Producto`.

Sin coincidencias, o sin ofertas disponibles, el cuerpo es exactamente un arreglo vacío:

```json
[]
```

No se devuelven entidades JPA, objetos de usuario/agricultor anidados, correo, documento, contraseña, hash ni datos de contacto. La identificación del producto permite continuar posteriormente con HU-05.

## 4. Escenarios BDD y nombres de prueba propuestos

Cada fila define un escenario Given/When/Then. Las pruebas son **propuestas pendientes** para T05-HU04 ([#164](https://github.com/Angel-uniajc/AgrovalleConnect/issues/164)), no evidencia de ejecución. Los datos de prueba deben incluir ofertas que difieran en estado, stock, municipio y categoría, para detectar filtros omitidos o combinados incorrectamente.

| ID | Given (Dado) | When (Cuando) | Then (Entonces) | Prueba propuesta |
|---|---|---|---|---|
| CAT-01 | Comprador autenticado; ofertas disponibles en Dagua/Frutas, Dagua/Granos y Palmira/Frutas | Consulta `?municipio=Dagua&categoria=Frutas` | `200`; solo Dagua/Frutas, con los ocho campos públicos | `consultarCatalogo_conAmbosFiltros_retornaSoloCoincidencias` |
| CAT-02 | Usuario autenticado; no hay ofertas que cumplan ambos filtros | Consulta ambos filtros | `200` y `[]`, sin relajar ninguno de los filtros | `consultarCatalogo_sinCoincidencias_retornaArregloVacio` |
| CAT-03 | Usuario autenticado; ofertas disponibles en distintos municipios y categorías | Consulta sin parámetros | `200`; todas las ofertas disponibles | `consultarCatalogo_sinFiltros_retornaOfertasDisponibles` |
| CAT-04 | Usuario autenticado; ofertas disponibles de varias categorías en Dagua y de otros municipios | Consulta solo `?municipio=Dagua` | `200`; todas las categorías de Dagua y ningún otro municipio | `consultarCatalogo_soloMunicipio_filtraPorMunicipio` |
| CAT-05 | Usuario autenticado; ofertas disponibles de Frutas en distintos municipios y de otras categorías | Consulta solo `?categoria=Frutas` | `200`; Frutas de todos los municipios y ninguna otra categoría | `consultarCatalogo_soloCategoria_filtraPorCategoria` |
| CAT-06 | Usuario autenticado; catálogo con Palmira/Frutas | Consulta `?municipio=pALmIrA&categoria=fRuTaS` | Mismo contenido que con los nombres canónicos | `consultarCatalogo_conMayusculasMixtas_ignoraMayusculas` |
| CAT-07 | Usuario autenticado; catálogo con Palmira/Frutas | Consulta `?municipio=%20Palmira%20&categoria=%20Frutas%20` | Mismo contenido que sin espacios periféricos | `consultarCatalogo_conEspaciosPerifericos_normalizaFiltros` |
| CAT-08 | Usuario autenticado; ofertas disponibles | Consulta filtros vacíos o solo espacios, incluidos uno vacío y otro informado | Los vacíos se omiten; el filtro informado se conserva; ambos vacíos equivalen a no filtrar | `consultarCatalogo_conFiltrosVacios_losTrataComoAusentes` |
| CAT-09 | Usuario autenticado; el nombre enviado no existe en el catálogo | Consulta municipio o categoría desconocidos, incluso junto a otro filtro válido | `200` y `[]` | `consultarCatalogo_conFiltroDesconocido_retornaArregloVacio` |
| CAT-10 | Usuario autenticado; productos `ACTIVO` con stock positivo/cero y productos `INACTIVO`/`AGOTADO` | Consulta con y sin filtros | Solo productos `ACTIVO` con stock mayor que cero; si no queda ninguno, `[]` | `consultarCatalogo_conOfertasNoDisponibles_lasExcluye` |
| CAT-11 | Usuario autenticado; varias ofertas disponibles creadas en la base | Consulta el catálogo | IDs en orden ascendente, sin duplicados ni campos privados | `consultarCatalogo_conVariasOfertas_ordenaPorIdYRespetaContrato` |
| CAT-12 | Petición sin token | Consulta el catálogo | `401`, sin ejecutar la consulta del servicio | `consultarCatalogo_sinToken_retorna401` |
| CAT-13 | Petición con token inválido o expirado | Consulta el catálogo | `401`, sin ejecutar la consulta del servicio | `consultarCatalogo_conTokenInvalidoOExpirado_retorna401` |
| CAT-14 | Agricultor autenticado con token válido | Consulta el catálogo | `200` con las mismas reglas de selección; no recibe `403` por su rol | `consultarCatalogo_conRolAgricultor_retorna200` |
| CAT-15 | Usuario autenticado; ofertas disponibles en categorías con tilde y en otros nombres | Consulta nombres parciales, sin la tilde requerida, o con `%`/`_` | No se amplía la coincidencia literal de nombre completo; `[]` si no existe ese nombre | `consultarCatalogo_conNombreNoExacto_noAmpliaLaBusqueda` |

En todas las consultas el stock y el número de productos permanecen iguales: no se crean pedidos, pagos ni mensajes de contacto, y no se emite `CosechaPublicadaEvent` por consultar.

## 5. Entrega por tareas y verificación

- **T01-HU04 (#160):** este contrato, criterios de HU-04 en [BACKLOG.md](../BACKLOG.md), matriz de [trazabilidad BDD](trazabilidad-bdd.md#hu-04--catálogo-y-filtros) y actualización de la historia del Kanban.
- **T02-HU04 (#161):** consulta de repositorio con estado, stock, filtros, orden estable y carga de las relaciones necesarias; verificar con datos propios.
- **T03-HU04 (#162):** DTO público y servicio de lectura con normalización y mapeo; no devolver entidades JPA.
- **T04-HU04 (#163):** añadir GET al controlador y conservar la seguridad y el POST existentes.
- **T05-HU04 (#164):** implementar los escenarios anteriores con comprador registrado y autenticado mediante HU-17; completar la matriz con pruebas reales y verificar el CI.
- **T06-HU04 (#165):** documentar el endpoint implementado y su diagrama de secuencia, ejemplos y demostración.

El contrato no requiere una tabla nueva ni cambios en `schema.sql`. No se incluyen frontend, paginación, precios regionales, pedidos, pagos, reserva de stock o envío de contacto. Los consumidores no deben interpretar este documento como evidencia de que el GET esté implementado.

## 6. Fuentes contrastadas en develop

Línea base: `92ee844`, posterior a los diagramas de secuencia y colaboración de HU-02.

- [Producto](../src/main/java/com/agrovalleconnect/agrovalle_connect/model/Producto.java): campos públicos, estados y relaciones.
- [Controlador de productos](../src/main/java/com/agrovalleconnect/agrovalle_connect/controller/ProductoController.java): en la línea base solo implementa POST.
- [SecurityConfig](../src/main/java/com/agrovalleconnect/agrovalle_connect/config/SecurityConfig.java) y [filtro JWT](../src/main/java/com/agrovalleconnect/agrovalle_connect/security/JwtAuthenticationFilter.java): autenticación y conservación de reglas de acceso.
- [Municipios iniciales](../src/main/java/com/agrovalleconnect/agrovalle_connect/config/CargadorMunicipios.java) y [categorías iniciales](../src/main/java/com/agrovalleconnect/agrovalle_connect/config/CargadorCategorias.java): nombres del catálogo.
- [Esquema SQL](diseno/schema.sql): tablas existentes para la consulta.
