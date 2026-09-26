package com.agrovalleconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agrovalleconnect.model.Agricultor;



public interface AgricultorRepository extends JpaRepository<Agricultor, Long> {

    boolean existsByDocumento(String documento);

    Optional<Agricultor> findById(Long id);
    
}

