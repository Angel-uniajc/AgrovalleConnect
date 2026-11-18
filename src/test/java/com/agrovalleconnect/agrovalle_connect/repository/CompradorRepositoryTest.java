package com.agrovalleconnect.agrovalle_connect.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import com.agrovalleconnect.agrovalle_connect.model.Comprador;
import com.agrovalleconnect.agrovalle_connect.model.Comprador.TipoComprador;
import com.agrovalleconnect.agrovalle_connect.model.Usuario;

import jakarta.persistence.PersistenceException;

@DataJpaTest
class CompradorRepositoryTest {

    @Autowired
    private CompradorRepository compradorRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario crearUsuarioComprador(String correo) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Restaurante La Ceiba");
        usuario.setCorreo(correo);
        usuario.setPasswordHash("hash_de_prueba");
        usuario.setRol(Usuario.Rol.COMPRADOR);
        return entityManager.persistAndFlush(usuario);
    }

    private Comprador crearComprador(String documento, Usuario usuario) {
        Comprador comprador = new Comprador();
        comprador.setDocumento(documento);
        comprador.setTipoComprador(TipoComprador.RESTAURANTE);
        comprador.setUsuario(usuario);
        return comprador;
    }

    @Test
    void guardarComprador_seRecuperaConSuUsuario() {
        // GIVEN: primero el usuario, luego el comprador
        Usuario usuario = crearUsuarioComprador("compras@laceiba.co");
        Comprador guardado = compradorRepository.save(crearComprador("900123456", usuario));

        // WHEN: forzamos la lectura desde la base de datos
        entityManager.flush();
        entityManager.clear();

        // THEN: se recupera con todos sus datos y su usuario
        Comprador recuperado = compradorRepository.findById(guardado.getId()).orElseThrow();
        assertEquals("900123456", recuperado.getDocumento());
        assertEquals(TipoComprador.RESTAURANTE, recuperado.getTipoComprador());
        assertEquals("compras@laceiba.co", recuperado.getUsuario().getCorreo());
        assertEquals(Usuario.Rol.COMPRADOR, recuperado.getUsuario().getRol());

        // THEN 2: el camino inverso, del usuario a su comprador (sin agricultor)
        Usuario usuarioRecuperado = entityManager.find(Usuario.class, usuario.getId());
        assertEquals("900123456", usuarioRecuperado.getComprador().getDocumento());
        assertEquals(null, usuarioRecuperado.getAgricultor());
    }

    @Test
    void findByUsuarioCorreo_encuentraElPerfilParaElLogin() {
        Usuario usuario = crearUsuarioComprador("sofia@correo.com");
        compradorRepository.saveAndFlush(crearComprador("12345678", usuario));
        entityManager.clear();

        Comprador encontrado = compradorRepository.findByUsuarioCorreo("sofia@correo.com").orElseThrow();

        assertEquals("12345678", encontrado.getDocumento());
        assertTrue(compradorRepository.findByUsuarioCorreo("nadie@correo.com").isEmpty());
    }

    @Test
    void existsByDocumento_detectaDocumentoRegistrado() {
        Usuario usuario = crearUsuarioComprador("sofia@correo.com");
        compradorRepository.saveAndFlush(crearComprador("12345678", usuario));

        assertTrue(compradorRepository.existsByDocumento("12345678"));
        assertFalse(compradorRepository.existsByDocumento("87654321"));
    }

    @Test
    void documentoDuplicado_violaLaRestriccionUnique() {
        compradorRepository.saveAndFlush(crearComprador("900123456", crearUsuarioComprador("a@correo.com")));
        Comprador otro = crearComprador("900123456", crearUsuarioComprador("b@correo.com"));

        assertThrows(DataIntegrityViolationException.class, () -> compradorRepository.saveAndFlush(otro));
    }

    @Test
    void mismoUsuarioConDosPerfiles_violaLaRestriccionUnique() {
        Usuario usuario = crearUsuarioComprador("a@correo.com");
        compradorRepository.saveAndFlush(crearComprador("11111111", usuario));
        Comprador segundoPerfil = crearComprador("22222222", usuario);

        assertThrows(DataIntegrityViolationException.class,
                () -> compradorRepository.saveAndFlush(segundoPerfil));
    }

    @Test
    void compradorSinUsuario_violaLaLlaveForanea() {
        Comprador sinUsuario = crearComprador("33333333", null);

        assertThrows(DataIntegrityViolationException.class,
                () -> compradorRepository.saveAndFlush(sinUsuario));
    }

    @Test
    void tipoComprador_seGuardaComoTextoEnMayusculas() {
        Comprador guardado = compradorRepository.saveAndFlush(
                crearComprador("44444444", crearUsuarioComprador("a@correo.com")));

        Object valor = entityManager.getEntityManager()
                .createNativeQuery("SELECT tipo_comprador FROM comprador WHERE id = ?1")
                .setParameter(1, guardado.getId())
                .getSingleResult();

        assertEquals("RESTAURANTE", valor);
    }

    @Test
    void tipoCompradorFueraDelEnum_loRechazaLaBaseDeDatos() {
        Usuario usuario = crearUsuarioComprador("a@correo.com");

        // Solo INDIVIDUAL, COMERCIANTE o RESTAURANTE, como el CHECK de schema.sql
        PersistenceException ex = assertThrows(PersistenceException.class, () -> entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO comprador (documento, tipo_comprador, usuario_id) "
                        + "VALUES ('55555555', 'MAYORISTA', ?1)")
                .setParameter(1, usuario.getId())
                .executeUpdate());

        // PostgreSQL lo rechaza con el CHECK; H2 (pruebas) con su tipo ENUM equivalente
        String mensaje = mensajeCompleto(ex).toLowerCase();
        assertTrue(mensaje.contains("check constraint") || mensaje.contains("value not permitted"),
                "Debe fallar por los valores permitidos de tipo_comprador: " + mensaje);
    }

    private static String mensajeCompleto(Throwable error) {
        StringBuilder mensaje = new StringBuilder();
        for (Throwable actual = error; actual != null; actual = actual.getCause()) {
            mensaje.append(actual.getMessage()).append(" | ");
        }
        return mensaje.toString();
    }
}
