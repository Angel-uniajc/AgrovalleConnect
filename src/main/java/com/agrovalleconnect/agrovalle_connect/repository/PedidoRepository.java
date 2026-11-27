package com.agrovalleconnect.agrovalle_connect.repository;

import com.agrovalleconnect.agrovalle_connect.model.Pedido;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * HU-12: acceso a pedidos. Los detalles se guardan en cascada con el pedido,
 * por eso no hay repositorio propio para {@code DetallePedido}.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * HU-07: carga el pedido con bloqueo de escritura (SELECT ... FOR UPDATE) hasta
     * el fin de la transacción. Pagar, rechazar, expirar o cancelar un pedido debe
     * empezar por aquí para que dos transiciones del mismo pedido no se crucen.
     *
     * @param id identificador del pedido
     * @return el pedido bloqueado, si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> buscarParaActualizar(@Param("id") Long id);
}
