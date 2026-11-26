package com.agrovalleconnect.agrovalle_connect.dto;

import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.CORREO_MAX;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.MSG_CORREO_FORMATO;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.MSG_CORREO_LARGO;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.MSG_CORREO_OBLIGATORIO;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.MSG_NOMBRE_LARGO;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.MSG_NOMBRE_OBLIGATORIO;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.MSG_PASSWORD_DEBIL;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.MSG_PASSWORD_OBLIGATORIA;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.MSG_ROL_INVALIDO;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.NOMBRE_MAX;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.REGEX_CORREO;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.REGEX_DOCUMENTO;
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.REGEX_PASSWORD;

import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

/**
 * HU-17: datos de entrada del registro de comprador.
 * No pide cédula ni ubicacion_valle (son del agricultor).
 */
public record RegistroCompradorRequest(

        @NotBlank(message = MSG_NOMBRE_OBLIGATORIO)
        @Size(max = NOMBRE_MAX, message = MSG_NOMBRE_LARGO)
        String nombre,

        @NotBlank(message = MSG_CORREO_OBLIGATORIO)
        @Size(max = CORREO_MAX, message = MSG_CORREO_LARGO)
        @Pattern(regexp = REGEX_CORREO, message = MSG_CORREO_FORMATO)
        String correo,

        @NotBlank(message = MSG_PASSWORD_OBLIGATORIA)
        @Pattern(regexp = REGEX_PASSWORD, message = MSG_PASSWORD_DEBIL)
        String password,

        @NotBlank(message = MSG_ROL_INVALIDO)
        @Pattern(regexp = "COMPRADOR", message = MSG_ROL_INVALIDO)
        String rol,

        @NotBlank(message = "El tipo de comprador es obligatorio")
        @Pattern(regexp = "(?i)individual|comerciante|restaurante",
                message = "El tipo de comprador debe ser individual, comerciante o restaurante")
        @JsonProperty("tipo_comprador")
        String tipoComprador,

        @NotBlank(message = "El documento es obligatorio")
        @Pattern(regexp = REGEX_DOCUMENTO,
                message = "El documento debe tener entre 6 y 10 dígitos, sin dígito de verificación")
        String documento

) implements RegistroRequest {

    /**
     * Devuelve el tipo en MAYÚSCULAS, como lo exige el CHECK de la tabla comprador.
     * Solo debe llamarse después de validar el DTO.
     */
    public Comprador.TipoComprador tipoCompradorNormalizado() {
        return Comprador.TipoComprador.valueOf(tipoComprador.toUpperCase(Locale.ROOT));
    }
}
