package com.agrovalleconnect.agrovalle_connect.mapper;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroCompradorResponse;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * HU-17: convierte el DTO de registro del comprador en entidades y la entidad en la respuesta 201.
 */
@Component
public class CompradorMapper {

    public Usuario aUsuario(RegistroCompradorRequest dto, String passwordHash) {
        return Usuario.builder()
                .nombre(dto.nombre())
                .correo(dto.correo())
                .passwordHash(passwordHash)
                .rol(Usuario.Rol.COMPRADOR)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    public Comprador aComprador(RegistroCompradorRequest dto, Usuario usuario) {
        return Comprador.builder()
                .documento(dto.documento())
                .tipoComprador(dto.tipoCompradorNormalizado())
                .usuario(usuario)
                .build();
    }

    /** Sin password, hash ni documento, como pide el contrato de HU-17. */
    public RegistroCompradorResponse aRespuesta(Comprador comprador) {
        return new RegistroCompradorResponse(
                comprador.getUsuario().getNombre(),
                comprador.getUsuario().getRol().name(),
                comprador.getTipoComprador().name(),
                "Comprador registrado exitosamente");
    }
}
