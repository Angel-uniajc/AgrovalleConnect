# Diagramas UML de AgroValle Connect

Consolidado de la sesión 13. Las secuencias de las secciones 1 a 3 describen la versión de `develop` en `62f037a`. Los escenarios proceden del [backlog](../../BACKLOG.md). La [matriz de trazabilidad BDD](../trazabilidad-bdd.md) recoge las pruebas de login y la [planificación del Sprint 2](../sprint-2-planning.md) recoge las de cosechas y fincas. La secuencia del registro y el login del comprador (HU-17) está en su propio archivo: [secuencia-registro-comprador-hu17.md](secuencia-registro-comprador-hu17.md).

## 1. Secuencia de inicio de sesión · HU-16

**Dado** un usuario registrado, **cuando** envía su `email` y `password` a `POST /api/v1/auth/login`, **entonces** recibe un JWT con HTTP 200 si las credenciales son válidas. Tanto un correo desconocido como una contraseña incorrecta producen el mismo 401. Spring MVC rechaza con 400 una petición inválida antes de invocar el servicio.

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant C as AuthController
    participant S as AutenticacionService
    participant R as UsuarioRepository
    participant DB as PostgreSQL
    participant P as PasswordEncoder
    participant J as JwtUtil
    U->>C: POST /api/v1/auth/login
    C->>S: login(request)
    S->>R: findByCorreo(email)
    R->>DB: Consultar usuario
    DB-->>R: Resultado
    R-->>S: Usuario opcional
    opt Usuario encontrado
        S->>P: matches(password, passwordHash)
        P-->>S: Resultado de BCrypt
    end
    alt Credenciales válidas
        S->>J: generarToken(correo, rol, perfilId)
        J-->>S: JWT firmado
        S-->>C: LoginResponse
        C-->>U: 200 OK
    else Correo desconocido o contraseña incorrecta
        S-->>C: CredencialesInvalidasException
        Note over C,U: ManejadorErrores traduce la excepción a 401
    end
```

El JWT tiene una vigencia configurada de **una hora**. El servicio incluye en el token el identificador del perfil correspondiente al rol del usuario (agricultor o comprador). Si el usuario carece de ese perfil, se omite el campo `perfil_id`. [Configuración](../../src/main/resources/application.properties) · [Secuencia detallada](../diseno/diagrama-secuencia.md) · [Login del comprador (HU-17)](secuencia-registro-comprador-hu17.md#3-login-del-comprador)

## 2. Secuencia de publicación de cosecha · HU-02

**Dado** un agricultor autenticado con una finca propia, **cuando** publica una cosecha con categoría existente, cantidad y precio positivos y fecha de hoy o posterior, **entonces** la API guarda el producto y devuelve su identificador con HTTP 201. Una fecha pasada produce 400 durante la validación del DTO y un token ausente o vencido produce 401 en la cadena de seguridad.

```mermaid
sequenceDiagram
    autonumber
    actor A as Agricultor
    participant C as ProductoController
    participant S as CosechaService
    participant R as Repositorios JPA
    participant DB as PostgreSQL
    participant P as ApplicationEventPublisher
    participant L as CosechaPublicadaListener
    A->>C: POST /api/v1/productos con JWT
    Note over A,C: Spring Security autoriza AGRICULTOR y MVC valida el DTO
    C->>S: publicar(correo, request)
    S->>R: FincaRepository.findById(fincaId)
    R->>DB: Consultar finca
    DB-->>R: Resultado de la consulta
    R-->>S: Finca opcional
    alt Finca inexistente
        S-->>C: RecursoNoEncontradoException
        Note over C,A: ManejadorErrores traduce la excepción a 404
    else Finca de otro agricultor
        S-->>C: AccesoDenegadoException
        Note over C,A: ManejadorErrores traduce la excepción a 403
    else Finca propia
        S->>R: CategoriaRepository.findById(categoriaId)
        R->>DB: Consultar categoría
        DB-->>R: Resultado de la consulta
        R-->>S: Categoría opcional
        alt Categoría inexistente
            S-->>C: CategoriaInvalidaException
            Note over C,A: ManejadorErrores traduce la excepción a 400
        else Categoría existente
            S->>R: ProductoRepository.save(producto)
            R->>DB: Insertar producto ACTIVO
            DB-->>R: Identificador del producto
            R-->>S: Producto con ID
            S->>P: publishEvent(CosechaPublicadaEvent)
            P->>L: registrarPublicacion(evento)
            L-->>P: Registro en log completado
            P-->>S: Retorno síncrono
            S-->>C: PublicarCosechaResponse al confirmar la transacción
            C-->>A: 201 Created con id
        end
    end
