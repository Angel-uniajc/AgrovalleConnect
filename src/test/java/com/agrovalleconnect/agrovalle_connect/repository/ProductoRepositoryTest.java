package com.agrovalleconnect.agrovalle_connect.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Producto productoValido() {
        Usuario usuario = entityManager.persist(Usuario.builder()
                .nombre("Pedro")
                .correo("pedro@mail.com")
                .passwordHash("hash_de_prueba")
                .rol(Usuario.Rol.AGRICULTOR)
                .fechaRegistro(LocalDateTime.now())
                .build());
        Agricultor agricultor = entityManager.persist(Agricultor.builder()
                .cedula("123456789")
                .ubicacionValle("Dagua")
                .usuario(usuario)
                .build());
        Municipio municipio = entityManager.persist(Municipio.builder().nombre("Dagua").build());
        Finca finca = entityManager.persist(Finca.builder()
                .nombre("La Esperanza")
                .agricultor(agricultor)
                .municipio(municipio)
                .build());
        Categoria categoria = entityManager.persist(Categoria.builder().nombre("Frutas").build());

        return Producto.builder()
                .tipo("Mango")
                .cantidadDisponible(new BigDecimal("125.50"))
                .precioUnitario(new BigDecimal("3500.25"))
                .fechaCosecha(LocalDate.of(2026, 11, 1))
                .finca(finca)
                .categoria(categoria)
                .build();
    }

    @Test
    void guardarProducto_seRecuperaConSuFincaCategoriaYEstadoActivo() {
        Producto producto = productoValido();
        Long fincaId = producto.getFinca().getId();
        Long categoriaId = producto.getCategoria().getId();

        Producto guardado = productoRepository.saveAndFlush(producto);
        entityManager.clear();

        Producto recuperado = productoRepository.findById(guardado.getId()).orElseThrow();
        assertThat(recuperado.getTipo()).isEqualTo("Mango");
        assertThat(recuperado.getCantidadDisponible()).isEqualByComparingTo("125.50");
        assertThat(recuperado.getPrecioUnitario()).isEqualByComparingTo("3500.25");
        assertThat(recuperado.getFechaCosecha()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(recuperado.getEstado()).isEqualTo(Producto.Estado.ACTIVO);
        assertThat(recuperado.getFinca().getId()).isEqualTo(fincaId);
        assertThat(recuperado.getFinca().getNombre()).isEqualTo("La Esperanza");
        assertThat(recuperado.getCategoria().getId()).isEqualTo(categoriaId);
        assertThat(recuperado.getCategoria().getNombre()).isEqualTo("Frutas");
    }

    @Test
    void guardarProductoAgotado_conCantidadCero_esAceptado() {
        Producto producto = productoValido();
        producto.setCantidadDisponible(BigDecimal.ZERO);
        producto.setEstado(Producto.Estado.AGOTADO);

        Long id = productoRepository.saveAndFlush(producto).getId();
        entityManager.clear();

        Producto recuperado = productoRepository.findById(id).orElseThrow();
        assertThat(recuperado.getCantidadDisponible()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(recuperado.getEstado()).isEqualTo(Producto.Estado.AGOTADO);
    }

    @Test
    void consultarOfertas_aplicaFiltrosOpcionalesPorNombreCompleto() {
        Producto mango = entityManager.persist(productoValido());
        Categoria tuberculos = entityManager.persist(Categoria.builder().nombre("Tubérculos").build());
        Finca otraFinca = entityManager.persist(Finca.builder()
                .nombre("El Porvenir").agricultor(mango.getFinca().getAgricultor())
                .municipio(entityManager.persist(Municipio.builder().nombre("El Valle").build())).build());
        Producto papa = guardarOferta(mango.getFinca(), tuberculos, Producto.Estado.ACTIVO, 2);
        Producto pera = guardarOferta(otraFinca, mango.getCategoria(), Producto.Estado.ACTIVO, 3);
        entityManager.flush();
        entityManager.clear();

        assertThat(productoRepository.consultarOfertasDisponibles("  daGUA ", null))
                .extracting(Producto::getId).containsExactly(mango.getId(), papa.getId());
        assertThat(productoRepository.consultarOfertasDisponibles("  ", " frUTas "))
                .extracting(Producto::getId).containsExactly(mango.getId(), pera.getId());
        assertThat(productoRepository.consultarOfertasDisponibles("Dagua", "TUBÉRCULOS"))
                .extracting(Producto::getId).containsExactly(papa.getId());
        assertThat(productoRepository.consultarOfertasDisponibles("El Valle", "Tubérculos")).isEmpty();
        assertThat(productoRepository.consultarOfertasDisponibles(null, "Tuberculos")).isEmpty();
        assertThat(productoRepository.consultarOfertasDisponibles("Da%", null)).isEmpty();
        assertThat(productoRepository.consultarOfertasDisponibles(null, "Fr_tas")).isEmpty();
    }

    @Test
    void consultarOfertas_excluyeNoDisponiblesYCargaRelacionesDelCatalogo() {
        Producto mango = entityManager.persist(productoValido());
        Finca finca = mango.getFinca();
        Categoria categoria = mango.getCategoria();
        guardarOferta(finca, categoria, Producto.Estado.AGOTADO, 2);
        guardarOferta(finca, categoria, Producto.Estado.ACTIVO, 0);
        guardarOferta(finca, categoria, Producto.Estado.INACTIVO, 4);
        Producto disponible = guardarOferta(finca, categoria, Producto.Estado.ACTIVO, 1);
        entityManager.flush();
        entityManager.clear();

        var ofertas = productoRepository.consultarOfertasDisponibles(null, "");
        entityManager.clear();
        assertThat(ofertas).extracting(Producto::getId).containsExactly(mango.getId(), disponible.getId());
        assertThat(ofertas).allSatisfy(oferta -> {
            assertThat(oferta.getFinca().getMunicipio().getNombre()).isEqualTo("Dagua");
            assertThat(oferta.getCategoria().getNombre()).isEqualTo("Frutas");
        });
    }

    private Producto guardarOferta(Finca finca, Categoria categoria, Producto.Estado estado, int cantidad) {
        return entityManager.persist(Producto.builder()
                .tipo("Oferta").finca(finca).categoria(categoria).estado(estado)
                .cantidadDisponible(BigDecimal.valueOf(cantidad)).precioUnitario(BigDecimal.TEN)
                .fechaCosecha(LocalDate.of(2020, 1, 1)).build());
    }

    @Test
    void guardarProductoConCantidadNegativa_esRechazado() {
        Producto producto = productoValido();
        producto.setCantidadDisponible(new BigDecimal("-1"));

        assertThatThrownBy(() -> productoRepository.saveAndFlush(producto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void guardarProductoConPrecioCero_esRechazado() {
        Producto producto = productoValido();
        producto.setPrecioUnitario(BigDecimal.ZERO);

        assertThatThrownBy(() -> productoRepository.saveAndFlush(producto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void buscarParaActualizar_devuelveLosProductosOrdenadosPorIdEIgnoraLosInexistentes() {
        Producto mango = productoRepository.saveAndFlush(productoValido());
        Producto tomate = productoRepository.saveAndFlush(Producto.builder()
                .tipo("Tomate")
                .cantidadDisponible(new BigDecimal("8.00"))
                .precioUnitario(new BigDecimal("2500.00"))
                .fechaCosecha(LocalDate.of(2026, 11, 2))
                .finca(mango.getFinca())
                .categoria(mango.getCategoria())
                .build());
        entityManager.clear();

        assertThat(productoRepository.buscarParaActualizar(
                List.of(tomate.getId(), 999_999L, mango.getId())))
                .extracting(Producto::getId)
                .containsExactly(mango.getId(), tomate.getId());
    }
}
