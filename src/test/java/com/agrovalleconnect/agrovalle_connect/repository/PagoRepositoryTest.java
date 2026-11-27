package com.agrovalleconnect.agrovalle_connect.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Pago;
import com.agrovalleconnect.agrovalle_connect.model.Pedido;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@DataJpaTest
class PagoRepositoryTest {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Pedido pedido;
    private Pedido otroPedido;

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
        Categoria categoria = entityManager.persist(Categoria.builder().nombre("Frutas").build());
        Producto mango = entityManager.persist(Producto.builder()
                .tipo("Mango")
                .cantidadDisponible(new BigDecimal("48.00"))
                .precioUnitario(new BigDecimal("3200.00"))
                .fechaCosecha(LocalDate.of(2026, 11, 20))
                .finca(finca)
                .categoria(categoria)
                .build());

        Usuario usuarioComprador = entityManager.persist(Usuario.builder()
                .nombre("Sofía")
                .correo("sofia@correo.com")
                .passwordHash("hash_de_prueba")
                .rol(Usuario.Rol.COMPRADOR)
                .fechaRegistro(LocalDateTime.now())
                .build());
        Comprador comprador = entityManager.persist(Comprador.builder()
                .documento("12345678")
                .tipoComprador(Comprador.TipoComprador.RESTAURANTE)
                .usuario(usuarioComprador)
                .build());

