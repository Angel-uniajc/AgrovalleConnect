# Trazabilidad BDD → JUnit 5 — AgroValle Connect

Cada escenario Given/When/Then del backlog se verifica con al menos una prueba automatizada.
Los nombres propuestos en `sprint-1-planning.md` (sección 7) se relacionan con las pruebas reales.

## HU-01 — Registro de Agricultores

| Escenario BDD | Resultado esperado | Nombre propuesto (planning) | Prueba real |
| :--- | :---: | :--- | :--- |
| Registro válido | `201 Created` y persiste con hash | `registrarAgricultor_conDatosValidos_retorna201` | `AuthControllerIntegrationTest#registroExitoso_persisteYGuardaHashNoTextoPlano` |
| Cédula duplicada | `409 Conflict`, no se duplica | `registrarAgricultor_conCedulaDuplicada_rechazaRegistro` | `AuthControllerIntegrationTest#rechazaCedulaDuplicada`, `AgricultorRegisterServiceTest#registrar_cedulaDuplicada_lanzaExcepcionYNoGuardaNada` |
| Campos vacíos | `400 Bad Request`, no se guarda | `registrarAgricultor_conCamposVacios_retorna400` | `AuthControllerIntegrationTest#registrarAgricultor_conCamposVacios_retorna400` |
| Cédula inválida | `400 Bad Request`, no se guarda | — | `AuthControllerIntegrationTest#registrarAgricultor_conCedulaInvalida_retorna400`, `RegistroAgricultorRequestTest` |
| Correo duplicado (adicional) | `409 Conflict` | — | `AuthControllerIntegrationTest#rechazaCorreoDuplicado` |
| Contraseña débil (adicional) | `400 Bad Request`, no se guarda | — | `AuthControllerIntegrationTest#contrasenaDebil_devuelve400YNoGuardaNada` |

## HU-16 — Inicio de Sesión

| Escenario BDD | Resultado esperado | Prueba real (nombre = matriz) |
| :--- | :---: | :--- |
| Credenciales válidas | `200 OK` con token JWT | `AuthLoginIntegrationTest#login_conCredencialesValidas_retorna200YToken` |
| Contraseña incorrecta | `401` con mensaje genérico | `AuthLoginIntegrationTest#login_conPasswordIncorrecta_retorna401` |
| Email inexistente | `401` con el mismo mensaje | `AuthLoginIntegrationTest#login_conEmailInexistente_retorna401` |
| Campos faltantes | `400 Bad Request` | `AuthLoginIntegrationTest#login_conCamposFaltantes_retorna400` |
| Mensaje no revela cuál dato falló (adicional) | Mismo cuerpo en ambos 401 | `AuthLoginIntegrationTest#login_emailInexistenteYPasswordIncorrecta_respondenElMismoMensaje` |
| Endpoint protegido sin token (adicional) | `401` | `AccesoProtegidoIntegrationTest#accesoProtegido_sinToken_retorna401` |
| Endpoint protegido con token válido (adicional) | Pasa la seguridad | `AccesoProtegidoIntegrationTest#accesoProtegido_conTokenValido_pasaLaSeguridad` |
| Endpoint protegido con token inválido (adicional) | `401` | `AccesoProtegidoIntegrationTest#accesoProtegido_conTokenInvalido_retorna401` |

## Cómo verificar

`./mvnw test` ejecuta todas las pruebas listadas. Un escenario sin prueba en esta tabla es una brecha de trazabilidad.