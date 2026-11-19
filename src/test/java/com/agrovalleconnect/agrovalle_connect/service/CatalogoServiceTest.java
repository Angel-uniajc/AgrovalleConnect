package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.OfertaCatalogoResponse;
import com.agrovalleconnect.agrovalle_connect.mapper.CatalogoMapper;
import com.agrovalleconnect.agrovalle_connect.model.Categoria;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.model.Producto;
import com.agrovalleconnect.agrovalle_connect.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Spy
    private CatalogoMapper catalogoMapper = new CatalogoMapper();

    @InjectMocks
    private CatalogoService catalogoService;

    @Test
    void consultar_conEspaciosYMayusculas_pasaFiltrosNormalizadosAlRepositorio() {
        catalogoService.consultar("  PALmira ", " FRuTaS  ");

        verify(productoRepository).consultarOfertasDisponibles("palmira", "frutas");
        verifyNoMoreInteractions(productoRepository);
    }

    @Test
    void consultar_conFiltrosVaciosOSoloEspacios_losPasaComoAusentes() {
        catalogoService.consultar("", "   ");

        verify(productoRepository).consultarOfertasDisponibles(null, null);
    }

    @Test
    void consultar_conFiltrosNulos_losPasaComoAusentes() {
        catalogoService.consultar(null, null);

        verify(productoRepository).consultarOfertasDisponibles(null, null);
    }

    @Test
    void consultar_soloConMunicipio_conservaElMunicipioYOmiteLaCategoria() {
        catalogoService.consultar("Dagua", "  ");

        verify(productoRepository).consultarOfertasDisponibles("dagua", null);
    }

    @Test
    void consultar_soloConCategoria_conservaLaCategoriaYOmiteElMunicipio() {
        catalogoService.consultar(null, "Granos");

        verify(productoRepository).consultarOfertasDisponibles(null, "granos");
    }

    @Test
    void consultar_conTildeEnElFiltro_laConservaSinQuitarla() {
        catalogoService.consultar(null, " Tubérculos ");

        verify(productoRepository).consultarOfertasDisponibles(null, "tubérculos");
    }

    @Test
    void consultar_conOferta_mapeaLosOchoCamposDelContrato() {
        when(productoRepository.consultarOfertasDisponibles("dagua", "frutas"))
                .thenReturn(List.of(producto(12L, "Mango", "Dagua", "Frutas")));

        List<OfertaCatalogoResponse> ofertas = catalogoService.consultar("Dagua", "Frutas");

        assertThat(ofertas).containsExactly(new OfertaCatalogoResponse(
                12L,
                "Mango",
                new BigDecimal("25.50"),
                new BigDecimal("3200.00"),
                LocalDate.of(2026, 11, 20),
                "Frutas",
                4L,
                "Dagua"));
    }

    @Test
    void consultar_sinOfertas_retornaListaVacia() {
        when(productoRepository.consultarOfertasDisponibles(null, null)).thenReturn(List.of());

        assertThat(catalogoService.consultar(null, null)).isEmpty();
    }

    @Test
    void consultar_conVariasOfertas_conservaElOrdenDelRepositorio() {
        when(productoRepository.consultarOfertasDisponibles(null, null)).thenReturn(List.of(
                producto(3L, "Yuca", "Buga", "Tubérculos"),
                producto(7L, "Lulo", "Dagua", "Frutas"),
                producto(9L, "Maíz", "Palmira", "Granos")));

        List<OfertaCatalogoResponse> ofertas = catalogoService.consultar(null, null);

        assertThat(ofertas).extracting(OfertaCatalogoResponse::id).containsExactly(3L, 7L, 9L);
    }

    @Test
    void consultar_alConsultar_soloLeeSinGuardarNada() {
        catalogoService.consultar("Dagua", "Frutas");

        verify(productoRepository).consultarOfertasDisponibles("dagua", "frutas");
        verifyNoMoreInteractions(productoRepository);
    }

    private Producto producto(Long id, String tipo, String municipio, String categoria) {
        Municipio municipioOferta = Municipio.builder().id(1L).nombre(municipio).build();
        Finca finca = Finca.builder().id(4L).municipio(municipioOferta).build();
        Categoria categoriaOferta = Categoria.builder().id(2L).nombre(categoria).build();
        return Producto.builder()
                .id(id)
                .tipo(tipo)
                .cantidadDisponible(new BigDecimal("25.50"))
                .precioUnitario(new BigDecimal("3200.00"))
                .fechaCosecha(LocalDate.of(2026, 11, 20))
                .finca(finca)
                .categoria(categoria == null ? null : categoriaOferta)
                .build();
    }
}
