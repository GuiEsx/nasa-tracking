package com.rastreadorespacial.risk;

import com.rastreadorespacial.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RiskRankingTest {

    // Nova implementação fictícia para demonstrar o polimorfismo do RF5
    private record TestDummySpaceObject(String id, String nome, RiskLevel riskLevel) implements SpaceObject {
        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getIdCanonico() {
            return id;
        }

        @Override
        public String getNome() {
            return nome;
        }

        @Override
        public RiskLevel avaliarRisco() {
            return riskLevel;
        }
    }

    @Test
    void testOrdenarPorPerigoColecaoMista() {
        // Criando objetos conhecidos com níveis de risco pré-determinados:
        // 1. Asteroide CRITICO (PHA + < 1 LD)
        Asteroid asteroideCritico = new Asteroid("99942", "Apophis", Distance.ofLunarDistances(0.8), true);
        assertEquals(RiskLevel.CRITICO, asteroideCritico.avaliarRisco());

        // 2. Cometa ALTO (Periélio < 10 LD)
        Comet cometaAlto = new Comet("C/2024-S1", "ATLAS", Distance.ofLunarDistances(8.0), 1.5);
        assertEquals(RiskLevel.ALTO, cometaAlto.avaliarRisco());

        // 3. Asteroide MEDIO (distância entre 5 e 20 LD, sem PHA)
        Asteroid asteroideMedio = new Asteroid("433", "Eros", Distance.ofLunarDistances(15.0), false);
        assertEquals(RiskLevel.MEDIO, asteroideMedio.avaliarRisco());

        // 4. Satélite BAIXO (órbita estável, sem decaimento rápido)
        Satellite sateliteBaixo = new Satellite("25544", "ISS (ZARYA)", 0.00001);
        assertEquals(RiskLevel.BAIXO, sateliteBaixo.avaliarRisco());

        // Lista desordenada propositalmente
        List<SpaceObject> listaMista = List.of(
                asteroideMedio,
                sateliteBaixo,
                asteroideCritico,
                cometaAlto
        );

        List<SpaceObject> resultado = RiskRanking.ordenarPorPerigo(listaMista);

        assertEquals(4, resultado.size());
        assertEquals(asteroideCritico, resultado.get(0), "1º deve ser CRITICO");
        assertEquals(cometaAlto, resultado.get(1), "2º deve ser ALTO");
        assertEquals(asteroideMedio, resultado.get(2), "3º deve ser MEDIO");
        assertEquals(sateliteBaixo, resultado.get(3), "4º deve ser BAIXO");
    }

    @Test
    void testOrdenarPorPerigoComNovoTipoDummyDemonstrandoPolimorfismo() {
        // Objeto de um tipo totalmente novo criado apenas no teste
        SpaceObject novoTipoCritico = new TestDummySpaceObject("SONDA-X", "Alien Probe", RiskLevel.CRITICO);
        SpaceObject asteroideBaixo = new Asteroid("4", "Vesta", Distance.ofLunarDistances(45.0), false);
        SpaceObject sateliteAlto = new Satellite("99999", "Debris Alpha", 0.005);

        List<SpaceObject> lista = List.of(asteroideBaixo, novoTipoCritico, sateliteAlto);
        List<SpaceObject> resultado = RiskRanking.ordenarPorPerigo(lista);

        assertEquals(3, resultado.size());
        assertEquals(novoTipoCritico, resultado.get(0));
        assertEquals(sateliteAlto, resultado.get(1));
        assertEquals(asteroideBaixo, resultado.get(2));
    }

    @Test
    void testDesempatePorNomeAlfabético() {
        SpaceObject objA = new TestDummySpaceObject("1", "Alpha Object", RiskLevel.ALTO);
        SpaceObject objB = new TestDummySpaceObject("2", "Beta Object", RiskLevel.ALTO);
        SpaceObject objZ = new TestDummySpaceObject("3", "Zulu Object", RiskLevel.ALTO);

        List<SpaceObject> lista = List.of(objZ, objA, objB);
        List<SpaceObject> resultado = RiskRanking.ordenarPorPerigo(lista);

        assertEquals(List.of(objA, objB, objZ), resultado, "Objetos com mesmo RiskLevel devem desempatar por nome A-Z");
    }

    @Test
    void testAgruparPorNivel() {
        Asteroid a1 = new Asteroid("1", "Asteroide 1", Distance.ofLunarDistances(0.5), true); // CRITICO
        Comet c1 = new Comet("2", "Cometa 1", Distance.ofLunarDistances(8.0), 1.0); // ALTO
        Asteroid a2 = new Asteroid("3", "Asteroide 2", Distance.ofLunarDistances(15.0), false); // MEDIO
        Satellite s1 = new Satellite("4", "Satelite 1", null); // BAIXO

        List<SpaceObject> lista = List.of(a1, c1, a2, s1);
        Map<RiskLevel, List<SpaceObject>> mapa = RiskRanking.agruparPorNivel(lista);

        assertNotNull(mapa);
        assertEquals(1, mapa.get(RiskLevel.CRITICO).size());
        assertTrue(mapa.get(RiskLevel.CRITICO).contains(a1));

        assertEquals(1, mapa.get(RiskLevel.ALTO).size());
        assertTrue(mapa.get(RiskLevel.ALTO).contains(c1));

        assertEquals(1, mapa.get(RiskLevel.MEDIO).size());
        assertTrue(mapa.get(RiskLevel.MEDIO).contains(a2));

        assertEquals(1, mapa.get(RiskLevel.BAIXO).size());
        assertTrue(mapa.get(RiskLevel.BAIXO).contains(s1));
    }

    @Test
    void testListaVaziaOuNula() {
        assertTrue(RiskRanking.ordenarPorPerigo(null).isEmpty());
        assertTrue(RiskRanking.ordenarPorPerigo(List.of()).isEmpty());
        assertTrue(RiskRanking.agruparPorNivel(null).isEmpty());
        assertTrue(RiskRanking.agruparPorNivel(List.of()).isEmpty());
    }
}
