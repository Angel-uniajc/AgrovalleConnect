package com.agrovalleconnect.agrovalle_connect.controller;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.dto.OfertaCatalogoResponse;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaResponse;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import com.agrovalleconnect.agrovalle_connect.service.CatalogoService;
import com.agrovalleconnect.agrovalle_connect.service.CosechaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * T04-HU04: GET /api/v1/productos expuesto en el controlador con la cadena de seguridad real.
 * Los servicios se simulan para aislar la capa web; los escenarios CAT-01 a CAT-15 con datos
 * persistidos corresponden a T05-HU04.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerWebTest {

    private static final String URL = "/api/v1/productos";

    private static final String CORREO_COMPRADOR = "comprador-catalogo@mail.com";

    private static final String CORREO_AGRICULTOR = "agricultor-catalogo@mail.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secreto;

    @MockitoBean
    private CatalogoService catalogoService;

    @MockitoBean
    private CosechaService cosechaService;

    private String tokenComprador() {
        return jwtUtil.generarToken(CORREO_COMPRADOR, "COMPRADOR", 1L);
    }

    private String tokenAgricultor() {
        return jwtUtil.generarToken(CORREO_AGRICULTOR, "AGRICULTOR", 2L);
    }

    private ResultActions consultar(MockHttpServletRequestBuilder peticion, String token)
            throws Exception {
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(peticion);
    }

    private OfertaCatalogoResponse mango() {
        return new OfertaCatalogoResponse(12L, "Mango", new BigDecimal("25.50"),
                new BigDecimal("3200.00"), LocalDate.of(2026, 11, 20), "Frutas", 4L, "Dagua");
    }

    @Test
    void consultarCatalogo_conAmbosFiltros_retorna200ConElFormatoJsonDelContrato() throws Exception {
        when(catalogoService.consultar("Dagua", "Frutas")).thenReturn(List.of(mango()));

        consultar(get(URL).param("municipio", "Dagua").param("categoria", "Frutas"),
                tokenComprador())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]", aMapWithSize(8)))
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].tipo").value("Mango"))
                .andExpect(jsonPath("$[0].cantidad_disponible").value(25.50))
                .andExpect(jsonPath("$[0].precio_unitario").value(3200.00))
                .andExpect(jsonPath("$[0].fecha_cosecha").value("2026-11-20"))
                .andExpect(jsonPath("$[0].categoria").value("Frutas"))
                .andExpect(jsonPath("$[0].finca_id").value(4))
                .andExpect(jsonPath("$[0].municipio").value("Dagua"));

        verify(catalogoService).consultar("Dagua", "Frutas");
    }

    @Test
    void consultarCatalogo_sinFiltros_delegaFiltrosAusentes() throws Exception {
        when(catalogoService.consultar(null, null)).thenReturn(List.of(mango()));

        consultar(get(URL), tokenComprador())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(catalogoService).consultar(null, null);
    }

    @Test
    void consultarCatalogo_soloMunicipio_delegaSinCategoria() throws Exception {
        when(catalogoService.consultar("Dagua", null)).thenReturn(List.of(mango()));

        consultar(get(URL).param("municipio", "Dagua"), tokenComprador())
                .andExpect(status().isOk());

        verify(catalogoService).consultar("Dagua", null);
    }

    @Test
    void consultarCatalogo_soloCategoria_delegaSinMunicipio() throws Exception {
        when(catalogoService.consultar(null, "Frutas")).thenReturn(List.of(mango()));

        consultar(get(URL).param("categoria", "Frutas"), tokenComprador())
                .andExpect(status().isOk());

        verify(catalogoService).consultar(null, "Frutas");
    }

    @Test
    void consultarCatalogo_sinCoincidencias_retorna200YArregloVacio() throws Exception {
        when(catalogoService.consultar("Buga", "Granos")).thenReturn(List.of());

        consultar(get(URL).param("municipio", "Buga").param("categoria", "Granos"),
                tokenComprador())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    void consultarCatalogo_conRolAgricultor_retorna200() throws Exception {
        when(catalogoService.consultar(null, null)).thenReturn(List.of());

        consultar(get(URL), tokenAgricultor()).andExpect(status().isOk());

        verify(catalogoService).consultar(null, null);
    }

    @Test
    void consultarCatalogo_sinToken_retorna401() throws Exception {
        consultar(get(URL), null).andExpect(status().isUnauthorized());

        verifyNoInteractions(catalogoService);
    }

    @Test
    void consultarCatalogo_conTokenInvalido_retorna401() throws Exception {
        consultar(get(URL), "token.no.valido").andExpect(status().isUnauthorized());

        verifyNoInteractions(catalogoService);
    }

    @Test
    void consultarCatalogo_conTokenFirmadoConOtraClave_retorna401() throws Exception {
        JwtUtil otraClave = new JwtUtil("otra-clave-distinta-con-mas-de-32-caracteres-987654", 60_000L);
        String tokenAjeno = otraClave.generarToken(CORREO_COMPRADOR, "COMPRADOR", 1L);

        consultar(get(URL), tokenAjeno).andExpect(status().isUnauthorized());

        verifyNoInteractions(catalogoService);
    }

    @Test
    void consultarCatalogo_conTokenExpirado_retorna401() throws Exception {
        JwtUtil expiraYa = new JwtUtil(secreto, -1_000L);
        String tokenExpirado = expiraYa.generarToken(CORREO_COMPRADOR, "COMPRADOR", 1L);

        consultar(get(URL), tokenExpirado).andExpect(status().isUnauthorized());

        verifyNoInteractions(catalogoService);
    }

    private String cosechaValida() {
        return """
                {
                  "finca_id": 1,
                  "categoria_id": 2,
                  "tipo": "Tomate chonto",
                  "cantidad": 100.00,
                  "precio_unitario": 2500.00,
                  "fecha_cosecha": "%s"
                }
                """.formatted(LocalDate.now());
    }

    @Test
    void publicarCosecha_conRolAgricultor_sigueRetornando201() throws Exception {
        when(cosechaService.publicar(eq(CORREO_AGRICULTOR), any(PublicarCosechaRequest.class)))
                .thenReturn(new PublicarCosechaResponse(10L));

        consultar(post(URL).contentType(MediaType.APPLICATION_JSON).content(cosechaValida()),
                tokenAgricultor())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void publicarCosecha_conRolComprador_sigueRetornando403() throws Exception {
        consultar(post(URL).contentType(MediaType.APPLICATION_JSON).content(cosechaValida()),
                tokenComprador())
                .andExpect(status().isForbidden());

        verifyNoInteractions(cosechaService);
    }
}
