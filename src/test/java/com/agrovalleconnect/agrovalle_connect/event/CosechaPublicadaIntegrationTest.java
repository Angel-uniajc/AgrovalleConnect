package com.agrovalleconnect.agrovalle_connect.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaRequest;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import com.agrovalleconnect.agrovalle_connect.service.CosechaService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@RecordApplicationEvents
@ExtendWith(OutputCaptureExtension.class)
class CosechaPublicadaIntegrationTest {

    @Autowired
    private CosechaService cosechaService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ApplicationEvents eventos;

    @Test
    void publicarCosecha_persisteEmiteEventoYActivaListener(CapturedOutput salida) {
        Usuario usuario = Usuario.builder().nombre("Agricultor de eventos")
                .correo("eventos@mail.com").passwordHash("hash_de_prueba")
                .rol(Usuario.Rol.AGRICULTOR).fechaRegistro(LocalDateTime.now()).build();
        entityManager.persist(usuario);
        Agricultor agricultor = Agricultor.builder().cedula("8877665544")
                .ubicacionValle("Palmira").usuario(usuario).build();
        entityManager.persist(agricultor);
        Municipio municipio = entityManager.createQuery(
                "select m from Municipio m where m.nombre = :nombre", Municipio.class)
                .setParameter("nombre", "Palmira").getSingleResult();
        Categoria categoria = entityManager.createQuery(
                "select c from Categoria c where c.nombre = :nombre", Categoria.class)
                .setParameter("nombre", "Frutas").getSingleResult();
        Finca finca = Finca.builder().nombre("Finca de eventos")
                .agricultor(agricultor).municipio(municipio).build();
        entityManager.persist(finca);
        Long fincaId = finca.getId();

        var respuesta = cosechaService.publicar(usuario.getCorreo(),
                new PublicarCosechaRequest(fincaId, categoria.getId(), "Mango",
                        new BigDecimal("25.50"), new BigDecimal("3500"), LocalDate.now()));
        entityManager.flush();
        entityManager.clear();

        Producto producto = productoRepository.findById(respuesta.id()).orElseThrow();
        assertThat(producto.getEstado()).isEqualTo(Producto.Estado.ACTIVO);
        assertThat(producto.getFinca().getId()).isEqualTo(fincaId);
        assertThat(eventos.stream(CosechaPublicadaEvent.class)).containsExactly(
                new CosechaPublicadaEvent(respuesta.id(), "Mango", fincaId, "Palmira"));
        assertThat(salida.getOut()).contains("Cosecha publicada: productoId=" + respuesta.id(),
                "tipo=Mango", "fincaId=" + fincaId, "municipio=Palmira");
    }
}
