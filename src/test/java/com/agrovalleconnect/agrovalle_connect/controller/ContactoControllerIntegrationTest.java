package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.MensajeContacto;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.repository.CategoriaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.MensajeContactoRepository;
import com.agrovalleconnect.agrovalle_connect.repository.MunicipioRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * T05-HU05: escenarios BDD CON-01 a CON-15 de POST /api/v1/contacto/mensaje con la
 * cadena real (seguridad, validación, servicio y H2). El comprador y el agricultor se
 * registran por la API (HU-17 y HU-02) y el producto lo publica el agricultor.
 * Cada prueba corre en su propia transacción y se revierte al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContactoControllerIntegrationTest {

    private static final String URL = "/api/v1/contacto/mensaje";

    private static final String PASSWORD = "Clave@123";

    private static final String CORREO_COMPRADOR = "comprador-contacto-bdd@mail.com";

    private static final String CORREO_OTRO_COMPRADOR = "otro-comprador-bdd@mail.com";

    private static final String CORREO_AGRICULTOR = "agricultor-contacto-bdd@mail.com";

    private static final String MUNICIPIO = "Municipio de pruebas HU-05";

    private static final String MENSAJE = "Buenas tardes, me interesan 20 kg de mango.";

    private static final long ID_INEXISTENTE = 999_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MensajeContactoRepository mensajeRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CompradorRepository compradorRepository;

    @Autowired
    private MunicipioRepository municipioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secreto;

    private String tokenComprador;

    private String tokenOtroComprador;

    private String tokenAgricultor;

    private long productoId;

    private long mensajesAntes;

    @BeforeEach
    void prepararCompradoresAgricultorYProductoPublicado() throws Exception {
        tokenComprador = registrarCompradorEIniciarSesion(CORREO_COMPRADOR, "1020304050");
        tokenOtroComprador = registrarCompradorEIniciarSesion(CORREO_OTRO_COMPRADOR, "1020304051");
        tokenAgricultor = registrarAgricultorEIniciarSesion(CORREO_AGRICULTOR, "9900112233");

        municipioRepository.saveAndFlush(Municipio.builder().nombre(MUNICIPIO).build());
        long categoriaId = categoriaRepository.saveAndFlush(
                Categoria.builder().nombre("Categoría de pruebas HU-05").build()).getId();
        long fincaId = registrarFinca(tokenAgricultor);
        productoId = publicarCosecha(tokenAgricultor, fincaId, categoriaId);

        mensajesAntes = mensajeRepository.count();
    }

    // ---------- Datos de prueba: todo entra por la API real ----------

    private String registrarCompradorEIniciarSesion(String correo, String documento) throws Exception {
        String registro = """
                {
                  "nombre": "Comprador de prueba",
                  "rol": "COMPRADOR",
                  "tipo_comprador": "individual",
                  "documento": "%s",
                  "correo": "%s",
                  "password": "%s"
                }
                """.formatted(documento, correo, PASSWORD);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated());
        return iniciarSesion(correo);
    }

    private String registrarAgricultorEIniciarSesion(String correo, String cedula) throws Exception {
        String registro = """
                {
                  "nombre": "Agricultor de prueba",
                  "ubicacion_valle": "Palmira",
                  "cedula": "%s",
                  "correo": "%s",
                  "password": "%s"
                }
                """.formatted(cedula, correo, PASSWORD);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated());
        return iniciarSesion(correo);
    }

    private String iniciarSesion(String correo) throws Exception {
        String login = """
                {"email": "%s", "password": "%s"}
                """.formatted(correo, PASSWORD);
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(respuesta, "$.token");
    }

    private long registrarFinca(String token) throws Exception {
        String finca = """
                {"nombre": "Finca de contacto", "municipio": "%s"}
                """.formatted(MUNICIPIO);
        String respuesta = mockMvc.perform(post("/api/v1/fincas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(finca))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Number id = JsonPath.read(respuesta, "$.id");
        return id.longValue();
    }

    private long publicarCosecha(String token, long fincaId, long categoriaId) throws Exception {
        String cosecha = """
                {
                  "finca_id": %d,
                  "categoria_id": %d,
                  "tipo": "Mango de azúcar",
                  "cantidad": 100.00,
                  "precio_unitario": 2500.00,
                  "fecha_cosecha": "%s"
                }
                """.formatted(fincaId, categoriaId, LocalDate.now());
        String respuesta = mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cosecha))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Number id = JsonPath.read(respuesta, "$.id");
        return id.longValue();
    }

    // ---------- Ayudas de las pruebas ----------

    private static String cuerpo(String idProducto, String mensaje) {
        return """
                {"id_producto": %s, "mensaje": %s}
                """.formatted(idProducto, mensaje);
    }

    private String cuerpoValido() {
        return cuerpo(String.valueOf(productoId), "\"" + MENSAJE + "\"");
    }

    private ResultActions enviar(String json, String token) throws Exception {
        var peticion = post(URL).contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(peticion);
    }

    /** Cambia estado y stock del producto publicado, y lo guarda en la base. */
    private void dejarProductoEn(Producto.Estado estado, String cantidad) {
        Producto producto = productoRepository.findById(productoId).orElseThrow();
        producto.setEstado(estado);
        producto.setCantidadDisponible(new BigDecimal(cantidad));
        productoRepository.saveAndFlush(producto);
    }

    private void assertNoSeGuardoNingunMensaje() {
        assertThat(mensajeRepository.count()).isEqualTo(mensajesAntes);
    }

    private void assertStockIntacto(String esperado) {
        entityManager.flush();
        entityManager.clear();
        Producto producto = productoRepository.findById(productoId).orElseThrow();
        assertThat(producto.getCantidadDisponible()).isEqualByComparingTo(esperado);
    }

    // ---------- Camino exitoso ----------

    @Test
    void enviarContacto_conDatosValidos_retorna200YPersiste() throws Exception {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(5);

        String respuesta = enviar(cuerpoValido(), tokenComprador)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", aMapWithSize(3)))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id_producto").value(productoId))
                .andExpect(jsonPath("$.fecha_envio").isNotEmpty())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Number id = JsonPath.read(respuesta, "$.id");
        entityManager.flush();
        entityManager.clear();
        MensajeContacto guardado = mensajeRepository.findById(id.longValue()).orElseThrow();
        Comprador delToken = compradorRepository.findByUsuarioCorreo(CORREO_COMPRADOR).orElseThrow();

        assertThat(mensajeRepository.count()).isEqualTo(mensajesAntes + 1);
        assertThat(guardado.getMensaje()).isEqualTo(MENSAJE);
        assertThat(guardado.getProducto().getId()).isEqualTo(productoId);
        assertThat(guardado.getComprador().getId()).isEqualTo(delToken.getId());
        assertThat(guardado.getFechaEnvio()).isNotNull().isAfter(antes);
        assertStockIntacto("100.00");
    }

    @Test
    void enviarContacto_conDatosValidos_noExponeDatosPrivadosEnLaRespuesta() throws Exception {
        String respuesta = enviar(cuerpoValido(), tokenComprador)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(respuesta)
                .doesNotContain(CORREO_COMPRADOR, CORREO_AGRICULTOR, PASSWORD)
                .doesNotContainIgnoringCase("password")
                .doesNotContainIgnoringCase("hash")
                .doesNotContainIgnoringCase("correo")
                .doesNotContainIgnoringCase("documento");
    }

    // ---------- Producto inexistente o no disponible ----------

    @Test
    void enviarContacto_conProductoInexistente_retorna404() throws Exception {
        enviar(cuerpo(String.valueOf(ID_INEXISTENTE), "\"" + MENSAJE + "\""), tokenComprador)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Producto no encontrado"));

        assertNoSeGuardoNingunMensaje();
    }

    @Test
    void enviarContacto_conProductoInactivo_retorna400() throws Exception {
        dejarProductoEn(Producto.Estado.INACTIVO, "100.00");

        enviar(cuerpoValido(), tokenComprador)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El producto no está disponible para contacto"));

        assertNoSeGuardoNingunMensaje();
        assertStockIntacto("100.00");
    }

    @Test
    void enviarContacto_conProductoAgotado_retorna400() throws Exception {
        dejarProductoEn(Producto.Estado.AGOTADO, "100.00");

        enviar(cuerpoValido(), tokenComprador)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El producto no está disponible para contacto"));

        assertNoSeGuardoNingunMensaje();
        assertStockIntacto("100.00");
    }

    @Test
    void enviarContacto_conStockCero_retorna400() throws Exception {
        dejarProductoEn(Producto.Estado.ACTIVO, "0.00");

        enviar(cuerpoValido(), tokenComprador)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El producto no está disponible para contacto"));

        assertNoSeGuardoNingunMensaje();
        assertStockIntacto("0.00");
    }

    // ---------- Validación del cuerpo ----------

    @Test
    void enviarContacto_conMensajeVacio_retorna400() throws Exception {
        for (String mensaje : List.of("null", "\"\"", "\"   \"")) {
            enviar(cuerpo(String.valueOf(productoId), mensaje), tokenComprador)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El mensaje es obligatorio"));
        }

        assertNoSeGuardoNingunMensaje();
    }

    @Test
    void enviarContacto_conMensajeEnElLimite_aceptaHasta2000() throws Exception {
        enviar(cuerpo(String.valueOf(productoId), "\"" + "a".repeat(2000) + "\""), tokenComprador)
                .andExpect(status().isOk());
        assertThat(mensajeRepository.count()).isEqualTo(mensajesAntes + 1);

        enviar(cuerpo(String.valueOf(productoId), "\"" + "a".repeat(2001) + "\""), tokenComprador)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El mensaje no puede superar 2000 caracteres"));
        assertThat(mensajeRepository.count()).isEqualTo(mensajesAntes + 1);
    }

    @Test
    void enviarContacto_conIdProductoInvalido_retorna400() throws Exception {
        for (String id : List.of("null", "0", "-5")) {
            enviar(cuerpo(id, "\"" + MENSAJE + "\""), tokenComprador)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.idProducto").isNotEmpty());
        }

        assertNoSeGuardoNingunMensaje();
    }

    @Test
    void enviarContacto_conJsonMalFormado_retorna400() throws Exception {
        for (String json : List.of("{\"id_producto\": 1, \"mensaje\": ",
                cuerpo("\"abc\"", "\"" + MENSAJE + "\""))) {
            enviar(json, tokenComprador)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$", aMapWithSize(1)))
                    .andExpect(jsonPath("$.json").value("El cuerpo de la solicitud no es un JSON válido"));
        }

        assertNoSeGuardoNingunMensaje();
    }

    // ---------- Seguridad: 401 y 403 ----------

    @Test
    void enviarContacto_sinToken_retorna401() throws Exception {
        enviar(cuerpoValido(), null).andExpect(status().isUnauthorized());

        assertNoSeGuardoNingunMensaje();
    }

    @Test
    void enviarContacto_conTokenInvalido_retorna401() throws Exception {
        String expirado = new JwtUtil(secreto, -1_000L)
                .generarToken(CORREO_COMPRADOR, "COMPRADOR", 1L);

        for (String token : List.of("token.no.valido", expirado)) {
            enviar(cuerpoValido(), token).andExpect(status().isUnauthorized());
        }

        assertNoSeGuardoNingunMensaje();
    }

    @Test
    void enviarContacto_conRolAgricultor_retorna403() throws Exception {
        enviar(cuerpoValido(), tokenAgricultor).andExpect(status().isForbidden());

        assertNoSeGuardoNingunMensaje();
    }

    @Test
    void enviarContacto_conCompradorSinPerfil_retorna403() throws Exception {
        // token firmado y de rol COMPRADOR, pero el correo no tiene perfil de comprador
        String sinPerfil = jwtUtil.generarToken("sin-perfil-bdd@mail.com", "COMPRADOR", 1L);

        enviar(cuerpoValido(), sinPerfil)
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acceso denegado"));

        assertNoSeGuardoNingunMensaje();
    }

    // ---------- Identidad y repetición ----------

    @Test
    void enviarContacto_conCompradorIdEnElCuerpo_loIgnora() throws Exception {
        Comprador otro = compradorRepository.findByUsuarioCorreo(CORREO_OTRO_COMPRADOR).orElseThrow();
        Comprador propio = compradorRepository.findByUsuarioCorreo(CORREO_COMPRADOR).orElseThrow();
        String suplantacion = """
                {
                  "id_producto": %d,
                  "mensaje": "%s",
                  "comprador_id": %d,
                  "agricultor_id": 888,
                  "fecha_envio": "2020-01-01T00:00:00"
                }
                """.formatted(productoId, MENSAJE, otro.getId());

        String respuesta = enviar(suplantacion, tokenComprador)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Number id = JsonPath.read(respuesta, "$.id");
        entityManager.flush();
        entityManager.clear();
        MensajeContacto guardado = mensajeRepository.findById(id.longValue()).orElseThrow();
        assertThat(guardado.getComprador().getId()).isEqualTo(propio.getId()).isNotEqualTo(otro.getId());
        assertThat(guardado.getFechaEnvio().getYear()).isNotEqualTo(2020);
    }

    @Test
    void enviarContacto_conMensajeRepetido_guardaDosRegistros() throws Exception {
        String primera = enviar(cuerpoValido(), tokenComprador).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String segunda = enviar(cuerpoValido(), tokenComprador).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Number idPrimera = JsonPath.read(primera, "$.id");
        Number idSegunda = JsonPath.read(segunda, "$.id");
        assertThat(idPrimera).isNotEqualTo(idSegunda);
        assertThat(mensajeRepository.count()).isEqualTo(mensajesAntes + 2);
        assertStockIntacto("100.00");
    }

    @Test
    void enviarContacto_conDosCompradores_cadaMensajeQuedaConSuPropioComprador() throws Exception {
        enviar(cuerpoValido(), tokenComprador).andExpect(status().isOk());
        enviar(cuerpoValido(), tokenOtroComprador).andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();
        Comprador primero = compradorRepository.findByUsuarioCorreo(CORREO_COMPRADOR).orElseThrow();
        Comprador segundo = compradorRepository.findByUsuarioCorreo(CORREO_OTRO_COMPRADOR).orElseThrow();
        List<Long> autores = mensajeRepository.findAll().stream()
                .map(mensaje -> mensaje.getComprador().getId())
                .toList();

        assertThat(autores).containsExactlyInAnyOrder(primero.getId(), segundo.getId());
    }
}
