package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorResponse;
import com.agrovalleconnect.agrovalle_connect.exception.CorreoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.exception.DocumentoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.mapper.CompradorMapper;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class CompradorRegisterService {

    private final UsuarioRepository usuarioRepository;

    private final CompradorRepository compradorRepository;

    private final PasswordEncoder passwordEncoder;

    private final CompradorMapper compradorMapper;

    private final TransactionTemplate transaccion;

    public CompradorRegisterService(UsuarioRepository usuarioRepository,
                                    CompradorRepository compradorRepository,
                                    PasswordEncoder passwordEncoder,
                                    CompradorMapper compradorMapper,
                                    PlatformTransactionManager transactionManager) {
        this.usuarioRepository = usuarioRepository;
        this.compradorRepository = compradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.compradorMapper = compradorMapper;
        this.transaccion = new TransactionTemplate(transactionManager);
    }

    public RegistroCompradorResponse registrar(RegistroCompradorRequest dto) {
        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new CorreoDuplicadoException();
        }
        if (compradorRepository.existsByDocumento(dto.documento())) {
            throw new DocumentoDuplicadoException();
        }

        String passwordHash = passwordEncoder.encode(dto.password());
        try {
            return transaccion.execute(estado -> {
                Usuario usuario = compradorMapper.aUsuario(dto, passwordHash);
                usuarioRepository.save(usuario);
                Comprador comprador = compradorMapper.aComprador(dto, usuario);
                compradorRepository.saveAndFlush(comprador);
                return compradorMapper.aRespuesta(comprador);
            });
        } catch (DataIntegrityViolationException ex) {
            if (usuarioRepository.existsByCorreo(dto.correo())) {
                throw new CorreoDuplicadoException();
            }
            if (compradorRepository.existsByDocumento(dto.documento())) {
                throw new DocumentoDuplicadoException();
            }
            throw ex;
        }
    }
}
