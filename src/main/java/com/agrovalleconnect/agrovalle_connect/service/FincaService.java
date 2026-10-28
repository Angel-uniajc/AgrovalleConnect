package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroFincaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroFincaResponse;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.FincaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agrovalleconnect.agrovalle_connect.exception.MunicipioNoPermitidoException;
import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Finca;
import com.agrovalleconnect.agrovalle_connect.model.Municipio;

@Service
@RequiredArgsConstructor
public class FincaService {

    private final AgricultorRepository agricultorRepository;

    private final MunicipioRepository municipioRepository;

    private final FincaRepository fincaRepository;

    @Transactional
    public RegistroFincaResponse registrar(String correoAgricultor, RegistroFincaRequest request) {
        Agricultor agricultor = agricultorRepository.findByUsuarioCorreo(correoAgricultor)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe un agricultor asociado al correo " + correoAgricultor));

        Municipio municipio = municipioRepository.findByNombreIgnoreCase(request.municipio())
                .orElseThrow(() -> new MunicipioNoPermitidoException(request.municipio()));

        Finca finca = Finca.builder()
                .nombre(request.nombre())
                .vereda(request.vereda())
                .areaHectareas(request.areaHectareas())
                .agricultor(agricultor)
                .municipio(municipio)
                .build();

        Finca guardada = fincaRepository.save(finca);

        return new RegistroFincaResponse(
                guardada.getId(),
                guardada.getNombre(),
                guardada.getMunicipio().getNombre(),
                guardada.getVereda(),
                guardada.getAreaHectareas());
    }
}
