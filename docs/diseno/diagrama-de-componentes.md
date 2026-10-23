# Diagrama de Componentes — AgroValle Connect

> Documento asociado a **T06-DIS (#79)**, parte del **Sprint de Diseño (#73)**.

> **Notación:** se usa una notación UML simplificada en Mermaid: cada caja es un componente (estereotipo `«component»`) y cada flecha, una dependencia («usa a»).
> **Leyenda:**  verde = implementado en `develop` ·  naranja = parcialmente implementado · amarillo punteado = diseñado, pendiente.
> Línea continua = dependencia existente · línea punteada = dependencia futura.

---

## 1. Vista general del sistema

```mermaid
flowchart TB
    Cliente["Cliente<br/>Frontend web / Postman"]
    Pasarela["Pasarela de pagos<br/>(servicio externo)"]
    Correo["Servicio de notificaciones<br/>(correo / SMTP)"]

    subgraph Backend["AgroValle Connect — Spring Boot 4.1 / Java 25"]
        direction TB

        subgraph Transversal["Componentes transversales"]
            JWT["«component»<br/>JwtAuthenticationFilter + JwtUtil<br/>identifica al usuario si trae token<br/>HU-16"]
            SEC["«component»<br/>SecurityConfig<br/>SecurityFilterChain<br/>decide rutas públicas y protegidas"]
            PE["«component»<br/>PasswordEncoder<br/>BCrypt"]
            ME["«component»<br/>ManejadorErrores<br/>@RestControllerAdvice<br/>400 / 401 / 409"]
        end

        subgraph Modulos["Módulos de negocio — cada uno con Controller + Service + Repository"]
            AUTH["«component»<br/>AUTH<br/>HU-01 registro agricultor ✔<br/>HU-16 login ✔<br/>HU-17 registro comprador ⏳"]
            FINCA["«component»<br/>FINCAS<br/>HU-14<br/>registrar finca por municipio"]
            PROD["«component»<br/>PRODUCTOS y CATÁLOGO<br/>HU-02 publicar cosecha<br/>HU-04 filtro municipio y categoría"]
            CONT["«component»<br/>CONTACTO<br/>HU-05<br/>mensaje comprador a agricultor"]
            PED["«component»<br/>PEDIDOS<br/>HU-12 realizar pedido<br/>HU-08 validar stock<br/>HU-15 alistamiento"]
            PAGO["«component»<br/>PAGOS<br/>HU-07<br/>cobro y comprobante"]
            LOG["«component»<br/>LOGÍSTICA<br/>HU-06 asignar ruta<br/>HU-13 consultar ruta"]
            CAL["«component»<br/>CALIFICACIONES<br/>HU-09 producto<br/>HU-10 transporte"]
            REC["«component»<br/>RECIBOS<br/>HU-11<br/>genera PDF de la compra"]
            PRE["«component»<br/>PRECIOS REGIONALES<br/>HU-03<br/>promedio por producto"]
            NOT["«component»<br/>NOTIFICACIONES<br/>avisos a agricultor y comprador<br/>HU-06, HU-07"]
        end
    end

    subgraph BD["PostgreSQL — schema.sql en 3FN"]
        direction LR
        T_USR[("usuario<br/>agricultor<br/>comprador<br/>usuario_telefono")]
        T_CAT[("municipio<br/>categoria")]
        T_OFE[("finca<br/>producto<br/>mensaje_contacto")]
        T_COM[("pedido<br/>detalle_pedido<br/>pago<br/>envio<br/>calificacion")]
    end

    %% Entrada y seguridad: el filtro identifica, SecurityConfig decide
    Cliente -->|"HTTP + JSON"| JWT
    JWT -->|"si hay token válido, identifica al usuario"| SEC
    SEC -->|"/auth/** es público"| AUTH
    SEC -->|"rutas protegidas: exige usuario identificado"| Modulos

    %% Dependencias entre módulos
    AUTH --> PE
    AUTH -->|"genera token al login"| JWT
    FINCA -->|"dueño: agricultor"| AUTH
    PROD -->|"se publica en una finca"| FINCA
    PROD -.->|"evento: cosecha publicada<br/>(CosechaPublicadaEvent)"| NOT
    CONT -->|"sobre un producto"| PROD
    PED -->|"valida y descuenta stock"| PROD
    PAGO -->|"confirma el pedido"| PED
    PAGO -->|"cobra"| Pasarela
    PAGO -.->|"avisa pago"| NOT
    LOG -->|"requiere pedido ALISTADO"| PED
    LOG -.->|"avisa envío"| NOT
    CAL -->|"solo pedidos ENTREGADOS"| PED
    REC -->|"lee pedido y pago"| PAGO
    PRE -->|"promedia ventas"| PED
    NOT -->|"envía"| Correo

    %% Persistencia
    AUTH -->|"JPA / Hibernate"| T_USR
    FINCA --> T_OFE
    FINCA --> T_CAT
    PROD --> T_OFE
    PROD --> T_CAT
    CONT --> T_OFE
    PED --> T_COM
    PAGO --> T_COM
    LOG --> T_COM
    CAL --> T_COM
    REC --> T_COM
    PRE --> T_COM

    %% Errores
    Modulos -.->|"lanzan excepciones"| ME
    ME -.->|"respuesta HTTP con código"| Cliente

    classDef hecho fill:#d4edda,stroke:#28a745,color:#000;
    classDef parcial fill:#ffe5b4,stroke:#e67e22,color:#000;
    classDef pendiente stroke-dasharray: 5 5,fill:#fff3cd,stroke:#b58900,color:#000;
    class JWT,SEC,PE,ME hecho;
    class AUTH parcial;
    class FINCA,PROD,CONT,PED,PAGO,LOG,CAL,REC,PRE,NOT pendiente;
```

---

## 2. Detalle por capas del módulo AUTH

Todos los demás módulos seguirán este mismo patrón de capas.

### 2.1 Registro de agricultor (HU-01)

```mermaid
flowchart LR
    C["AuthController<br/>POST /api/v1/auth/register"]
    D["RegistroAgricultorRequest<br/>RegistroAgricultorResponse<br/>validaciones Jakarta"]
    S["AgricultorRegisterService<br/>unicidad de correo y cédula<br/>hash BCrypt"]
    R1["UsuarioRepository"]
    R2["AgricultorRepository"]
    E["Entidades JPA<br/>Usuario 1 a 1 Agricultor"]
    DB[("PostgreSQL<br/>usuario / agricultor")]

    C -->|"@Valid"| D
    C --> S
    S --> R1
    S --> R2
    R1 --> E
    R2 --> E
    E -->|"Hibernate"| DB

    classDef hecho fill:#d4edda,stroke:#28a745,color:#000;
    class C,D,S,R1,R2,E,DB hecho;
```

### 2.2 Inicio de sesión (HU-16)

```mermaid
flowchart LR
    C["AuthController<br/>POST /api/v1/auth/login"]
    D["LoginRequest<br/>LoginResponse<br/>validaciones Jakarta"]
    S["AutenticacionService<br/>busca usuario por correo<br/>compara con matches()"]
    PE["PasswordEncoder<br/>BCrypt"]
    J["JwtUtil<br/>genera token HS256"]
    R["UsuarioRepository"]
    EX["CredencialesInvalidasException<br/>ManejadorErrores responde 401"]
    DB[("PostgreSQL<br/>usuario")]

    C -->|"@Valid"| D
    C --> S
    S --> R
    S --> PE
    S -->|"credenciales válidas"| J
    S -.->|"credenciales inválidas"| EX
    R -->|"Hibernate"| DB

    classDef hecho fill:#d4edda,stroke:#28a745,color:#000;
    class C,D,S,PE,J,R,EX,DB hecho;
```

---

## 3. Catálogo de componentes

| Componente | HU | Responsabilidad | Tablas que usa | Estado |
| :--- | :---: | :--- | :--- | :---: |
| **JwtAuthenticationFilter + JwtUtil** | HU-16 | `JwtUtil` genera y valida tokens HS256; el filtro lee el token en cada petición e identifica al usuario. | — | ✅ |
| **SecurityConfig** | HU-16 | Con el filtro JWT conectado, decide qué rutas son públicas y cuáles exigen autenticación (401 sin token). Sesión sin estado. | — | ✅ |
| **PasswordEncoder (BCrypt)** | HU-01, 16 | Hash irreversible al registrar y verificación con `matches()` al iniciar sesión. | `usuario.password_hash` | ✅ |
| **ManejadorErrores** | todas | Traduce excepciones a HTTP: 409 (duplicados), 401 (credenciales inválidas) y 400 (validación, con detalle por campo). | — | ✅ |
| **AUTH** | HU-01, 16, 17 | Registro de agricultor y login (hechos). Registro de comprador (HU-17) pendiente. | `usuario`, `agricultor`, `comprador`, `usuario_telefono` |  parcial |
| **FINCAS** | HU-14 | Registrar fincas asociadas a un municipio del Valle. | `finca`, `municipio` | pendiente |
| **PRODUCTOS y CATÁLOGO** | HU-02, 04 | Publicar cosechas (publica el evento `CosechaPublicadaEvent`); filtrar por municipio y categoría. | `producto`, `finca`, `categoria`, `municipio` | pendiente |
| **CONTACTO** | HU-05 | Mensaje directo del comprador al agricultor. | `mensaje_contacto` | pendiente |
| **PEDIDOS** | HU-12, 08, 15 | Crear pedido, validar stock, marcar alistamiento. | `pedido`, `detalle_pedido`, `producto` | pendiente |
| **PAGOS** | HU-07 | Cobrar vía pasarela y registrar el pago. | `pago`, `pedido` | pendiente |
| **LOGÍSTICA** | HU-06, 13 | Programar ruta, generar código de seguimiento, consultarlo. | `envio`, `pedido` | pendiente |
| **CALIFICACIONES** | HU-09, 10 | Calificar producto y transporte (una por tipo y pedido). | `calificacion`, `pedido` | pendiente |
| **RECIBOS** | HU-11 | Generar el PDF de la compra. | `pedido`, `detalle_pedido`, `pago` | pendiente |
| **PRECIOS REGIONALES** | HU-03 | Calcular el precio promedio por producto. | `detalle_pedido`, `producto` | pendiente |
| **NOTIFICACIONES** | HU-06, 07 | Avisar a agricultor y comprador; escucha eventos como `CosechaPublicadaEvent` (patrón Observer). | — | pendiente |

## 4. Reglas de arquitectura

1. **Las flechas solo bajan:** Controller → Service → Repository → BD. Un controller nunca toca la BD (atributo ISO 25010 de **Mantenibilidad**).
2. **Un módulo habla con otro por su Service**, nunca por su Repository, para que cada módulo proteja sus propias reglas (alta cohesión, bajo acoplamiento).
3. **Seguridad en dos pasos:** el filtro JWT solo *identifica* al usuario en cada petición; `SecurityConfig` es quien *decide* qué rutas exigen autenticación.
4. **Seguridad y manejo de errores son transversales:** se escriben una vez y aplican a todos los módulos.
5. **Los servicios externos** (pasarela, correo) quedan aislados en su propio componente para poder cambiarlos o simularlos en las pruebas.
6. **Comunicación por eventos:** cuando un módulo solo necesita *avisar* algo (cosecha publicada), lo hace con un evento en vez de llamar directamente a NOTIFICACIONES (patrón Observer).