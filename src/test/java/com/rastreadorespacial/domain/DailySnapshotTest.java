package com.rastreadorespacial.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DailySnapshotTest {

    @Test
    void testQuantidadeDeObjetosEImutabilidade() {
        Asteroid a1 = new Asteroid("1", "Asteroid 1", Distance.ofLunarDistances(10.0), false);
        Asteroid a2 = new Asteroid("2", "Asteroid 2", Distance.ofLunarDistances(20.0), false);

        DailySnapshot snapshot = new DailySnapshot(LocalDate.of(2026, 7, 19), List.of(a1, a2));

        assertEquals(2, snapshot.quantidadeDeObjetos());
        assertEquals(LocalDate.of(2026, 7, 19), snapshot.getData());

        // Garante imutabilidade da lista retornada
        assertThrows(UnsupportedOperationException.class, () ->
                snapshot.getObjetos().add(new Asteroid("3", "Asteroid 3", Distance.ofLunarDistances(5.0), true)));
    }

    @Test
    void testMaisPerigosoEntreTiposMistos() {
        Asteroid astBaixo = new Asteroid("1", "Asteroid Low", Distance.ofLunarDistances(50.0), false);
        Comet cometMedio = new Comet("2", "Comet Mid", Distance.ofLunarDistances(35.0), null);
        Satellite satCritico = new Satellite("3", "Satellite Reentry", 0.02); // CRITICO
        Asteroid astAlto = new Asteroid("4", "Asteroid High", Distance.ofLunarDistances(3.0), false); // ALTO

        DailySnapshot snapshot = new DailySnapshot(LocalDate.of(2026, 7, 19),
                List.of(astBaixo, cometMedio, satCritico, astAlto));

        Optional<SpaceObject> perigoso = snapshot.maisPerigoso();
        assertTrue(perigoso.isPresent());
        assertEquals(satCritico, perigoso.get(), "O mais perigoso deve ser o satélite em nível CRITICO");
        assertEquals(RiskLevel.CRITICO, perigoso.get().avaliarRisco());
    }

    @Test
    void testMaisProximoConsideraApenasObjetosComDistancia() {
        // Satélite não expõe distância astronômica
        Satellite sat = new Satellite("1", "ISS", null);

        // Cometa a 5.0 LD
        Comet comet = new Comet("2", "Comet 1", Distance.ofLunarDistances(5.0), 1.0);

        // Asteroide mais próximo a 0.8 LD
        Asteroid astProximo = new Asteroid("3", "2026-AB", Distance.ofLunarDistances(0.8), false);

        // Asteroide mais distante a 12.0 LD
        Asteroid astDistante = new Asteroid("4", "2026-CD", Distance.ofLunarDistances(12.0), false);

        DailySnapshot snapshot = new DailySnapshot(LocalDate.of(2026, 7, 19),
                List.of(sat, comet, astProximo, astDistante));

        Optional<SpaceObject> proximo = snapshot.maisProximo();
        assertTrue(proximo.isPresent());
        assertEquals(astProximo, proximo.get(), "O mais próximo deve ser o asteroide 2026-AB a 0.8 LD");
    }

    @Test
    void testContagemPorNivelDeRisco() {
        Asteroid a1 = new Asteroid("1", "A1", Distance.ofLunarDistances(0.5), true); // CRITICO
        Asteroid a2 = new Asteroid("2", "A2", Distance.ofLunarDistances(3.0), false); // ALTO
        Comet c1 = new Comet("3", "C1", Distance.ofLunarDistances(30.0), 3.0); // ALTO
        Asteroid a3 = new Asteroid("4", "A3", Distance.ofLunarDistances(15.0), false); // MEDIO
        Satellite s1 = new Satellite("5", "S1", null); // BAIXO

        DailySnapshot snapshot = new DailySnapshot(LocalDate.of(2026, 7, 19),
                List.of(a1, a2, c1, a3, s1));

        Map<RiskLevel, Long> contagens = snapshot.contagemPorNivelDeRisco();

        assertEquals(1L, contagens.get(RiskLevel.CRITICO));
        assertEquals(2L, contagens.get(RiskLevel.ALTO));
        assertEquals(1L, contagens.get(RiskLevel.MEDIO));
        assertEquals(1L, contagens.get(RiskLevel.BAIXO));
    }

    @Test
    void testResumoTextualFormatado() {
        Asteroid astProximo = new Asteroid("1", "asteroide 2026-AB", Distance.ofLunarDistances(0.8), false); // MEDIO
        Asteroid astPerigoso = new Asteroid("2", "2024-XY", Distance.ofLunarDistances(3.0), false); // ALTO

        DailySnapshot snapshot = new DailySnapshot(LocalDate.of(2026, 7, 19),
                List.of(astProximo, astPerigoso));

        String resumo = SnapshotFormatter.resumoTextual(snapshot);

        assertTrue(resumo.contains("Retrato de 19/07/2026: 2 objetos."));
        assertTrue(resumo.contains("Mais próximo: asteroide 2026-AB a 0,8 distâncias lunares."));
        assertTrue(resumo.contains("Mais perigoso: 2024-XY (ALTO)."));
    }

    @Test
    void testResumoTextualListaVazia() {
        DailySnapshot snapshotVazio = new DailySnapshot(LocalDate.of(2026, 7, 19), List.of());
        assertEquals("Nenhum objeto rastreado nesta data.", SnapshotFormatter.resumoTextual(snapshotVazio));
        assertEquals("Nenhum objeto rastreado nesta data.", SnapshotFormatter.resumoTextual(null));
    }
}

