package com.agrovalleconnect.agrovalle_connect.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.agrovalleconnect.agrovalle_connect.dto.CrearPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.ItemPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.event.PedidoCreadoEvent;
import com.agrovalleconnect.agrovalle_connect.exception.AccesoDenegadoException;
import com.agrovalleconnect.agrovalle_connect.exception.MunicipioSinTarifaException;
import com.agrovalleconnect.agrovalle_connect.exception.ProductoNoDisponibleException;
import com.agrovalleconnect.agrovalle_connect.exception.RecursoNoEncontradoException;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Pedido;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.repository.CompradorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.PedidoRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.agrovalleconnect.agrovalle_connect.service.flete.CalculadoraFlete;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private CompradorRepository compradores;

    @Mock
    private ProductoRepository productos;

    @Mock
    private PedidoRepository pedidos;

    @Mock
    private CalculadoraFlete flete;

    @Mock
    private ApplicationEventPublisher eventos;

    private PedidoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PedidoService(compradores, productos, pedidos, flete, eventos);
    }

    private void comprador() {
        when(compradores.findByUsuarioCorreo("comprador@test.co"))
                .thenReturn(Optional.of(Comprador.builder().id(7L).build()));
    }

    private Producto producto(long id, String stock, String municipio) {
        Municipio lugar = new Municipio();
        lugar.setNombre(municipio);
        Agricultor agricultor = new Agricultor();
        agricultor.setId(9L);
        Finca finca = new Finca();
        finca.setMunicipio(lugar);
        finca.setAgricultor(agricultor);
        Producto producto = Producto.builder().id(id).finca(finca)
                .precioUnitario(new BigDecimal("3200.00"))
                .cantidadDisponible(new BigDecimal(stock)).build();
        when(productos.findById(id)).thenReturn(Optional.of(producto));
        return producto;
    }

    private CrearPedidoRequest request(ItemPedidoRequest... items) {
        return new CrearPedidoRequest(List.of(items));
    }

    private ItemPedidoRequest item(long id, String cantidad) {
        return new ItemPedidoRequest(id, new BigDecimal(cantidad));
    }

    @Test
    void crear_valido_reservaAntesDeGuardarYPublicaDespues() {
        comprador();
        Producto producto = producto(12L, "10.00", "Palmira");
        when(flete.calcularTotal(List.of("Palmira"))).thenReturn(new BigDecimal("10000.00"));
        doAnswer(invocacion -> {
            assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("8.00");
            Pedido pedido = invocacion.getArgument(0);
            pedido.setId(41L);
            return pedido;
        }).when(pedidos).save(any(Pedido.class));

        var respuesta = servicio.crear("comprador@test.co", request(item(12L, "2.00")));

        assertThat(respuesta.id()).isEqualTo(41L);
        assertThat(respuesta.estado()).isEqualTo("PENDIENTE_PAGO");
        assertThat(respuesta.subtotal()).isEqualByComparingTo("6400.00");
        assertThat(respuesta.costoFlete()).isEqualByComparingTo("10000.00");
        assertThat(respuesta.total()).isEqualByComparingTo("16400.00");
        assertThat(respuesta.items()).hasSize(1);
        assertThat(respuesta.items().getFirst().precioUnitario()).isEqualByComparingTo("3200.00");
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        InOrder orden = inOrder(pedidos, eventos);
        orden.verify(pedidos).save(captor.capture());
        orden.verify(eventos).publishEvent(new PedidoCreadoEvent(41L, 7L, List.of(9L)));
        orden.verifyNoMoreInteractions();
        assertThat(captor.getValue().getComprador().getId()).isEqualTo(7L);
        assertThat(captor.getValue().getDetalles().getFirst().getPedido()).isSameAs(captor.getValue());
        producto.setPrecioUnitario(BigDecimal.ONE);
        assertThat(captor.getValue().getDetalles().getFirst().getPrecioUnitario())
                .isEqualByComparingTo("3200.00");
    }

    @Test
    void crear_repetidos_consolidaUnDetalleYAgotaStockExacto() {
        comprador();
        Producto producto = producto(12L, "10.00", "Palmira");
        when(flete.calcularTotal(List.of("Palmira"))).thenReturn(new BigDecimal("10000.00"));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var respuesta = servicio.crear("comprador@test.co", request(item(12L, "5"), item(12L, "5")));

        assertThat(producto.getCantidadDisponible()).isZero();
        assertThat(producto.getEstado()).isEqualTo(Producto.Estado.AGOTADO);
        assertThat(respuesta.items()).hasSize(1);
        assertThat(respuesta.items().getFirst().cantidad()).isEqualByComparingTo("10");
        verify(productos).findById(12L);
    }

    @Test
    void crear_sinPerfil_noGuardaNiPublica() {
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "2"))))
                .isInstanceOf(AccesoDenegadoException.class);
        verifyNoInteractions(productos, pedidos, flete, eventos);
    }

    @Test
    void crear_segundoProductoInexistente_noReservaElPrimero() {
        comprador();
        Producto producto = producto(12L, "10", "Palmira");
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "2"), item(18L, "1"))))
                .isInstanceOf(RecursoNoEncontradoException.class);
        assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("10");
        verifyNoInteractions(pedidos, flete, eventos);
    }

    @Test
    void crear_inactivo_noReservaNiPublica() {
        comprador();
        Producto producto = producto(12L, "10", "Palmira");
        producto.setEstado(Producto.Estado.INACTIVO);
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "2"))))
                .isInstanceOf(ProductoNoDisponibleException.class).hasMessage("Producto no disponible");
        assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("10");
        verifyNoInteractions(pedidos, flete, eventos);
    }

    @Test
    void crear_sinTarifa_noReservaNiPublica() {
        comprador();
        Producto producto = producto(12L, "10", "Cali");
        when(flete.calcularTotal(List.of("Cali"))).thenThrow(new MunicipioSinTarifaException());
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "2"))))
                .isInstanceOf(MunicipioSinTarifaException.class);
        assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("10");
        verifyNoInteractions(pedidos, eventos);
    }

    @Test
    void crear_errorAlGuardar_noPublica() {
        comprador();
        producto(12L, "10", "Palmira");
        when(flete.calcularTotal(List.of("Palmira"))).thenReturn(BigDecimal.TEN);
        when(pedidos.save(any(Pedido.class))).thenThrow(new IllegalStateException("Fallo de persistencia"));
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "2"))))
                .isInstanceOf(IllegalStateException.class).hasMessage("Fallo de persistencia");
        verifyNoInteractions(eventos);
    }

    @Test
    void crear_variosMunicipios_delegaFleteYConservaPreciosDecimales() {
        comprador();
        producto(12L, "10", "Palmira");
        producto(18L, "10", "Dagua");
        when(flete.calcularTotal(List.of("Palmira", "Dagua"))).thenReturn(new BigDecimal("22000.00"));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        var respuesta = servicio.crear("comprador@test.co", request(item(12L, "2"), item(18L, "1.50")));
        assertThat(respuesta.subtotal()).isEqualByComparingTo("11200.00");
        assertThat(respuesta.total()).isEqualByComparingTo("33200.00");
        verify(eventos).publishEvent(new PedidoCreadoEvent(null, 7L, List.of(9L)));
    }

    @Test
    void crear_stockInsuficienteEnSegundaLinea_noReservaNinguna() {
        comprador();
        Producto primero = producto(12L, "10", "Palmira");
        Producto segundo = producto(18L, "3", "Palmira");
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "2"), item(18L, "5"))))
                .isInstanceOf(ProductoNoDisponibleException.class).hasMessage("Stock insuficiente");
        assertThat(primero.getCantidadDisponible()).isEqualByComparingTo("10");
        assertThat(segundo.getCantidadDisponible()).isEqualByComparingTo("3");
        verifyNoInteractions(pedidos, flete, eventos);
    }

    @Test
    void crear_repetidosSuperanStock_noReservaNiPublica() {
        comprador();
        Producto producto = producto(12L, "10", "Palmira");
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "6"), item(12L, "6"))))
                .isInstanceOf(ProductoNoDisponibleException.class).hasMessage("Stock insuficiente");
        assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("10");
        verifyNoInteractions(pedidos, flete, eventos);
    }

    @Test
    void crear_agotadoAunqueTengaStock_noReservaNiPublica() {
        comprador();
        Producto producto = producto(12L, "10", "Palmira");
        producto.setEstado(Producto.Estado.AGOTADO);
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "2"))))
                .isInstanceOf(ProductoNoDisponibleException.class).hasMessage("Producto agotado");
        assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("10");
        verifyNoInteractions(pedidos, flete, eventos);
    }

    @Test
    void crear_activoSinStock_noReservaNiPublica() {
        comprador();
        producto(12L, "0", "Palmira");
        assertThatThrownBy(() -> servicio.crear("comprador@test.co", request(item(12L, "2"))))
                .isInstanceOf(ProductoNoDisponibleException.class).hasMessage("Producto agotado");
        verifyNoInteractions(pedidos, flete, eventos);
    }
}
