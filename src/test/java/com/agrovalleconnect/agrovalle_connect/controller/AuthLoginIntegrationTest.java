package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Escenarios BDD de HU-16 (POST /api/v1/auth/login) — T04-HU16.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthLoginIntegrationTest {

    private static final String URL = "/api/v1/auth/login";

    private static final String EMAIL = "pedro@mail.com";

    private static final String PASSWORD = "Clave@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgricultorRepository agricultorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private Long agricultorId;

    @BeforeEach
    void prepararUsuarioRegistrado() {
        agricultorRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Pedro Gomez")
                .correo(EMAIL)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .rol(Usuario.Rol.AGRICULTOR)
                .fechaRegistro(LocalDateTime.now())
                .build());

        Agricultor agricultor = agricultorRepository.save(Agricultor.builder()
                .cedula("12345678")
                .ubicacionValle("Palmira")
                .usuario(usuario)
                .build());

        agricultorId = agricultor.getId();
    }

    private String json(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    private ResultActions enviar(String cuerpo) throws Exception {
        return mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    private String cuerpoDe(ResultActions resultado) throws Exception {
        return resultado.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void login_conCredencialesValidas_retorna200YToken() throws Exception {
        ResultActions resultado = enviar(json(EMAIL, PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));

        String token = JsonPath.read(cuerpoDe(resultado), "$.token");
        Claims claims = jwtUtil.extraerClaims(token);

        assertThat(jwtUtil.esValido(token)).isTrue();
        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(claims.get("rol", String.class)).isEqualTo("AGRICULTOR");
        assertThat(claims.get("perfil_id", Long.class)).isEqualTo(agricultorId);
    }

    @Test
    void login_conPasswordIncorrecta_retorna401() throws Exception {
        String cuerpo = cuerpoDe(enviar(json(EMAIL, "ClaveMala@1"))
                .andExpect(status().isUnauthorized()));

        assertThat(cuerpo).contains("Credenciales");
    }

    @Test
    void login_conEmailInexistente_retorna401() throws Exception {
        String cuerpo = cuerpoDe(enviar(json("nadie@mail.com", PASSWORD))
                .andExpect(status().isUnauthorized()));

        assertThat(cuerpo).contains("Credenciales");
    }

    @Test
    void login_emailInexistenteYPasswordIncorrecta_respondenElMismoMensaje() throws Exception {
        String porPassword = cuerpoDe(enviar(json(EMAIL, "ClaveMala@1")));
        String porEmail = cuerpoDe(enviar(json("nadie@mail.com", PASSWORD)));

        assertThat(porEmail).isEqualTo(porPassword);
    }

    @Test
    void login_conCamposFaltantes_retorna400() throws Exception {
        String sinPassword = """
                {
                  "email": "pedro@mail.com"
                }
                """;

        enviar(sinPassword)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void login_conEmailMalFormado_retorna400() throws Exception {
        enviar(json("esto-no-es-un-email", PASSWORD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void login_respuestaNoExponeLaContrasena() throws Exception {
        String cuerpo = cuerpoDe(enviar(json(EMAIL, PASSWORD)));

        assertThat(cuerpo).doesNotContain(PASSWORD);
        assertThat(cuerpo).doesNotContain("passwordHash");
    }
}
