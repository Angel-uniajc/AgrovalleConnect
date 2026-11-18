# Contrato de registro — HU-17 (Comprador) y compatibilidad con HU-01

## Endpoint
`POST /api/v1/auth/register` (único; se distingue el tipo de usuario por el campo `rol`).

## Discriminación por rol
- `rol` ausente → AGRICULTOR (compatibilidad HU-01: el JSON actual sigue funcionando igual).
- `rol = "AGRICULTOR"` → reglas de HU-01.
- `rol = "COMPRADOR"` → reglas de este documento.
- Cualquier otro valor (por ejemplo `"ADMIN"`) → `400 Bad Request` con `{"rol": "El rol debe ser AGRICULTOR o COMPRADOR"}`. Nunca se registra como agricultor.

## Request del comprador
```json
{
  "nombre": "Restaurante La Ceiba",
  "correo": "compras@laceiba.co",
  "password": "Segura@2026",
  "rol": "COMPRADOR",
  "tipo_comprador": "restaurante",
  "documento": "900123456"
}
```

| Campo | Regla |
|---|---|
| nombre | obligatorio, máximo 50 caracteres |
| correo | obligatorio, máximo 50, formato válido, único entre todos los usuarios |
| password | mismas reglas que HU-01: 8 o más caracteres, con mayúscula, minúscula, número y al menos un especial de `@ $ ! % * ? &` (otros símbolos, como `#`, no se aceptan) |
| rol | `COMPRADOR` |
| tipo_comprador | `individual`, `comerciante` o `restaurante` (sin distinguir mayúsculas); se guarda en MAYÚSCULAS |
| documento | obligatorio, de 6 a 10 dígitos, **sin dígito de verificación** (un NIT `900123456-7` se envía como `900123456`); único entre compradores |

## Respuesta 201 (sin password ni hash)
```json
{
  "nombre": "Restaurante La Ceiba",
  "rol": "COMPRADOR",
  "tipo_comprador": "RESTAURANTE",
  "mensaje": "Comprador registrado exitosamente"
}
```
`tipo_comprador` se devuelve normalizado, igual a como queda guardado (MAYÚSCULAS).
La respuesta del agricultor (HU-01) no cambia: `{ "nombre", "mensaje" }`.

## Errores
Todas las respuestas de validación usan el mismo formato de mapa campo → mensaje que hoy produce `ManejadorErrores`.

| Caso | Código |
|---|---|
| Campos faltantes o con formato inválido | 400 |
| `tipo_comprador` fuera de los valores permitidos | 400 |
| `rol` inválido | 400 |
| Correo duplicado (agricultor o comprador) | 409 Conflict |
| Documento duplicado (comprador) | 409 Conflict |
| Cédula duplicada (agricultor) | 409 Conflict (sin cambios, HU-01) |

## Decisiones del esquema
- `comprador.documento` se mantiene `NOT NULL UNIQUE`; se agregó a HU-17 en `BACKLOG.md`.
- La unicidad del documento es solo entre compradores: puede coincidir con la cédula de un agricultor, porque son cuentas distintas (y el correo de cada cuenta debe ser diferente).
- `tipo_comprador` se guarda en MAYÚSCULAS para cumplir el `CHECK` de la tabla `comprador`.
- Ningún registro rechazado cambia el número de usuarios ni de compradores.

## Estrategia para la implementación
- Dos DTO: `RegistroAgricultorRequest` (el actual) y `RegistroCompradorRequest`, cada uno con sus propias validaciones (`@NotBlank`, `@Pattern`…).
- Un tipo común con `@JsonTypeInfo(use = NAME, include = EXISTING_PROPERTY, property = "rol", visible = true, defaultImpl = RegistroAgricultorRequest.class)` y `@JsonSubTypes` para `AGRICULTOR` y `COMPRADOR`. El controlador recibe `@Valid @RequestBody` de ese tipo común y envía cada caso a su servicio.
- **Cuidado 1:** `defaultImpl` se usa cuando el rol falta **y también cuando es desconocido**. Por eso el DTO del agricultor debe validar que `rol` sea nulo o `AGRICULTOR`; si no, `"ADMIN"` se registraría como agricultor.
- **Cuidado 2:** un JSON mal formado lanza `HttpMessageNotReadableException`, que `ManejadorErrores` no captura hoy. Hay que agregar un manejador que responda 400 con formato de mapa.
- Duplicados: se reutiliza `CorreoDuplicadoException` (409) y se crea `DocumentoDuplicadoException` que herede de `RecursoDuplicadoException` (409).
