package com.agrovalleconnect.agrovalle_connect.repository;

import com.agrovalleconnect.agrovalle_connect.model.Pago;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** HU-07: acceso a los pagos. Contrato en docs/contrato-pago-hu07.md. */
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** Pago del pedido, si ya existe un intento (hay como máximo uno). */
    Optional<Pago> findByPedidoId(Long pedidoId);

    /** Intento registrado con esta clave; permite repetir la respuesta sin volver a cobrar. */
    Optional<Pago> findByClaveIdempotencia(UUID claveIdempotencia);
}
