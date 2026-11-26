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
import static com.agrovalleconnect.agrovalle_connect.dto.ReglasRegistro.REGEX_PASSWORD;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroAgricultorRequest(

        @NotBlank(message = MSG_NOMBRE_OBLIGATORIO)
        @Size(max = NOMBRE_MAX, message = MSG_NOMBRE_LARGO)
        String nombre,

        @NotBlank(message = "La ubicación en el Valle es obligatoria")
        @Size(max = 15, message = "La ubicacion debe tener menos de 15 caracteres")
        @JsonProperty("ubicacion_valle")
        String ubicacionValle,

        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "\\d{6,10}", message = "La cédula debe tener entre 6 y 10 dígitos")
        String cedula,

        @NotBlank(message = MSG_CORREO_OBLIGATORIO)
        @Size(max = CORREO_MAX, message = MSG_CORREO_LARGO)
        @Pattern(regexp = REGEX_CORREO, message = MSG_CORREO_FORMATO)
        String correo,

        @NotBlank(message = MSG_PASSWORD_OBLIGATORIA)
        @Pattern(regexp = REGEX_PASSWORD, message = MSG_PASSWORD_DEBIL)
        String password,

        // Opcional: null (JSON de HU-01) o "AGRICULTOR". Cualquier otro valor, como "ADMIN", se rechaza.
        @Pattern(regexp = "AGRICULTOR", message = MSG_ROL_INVALIDO)
        String rol

) implements RegistroRequest {

    /** Compatibilidad con HU-01: mismo constructor de 5 datos de siempre, sin rol. */
    public RegistroAgricultorRequest(String nombre, String ubicacionValle, String cedula,
                                     String correo, String password) {
        this(nombre, ubicacionValle, cedula, correo, password, null);
    }
}
