---
name: Reporte de error
about: Informar un fallo del backend, de la documentación o del pipeline
title: "[BUG] "
labels: bug
assignees: ''
---

## Descripción del error
Qué pasa y qué debería pasar, en una o dos frases.

## Pasos para reproducirlo
1. Endpoint o pantalla: `POST /api/v1/...`
2. Cuerpo enviado o datos usados:
3. Token o rol con el que se probó:
4. Resultado:

## Resultado esperado
Código HTTP y cuerpo que debería responder, con el escenario BDD o el contrato que lo define (por ejemplo, `STK-01` en `docs/contrato-stock-hu08.md`).

## Resultado obtenido
Código HTTP, cuerpo de la respuesta y, si aplica, el log o la ejecución del CI.

## Evidencias
Capturas, enlaces a la ejecución de GitHub Actions o al reporte de JaCoCo o Checkstyle.

## Entorno
- Rama o commit:
- Local (H2 o PostgreSQL) o staging:
- Java y sistema operativo:

## Atributo de calidad afectado (ISO/IEC 25010)
Por ejemplo: fiabilidad, seguridad, mantenibilidad o adecuación funcional.
