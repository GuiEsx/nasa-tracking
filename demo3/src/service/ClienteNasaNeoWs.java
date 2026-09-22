package service;

import model.Asteroide;
import service.feed.NasaNeoWsFeed;

import java.util.ArrayList;
import java.util.List;

public class ClienteNasaNeoWs {

    public static Asteroide buscarPrimeiroAsteroide() {
        try {
            NasaNeoWsFeed feed = new NasaNeoWsFeed();
            List<Asteroide> asteroides = feed.consultarFeed();
            if (asteroides != null && !asteroides.isEmpty()) {
                return asteroides.get(0);
            }
        } catch (Exception e) {
            System.out.println("Falha ao buscar asteroide pelo feed NeoWs: " + e.getMessage());
        }

        Asteroide backup = BackupDadosService.carregarAsteroide();
        if (backup != null) {
            return backup;
        }
        return RastreadorService.criarAsteroideFallback();
    }

    public static List<Asteroide> buscarTodosAsteroides() {
        try {
            NasaNeoWsFeed feed = new NasaNeoWsFeed();
            List<Asteroide> asteroides = feed.consultarFeed();
            if (asteroides != null && !asteroides.isEmpty()) {
                return asteroides;
            }
        } catch (Exception e) {
            System.out.println("Falha ao buscar asteroides pelo feed: " + e.getMessage());
        }

        List<Asteroide> backup = BackupDadosService.carregarTodosAsteroides();
        if (backup != null && !backup.isEmpty()) {
            return backup;
        }

        List<Asteroide> fallback = new ArrayList<>();
        fallback.add(RastreadorService.criarAsteroideFallback());
        return fallback;
    }
}
