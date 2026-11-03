package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.repository.CategoriaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductoControllerIntegrationTest {

    private static final String URL = "/api/v1/productos";

    private static final String PASSWORD = "Clave@123";

    private static final long ID_INEXISTENTE = 999_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EntityManager entityManager;

    private String tokenAgricultor;

    private long fincaPropiaId;

    private long fincaAjenaId;

    private long categoriaId;

    private long productosAntes;

    @BeforeEach
    void prepararAgricultoresFincasYCategoria() throws Exception {
        tokenAgricultor = registrarEIniciarSesion("cosechas-bdd@mail.com", "9988776651");
        fincaPropiaId = registrarFinca(tokenAgricultor, "La Esperanza");

        String tokenOtroAgricultor = registrarEIniciarSesion("vecino-bdd@mail.com", "9988776652");
        fincaAjenaId = registrarFinca(tokenOtroAgricultor, "El Vecino");

        categoriaId = categoriaRepository.saveAndFlush(
                Categoria.builder().nombre("Categoría de pruebas HU-02").build()).getId();

        productosAntes = productoRepository.count();
    }

    private String registrarEIniciarSesion(String correo, String cedula) throws Exception {
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

        String login = """
                {"email": "%s", "password": "%s"}
                """.formatted(correo, PASSWORD);
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(respuesta, "$.token");
    }

    private long registrarFinca(String token, String nombre) throws Exception {
        String finca = """
                {"nombre": "%s", "municipio": "Palmira"}
                """.formatted(nombre);
        String respuesta = mockMvc.perform(post("/api/v1/fincas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(finca))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Number id = JsonPath.read(respuesta, "$.id");
        return id.longValue();
    }

    private String cosecha(long fincaId, long categoriaId, String cantidad, LocalDate fecha) {
        return """
                {
                  "finca_id": %d,
                  "categoria_id": %d,
                  "tipo": "Tomate chonto",
                  "cantidad": %s,
                  "precio_unitario": 2500.00,
                  "fecha_cosecha": "%s"
                }
                """.formatted(fincaId, categoriaId, cantidad, fecha);
    }

    private String cosechaValida() {
        return cosecha(fincaPropiaId, categoriaId, "100.00", LocalDate.now());
    }

    private ResultActions publicar(String cuerpo, String token) throws Exception {
        var peticion = post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(peticion);
    }

    @Test
    void publicarCosecha_conDatosValidos_retorna201YId() throws Exception {
        String respuesta = publicar(cosechaValida(), tokenAgricultor)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Number id = JsonPath.read(respuesta, "$.id");
        entityManager.flush();
        entityManager.clear();
        Producto producto = productoRepository.findById(id.longValue()).orElseThrow();
        assertThat(productoRepository.count()).isEqualTo(productosAntes + 1);
        assertThat(producto.getTipo()).isEqualTo("Tomate chonto");
        assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("100.00");
        assertThat(producto.getPrecioUnitario()).isEqualByComparingTo("2500.00");
        assertThat(producto.getFechaCosecha()).isEqualTo(LocalDate.now());
        assertThat(producto.getEstado()).isEqualTo(Producto.Estado.ACTIVO);
        assertThat(producto.getFinca().getId()).isEqualTo(fincaPropiaId);
        assertThat(producto.getCategoria().getId()).isEqualTo(categoriaId);
    }

    @Test
    void publicarCosecha_conFechaPasada_retorna400() throws Exception {
        String cuerpo = cosecha(fincaPropiaId, categoriaId, "100.00", LocalDate.now().minusDays(1));

        publicar(cuerpo, tokenAgricultor)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fechaCosecha").isNotEmpty());

        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void publicarCosecha_conCantidadInvalida_retorna400() throws Exception {
        String cuerpo = cosecha(fincaPropiaId, categoriaId, "0", LocalDate.now());

        publicar(cuerpo, tokenAgricultor)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cantidad").isNotEmpty());

        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void publicarCosecha_conCategoriaInexistente_retorna400() throws Exception {
        String cuerpo = cosecha(fincaPropiaId, ID_INEXISTENTE, "100.00", LocalDate.now());

        publicar(cuerpo, tokenAgricultor).andExpect(status().isBadRequest());

        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void publicarCosecha_sinToken_retorna401() throws Exception {
        publicar(cosechaValida(), null).andExpect(status().isUnauthorized());

        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void publicarCosecha_enFincaAjena_retorna403() throws Exception {
        String cuerpo = cosecha(fincaAjenaId, categoriaId, "100.00", LocalDate.now());

        publicar(cuerpo, tokenAgricultor)
                .andExpect(status().isForbidden())
                .andExpect(content().string("La finca no pertenece al agricultor autenticado"));

        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }

    @Test
    void publicarCosecha_conFincaInexistente_retorna404() throws Exception {
        String cuerpo = cosecha(ID_INEXISTENTE, categoriaId, "100.00", LocalDate.now());

        publicar(cuerpo, tokenAgricultor)
                .andExpect(status().isNotFound())
                .andExpect(content().string("Finca no encontrada"));

        assertThat(productoRepository.count()).isEqualTo(productosAntes);
    }
}
