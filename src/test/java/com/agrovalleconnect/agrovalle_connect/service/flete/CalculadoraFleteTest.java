package com.agrovalleconnect.agrovalle_connect.service.flete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.agrovalleconnect.agrovalle_connect.exception.MunicipioSinTarifaException;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraFleteTest {

    private final CalculadoraFlete calculadoraFlete =
            new CalculadoraFlete(List.of(new FleteDagua(), new FletePalmira(), new FleteBuga()));

    @Test
    void calcular_municipioDagua_devuelveDoceMil() {
        assertThat(calculadoraFlete.calcular("Dagua")).isEqualByComparingTo("12000.00");
    }

    @Test
    void calcular_municipioPalmira_devuelveDiezMil() {
        assertThat(calculadoraFlete.calcular("Palmira")).isEqualByComparingTo("10000.00");
    }

    @Test
    void calcular_municipioBuga_devuelveQuinceMil() {
        assertThat(calculadoraFlete.calcular("Buga")).isEqualByComparingTo("15000.00");
    }

    @Test
    void calcular_municipioSinTarifa_lanzaMunicipioSinTarifaException() {
        assertThatThrownBy(() -> calculadoraFlete.calcular("Cali"))
                .isInstanceOf(MunicipioSinTarifaException.class)
                .hasMessage("Municipio sin tarifa de flete");
    }

    @Test
    void calcularTotal_municipiosRepetidos_sumaUnaTarifaPorMunicipio() {
        assertThat(calculadoraFlete.calcularTotal(List.of("Dagua", "Palmira", "Dagua")))
                .isEqualByComparingTo("22000.00");
    }

    @Test
    void calcularTotal_unMunicipioSinTarifa_lanzaMunicipioSinTarifaException() {
        assertThatThrownBy(() -> calculadoraFlete.calcularTotal(List.of("Palmira", "Cali")))
                .isInstanceOf(MunicipioSinTarifaException.class);
    }
}
