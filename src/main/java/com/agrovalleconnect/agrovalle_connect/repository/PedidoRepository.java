package com.agrovalleconnect.agrovalle_connect.repository;

import com.agrovalleconnect.agrovalle_connect.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * HU-12: acceso a pedidos. Los detalles se guardan en cascada con el pedido,
 * por eso no hay repositorio propio para {@code DetallePedido}.
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
