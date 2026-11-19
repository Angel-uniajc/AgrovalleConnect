package com.agrovalleconnect.agrovalle_connect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agrovalleconnect.agrovalle_connect.model.Comprador;

public interface CompradorRepository extends JpaRepository<Comprador, Long> {

    // verificar si otro comprador ya usa ese documento (409 en el registro)
    boolean existsByDocumento(String documento);

    // busca al comprador cuyo usuario tiene ese correo (el correo viene en el token JWT)
    Optional<Comprador> findByUsuarioCorreo(String correo);
}
