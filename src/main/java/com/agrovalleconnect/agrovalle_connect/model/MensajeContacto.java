package com.agrovalleconnect.agrovalle_connect.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * HU-05: intención de contacto de un comprador sobre un producto.
 * Tabla N:M "contacta" de schema.sql. Contrato en docs/contrato-contacto-hu05.md.
 * Las relaciones no llevan cascade: borrar o guardar un mensaje nunca
 * modifica al comprador ni al producto.
 */
@Entity
@Table(name = "mensaje_contacto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Comprador comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // TEXT en la base; el límite de 2000 caracteres lo valida el DTO
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    // La asigna el servidor al guardar; nunca viene del JSON
    @CreationTimestamp
    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;
}
