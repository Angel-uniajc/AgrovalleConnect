package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.event.CosechaPublicadaEvent;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.repository.CategoriaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.FincaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.MunicipioRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import com.agrovalleconnect.agrovalle_connect.service.CatalogoService;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * T05-HU04 (#164): escenarios BDD CAT-01 a CAT-15 del catálogo (GET /api/v1/productos)
 * con un comprador real registrado y autenticado por HU-17 y datos sembrados por la propia prueba.
 * Contrato en docs/contrato-catalogo-hu04.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents
class CatalogoIntegrationTest {

    private static final String URL = "/api/v1/productos";

    private static final String PASSWORD = "Clave@123";

    private static final List<String> CAMPOS_PUBLICOS = List.of("id", "tipo", "cantidad_disponible",
            "precio_unitario", "fecha_cosecha", "categoria", "finca_id", "municipio");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MunicipioRepository municipioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FincaRepository fincaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ApplicationEvents eventos;

    @MockitoSpyBean
    private CatalogoService catalogoService;

    @Value("${jwt.secret}")
    private String secretoJwt;

    private String tokenComprador;

    private String tokenAgricultor;

    private Long daguaFrutas;

    private Long daguaGranos;

    private Long daguaTuberculos;

    private Long palmiraFrutas;

    private Long palmiraHortalizas;

    private Long bugaHortalizas;

    private Long fincaDaguaId;

    private long productosAntes;

    private Map<Long, BigDecimal> stockAntes;

    @BeforeEach
    void sembrarCatalogoConCompradorReal() throws Exception {
        tokenComprador = registrarCompradorEIniciarSesion("comprador-catalogo@mail.com", "1029384756");
        tokenAgricultor = registrarAgricultorEIniciarSesion("agricultor-catalogo@mail.com", "7766554433");
        String tokenOtroAgricultor = registrarAgricultorEIniciarSesion("vecino-catalogo@mail.com", "7766554434");

        Municipio dagua = municipio("Dagua");
        Municipio palmira = municipio("Palmira");
        Municipio buga = municipio("Buga");
        Categoria frutas = categoria("Frutas");
        Categoria granos = categoria("Granos");
        Categoria hortalizas = categoria("Hortalizas");
        Categoria tuberculos = categoria("Tubérculos");

        fincaDaguaId = registrarFinca(tokenAgricultor, "La Esperanza", dagua.getNombre());
        Long fincaPalmiraId = registrarFinca(tokenAgricultor, "El Edén", palmira.getNombre());
        Long fincaBugaId = registrarFinca(tokenOtroAgricultor, "Los Guaduales", buga.getNombre());
        Finca fincaDagua = fincaRepository.getReferenceById(fincaDaguaId);
        Finca fincaPalmira = fincaRepository.getReferenceById(fincaPalmiraId);
        Finca fincaBuga = fincaRepository.getReferenceById(fincaBugaId);

        // Ofertas disponibles (ACTIVO y stock mayor que cero)
        daguaFrutas = producto("Mango", "25.50", fincaDagua, frutas, Producto.Estado.ACTIVO);
        daguaGranos = producto("Maíz", "40.00", fincaDagua, granos, Producto.Estado.ACTIVO);
        palmiraFrutas = producto("Piña", "12.75", fincaPalmira, frutas, Producto.Estado.ACTIVO);
        bugaHortalizas = producto("Tomate", "30.00", fincaBuga, hortalizas, Producto.Estado.ACTIVO);
        daguaTuberculos = producto("Yuca", "18.00", fincaDagua, tuberculos, Producto.Estado.ACTIVO);
        palmiraHortalizas = producto("Cebolla", "9.50", fincaPalmira, hortalizas, Producto.Estado.ACTIVO);

        // Ofertas NO disponibles: deben quedar fuera aunque coincidan con los filtros
        producto("Mango inactivo", "10.00", fincaDagua, frutas, Producto.Estado.INACTIVO);
        producto("Mango agotado", "10.00", fincaDagua, frutas, Producto.Estado.AGOTADO);
        producto("Mango sin stock", "0.00", fincaDagua, frutas, Producto.Estado.ACTIVO);
        producto("Piña inactiva", "5.00", fincaPalmira, frutas, Producto.Estado.INACTIVO);

        entityManager.flush();
        entityManager.clear();
        productosAntes = productoRepository.count();
        stockAntes = stockActual();
    }

    @AfterEach
    void consultarNoModificaDatosNiEmiteEventos() {
        entityManager.flush();
        entityManager.clear();
        assertThat(productoRepository.count()).isEqualTo(productosAntes);
        assertThat(stockActual()).isEqualTo(stockAntes);
        assertThat(eventos.stream(CosechaPublicadaEvent.class)).isEmpty();
    }

    // ---------- Preparación de datos ----------

    private String registrarCompradorEIniciarSesion(String correo, String documento) throws Exception {
        String registro = """
                {
                  "nombre": "Comprador del catálogo",
                  "rol": "COMPRADOR",
                  "tipo_comprador": "comerciante",
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
                  "nombre": "Agricultor del catálogo",
                  "ubicacion_valle": "Dagua",
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

    private Long registrarFinca(String token, String nombre, String municipio) throws Exception {
        String finca = """
                {"nombre": "%s", "municipio": "%s"}
                """.formatted(nombre, municipio);
        String respuesta = mockMvc.perform(post("/api/v1/fincas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(finca))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Number id = JsonPath.read(respuesta, "$.id");
        return id.longValue();
    }

    private Municipio municipio(String nombre) {
        return municipioRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> municipioRepository.saveAndFlush(Municipio.builder().nombre(nombre).build()));
    }

    private Categoria categoria(String nombre) {
        return categoriaRepository.findAll().stream()
                .filter(categoria -> categoria.getNombre().equals(nombre))
                .findFirst()
                .orElseGet(() -> categoriaRepository.saveAndFlush(Categoria.builder().nombre(nombre).build()));
    }

    private Long producto(String tipo, String cantidad, Finca finca, Categoria categoria, Producto.Estado estado) {
        Producto producto = Producto.builder()
                .tipo(tipo)
                .cantidadDisponible(new BigDecimal(cantidad))
                .precioUnitario(new BigDecimal("3200.00"))
                .fechaCosecha(LocalDate.of(2026, 11, 20))
                .estado(estado)
                .finca(finca)
                .categoria(categoria)
                .build();
        return productoRepository.saveAndFlush(producto).getId();
    }

    private Map<Long, BigDecimal> stockActual() {
        return productoRepository.findAll().stream()
                .collect(Collectors.toMap(Producto::getId, Producto::getCantidadDisponible));
    }

    // ---------- Consulta del catálogo ----------

    private ResultActions consultar(String token, String municipio, String categoria) throws Exception {
        MockHttpServletRequestBuilder peticion = get(URL);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        if (municipio != null) {
            peticion.param("municipio", municipio);
        }
        if (categoria != null) {
            peticion.param("categoria", categoria);
        }
        return mockMvc.perform(peticion);
    }

    private String cuerpoOk(String token, String municipio, String categoria) throws Exception {
        return consultar(token, municipio, categoria)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private List<Long> ids(String token, String municipio, String categoria) throws Exception {
        List<Number> ids = JsonPath.read(cuerpoOk(token, municipio, categoria), "$[*].id");
        return ids.stream().map(Number::longValue).toList();
    }

    private List<Long> idsComprador(String municipio, String categoria) throws Exception {
        return ids(tokenComprador, municipio, categoria);
    }

    private List<Long> todasLasDisponibles() {
        return List.of(daguaFrutas, daguaGranos, palmiraFrutas, bugaHortalizas, daguaTuberculos, palmiraHortalizas);
    }

    private String tokenExpirado() {
        return new JwtUtil(secretoJwt, -60_000L).generarToken("comprador-catalogo@mail.com", "COMPRADOR", 1L);
    }

    private String tokenFirmadoConOtraClave() {
        return new JwtUtil("otra-clave-distinta-que-tambien-tiene-mas-de-32-caracteres", 3_600_000L)
                .generarToken("comprador-catalogo@mail.com", "COMPRADOR", 1L);
    }

    // ---------- Escenarios BDD ----------

    @Test
    void consultarCatalogo_conAmbosFiltros_retornaSoloCoincidencias() throws Exception {
        // CAT-01
        String cuerpo = cuerpoOk(tokenComprador, "Dagua", "Frutas");

        List<Map<String, Object>> ofertas = JsonPath.read(cuerpo, "$");
        assertThat(ofertas).hasSize(1);
        Map<String, Object> oferta = ofertas.get(0);
        assertThat(oferta).containsOnlyKeys(CAMPOS_PUBLICOS);
        assertThat(((Number) oferta.get("id")).longValue()).isEqualTo(daguaFrutas);
        assertThat(oferta.get("tipo")).isEqualTo("Mango");
        assertThat(new BigDecimal(oferta.get("cantidad_disponible").toString())).isEqualByComparingTo("25.50");
        assertThat(new BigDecimal(oferta.get("precio_unitario").toString())).isEqualByComparingTo("3200.00");
        assertThat(oferta.get("cantidad_disponible")).isInstanceOf(Number.class);
        assertThat(oferta.get("precio_unitario")).isInstanceOf(Number.class);
        assertThat(oferta.get("fecha_cosecha")).isEqualTo("2026-11-20");
        assertThat(oferta.get("categoria")).isEqualTo("Frutas");
        assertThat(((Number) oferta.get("finca_id")).longValue()).isEqualTo(fincaDaguaId);
        assertThat(oferta.get("municipio")).isEqualTo("Dagua");
    }

    @Test
    void consultarCatalogo_sinCoincidencias_retornaArregloVacio() throws Exception {
        // CAT-02: Buga existe y Frutas existe, pero no hay Frutas disponibles en Buga
        assertThat(cuerpoOk(tokenComprador, "Buga", "Frutas")).isEqualTo("[]");
    }

    @Test
    void consultarCatalogo_sinFiltros_retornaOfertasDisponibles() throws Exception {
        // CAT-03
        assertThat(idsComprador(null, null)).containsExactlyElementsOf(todasLasDisponibles());
    }

    @Test
    void consultarCatalogo_soloMunicipio_filtraPorMunicipio() throws Exception {
        // CAT-04
        assertThat(idsComprador("Dagua", null)).containsExactly(daguaFrutas, daguaGranos, daguaTuberculos);
    }

    @Test
    void consultarCatalogo_soloCategoria_filtraPorCategoria() throws Exception {
        // CAT-05
        assertThat(idsComprador(null, "Frutas")).containsExactly(daguaFrutas, palmiraFrutas);
    }

    @Test
    void consultarCatalogo_conMayusculasMixtas_ignoraMayusculas() throws Exception {
        // CAT-06
        String canonico = cuerpoOk(tokenComprador, "Palmira", "Frutas");

        assertThat(cuerpoOk(tokenComprador, "pALmIrA", "fRuTaS")).isEqualTo(canonico);
        assertThat(idsComprador("pALmIrA", "fRuTaS")).containsExactly(palmiraFrutas);
    }

    @Test
    void consultarCatalogo_conEspaciosPerifericos_normalizaFiltros() throws Exception {
        // CAT-07
        String canonico = cuerpoOk(tokenComprador, "Palmira", "Frutas");

        assertThat(cuerpoOk(tokenComprador, " Palmira ", " Frutas ")).isEqualTo(canonico);
        assertThat(idsComprador("   Palmira", "Frutas   ")).containsExactly(palmiraFrutas);
    }

    @Test
    void consultarCatalogo_conFiltrosVacios_losTrataComoAusentes() throws Exception {
        // CAT-08
        assertThat(idsComprador("", "")).containsExactlyElementsOf(todasLasDisponibles());
        assertThat(idsComprador("   ", "   ")).containsExactlyElementsOf(todasLasDisponibles());
        assertThat(idsComprador("", "Frutas")).containsExactly(daguaFrutas, palmiraFrutas);
        assertThat(idsComprador("Dagua", "  ")).containsExactly(daguaFrutas, daguaGranos, daguaTuberculos);
    }

    @Test
    void consultarCatalogo_conFiltroDesconocido_retornaArregloVacio() throws Exception {
        // CAT-09
        assertThat(cuerpoOk(tokenComprador, "Cali", null)).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, null, "Lácteos")).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, "Cali", "Frutas")).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, "Dagua", "Lácteos")).isEqualTo("[]");
    }

    @Test
    void consultarCatalogo_conOfertasNoDisponibles_lasExcluye() throws Exception {
        // CAT-10: en Dagua/Frutas hay un ACTIVO con stock, uno INACTIVO, uno AGOTADO y uno ACTIVO con stock 0
        assertThat(idsComprador("Dagua", "Frutas")).containsExactly(daguaFrutas);
        assertThat(idsComprador(null, null)).containsExactlyElementsOf(todasLasDisponibles());

        // Si la única oferta disponible deja de estarlo, la respuesta es []
        Producto mango = productoRepository.findById(daguaFrutas).orElseThrow();
        mango.setEstado(Producto.Estado.AGOTADO);
        productoRepository.saveAndFlush(mango);
        stockAntes = stockActual();

        assertThat(cuerpoOk(tokenComprador, "Dagua", "Frutas")).isEqualTo("[]");
    }

    @Test
    void consultarCatalogo_conVariasOfertas_ordenaPorIdYRespetaContrato() throws Exception {
        // CAT-11
        String cuerpo = cuerpoOk(tokenComprador, null, null);

        List<Long> ids = idsComprador(null, null);
        assertThat(ids).isSorted().doesNotHaveDuplicates();
        List<Map<String, Object>> ofertas = JsonPath.read(cuerpo, "$");
        assertThat(ofertas).allSatisfy(oferta -> {
            assertThat(oferta).containsOnlyKeys(CAMPOS_PUBLICOS);
            assertThat(oferta.values()).doesNotContainNull();
        });
        assertThat(cuerpo).doesNotContain("correo", "password", "hash", "cedula", "documento", "usuario");
    }

    @Test
    void consultarCatalogo_sinToken_retorna401() throws Exception {
        // CAT-12
        consultar(null, "Dagua", "Frutas").andExpect(status().isUnauthorized());

        verify(catalogoService, never()).consultar(any(), any());
    }

    @Test
    void consultarCatalogo_conTokenInvalidoOExpirado_retorna401() throws Exception {
        // CAT-13
        consultar("esto.no.es.un.token", null, null).andExpect(status().isUnauthorized());
        consultar(tokenFirmadoConOtraClave(), null, null).andExpect(status().isUnauthorized());
        consultar(tokenExpirado(), null, null).andExpect(status().isUnauthorized());

        verify(catalogoService, never()).consultar(any(), any());
    }

    @Test
    void consultarCatalogo_conRolAgricultor_retorna200() throws Exception {
        // CAT-14
        assertThat(ids(tokenAgricultor, null, null)).containsExactlyElementsOf(todasLasDisponibles());
        assertThat(ids(tokenAgricultor, "Dagua", "Frutas")).containsExactly(daguaFrutas);
    }

    @Test
    void consultarCatalogo_conNombreNoExacto_noAmpliaLaBusqueda() throws Exception {
        // CAT-15: existe "Tubérculos" (con tilde) en Dagua
        assertThat(idsComprador(null, "Tubérculos")).containsExactly(daguaTuberculos);

        assertThat(cuerpoOk(tokenComprador, null, "Tuberculos")).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, "Dag", null)).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, null, "Frut")).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, "Dag%", null)).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, "%", null)).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, "D_gua", "Frutas")).isEqualTo("[]");
        assertThat(cuerpoOk(tokenComprador, null, "_rutas")).isEqualTo("[]");
    }
}
