package com.agrovalleconnect.agrovalle_connect.mapper;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroAgricultorResponse;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Convierte los DTO de registro en entidades y las entidades en la respuesta del registro.
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

    public RegistroAgricultorResponse aRespuesta(Agricultor agricultor) {
        return new RegistroAgricultorResponse(
                agricultor.getUsuario().getNombre(),
                "Agricultor registrado exitosamente");
    }
}
