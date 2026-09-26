package com.agrovalleconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agrovalleconnect.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo);
    
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Long id);
}
