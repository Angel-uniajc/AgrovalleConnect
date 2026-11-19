package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.OfertaCatalogoResponse;
import com.agrovalleconnect.agrovalle_connect.mapper.CatalogoMapper;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ProductoRepository productoRepository;

    private final CatalogoMapper catalogoMapper;

    public List<OfertaCatalogoResponse> consultar(String municipio, String categoria) {
        return List.of();
    }
}
