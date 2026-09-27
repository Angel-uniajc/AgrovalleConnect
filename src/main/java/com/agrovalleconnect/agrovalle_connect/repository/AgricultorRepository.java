package com.agrovalleconnect.agrovalle_connect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;



public interface AgricultorRepository extends JpaRepository<Agricultor, Long> {

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    Optional<Agricultor> findById(Long id);
}

