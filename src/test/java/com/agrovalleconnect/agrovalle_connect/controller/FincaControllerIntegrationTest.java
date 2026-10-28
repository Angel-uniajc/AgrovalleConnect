package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.FincaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FincaControllerIntegrationTest {

    private static final String URL = "/api/v1/fincas";

    private static final String CORREO = "fincas-bdd@mail.com";

    private static final String PASSWORD = "Clave@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FincaRepository fincaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private String tokenAgricultor;

    private long fincasAntes;

    @BeforeEach
    void prepararAgricultorAutenticado() throws Exception {
        fincasAntes = fincaRepository.count();
        String registro = """
                {
                  "nombre": "Agricultor de prueba",
                  "ubicacion_valle": "Palmira",
                  "cedula": "9988776655",
                  "correo": "%s",
                  "password": "%s"
                }
                """.formatted(CORREO, PASSWORD);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated());
        tokenAgricultor = iniciarSesion(CORREO);
    }

    private String iniciarSesion(String correo) throws Exception {
        String login = """
                {"email": "%s", "password": "%s"}
                """.formatted(correo, PASSWORD);
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.read(respuesta, "$.token");
    }

    private String datosValidos() {
        return """
                {
                  "nombre": "La Esperanza",
                  "municipio": "Palmira",
                  "vereda": "El Palmar",
                  "area_hectareas": 2.50
                }
                """;
    }

    private ResultActions registrar(String cuerpo, String token) throws Exception {
        var peticion = post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(peticion);
    }

    private Finca fincaPersistida(String respuesta) {
        Number id = JsonPath.read(respuesta, "$.id");
        entityManager.flush();
        entityManager.clear();
        return fincaRepository.findById(id.longValue()).orElseThrow();
    }

    @Test
    void registrarFinca_conDatosValidos_retorna201() throws Exception {
        String respuesta = registrar(datosValidos(), tokenAgricultor)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("La Esperanza"))
                .andExpect(jsonPath("$.municipio").value("Palmira"))
                .andExpect(jsonPath("$.vereda").value("El Palmar"))
                .andExpect(jsonPath("$.area_hectareas").value(2.50))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Finca finca = fincaPersistida(respuesta);
        assertThat(fincaRepository.count()).isEqualTo(fincasAntes + 1);
        assertThat(finca.getNombre()).isEqualTo("La Esperanza");
        assertThat(finca.getMunicipio().getNombre()).isEqualTo("Palmira");
        assertThat(finca.getVereda()).isEqualTo("El Palmar");
        assertThat(finca.getAreaHectareas()).isEqualByComparingTo("2.50");
        assertThat(finca.getAgricultor().getUsuario().getCorreo()).isEqualTo(CORREO);
    }

    @Test
    void registrarFinca_conMunicipioNoPermitido_retorna400() throws Exception {
        registrar(datosValidos().replace("Palmira", "Bogotá"), tokenAgricultor)
                .andExpect(status().isBadRequest());

        assertThat(fincaRepository.count()).isEqualTo(fincasAntes);
    }

    @Test
    void registrarFinca_conCamposInvalidos_retorna400() throws Exception {
        String datosInvalidos = datosValidos().replace("La Esperanza", "").replace("2.50", "0");

        registrar(datosInvalidos, tokenAgricultor)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").isNotEmpty())
                .andExpect(jsonPath("$.areaHectareas").isNotEmpty());

        assertThat(fincaRepository.count()).isEqualTo(fincasAntes);
    }

    @Test
    void registrarFinca_sinToken_retorna401() throws Exception {
        registrar(datosValidos(), null).andExpect(status().isUnauthorized());

        assertThat(fincaRepository.count()).isEqualTo(fincasAntes);
    }

    @Test
    void registrarFinca_conRolComprador_retorna403() throws Exception {
        // HU-17 aún no tiene endpoint: se prepara el usuario y se obtiene su JWT con el login real.
        String correoComprador = "comprador-fincas@mail.com";
        usuarioRepository.saveAndFlush(Usuario.builder()
                .nombre("Comprador de prueba")
                .correo(correoComprador)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .rol(Usuario.Rol.COMPRADOR)
                .fechaRegistro(LocalDateTime.now())
                .build());

        registrar(datosValidos(), iniciarSesion(correoComprador)).andExpect(status().isForbidden());

        assertThat(fincaRepository.count()).isEqualTo(fincasAntes);
    }

    @Test
    void registrarFinca_conTokenInvalido_retorna401() throws Exception {
        registrar(datosValidos(), "esto.no.es.un.token").andExpect(status().isUnauthorized());

        assertThat(fincaRepository.count()).isEqualTo(fincasAntes);
    }

    @Test
    void registrarFinca_conMunicipioMinusculasYSinOpcionales_retorna201() throws Exception {
        String respuesta = registrar("""
                {"nombre": "Finca sin opcionales", "municipio": "palmira"}
                """, tokenAgricultor)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.municipio").value("Palmira"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Finca finca = fincaPersistida(respuesta);
        assertThat(fincaRepository.count()).isEqualTo(fincasAntes + 1);
        assertThat(finca.getMunicipio().getNombre()).isEqualTo("Palmira");
        assertThat(finca.getVereda()).isNull();
        assertThat(finca.getAreaHectareas()).isNull();
        assertThat(finca.getAgricultor().getUsuario().getCorreo()).isEqualTo(CORREO);
    }
}
