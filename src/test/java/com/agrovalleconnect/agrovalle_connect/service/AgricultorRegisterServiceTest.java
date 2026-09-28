package com.agrovalleconnect.agrovalle_connect.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalleconnect.agrovalle_connect.dto.AgricultorRegisterDTO;
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

    private AgricultorRegisterDTO dtoValido() {
        AgricultorRegisterDTO dto = new AgricultorRegisterDTO();
        dto.setNombre("Pedro");
        dto.setCorreo("pedro@mail.com");
        dto.setPassword("Secreta123");
        dto.setUbicacionValle("Dagua");
        dto.setCedula("1234567890");
        return dto;
    }

    @Test
    void registrar_hasheaLaContrasena() {
        when(passwordEncoder.encode("Secreta123")).thenReturn("HASH_FALSO");
        // SUPUESTO: el Service guarda con usuarioRepository.save(...)
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.registrar(dtoValido());   // SUPUESTO: nombre del método

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASH_FALSO");
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("Secreta123");
    }

    @Test
    void registrar_correoDuplicado_lanzaExcepcionYNoGuarda() {
        // SUPUESTO: existsByCorreo
        when(usuarioRepository.existsByCorreo("pedro@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
            .isInstanceOf(RuntimeException.class);   // ajusta a la excepción real

        verify(usuarioRepository, never()).save(any());
        verify(agricultorRepository, never()).save(any());
    }

    @Test
    void registrar_cedulaDuplicada_lanzaExcepcionYNoGuarda() {
        // SUPUESTO: existsByCedula
        when(agricultorRepository.existsByCedula("1234567890")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
            .isInstanceOf(RuntimeException.class);

        verify(usuarioRepository, never()).save(any());
        verify(agricultorRepository, never()).save(any());
    }

    @Test
    void registrar_datosValidos_guardaUsuarioYAgricultor() {
        when(passwordEncoder.encode(any())).thenReturn("HASH_FALSO");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.registrar(dtoValido());

        verify(usuarioRepository).save(any(Usuario.class));
        verify(agricultorRepository).save(any(Agricultor.class));
    }
}