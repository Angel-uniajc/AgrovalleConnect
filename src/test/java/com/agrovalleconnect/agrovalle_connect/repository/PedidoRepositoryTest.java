package com.agrovalleconnect.agrovalle_connect.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.DetallePedido;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Pedido;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Comprador comprador;
    private Producto mango;
    private Producto tomate;

    @BeforeEach
    void prepararDatos() {
        Usuario usuarioAgricultor = entityManager.persist(Usuario.builder()
                .nombre("Pedro")
                .correo("pedro@mail.com")
                .passwordHash("hash_de_prueba")
                .rol(Usuario.Rol.AGRICULTOR)
                .fechaRegistro(LocalDateTime.now())
                .build());
        Agricultor agricultor = entityManager.persist(Agricultor.builder()
                .cedula("123456789")
                .ubicacionValle("Palmira")
                .usuario(usuarioAgricultor)
                .build());
        Municipio municipio = entityManager.persist(Municipio.builder().nombre("Palmira").build());
        Finca finca = entityManager.persist(Finca.builder()
                .nombre("El Paraíso")
                .agricultor(agricultor)
                .municipio(municipio)
                .build());
        Categoria frutas = entityManager.persist(Categoria.builder().nombre("Frutas").build());
        Categoria hortalizas = entityManager.persist(Categoria.builder().nombre("Hortalizas").build());
        mango = entityManager.persist(producto("Mango", "3200.00", finca, frutas));
        tomate = entityManager.persist(producto("Tomate", "2500.00", finca, hortalizas));

        Usuario usuarioComprador = entityManager.persist(Usuario.builder()
                .nombre("Sofía")
                .correo("sofia@correo.com")
                .passwordHash("hash_de_prueba")
                .rol(Usuario.Rol.COMPRADOR)
                .fechaRegistro(LocalDateTime.now())
                .build());
        comprador = entityManager.persist(Comprador.builder()
                .documento("12345678")
                .tipoComprador(Comprador.TipoComprador.RESTAURANTE)
                .usuario(usuarioComprador)
                .build());
        entityManager.flush();
    }

    private static Producto producto(String tipo, String precio, Finca finca, Categoria categoria) {
        return Producto.builder()
                .tipo(tipo)
                .cantidadDisponible(new BigDecimal("50.00"))
                .precioUnitario(new BigDecimal(precio))
                .fechaCosecha(LocalDate.of(2026, 11, 20))
                .finca(finca)
                .categoria(categoria)
                .build();
    }

    private Pedido pedido(String costoFlete) {
        return Pedido.builder()
                .comprador(comprador)
                .costoFlete(costoFlete == null ? null : new BigDecimal(costoFlete))
                .build();
    }

    private Pedido pedidoConDosLineas() {
        Pedido pedido = pedido("10000.00");
        pedido.agregarDetalle(mango, new BigDecimal("2.00"), new BigDecimal("3200.00"));
        pedido.agregarDetalle(tomate, new BigDecimal("1.50"), new BigDecimal("2500.00"));
        return pedido;
    }

    private long contarDetalles(Long pedidoId) {
        return ((Number) entityManager.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM detalle_pedido WHERE pedido_id = ?1")
                .setParameter(1, pedidoId)
                .getSingleResult()).longValue();
    }

    // ---------- Guardado en cascada ----------

    @Test
    void guardarPedido_guardaSusDetallesEnCascada() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());
        entityManager.clear();

        Pedido recuperado = pedidoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(recuperado.getComprador().getId()).isEqualTo(comprador.getId());
        assertThat(recuperado.getCostoFlete()).isEqualByComparingTo("10000.00");
        assertThat(recuperado.getDetalles())
                .extracting(d -> d.getProducto().getTipo(), DetallePedido::getCantidad,
                        DetallePedido::getPrecioUnitario)
                .containsExactlyInAnyOrder(
                        tuple(
                                "Mango", new BigDecimal("2.00"), new BigDecimal("3200.00")),
                        tuple(
                                "Tomate", new BigDecimal("1.50"), new BigDecimal("2500.00")));
        assertThat(contarDetalles(guardado.getId())).isEqualTo(2);
    }

    @Test
    void detalle_usaClaveCompuestaPedidoYProducto() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());
        entityManager.clear();

        DetallePedido detalle = pedidoRepository.findById(guardado.getId()).orElseThrow()
                .getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(mango.getId()))
                .findFirst().orElseThrow();
        assertThat(detalle.getId().getPedidoId()).isEqualTo(guardado.getId());
        assertThat(detalle.getId().getProductoId()).isEqualTo(mango.getId());
        assertThat(detalle.subtotal()).isEqualByComparingTo("6400.00");
    }

    @Test
    void pedidoNuevo_quedaEnPendientePagoConFechaDelServidor() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());
        entityManager.clear();

        Pedido recuperado = pedidoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(recuperado.getEstado()).isEqualTo(Pedido.Estado.PENDIENTE_PAGO);
        assertThat(recuperado.getFechaPedido()).isNotNull().isAfter(antes);
    }

    @Test
    void pedidoCreadoSinBuilder_tambienEmpiezaEnPendientePago() {
        assertThat(new Pedido().getEstado()).isEqualTo(Pedido.Estado.PENDIENTE_PAGO);
    }

    @Test
    void insercionSinEstado_usaElDefaultDeLaBase() {
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO pedido (comprador_id, costo_flete) VALUES (?1, 0)")
                .setParameter(1, comprador.getId())
                .executeUpdate();

        Object[] fila = (Object[]) entityManager.getEntityManager()
                .createNativeQuery("SELECT estado, fecha_pedido FROM pedido")
                .getSingleResult();
        assertThat(fila[0]).isEqualTo("PENDIENTE_PAGO");
        assertThat(fila[1]).isNotNull();
    }

    @Test
    void precioDelDetalle_noCambiaSiCambiaElPrecioDelProducto() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());
        mango.setPrecioUnitario(new BigDecimal("9999.00"));
        entityManager.flush();
        entityManager.clear();

        DetallePedido detalleMango = pedidoRepository.findById(guardado.getId()).orElseThrow()
                .getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(mango.getId()))
                .findFirst().orElseThrow();
        assertThat(detalleMango.getPrecioUnitario()).isEqualByComparingTo("3200.00");
    }

    @Test
    void cambiarEstado_seGuarda() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());
        guardado.setEstado(Pedido.Estado.PAGADO);
        pedidoRepository.saveAndFlush(guardado);
        entityManager.clear();

        assertThat(pedidoRepository.findById(guardado.getId()).orElseThrow().getEstado())
                .isEqualTo(Pedido.Estado.PAGADO);
    }

    // ---------- Bloqueo para transiciones de pago (HU-07) ----------

    @Test
    void buscarParaActualizar_devuelveElPedidoConSusDetalles() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());
        entityManager.clear();

        Pedido bloqueado = pedidoRepository.buscarParaActualizar(guardado.getId()).orElseThrow();

        assertThat(bloqueado.getEstado()).isEqualTo(Pedido.Estado.PENDIENTE_PAGO);
        assertThat(bloqueado.getDetalles()).hasSize(2);
    }

    @Test
    void buscarParaActualizar_pedidoInexistente_devuelveVacio() {
        assertThat(pedidoRepository.buscarParaActualizar(999_999L)).isEmpty();
    }

    // ---------- Borrado ----------

    @Test
    void quitarDetalleDeLaLista_loBorra() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());

        guardado.getDetalles().removeIf(d -> d.getProducto().getId().equals(tomate.getId()));
        pedidoRepository.saveAndFlush(guardado);

        assertThat(contarDetalles(guardado.getId())).isEqualTo(1);
    }

    @Test
    void borrarPedido_borraDetallesPeroNoProductosNiComprador() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());

        pedidoRepository.delete(guardado);
        pedidoRepository.flush();
        entityManager.clear();

        assertThat(pedidoRepository.count()).isZero();
        assertThat(contarDetalles(guardado.getId())).isZero();
        assertThat(entityManager.find(Producto.class, mango.getId())).isNotNull();
        assertThat(entityManager.find(Producto.class, tomate.getId())).isNotNull();
        assertThat(entityManager.find(Comprador.class, comprador.getId())).isNotNull();
    }

    @Test
    void borrarPedidoConSql_laFkBorraLosDetallesEnCascada() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedidoConDosLineas());
        entityManager.clear();

        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM pedido WHERE id = ?1")
                .setParameter(1, guardado.getId())
                .executeUpdate();

        assertThat(contarDetalles(guardado.getId())).isZero();
    }

    // ---------- Restricciones de pedido ----------

    @Test
    void pedidoSinComprador_violaNotNull() {
        Pedido sinComprador = pedido("10000.00");
        sinComprador.setComprador(null);

        assertThatThrownBy(() -> pedidoRepository.saveAndFlush(sinComprador))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void pedidoSinCostoFlete_violaNotNull() {
        Pedido sinFlete = pedido(null);

        assertThatThrownBy(() -> pedidoRepository.saveAndFlush(sinFlete))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void costoFleteNegativo_violaCheck() {
        Pedido negativo = pedido("-1.00");

        assertThatThrownBy(() -> pedidoRepository.saveAndFlush(negativo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void costoFleteCero_esValido() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedido("0.00"));

        assertThat(guardado.getId()).isNotNull();
    }

    @Test
    void estadoFueraDelCatalogo_violaCheck() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedido("0.00"));

        assertThatThrownBy(() -> entityManager.getEntityManager()
                .createNativeQuery("UPDATE pedido SET estado = 'DESCONOCIDO' WHERE id = ?1")
                .setParameter(1, guardado.getId())
                .executeUpdate())
                .isInstanceOf(PersistenceException.class);
    }

    // ---------- Restricciones de detalle ----------

    @Test
    void detalleConCantidadCero_violaCheck() {
        Pedido pedido = pedido("0.00");
        pedido.agregarDetalle(mango, BigDecimal.ZERO, new BigDecimal("3200.00"));

        assertThatThrownBy(() -> pedidoRepository.saveAndFlush(pedido))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void detalleConPrecioCero_violaCheck() {
        Pedido pedido = pedido("0.00");
        pedido.agregarDetalle(mango, new BigDecimal("1.00"), BigDecimal.ZERO);

        assertThatThrownBy(() -> pedidoRepository.saveAndFlush(pedido))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void detalleSinCantidad_violaNotNull() {
        Pedido pedido = pedido("0.00");
        pedido.agregarDetalle(mango, null, new BigDecimal("3200.00"));

        assertThatThrownBy(() -> pedidoRepository.saveAndFlush(pedido))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void mismoProductoDosVecesEnUnPedido_violaLaClavePrimaria() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedido("0.00"));

        assertThatThrownBy(() -> {
            for (int i = 0; i < 2; i++) {
                entityManager.getEntityManager()
                        .createNativeQuery("INSERT INTO detalle_pedido "
                                + "(pedido_id, producto_id, cantidad, precio_unitario) "
                                + "VALUES (?1, ?2, 1, 3200)")
                        .setParameter(1, guardado.getId())
                        .setParameter(2, mango.getId())
                        .executeUpdate();
            }
        }).isInstanceOf(PersistenceException.class);
    }
}
