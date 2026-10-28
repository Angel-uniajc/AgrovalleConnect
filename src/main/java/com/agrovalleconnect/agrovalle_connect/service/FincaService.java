package com.agrovalleconnect.agrovalle_connect.service;

import com.agrovalleconnect.agrovalle_connect.dto.RegistroFincaRequest;
import com.agrovalleconnect.agrovalle_connect.dto.RegistroFincaResponse;
import com.agrovalleconnect.agrovalle_connect.repository.AgricultorRepository;
import com.agrovalleconnect.agrovalle_connect.repository.FincaRepository;
import com.agrovalleconnect.agrovalle_connect.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FincaService {

    private final AgricultorRepository agricultorRepository;

    private final MunicipioRepository municipioRepository;

    private final FincaRepository fincaRepository;

    @Transactional
    public RegistroFincaResponse registrar(String correoAgricultor, RegistroFincaRequest request) {
        throw new UnsupportedOperationException("pendiente");
    }
}