```

El servicio verifica la finca y la categoría antes de guardar el producto. El listener se ejecuta dentro de la transacción. [Pruebas de HU-02 en la sección 7 del Sprint 2](../sprint-2-planning.md) · [Alternativas y orden del Observer](../diseno/diagrama-secuencia.md) · [Colaboración de cosechas](../diseno/diagrama-colaboracion.md)

## 3. Secuencia de registro de finca · HU-14

**Dado** un agricultor autenticado, **cuando** registra su finca en un municipio permitido, **entonces** la finca queda vinculada a ese agricultor y municipio y recibe HTTP 201. Si el municipio no está en el catálogo, recibe 400.

```mermaid
sequenceDiagram
    autonumber
    actor A as Agricultor
    participant C as FincaController
    participant S as FincaService
    participant R as Repositorios JPA
    participant DB as PostgreSQL
    A->>C: POST /api/v1/fincas con JWT
    Note over A,C: Spring Security autoriza AGRICULTOR y MVC valida el DTO
    C->>S: registrar(authentication.getName(), request)
    S->>R: AgricultorRepository.findByUsuarioCorreo(correo)
    R->>DB: Consultar agricultor
    R-->>S: Agricultor autenticado
    S->>R: MunicipioRepository.findByNombreIgnoreCase(municipio)
    R->>DB: Consultar municipio
    R-->>S: Municipio opcional
    alt Municipio permitido
        S->>R: FincaRepository.save(finca)
        R->>DB: Insertar finca con ambas referencias
        R-->>S: Finca guardada
        S-->>C: RegistroFincaResponse
        C-->>A: 201 Created
    else Municipio desconocido
        S-->>C: MunicipioNoPermitidoException
        Note over C,A: ManejadorErrores traduce la excepción a 400
    end
```

[Pruebas de HU-14 en la sección 3 del Sprint 2](../sprint-2-planning.md)

## 4. Componentes del backend

Vista general adaptada del [diagrama de componentes](../diseno/diagrama-de-componentes.md). El backend usa **Java 25**. Los controladores reciben HTTP y delegan en servicios, que aplican reglas de negocio y delimitan transacciones. Los repositorios JPA acceden a PostgreSQL. [Configuración de compilación](../../pom.xml)

```mermaid
flowchart TB
    Cliente["Cliente HTTP"] --> Seguridad["JwtAuthenticationFilter y SecurityConfig"]
    subgraph Backend["Backend Spring Boot · Java 25"]
        Seguridad --> Controladores["REST: AuthController, FincaController, ProductoController y ContactoController"]
        Controladores --> Auth["Registro por rol e inicio de sesión"]
        Controladores --> Fincas["FincaService"]
        Controladores --> Cosechas["CosechaService"]
        Auth --> Credenciales["PasswordEncoder y JwtUtil"]
        Auth --> JPA["Repositorios Spring Data JPA"]
        Fincas --> JPA
        Cosechas --> JPA
        Cosechas --> Eventos["ApplicationEventPublisher"]
        Eventos --> Observer["CosechaPublicadaListener: log"]
        Controladores --> Catalogo["CatalogoService: consulta de ofertas"]
        Catalogo --> JPA
        Controladores --> Contacto["ContactoService: envío de mensajes"]
        Contacto --> ContactoRepo["MensajeContactoRepository"]
        ContactoRepo --> JPA
        Contacto --> JPA
        Controladores -.-> Errores["ManejadorErrores: respuestas HTTP"]
    end
    JPA --> DB[("PostgreSQL")]
