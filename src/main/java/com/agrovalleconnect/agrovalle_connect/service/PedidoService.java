package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.CrearPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PedidoResponse;
import com.agrovalleconnect.agrovalle_connect.dto.ItemPedidoResponse;
import com.agrovalleconnect.agrovalle_connect.event.PedidoCreadoEvent;
import com.agrovalleconnect.agrovalle_connect.exception.AccesoDenegadoException;
import com.agrovalleconnect.agrovalle_connect.exception.ProductoNoDisponibleException;
import com.agrovalleconnect.agrovalle_connect.exception.ProductoAgotadoException;
import com.agrovalleconnect.agrovalle_connect.exception.StockInsuficienteException;
import com.agrovalleconnect.agrovalle_connect.exception.RecursoNoEncontradoException;
import com.agrovalleconnect.agrovalle_connect.model.Pedido;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.PedidoRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.agrovalleconnect.agrovalle_connect.service.flete.CalculadoraFlete;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Creación atómica del pedido. El correo procede de la autenticación, nunca del cuerpo JSON. */
@Service
public class PedidoService {

    private final CompradorRepository compradores;

    private final ProductoRepository productos;

    private final PedidoRepository pedidos;

    private final CalculadoraFlete flete;

    private final ApplicationEventPublisher eventos;

    public PedidoService(CompradorRepository compradores, ProductoRepository productos,
            PedidoRepository pedidos, CalculadoraFlete flete, ApplicationEventPublisher eventos) {
        this.compradores = compradores;
        this.productos = productos;
        this.pedidos = pedidos;
        this.flete = flete;
        this.eventos = eventos;
    }

    /**
     * Valida antes de reservar y publica después de guardar, dentro de la misma transacción.
     * El request debe haber pasado Bean Validation. Los efectos externos del evento requieren AFTER_COMMIT.
     */
    @Transactional
    public PedidoResponse crear(String correo, CrearPedidoRequest request) {
        var comprador = compradores.findByUsuarioCorreo(correo)
                .orElseThrow(() -> new AccesoDenegadoException("Acceso denegado"));
        Map<Long, BigDecimal> cantidades = agruparCantidades(request);
        List<Producto> seleccion = validarProductos(cantidades);
        BigDecimal costoFlete = flete.calcularTotal(seleccion.stream()
                .map(producto -> producto.getFinca().getMunicipio().getNombre()).toList());
        Pedido pedido = Pedido.builder().comprador(comprador).costoFlete(costoFlete).build();
        reservar(pedido, seleccion, cantidades);
        // Los productos están gestionados por JPA: dirty checking persiste la reserva al confirmar.
        Pedido guardado = pedidos.save(pedido);
        eventos.publishEvent(new PedidoCreadoEvent(guardado.getId(), comprador.getId(), seleccion.stream()
                .map(producto -> producto.getFinca().getAgricultor().getId()).distinct().toList()));
        return respuesta(guardado);
    }

    private Map<Long, BigDecimal> agruparCantidades(CrearPedidoRequest request) {
        Map<Long, BigDecimal> cantidades = new LinkedHashMap<>();
        request.items().forEach(item -> cantidades.merge(item.idProducto(), item.cantidad(), BigDecimal::add));
        return cantidades;
    }

    private List<Producto> validarProductos(Map<Long, BigDecimal> cantidades) {
        List<Producto> seleccion = new ArrayList<>();
        for (var entrada : cantidades.entrySet()) {
            Producto producto = productos.findById(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
            if (producto.getEstado() == Producto.Estado.AGOTADO || producto.getCantidadDisponible().signum() == 0) {
                throw new ProductoAgotadoException(producto.getId());
            }
            if (producto.getEstado() != Producto.Estado.ACTIVO) {
                throw new ProductoNoDisponibleException("Producto no disponible");
            }
            if (entrada.getValue().compareTo(producto.getCantidadDisponible()) > 0) {
                throw new StockInsuficienteException(producto.getId(), entrada.getValue(),
                        producto.getCantidadDisponible());
            }
            seleccion.add(producto);
        }
        return seleccion;
    }

    private void reservar(Pedido pedido, List<Producto> seleccion, Map<Long, BigDecimal> cantidades) {
        for (Producto producto : seleccion) {
            BigDecimal cantidad = cantidades.get(producto.getId());
            pedido.agregarDetalle(producto, cantidad, producto.getPrecioUnitario());
            producto.setCantidadDisponible(producto.getCantidadDisponible().subtract(cantidad));
            if (producto.getCantidadDisponible().signum() == 0) {
                producto.setEstado(Producto.Estado.AGOTADO);
            }
        }
    }

    private PedidoResponse respuesta(Pedido guardado) {
        List<ItemPedidoResponse> items = guardado.getDetalles().stream()
                .map(detalle -> new ItemPedidoResponse(detalle.getProducto().getId(),
                        detalle.getCantidad(), detalle.getPrecioUnitario())).toList();
        BigDecimal subtotal = guardado.getDetalles().stream().map(detalle -> detalle.subtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PedidoResponse(guardado.getId(), guardado.getEstado().name(), items,
                subtotal, guardado.getCostoFlete(), subtotal.add(guardado.getCostoFlete()));
    }
}
