package com.agrovalleconnect.agrovalle_connect.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.model.Usuario.Rol;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AutenticacionControllerIntegrationTest.ControllerDePrueba.class)
class AutenticacionControllerIntegrationTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String URL_PROTEGIDA = "/api/v1/test/protegido";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void crearUsuarioDePrueba() {
        Usuario usuario = Usuario.builder()
                .nombre("Ana")
                .correo("ana@correo.com")
                .passwordHash(passwordEncoder.encode("Clave123"))
                .rol(Rol.COMPRADOR)
                .fechaRegistro(LocalDateTime.now())
                .build();
        usuarioRepository.save(usuario);
    }

    @Test
    void login_conCredencialesValidas_retorna200YToken() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"email": "ana@correo.com", "password": "Clave123"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void login_conPasswordIncorrecta_retorna401() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"email": "ana@correo.com", "password": "claveMala"}
                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conEmailInexistente_retorna401() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"email": "nadie@correo.com", "password": "Clave123"}
                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conCamposFaltantes_retorna400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accesoProtegido_sinToken_retorna401() throws Exception {
        mockMvc.perform(get(URL_PROTEGIDA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accesoProtegido_conTokenValido_retorna200() throws Exception {
        String token = jwtUtil.generarToken("ana@correo.com", "COMPRADOR", null);

        mockMvc.perform(get(URL_PROTEGIDA)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // Endpoint de utilería: existe solo durante este test, no en la aplicación real.
    @RestController
    static class ControllerDePrueba {

        @GetMapping(URL_PROTEGIDA)
        String protegido() {
            return "ok";
        }
    }
}