```

En `develop` (`0233ff4`), compradores y agricultores autenticados consultan el catálogo mediante `GET /api/v1/productos`. El controlador delega los filtros de municipio y categoría en `CatalogoService`. El contacto del comprador tiene su propia ruta, `POST /api/v1/contacto/mensaje`. [Secuencia de consulta del catálogo](../diseno/diagrama-secuencia.md#10-hu-04--consulta-del-catálogo) · [Secuencia de contacto](../diseno/diagrama-secuencia.md#11-hu-05--contacto-directo-del-comprador)

## 5. Colaboración para confirmar un pedido · HU-12

Actualizada por [T08-HU12 #206](https://github.com/Angel-uniajc/AgrovalleConnect/issues/206), sobre `develop` en `dbb9498`. Sustituye la propuesta de T14-DIS: el servicio recibe `items`, sin carrito persistente ni pago. [Contrato de pedidos](../contrato-pedido-hu12.md) · [Secuencia detallada](secuencia-pedido-hu12.md)

**Dado** un comprador autenticado con perfil y una lista válida de productos disponibles, **cuando** envía `POST /api/v1/pedidos`, **entonces** se guarda el pedido `PENDIENTE_PAGO`, se reserva el stock y se publica `PedidoCreadoEvent`. `PedidoController`, incorporado en T06-HU12 (#204), devuelve 201 con `PedidoResponse` tras el commit. La seguridad exige COMPRADOR y Spring MVC valida el cuerpo antes de ejecutar el controlador.

```mermaid
flowchart TB
    B["Comprador autenticado"]
    E["controlador : PedidoController"]
    S["servicio : PedidoService"]
    C["compradores : CompradorRepository"]
    R["productos : ProductoRepository"]
    F["flete : CalculadoraFlete"]
    EF["estrategias : EstrategiaFlete<br/>FleteDagua / FletePalmira / FleteBuga"]
    P["pedidos : PedidoRepository"]
    EV["eventos : ApplicationEventPublisher"]
    L["listener : PedidoCreadoListener"]
    B -->|"0. POST /api/v1/pedidos, JWT e items"| E
    E -->|"1. crear(authentication.getName(), request validado)"| S
    S -->|"1.1 findByUsuarioCorreo(correo)"| C
    S -->|"1.2 agruparCantidades: sumar repetidos"| S
    S -->|"1.3 findById por producto y validar todos"| R
    S -->|"1.4 calcularTotal(municipios)"| F
    F -->|"1.4.1 tarifa por municipio distinto"| EF
    S -->|"1.5 reservar: agregarDetalle, descontar stock, marcar AGOTADO"| S
    S -->|"1.6 save(pedido), detalles en cascada"| P
    S -->|"1.7 publishEvent(PedidoCreadoEvent)"| EV
    EV -->|"1.7.1 registrarPedido(evento), síncrono"| L
    L -->|"1.7.1.1 aviso en log por agricultor distinto"| L
    S -.->|"2. PedidoResponse tras confirmar la transacción"| E
    E -.->|"3. 201 Created con JSON"| B
```

La entrada HTTP resume la cadena de filtros y Spring MVC: JWT ausente o inválido → 401, rol distinto → 403 y JSON o DTO inválido → 400, sin ejecutar el servicio. La [secuencia detallada](secuencia-pedido-hu12.md#2-secuencia-http-y-servicio-implementados) muestra estos rechazos y el manejador de errores, incluido 400 para municipio sin tarifa.

El flete usa Strategy y suma una tarifa por municipio distinto: Dagua 12000.00, Palmira 10000.00 y Buga 15000.00 COP. Los detalles copian el precio vigente y consolidan los productos repetidos. JPA persiste los cambios de stock mediante seguimiento de entidades gestionadas; no se llama a `ProductoRepository.save` para reservar.

La validación de todas las líneas y el cálculo del flete ocurren antes de cualquier descuento. Si fallan, no hay reservas, detalles, pedido ni evento. El guardado y la reserva comparten una transacción: una excepción de persistencia o del listener síncrono la revierte. El evento se entrega antes del commit; el log no confirma por sí mismo una compra persistida. Los efectos externos futuros requieren ejecución después del commit.

No se vacía un carrito ni se procesa un pago. La concurrencia entre compradores y la liberación de reservas quedan fuera del contrato de este sprint. [Límites y errores actuales](secuencia-pedido-hu12.md#4-observer-y-errores) · [Pruebas existentes](secuencia-pedido-hu12.md#5-código-y-verificación)

## 6. Colaboración para notificar el alistamiento · HU-15

**Diseño propuesto.** El ejemplo de alistamiento aparece como HU-13 en la guía de la sesión 13. En el backlog de AgroValle Connect corresponde a **HU-15, Confirmación de Alistamiento de Lote**. El equipo reserva HU-13 para la consulta de la ruta de envío. [Historias de usuario](../../BACKLOG.md) · [T15-DIS](https://github.com/Angel-uniajc/AgrovalleConnect/issues/185)

**Dado** un agricultor con un pedido pagado de su producción, **cuando** marca el lote como alistado mediante `PATCH /api/v1/pedidos/{id}/alistamiento`, **entonces** el pedido pasa de `PAGADO` a `ALISTADO`, queda disponible para la asignación de transporte y la API responde `200 OK`. Para notificar al comprador, se propone registrar un aviso asociado al pedido mediante el [patrón Observer usado en HU-02](../diseno/diagrama-colaboracion.md).

**Alcance del alistamiento.** El esquema guarda el estado del pedido completo. Para este PATCH se propone exigir al menos un detalle y que **todas** sus líneas correspondan al agricultor autenticado. El servicio obtiene el comprador de `pedido.comprador_id` y recorre `detalle_pedido → producto → finca → agricultor` para verificar quién produjo cada línea. Un pedido mixto requiere un diseño de alistamiento parcial antes de que cada productor pueda confirmar su parte. [Esquema de datos](../diseno/schema.sql)

**Componentes propuestos.** La entidad `Notificacion`, la tabla `notificacion` y `NotificacionRepository` se incorporarían para registrar el aviso al comprador. También se propone restringir este PATCH al rol `AGRICULTOR` en `SecurityConfig`, siguiendo la regla de publicación de productos. El servicio verificaría además la titularidad de las líneas.

```mermaid
flowchart TB
    A["Agricultor"]
    C["c : PedidoController"]
    S["s : PedidoService"]
    R["r : PedidoRepository"]
    P["p : ApplicationEventPublisher"]
    L["l : PedidoAlistadoListener"]
    N["n : NotificacionRepository"]

    A -- "1: PATCH de alistamiento →" --- C
    C -- "1.1: alistar pedido del agricultor →" --- S
    S -- "1.1.1: cargar pedido, comprador y productor de cada línea →" --- R
    S -- "1.1.2: validar líneas propias y estado PAGADO" --- S
    S -- "1.1.3: guardar estado ALISTADO →" --- R
    S -- "1.1.4: publicar PedidoAlistadoEvent →" --- P
    P -- "1.1.4.1: entregar evento →" --- L
    L -- "1.1.4.1.1: guardar aviso para el comprador →" --- N
    S -- "1.2: devolver resultado tras confirmar la transacción →" --- C
    C -- "2: responder 200 OK →" --- A

    classDef observer fill:#fff3e0,stroke:#e65100,color:#000
    class S,P,L observer
