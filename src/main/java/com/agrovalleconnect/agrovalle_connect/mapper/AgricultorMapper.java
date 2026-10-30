package com.agrovalleconnect.agrovalle_connect.mapper;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorRequest;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Convierte el formulario de registro (DTO) en las entidades que se guardan en BD.
 */
@Component
public class AgricultorMapper {

    public Usuario aUsuario(RegistroAgricultorRequest dto, String passwordHash) {
        return Usuario.builder()
                .nombre(dto.nombre())
                .correo(dto.correo())
                .passwordHash(passwordHash)
                .rol(Usuario.Rol.AGRICULTOR)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    public Agricultor aAgricultor(RegistroAgricultorRequest dto, Usuario usuario) {
        return Agricultor.builder()
                .cedula(dto.cedula())
                .ubicacionValle(dto.ubicacionValle())
                .usuario(usuario)
                .build();
    }
}
