# Planificación y trazabilidad del Sprint 2 — AgroValle Connect

**Stack:** Java 25, Spring Boot, Spring Data JPA, Spring Security + JWT, PostgreSQL; H2 en pruebas.

## 1. Objetivo funcional

Habilitar el registro de fincas por municipio (HU-14) y la publicación de cosechas (HU-02) para agricultores autenticados. El registro de agricultores (HU-01) y el login con JWT (HU-16) constituyen la base del flujo.

Este documento incorpora la trazabilidad de HU-14 correspondiente a T05-HU14 (#101). La matriz de HU-02 se completará con sus pruebas reales en T06-HU02 (#107).

## 2. Desglose y dependencias de HU-14

| Tarea | Entrega | Dependencia |
|---|---|---|
| T01-HU14 (#97) | Entidades Finca y Municipio, repositorios y catálogo inicial | Ninguna |
| T02-HU14 (#98) | DTOs de entrada/salida y validación | Ninguna |
| T03-HU14 (#99) | FincaService con registro transaccional y validación del municipio | T01 y T02 |
| T04-HU14 (#100) | POST `/api/v1/fincas`, rol AGRICULTOR y mapeo de errores | T03 |
| T05-HU14 (#101) | Pruebas de integración y trazabilidad BDD → JUnit | T04 |

## 3. Matriz de trazabilidad BDD → JUnit 5 — HU-14

Todas las pruebas pertenecen a `FincaControllerIntegrationTest`, en `src/test/java/com/agrovalleconnect/agrovalle_connect/controller/`.

| Escenario | Given | When | Then | Método real |
|---|---|---|---|---|
| Registro válido | Agricultor registrado mediante HU-01 y autenticado mediante HU-16 | Envía datos válidos a POST `/api/v1/fincas` | `201`, JSON con `id`, `municipio` y `area_hectareas`; persiste la finca con el agricultor del token | `registrarFinca_conDatosValidos_retorna201` |
| Municipio no permitido | Agricultor autenticado | Envía un municipio fuera del catálogo | `400`, sin crear fincas | `registrarFinca_conMunicipioNoPermitido_retorna400` |
| Campos inválidos | Agricultor autenticado | Envía nombre vacío y área cero | `400`, mensajes en `nombre` y `areaHectareas`, sin crear fincas | `registrarFinca_conCamposInvalidos_retorna400` |
| Sin autenticación | Petición sin token | Intenta registrar una finca | `401`, sin crear fincas | `registrarFinca_sinToken_retorna401` |
| Rol no autorizado | Usuario COMPRADOR autenticado mediante el login real | Intenta registrar una finca | `403`, sin crear fincas | `registrarFinca_conRolComprador_retorna403` |
| Token inválido (adicional) | Petición con token malformado | Intenta registrar una finca | `401`, sin crear fincas | `registrarFinca_conTokenInvalido_retorna401` |
| Opcionales y normalización (adicional) | Agricultor autenticado | Envía `palmira` sin vereda ni área | `201`, municipio `Palmira`, opcionales nulos y propietario correcto | `registrarFinca_conMunicipioMinusculasYSinOpcionales_retorna201` |

El contrato JSON usa `area_hectareas`. Los errores de `ManejadorErrores` utilizan el nombre Java `areaHectareas`; la prueba de campos inválidos verifica ese contrato existente.

## 4. Ejecución y aislamiento

```bash
./mvnw -Dtest=FincaControllerIntegrationTest test
./mvnw verify
```

En Windows se usa `mvnw.cmd` en lugar de `./mvnw`. Se requiere JDK 25.

- `@SpringBootTest` y MockMvc ejercitan el controlador, la cadena de seguridad JWT, el servicio y los repositorios reales.
- H2 usa la configuración de `src/test/resources/application.properties`. La persistencia se comprueba después de hacer `flush` y limpiar el contexto JPA, forzando la lectura de la base de datos.
- Cada prueba se ejecuta en una transacción que se revierte al terminar, evitando dejar fincas que interfieran con otras pruebas.
- El comprador se prepara en el repositorio porque el registro de compradores pertenece a HU-17; su token se obtiene mediante `/api/v1/auth/login`.
- GitHub Actions ejecuta `./mvnw verify` en cada PR, incluyendo estas pruebas y Checkstyle.
- Estas pruebas usan H2; la compatibilidad específica de PostgreSQL requiere verificación en ese motor.

## 5. Calidad y cierre

T05-HU14 aporta evidencia del atributo de fiabilidad de ISO/IEC 25010: valida el camino exitoso, los rechazos de entrada y los límites de acceso. Para cerrar la tarea se requiere CI en verde y revisión por un compañero, conforme a [Definition of Done](dod.md).
