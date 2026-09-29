package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroAgricultorRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    String nombre,

    @NotBlank(message = "La ubicación en el Valle es obligatoria")
    @Size(max = 15, message = "La ubicacion debe tener menos de 15 caracteres") 
    @JsonProperty("ubicacion_valle")
    String ubicacionValle,

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "\\d{6,10}", message = "La cédula debe tener entre 6 y 10 dígitos")
    String cedula,
    
    @NotBlank (message = "El correo electrónico es obligatorio")
    @Size(max = 50, message = "El correo no puede superar 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$", 
    message = "El correo electrónico debe tener un formato válido")
    String correo,

    @NotBlank (message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
    message = "La contraseña debe tener al menos 8 caracteres, incluyendo una letra mayúscula"+
    ", una letra minúscula, un número y un carácter especial")          
    String password
)

{}
