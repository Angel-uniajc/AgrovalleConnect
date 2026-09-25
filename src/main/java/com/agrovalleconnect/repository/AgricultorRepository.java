package com.agrovalleconnect.repository;

import com.agrovalleconnect.model.Agricultor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgricultorRepository extends JpaRepository<Agricultor, Long> {

    Agricultor findByAgricultor(String documentoIdentidad);
}

