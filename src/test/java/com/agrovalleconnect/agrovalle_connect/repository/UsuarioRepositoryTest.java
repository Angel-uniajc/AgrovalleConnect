package com.agrovalleconnect.agrovalle_connect.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import com.agrovalleconnect.agrovalle_connect.model.Agricultor;
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

@Test
void borrarUsuario_tambienBorraSuAgricultor() {
    // GIVEN: un usuario con su agricultor ya guardados en la BD
    Agricultor sembrado = sembrarUsuarioConAgricultor();
    Long usuarioId = sembrado.getUsuario().getId();
    Long agricultorId = sembrado.getId();

    // WHEN: cargamos el usuario desde la BD y lo borramos
    Usuario usuario = entityManager.find(Usuario.class, usuarioId);
    entityManager.remove(usuario);
    entityManager.flush();
    entityManager.clear();

    // THEN: ambos desaparecieron
    assertThat(entityManager.find(Usuario.class, usuarioId)).isNull();
    assertThat(entityManager.find(Agricultor.class, agricultorId)).isNull();
}

@Test
void desvincularAgricultor_lo_borraDeLaBD() {
    // GIVEN
    Agricultor sembrado = sembrarUsuarioConAgricultor();
    Long usuarioId = sembrado.getUsuario().getId();
    Long agricultorId = sembrado.getId();

    // WHEN: al usuario le quitamos su agricultor (queda "huérfano")
    Usuario usuario = entityManager.find(Usuario.class, usuarioId);
    usuario.setAgricultor(null);
    entityManager.flush();
    entityManager.clear();

    // THEN: el usuario sigue existiendo, pero el agricultor se borró
    assertThat(entityManager.find(Usuario.class, usuarioId)).isNotNull();
    assertThat(entityManager.find(Agricultor.class, agricultorId)).isNull();
}

// Método auxiliar: prepara los datos para no repetir código
private Agricultor sembrarUsuarioConAgricultor() {
    Usuario usuario = Usuario.builder()
        .nombre("Pedro")
        .correo("pedro@mail.com")
        .passwordHash("hash_de_prueba")
        .rol(Usuario.Rol.AGRICULTOR)
        .fechaRegistro(LocalDateTime.now())
        .build();
    entityManager.persist(usuario);

    Agricultor agricultor = Agricultor.builder()
        .cedula("1234567890")
        .ubicacionValle("Dagua")
        .usuario(usuario)
        .build();
    entityManager.persist(agricultor);

    entityManager.flush();
    entityManager.clear();
    return agricultor;
}

        
    
}
