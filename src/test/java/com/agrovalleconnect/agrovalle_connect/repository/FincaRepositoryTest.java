package com.agrovalleconnect.agrovalle_connect.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agrovalleconnect.agrovalle_connect.config.CargadorMunicipios;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(CargadorMunicipios.class)
class FincaRepositoryTest {

    @Autowired
    private FincaRepository fincaRepository;

    @Autowired
    private MunicipioRepository municipioRepository;

    @Autowired
    private AgricultorRepository agricultorRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Método auxiliar: deja un agricultor listo en la BD de pruebas
    private Agricultor sembrarAgricultor(String correo, String cedula) {
        Usuario usuario = Usuario.builder()
                .nombre("Pedro")
                .correo(correo)
                .passwordHash("hash_de_prueba")
                .rol(Usuario.Rol.AGRICULTOR)
                .fechaRegistro(LocalDateTime.now())
                .build();
        entityManager.persist(usuario);

        Agricultor agricultor = Agricultor.builder()
                .cedula(cedula)
                .ubicacionValle("Dagua")
                .usuario(usuario)
                .build();
        entityManager.persistAndFlush(agricultor);
        return agricultor;
    }

    @Test
    void catalogoInicial_tieneDaguaPalmiraYBuga() {
        assertThat(municipioRepository.count()).isEqualTo(3);
        assertThat(municipioRepository.findByNombreIgnoreCase("Palmira")).isPresent();
        assertThat(municipioRepository.findByNombreIgnoreCase("Buga")).isPresent();
    }

    @Test
    void guardarFinca_seRecuperaConSuAgricultorYMunicipio() {
        // Given: un agricultor y un municipio del catálogo
        Agricultor agricultor = sembrarAgricultor("pedro@mail.com", "123456789");
        Municipio dagua = municipioRepository.findByNombreIgnoreCase("Dagua").orElseThrow();
        Finca finca = Finca.builder()
                .nombre("La Esperanza")
                .vereda("El Palmar")
                .areaHectareas(new BigDecimal("2.50"))
                .agricultor(agricultor)
                .municipio(dagua)
                .build();

        // When: guardamos y forzamos la lectura real desde la BD
        Finca guardada = fincaRepository.save(finca);
        entityManager.flush();
        entityManager.clear();

        // Then: se recupera con todos sus datos
        Finca recuperada = fincaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(recuperada.getNombre()).isEqualTo("La Esperanza");
        assertThat(recuperada.getVereda()).isEqualTo("El Palmar");
        assertThat(recuperada.getAreaHectareas()).isEqualByComparingTo("2.50");
        assertThat(recuperada.getAgricultor().getId()).isEqualTo(agricultor.getId());
        assertThat(recuperada.getMunicipio().getNombre()).isEqualTo("Dagua");
    }

    @Test
    void buscarMunicipio_ignoraMayusculasYMinusculas() {
        assertThat(municipioRepository.findByNombreIgnoreCase("dAgUa")).isPresent();
        assertThat(municipioRepository.findByNombreIgnoreCase("Cali")).isEmpty();
    }

    @Test
    void buscarAgricultorPorCorreo_loEncuentra() {
        sembrarAgricultor("pedro@mail.com", "123456789");

        assertThat(agricultorRepository.findByUsuarioCorreo("pedro@mail.com")).isPresent();
        assertThat(agricultorRepository.findByUsuarioCorreo("pedro@mail.com").orElseThrow().getCedula())
                .isEqualTo("123456789");
        assertThat(agricultorRepository.findByUsuarioCorreo("nadie@mail.com")).isEmpty();
    }

    @Test
    void guardarMunicipioConNombreRepetido_esRechazado() {
        Municipio repetido = Municipio.builder().nombre("Dagua").build();

        assertThatThrownBy(() -> municipioRepository.saveAndFlush(repetido))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
