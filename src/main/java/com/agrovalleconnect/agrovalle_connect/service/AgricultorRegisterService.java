package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorRequest;
import com.agrovalleconnect.agrovalle_connect.exception.CedulaDuplicadaException;
import com.agrovalleconnect.agrovalle_connect.exception.CorreoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.mapper.AgricultorMapper;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AgricultorRegisterService {

    private final UsuarioRepository usuarioRepository;

    private final AgricultorRepository agricultorRepository;

    private final PasswordEncoder passwordEncoder;

    private final AgricultorMapper agricultorMapper;

    @Transactional
    public Agricultor registrar(RegistroAgricultorRequest dto) {
        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new CorreoDuplicadoException();
        }
        if (agricultorRepository.existsByCedula(dto.cedula())) {
            throw new CedulaDuplicadaException();
        }

        Usuario usuario = agricultorMapper.aUsuario(dto, passwordEncoder.encode(dto.password()));
        usuarioRepository.save(usuario);

        Agricultor agricultor = agricultorMapper.aAgricultor(dto, usuario);
        return agricultorRepository.save(agricultor);
    }
}

