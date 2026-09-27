package com.agrovalleconnect.agrovalle_connect.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;



public interface AgricultorRepository extends JpaRepository<Agricultor, Long> {
    // verificar la existencia de un agricultor con la misma cédula
    boolean existsByCedula(String cedula);
     
    // verificar la existencia de un agricultor con la misma cédula pero diferente ID
    boolean existsByCedulaAndIdNot(String cedula, Long id); 
    
}

