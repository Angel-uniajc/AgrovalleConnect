package com.agrovalleconnect.agrovalle_connect.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroFincaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroFincaResponse;
import com.agrovalleconnect.agrovalle_connect.exception.MunicipioNoPermitidoException;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.FincaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.MunicipioRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FincaServiceTest {

    private static final String CORREO = "pedro@mail.com";

    @Mock
    private AgricultorRepository agricultorRepository;

    @Mock
    private MunicipioRepository municipioRepository;

    @Mock
    private FincaRepository fincaRepository;

    @InjectMocks
    private FincaService fincaService;

    private void agricultorExiste(Agricultor agricultor) {
        when(agricultorRepository.findByUsuarioCorreo(CORREO)).thenReturn(Optional.of(agricultor));
    }

    private void fincaSeGuardaConId(Long id) {
        when(fincaRepository.save(any(Finca.class))).thenAnswer(invocacion -> {
            Finca finca = invocacion.getArgument(0);
            finca.setId(id);
            return finca;
        });
    }

    @Test
    void registrar_datosValidos_guardaFincaYDevuelveRespuesta() {
        Agricultor agricultor = Agricultor.builder().id(1L).build();
        Municipio palmira = Municipio.builder().id(2L).nombre("Palmira").build();
        agricultorExiste(agricultor);
        when(municipioRepository.findByNombreIgnoreCase("Palmira")).thenReturn(Optional.of(palmira));
        fincaSeGuardaConId(10L);

        RegistroFincaResponse respuesta = fincaService.registrar(CORREO,
                new RegistroFincaRequest("La Esperanza", "Palmira", "El Retiro", new BigDecimal("12.50")));

        ArgumentCaptor<Finca> captor = ArgumentCaptor.forClass(Finca.class);
        verify(fincaRepository).save(captor.capture());
        Finca guardada = captor.getValue();
        assertThat(guardada.getAgricultor()).isSameAs(agricultor);
        assertThat(guardada.getMunicipio()).isSameAs(palmira);
        assertThat(guardada.getNombre()).isEqualTo("La Esperanza");

        assertThat(respuesta.id()).isEqualTo(10L);
        assertThat(respuesta.nombre()).isEqualTo("La Esperanza");
        assertThat(respuesta.municipio()).isEqualTo("Palmira");
        assertThat(respuesta.vereda()).isEqualTo("El Retiro");
        assertThat(respuesta.areaHectareas()).isEqualByComparingTo("12.50");
    }

    @Test
    void registrar_municipioNoPermitido_lanzaExcepcionYNoGuardaNada() {
        agricultorExiste(Agricultor.builder().id(1L).build());
        when(municipioRepository.findByNombreIgnoreCase("Bogotá")).thenReturn(Optional.empty());

        RegistroFincaRequest request = new RegistroFincaRequest("La Esperanza", "Bogotá", null, null);

        assertThatThrownBy(() -> fincaService.registrar(CORREO, request))
                .isInstanceOf(MunicipioNoPermitidoException.class);

        verify(fincaRepository, never()).save(any());
    }

    @Test
    void registrar_municipioEnMinusculas_seAceptaYRespondeNombreDelCatalogo() {
        Municipio palmira = Municipio.builder().id(2L).nombre("Palmira").build();
        agricultorExiste(Agricultor.builder().id(1L).build());
        when(municipioRepository.findByNombreIgnoreCase("palmira")).thenReturn(Optional.of(palmira));
        fincaSeGuardaConId(11L);

        RegistroFincaResponse respuesta = fincaService.registrar(CORREO,
                new RegistroFincaRequest("La Esperanza", "palmira", null, null));

        assertThat(respuesta.municipio()).isEqualTo("Palmira");
    }

    @Test
    void registrar_sinVeredaNiArea_seGuardaConValoresNulos() {
        Municipio dagua = Municipio.builder().id(3L).nombre("Dagua").build();
        agricultorExiste(Agricultor.builder().id(1L).build());
        when(municipioRepository.findByNombreIgnoreCase("Dagua")).thenReturn(Optional.of(dagua));
        fincaSeGuardaConId(12L);

        RegistroFincaResponse respuesta = fincaService.registrar(CORREO,
                new RegistroFincaRequest("Mi Finca", "Dagua", null, null));

        assertThat(respuesta.id()).isEqualTo(12L);
        assertThat(respuesta.vereda()).isNull();
        assertThat(respuesta.areaHectareas()).isNull();
    }
}
