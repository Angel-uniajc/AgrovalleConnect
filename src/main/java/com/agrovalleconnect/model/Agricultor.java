package com.agrovalleconnect.model;

import jakarta.persistence.*;

@Entity 
@Table(name = "agricultor")
public class Agricultor {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column (nullable = false, length = 100)
    private String nombre;

    @Column (nullable = false, length = 20)
    private String documentoIdentidad;

    @Column (nullable = false, length = 20)
    private String telefono;

    @Column (length = 100)
    private String correo;

    @Column (name = "fecha_registro")
    private java.time.LocalDate fechaRegistro;
    


}
