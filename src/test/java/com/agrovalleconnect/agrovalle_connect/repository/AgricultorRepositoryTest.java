package com.agrovalleconnect.agrovalle_connect.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;

@DataJpaTest 
public class AgricultorRepositoryTest {
    
    @Autowired 
    private AgricultorRepository agricultorRepository;

    @Autowired 
    private TestEntityManager entityManager;

    @Test 
    void guardarAgricultor_seRecuperaConTodosSuUsuario(){
        //Given: primero el usuario, luego el agricultor
        Usuario usuario = new Usuario();
        usuario.setNombre("pedro");
        usuario.setCorreo("pedro@example.com");
        usuario.setPasswordHash("hash_de_prueba");
        usuario.setRol(Usuario.Rol.AGRICULTOR);
        entityManager.persistAndFlush(usuario);

        Agricultor agricultor = new Agricultor();
        agricultor.setCedula("123456789");
        agricultor.setUbicacionValle("Valle Central");
        agricultor.setUsuario(usuario);
        entityManager.persistAndFlush(agricultor);

        // WHEN: guardamos y forzamos la lectura desde la base de datos
        Agricultor guardado =  agricultorRepository.save(agricultor);
        entityManager.flush();
        entityManager.clear();

        // THEN: se recupera con todos sus datos
        Agricultor recuperado = agricultorRepository.findById(guardado.getId()).orElseThrow();

        assert(recuperado.getCedula().equals("123456789"));
        assert(recuperado.getUbicacionValle().equals("Valle Central"));
        assert(recuperado.getUsuario().getNombre().equals("pedro"));

        // THEN 2: el camino inverso, del usuario a su agricultor
        Usuario usuarioRecuperado = entityManager.find(Usuario.class, usuario.getId());

        assert(usuarioRecuperado.getAgricultor().getCedula().equals("123456789"));

    }
}
