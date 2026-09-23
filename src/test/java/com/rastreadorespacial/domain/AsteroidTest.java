package com.rastreadorespacial.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AsteroidTest {

    @Test
    void testAvaliarRiscoCritico() {
        // PHA da NASA com aproximação perigosa < 1.0 LD (ex: 0.5 LD)
        Asteroid a = new Asteroid("99942", "Apophis", Distance.ofLunarDistances(0.5), true);
        assertEquals(RiskLevel.CRITICO, a.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoAlto() {
        // PHA da NASA com distância moderada (ex: 8.0 LD) -> ALTO pela flag
        Asteroid a1 = new Asteroid("101955", "Bennu", Distance.ofLunarDistances(8.0), true);
        assertEquals(RiskLevel.ALTO, a1.avaliarRisco());

        // Não-PHA mas passagem muito próxima < 5.0 LD (ex: 3.2 LD) -> ALTO pela proximidade
        Asteroid a2 = new Asteroid("2024-AA", "2024 AA", Distance.ofLunarDistances(3.2), false);
        assertEquals(RiskLevel.ALTO, a2.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoMedio() {
        // Não-PHA com passagem entre 5 e 20 LD (ex: 12.5 LD)
        Asteroid a = new Asteroid("433", "Eros", Distance.ofLunarDistances(12.5), false);
        assertEquals(RiskLevel.MEDIO, a.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoBaixo() {
        // Não-PHA distante (> 20 LD, ex: 45.0 LD)
        Asteroid a = new Asteroid("4", "Vesta", Distance.ofLunarDistances(45.0), false);
        assertEquals(RiskLevel.BAIXO, a.avaliarRisco());
    }

    @Test
    void testEncapsulamentoNenhumGetterPublicoDeLimiares() {
        Method[] methods = Asteroid.class.getMethods();

        boolean hasPublicThresholdGetter = Arrays.stream(methods)
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .anyMatch(m -> m.getName().toLowerCase().contains("limiar")
                        || m.getName().toLowerCase().contains("threshold")
                        || m.getName().toLowerCase().contains("distanciacritica")
                        || m.getName().toLowerCase().contains("distanciaalta"));

        assertFalse(hasPublicThresholdGetter, "Nenhum método público deve expor os limiares internos de risco de Asteroid");
    }
}
