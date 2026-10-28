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
}
