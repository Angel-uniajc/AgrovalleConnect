package com.agrovalleconnect.agrovalle_connect.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agrovalleconnect.agrovalle_connect.config.CargadorCategorias;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(CargadorCategorias.class)
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CargadorCategorias cargadorCategorias;

    @Test
    void catalogoInicial_tieneLasCuatroCategorias() {
        assertThat(categoriaRepository.findAll()).extracting(Categoria::getNombre)
                .containsExactlyInAnyOrder("Frutas", "Hortalizas", "Granos", "Tubérculos");
    }

    @Test
    void ejecutarCargadorOtraVez_noDuplicaElCatalogo() {
        var idsIniciales = categoriaRepository.findAll().stream().map(Categoria::getId).toList();

        cargadorCategorias.run(new DefaultApplicationArguments());

        assertThat(categoriaRepository.findAll()).extracting(Categoria::getId)
                .containsExactlyInAnyOrderElementsOf(idsIniciales);
    }

    @Test
    void tablaConUnaCategoria_noCargaElCatalogoInicial() {
        categoriaRepository.deleteAll();
        Categoria existente = categoriaRepository.saveAndFlush(
                Categoria.builder().nombre("Otra categoría").build());

        cargadorCategorias.run(new DefaultApplicationArguments());

        assertThat(categoriaRepository.findAll()).extracting(Categoria::getId)
                .containsExactly(existente.getId());
    }

    @Test
    void guardarCategoriaConNombreRepetido_esRechazado() {
        Categoria repetida = Categoria.builder().nombre("Frutas").build();

        assertThatThrownBy(() -> categoriaRepository.saveAndFlush(repetida))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
