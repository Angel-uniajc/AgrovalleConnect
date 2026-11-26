package com.agrovalleconnect.agrovalle_connect.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorRequest;
import com.agrovalleconnect.agrovalle_connect.exception.CorreoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.exception.DocumentoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Comprador.TipoComprador;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * T04-HU17: el registro usa la base real (H2) y la transacción real.
 * Sin @Transactional en la clase: cada registro debe confirmarse o deshacerse por sí mismo,
 * igual que en producción.
 */
@SpringBootTest
class CompradorRegisterServiceIntegrationTest {

    private static final String PASSWORD = "Compras@2026";

    @Autowired
    private CompradorRegisterService servicio;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgricultorRepository agricultorRepository;

    @MockitoSpyBean
    private CompradorRepository compradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    @AfterEach
    void limpiarBase() {
        compradorRepository.deleteAll();
        agricultorRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private RegistroCompradorRequest dto(String correo, String documento) {
        return new RegistroCompradorRequest("Restaurante La Ceiba", correo, PASSWORD,
                "COMPRADOR", "restaurante", documento);
    }

    @Test
    void registrar_guardaUsuarioYCompradorConHashBcrypt() {
        servicio.registrar(dto("compras@laceiba.co", "900123456"));

        Comprador guardado = transactionTemplate.execute(estado -> {
            Comprador comprador = compradorRepository.findByUsuarioCorreo("compras@laceiba.co").orElseThrow();
            comprador.getUsuario().getPasswordHash(); // cargar el usuario dentro de la transacción
            return comprador;
        });

        assertThat(guardado.getDocumento()).isEqualTo("900123456");
        assertThat(guardado.getTipoComprador()).isEqualTo(TipoComprador.RESTAURANTE);
        assertThat(guardado.getUsuario().getRol()).isEqualTo(Usuario.Rol.COMPRADOR);
        assertThat(guardado.getUsuario().getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches(PASSWORD, guardado.getUsuario().getPasswordHash())).isTrue();
    }

    @Test
    void falloAlCrearElPerfil_deshaceTambienElUsuario() {
        doThrow(new IllegalStateException("fallo simulado al guardar el perfil"))
                .when(compradorRepository).saveAndFlush(any());

        assertThatThrownBy(() -> servicio.registrar(dto("compras@laceiba.co", "900123456")))
                .isInstanceOf(IllegalStateException.class);

        // Rollback: el usuario se insertó dentro de la transacción, pero no quedó guardado
        assertThat(usuarioRepository.existsByCorreo("compras@laceiba.co")).isFalse();
        assertThat(usuarioRepository.count()).isZero();
        assertThat(compradorRepository.count()).isZero();
    }

    @Test
    void documentoDuplicadoQueEsquivaLaVerificacion_loFrenaLaBaseYSeDeshaceTodo() {
        servicio.registrar(dto("primero@correo.com", "900123456"));
        long usuariosAntes = usuarioRepository.count();

        // Simula un registro simultáneo: la verificación previa todavía no ve el documento
        // (1.ª llamada) y la revisión posterior al rechazo de la base sí (2.ª llamada).
        // El rechazo es real: lo produce el UNIQUE de comprador.documento en la base.
        doReturn(false).doReturn(true).when(compradorRepository).existsByDocumento(anyString());

        assertThatThrownBy(() -> servicio.registrar(dto("segundo@correo.com", "900123456")))
                .isInstanceOf(DocumentoDuplicadoException.class);

        assertThat(usuarioRepository.existsByCorreo("segundo@correo.com")).isFalse();
        assertThat(usuarioRepository.count()).isEqualTo(usuariosAntes);
        assertThat(compradorRepository.count()).isEqualTo(1);
    }

    @Test
    void correoDeAgricultor_noPuedeUsarseParaUnComprador() {
        Usuario agricultor = Usuario.builder()
                .nombre("Pedro Gómez")
                .correo("pedro@mail.com")
                .passwordHash("hash")
                .rol(Usuario.Rol.AGRICULTOR)
                .fechaRegistro(LocalDateTime.now())
                .build();
        usuarioRepository.save(agricultor);

        assertThatThrownBy(() -> servicio.registrar(dto("pedro@mail.com", "900123456")))
                .isInstanceOf(CorreoDuplicadoException.class);

        assertThat(compradorRepository.count()).isZero();
    }

    @Test
    void violacionQueNoEsDuplicado_noSeTraduceA409() {
        doThrow(new DataIntegrityViolationException("otra restricción"))
                .when(compradorRepository).saveAndFlush(any());

        assertThatThrownBy(() -> servicio.registrar(dto("compras@laceiba.co", "900123456")))
                .isExactlyInstanceOf(DataIntegrityViolationException.class);

        assertThat(usuarioRepository.count()).isZero();
    }
}
