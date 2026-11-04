package com.agrovalleconnect.agrovalle_connect.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * HU-16 / T05: acceso a un endpoint protegido con y sin token (filtro JWT).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccesoProtegidoIntegrationTest {

    private static final String URL_REGISTRO = "/api/v1/auth/register";

    private static final String URL_LOGIN = "/api/v1/auth/login";

    private static final String RUTA_PROTEGIDA = "/api/v1/ruta-protegida-de-prueba";

    private static final String EMAIL = "pedro@mail.com";

    private static final String PASSWORD = "LagordaTaborda@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgricultorRepository agricultorRepository;

    @BeforeEach
    void prepararUsuarioRegistrado() throws Exception {
        agricultorRepository.deleteAll();
        usuarioRepository.deleteAll();

        String registro = """
                {
                  "nombre": "Pedro Gomez",
                  "ubicacion_valle": "Cali",
                  "cedula": "12345678",
                  "correo": "%s",
                  "password": "%s"
                }
                """.formatted(EMAIL, PASSWORD);

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro))
                .andExpect(status().isCreated());
    }

    private String obtenerToken() throws Exception {
        String login = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(EMAIL, PASSWORD);

        String respuesta = mockMvc.perform(post(URL_LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(respuesta, "$.token");
    }

    @Test
    void accesoProtegido_sinToken_retorna401() throws Exception {
        mockMvc.perform(get(RUTA_PROTEGIDA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accesoProtegido_conTokenValido_pasaLaSeguridad() throws Exception {
        String token = obtenerToken();

        // la ruta no existe: un 404 prueba que la seguridad SI dejo pasar
        mockMvc.perform(get(RUTA_PROTEGIDA).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void accesoProtegido_conTokenInvalido_retorna401() throws Exception {
        mockMvc.perform(get(RUTA_PROTEGIDA).header("Authorization", "Bearer esto.no.es.un.token"))
                .andExpect(status().isUnauthorized());
    }
}