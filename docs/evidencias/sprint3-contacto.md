# Evidencia de HU-05 — contacto directo

Esta evidencia corresponde a T06-HU05 (#171) y complementa las pruebas de T05-HU05 (#170).

## Verificación automatizada

- Prueba: `ContactoControllerIntegrationTest`
- Comando: `./mvnw test -Dtest=ContactoControllerIntegrationTest`
- Cobertura: CON-01 a CON-15, más la respuesta sin datos privados y mensajes de dos compradores.
- Datos: H2 aislada; cada prueba registra sus propios usuarios, finca, categoría y producto mediante la API y revierte su transacción al terminar.
- Resultado registrado en el PR #209: 17 pruebas, 0 fallos; `Maven Verify` en verde, sin violaciones de Checkstyle y con JaCoCo cumplido.

## Recorrido manual en PostgreSQL/staging

1. Registrar y autenticar un agricultor.
2. Crear una finca y publicar una cosecha activa con stock positivo; guardar `id_producto`.
3. Registrar y autenticar un comprador con `rol: COMPRADOR`.
4. Enviar `POST /api/v1/contacto/mensaje` con el JWT del comprador:

```json
{
  "id_producto": 1,
  "mensaje": "Estoy interesado en comprar esta cosecha."
}
```

5. Esperar `200 OK` con `id`, `id_producto` y `fecha_envio`. Consultar la interacción persistida:

```sql
SELECT mc.id, mc.comprador_id, mc.producto_id, mc.mensaje, mc.fecha_envio,
       u.correo AS comprador_correo
FROM mensaje_contacto mc
JOIN comprador c ON c.id = mc.comprador_id
JOIN usuario u ON u.id = c.usuario_id
WHERE mc.producto_id = 1
ORDER BY mc.id DESC;
```

6. Verificar que el contacto no modifica la oferta:

```sql
SELECT id, cantidad_disponible, estado
FROM producto
WHERE id = 1;
```

La fila debe quedar asociada al comprador del JWT y la fecha debe ser la asignada por el servidor. El stock y el estado deben conservar los valores anteriores al envío.
