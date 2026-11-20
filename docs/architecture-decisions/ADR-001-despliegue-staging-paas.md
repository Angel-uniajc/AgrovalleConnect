# ADR-001 · Despliegue de staging en un PaaS

**Estado:** propuesto para revisión en [T16-DIS](https://github.com/Angel-uniajc/AgrovalleConnect/issues/186).

**Fecha** 20 de noviembre de 2026.

**Decisores propuestos** Equipo AgroValle Connect. Propuesta de Ángel Castillo, con revisión asignada a Taborda en T16-DIS. La aprobación está pendiente.

## Contexto

El equipo necesita probar el backend integrado en un entorno compartido con Java 25 y una base PostgreSQL independiente. Los diseños de [T04-DIS](https://github.com/Angel-uniajc/AgrovalleConnect/issues/77) y [T09-DIS](https://github.com/Angel-uniajc/AgrovalleConnect/issues/110) aportan la topología de aplicación y base de datos y un ejemplo de contenedor Java 25.

**Verificado en `develop` (`f4d61d0`).** El proyecto compila para Java 25. GitHub Actions ejecuta `./mvnw -B clean verify` en los PR y al subir cambios a `main`. El workflow termina tras la verificación. El Dockerfile figura como ejemplo en la documentación. La existencia del servicio remoto, sus secretos y una conexión efectiva a staging queda pendiente de verificación. [POM](../../pom.xml) · [Workflow](../../.github/workflows/ci.yml) · [Dockerfile de referencia](../diseno/diagrama_de_despliegue.md)

## Opciones consideradas

| Opción | Consecuencia para el equipo |
|---|---|
| Render con Docker y PostgreSQL administrado | El equipo fija Java 25 en la imagen y el PaaS construye y ejecuta el servicio desde un commit identificable. |
| Máquina virtual con Docker y PostgreSQL separado | Conserva el control del servidor y exige administrar actualizaciones del sistema, certificados y reinicios. |

## Decisión propuesta

Usar un servicio web Docker en Render vinculado a `develop` y una base PostgreSQL de pruebas llamada `agrovalle_staging`. El equipo puede reutilizar el diseño del contenedor y delegar la operación del servidor. [Docker en Render](https://render.com/docs/docker)

1. Incorporar un Dockerfile que compile con JDK 25 y ejecute el JAR con JRE 25. Configurar el servicio para que el despliegue lo inicie GitHub Actions y desactivar el despliegue automático por cada push en Render.
2. Ampliar el workflow para verificar cada push a `develop`. Solo tras el éxito de Maven Verify en esa rama, invocar por HTTPS el hook almacenado como `STAGING_DEPLOY_HOOK_URL`, con el SHA verificado en el parámetro `ref`. Así se despliega el mismo commit aunque llegue otro cambio durante la compilación. [Deploy hooks de Render](https://render.com/docs/deploy-hooks)
3. Preparar el esquema en la base destinada al ejercicio. Configurar en Render `SPRING_DATASOURCE_URL` con la URL JDBC de staging y SSL, además de `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` y un `JWT_SECRET` propio del entorno. El hook únicamente inicia el despliegue y la aplicación lee sus variables desde Render. [Esquema de referencia](../diseno/schema.sql) · [Configuración de Spring](../../src/main/resources/application.properties)

La URL de conexión entregada por el proveedor debe adaptarse al formato JDBC. Para una base llamada `agrovalle_staging`, usar `jdbc:postgresql://<host>:5432/agrovalle_staging?sslmode=require` en `SPRING_DATASOURCE_URL`, con el host real del servicio. El usuario y la contraseña se configuran en sus variables correspondientes.

Configurar explícitamente `JWT_SECRET` en Render. Si se omite, la aplicación utiliza la clave de desarrollo incluida por defecto en `application.properties` y puede arrancar con ella. Antes de habilitar staging, comprobar que la variable del entorno contiene una clave propia.

## Consecuencias y comprobación

Cada despliegue se puede asociar con una ejecución de CI y un commit. A cambio, el equipo mantiene la imagen, las credenciales y el esquema de staging, y depende de la disponibilidad del PaaS y de PostgreSQL.

Si se elige el plan gratuito de Render, el servicio web se suspende tras 15 minutos sin tráfico y la siguiente petición espera su reactivación. PostgreSQL gratuito expira a los 30 días y queda inaccesible. Después hay 14 días de gracia para pasar a un plan de pago antes de que se borren la base y sus datos. Estas condiciones afectan la disponibilidad para demostraciones y la conservación de los datos de prueba. [Límites del plan gratuito de Render](https://render.com/docs/free)

Para confirmar la puesta en servicio, registrar la ejecución de Maven Verify, el SHA desplegado en Render y una consulta autenticada a `GET /api/v1/productos` que devuelva una oferta de prueba conocida de `agrovalle_staging`. Verificar en PostgreSQL que la sesión del backend usa SSL. El acuse de recibo del hook solo acredita el inicio o encolado del despliegue. [Diagrama de staging](../architecture/diagramas-uml.md#7-despliegue-de-staging)
