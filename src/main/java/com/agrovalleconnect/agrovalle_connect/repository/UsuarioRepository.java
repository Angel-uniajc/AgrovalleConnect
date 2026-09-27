package com.agrovalleconnect.agrovalle_connect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agrovalleconnect.agrovalle_connect.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo); // verificar la existencia de un usuario con el mismo correo
    
    Optional<Usuario> findByCorreo(String correo); // buscar un usuario por correo

    boolean existsByCorreoAndIdNot(String correo, Long id); // verificar la existencia de un usuario con el mismo correo pero diferente ID
}
