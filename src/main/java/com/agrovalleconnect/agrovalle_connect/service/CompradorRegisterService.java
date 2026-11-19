package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorResponse;
import com.agrovalleconnect.agrovalle_connect.exception.CorreoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.exception.DocumentoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.exception.RecursoDuplicadoException;
import com.agrovalleconnect.agrovalle_connect.mapper.CompradorMapper;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.UsuarioRepository;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * HU-17: registro de comprador. Contrato en docs/contrato-registro-hu17.md.
 *
 * <p>Usuario y Comprador se guardan en una sola transacción: si falla el perfil, el usuario
 * también se deshace. La transacción se abre con TransactionTemplate (y no con @Transactional)
 * para poder revisar, después del rollback y en una consulta nueva, si un rechazo de la base
 * fue un duplicado de correo o de documento. En PostgreSQL no se puede consultar dentro de una
 * transacción que ya falló.
 *
 * <p>Debe llamarse fuera de otra transacción (desde el controlador), igual que en producción.
 */
@Service
public class CompradorRegisterService {

    private static final String SQLSTATE_CLAVE_DUPLICADA = "23505";

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
        // Antes de hashear: BCrypt es costoso y no se gasta en un registro que se va a rechazar
        buscarDuplicado(dto).ifPresent(duplicado -> {
            throw duplicado;
        });

        String passwordHash = passwordEncoder.encode(dto.password());
        try {
            return transaccion.execute(estado -> guardarUsuarioYPerfil(dto, passwordHash));
        } catch (DataIntegrityViolationException ex) {
            if (!(ex.getMostSpecificCause() instanceof SQLException causa)
                    || !SQLSTATE_CLAVE_DUPLICADA.equals(causa.getSQLState())) {
                throw ex;
            }
            // Otro registro simultáneo pudo ganar el correo o el documento entre la verificación
            // y el INSERT; el UNIQUE de la base lo frena. Solo se responde 409 si al revisar de
            // nuevo el duplicado existe: cualquier otra violación se propaga tal cual.
            Optional<RecursoDuplicadoException> duplicado = buscarDuplicado(dto);
            if (duplicado.isPresent()) {
                throw duplicado.get();
            }
            throw ex;
        }
    }

    /** Correo único entre todos los roles (se revisa primero); documento único entre compradores. */
    private Optional<RecursoDuplicadoException> buscarDuplicado(RegistroCompradorRequest dto) {
        if (usuarioRepository.existsByCorreo(dto.correo())) {
            return Optional.of(new CorreoDuplicadoException());
        }
        if (compradorRepository.existsByDocumento(dto.documento())) {
            return Optional.of(new DocumentoDuplicadoException());
        }
        return Optional.empty();
    }

    private RegistroCompradorResponse guardarUsuarioYPerfil(RegistroCompradorRequest dto, String passwordHash) {
        Usuario usuario = compradorMapper.aUsuario(dto, passwordHash);
        usuarioRepository.save(usuario);

        Comprador comprador = compradorMapper.aComprador(dto, usuario);
        // flush: si la base rechaza el perfil, el error sale aquí, dentro de la transacción
        compradorRepository.saveAndFlush(comprador);
        return compradorMapper.aRespuesta(comprador);
    }
}
