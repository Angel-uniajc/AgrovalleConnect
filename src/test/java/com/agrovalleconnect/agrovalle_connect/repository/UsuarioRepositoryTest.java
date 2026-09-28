package com.agrovalleconnect.agrovalle_connect.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import com.agrovalleconnect.agrovalle_connect.model.Usuario;
import com.agrovalleconnect.agrovalle_connect.model.Usuario.Rol;

@DataJpaTest 
public class UsuarioRepositoryTest {
    
    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Autowired 
    private TestEntityManager entityManager;

    @Test 
    void guardarUsuario_seRecuperaConTodosSusDatos(){
        //Given
        Usuario usuario = new Usuario();
        usuario.setNombre("pedro");
        usuario.setCorreo("pedro@mail.com");
        usuario.setPasswordHash("hash_de_prueba");
        usuario.setRol(Rol.AGRICULTOR);


        //When
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        //Then
        Usuario recuperado = usuarioRepository.findById(usuarioGuardado.getId()).orElseThrow();

        assertThat(recuperado.getId()).isNotNull();
        assertThat(recuperado.getNombre()).isEqualTo("pedro");
        assertThat(recuperado.getCorreo()).isEqualTo("pedro@mail.com");
        assertThat(recuperado.getPasswordHash()).isEqualTo("hash_de_prueba");
        assertThat(recuperado.getRol()).isEqualTo(Usuario.Rol.AGRICULTOR);
        assertThat(recuperado.getFechaRegistro()).isNotNull();
    }
    
}
