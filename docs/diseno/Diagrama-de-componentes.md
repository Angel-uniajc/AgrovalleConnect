# Diagrama de Componentes — AgroValle Connect


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
            SEC["SecurityConfig<br/>SecurityFilterChain<br/>rutas públicas y protegidas"]
            JWT["JwtService + JwtFilter<br/>emite y valida tokens<br/>HU-16"]
            PE["PasswordEncoder<br/>BCrypt"]
            ME["ManejadorErrores<br/>@RestControllerAdvice<br/>convierte excepciones en HTTP"]
        end

        subgraph Modulos["Módulos de negocio — cada uno con Controller + Service + Repository"]
            AUTH["AUTH<br/>HU-01 registro agricultor<br/>HU-17 registro comprador<br/>HU-16 login"]
            FINCA["FINCAS<br/>HU-14<br/>registrar finca por municipio"]
            PROD["PRODUCTOS y CATÁLOGO<br/>HU-02 publicar cosecha<br/>HU-04 filtro municipio y categoría"]
            CONT["CONTACTO<br/>HU-05<br/>mensaje comprador a agricultor"]
            PED["PEDIDOS<br/>HU-12 realizar pedido<br/>HU-08 validar stock<br/>HU-15 alistamiento"]
            PAGO["PAGOS<br/>HU-07<br/>cobro y comprobante"]
            LOG["LOGÍSTICA<br/>HU-06 asignar ruta<br/>HU-13 consultar ruta"]
            CAL["CALIFICACIONES<br/>HU-09 producto<br/>HU-10 transporte"]
            REC["RECIBOS<br/>HU-11<br/>genera PDF de la compra"]
            PRE["PRECIOS REGIONALES<br/>HU-03<br/>promedio por producto"]
            NOT["NOTIFICACIONES<br/>avisos a agricultor y comprador<br/>HU-06, HU-07"]
        end
    end

    subgraph BD["PostgreSQL — schema.sql en 3FN"]
        direction LR
        T_USR[("usuario<br/>agricultor<br/>comprador<br/>usuario_telefono")]
        T_CAT[("municipio<br/>categoria")]
        T_OFE[("finca<br/>producto<br/>mensaje_contacto")]
        T_COM[("pedido<br/>detalle_pedido<br/>pago<br/>envio<br/>calificacion")]
    end

    %% Entrada y seguridad
    Cliente -->|"HTTP + JSON"| SEC
    SEC -->|"/auth/** es público"| AUTH
    SEC -->|"resto de rutas exige token"| JWT
    JWT -->|"solo usuarios autenticados"| Modulos

    %% Dependencias entre módulos
    AUTH --> PE
    AUTH -.->|"emite token al login"| JWT
    FINCA -->|"dueño: agricultor"| AUTH
    PROD -->|"se publica en una finca"| FINCA
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
    class PE,ME hecho;
    class SEC,AUTH parcial;
    class JWT,FINCA,PROD,CONT,PED,PAGO,LOG,CAL,REC,PRE,NOT pendiente;
```

---

## 2. Detalle por capas del módulo implementado (AUTH / HU-01)

Todos los demás módulos seguirán este mismo patrón de capas.

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

---

## 3. Catálogo de componentes

| Componente | HU | Responsabilidad | Tablas que usa | Estado |
| :--- | :---: | :--- | :--- | :---: |
| **SecurityConfig** | HU-16 | Define rutas públicas y protegidas; sesión sin estado. Falta conectar el filtro JWT. | — | parcial |
| **PasswordEncoder (BCrypt)** | HU-01 | Hash irreversible de contraseñas. | `usuario.password_hash` | ✅ |
| **ManejadorErrores** | todas | Traduce excepciones a códigos HTTP. Hoy solo maneja `IllegalArgumentException` → 409. | — | ✅ |
| **JwtService / JwtFilter** | HU-16 | Genera token HS256 con `rol` y `perfil_id`; lo valida en cada petición. | — | Pendiente |
| **AUTH** | HU-01, 16, 17 | Registro de agricultor (hecho), registro de comprador y login (pendientes). | `usuario`, `agricultor`, `comprador`, `usuario_telefono` |  parcial |
| **FINCAS** | HU-14 | Registrar fincas asociadas a un municipio del Valle. | `finca`, `municipio` | Pendiente |
| **PRODUCTOS y CATÁLOGO** | HU-02, 04 | Publicar cosechas; filtrar por municipio y categoría. | `producto`, `finca`, `categoria`, `municipio` | Pendiente |
| **CONTACTO** | HU-05 | Mensaje directo del comprador al agricultor. | `mensaje_contacto` | Pendiente |
| **PEDIDOS** | HU-12, 08, 15 | Crear pedido, validar stock, marcar alistamiento. | `pedido`, `detalle_pedido`, `producto` | Pendiente |
| **PAGOS** | HU-07 | Cobrar vía pasarela y registrar el pago. | `pago`, `pedido` | Pendiente |
| **LOGÍSTICA** | HU-06, 13 | Programar ruta, generar código de seguimiento, consultarlo. | `envio`, `pedido` | Pendiente |
| **CALIFICACIONES** | HU-09, 10 | Calificar producto y transporte (una por tipo y pedido). | `calificacion`, `pedido` | Pendiente |
| **RECIBOS** | HU-11 | Generar el PDF de la compra. | `pedido`, `detalle_pedido`, `pago` | Pendiente |
| **PRECIOS REGIONALES** | HU-03 | Calcular el precio promedio por producto. | `detalle_pedido`, `producto` | Pendiente |
| **NOTIFICACIONES** | HU-06, 07 | Avisar a agricultor y comprador. | — | Pendiente |

## 4. Reglas de arquitectura

1. **Las flechas solo bajan:** Controller → Service → Repository → BD. Un controller nunca toca la BD (atributo ISO 25010 de **Mantenibilidad**).
2. **Un módulo habla con otro por su Service**, nunca por su Repository, para que cada módulo proteja sus propias reglas.
3. **Seguridad y manejo de errores son transversales:** se escriben una vez y aplican a todos los módulos.
4. **Los servicios externos** (pasarela, correo) quedan aislados en su propio componente para poder cambiarlos o simularlos en las pruebas.
