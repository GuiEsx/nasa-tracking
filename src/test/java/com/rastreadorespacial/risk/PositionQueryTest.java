package com.rastreadorespacial.risk;

import com.rastreadorespacial.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PositionQueryTest {

    // Implementação de teste que NÃO implementa Locatable
    private record NonLocatableObject(String id, String nome) implements SpaceObject {
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
            return RiskLevel.BAIXO;
        }
    }

    @Test
    void testSatelliteReturnsValidPosition() {
        Satellite sat = new Satellite("25544", "ISS", 0.0001, 51.64, 0.0005, 15.5);
        Position pos = sat.getPosicaoAtual();

        assertNotNull(pos);
        assertTrue(pos.latitudeGraus() >= -90.0 && pos.latitudeGraus() <= 90.0, "Latitude do satélite deve estar entre -90 e 90");
        assertTrue(pos.longitudeGraus() >= -180.0 && pos.longitudeGraus() <= 180.0, "Longitude do satélite deve estar entre -180 e 180");
        assertTrue(pos.altitude().emQuilometros() >= 100.0, "Altitude LEO deve ser plausível");
        assertNotNull(pos.instanteDaMedicao());
    }

    @Test
    void testAsteroidReturnsValidPosition() {
        Asteroid ast = new Asteroid("99942", "Apophis", Distance.ofLunarDistances(0.8), true);
        Position pos = ast.getPosicaoAtual();

        assertNotNull(pos);
        assertTrue(pos.latitudeGraus() >= -90.0 && pos.latitudeGraus() <= 90.0);
        assertTrue(pos.longitudeGraus() >= -180.0 && pos.longitudeGraus() <= 180.0);
        assertEquals(0.8, pos.altitude().emDistanciasLunares(), 1e-4);
        assertNotNull(pos.instanteDaMedicao());
    }

    @Test
    void testCometReturnsValidPosition() {
        Comet comet = new Comet("1P", "Halley", Distance.ofLunarDistances(5.0), 11.0);
        Position pos = comet.getPosicaoAtual();

        assertNotNull(pos);
        assertTrue(pos.latitudeGraus() >= -90.0 && pos.latitudeGraus() <= 90.0);
        assertTrue(pos.longitudeGraus() >= -180.0 && pos.longitudeGraus() <= 180.0);
        assertEquals(5.0, pos.altitude().emDistanciasLunares(), 1e-4);
        assertNotNull(pos.instanteDaMedicao());
    }

    @Test
    void testPosicoesAtuaisPolymorphicExtraction() {
        Satellite sat = new Satellite("25544", "ISS", null);
        Asteroid ast = new Asteroid("433", "Eros", Distance.ofLunarDistances(15.0), false);
        Comet comet = new Comet("C/2024-S1", "ATLAS", Distance.ofLunarDistances(8.0), 1.0);
        NonLocatableObject nonLocatable = new NonLocatableObject("UNKNOWN-1", "Theoretical Debris");

        List<SpaceObject> listaMista = List.of(sat, ast, nonLocatable, comet);

        List<Position> posicoes = PositionQuery.posicoesAtuais(listaMista);

        // Deve retornar exatamente 3 posições (o não-localizável é ignorado)
        assertEquals(3, posicoes.size(), "Deve conter posições apenas dos 3 objetos que implementam Locatable");

        for (Position pos : posicoes) {
            assertNotNull(pos.altitude());
            assertNotNull(pos.instanteDaMedicao());
        }
    }

    @Test
    void testPositionValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                new Position(95.0, 0.0, Distance.ofKilometers(100), Instant.now()));
        assertThrows(IllegalArgumentException.class, () ->
                new Position(-95.0, 0.0, Distance.ofKilometers(100), Instant.now()));
        assertThrows(IllegalArgumentException.class, () ->
                new Position(0.0, 190.0, Distance.ofKilometers(100), Instant.now()));
        assertThrows(IllegalArgumentException.class, () ->
                new Position(0.0, -190.0, Distance.ofKilometers(100), Instant.now()));
        assertThrows(NullPointerException.class, () ->
                new Position(0.0, 0.0, null, Instant.now()));
        assertThrows(NullPointerException.class, () ->
                new Position(0.0, 0.0, Distance.ofKilometers(100), null));
    }
}
