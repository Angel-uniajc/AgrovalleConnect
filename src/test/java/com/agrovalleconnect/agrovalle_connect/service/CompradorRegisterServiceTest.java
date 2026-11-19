package com.agrovalleconnect.agrovalle_connect.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorResponse;
import com.agrovalleconnect.agrovalle_connect.exception.CorreoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.exception.DocumentoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.mapper.CompradorMapper;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Comprador.TipoComprador;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * T04-HU17: pruebas unitarias del registro de comprador (TDD).
 * Los repositorios y el PasswordEncoder son simulados; la transacción real se prueba
 * en CompradorRegisterServiceIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class CompradorRegisterServiceTest {

    private static final String PASSWORD = "Compras@2026";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CompradorRepository compradorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PlatformTransactionManager transactionManager;

    private CompradorRegisterService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new CompradorRegisterService(usuarioRepository, compradorRepository,
                passwordEncoder, new CompradorMapper(), transactionManager);
    }

    private RegistroCompradorRequest dtoValido() {
        return new RegistroCompradorRequest(
                "Restaurante La Ceiba",
                "compras@laceiba.co",
                PASSWORD,
                "COMPRADOR",
                "restaurante",
                "900123456");
    }

    // ---------- Camino feliz ----------

    @Test
    void registrar_datosValidos_guardaUsuarioYCompradorEnlazados() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn("HASH_FALSO");

        servicio.registrar(dtoValido());

        ArgumentCaptor<Usuario> captorUsuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captorUsuario.capture());
        Usuario usuario = captorUsuario.getValue();

        ArgumentCaptor<Comprador> captorComprador = ArgumentCaptor.forClass(Comprador.class);
        verify(compradorRepository).saveAndFlush(captorComprador.capture());
        Comprador comprador = captorComprador.getValue();

        assertThat(usuario.getNombre()).isEqualTo("Restaurante La Ceiba");
        assertThat(usuario.getCorreo()).isEqualTo("compras@laceiba.co");
        assertThat(usuario.getRol()).isEqualTo(Usuario.Rol.COMPRADOR);
        assertThat(usuario.getFechaRegistro()).isNotNull();

        assertThat(comprador.getDocumento()).isEqualTo("900123456");
        assertThat(comprador.getUsuario()).isSameAs(usuario);
    }

    @Test
    void registrar_tipoEnMinusculas_seGuardaEnMayusculas() {
        when(passwordEncoder.encode(any())).thenReturn("HASH_FALSO");
        RegistroCompradorRequest dto = new RegistroCompradorRequest("Sofía Ruiz", "sofia@correo.com",
                PASSWORD, "COMPRADOR", "Comerciante", "12345678");

        servicio.registrar(dto);

        ArgumentCaptor<Comprador> captor = ArgumentCaptor.forClass(Comprador.class);
        verify(compradorRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getTipoComprador()).isEqualTo(TipoComprador.COMERCIANTE);
    }

    @Test
    void registrar_hasheaLaContrasenaYNoGuardaElTextoPlano() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn("HASH_FALSO");

        servicio.registrar(dtoValido());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        verify(passwordEncoder).encode(PASSWORD);
        assertThat(captor.getValue().getPasswordHash())
                .isEqualTo("HASH_FALSO")
                .isNotEqualTo(PASSWORD);
    }

    @Test
    void registrar_devuelveRespuestaDelContratoSinDatosSensibles() {
        when(passwordEncoder.encode(any())).thenReturn("HASH_FALSO");

        RegistroCompradorResponse respuesta = servicio.registrar(dtoValido());

        assertThat(respuesta.nombre()).isEqualTo("Restaurante La Ceiba");
        assertThat(respuesta.rol()).isEqualTo("COMPRADOR");
        assertThat(respuesta.tipoComprador()).isEqualTo("RESTAURANTE");
        assertThat(respuesta.mensaje()).isEqualTo("Comprador registrado exitosamente");
        assertThat(respuesta.toString())
                .doesNotContain(PASSWORD)
                .doesNotContain("HASH_FALSO")
                .doesNotContain("900123456");
    }

    // ---------- Duplicados detectados antes de escribir ----------

    @Test
    void registrar_correoDuplicado_lanza409SinHashearNiGuardar() {
        when(usuarioRepository.existsByCorreo("compras@laceiba.co")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
                .isInstanceOf(CorreoDuplicadoException.class)
                .hasMessage("El correo ya está registrado");

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
        verify(compradorRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_documentoDuplicado_lanza409SinHashearNiGuardar() {
        when(compradorRepository.existsByDocumento("900123456")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
                .isInstanceOf(DocumentoDuplicadoException.class)
                .hasMessage("El documento ya está registrado");

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
        verify(compradorRepository, never()).saveAndFlush(any());
    }

    @Test
    void registrar_correoYDocumentoDuplicados_reportaPrimeroElCorreo() {
        when(usuarioRepository.existsByCorreo("compras@laceiba.co")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
                .isInstanceOf(CorreoDuplicadoException.class);

        // El correo es único entre todos los roles: con él basta para rechazar
        verify(compradorRepository, never()).existsByDocumento(any());
    }

    // ---------- Duplicados que llegan a la base (registros simultáneos) ----------

    @Test
    void registrar_correoGanadoPorOtroRegistroSimultaneo_lanza409DeCorreo() {
        when(passwordEncoder.encode(any())).thenReturn("HASH_FALSO");
        // La verificación previa no lo ve; la base lo rechaza; al revisar de nuevo ya existe
        when(usuarioRepository.existsByCorreo("compras@laceiba.co")).thenReturn(false, true);
        when(usuarioRepository.save(any())).thenThrow(new DataIntegrityViolationException(
                "unique correo", new SQLException("correo duplicado", "23505")));

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
                .isInstanceOf(CorreoDuplicadoException.class);

        verify(compradorRepository, never()).saveAndFlush(any());
        verify(transactionManager).rollback(any());
    }

    @Test
    void registrar_documentoGanadoPorOtroRegistroSimultaneo_lanza409DeDocumento() {
        when(passwordEncoder.encode(any())).thenReturn("HASH_FALSO");
        when(compradorRepository.existsByDocumento("900123456")).thenReturn(false, true);
        when(compradorRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException(
                        "unique documento", new SQLException("documento duplicado", "23505")));

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
                .isInstanceOf(DocumentoDuplicadoException.class);

        verify(transactionManager).rollback(any());
    }

    @Test
    void registrar_uniqueSinCorreoNiDocumentoDuplicado_conservaElErrorOriginal() {
        DataIntegrityViolationException original = new DataIntegrityViolationException(
                "otra clave única", new SQLException("clave duplicada", "23505"));
        when(compradorRepository.saveAndFlush(any())).thenThrow(original);

        assertThatThrownBy(() -> servicio.registrar(dtoValido())).isSameAs(original);
        verify(transactionManager).rollback(any());
    }

    @Test
    void registrar_otraViolacionDeIntegridad_noSeConvierteEnDuplicado() {
        when(passwordEncoder.encode(any())).thenReturn("HASH_FALSO");
        DataIntegrityViolationException original = new DataIntegrityViolationException(
                "check tipo_comprador", new SQLException("tipo inválido", "23514"));
        when(usuarioRepository.existsByCorreo("compras@laceiba.co")).thenReturn(false, true);
        when(compradorRepository.saveAndFlush(any())).thenThrow(original);

        // Aunque otro registro gane el correo, el fallo original fue de CHECK.
        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
                .isSameAs(original);

        verify(transactionManager).rollback(any());
    }
}
