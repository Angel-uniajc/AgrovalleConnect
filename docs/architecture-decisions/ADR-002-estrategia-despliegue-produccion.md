# ADR-002 · Estrategia de despliegue a producción

**Estado:** propuesto. Depende de que el despliegue a staging del [ADR-001](ADR-001-despliegue-staging-paas.md) esté en servicio.

**Fecha:** 26 de noviembre de 2026.

**Decisores propuestos:** equipo AgroValle Connect. Lo revisa el equipo en la planificación del siguiente sprint.

## Contexto

El ADR-001 propone un servicio Docker en Render para staging, desplegado por GitHub Actions después de que *Maven Verify* pasa. Para producción hace falta decidir cómo se publica una versión nueva sin dejar a los usuarios sin servicio y cómo se vuelve atrás si falla.

Condiciones del proyecto:

- Un solo backend Spring Boot (JAR en contenedor) y una base PostgreSQL.
- Pocos usuarios al comienzo: agricultores y compradores del Valle del Cauca en una prueba piloto.
- Presupuesto de estudiantes: un PaaS con planes gratuitos o de bajo costo y sin un equipo de operaciones.
- `main` está protegido: solo recibe versiones por PR, con una aprobación y el check *Maven Verify*.

## Opciones consideradas

| Estrategia | Cómo funciona | Ventaja | Costo o riesgo para el equipo |
|---|---|---|---|
| **Blue-Green** | Dos ambientes iguales. La versión nueva se publica en el inactivo y el tráfico se cambia de una vez. | Volver atrás es inmediato: se regresa el tráfico al ambiente anterior. | Duplica el costo de la infraestructura. En la industria casi nunca se aprueba por eso. |
| **Canary** | La versión nueva recibe primero un porcentaje pequeño del tráfico y se amplía si las métricas están bien. | Un error afecta solo a unos pocos usuarios. | Exige balanceo por porcentaje y monitoreo de errores y latencia, que el proyecto no tiene. |
| **Rolling** | Las instancias se reemplazan una por una. El PaaS solo mueve el tráfico cuando la nueva pasa el healthcheck. | No requiere infraestructura extra. Render lo hace sin tiempo fuera de servicio si hay un healthcheck. | Durante unos segundos conviven dos versiones, así que los cambios de la base deben ser compatibles hacia atrás. |

## Decisión propuesta

Usar **Rolling** en producción con el despliegue sin tiempo fuera de servicio del PaaS, y dejar **Canary** como objetivo cuando haya usuarios reales y monitoreo.

1. **Disparador:** un tag de versión sobre `main` (por ejemplo `v3.0.0-final`). Un job de `cd.yml` con `needs` del CI llama el hook de producción guardado en GitHub Secrets (`PROD_DEPLOY_HOOK_URL`), con el SHA del tag.
2. **Healthcheck:** `GET /api/v1/estado` responde `200` con el perfil y el estado de la base. El PaaS solo envía tráfico a la instancia nueva cuando ese endpoint responde.
3. **Base de datos:** las migraciones de Flyway deben ser compatibles hacia atrás. Primero se agregan columnas o tablas, y lo que se borra se elimina en una versión posterior.
4. **Volver atrás:** se vuelve a desplegar el tag anterior desde el PaaS o con el mismo workflow. Cada versión queda identificada por su tag y su ejecución del CI.
5. **Configuración:** el perfil `prod` lee `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` y `JWT_SECRET` de variables de entorno, nunca del repositorio.

## Consecuencias

- **Positivas:** no se duplica la infraestructura, el flujo es el mismo de staging con otro hook, y cada versión en producción corresponde a un tag revisado.
- **Negativas:** si una versión falla después del healthcheck, todos los usuarios la ven hasta que se haga el rollback. Por eso las pruebas de staging (ADR-001) son obligatorias antes de crear el tag.
- **Pendiente para aplicar esta decisión:** `GET /api/v1/estado`, el `Dockerfile`, el perfil de producción, la migración inicial de Flyway (`V1__schema_inicial.sql`) y el job de producción en `cd.yml`.
