package com.agrovalleconnect.agrovalle_connect.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.MensajeContacto;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
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
class MensajeContactoRepositoryTest {

    @Autowired
    private MensajeContactoRepository mensajeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Comprador comprador;
    private Producto producto;

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
                .ubicacionValle("Dagua")
                .usuario(usuarioAgricultor)
                .build());
        Municipio municipio = entityManager.persist(Municipio.builder().nombre("Dagua").build());
        Finca finca = entityManager.persist(Finca.builder()
                .nombre("La Esperanza")
                .agricultor(agricultor)
                .municipio(municipio)
                .build());
        Categoria categoria = entityManager.persist(Categoria.builder().nombre("Frutas").build());
        producto = entityManager.persist(Producto.builder()
                .tipo("Mango")
                .cantidadDisponible(new BigDecimal("20.00"))
                .precioUnitario(new BigDecimal("3500.00"))
                .fechaCosecha(LocalDate.of(2026, 11, 1))
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
        comprador = entityManager.persist(Comprador.builder()
                .documento("12345678")
                .tipoComprador(Comprador.TipoComprador.INDIVIDUAL)
                .usuario(usuarioComprador)
                .build());
        entityManager.flush();
    }

    private MensajeContacto mensaje(Comprador deQuien, Producto sobreQue, String texto) {
        return MensajeContacto.builder()
                .comprador(deQuien)
                .producto(sobreQue)
                .mensaje(texto)
                .build();
    }

    @Test
    void guardarMensaje_seRecuperaConCompradorProductoYFechaDelServidor() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        MensajeContacto guardado = mensajeRepository.saveAndFlush(
                mensaje(comprador, producto, "Me interesan 20 kg de mango"));
        entityManager.clear();

        MensajeContacto recuperado = mensajeRepository.findById(guardado.getId()).orElseThrow();
        assertThat(recuperado.getMensaje()).isEqualTo("Me interesan 20 kg de mango");
        assertThat(recuperado.getComprador().getId()).isEqualTo(comprador.getId());
        assertThat(recuperado.getComprador().getDocumento()).isEqualTo("12345678");
        assertThat(recuperado.getProducto().getId()).isEqualTo(producto.getId());
        assertThat(recuperado.getProducto().getTipo()).isEqualTo("Mango");
        assertThat(recuperado.getFechaEnvio()).isNotNull().isAfter(antes);
    }

    @Test
    void mensajeDe2000Caracteres_seGuardaCompleto() {
        String largo = "a".repeat(2000);

        MensajeContacto guardado = mensajeRepository.saveAndFlush(mensaje(comprador, producto, largo));
        entityManager.clear();

        assertThat(mensajeRepository.findById(guardado.getId()).orElseThrow().getMensaje())
                .hasSize(2000);
    }

    @Test
    void mismoCompradorYProducto_admiteVariosMensajes() {
        mensajeRepository.saveAndFlush(mensaje(comprador, producto, "Hola"));
        mensajeRepository.saveAndFlush(mensaje(comprador, producto, "Hola"));

        assertThat(mensajeRepository.count()).isEqualTo(2);
    }

    @Test
    void mensajeSinComprador_violaNotNull() {
        MensajeContacto sinComprador = mensaje(null, producto, "Hola");

        assertThatThrownBy(() -> mensajeRepository.saveAndFlush(sinComprador))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void mensajeSinProducto_violaNotNull() {
        MensajeContacto sinProducto = mensaje(comprador, null, "Hola");

        assertThatThrownBy(() -> mensajeRepository.saveAndFlush(sinProducto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void mensajeNulo_violaNotNull() {
        MensajeContacto sinTexto = mensaje(comprador, producto, null);

        assertThatThrownBy(() -> mensajeRepository.saveAndFlush(sinTexto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void borrarMensaje_noBorraNiCompradorNiProducto() {
        MensajeContacto guardado = mensajeRepository.saveAndFlush(mensaje(comprador, producto, "Hola"));

        mensajeRepository.delete(guardado);
        mensajeRepository.flush();
        entityManager.clear();

        assertThat(entityManager.find(Comprador.class, comprador.getId())).isNotNull();
        assertThat(entityManager.find(Producto.class, producto.getId())).isNotNull();
    }
}
