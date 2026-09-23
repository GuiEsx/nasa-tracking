package com.rastreadorespacial.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SatelliteTest {

    @Test
    void testAvaliarRiscoCritico() {
        // Taxa de decaimento >= 0.01 (reentrada iminente)
        Satellite s = new Satellite("25544", "ISS (Reentrada de detrito)", 0.015);
        assertEquals(RiskLevel.CRITICO, s.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoAlto() {
        // Taxa de decaimento entre 0.001 e 0.01
        Satellite s = new Satellite("48274", "Starlink-1234 (Decaindo)", 0.0035);
        assertEquals(RiskLevel.ALTO, s.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoMedio() {
        // Taxa de decaimento entre 0.0001 e 0.001
        Satellite s = new Satellite("39084", "Tiangong-1", 0.00045);
        assertEquals(RiskLevel.MEDIO, s.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoBaixo() {
        // Órbita perfeitamente estável (< 0.0001 ou nulo)
        Satellite s1 = new Satellite("25544", "ISS (ZARYA)", 0.00002);
        assertEquals(RiskLevel.BAIXO, s1.avaliarRisco());

        Satellite s2 = new Satellite("20580", "HST (Hubble)", null);
        assertEquals(RiskLevel.BAIXO, s2.avaliarRisco());
    }

    @Test
    void testEncapsulamentoNenhumGetterPublicoDeLimiares() {
        Method[] methods = Satellite.class.getMethods();

        boolean hasPublicThresholdGetter = Arrays.stream(methods)
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .anyMatch(m -> m.getName().toLowerCase().contains("limiar")
                        || m.getName().toLowerCase().contains("threshold")
                        || m.getName().toLowerCase().contains("decaimentocritico"));

        assertFalse(hasPublicThresholdGetter, "Nenhum método público deve expor os limiares internos de risco de Satellite");
    }
}
