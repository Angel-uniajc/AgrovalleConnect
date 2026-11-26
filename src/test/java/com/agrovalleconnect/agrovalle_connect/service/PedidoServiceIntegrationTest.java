package com.agrovalleconnect.agrovalle_connect.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.agrovalleconnect.agrovalle_connect.dto.CrearPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.dto.ItemPedidoRequest;
import com.agrovalleconnect.agrovalle_connect.event.PedidoCreadoEvent;
import com.agrovalleconnect.agrovalle_connect.listener.PedidoCreadoListener;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Pedido;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.PedidoRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.agrovalleconnect.agrovalle_connect.service.flete.CalculadoraFlete;
import com.agrovalleconnect.agrovalle_connect.service.flete.FletePalmira;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Transacciones reales del servicio: la prueba no envuelve crear() en otra transacción. */
@DataJpaTest
@Import({PedidoService.class, CalculadoraFlete.class, FletePalmira.class, PedidoCreadoListener.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@RecordApplicationEvents
@ExtendWith(OutputCaptureExtension.class)
class PedidoServiceIntegrationTest {

    @Autowired
    private PedidoService servicio;

    @Autowired
    private PedidoRepository pedidos;

    @Autowired
    private ProductoRepository productos;

    @Autowired
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transacciones;

    @Autowired
    private ApplicationEvents eventos;

    @MockitoSpyBean
    private PedidoCreadoListener listener;

    private Long productoId;

    private Long compradorId;

    private Long agricultorId;

    private String correo;

    @BeforeEach
    void datosPropios() {
        String sufijo = UUID.randomUUID().toString().substring(0, 16);
        String documento = String.format("%010d", Integer.toUnsignedLong(sufijo.hashCode()));
        correo = sufijo + "@comprador.test";
        new TransactionTemplate(transacciones).executeWithoutResult(estado -> {
            Usuario vendedor = usuario(sufijo + "@agricultor.test", Usuario.Rol.AGRICULTOR);
            Agricultor agricultor = Agricultor.builder().cedula(documento)
                    .ubicacionValle("Palmira").usuario(vendedor).build();
            em.persist(agricultor);
            agricultorId = agricultor.getId();
            // DataJpaTest no ejecuta los cargadores: cada prueba crea sus catálogos.
            Municipio municipio = em.createQuery("from Municipio where nombre = :nombre", Municipio.class)
                    .setParameter("nombre", "Palmira").getResultStream().findFirst().orElseGet(() -> {
                        Municipio nuevo = Municipio.builder().nombre("Palmira").build();
                        em.persist(nuevo);
                        return nuevo;
                    });
            Categoria categoria = Categoria.builder().nombre("Fruta " + sufijo.substring(0, 8)).build();
            em.persist(categoria);
            Finca finca = Finca.builder().nombre("Finca de pedidos").agricultor(agricultor)
                    .municipio(municipio).build();
            em.persist(finca);
            Producto producto = Producto.builder().tipo("Mango").finca(finca).categoria(categoria)
                    .precioUnitario(new BigDecimal("3200.00")).cantidadDisponible(new BigDecimal("10.00"))
                    .fechaCosecha(LocalDate.now()).build();
            em.persist(producto);
            productoId = producto.getId();
            Comprador comprador = Comprador.builder().usuario(usuario(correo, Usuario.Rol.COMPRADOR))
                    .documento(documento).tipoComprador(Comprador.TipoComprador.INDIVIDUAL).build();
            em.persist(comprador);
            compradorId = comprador.getId();
        });
    }

    private Usuario usuario(String email, Usuario.Rol rol) {
        Usuario usuario = Usuario.builder().nombre("Usuario de pedidos").correo(email)
                .passwordHash("hash_de_prueba").rol(rol).fechaRegistro(LocalDateTime.now()).build();
        em.persist(usuario);
        return usuario;
    }

    private CrearPedidoRequest request() {
        return new CrearPedidoRequest(List.of(new ItemPedidoRequest(productoId, new BigDecimal("10.00"))));
    }

    @Test
    void crear_confirmaPedidoDetallesStockYActivaListener(CapturedOutput salida) {
        var respuesta = servicio.crear(correo, request());
        new TransactionTemplate(transacciones).executeWithoutResult(estado -> {
            Producto producto = productos.findById(productoId).orElseThrow();
            assertThat(producto.getCantidadDisponible()).isZero();
            assertThat(producto.getEstado()).isEqualTo(Producto.Estado.AGOTADO);
            Pedido pedido = pedidos.findById(respuesta.id()).orElseThrow();
            assertThat(pedido.getComprador().getId()).isEqualTo(compradorId);
            assertThat(pedido.getDetalles()).hasSize(1);
            assertThat(pedido.getDetalles().getFirst().getId().getProductoId()).isEqualTo(productoId);
            assertThat(pedido.getCostoFlete()).isEqualByComparingTo("10000.00");
        });
        assertThat(eventos.stream(PedidoCreadoEvent.class)).containsExactly(
                new PedidoCreadoEvent(respuesta.id(), compradorId, List.of(agricultorId)));
        assertThat(salida.getOut()).contains("Aviso de pedido creado: pedidoId=" + respuesta.id(),
                "agricultorId=" + agricultorId);
    }

    @Test
    void crear_listenerFalla_reviertePedidoDetallesYStock() {
        long antes = pedidos.count();
        Long detallesAntes = new TransactionTemplate(transacciones)
                .execute(estado -> em.createQuery("select count(d) from DetallePedido d", Long.class).getSingleResult());
        doThrow(new IllegalStateException("Fallo del observador")).when(listener).registrarPedido(any());

        assertThatThrownBy(() -> servicio.crear(correo, request()))
                .isInstanceOf(IllegalStateException.class).hasMessage("Fallo del observador");

        assertThat(pedidos.count()).isEqualTo(antes);
        new TransactionTemplate(transacciones).executeWithoutResult(estado -> {
            assertThat(em.createQuery("select count(d) from DetallePedido d", Long.class).getSingleResult())
                    .isEqualTo(detallesAntes);
            Producto producto = productos.findById(productoId).orElseThrow();
            assertThat(producto.getCantidadDisponible()).isEqualByComparingTo("10.00");
            assertThat(producto.getEstado()).isEqualTo(Producto.Estado.ACTIVO);
        });
    }
}
