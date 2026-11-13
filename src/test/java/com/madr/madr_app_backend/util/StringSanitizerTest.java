package com.madr.madr_app_backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class StringSanitizerTest {

    @ParameterizedTest(name = "Entrada: \"{0}\" -> Saída esperada: \"{1}\"")
    @CsvSource({
            "Machado de Assis, machado de assis",
            "Manuel Bandeira, manuel bandeira",
            "'Edgar Alan Poe ', edgar alan poe",
            "Androides Sonham Com Ovelhas Elétricas?, androides sonham com ovelhas elétricas?",
            "' breve história do tempo ', breve história do tempo"
    })
    @DisplayName("Deve sanitizar strings conforme a tabela do enunciado")
    void deveSanitiarNomesCorretamente(String entrada, String esperado){
        String resultado = StringSanitizer.sanitize(entrada);
        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Deve retornar nulo se a entrada for nula")
    void deveRetornarNullQuandoEntradaForNull(){
        assertThat(StringSanitizer.sanitize(null)).isNull();
    }

}
