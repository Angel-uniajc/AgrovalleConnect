package com.agrovalleconnect.agrovalle_connect.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * HU-08, HU-12: línea de un pedido. Tabla N:M "incluye" (detalle_pedido) de
 * schema.sql, con clave compuesta (pedido_id, producto_id): un producto aparece
 * una sola vez en cada pedido.
 *
 * <p>{@code precioUnitario} es una copia del precio al momento de la compra;
 * si el agricultor cambia luego el precio del producto, el detalle no cambia.
 */
@Entity
@Table(name = "detalle_pedido")
@Check(constraints = "cantidad > 0 and precio_unitario > 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedido {

    @Builder.Default
    @EmbeddedId
    private DetallePedidoId id = new DetallePedidoId();

    // ON DELETE CASCADE en la FK, igual que schema.sql
    @MapsId("pedidoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Pedido pedido;

    // Sin cascade: borrar un detalle nunca borra el producto
    @MapsId("productoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Subtotal de la línea (cantidad × precio unitario). Es un valor derivado
     * y no se guarda en la base.
     *
     * @return el subtotal de la línea
     */
    public BigDecimal subtotal() {
        return cantidad.multiply(precioUnitario);
    }
}
