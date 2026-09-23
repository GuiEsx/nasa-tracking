package com.rastreadorespacial.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CometTest {

    @Test
    void testAvaliarRiscoCritico() {
        // Periélio extremamente próximo < 2.0 LD (ex: 1.2 LD)
        Comet c1 = new Comet("C/2024-S1", "ATLAS", Distance.ofLunarDistances(1.2), 1.0);
        assertEquals(RiskLevel.CRITICO, c1.avaliarRisco());

        // Núcleo gigante (>= 5 km) com periélio < 10 LD (ex: 6.5 LD)
        Comet c2 = new Comet("1P", "Halley", Distance.ofLunarDistances(6.5), 11.0);
        assertEquals(RiskLevel.CRITICO, c2.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoAlto() {
        // Periélio < 10 LD (ex: 8.0 LD) com núcleo menor
        Comet c1 = new Comet("2P", "Encke", Distance.ofLunarDistances(8.0), 1.5);
        assertEquals(RiskLevel.ALTO, c1.avaliarRisco());

        // Periélio entre 10 e 50 LD (ex: 30.0 LD) com núcleo médio >= 2.0 km
        Comet c2 = new Comet("67P", "Churyumov-Gerasimenko", Distance.ofLunarDistances(30.0), 4.1);
        assertEquals(RiskLevel.ALTO, c2.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoMedio() {
        // Periélio < 50 LD (ex: 35.0 LD) com núcleo pequeno ou desconhecido
        Comet c = new Comet("103P", "Hartley 2", Distance.ofLunarDistances(35.0), null);
        assertEquals(RiskLevel.MEDIO, c.avaliarRisco());
    }

    @Test
    void testAvaliarRiscoBaixo() {
        // Periélio distante (ex: 120.0 LD)
        Comet c = new Comet("C/1995-O1", "Hale-Bopp", Distance.ofLunarDistances(120.0), 60.0);
        assertEquals(RiskLevel.BAIXO, c.avaliarRisco());
    }

    @Test
    void testEncapsulamentoNenhumGetterPublicoDeLimiares() {
        Method[] methods = Comet.class.getMethods();

        boolean hasPublicThresholdGetter = Arrays.stream(methods)
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .anyMatch(m -> m.getName().toLowerCase().contains("limiar")
                        || m.getName().toLowerCase().contains("threshold")
                        || m.getName().toLowerCase().contains("perieliocritico"));

        assertFalse(hasPublicThresholdGetter, "Nenhum método público deve expor os limiares internos de risco de Comet");
    }
}
