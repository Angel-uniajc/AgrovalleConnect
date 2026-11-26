# Lista de chequeo de Clean Code · AgroValle Connect

**Fecha de la revisión:** 26 de noviembre de 2026 (sesión 15).
**Código revisado:** `develop` en `a4879eb`, igual al contenido de la release `0.3.0` en `main` (PR [#228](https://github.com/Angel-uniajc/AgrovalleConnect/pull/228)). Son 86 clases en `src/main/java` y 49 clases de prueba en `src/test/java`.
**Cómo se revisó:** lectura del código, el resultado de Checkstyle y del CI, y un recorrido automático de todos los métodos de `src/main/java`, que contó las líneas de cada método, los parámetros, los bloques `catch` vacíos y los `System.out`.

## 1. Checklist de Code Review (sesión 15)

| # | Criterio | Resultado | Evidencia |
|---|---|---|---|
| 1 | 0 errores de Checkstyle | ✅ Cumple | `checkstyle:check` corre en Husky y en el CI. El último reporte local (`target/checkstyle-result.xml`) no tiene violaciones. Ver la observación O-1. |
| 2 | Métodos concisos (menos de 20 líneas) | ✅ Cumple | Ningún método de `src/main/java` pasa de 20 líneas de código. El servicio más grande, `PedidoService`, tiene 104 líneas no vacías y reparte el trabajo en `crear`, `agruparCantidades`, `validarProductos`, `reservar` y `respuesta`. |
| 3 | Nombres expresivos en clases, métodos y variables | ✅ Cumple | Nombres en español del dominio (`CalculadoraFlete`, `StockInsuficienteException`, `buscarAgricultor`). Las pruebas siguen el formato `accion_condicion_resultadoEsperado` o el código del escenario (`stk09_productoRepetidoSuperaStock`). |
| 4 | Pruebas JUnit 5 y Mockito en verde | ✅ Cumple | 381 pruebas sin fallos en la validación de la release. JaCoCo exige 60 % de instrucciones y de ramas, y el build falla si no se cumple. |
| 5 | Manejo explícito de excepciones, sin `catch` vacíos | ✅ Cumple | No hay bloques `catch` vacíos ni `printStackTrace`. Los errores de negocio son excepciones propias que `ManejadorErrores` traduce a `400`, `401`, `403`, `404` o `409`. Ver el smell S-2. |

## 2. Detección de code smells

| Smell | Resultado | Detalle |
|---|---|---|
| Clase Dios (viola SRP) | No se encontró | Cada capa tiene su responsabilidad: los controladores solo reciben la petición y sacan el correo del token; los servicios orquestan; los mappers traducen DTO y entidades; las excepciones y el manejador separan los errores. Ninguna clase de `src/main/java` pasa de 104 líneas no vacías. |
| Envidia de funciones | No se encontró | Las reglas del flete viven en las estrategias (`FleteDagua`, `FletePalmira`, `FleteBuga`) y no en `PedidoService`. |
| Lista larga de parámetros | No se encontró | Ningún método de negocio recibe más de tres parámetros. Los endpoints reciben DTO (`CrearPedidoRequest`, `RegistroRequest`) en lugar de datos sueltos. El constructor de `PedidoService` recibe cinco dependencias, que Spring inyecta; si crece más, sería señal de que el servicio hace demasiado. |
| Comentarios redundantes | Leve | Hay 73 líneas de comentario en `src/main/java`. La mayoría explica el porqué (por ejemplo, "Sin cascade: borrar un detalle nunca borra el producto"). Algunos repiten el nombre del método o la HU y se pueden quitar. |
| Código duplicado (DRY) | Presente | Ver S-1. |

## 3. Los tres code smells principales y su refactorización

| ID | Smell | Dónde | Por qué importa (ISO/IEC 25010) | Refactorización propuesta |
|---|---|---|---|---|
| S-1 | Duplicación y dos formatos de respuesta | `ManejadorErrores`: siete métodos repiten `ResponseEntity.status(X).body(ex.getMessage())` con texto plano, mientras que los tres errores del Sprint 4 responden JSON (`{"error": ...}`). | **Mantenibilidad** (cada error nuevo copia el mismo código) y **usabilidad de la API** (el cliente tiene que leer dos formatos). | Crear un `record ErrorResponse(String error)` y un método privado que arme la respuesta, y responder todos los errores en JSON. Es un cambio de contrato, así que va en su propia tarea, con pruebas que actualicen los cuerpos esperados. |
| S-2 | Excepción genérica que termina en `500` | `FincaService.buscarAgricultor` lanza `IllegalStateException` cuando el correo del token no tiene perfil de agricultor. | **Fiabilidad**: la rúbrica pide la demo sin errores `500`. | Reemplazarla por `AccesoDenegadoException` (`403`), igual que en `PedidoService` cuando el comprador no tiene perfil. |
| S-3 | Regla de concurrencia ausente | `PedidoService.reservar` descuenta `cantidad_disponible` sin `@Version` ni bloqueo en `Producto`. | **Fiabilidad**: dos compradores a la vez pueden reservar las mismas unidades. | Agregar `@Version` a `Producto` (bloqueo optimista) y responder `409` si la versión cambió, o leer el producto con `@Lock(PESSIMISTIC_WRITE)` en el repositorio. Va como issue aparte (MEJ-15). |

## 4. Observaciones sobre la configuración

- **O-1. Checkstyle no tiene Google Java Style completo.** El `checkstyle.xml` del proyecto aplica siete reglas: largo de línea máximo de 120, sin imports con `*`, línea en blanco entre bloques, y nombres de variables, métodos, parámetros y tipos, además de la visibilidad de los campos. El checklist de la sesión 15 pide Google Java Style completo, que además revisa la sangría, las llaves, el orden de los imports y el Javadoc. Pasar a `google_checks.xml` generaría muchas violaciones nuevas, así que conviene hacerlo como tarea técnica y no en la víspera de la sustentación.
- **O-2. Checkstyle no detecta imports sin usar.** Ya pasó en el PR #128. Se puede agregar la regla `UnusedImports`.
- **O-3. Inyección por constructor.** No hay ningún `@Autowired` en campos: todas las dependencias entran por el constructor (DIP).

## 5. Uso en las revisiones

Desde esta fecha, cada revisor marca en el PR los cinco criterios de la sección 1 antes de aprobar. Las fallas que encuentre en otro repositorio durante la auditoría cruzada se registran como issue con la plantilla [`bug_report`](../.github/ISSUE_TEMPLATE/bug_report.md).
