package com.agrovalleconnect.agrovalle_connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.agrovalleconnect.agrovalle_connect.dto.LoginRequest;
import com.agrovalleconnect.agrovalle_connect.dto.LoginResponse;
import com.agrovalleconnect.agrovalle_connect.exception.CredencialesInvalidasException;

import java.util.Optional;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AutenticacionService autenticacionService;

    @Test
    void loginValidoDevuelveToken() {
        // Preparar (Arrange): maniquíes con sus "etiquetas"
        Usuario usuario = mock(Usuario.class);
        Agricultor agricultor = mock(Agricultor.class);

        when(usuario.getCorreo()).thenReturn("ana@correo.com");
        when(usuario.getPasswordHash()).thenReturn("hash-guardado");
        when(usuario.getRol()).thenReturn(Usuario.Rol.AGRICULTOR);
        when(usuario.getAgricultor()).thenReturn(agricultor);
        when(agricultor.getId()).thenReturn(7L);

        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave123", "hash-guardado")).thenReturn(true);
        when(jwtUtil.generarToken("ana@correo.com", "AGRICULTOR", 7L)).thenReturn("token-abc");

        // Actuar (Act)
        LoginResponse respuesta = autenticacionService.login(
                new LoginRequest("ana@correo.com", "Clave123"));

        // Verificar (Assert)
        assertEquals("token-abc", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
    }

    @Test
    void correoInexistenteLanzaExcepcion() {
        when(usuarioRepository.findByCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> autenticacionService.login(new LoginRequest("nadie@correo.com", "cualquiera")));

        assertEquals("Credenciales inválidas", ex.getMessage());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void contrasenaIncorrectaLanzaLaMismaExcepcion() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getPasswordHash()).thenReturn("hash-guardado");

        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "hash-guardado")).thenReturn(false);

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> autenticacionService.login(new LoginRequest("ana@correo.com", "mala")));

        assertEquals("Credenciales inválidas", ex.getMessage());
        verifyNoInteractions(jwtUtil);
    }
}