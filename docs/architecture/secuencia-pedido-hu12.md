# Secuencia de creación de pedidos · HU-12

Entrega de [T08-HU12 #206](https://github.com/Angel-uniajc/AgrovalleConnect/issues/206), que actualiza el diseño de T14-DIS. Línea base: `develop` en `dbb9498`, con el servicio de T05-HU12 (#203), las excepciones de T02-HU08 (#208) y la capa HTTP de T06-HU12 (#204, PR #222) fusionados.

## 1. Alcance y entrada

El [contrato de HU-12](../contrato-pedido-hu12.md) define `POST /api/v1/pedidos`, exclusivo para compradores, y una lista `items` con `id_producto` y `cantidad`. No hay repositorio de carrito ni se ejecuta un pago. `comprador_id` se ignora: la identidad procede del correo autenticado.

El diagrama muestra el recorrido HTTP **ya implementado** por [T06-HU12 #204](https://github.com/Angel-uniajc/AgrovalleConnect/issues/204). `JwtAuthenticationFilter` valida el JWT y establece la autenticación. La cadena de seguridad aplica `hasRole("COMPRADOR")`, configurado en `SecurityConfig`: responde 401 sin JWT válido y 403 para otro rol. Spring MVC deserializa y valida `CrearPedidoRequest` con `@Valid` antes de ejecutar `PedidoController.crear`. El controlador obtiene el correo con `authentication.getName()`, llama al servicio y devuelve 201 con `PedidoResponse`.

## 2. Secuencia HTTP y servicio implementados

```mermaid
sequenceDiagram
    autonumber
    actor B as Comprador
    participant J as JwtAuthenticationFilter
    participant A as Cadena de autorización Spring Security
    participant M as Spring MVC
    participant E as PedidoController
    participant H as ManejadorErrores
    participant T as Interceptor transaccional Spring
    participant S as PedidoService
    participant C as CompradorRepository
    participant R as ProductoRepository
    participant F as CalculadoraFlete
    participant EF as EstrategiaFlete
    participant P as PedidoRepository
    participant DB as PostgreSQL
    participant EV as ApplicationEventPublisher
    participant L as PedidoCreadoListener
    B->>J: POST /api/v1/pedidos, Bearer JWT e items
    J->>J: Validar token y establecer Authentication si es válido
    J->>A: Continuar cadena de filtros
    Note over A: Aplicar hasRole COMPRADOR de SecurityConfig
    alt Sin token o token inválido o expirado
        A-->>B: 401 Unauthorized
    else Autenticado con otro rol
        A-->>B: 403 Forbidden
    else Comprador autenticado
        A->>M: Autorizar petición
        M->>M: Deserializar JSON y validar DTO con @Valid
        alt JSON o datos inválidos
            M->>H: Resolver error de lectura o validación
            H-->>B: 400 Bad Request sin invocar servicio
        else Datos válidos
            M->>E: crear(request, authentication)
            E->>T: crear(authentication.getName(), request)
            T->>S: Iniciar transacción y ejecutar crear
            S->>C: findByUsuarioCorreo(correo)
            C->>DB: Consultar perfil comprador
            DB-->>C: Resultado
            C-->>S: Comprador opcional
            alt Perfil ausente
                S-->>T: AccesoDenegadoException
                T-->>E: Revertir y propagar
                E-->>M: AccesoDenegadoException
                M->>H: Resolver excepción
                H-->>B: 403 Forbidden
            else Perfil existente
                S->>S: agruparCantidades(request)
                Note over S,R: LinkedHashMap conserva el orden y suma productos repetidos
                loop Cada producto distinto, hasta el primer rechazo
                    S->>R: findById(idProducto)
                    R->>DB: Consultar producto, finca y municipio
                    DB-->>R: Resultado
                    R-->>S: Producto opcional gestionado por JPA
                    S->>S: Validar existencia, estado y stock acumulado
                end
                alt Alguna línea inválida
                    S-->>T: Excepción antes de reservar
                    T-->>E: Revertir y propagar sin pedido ni evento
                    E-->>M: Excepción de dominio
                    M->>H: Resolver excepción
                    H-->>B: 404 inexistente o 400 no disponible
                else Todas las líneas válidas
                    S->>F: calcularTotal(municipios)
                    loop Cada municipio distinto
                        F->>F: calcular(municipio)
                        F->>EF: tarifa()
                        EF-->>F: Importe en COP
                    end
                    alt Municipio sin estrategia
                        F-->>S: MunicipioSinTarifaException
                        S-->>T: Propagar sin reservar
                        T-->>E: Revertir y propagar
                        E-->>M: MunicipioSinTarifaException
                        M->>H: municipioSinTarifa(ex)
                        H-->>B: 400 con error Municipio sin tarifa de flete
                    else Flete calculado
                        F-->>S: Suma de tarifas
                        S->>S: Crear Pedido PENDIENTE_PAGO
                        S->>S: reservar(pedido, seleccion, cantidades)
                        Note over S,P: agregarDetalle copia el precio vigente, descuenta stock y marca AGOTADO si llega a cero
                        S->>P: save(pedido)
                        P->>DB: Persistir pedido y detalles en cascada
                        DB-->>P: Identificador
                        P-->>S: Pedido guardado
                        S->>EV: publishEvent(PedidoCreadoEvent)
                        EV->>L: registrarPedido(evento)
                        L->>L: Registrar aviso por cada agricultor distinto
                        L-->>EV: Retorno síncrono
                        EV-->>S: Retorno síncrono
                        S->>S: respuesta(guardado)
                        S-->>T: PedidoResponse
                        T->>DB: Flush del stock gestionado y COMMIT
                        DB-->>T: Transacción confirmada
                        T-->>E: PedidoResponse
                        E-->>M: ResponseEntity CREATED con PedidoResponse
                        M-->>B: 201 Created con JSON
                    end
                end
            end
        end
    end
```

El diagrama representa las operaciones lógicas, no un orden fijo de sentencias SQL: Hibernate puede ejecutar inserciones o un flush antes del commit. `ProductoRepository.save` no se invoca para la reserva; JPA detecta los cambios de los productos gestionados dentro de la transacción.

## 3. Strategy, reserva y respuesta

- `CalculadoraFlete` recibe las implementaciones `FleteDagua`, `FletePalmira` y `FleteBuga` de `EstrategiaFlete`. Las tarifas son 12000.00, 10000.00 y 15000.00 COP, respectivamente. Cobra una tarifa por municipio distinto.
- Todas las líneas se validan y el flete se calcula **antes** de modificar existencias. Ante un rechazo no hay reservas parciales, pedido, detalles ni evento.
- Un producto repetido se consolida en un detalle con la cantidad total; respeta la clave `(pedido_id, producto_id)` de `DetallePedido`.
- El precio unitario se copia al detalle. `subtotal` suma cantidad por precio; `total` suma subtotal y `costo_flete`. La respuesta incluye `id`, `estado`, `items`, `subtotal`, `costo_flete` y `total`.
- No se implementan liberación de reservas ni protección frente a reservas concurrentes de distintos compradores; son límites explícitos del [contrato de HU-08](../contrato-stock-hu08.md#6-fuera-de-alcance).

## 4. Observer y errores

`PedidoCreadoEvent` es un record sin entidades JPA: contiene `pedidoId`, `compradorId` y una copia inmutable de `agricultoresIds`. Se emite una sola vez después de `PedidoRepository.save`, **antes del commit**. `PedidoCreadoListener` usa `@EventListener` y solo registra avisos en el log; no envía correo ni guarda notificaciones.

Una excepción del guardado o de un listener síncrono se propaga y revierte la transacción. Un mensaje ya escrito en el log no se revierte ni demuestra que el commit se haya confirmado. Los efectos externos futuros deben ejecutarse después del commit, por ejemplo con `@TransactionalEventListener(phase = AFTER_COMMIT)`.

Desde T03-HU08 (#210, PR #225), un producto inexistente lanza `RecursoNoEncontradoException` (404); uno con estado `AGOTADO` o stock en cero, `ProductoAgotadoException`; uno `INACTIVO` con stock positivo, `ProductoNoDisponibleException`, y una cantidad que supera el stock, `StockInsuficienteException`. Los caminos de rechazo, con sus respuestas, están en la [secuencia del rechazo por stock](secuencia-rechazo-stock-hu08.md) de [T05-HU08 #212](https://github.com/Angel-uniajc/AgrovalleConnect/issues/212), que parte de esta secuencia sin duplicarla.

`ManejadorErrores.municipioSinTarifa` ya traduce `MunicipioSinTarifaException` a 400 con `{"error":"Municipio sin tarifa de flete"}`, conforme al contrato y a T06-HU12 (#204).

## 5. Código y verificación

| Evidencia | Qué verifica |
|---|---|
| [PedidoController](../../src/main/java/com/agrovalleconnect/agrovalle_connect/controller/PedidoController.java) | Correo autenticado, delegación y respuesta 201 |
| [PedidoControllerWebTest](../../src/test/java/com/agrovalleconnect/agrovalle_connect/controller/PedidoControllerWebTest.java) | JWT y seguridad reales, 201, 401, 403, 400 y 404 con servicio simulado |
| [PedidoService](../../src/main/java/com/agrovalleconnect/agrovalle_connect/service/PedidoService.java) | Validación, flete, reserva, guardado y evento |
| [CalculadoraFlete](../../src/main/java/com/agrovalleconnect/agrovalle_connect/service/flete/CalculadoraFlete.java) | Selección de Strategy y municipios distintos |
| [PedidoServiceTest](../../src/test/java/com/agrovalleconnect/agrovalle_connect/service/PedidoServiceTest.java) | Reserva antes de guardar, evento después del guardado y rechazos sin evento |
| [PedidoServiceIntegrationTest](../../src/test/java/com/agrovalleconnect/agrovalle_connect/service/PedidoServiceIntegrationTest.java) | Persistencia, listener real y rollback ante fallo del observador |
| [PedidoCreadoEventTest](../../src/test/java/com/agrovalleconnect/agrovalle_connect/event/PedidoCreadoEventTest.java) | Inmutabilidad de los destinatarios |

La [colaboración del pedido](diagramas-uml.md#5-colaboración-para-confirmar-un-pedido--hu-12) muestra los mismos participantes y mensajes numerados. Las pruebas BDD HTTP completas siguen asignadas a T07-HU12 y T04-HU08; este documento no las da por realizadas.
