package com.agrovalleconnect.model;

import jakarta.persistence.*;

@Entity 
@Table (name = "agricultor")
public class Agricultor {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column (length = 20, nullable = false)
    private String documentoIdentidad;

    @Column ( length = 100)
    private String correo;

    @Column (length = 20, nullable = false)
    private String telefono;

    @Column (name = "fecha_registro")
    private java.time.LocalDate fechaRegistro;

    
}   
