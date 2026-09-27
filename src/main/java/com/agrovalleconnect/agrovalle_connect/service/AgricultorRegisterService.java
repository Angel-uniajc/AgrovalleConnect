package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.AgricultorRegisterDTO;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgricultorRegisterService {

    private final UsuarioRepository usuarioRepository;

    private final AgricultorRepository agricultorRepository;
    
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Agricultor registrar(AgricultorRegisterDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        if (agricultorRepository.existsByCedula(dto.getCedula())) {
            throw new IllegalArgumentException("La cédula ya está registrada");
        }

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .rol(Usuario.Rol.AGRICULTOR)
                .fechaRegistro(LocalDateTime.now())
                .build();
        usuarioRepository.save(usuario);

        Agricultor agricultor = Agricultor.builder()
                .cedula(dto.getCedula())
                .ubicacionValle(dto.getUbicacionValle())
                .usuario(usuario)
                .build();

        return agricultorRepository.save(agricultor);
    }
}