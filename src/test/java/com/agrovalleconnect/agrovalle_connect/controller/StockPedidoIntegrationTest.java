package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.event.PedidoCreadoEvent;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** HU-08: HTTP, seguridad, servicio, eventos y persistencia reales, sin transacción de prueba ni mocks. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:stock_hu08;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@RecordApplicationEvents
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StockPedidoIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transacciones;

    @Autowired
    private ProductoRepository productos;

    @Autowired
    private ApplicationEvents eventos;

    private String token;

    private Long fincaId;

    private Long categoriaId;

    private final Map<Long, Producto> originales = new LinkedHashMap<>();

    @BeforeEach
    void prepararDatosPropiosYCompradorReal() throws Exception {
        // Base H2 exclusiva de esta clase. La limpieza respeta las claves foráneas.
        for (String tabla : new String[]{"mensaje_contacto", "detalle_pedido", "pedido", "producto", "finca",
                "comprador", "agricultor", "usuario", "categoria", "municipio"}) {
            jdbc.update("delete from " + tabla);
        }
        originales.clear();
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombre":"Comprador stock","correo":"stock@test.com","password":"Clave@123",
                 "rol":"COMPRADOR","tipo_comprador":"individual","documento":"1234567890"}
                """)).andExpect(status().isCreated());
        String login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"stock@test.com\",\"password\":\"Clave@123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        token = JsonPath.read(login, "$.token");
        new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
            Usuario usuario = Usuario.builder().nombre("Agricultor stock").correo("finca@test.com")
                    .passwordHash("hash-fixture").rol(Usuario.Rol.AGRICULTOR)
                    .fechaRegistro(LocalDateTime.now()).build();
            em.persist(usuario);
            Agricultor agricultor = Agricultor.builder().usuario(usuario).cedula("9876543210")
                    .ubicacionValle("Palmira").build();
            em.persist(agricultor);
            Municipio municipio = Municipio.builder().nombre("Palmira").build();
            em.persist(municipio);
            Categoria categoria = Categoria.builder().nombre("Fruta de prueba").build();
            em.persist(categoria);
            categoriaId = categoria.getId();
            Finca finca = Finca.builder().nombre("Finca stock").municipio(municipio).agricultor(agricultor).build();
            em.persist(finca);
            fincaId = finca.getId();
        });
        eventos.clear();
    }

    private Long producto(String stock, Producto.Estado estado) {
        Producto producto = productos.saveAndFlush(Producto.builder().tipo("Mango")
                .finca(em.getReference(Finca.class, fincaId)).categoria(em.getReference(Categoria.class, categoriaId))
                .precioUnitario(new BigDecimal("3200.00")).cantidadDisponible(new BigDecimal(stock))
                .fechaCosecha(LocalDate.now()).estado(estado).build());
        originales.put(producto.getId(), producto);
        return producto.getId();
    }

    private String item(Long id, String cantidad) {
        return "{\"id_producto\":%d,\"cantidad\":%s}".formatted(id, cantidad);
    }

    private ResultActions pedir(String... items) throws Exception {
        return mvc.perform(post("/api/v1/pedidos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[" + String.join(",", items) + "]}"));
    }

    private void insuficiente(Long id, String solicitada, String disponible, String... items) throws Exception {
        ResultActions resultado = pedir(items).andExpect(status().isBadRequest());
        sinCambios();
        resultado.andExpect(jsonPath("$", aMapWithSize(4)))
                .andExpect(jsonPath("$.error").value("Stock insuficiente"))
                .andExpect(jsonPath("$.id_producto").value(id))
                .andExpect(jsonPath("$.cantidad_solicitada").value(Double.parseDouble(solicitada)))
                .andExpect(jsonPath("$.stock_disponible").value(Double.parseDouble(disponible)));
    }

    private void agotado(Long id) throws Exception {
        ResultActions resultado = pedir(item(id, "1.00")).andExpect(status().isBadRequest());
        sinCambios();
        resultado.andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.error").value("Producto agotado"))
                .andExpect(jsonPath("$.id_producto").value(id));
    }

    private void sinCambios() {
        assertThat(jdbc.queryForObject("select count(*) from pedido", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from detalle_pedido", Long.class)).isZero();
        originales.forEach((id, original) -> {
            Producto actual = productos.findById(id).orElseThrow();
            assertThat(actual.getCantidadDisponible()).isEqualByComparingTo(original.getCantidadDisponible());
            assertThat(actual.getEstado()).isEqualTo(original.getEstado());
        });
        assertThat(eventos.stream(PedidoCreadoEvent.class)).isEmpty();
    }

    private void exitoso(Long id, String cantidad, String restante, String... items) throws Exception {
        String cuerpo = pedir(items).andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_PAGO"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id_producto").value(id))
                .andExpect(jsonPath("$.items[0].cantidad").value(Double.parseDouble(cantidad)))
                .andReturn().getResponse().getContentAsString();
        long pedidoId = ((Number) JsonPath.read(cuerpo, "$.id")).longValue();
        assertThat(jdbc.queryForObject("select count(*) from pedido", Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("select count(*) from detalle_pedido", Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("select cantidad from detalle_pedido where pedido_id = ? and producto_id = ?",
                BigDecimal.class, pedidoId, id)).isEqualByComparingTo(cantidad);
        assertThat(jdbc.queryForObject("select estado from pedido where id = ?", String.class, pedidoId))
                .isEqualTo("PENDIENTE_PAGO");
        Producto actual = productos.findById(id).orElseThrow();
        assertThat(actual.getCantidadDisponible()).isEqualByComparingTo(restante);
        assertThat(actual.getEstado()).isEqualTo(new BigDecimal(restante).signum() == 0
                ? Producto.Estado.AGOTADO : Producto.Estado.ACTIVO);
        assertThat(eventos.stream(PedidoCreadoEvent.class).map(PedidoCreadoEvent::pedidoId))
                .containsExactly(pedidoId);
    }

    @Test
    @DisplayName("STK-01 — Cantidad superior al stock")
    void stk01_cantidadSuperiorAlStock() throws Exception {
        Long id = producto("10.00", Producto.Estado.ACTIVO);
        insuficiente(id, "16.00", "10.00", item(id, "16.00"));
    }

    @Test
    @DisplayName("STK-02 — Cantidad igual al stock")
    void stk02_cantidadIgualAlStock() throws Exception {
        Long id = producto("10.00", Producto.Estado.ACTIVO);
        exitoso(id, "10.00", "0.00", item(id, "10.00"));
    }

    @Test
    @DisplayName("STK-03 — Cantidad menor al stock")
    void stk03_cantidadMenorAlStock() throws Exception {
        Long id = producto("10.00", Producto.Estado.ACTIVO);
        exitoso(id, "4.00", "6.00", item(id, "4.00"));
    }

    @Test
    @DisplayName("STK-04 — Stock en cero con estado ACTIVO")
    void stk04_stockEnCeroConEstadoActivo() throws Exception {
        agotado(producto("0.00", Producto.Estado.ACTIVO));
    }

    @Test
    @DisplayName("STK-05 — Producto en estado AGOTADO")
    void stk05_productoEnEstadoAgotado() throws Exception {
        agotado(producto("10.00", Producto.Estado.AGOTADO));
    }

    @Test
    @DisplayName("STK-06 — Producto INACTIVO con stock en cero")
    void stk06_productoInactivoConStockEnCero() throws Exception {
        agotado(producto("0.00", Producto.Estado.INACTIVO));
    }

    @Test
    @DisplayName("STK-07 — Pedido mixto: una línea válida y otra insuficiente")
    void stk07_pedidoMixtoSinReservasParciales() throws Exception {
        Long valido = producto("10.00", Producto.Estado.ACTIVO);
        Long insuficiente = producto("3.00", Producto.Estado.ACTIVO);
        insuficiente(insuficiente, "5.00", "3.00", item(valido, "2.00"), item(insuficiente, "5.00"));
    }

    @Test
    @DisplayName("STK-08 — Varias líneas rechazadas")
    void stk08_variasLineasRechazadas() throws Exception {
        Long agotado = producto("0.00", Producto.Estado.AGOTADO);
        Long insuficiente = producto("3.00", Producto.Estado.ACTIVO);
        insuficiente(insuficiente, "5.00", "3.00", item(insuficiente, "5.00"), item(agotado, "1.00"));
    }

    @Test
    @DisplayName("STK-09 — Producto repetido cuya suma supera el stock")
    void stk09_productoRepetidoSuperaStock() throws Exception {
        Long id = producto("10.00", Producto.Estado.ACTIVO);
        insuficiente(id, "12.00", "10.00", item(id, "6.00"), item(id, "6.00"));
    }

    @Test
    @DisplayName("STK-10 — Producto repetido cuya suma alcanza el stock")
    void stk10_productoRepetidoAlcanzaStock() throws Exception {
        Long id = producto("10.00", Producto.Estado.ACTIVO);
        exitoso(id, "10.00", "0.00", item(id, "5.00"), item(id, "5.00"));
    }

    @Test
    @DisplayName("STK-11 — Frontera decimal")
    void stk11_fronteraDecimal() throws Exception {
        Long id = producto("10.00", Producto.Estado.ACTIVO);
        insuficiente(id, "10.01", "10.00", item(id, "10.01"));
    }
}
