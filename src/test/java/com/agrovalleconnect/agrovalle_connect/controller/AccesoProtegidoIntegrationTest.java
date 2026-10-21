package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class AuthLoginIntegrationTest {

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
    void prepararEscenario() throws Exception {
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

    private ResultActions enviarLogin(String cuerpo) throws Exception {
        return mockMvc.perform(post(URL_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    private String jsonLogin(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    private String obtenerToken() throws Exception {
        String respuesta = enviarLogin(jsonLogin(EMAIL, PASSWORD))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    @Test
    void login_conCredencialesValidas_retorna200YToken() throws Exception {
        String respuesta = enviarLogin(jsonLogin(EMAIL, PASSWORD))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(respuesta, "$.token");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void login_conPasswordIncorrecta_retorna401() throws Exception {
        enviarLogin(jsonLogin(EMAIL, "ClaveEquivocada@999"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conEmailInexistente_retorna401() throws Exception {
        String cuerpoPasswordMala = enviarLogin(jsonLogin(EMAIL, "ClaveEquivocada@999"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String cuerpoEmailMalo = enviarLogin(jsonLogin("nadie@mail.com", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(cuerpoEmailMalo).isEqualTo(cuerpoPasswordMala);
    }

    @Test
    void login_conCamposFaltantes_retorna400() throws Exception {
        enviarLogin("{\"email\": \"" + EMAIL + "\"}")
                .andExpect(status().isBadRequest());

        enviarLogin("{\"password\": \"" + PASSWORD + "\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void accesoProtegido_sinToken_retorna401() throws Exception {
        mockMvc.perform(get(RUTA_PROTEGIDA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accesoProtegido_conTokenValido_pasaLaSeguridad() throws Exception {
        String token = obtenerToken();

        // la ruta no existe: un 404 prueba que el guardia SI dejo pasar
        mockMvc.perform(get(RUTA_PROTEGIDA).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
