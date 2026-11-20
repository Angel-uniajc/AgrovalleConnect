package com.agrovalleconnect.agrovalle_connect.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
    private CompradorRepository compradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @AfterEach
    void limpiarBodega() {
        compradorRepository.deleteAll();
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

    private String comprador(String tipo, String documento, String correo) {
        return """
                {"nombre":"La Ceiba","rol":"COMPRADOR","correo":"%s","password":"%s",
                 "tipo_comprador":"%s","documento":"%s"}
                """.formatted(correo, PASSWORD, tipo, documento);
    }

    @Test
    void registrarComprador_valido_retorna201ConLosCuatroCamposPublicos() throws Exception {
        enviar(comprador("restaurante", "900123456", "compras@ceiba.co"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$.nombre").value("La Ceiba"))
                .andExpect(jsonPath("$.rol").value("COMPRADOR"))
                .andExpect(jsonPath("$.tipo_comprador").value("RESTAURANTE"))
                .andExpect(jsonPath("$.mensaje").value("Comprador registrado exitosamente"));

        Usuario usuario = usuarioRepository.findByCorreo("compras@ceiba.co").orElseThrow();
        Comprador perfil = compradorRepository.findByUsuarioCorreo("compras@ceiba.co").orElseThrow();
        assertThat(usuario.getRol()).isEqualTo(Usuario.Rol.COMPRADOR);
        assertThat(passwordEncoder.matches(PASSWORD, usuario.getPasswordHash())).isTrue();
        assertThat(perfil.getDocumento()).isEqualTo("900123456");
        assertThat(perfil.getTipoComprador()).isEqualTo(Comprador.TipoComprador.RESTAURANTE);
        verificarConteos(1, 1);
    }

    private void verificarConteos(long usuarios, long compradores) {
        assertThat(usuarioRepository.count()).isEqualTo(usuarios);
        assertThat(compradorRepository.count()).isEqualTo(compradores);
    }

    @Test
    void registrarComprador_tipoInvalido_retorna400() throws Exception {
        enviar(comprador("otro", "900123456", "compras@ceiba.co"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipoComprador").isString());
        verificarConteos(0, 0);
    }

    @Test
    void registrar_rolDesconocido_retorna400() throws Exception {
        String cuerpo = json("12345678", "pedro@mail.com").replace("{", "{\"rol\":\"ADMIN\",");
        enviar(cuerpo).andExpect(status().isBadRequest()).andExpect(jsonPath("$.rol").isString());
        verificarConteos(0, 0);
    }

    @Test
    void registrar_jsonIncompleto_retornaSoloMensajePublico() throws Exception {
        enviar("{\"password\":\"" + PASSWORD + "\"")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.json").value("El cuerpo de la solicitud no es un JSON válido"));
        verificarConteos(0, 0);
    }

    @Test
    void registrarComprador_correoODocumentoDuplicado_retorna409() throws Exception {
        enviar(comprador("individual", "900123456", "compras@ceiba.co")).andExpect(status().isCreated());
        verificarConteos(1, 1);
        enviar(comprador("individual", "900654321", "compras@ceiba.co")).andExpect(status().isConflict());
        verificarConteos(1, 1);
        enviar(comprador("individual", "900123456", "otro@ceiba.co")).andExpect(status().isConflict());
        verificarConteos(1, 1);
    }

    @Test
    void registrarAgricultor_conRolExplicito_persistePerfilDeAgricultor() throws Exception {
        String cuerpo = json("12345678", "pedro@mail.com").replace("{", "{\"rol\":\"AGRICULTOR\",");
        enviar(cuerpo).andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pedro Gomez"))
                .andExpect(jsonPath("$.mensaje").isString())
                .andExpect(jsonPath("$.length()").value(2));
        assertThat(agricultorRepository.existsByCedula("12345678")).isTrue();
        verificarConteos(1, 0);
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
    @Test
    void registrarAgricultor_conCamposVacios_retorna400() throws Exception {
        String cuerpo = """
                {
                  "nombre": "",
                  "ubicacion_valle": "Cali",
                  "cedula": "",
                  "correo": "pedro@mail.com",
                  "password": "%s"
                }
                """.formatted(PASSWORD);

        enviar(cuerpo).andExpect(status().isBadRequest());

        assertThat(usuarioRepository.count()).isZero();
        assertThat(agricultorRepository.count()).isZero();
    }

    @Test
    void registrarAgricultor_conCedulaInvalida_retorna400() throws Exception {
        enviar(json("12ab56", "pedro@mail.com")).andExpect(status().isBadRequest());

        assertThat(usuarioRepository.count()).isZero();
        assertThat(agricultorRepository.count()).isZero();
    }

    @Test
    void registrarComprador_conCamposFaltantes_retorna400() throws Exception {
        enviar("{\"rol\":\"COMPRADOR\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").isString())
                .andExpect(jsonPath("$.correo").isString())
                .andExpect(jsonPath("$.password").isString())
                .andExpect(jsonPath("$.tipoComprador").isString())
                .andExpect(jsonPath("$.documento").isString());
        verificarConteos(0, 0);
    }

    @Test
    void registrarComprador_conPasswordInvalida_retorna400() throws Exception {
        for (String debil : List.of("123", "Segura#2026")) {
            enviar(comprador("individual", "900123456", "compras@ceiba.co").replace(PASSWORD, debil))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.password").isString());
            verificarConteos(0, 0);
        }
    }

    @Test
    void registrarComprador_conDocumentoInvalido_retorna400() throws Exception {
        for (String invalido : List.of("12345", "12345678901", "9001234ab", "900123456-7")) {
            enviar(comprador("individual", invalido, "compras@ceiba.co"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.documento").isString());
            verificarConteos(0, 0);
        }
    }

    @Test
    void registrarComprador_conCorreoDeAgricultor_retorna409() throws Exception {
        enviar(json("12345678", "pedro@mail.com")).andExpect(status().isCreated());
        verificarConteos(1, 0);

        enviar(comprador("individual", "900123456", "pedro@mail.com")).andExpect(status().isConflict());

        verificarConteos(1, 0);
    }

    @Test
    void registrarComprador_conDocumentoIgualACedulaDeAgricultor_retorna201() throws Exception {
        enviar(json("900123456", "pedro@mail.com")).andExpect(status().isCreated());

        enviar(comprador("individual", "900123456", "compras@ceiba.co")).andExpect(status().isCreated());

        verificarConteos(2, 1);
    }

    @Test
    void registrarComprador_conTipoEnMayusculasOMixtas_seNormalizaYRetorna201() throws Exception {
        enviar(comprador("Comerciante", "900123456", "compras@ceiba.co"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo_comprador").value("COMERCIANTE"));
        enviar(comprador("RESTAURANTE", "900123457", "cocina@ceiba.co"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo_comprador").value("RESTAURANTE"));

        Comprador mixto = compradorRepository.findByUsuarioCorreo("compras@ceiba.co").orElseThrow();
        Comprador mayusculas = compradorRepository.findByUsuarioCorreo("cocina@ceiba.co").orElseThrow();
        assertThat(mixto.getTipoComprador()).isEqualTo(Comprador.TipoComprador.COMERCIANTE);
        assertThat(mayusculas.getTipoComprador()).isEqualTo(Comprador.TipoComprador.RESTAURANTE);
        verificarConteos(2, 2);
    }

    @Test
    void registrarComprador_valido_respuestaNoExponePasswordNiHash() throws Exception {
        MvcResult resultado = enviar(comprador("restaurante", "900123456", "compras@ceiba.co"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist())
                .andReturn();

        String hash = usuarioRepository.findByCorreo("compras@ceiba.co").orElseThrow().getPasswordHash();
        assertThat(resultado.getResponse().getContentAsString()).doesNotContain(PASSWORD).doesNotContain(hash);
    }
}
