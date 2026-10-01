package com.agrovalleconnect.agrovalle_connect.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorRequest;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AgricultorRegisterServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AgricultorRepository agricultorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AgricultorRegisterService servicio;

private RegistroAgricultorRequest dtoValido() {
    return new RegistroAgricultorRequest(
                "Pedro Gómez",
                "Cali",
                "12345678",
                "pedro@mail.com",
                "LagordaTaborda@123")
    ;
}

    @Test
    void registrar_correoDuplicado_lanzaExcepcionYNoGuardaNada() {
        when(usuarioRepository.existsByCorreo("pedro@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("El correo ya está registrado");

        verify(usuarioRepository, never()).save(any());
        verify(agricultorRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registrar_cedulaDuplicada_lanzaExcepcionYNoGuardaNada() {
        when(agricultorRepository.existsByCedula("12345678")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("La cédula ya está registrada");

        verify(usuarioRepository, never()).save(any());
        verify(agricultorRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registrar_hasheaLaContrasena() {
        when(passwordEncoder.encode("LagordaTaborda@123")).thenReturn("HASH_FALSO");

        servicio.registrar(dtoValido());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        verify(passwordEncoder).encode("LagordaTaborda@123");

        assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASH_FALSO");
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo(dtoValido().password());
    }

    @Test
    void registrar_datosValidos_guardaUsuarioYAgricultorEnlazados() {
        when(passwordEncoder.encode(any())).thenReturn("HASH_FALSO");
        when(agricultorRepository.save(any(Agricultor.class))).thenAnswer(inv -> inv.getArgument(0));

        Agricultor resultado = servicio.registrar(dtoValido());

        ArgumentCaptor<Usuario> captorUsuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captorUsuario.capture());
        Usuario usuarioGuardado = captorUsuario.getValue();

        assertThat(usuarioGuardado.getNombre()).isEqualTo("Pedro Gómez");
        assertThat(usuarioGuardado.getCorreo()).isEqualTo("pedro@mail.com");
        assertThat(usuarioGuardado.getRol()).isEqualTo(Usuario.Rol.AGRICULTOR);

        assertThat(resultado.getCedula()).isEqualTo("12345678");
        assertThat(resultado.getUbicacionValle()).isEqualTo("Cali");
        assertThat(resultado.getUsuario()).isSameAs(usuarioGuardado);
    }
}