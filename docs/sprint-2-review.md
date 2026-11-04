# Sprint 2 Review · AgroValle Connect

**Fecha de la sesión y corte técnico:** 3 de noviembre de 2026. El representante confirmó la fecha y la participación de todos los integrantes. Esta revisión documental recoge las entregas, las comprobaciones automatizadas y los comentarios técnicos registrados en los PR del sprint.

El objetivo fue habilitar el registro de fincas y la publicación de cosechas para agricultores autenticados. El recorrido se apoya en el registro HU-01 y el inicio de sesión HU-16.

La revisión corresponde a la tarea [DOC #134](https://github.com/Angel-uniajc/AgrovalleConnect/issues/134). La línea base funcional es `develop` en `b1090a05439ad5152087f47dfb1967b1671d585e`, que incorpora el PR #132. El incremento funcional se encuentra integrado en esa rama.

## Fechas verificadas de la documentación

Las horas corresponden a Colombia (UTC−5). La tarea #134 se creó el 3 de noviembre de 2026 a las 21:43 y se agregó al tablero a las 21:43. Su historial registra otro cambio de estado a las 21:44. El PR #138 con esta revisión se abrió el mismo día a las 21:48. La elaboración documental y la sesión de revisión corresponden al 3 de noviembre de 2026. La fecha de la sesión fue confirmada por el representante.

El cierre funcional de HU-02 quedó registrado el 3 de noviembre a las 03:21, después del merge del PR #132. Esta secuencia permite distinguir el cierre de la funcionalidad y la elaboración posterior de la revisión.

| Entrega | Recorrido verificable | Evidencia |
|---|---|---|
| HU-14 · Registro de fincas | Un agricultor autenticado registra una finca en un municipio permitido. La API devuelve `201` y asocia la finca con el agricultor del token. La suite cubre entradas inválidas y restricciones de acceso. | [Endpoint #120](https://github.com/Angel-uniajc/AgrovalleConnect/pull/120), [integración #121](https://github.com/Angel-uniajc/AgrovalleConnect/pull/121) |
| HU-02 · Publicación de cosechas | El agricultor publica en una finca propia con categoría válida y obtiene un identificador. La suite cubre los siete escenarios exigidos y un caso adicional de rol COMPRADOR. | [Endpoint #131](https://github.com/Angel-uniajc/AgrovalleConnect/pull/131), [integración #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) |
| Calidad y diseño | El servicio emite un evento de cosecha tras guardar el producto. Un listener independiente lo registra. El controlador de autenticación queda desacoplado de las entidades y CI incorpora la verificación de cobertura. | [Observer #130](https://github.com/Angel-uniajc/AgrovalleConnect/pull/130), [refactor #129](https://github.com/Angel-uniajc/AgrovalleConnect/pull/129), [JaCoCo #124](https://github.com/Angel-uniajc/AgrovalleConnect/pull/124) |

## Recorrido funcional respaldado por las pruebas

1. Las pruebas registran al agricultor y obtienen un JWT mediante el inicio de sesión real.
2. El agricultor crea una finca con `POST /api/v1/fincas` y recibe su identificador.
3. El agricultor publica una cosecha en esa finca con `POST /api/v1/productos` y recibe `201` con el ID. El servicio emite el evento de publicación.
4. Los intentos de publicar en una finca ajena o con rol COMPRADOR reciben `403`. La suite comprueba que los rechazos conservan el número de productos.
5. La [matriz de planificación y trazabilidad](sprint-2-planning.md) relaciona estos resultados con los métodos JUnit. El [PR #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) conserva la evidencia de revisión e integración.

La primera verificación local del PR #132 ejecutó 112 pruebas sin fallos con Java 25 y H2. En esa versión se observaron 98,84 % de instrucciones y 90 % de ramas. Después de esa revisión, el autor incorporó el caso COMPRADOR. Las cifras locales corresponden a la versión anterior a esa mejora. La evidencia institucional corresponde al commit final de `main` y se debe adjuntar después de su integración.

## Resultados técnicos y límites de la evidencia

El caso válido de publicación verifica cantidad, precio, fecha, estado `ACTIVO`, finca y categoría después de vaciar el contexto JPA. Los siete rechazos, incluido el rol COMPRADOR adicional, comprueban que la cantidad de productos permanece igual. La prueba de finca ajena comprueba el mensaje de propiedad además del código `403`. La [matriz BDD del Sprint 2](sprint-2-planning.md) enlaza los escenarios con sus métodos JUnit.

La verificación local se ejecutó con `clean verify`, que incluye Checkstyle, pruebas y JaCoCo. El CI del PR #132 terminó satisfactoriamente. El ensayo local usa H2. La compatibilidad específica de PostgreSQL requiere evidencia en ese motor.

El Observer se ejecuta de forma síncrona dentro de la transacción. Una excepción del listener puede revertir la publicación. La arquitectura documenta el uso de observadores posteriores al commit para futuros efectos externos.

## Seguimiento del cierre

El servicio de cosechas se integró el 30 de octubre y las últimas pruebas se integraron el 3 de noviembre. La [bitácora](bitacora-daily-scrum.md) permite complementar esta secuencia con los impedimentos que el equipo confirme. La disponibilidad limitada queda como explicación pendiente de validación.

Para la entrega institucional se requiere consolidar el incremento en `main`, comprobar las reglas de protección y adjuntar capturas de CI y cobertura de su último commit. El equipo debe contrastar el umbral configurado de instrucciones con la exigencia adicional de ramas de la guía del segundo corte.

## Feedback técnico y respuesta del incremento

Los siguientes hallazgos proceden de las revisiones de código registradas en GitHub. La síntesis conecta cada comentario con el cambio incorporado.

| Hallazgo de revisión | Respuesta incorporada | Evidencia |
|---|---|---|
| La prueba del evento dependía de los municipios y categorías creados al iniciar la aplicación. | La prueba prepara sus propios datos de municipio y categoría. | [Revisión de Observer #130](https://github.com/Angel-uniajc/AgrovalleConnect/pull/130) |
| El listener síncrono puede propagar una excepción y revertir la publicación. La tabla de capas y el árbol de paquetes requerían reflejar el evento y el listener. | La documentación explica el riesgo transaccional, el uso futuro de `AFTER_COMMIT` para efectos externos y los paquetes implementados. | [Revisión de Observer #130](https://github.com/Angel-uniajc/AgrovalleConnect/pull/130) |
| El rechazo por finca ajena debía complementarse con un rechazo por rol COMPRADOR. | Se agregó la prueba del comprador autenticado y su fila en la matriz BDD. | [Revisión de integración #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) |
| Repetir `LocalDate.now()` y depender del municipio inicial podía volver frágiles las pruebas. | La suite de productos conserva la fecha de cada prueba en una variable y prepara su propio municipio. | [Revisión de integración #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) |

## Acciones de seguimiento propuestas

La revisión técnica respalda el cumplimiento funcional de HU-14 y HU-02. Para preparar la entrega institucional se proponen las siguientes acciones, cuyos responsables se asignarán en el tablero.

- Consolidar los documentos del segundo corte y vincularlos a sus tareas de documentación.
- Verificar el mínimo del 60 % de instrucciones y ramas, las reglas de protección y el CI de la versión entregada en `main`.
- Adjuntar las capturas de ejecución y cobertura del commit final y conservar el vínculo con las historias y pruebas.

Estas acciones se derivan de la revisión técnica y los requisitos del entregable. Su inclusión permite al equipo planificar el cierre en el Kanban.
