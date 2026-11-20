package com.agrovalleconnect.agrovalle_connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.agrovalleconnect.agrovalle_connect.dto.LoginRequest;
import com.agrovalleconnect.agrovalle_connect.dto.LoginResponse;
import com.agrovalleconnect.agrovalle_connect.exception.CredencialesInvalidasException;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.model.Usuario.Rol;

import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import com.agrovalleconnect.agrovalle_connect.security.JwtUtil;
import java.util.Optional;
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
        Usuario usuario = mock(Usuario.class);
        Agricultor agricultor = mock(Agricultor.class);

        when(usuario.getCorreo()).thenReturn("ana@correo.com");
        when(usuario.getPasswordHash()).thenReturn("hash-guardado");
        when(usuario.getRol()).thenReturn(Rol.AGRICULTOR);
        when(usuario.getAgricultor()).thenReturn(agricultor);
        when(agricultor.getId()).thenReturn(7L);

        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave123", "hash-guardado")).thenReturn(true);
        when(jwtUtil.generarToken("ana@correo.com", "AGRICULTOR", 7L)).thenReturn("token-abc");

        LoginResponse respuesta = autenticacionService.login(
                new LoginRequest("ana@correo.com", "Clave123"));

        assertEquals("token-abc", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
    }

    @Test
    void loginValidoSinAgricultorGeneraTokenConPerfilIdNulo() {
        Usuario comprador = mock(Usuario.class);
        // getAgricultor() no se programa: un mock devuelve null por defecto,
        // igual que un comprador sin agricultor asociado.

        when(comprador.getCorreo()).thenReturn("sofia@correo.com");
        when(comprador.getPasswordHash()).thenReturn("hash-guardado");
        when(comprador.getRol()).thenReturn(Rol.COMPRADOR);

        when(usuarioRepository.findByCorreo("sofia@correo.com")).thenReturn(Optional.of(comprador));
        when(passwordEncoder.matches("Clave123", "hash-guardado")).thenReturn(true);
        when(jwtUtil.generarToken("sofia@correo.com", "COMPRADOR", null))
                .thenReturn("token-comprador");

        LoginResponse respuesta = autenticacionService.login(
                new LoginRequest("sofia@correo.com", "Clave123"));

        assertEquals("token-comprador", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
    }

    @Test
    void loginCompradorConPerfilGeneraTokenConIdDelComprador() {
        Usuario usuario = mock(Usuario.class);
        Comprador comprador = mock(Comprador.class);

        when(usuario.getCorreo()).thenReturn("laceiba@correo.com");
        when(usuario.getPasswordHash()).thenReturn("hash-guardado");
        when(usuario.getRol()).thenReturn(Rol.COMPRADOR);
        when(usuario.getComprador()).thenReturn(comprador);
        when(comprador.getId()).thenReturn(12L);

        when(usuarioRepository.findByCorreo("laceiba@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave123", "hash-guardado")).thenReturn(true);
        when(jwtUtil.generarToken("laceiba@correo.com", "COMPRADOR", 12L)).thenReturn("token-comprador");

        LoginResponse respuesta = autenticacionService.login(
                new LoginRequest("laceiba@correo.com", "Clave123"));

        assertEquals("token-comprador", respuesta.token());
        verify(usuario, never()).getAgricultor();
    }

    @Test
    void loginAgricultorSinPerfilNoUsaElPerfilDeComprador() {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getCorreo()).thenReturn("pedro@correo.com");
        when(usuario.getPasswordHash()).thenReturn("hash-guardado");
        when(usuario.getRol()).thenReturn(Rol.AGRICULTOR);

        when(usuarioRepository.findByCorreo("pedro@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave123", "hash-guardado")).thenReturn(true);
        when(jwtUtil.generarToken("pedro@correo.com", "AGRICULTOR", null)).thenReturn("token-agricultor");

        LoginResponse respuesta = autenticacionService.login(
                new LoginRequest("pedro@correo.com", "Clave123"));

        assertEquals("token-agricultor", respuesta.token());
        verify(usuario, never()).getComprador();
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
