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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

/**
 * HU-07: intento de pago de un pedido. Tabla "pago" de schema.sql.
 * Contrato en docs/contrato-pago-hu07.md (sección 4).
 *
 * <p>Reglas de persistencia:
 * <ul>
 *   <li>Un solo pago por pedido ({@code pedido_id UNIQUE}): no puede haber dos
 *       pagos aprobados para el mismo pedido.</li>
 *   <li>La clave de idempotencia es única y se guarda antes de llamar a la
 *       pasarela; la referencia del proveedor es única cuando existe.</li>
 *   <li>{@code fechaPago} es la fecha del resultado: nula solo mientras el pago
 *       está {@code PENDIENTE}. Un pago {@code APROBADO} siempre tiene referencia.</li>
 *   <li>No se guarda ningún dato de tarjeta ni de cuenta bancaria.</li>
 *   <li>{@code version} protege el registro contra dos confirmaciones concurrentes.</li>
 * </ul>
 */
@Entity
@Table(name = "pago")
@Check(constraints = "monto > 0"
        + " and ((estado = 'PENDIENTE' and fecha_pago is null)"
        + " or (estado <> 'PENDIENTE' and fecha_pago is not null))"
        + " and (estado <> 'APROBADO' or referencia_pago is not null)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sin cascade: guardar o borrar un pago nunca modifica el pedido
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true, updatable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Metodo metodo;

    // Subtotal + flete del pedido, calculado por el servidor; nunca viene del JSON
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDIENTE'")
    @Column(nullable = false, length = 15)
    private Estado estado = Estado.PENDIENTE;

    // UUID enviado por el cliente en X-Idempotency-Key; no cambia nunca
    @Column(name = "clave_idempotencia", nullable = false, unique = true, updatable = false)
    private UUID claveIdempotencia;

    // Referencia de la operación en la pasarela (comprobante); nula mientras no exista
    @Column(name = "referencia_pago", unique = true, length = 60)
    private String referenciaPago;

    // Momento en que se registró el intento; la asigna el servidor
    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Fecha del resultado definitivo; nula mientras el pago está PENDIENTE
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Version
    @ColumnDefault("0")
    @Column(nullable = false)
    private Long version;

    /** Estados del pago; mismos valores que el CHECK de schema.sql. */
    public enum Estado {
        PENDIENTE,
        APROBADO,
        RECHAZADO,
        EXPIRADO
    }

    /** Métodos admitidos. En el Sprint 5 solo existe el simulador de pasarela. */
    public enum Metodo {
        SIMULADOR
    }
}
