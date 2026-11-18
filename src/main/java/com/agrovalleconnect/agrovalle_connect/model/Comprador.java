package com.agrovalleconnect.agrovalle_connect.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-17: perfil del comprador. Sigue el mismo patrón que {@link Agricultor}:
 * una tabla hija de "usuario" unida por usuario_id (FK + UNIQUE).
 * Contrato y decisiones en docs/contrato-registro-hu17.md.
 */
@Entity
@Table(name = "comprador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 6 a 10 dígitos sin dígito de verificación; único solo entre compradores
    @Column(
        nullable = false,
        length = 20,
        unique = true)
    private String documento;

    // Se guarda como texto en MAYÚSCULAS, igual que el CHECK de la tabla
    @Enumerated(EnumType.STRING)
    @Column(
        name = "tipo_comprador",
        nullable = false,
        length = 15)
    private TipoComprador tipoComprador;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "usuario_id",
        nullable = false,
        unique = true)
    private Usuario usuario;

    public enum TipoComprador {
        INDIVIDUAL,
        COMERCIANTE,
        RESTAURANTE
    }
}
