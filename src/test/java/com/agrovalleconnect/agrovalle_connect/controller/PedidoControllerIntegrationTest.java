package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Pedido;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.repository.CategoriaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.PedidoRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * T07-HU12: escenarios BDD de docs/contrato-pedido-hu12.md con MockMvc y H2.
 * Cada prueba crea sus propios datos con los endpoints reales (HU-01, HU-14, HU-02 y HU-17)
 * y @Transactional los deshace al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoControllerIntegrationTest {

    private static final String URL = "/api/v1/pedidos";

    private static final String PASSWORD = "Clave@123";

    private static final long ID_INEXISTENTE = 999_999L;

    private static final String STOCK_INICIAL = "10.00";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EntityManager entityManager;

    private String tokenComprador;

    private String tokenAgricultor;

    private long productoId;

    private long pedidosAntes;

    @BeforeEach
    void prepararAgricultorProductoYComprador() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);

        tokenAgricultor = registrarAgricultorEIniciarSesion("agricultor-" + sufijo + "@pedidos.test");
        long fincaId = registrarFinca(tokenAgricultor);
        long categoriaId = categoriaRepository.saveAndFlush(
                Categoria.builder().nombre("Categoría pedidos " + sufijo).build()).getId();
        productoId = publicarCosecha(tokenAgricultor, fincaId, categoriaId);

        tokenComprador = registrarCompradorEIniciarSesion("comprador-" + sufijo + "@pedidos.test");

        pedidosAntes = pedidoRepository.count();
    }

    // ---------- Datos de prueba creados con los flujos reales ----------

    private String documentoAleatorio() {
        return String.format("%010d", (long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
    }

    private String registrarAgricultorEIniciarSesion(String correo) throws Exception {
        String registro = """
                {
                  "nombre": "Agricultor de pedidos",
                  "ubicacion_valle": "Palmira",
                  "cedula": "%s",
                  "correo": "%s",
                  "password": "%s"
                }
                """.formatted(documentoAleatorio(), correo, PASSWORD);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated());
        return iniciarSesion(correo);
    }

    /** HU-17: el comprador se registra y entra igual que en la aplicación real. */
    private String registrarCompradorEIniciarSesion(String correo) throws Exception {
        String registro = """
                {
                  "nombre": "Comprador de pedidos",
                  "rol": "COMPRADOR",
                  "tipo_comprador": "individual",
                  "documento": "%s",
                  "correo": "%s",
                  "password": "%s"
                }
                """.formatted(documentoAleatorio(), correo, PASSWORD);
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

    /** La finca queda en Palmira, cuya tarifa de flete es 10 000.00. */
    private long registrarFinca(String token) throws Exception {
        String finca = """
                {"nombre": "Finca de pedidos", "municipio": "Palmira"}
                """;
        String respuesta = mockMvc.perform(post("/api/v1/fincas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(finca))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Number id = JsonPath.read(respuesta, "$.id");
        return id.longValue();
    }

    /** Producto ACTIVO con 10.00 unidades a 2500.00 cada una. */
    private long publicarCosecha(String token, long fincaId, long categoriaId) throws Exception {
        String cosecha = """
                {
                  "finca_id": %d,
                  "categoria_id": %d,
                  "tipo": "Tomate chonto",
                  "cantidad": %s,
                  "precio_unitario": 2500.00,
                  "fecha_cosecha": "%s"
                }
                """.formatted(fincaId, categoriaId, STOCK_INICIAL, LocalDate.now());
        String respuesta = mockMvc.perform(post("/api/v1/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cosecha))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Number id = JsonPath.read(respuesta, "$.id");
        return id.longValue();
    }

    // ---------- Ayudantes de la petición y de las verificaciones ----------

    private String pedido(long idProducto, String cantidad) {
        return """
                {"items": [{"id_producto": %d, "cantidad": %s}]}
                """.formatted(idProducto, cantidad);
    }

    private ResultActions crearPedido(String cuerpo, String token) throws Exception {
        var peticion = post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(peticion);
    }

    /** Vuelve a leer desde H2 para no confiar en la copia en memoria de JPA. */
    private Producto productoDesdeBaseDeDatos() {
        entityManager.clear();
        return productoRepository.findById(productoId).orElseThrow();
    }

    /** Regla de los rechazos: ni pedido nuevo ni stock descontado. */
    private void verificarSinPedidoYStockIntacto() {
        assertThat(pedidoRepository.count()).isEqualTo(pedidosAntes);
        assertThat(productoDesdeBaseDeDatos().getCantidadDisponible()).isEqualByComparingTo(STOCK_INICIAL);
    }

    // ---------- Escenarios BDD ----------

    /** PED-01: 201, pedido PENDIENTE_PAGO, flete de Palmira y stock descontado. */
    @Test
    void ped01_crearPedidoValido_retorna201ConFleteYDescuentaStock() throws Exception {
        String respuesta = crearPedido(pedido(productoId, "4.00"), tokenComprador)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_PAGO"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id_producto").value(productoId))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(new BigDecimal(JsonPath.read(respuesta, "$.subtotal").toString()))
                .isEqualByComparingTo("10000.00");
        assertThat(new BigDecimal(JsonPath.read(respuesta, "$.costo_flete").toString()))
                .isEqualByComparingTo("10000.00");
        assertThat(new BigDecimal(JsonPath.read(respuesta, "$.total").toString()))
                .isEqualByComparingTo("20000.00");

        entityManager.flush();
        Producto producto = productoDesdeBaseDeDatos();
        assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("6.00");
        assertThat(producto.getEstado()).isEqualTo(Producto.Estado.ACTIVO);

        Number id = JsonPath.read(respuesta, "$.id");
        Pedido guardado = pedidoRepository.findById(id.longValue()).orElseThrow();
        assertThat(pedidoRepository.count()).isEqualTo(pedidosAntes + 1);
        assertThat(guardado.getEstado()).isEqualTo(Pedido.Estado.PENDIENTE_PAGO);
        assertThat(guardado.getCostoFlete()).isEqualByComparingTo("10000.00");
        assertThat(guardado.getDetalles()).hasSize(1);
    }

    /** PED-02: sin token responde 401 y no toca nada. */
    @Test
    void ped02_crearPedidoSinToken_retorna401() throws Exception {
        crearPedido(pedido(productoId, "4.00"), null)
                .andExpect(status().isUnauthorized());

        verificarSinPedidoYStockIntacto();
    }

    /** PED-03: un agricultor no puede comprar. */
    @Test
    void ped03_crearPedidoConTokenDeAgricultor_retorna403() throws Exception {
        crearPedido(pedido(productoId, "4.00"), tokenAgricultor)
                .andExpect(status().isForbidden());

        verificarSinPedidoYStockIntacto();
    }

    /** PED-05: la lista de productos no puede llegar vacía. */
    @Test
    void ped05_crearPedidoConListaVacia_retorna400() throws Exception {
        crearPedido("{\"items\": []}", tokenComprador)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.items").value("El pedido debe incluir al menos un producto"));

        verificarSinPedidoYStockIntacto();
    }

    /** PED-06: un id_producto que no existe responde 404. */
    @Test
    void ped06_crearPedidoConProductoInexistente_retorna404() throws Exception {
        crearPedido(pedido(ID_INEXISTENTE, "1.00"), tokenComprador)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Producto no encontrado"));

        verificarSinPedidoYStockIntacto();
    }

    /** PED-07: un producto INACTIVO con stock positivo no se puede pedir. */
    @Test
    void ped07_crearPedidoConProductoInactivo_retorna400() throws Exception {
        Producto producto = productoRepository.findById(productoId).orElseThrow();
        producto.setEstado(Producto.Estado.INACTIVO);
        productoRepository.saveAndFlush(producto);

        crearPedido(pedido(productoId, "1.00"), tokenComprador)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Producto no disponible"));

        verificarSinPedidoYStockIntacto();
        assertThat(productoDesdeBaseDeDatos().getEstado()).isEqualTo(Producto.Estado.INACTIVO);
    }
}
