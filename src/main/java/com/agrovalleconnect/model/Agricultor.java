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

    // Constructor vacio

    public Agricultor() {}

    //getters y setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public java.time.LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(java.time.LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

}
