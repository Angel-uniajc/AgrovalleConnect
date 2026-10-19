package com.agrovalleconnect.agrovalle_connect.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres-123";

    private final JwtUtil jwtUtil = new JwtUtil(SECRETO, 3_600_000L);

    @Test
    void generarToken_incluyeRolPerfilIdYExpiracion() {
        String token = jwtUtil.generarToken("pedro@mail.com", "AGRICULTOR", 7L);

        Claims claims = jwtUtil.extraerClaims(token);

        assertThat(claims.getSubject()).isEqualTo("pedro@mail.com");
        assertThat(claims.get("rol", String.class)).isEqualTo("AGRICULTOR");
        assertThat(claims.get("perfil_id", Long.class)).isEqualTo(7L);
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void esValido_conTokenCorrecto_devuelveTrue() {
        String token = jwtUtil.generarToken("pedro@mail.com", "AGRICULTOR", 7L);

        assertThat(jwtUtil.esValido(token)).isTrue();
    }

    @Test
    void esValido_conTokenExpirado_devuelveFalse() {
        JwtUtil expiraYa = new JwtUtil(SECRETO, -1000L);
        String token = expiraYa.generarToken("pedro@mail.com", "AGRICULTOR", 7L);

        assertThat(jwtUtil.esValido(token)).isFalse();
    }

    @Test
    void esValido_conFirmaDeOtraClave_devuelveFalse() {
        JwtUtil otro = new JwtUtil("otro-secreto-distinto-con-mas-de-32-caracteres-9", 3_600_000L);
        String tokenFalso = otro.generarToken("pedro@mail.com", "AGRICULTOR", 7L);

        assertThat(jwtUtil.esValido(tokenFalso)).isFalse();
    }

    @Test
    void esValido_conTokenAlterado_devuelveFalse() {
        String token = jwtUtil.generarToken("pedro@mail.com", "AGRICULTOR", 7L);
        String alterado = token.substring(0, token.length() - 3) + "abc";

        assertThat(jwtUtil.esValido(alterado)).isFalse();
    }

    @Test
    void esValido_conTextoBasura_devuelveFalse() {
        assertThat(jwtUtil.esValido("esto-no-es-un-token")).isFalse();
    }
}
