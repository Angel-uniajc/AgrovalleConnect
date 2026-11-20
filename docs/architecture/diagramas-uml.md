# Diagramas UML de AgroValle Connect

Consolidado de la sesión 13. Las vistas de secuencia y componentes describen la versión de `develop` en `5d99ab6`. Los escenarios proceden del [backlog](../../BACKLOG.md). La [matriz de trazabilidad BDD](../trazabilidad-bdd.md) recoge las pruebas de login y la [planificación del Sprint 2](../sprint-2-planning.md) recoge las de cosechas y fincas.

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

El JWT tiene una vigencia configurada de **una hora**. El servicio incluye en el token el identificador del perfil correspondiente al rol del usuario (agricultor o comprador). Si el usuario carece de ese perfil, se omite el campo `perfil_id`. [Configuración](../../src/main/resources/application.properties) · [Secuencia detallada](../diseno/diagrama-secuencia.md)

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
        Seguridad --> Controladores["REST: AuthController, FincaController y ProductoController"]
        Controladores --> Auth["Registro por rol e inicio de sesión"]
        Controladores --> Fincas["FincaService"]
        Controladores --> Cosechas["CosechaService"]
        Auth --> Credenciales["PasswordEncoder y JwtUtil"]
        Auth --> JPA["Repositorios Spring Data JPA"]
        Fincas --> JPA
        Cosechas --> JPA
        Cosechas --> Eventos["ApplicationEventPublisher"]
        Eventos --> Observer["CosechaPublicadaListener: log"]
        Catalogo["CatalogoService: consulta de ofertas"] --> JPA
        Contacto["MensajeContactoRepository"] --> JPA
        Controladores -.-> Errores["ManejadorErrores: respuestas HTTP"]
    end
    JPA --> DB[("PostgreSQL")]
```

En esta versión el registro por rol, el login, las fincas y la publicación tienen rutas REST. El catálogo cuenta con servicio de lectura y el contacto con entidad, DTO y repositorio. Las rutas para consultar el catálogo y enviar mensajes corresponden a tareas posteriores.

## 5. Colaboración para confirmar un pedido · HU-12

Diseño pendiente en [T14-DIS](https://github.com/Angel-uniajc/AgrovalleConnect/issues/184).

## 6. Colaboración para notificar el alistamiento · HU-15

Diseño pendiente en [T15-DIS](https://github.com/Angel-uniajc/AgrovalleConnect/issues/185).

## 7. Despliegue de staging

El [diagrama de despliegue existente](../diseno/diagrama_de_despliegue.md) describe la arquitectura de referencia. El diagrama de staging y el ADR sobre la elección del PaaS corresponden a [T16-DIS](https://github.com/Angel-uniajc/AgrovalleConnect/issues/186).
