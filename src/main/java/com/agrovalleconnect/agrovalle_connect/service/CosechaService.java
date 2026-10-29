package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.PublicarCosechaResponse;
import com.agrovalleconnect.agrovalle_connect.exception.AccesoDenegadoException;
import com.agrovalleconnect.agrovalle_connect.exception.CategoriaInvalidaException;
import com.agrovalleconnect.agrovalle_connect.exception.RecursoNoEncontradoException;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.model.Producto.Estado;
import com.agrovalleconnect.agrovalle_connect.repository.CategoriaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.FincaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import org.springframework.stereotype.Service;

@Service
public class CosechaService {

    private final ProductoRepository productoRepository;

    private final FincaRepository fincaRepository;

    private final CategoriaRepository categoriaRepository;

    public CosechaService(ProductoRepository productoRepository,
                          FincaRepository fincaRepository,
                          CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.fincaRepository = fincaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public PublicarCosechaResponse publicar(String correoAgricultor, PublicarCosechaRequest request) {
        Finca finca = obtenerFincaDelAgricultor(request.fincaId(), correoAgricultor);
        Categoria categoria = obtenerCategoria(request.categoriaId());

        Producto producto = Producto.builder()
                .tipo(request.tipo())
                .cantidadDisponible(request.cantidad())
                .precioUnitario(request.precioUnitario())
                .fechaCosecha(request.fechaCosecha())
                .estado(Estado.ACTIVO)
                .finca(finca)
                .categoria(categoria)
                .build();

        Producto guardado = productoRepository.save(producto);
        return new PublicarCosechaResponse(guardado.getId());
    }

    private Finca obtenerFincaDelAgricultor(Long fincaId, String correoAgricultor) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Finca no encontrada"));

        if (!finca.getAgricultor().getUsuario().getCorreo().equals(correoAgricultor)) {
            throw new AccesoDenegadoException("La finca no pertenece al agricultor autenticado");
        }

        return finca;
    }

    private Categoria obtenerCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new CategoriaInvalidaException("Categoría inválida"));
    }
}