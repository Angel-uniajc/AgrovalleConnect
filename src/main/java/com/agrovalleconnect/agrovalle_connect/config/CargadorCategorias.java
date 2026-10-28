package com.agrovalleconnect.agrovalle_connect.config;

import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.repository.CategoriaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CargadorCategorias implements ApplicationRunner {

    private static final List<String> CATEGORIAS_INICIALES =
            List.of("Frutas", "Hortalizas", "Granos", "Tubérculos");

    private final CategoriaRepository categoriaRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (categoriaRepository.count() == 0) {
            CATEGORIAS_INICIALES.forEach(nombre ->
                    categoriaRepository.save(Categoria.builder().nombre(nombre).build()));
        }
    }
}
