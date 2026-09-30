package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    private static final String URL = "/api/v1/auth/register";

    private static final String PASSWORD = "LagordaTaborda@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgricultorRepository agricultorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiarBodega() {
        agricultorRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private String json(String cedula, String correo) {
        return """
                {
                  "nombre": "Pedro Gomez",
                  "ubicacion_valle": "Cali",
                  "cedula": "%s",
                  "correo": "%s",
                  "password": "%s"
                }
                """.formatted(cedula, correo, PASSWORD);
    }

    private ResultActions enviar(String cuerpo) throws Exception {
        return mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    @Test
    void registroExitoso_persisteYGuardaHashNoTextoPlano() throws Exception {
        MvcResult resultado = enviar(json("12345678", "pedro@mail.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        assertThat(resultado.getResponse().getContentAsString()).doesNotContain(PASSWORD);

        Usuario usuario = usuarioRepository.findByCorreo("pedro@mail.com").orElseThrow();
        assertThat(usuario.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, usuario.getPasswordHash())).isTrue();
        assertThat(agricultorRepository.existsByCedula("12345678")).isTrue();
        assertThat(usuarioRepository.count()).isEqualTo(1);
        assertThat(agricultorRepository.count()).isEqualTo(1);
    }

    @Test
    void rechazaCorreoDuplicado() throws Exception {
        enviar(json("12345678", "pedro@mail.com")).andExpect(status().isCreated());

        enviar(json("87654321", "pedro@mail.com")).andExpect(status().isConflict());

        assertThat(usuarioRepository.count()).isEqualTo(1);
        assertThat(agricultorRepository.count()).isEqualTo(1);
    }

    @Test
    void rechazaCedulaDuplicada() throws Exception {
        enviar(json("12345678", "pedro@mail.com")).andExpect(status().isCreated());

        enviar(json("12345678", "otro@mail.com")).andExpect(status().isConflict());

        assertThat(usuarioRepository.count()).isEqualTo(1);
        assertThat(agricultorRepository.count()).isEqualTo(1);
    }

    @Test
    void contrasenaDebil_devuelve400YNoGuardaNada() throws Exception {
        String cuerpo = """
                {
                  "nombre": "Pedro Gomez",
                  "ubicacion_valle": "Cali",
                  "cedula": "12345678",
                  "correo": "pedro@mail.com",
                  "password": "123"
                }
                """;

        enviar(cuerpo).andExpect(status().isBadRequest());

        assertThat(usuarioRepository.count()).isZero();
        assertThat(agricultorRepository.count()).isZero();
    }
}