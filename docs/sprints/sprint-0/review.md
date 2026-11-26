# Sprint 0 Review · AgroValle Connect

**Fecha:** 15 de septiembre de 2026, con ajustes hasta el 24 de septiembre.
**Fecha de elaboración del documento:** 26 de noviembre de 2026, a partir del historial de Git.

## Resultado

El repositorio quedó listo para el Sprint 1. Todo se integró a `develop` por Pull Request el mismo 15 de septiembre, y `develop` se llevó a `main` con el PR #6.

| Entregable | Estado | Evidencia |
|---|---|---|
| Proyecto Spring Boot con Maven Wrapper | Hecho | Commit `0f5a557` |
| Husky con Checkstyle y pruebas antes de cada commit | Hecho | PR #1 (`feature/configuracion-husky`) y [`.husky/pre-commit`](../../../.husky/pre-commit) |
| `.gitignore` | Hecho, con ajustes posteriores | PR #2; corregido el 17 y el 24 de septiembre (PR #13 y #14) por problemas con la compilación de Maven y con la carpeta `target/` |
| Definition of Done firmada por los cuatro integrantes | Hecho | PR #3 y [`docs/dod.md`](../../dod.md) |
| Product Backlog con 15 historias, MoSCoW, BDD y Story Points | Hecho | PR #4 y #5, [`BACKLOG.md`](../../../BACKLOG.md). Hoy tiene 17 historias. |
| README con visión, integrantes y GitFlow | Hecho | PR #8 y [`README.md`](../../../README.md) |
| Integración en `main` | Hecho | PR #6 |
| Pipeline de GitHub Actions | Pasó al Sprint 1 | PR #51 del 26 de septiembre |

## Observaciones

- El archivo del backlog se creó primero como `BACKLOG.dm`. Se renombró con el PR #5 y el archivo viejo se borró con un commit directo desde la web de GitHub (`Delete BACKLOG.dm`), sin PR.
- Varios mensajes de este sprint no siguen Conventional Commits (`fix:Arreglo de extensión...`, `doc(README): ...`). La convención se aplicó de forma estricta a partir del Sprint 1.
- La configuración de Git con correo institucional y llave SSH la hizo cada integrante en su equipo, así que no deja rastro en el repositorio.
