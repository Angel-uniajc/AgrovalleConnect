# Contrato de registro — HU-17 (Comprador) y compatibilidad con HU-01

## Endpoint
`POST /api/v1/auth/register` (único; se distingue el tipo de usuario por el campo `rol`).

## Discriminación por rol
- `rol` ausente → AGRICULTOR (compatibilidad HU-01: el JSON actual sigue funcionando igual).
- `rol = "AGRICULTOR"` → reglas de HU-01.
- `rol = "COMPRADOR"` → reglas de este documento.
- Cualquier otro valor → `400 Bad Request`.

## Request del comprador
```json
{
  "nombre": "Restaurante La Ceiba",
  "correo": "compras@laceiba.co",
  "password": "Segura#2026",
  "rol": "COMPRADOR",
  "tipo_comprador": "restaurante",
  "documento": "900123456"
}
```

| Campo | Regla |
|---|---|
| nombre | obligatorio, máximo 50 caracteres |
| correo | obligatorio, máximo 50, formato válido, único |
| password | mismas reglas que HU-01 (8 o más caracteres, mayúscula, minúscula, número y carácter especial) |
| rol | `COMPRADOR` |
| tipo_comprador | `individual`, `comerciante` o `restaurante` (sin distinguir mayúsculas); se guarda en MAYÚSCULAS |
| documento | obligatorio, de 6 a 10 dígitos, único |

## Respuesta 201 (sin password ni hash)
```json
{
  "nombre": "Restaurante La Ceiba",
  "rol": "COMPRADOR",
  "tipo_comprador": "restaurante",
  "mensaje": "Comprador registrado exitosamente"
}
```
La respuesta del agricultor (HU-01) no cambia: `{ "nombre", "mensaje" }`.

## Errores
| Caso | Código |
|---|---|
| Campos faltantes o con formato inválido | 400 (mapa campo → mensaje, como hoy) |
| `tipo_comprador` fuera de los valores permitidos | 400 |
| `rol` inválido | 400 |
| Correo duplicado (comprador) | 400 (BDD vigente de HU-17) |
| Documento duplicado (comprador) | 400 |
| Correo o cédula duplicados (agricultor) | 409 (sin cambios, HU-01) |

## Decisiones del esquema
- `comprador.documento` se mantiene `NOT NULL UNIQUE`, como ya está en `schema.sql`; se agregó a HU-17 en `BACKLOG.md`.
- `tipo_comprador` se guarda en MAYÚSCULAS para cumplir el `CHECK` de la tabla `comprador`.
- Ningún registro rechazado cambia el número de usuarios ni de compradores.

## Nota para la implementación
`CorreoDuplicadoException` hereda de `RecursoDuplicadoException`, y `ManejadorErrores` la convierte siempre en `409`. Para que el comprador reciba `400`, la implementación necesita una excepción propia mapeada a `BAD_REQUEST`.
