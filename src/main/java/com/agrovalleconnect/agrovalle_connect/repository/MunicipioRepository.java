package com.agrovalleconnect.agrovalle_connect.repository;

import com.agrovalleconnect.agrovalle_connect.model.Municipio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MunicipioRepository extends JpaRepository<Municipio, Long> {

    Optional<Municipio> findByNombreIgnoreCase(String nombre);
}