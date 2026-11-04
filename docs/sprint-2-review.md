# Sprint 2 Review · AgroValle Connect

**Corte técnico:** 3 de noviembre de 2026. Esta revisión documental se basa en las integraciones en GitHub. La fecha y los asistentes de la sesión, así como el feedback recibido, quedan por registrar con el equipo.

El objetivo fue habilitar el registro de fincas y la publicación de cosechas para agricultores autenticados. El recorrido se apoya en el registro HU-01 y el inicio de sesión HU-16.

La revisión corresponde a la tarea [DOC #134](https://github.com/Angel-uniajc/AgrovalleConnect/issues/134). La línea base funcional es `develop` en `b1090a05439ad5152087f47dfb1967b1671d585e`, que incorpora el PR #132. El incremento funcional se encuentra integrado en esa rama.

| Entrega | Recorrido verificable | Evidencia |
|---|---|---|
| HU-14 · Registro de fincas | Un agricultor autenticado registra una finca en un municipio permitido. La API devuelve `201` y asocia la finca con el agricultor del token. La suite cubre entradas inválidas y restricciones de acceso. | [Endpoint #120](https://github.com/Angel-uniajc/AgrovalleConnect/pull/120), [integración #121](https://github.com/Angel-uniajc/AgrovalleConnect/pull/121) |
| HU-02 · Publicación de cosechas | El agricultor publica en una finca propia con categoría válida y obtiene un identificador. Los siete escenarios verifican persistencia y rechazos por validación, autenticación y propiedad. | [Endpoint #131](https://github.com/Angel-uniajc/AgrovalleConnect/pull/131), [integración #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132) |
| Calidad y diseño | El servicio emite un evento de cosecha tras guardar el producto. Un listener independiente lo registra. El controlador de autenticación queda desacoplado de las entidades y CI incorpora la verificación de cobertura. | [Observer #130](https://github.com/Angel-uniajc/AgrovalleConnect/pull/130), [refactor #129](https://github.com/Angel-uniajc/AgrovalleConnect/pull/129), [JaCoCo #124](https://github.com/Angel-uniajc/AgrovalleConnect/pull/124) |

## Guion propuesto para la demostración

1. Registrar un agricultor e iniciar sesión para obtener un JWT.
2. Crear una finca con `POST /api/v1/fincas` y conservar su ID.
3. Publicar una cosecha en esa finca con `POST /api/v1/productos`. Mostrar el `201`, el ID y el registro del evento.
4. Repetir con una finca ajena y mostrar el `403`. Mostrar la prueba que comprueba que el rechazo no guarda productos.
5. Abrir la [matriz de planificación y trazabilidad](sprint-2-planning.md) y el CI del [PR #132](https://github.com/Angel-uniajc/AgrovalleConnect/pull/132).

La verificación local de la revisión del PR #132 ejecutó 112 pruebas sin fallos con Java 25 y H2. Se observaron 98,84 % de instrucciones y 90 % de ramas. La evidencia institucional corresponde al commit final de `main` y se debe adjuntar después de su integración.

## Resultados técnicos y límites de la evidencia

El caso válido de publicación verifica cantidad, precio, fecha, estado `ACTIVO`, finca y categoría después de vaciar el contexto JPA. Los seis rechazos comprueban que la cantidad de productos permanece igual. La prueba de finca ajena comprueba el mensaje de propiedad además del código `403`. La [matriz BDD del Sprint 2](sprint-2-planning.md) enlaza los escenarios con sus métodos JUnit.

La verificación local se ejecutó con `clean verify`, que incluye Checkstyle, pruebas y JaCoCo. El CI del PR #132 terminó satisfactoriamente. El ensayo local usa H2. La compatibilidad específica de PostgreSQL requiere evidencia en ese motor.

El Observer se ejecuta de forma síncrona dentro de la transacción. Una excepción del listener puede revertir la publicación. La arquitectura documenta el uso de observadores posteriores al commit para futuros efectos externos.

## Seguimiento del cierre

El servicio de cosechas se integró el 30 de octubre y las últimas pruebas se integraron el 3 de noviembre. La [bitácora](bitacora-daily-scrum.md) permite complementar esta secuencia con los impedimentos que el equipo confirme. La disponibilidad limitada queda como explicación pendiente de validación.

Para la entrega institucional se requiere consolidar el incremento en `main`, comprobar las reglas de protección y adjuntar capturas de CI y cobertura de su último commit. El equipo debe contrastar el umbral configurado de instrucciones con la exigencia adicional de ramas de la guía del segundo corte.

**Registro de la sesión pendiente:** el equipo debe anotar la fecha, los asistentes, los escenarios que efectivamente mostró, el feedback textual o resumido y los cambios de backlog derivados de ese feedback.
