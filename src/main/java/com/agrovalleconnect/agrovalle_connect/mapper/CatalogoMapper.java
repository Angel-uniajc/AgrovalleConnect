package com.agrovalleconnect.agrovalle_connect.mapper;

import com.agrovalleconnect.agrovalle_connect.dto.OfertaCatalogoResponse;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import org.springframework.stereotype.Component;

@Component
public class CatalogoMapper {

    public OfertaCatalogoResponse aOferta(Producto producto) {
        return new OfertaCatalogoResponse(
                producto.getId(),
                producto.getTipo(),
                producto.getCantidadDisponible(),
                producto.getPrecioUnitario(),
                producto.getFechaCosecha(),
                producto.getCategoria().getNombre(),
                producto.getFinca().getId(),
                producto.getFinca().getMunicipio().getNombre());
    }
}
