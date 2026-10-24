package com.agrovalleconnect.agrovalle_connect.config;

import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.repository.MunicipioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CargadorMunicipios implements ApplicationRunner {

    private static final List<String> MUNICIPIOS_INICIALES = List.of("Dagua", "Palmira", "Buga");

    private final MunicipioRepository municipioRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (municipioRepository.count() == 0) {
            MUNICIPIOS_INICIALES.forEach(nombre ->
                    municipioRepository.save(Municipio.builder().nombre(nombre).build()));
        }
    }
}