```

El servicio publica un evento con los identificadores del pedido y del comprador. El listener registra el aviso en la misma transacción que el cambio de estado. Si falla el registro, ambos cambios se revierten. El controlador devuelve el `200` después de confirmarse la transacción.

**Rechazos.** HU-15 establece `400 Bad Request` para un pedido en `PENDIENTE_PAGO`. Para los demás casos se propone:

| Condición | Respuesta |
|---|---|
| El pedido no existe | `404 Not Found` |
| El usuario autenticado carece del rol agricultor o alguna línea pertenece a otro agricultor (incluidos los pedidos mixtos) | `403 Forbidden` |
| El pedido carece de detalles o está en `ALISTADO`, `EN_CAMINO`, `ENTREGADO` o `CANCELADO` | `409 Conflict` |

En todos los rechazos se conserva el estado existente y se omite la publicación del evento. Repetir el PATCH sobre un pedido ya alistado tampoco duplica el aviso.

## 7. Despliegue de staging

**Diseño propuesto.** El flujo parte del commit integrado en `develop`. Hoy GitHub Actions ejecuta Maven Verify en los PR y en los envíos a `main`. La verificación posterior al merge en `develop`, el despliegue y la conexión con la base de staging quedan como propuesta. [Workflow vigente](../../.github/workflows/ci.yml) · [ADR-001: elección del PaaS para staging](../architecture-decisions/ADR-001-despliegue-staging-paas.md)

```mermaid
flowchart TB
    G["GitHub · commit integrado en develop"]
    CI["GitHub Actions · Maven Verify con JDK 25"]
    H["Deploy hook de staging"]
    subgraph PAAS["Render · servicio web de staging"]
        B["Construcción Docker del commit verificado"]
        A["Spring Boot · JRE 25"]
        B --> A
    end
    DB[("PostgreSQL · agrovalle_staging")]
    G -->|"Push a develop (propuesto)"| CI
    CI -->|"HTTPS · STAGING_DEPLOY_HOOK_URL<br/>tras verify exitoso, con el SHA"| H
    H --> B
    A -->|"JDBC con SSL"| DB
```

El servicio recibe la configuración de conexión y la clave JWT como variables del entorno de staging. La URL del hook se guarda como secreto de GitHub Actions. [Arquitectura de despliegue de referencia](../diseno/diagrama_de_despliegue.md)

