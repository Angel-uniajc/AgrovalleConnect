package com.agrovalleconnect.agrovalle_connect.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/**
 * HU-12: pedido de un comprador. Tabla "pedido" de schema.sql.
 * Contrato en docs/contrato-pedido-hu12.md.
 *
 * <p>Los detalles se guardan y se borran junto con el pedido (cascada y
 * orphanRemoval). El total no se guarda: se calcula como la suma de los
 * subtotales de los detalles más {@code costoFlete} (atributo derivado, 3FN).
 */
@Entity
@Table(
    name = "pedido",
    indexes = @Index(name = "idx_pedido_comprador", columnList = "comprador_id"))
@Check(constraints = "costo_flete >= 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sin cascade: guardar o borrar un pedido nunca modifica al comprador
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Comprador comprador;

    // La asigna el servidor al guardar; nunca viene del JSON
    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_pedido", nullable = false, updatable = false)
    private LocalDateTime fechaPedido;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDIENTE_PAGO'")
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.PENDIENTE_PAGO;

    // Calculado con la estrategia de flete del municipio de la finca (HU-12)
    @Column(name = "costo_flete", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoFlete;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    /**
     * Agrega una línea al pedido y mantiene los dos lados de la relación.
     *
     * @param producto producto comprado
     * @param cantidad cantidad solicitada
     * @param precioUnitario precio del producto al momento de la compra
     * @return el detalle creado
     */
    public DetallePedido agregarDetalle(Producto producto, BigDecimal cantidad,
            BigDecimal precioUnitario) {
        DetallePedido detalle = DetallePedido.builder()
                .pedido(this)
                .producto(producto)
                .cantidad(cantidad)
                .precioUnitario(precioUnitario)
                .build();
        detalles.add(detalle);
        return detalle;
    }

    /** Estados del pedido; mismos valores que el CHECK de schema.sql. */
    public enum Estado {
        PENDIENTE_PAGO,
        PAGADO,
        ALISTADO,
        EN_CAMINO,
        ENTREGADO,
        CANCELADO
    }
}