        pedido = Pedido.builder().comprador(comprador).costoFlete(new BigDecimal("10000.00")).build();
        pedido.agregarDetalle(mango, new BigDecimal("2.00"), new BigDecimal("3200.00"));
        entityManager.persist(pedido);
        otroPedido = entityManager.persist(Pedido.builder()
                .comprador(comprador).costoFlete(new BigDecimal("10000.00")).build());
        entityManager.flush();
    }

    private Pago pendiente(Pedido deQuePedido) {
        return Pago.builder()
                .pedido(deQuePedido)
                .metodo(Pago.Metodo.SIMULADOR)
                .monto(new BigDecimal("16400.00"))
                .claveIdempotencia(UUID.randomUUID())
                .build();
    }

    private Pago aprobado(Pedido deQuePedido, String referencia) {
        Pago pago = pendiente(deQuePedido);
        pago.setEstado(Pago.Estado.APROBADO);
        pago.setReferenciaPago(referencia);
        pago.setFechaPago(LocalDateTime.now());
        return pago;
    }

    private Object[] filaNativa(String sql, Object... parametros) {
        var consulta = entityManager.getEntityManager().createNativeQuery(sql);
        for (int i = 0; i < parametros.length; i++) {
            consulta.setParameter(i + 1, parametros[i]);
        }
        return (Object[]) consulta.getSingleResult();
    }

    // ---------- Registro del intento ----------

    @Test
    void pagoNuevo_quedaPendienteSinResultadoYConFechaDeCreacion() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        Pago guardado = pagoRepository.saveAndFlush(pendiente(pedido));
        entityManager.clear();

        Pago recuperado = pagoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(recuperado.getEstado()).isEqualTo(Pago.Estado.PENDIENTE);
        assertThat(recuperado.getMetodo()).isEqualTo(Pago.Metodo.SIMULADOR);
        assertThat(recuperado.getMonto()).isEqualByComparingTo("16400.00");
        assertThat(recuperado.getPedido().getId()).isEqualTo(pedido.getId());
        assertThat(recuperado.getReferenciaPago()).isNull();
        assertThat(recuperado.getFechaPago()).isNull();
        assertThat(recuperado.getFechaCreacion()).isNotNull().isAfter(antes);
        assertThat(recuperado.getVersion()).isZero();
    }

    @Test
    void pagoCreadoSinBuilder_tambienEmpiezaPendiente() {
        assertThat(new Pago().getEstado()).isEqualTo(Pago.Estado.PENDIENTE);
    }

    @Test
    void insercionSinEstado_usaLosDefaultsDeLaBase() {
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO pago (pedido_id, metodo, monto, clave_idempotencia) "
                        + "VALUES (?1, 'SIMULADOR', 100, ?2)")
                .setParameter(1, pedido.getId())
                .setParameter(2, UUID.randomUUID())
                .executeUpdate();

        Object[] fila = filaNativa("SELECT estado, fecha_creacion, version FROM pago");
        assertThat(fila[0]).isEqualTo("PENDIENTE");
        assertThat(fila[1]).isNotNull();
        assertThat(((Number) fila[2]).longValue()).isZero();
    }

    @Test
    void buscarPorPedidoYPorClave_encuentraElIntento() {
        Pago guardado = pagoRepository.saveAndFlush(pendiente(pedido));
        entityManager.clear();

        assertThat(pagoRepository.findByPedidoId(pedido.getId()))
                .get().extracting(Pago::getId).isEqualTo(guardado.getId());
        assertThat(pagoRepository.findByClaveIdempotencia(guardado.getClaveIdempotencia()))
                .get().extracting(Pago::getId).isEqualTo(guardado.getId());
        assertThat(pagoRepository.findByPedidoId(otroPedido.getId())).isEmpty();
        assertThat(pagoRepository.findByClaveIdempotencia(UUID.randomUUID())).isEmpty();
    }

    // ---------- Resultado ----------

    @Test
    void aprobarPago_guardaReferenciaYFechaEIncrementaLaVersion() {
        Pago guardado = pagoRepository.saveAndFlush(pendiente(pedido));

        guardado.setEstado(Pago.Estado.APROBADO);
        guardado.setReferenciaPago("SIM-" + pedido.getId() + "-001");
        guardado.setFechaPago(LocalDateTime.now());
        pagoRepository.saveAndFlush(guardado);
        entityManager.clear();

        Pago recuperado = pagoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(recuperado.getEstado()).isEqualTo(Pago.Estado.APROBADO);
        assertThat(recuperado.getReferenciaPago()).isEqualTo("SIM-" + pedido.getId() + "-001");
        assertThat(recuperado.getFechaPago()).isNotNull();
        assertThat(recuperado.getVersion()).isEqualTo(1L);
    }

    @Test
    void rechazoYExpiracion_seGuardanConFechaYSinReferencia() {
        Pago rechazado = pendiente(pedido);
        rechazado.setEstado(Pago.Estado.RECHAZADO);
        rechazado.setFechaPago(LocalDateTime.now());
        Pago expirado = pendiente(otroPedido);
        expirado.setEstado(Pago.Estado.EXPIRADO);
        expirado.setFechaPago(LocalDateTime.now());

        pagoRepository.saveAllAndFlush(List.of(rechazado, expirado));

        assertThat(pagoRepository.count()).isEqualTo(2);
    }

    @Test
    void confirmacionConcurrente_conVersionVieja_seRechaza() {
        Pago guardado = pagoRepository.saveAndFlush(pendiente(pedido));
        entityManager.clear();
        Pago copiaVieja = pagoRepository.findById(guardado.getId()).orElseThrow();
        // Otra transacción confirmó el pago entre la lectura y la escritura
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE pago SET version = version + 1 WHERE id = ?1")
                .setParameter(1, guardado.getId())
                .executeUpdate();

        copiaVieja.setEstado(Pago.Estado.RECHAZADO);
        copiaVieja.setFechaPago(LocalDateTime.now());

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(copiaVieja))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    // ---------- Unicidad ----------

    @Test
    void segundoPagoParaElMismoPedido_violaUnique() {
        pagoRepository.saveAndFlush(aprobado(pedido, "SIM-1"));

        Pago segundo = aprobado(pedido, "SIM-2");

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(segundo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void claveDeIdempotenciaRepetida_violaUnique() {
        Pago primero = pagoRepository.saveAndFlush(pendiente(pedido));
        Pago mismaClave = pendiente(otroPedido);
        mismaClave.setClaveIdempotencia(primero.getClaveIdempotencia());

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(mismaClave))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void referenciaRepetida_violaUnique() {
        pagoRepository.saveAndFlush(aprobado(pedido, "SIM-REPETIDA"));
        Pago mismaReferencia = aprobado(otroPedido, "SIM-REPETIDA");

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(mismaReferencia))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void variosPagosPendientesSinReferencia_seAdmiten() {
        pagoRepository.saveAllAndFlush(List.of(pendiente(pedido), pendiente(otroPedido)));

        assertThat(pagoRepository.count()).isEqualTo(2);
    }

    // ---------- CHECK y NOT NULL ----------

    @Test
    void montoCero_violaCheck() {
        Pago pago = pendiente(pedido);
        pago.setMonto(BigDecimal.ZERO);

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(pago))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sinMonto_violaNotNull() {
        Pago pago = pendiente(pedido);
        pago.setMonto(null);

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(pago))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sinPedido_violaNotNull() {
        Pago pago = pendiente(null);

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(pago))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sinClaveDeIdempotencia_violaNotNull() {
        Pago pago = pendiente(pedido);
        pago.setClaveIdempotencia(null);

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(pago))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sinMetodo_violaNotNull() {
        Pago pago = pendiente(pedido);
        pago.setMetodo(null);

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(pago))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void pendienteConFechaDeResultado_violaCheck() {
        Pago pago = pendiente(pedido);
        pago.setFechaPago(LocalDateTime.now());

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(pago))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void resultadoDefinitivoSinFecha_violaCheck() {
        Pago pago = pendiente(pedido);
        pago.setEstado(Pago.Estado.RECHAZADO);

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(pago))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aprobadoSinReferencia_violaCheck() {
        Pago pago = aprobado(pedido, null);

        assertThatThrownBy(() -> pagoRepository.saveAndFlush(pago))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void estadoFueraDelCatalogo_violaCheck() {
        Pago guardado = pagoRepository.saveAndFlush(pendiente(pedido));

        assertThatThrownBy(() -> entityManager.getEntityManager()
                .createNativeQuery("UPDATE pago SET estado = 'DESCONOCIDO' WHERE id = ?1")
                .setParameter(1, guardado.getId())
                .executeUpdate())
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    void metodoFueraDelCatalogo_violaCheck() {
        Pago guardado = pagoRepository.saveAndFlush(pendiente(pedido));

        assertThatThrownBy(() -> entityManager.getEntityManager()
                .createNativeQuery("UPDATE pago SET metodo = 'TARJETA' WHERE id = ?1")
                .setParameter(1, guardado.getId())
                .executeUpdate())
                .isInstanceOf(PersistenceException.class);
    }

    // ---------- Trazabilidad ----------

    @Test
    void borrarPedidoConPago_loImpideLaClaveForanea() {
        pagoRepository.saveAndFlush(pendiente(pedido));

        assertThatThrownBy(() -> entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM pedido WHERE id = ?1")
                .setParameter(1, pedido.getId())
                .executeUpdate())
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    void tablaPago_soloTieneLasColumnasDelContratoYNingunDatoDeTarjeta() {
        @SuppressWarnings("unchecked")
        List<String> columnas = entityManager.getEntityManager()
                .createNativeQuery("SELECT LOWER(column_name) FROM information_schema.columns "
                        + "WHERE LOWER(table_name) = 'pago'")
                .getResultList();

        assertThat(columnas).containsExactlyInAnyOrder("id", "pedido_id", "metodo", "monto", "estado",
                "clave_idempotencia", "referencia_pago", "fecha_creacion", "fecha_pago", "version");
    }
}
