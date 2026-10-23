package com.agrovalleconnect.agrovalle_connect.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import java.util.Optional;



public interface AgricultorRepository extends JpaRepository<Agricultor, Long> {
    // verificar la existencia de un agricultor con la misma cédula
    boolean existsByCedula(String cedula);
     
    // verificar la existencia de un agricultor con la misma cédula pero diferente ID
    boolean existsByCedulaAndIdNot(String cedula, Long id);

    // busca al agricultor cuyo usuario tiene ese correo (el correo viene en el token JWT)
    Optional<Agricultor> findByUsuarioCorreo(String correo);
    
}

