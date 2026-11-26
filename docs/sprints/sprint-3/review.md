# Sprint 3 Review · AgroValle Connect

**Periodo del sprint:** del 17 al 24 de noviembre de 2026.
**Fecha de elaboración:** 26 de noviembre de 2026.
**Línea base revisada:** `develop` en `0233ff4` (cierre funcional del 24/11). El incremento llegó a `main` el 26 de noviembre con el PR [#228](https://github.com/Angel-uniajc/AgrovalleConnect/pull/228).
**Base de la revisión:** [planificación](planning.md), tarjetas del Kanban, PR fusionados y sus revisiones.

## Sprint Goal y resultado

> Permitir que un comprador se registre e inicie sesión, encuentre cosechas disponibles por municipio y categoría y contacte al agricultor con un mensaje que quede asociado a la oferta.

El objetivo se cumplió. Las tres historias comprometidas y sus 20 tareas quedaron cerradas el 24 de noviembre.

| Historia | Story Points | Tareas | Cierre | Estado |
|---|---:|---:|---|---|
| [HU-17](https://github.com/Angel-uniajc/AgrovalleConnect/issues/44) · Registro de comprador | 5 | 8 | 24/11, 17:00 | Hecha |
| [HU-04](https://github.com/Angel-uniajc/AgrovalleConnect/issues/18) · Filtro por categoría y municipio | 3 | 6 | 24/11, 15:55 | Hecha |
| [HU-05](https://github.com/Angel-uniajc/AgrovalleConnect/issues/20) · Contacto directo | 3 | 6 | 24/11, 17:31 | Hecha |
| **Total** | **11 de 11** | **20 de 20** | | |

Las horas son hora de Colombia. Velocidad del sprint: **11 SP completados de 11 comprometidos**.

## Incremento entregado

| Entrega | Qué puede hacer el usuario | Evidencia |
|---|---|---|
| HU-17 | Un comprador se registra en `POST /api/v1/auth/register` con `rol` igual a `COMPRADOR`. El usuario y el perfil se guardan en una sola transacción, y la API responde `201`, `400` o `409`. Al iniciar sesión recibe un JWT con su rol y su `perfil_id`. | [Contrato](../../contrato-registro-hu17.md), [#178](https://github.com/Angel-uniajc/AgrovalleConnect/pull/178), [#180](https://github.com/Angel-uniajc/AgrovalleConnect/pull/180), [#187](https://github.com/Angel-uniajc/AgrovalleConnect/pull/187), [#191](https://github.com/Angel-uniajc/AgrovalleConnect/pull/191) |
| HU-04 | Cualquier usuario autenticado consulta `GET /api/v1/productos` con los filtros opcionales `municipio` y `categoria`. La consulta devuelve solo ofertas activas con stock, ordenadas por ID. | [Contrato](../../contrato-catalogo-hu04.md), [#177](https://github.com/Angel-uniajc/AgrovalleConnect/pull/177), [#189](https://github.com/Angel-uniajc/AgrovalleConnect/pull/189), [#196](https://github.com/Angel-uniajc/AgrovalleConnect/pull/196), [registro HTTP](../../evidencias/sprint3-catalogo-http.json) |
| HU-05 | Un comprador envía un mensaje sobre una oferta con `POST /api/v1/contacto/mensaje`. El mensaje se guarda asociado al producto, sin crear pedidos ni tocar el stock. | [Contrato](../../contrato-contacto-hu05.md), [#192](https://github.com/Angel-uniajc/AgrovalleConnect/pull/192), [#195](https://github.com/Angel-uniajc/AgrovalleConnect/pull/195), [#209](https://github.com/Angel-uniajc/AgrovalleConnect/pull/209), [recorrido](../../evidencias/sprint3-contacto.md) |
| Diseño | Cada historia trae su diagrama de secuencia. Además se agregaron la vista de componentes consolidada, dos diagramas de colaboración y el despliegue de staging con el ADR-001 (tarjeta [#182](https://github.com/Angel-uniajc/AgrovalleConnect/issues/182)). | [#188](https://github.com/Angel-uniajc/AgrovalleConnect/pull/188), [#190](https://github.com/Angel-uniajc/AgrovalleConnect/pull/190), [#193](https://github.com/Angel-uniajc/AgrovalleConnect/pull/193), [#194](https://github.com/Angel-uniajc/AgrovalleConnect/pull/194), [#198](https://github.com/Angel-uniajc/AgrovalleConnect/pull/198) |

El recorrido de demostración (registrar agricultor, finca y cosecha, registrar comprador, consultar el catálogo y enviar un contacto) está en la [planificación del sprint](planning.md#recorrido-de-demostración-del-catálogo). Cada escenario BDD tiene su prueba en la [matriz de trazabilidad](../../trazabilidad-bdd.md).

## Calidad verificada

- Las 39 ejecuciones del CI entre el 17 y el 23 de noviembre terminaron en verde. Incluyen Checkstyle, pruebas y JaCoCo con el mínimo del 60 % en instrucciones y ramas.
- Los rechazos se verifican también en la base de datos: las pruebas comprueban que no se guardó ningún comprador, mensaje o perfil.
- Las suites de integración crean sus propios municipios, categorías y usuarios, sin depender de los cargadores iniciales.

## Feedback de las revisiones de código

| Hallazgo | Respuesta incorporada | PR |
|---|---|---|
| El contrato del comprador respondía `400` para correo o documento duplicados, distinto del `409` de HU-01. | Se unificó en `409` y se agregó `DocumentoDuplicadoException` como subclase de `RecursoDuplicadoException`. | [#172](https://github.com/Angel-uniajc/AgrovalleConnect/pull/172) |
| La tarjeta de T07-HU17 seguía diciendo `400` para el correo repetido, aunque el contrato y las pruebas decían `409`. | Se corrigió la tarjeta antes de aprobar. | [#191](https://github.com/Angel-uniajc/AgrovalleConnect/pull/191) |
| El revisor pidió cambios en la entidad de contacto, en la consolidación de diagramas y en la documentación del catálogo. | Los autores corrigieron en la misma rama y el revisor aprobó después. | [#181](https://github.com/Angel-uniajc/AgrovalleConnect/pull/181), [#188](https://github.com/Angel-uniajc/AgrovalleConnect/pull/188), [#197](https://github.com/Angel-uniajc/AgrovalleConnect/pull/197) |

## Lo que quedó pendiente

- **Peer Review incompleto.** [#176](https://github.com/Angel-uniajc/AgrovalleConnect/pull/176) y [#180](https://github.com/Angel-uniajc/AgrovalleConnect/pull/180) se fusionaron sin revisión registrada. [#172](https://github.com/Angel-uniajc/AgrovalleConnect/pull/172) y [#175](https://github.com/Angel-uniajc/AgrovalleConnect/pull/175) tuvieron una revisión como comentario, pero no una aprobación. El DoD pide una aprobación por PR.
- **Títulos sin Conventional Commits:** [#189](https://github.com/Angel-uniajc/AgrovalleConnect/pull/189) y [#195](https://github.com/Angel-uniajc/AgrovalleConnect/pull/195) entraron a `develop` con el nombre de la rama como mensaje.
- **Staging:** el laboratorio de la sesión 12 pedía verificar `GET /api/v1/estado` en staging y dejar la evidencia en esta revisión. El endpoint, el `Dockerfile`, el perfil de staging y el `cd.yml` todavía no existen. El ADR-001 sigue en estado *propuesto*. Esta revisión no presenta evidencia de staging.
- **Horas reales:** las tarjetas tienen las horas estimadas (49 en total), pero las horas reales no se registraron en todas. La comparación de estimado contra real queda incompleta.
- **Frontend:** igual que en los sprints anteriores, el incremento es solo backend y se demuestra con Postman. Siguiendo la retroalimentación del segundo corte, el equipo reconoce que la planificación debe mejorar y que los próximos sprints deben incluir tareas de frontend para entregar funcionalidad completa al cliente.
