package com.agrovalleconnect.agrovalle_connect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AgricultorRegisterDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    private String nombre;

    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 50, message = "El correo no puede superar 50 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "La cédula no puede estar vacía")
    @Size(max = 20, message = "La cédula no puede superar 20 caracteres")
    private String cedula;

    @NotBlank(message = "La ubicación no puede estar vacía")
    @Size(max = 15, message = "La ubicación no puede superar 15 caracteres")
    private String ubicacionValle;

}
