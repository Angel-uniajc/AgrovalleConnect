package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.OfertaCatalogoResponse;
import com.agrovalleconnect.agrovalle_connect.mapper.CatalogoMapper;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ProductoRepository productoRepository;

    private final CatalogoMapper catalogoMapper;

    @Transactional(readOnly = true)
    public List<OfertaCatalogoResponse> consultar(String municipio, String categoria) {
        return productoRepository.consultarOfertasDisponibles(normalizar(municipio), normalizar(categoria))
                .stream()
                .map(catalogoMapper::aOferta)
                .toList();
    }

    private String normalizar(String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return null;
        }
        return filtro.trim().toLowerCase(Locale.ROOT);
    }
}
