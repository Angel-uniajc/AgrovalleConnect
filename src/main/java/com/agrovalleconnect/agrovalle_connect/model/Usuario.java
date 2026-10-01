package com.agrovalleconnect.agrovalle_connect.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Usuario {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (
        name = "nombre",
        nullable = false, 
        length = 50)
    private String nombre;

    @Column (
        name = "password_hash",
        nullable = false)
    private String passwordHash;

    @Column (
        nullable = false, 
        length = 50,
        unique = true)
    private String correo;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private Rol rol;

    @Column (name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToOne(
        mappedBy = "usuario", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true) // Relación bidireccional con Agricultor
    private Agricultor agricultor;

    public enum Rol {
        AGRICULTOR,
        COMPRADOR
    }
}