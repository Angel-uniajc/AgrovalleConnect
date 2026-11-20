package com.agrovalleconnect.agrovalle_connect.controller;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.dto.MensajeContactoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.MensajeContactoResponse;
import com.agrovalleconnect.agrovalle_connect.exception.AccesoDenegadoException;
import com.agrovalleconnect.agrovalle_connect.exception.ProductoNoDisponibleException;
import com.agrovalleconnect.agrovalle_connect.exception.RecursoNoEncontradoException;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import com.agrovalleconnect.agrovalle_connect.service.CatalogoService;
import com.agrovalleconnect.agrovalle_connect.service.ContactoService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * T04-HU05: POST /api/v1/contacto/mensaje con la cadena de seguridad real y el
 * ManejadorErrores. El servicio se simula para aislar la capa web: si no se invoca,
 * no se guarda ningún mensaje. Los escenarios CON-01 a CON-15 con datos persistidos
 * y comprador registrado corresponden a T05-HU05.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContactoControllerWebTest {

    private static final String URL = "/api/v1/contacto/mensaje";

    private static final String CORREO_COMPRADOR = "comprador-contacto@mail.com";

    private static final String CORREO_AGRICULTOR = "agricultor-contacto@mail.com";

    private static final String MENSAJE = "Buenas tardes, me interesan 20 kg de mango.";

    private static final String JSON_INVALIDO = "El cuerpo de la solicitud no es un JSON válido";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secreto;

    @MockitoBean
    private ContactoService contactoService;

    @MockitoBean
    private CatalogoService catalogoService;

    private String tokenComprador() {
        return jwtUtil.generarToken(CORREO_COMPRADOR, "COMPRADOR", 1L);
    }

    private String tokenAgricultor() {
        return jwtUtil.generarToken(CORREO_AGRICULTOR, "AGRICULTOR", 2L);
    }

    private static String cuerpo(String idProducto, String mensaje) {
        return """
                {"id_producto": %s, "mensaje": %s}
                """.formatted(idProducto, mensaje);
    }

    private static String cuerpoValido() {
        return cuerpo("12", "\"" + MENSAJE + "\"");
    }

    private ResultActions enviar(String json, String token) throws Exception {
        var peticion = post(URL).contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(peticion);
    }

    private void respondeConExito() {
        when(contactoService.enviar(eq(CORREO_COMPRADOR), any(MensajeContactoRequest.class)))
                .thenReturn(new MensajeContactoResponse(
                        7L, 12L, LocalDateTime.of(2026, 11, 20, 15, 30)));
    }

    // ---------- Camino exitoso ----------

    @Test
    void enviarContacto_conDatosValidos_retorna200ConElFormatoDelContrato() throws Exception {
        respondeConExito();

        enviar(cuerpoValido(), tokenComprador())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", aMapWithSize(3)))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.id_producto").value(12))
                .andExpect(jsonPath("$.fecha_envio").value("2026-11-20T15:30:00"));

        verify(contactoService).enviar(CORREO_COMPRADOR, new MensajeContactoRequest(12L, MENSAJE));
    }

    @Test
    void enviarContacto_conCompradorYDestinatarioEnElCuerpo_usaSoloElCompradorDelToken()
            throws Exception {
        respondeConExito();
        String suplantacion = """
                {
                  "id_producto": 12,
                  "mensaje": "%s",
                  "comprador_id": 999,
                  "agricultor_id": 888,
                  "correo": "otro@mail.com",
                  "fecha_envio": "2020-01-01T00:00:00"
                }
                """.formatted(MENSAJE);

        enviar(suplantacion, tokenComprador()).andExpect(status().isOk());

        ArgumentCaptor<String> correo = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MensajeContactoRequest> request =
                ArgumentCaptor.forClass(MensajeContactoRequest.class);
        verify(contactoService).enviar(correo.capture(), request.capture());
        assertEquals(CORREO_COMPRADOR, correo.getValue());
        assertEquals(new MensajeContactoRequest(12L, MENSAJE), request.getValue());
    }

    @Test
    void enviarContacto_conMensajeDe2000Caracteres_retorna200() throws Exception {
        respondeConExito();

        enviar(cuerpo("12", "\"" + "a".repeat(2000) + "\""), tokenComprador())
                .andExpect(status().isOk());
    }

    // ---------- Validación del cuerpo: 400 ----------

    @Test
    void enviarContacto_conMensajeVacioOSoloEspacios_retorna400() throws Exception {
        for (String mensaje : List.of("null", "\"\"", "\"   \"")) {
            enviar(cuerpo("12", mensaje), tokenComprador())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensaje").value("El mensaje es obligatorio"));
        }

        verifyNoInteractions(contactoService);
    }

    @Test
    void enviarContacto_conMensajeDe2001Caracteres_retorna400() throws Exception {
        enviar(cuerpo("12", "\"" + "a".repeat(2001) + "\""), tokenComprador())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El mensaje no puede superar 2000 caracteres"));

        verifyNoInteractions(contactoService);
    }

    @Test
    void enviarContacto_conIdProductoNuloCeroONegativo_retorna400() throws Exception {
        for (String id : List.of("null", "0", "-5")) {
            enviar(cuerpo(id, "\"" + MENSAJE + "\""), tokenComprador())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.idProducto").isNotEmpty());
        }

        verifyNoInteractions(contactoService);
    }

    @Test
    void enviarContacto_conJsonMalFormado_retorna400ConElMapaJson() throws Exception {
        for (String json : List.of("{\"id_producto\": 12, \"mensaje\": ",
                cuerpo("\"abc\"", "\"" + MENSAJE + "\""))) {
            enviar(json, tokenComprador())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$", aMapWithSize(1)))
                    .andExpect(jsonPath("$.json").value(JSON_INVALIDO));
        }

        verifyNoInteractions(contactoService);
    }

    // ---------- Reglas del servicio traducidas por ManejadorErrores ----------

    @Test
    void enviarContacto_conProductoNoDisponible_retorna400() throws Exception {
        when(contactoService.enviar(anyString(), any(MensajeContactoRequest.class)))
                .thenThrow(new ProductoNoDisponibleException(
                        "El producto no está disponible para contacto"));

        enviar(cuerpoValido(), tokenComprador())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El producto no está disponible para contacto"));
    }

    @Test
    void enviarContacto_conProductoInexistente_retorna404() throws Exception {
        when(contactoService.enviar(anyString(), any(MensajeContactoRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado"));

        enviar(cuerpoValido(), tokenComprador())
                .andExpect(status().isNotFound())
                .andExpect(content().string("Producto no encontrado"));
    }

    @Test
    void enviarContacto_conCompradorSinPerfil_retorna403Generico() throws Exception {
        when(contactoService.enviar(anyString(), any(MensajeContactoRequest.class)))
                .thenThrow(new AccesoDenegadoException("Acceso denegado"));

        enviar(cuerpoValido(), tokenComprador())
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acceso denegado"));
    }

    // ---------- Seguridad: 401 y 403 ----------

    @Test
    void enviarContacto_sinToken_retorna401() throws Exception {
        enviar(cuerpoValido(), null).andExpect(status().isUnauthorized());

        verifyNoInteractions(contactoService);
    }

    @Test
    void enviarContacto_conTokenInvalido_retorna401() throws Exception {
        enviar(cuerpoValido(), "token.no.valido").andExpect(status().isUnauthorized());

        verifyNoInteractions(contactoService);
    }

    @Test
    void enviarContacto_conTokenExpirado_retorna401() throws Exception {
        String expirado = new JwtUtil(secreto, -1_000L)
                .generarToken(CORREO_COMPRADOR, "COMPRADOR", 1L);

        enviar(cuerpoValido(), expirado).andExpect(status().isUnauthorized());

        verifyNoInteractions(contactoService);
    }

    @Test
    void enviarContacto_conRolAgricultor_retorna403() throws Exception {
        enviar(cuerpoValido(), tokenAgricultor()).andExpect(status().isForbidden());

        verifyNoInteractions(contactoService);
    }

    @Test
    void enviarContacto_conRolAgricultorYJsonMalFormado_rechazaPorRolAntesDeLeerElCuerpo()
            throws Exception {
        enviar("{no es json", tokenAgricultor()).andExpect(status().isForbidden());

        verifyNoInteractions(contactoService);
    }

    // ---------- Regresión de las demás reglas de SecurityConfig ----------

    @Test
    void login_sigueSiendoPublico() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void catalogo_conRolComprador_sigueRetornando200() throws Exception {
        when(catalogoService.consultar(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/productos")
                        .header("Authorization", "Bearer " + tokenComprador()))
                .andExpect(status().isOk());
    }

    @Test
    void publicarProductoYRegistrarFinca_conRolComprador_sigueRetornando403() throws Exception {
        for (String ruta : List.of("/api/v1/productos", "/api/v1/fincas")) {
            mockMvc.perform(post(ruta)
                            .header("Authorization", "Bearer " + tokenComprador())
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }
    }
}
