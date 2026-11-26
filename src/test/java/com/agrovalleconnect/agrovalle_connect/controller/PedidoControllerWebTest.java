package com.agrovalleconnect.agrovalle_connect.controller;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.dto.CrearPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.ItemPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.ItemPedidoResponse;
import com.agrovalleconnect.agrovalle_connect.dto.PedidoResponse;
import com.agrovalleconnect.agrovalle_connect.exception.AccesoDenegadoException;
import com.agrovalleconnect.agrovalle_connect.exception.MunicipioSinTarifaException;
import com.agrovalleconnect.agrovalle_connect.exception.ProductoNoDisponibleException;
import com.agrovalleconnect.agrovalle_connect.exception.RecursoNoEncontradoException;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import com.agrovalleconnect.agrovalle_connect.service.PedidoService;
import java.math.BigDecimal;
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

/** Cadena JWT y validación reales; las pruebas de persistencia BDD corresponden a T07-HU12. */
@SpringBootTest
@AutoConfigureMockMvc
class PedidoControllerWebTest {

    private static final String CORREO = "comprador-pedidos@test.co";

    private static final String JSON = "{\"items\":[{\"id_producto\":12,\"cantidad\":2.00}]}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtUtil jwt;

    @Value("${jwt.secret}")
    private String secreto;

    @MockitoBean
    private PedidoService servicio;

    private String token(String rol) {
        return jwt.generarToken(CORREO, rol, 7L);
    }

    private ResultActions enviar(String json, String token) throws Exception {
        var peticion = post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(peticion);
    }

    @Test
    void comprador_retorna201ConContratoYUsaCorreoDelToken() throws Exception {
        when(servicio.crear(anyString(), any())).thenReturn(new PedidoResponse(41L, "PENDIENTE_PAGO",
                List.of(new ItemPedidoResponse(12L, new BigDecimal("2.00"), new BigDecimal("3200.00"))),
                new BigDecimal("6400.00"), new BigDecimal("10000.00"), new BigDecimal("16400.00")));
        String cuerpo = JSON.replace("{\"items\"", "{\"comprador_id\":999,\"correo\":\"otro@test.co\",\"items\"");
        enviar(cuerpo, token("COMPRADOR"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", aMapWithSize(6)))
                .andExpect(jsonPath("$.id").value(41))
                .andExpect(jsonPath("$.estado").value("PENDIENTE_PAGO"))
                .andExpect(jsonPath("$.subtotal").value(6400))
                .andExpect(jsonPath("$.costo_flete").value(10000))
                .andExpect(jsonPath("$.total").value(16400))
                .andExpect(jsonPath("$.items[0]", aMapWithSize(3)))
                .andExpect(jsonPath("$.items[0].id_producto").value(12))
                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                .andExpect(jsonPath("$.items[0].precio_unitario").value(3200));
        verify(servicio).crear(CORREO,
                new CrearPedidoRequest(List.of(new ItemPedidoRequest(12L, new BigDecimal("2.00")))));
    }

    @Test
    void sinToken_retorna401SinServicio() throws Exception {
        enviar(JSON, null).andExpect(status().isUnauthorized());
        verifyNoInteractions(servicio);
    }

    @Test
    void tokenInvalidoOExpirado_retorna401SinServicio() throws Exception {
        String expirado = new JwtUtil(secreto, -1000L).generarToken(CORREO, "COMPRADOR", 7L);
        for (String token : List.of("invalido", expirado)) {
            enviar(JSON, token).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(servicio);
    }

    @Test
    void agricultor_retorna403AntesDeValidarCuerpo() throws Exception {
        enviar(JSON, token("AGRICULTOR")).andExpect(status().isForbidden());
        enviar("{invalido", token("AGRICULTOR")).andExpect(status().isForbidden());
        verifyNoInteractions(servicio);
    }

    @Test
    void listaVaciaNulaOElementoNulo_retorna400SinServicio() throws Exception {
        for (String cuerpo : List.of("{}", "{\"items\":[]}", "{\"items\":null}", "{\"items\":[null]}")) {
            enviar(cuerpo, token("COMPRADOR")).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(servicio);
    }

    @Test
    void camposInvalidos_retorna400SinServicio() throws Exception {
        for (String cantidad : List.of("null", "0", "-1", "1.001", "10000000000")) {
            enviar(JSON.replace("2.00", cantidad), token("COMPRADOR")).andExpect(status().isBadRequest());
        }
        for (String id : List.of("null", "0", "-1", "1.5", "\"12\"")) {
            enviar(JSON.replace(":12", ":" + id), token("COMPRADOR")).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(servicio);
    }

    @Test
    void jsonMalFormado_retorna400SinServicio() throws Exception {
        enviar("{invalido", token("COMPRADOR"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.json").exists());
        verifyNoInteractions(servicio);
    }

    @Test
    void compradorSinPerfil_retorna403() throws Exception {
        when(servicio.crear(anyString(), any())).thenThrow(new AccesoDenegadoException("Acceso denegado"));
        enviar(JSON, token("COMPRADOR")).andExpect(status().isForbidden());
    }

    @Test
    void productoInexistente_retorna404() throws Exception {
        when(servicio.crear(anyString(), any())).thenThrow(new RecursoNoEncontradoException("Producto no encontrado"));
        enviar(JSON, token("COMPRADOR")).andExpect(status().isNotFound());
    }

    @Test
    void productoNoDisponible_conserva400EnTexto() throws Exception {
        when(servicio.crear(anyString(), any())).thenThrow(new ProductoNoDisponibleException("Producto no disponible"));
        enviar(JSON, token("COMPRADOR")).andExpect(status().isBadRequest())
                .andExpect(content().string("Producto no disponible"));
    }

    @Test
    void municipioSinTarifa_retorna400ConJsonDelContrato() throws Exception {
        when(servicio.crear(anyString(), any())).thenThrow(new MunicipioSinTarifaException());
        enviar(JSON, token("COMPRADOR")).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", aMapWithSize(1)))
                .andExpect(jsonPath("$.error").value("Municipio sin tarifa de flete"));
    }
}
