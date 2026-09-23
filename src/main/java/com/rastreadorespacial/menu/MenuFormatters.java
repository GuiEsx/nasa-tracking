package com.rastreadorespacial.menu;

import com.rastreadorespacial.domain.Asteroid;
import com.rastreadorespacial.domain.Comet;
import com.rastreadorespacial.domain.Distance;
import com.rastreadorespacial.domain.DistanceUnit;
import com.rastreadorespacial.domain.HasDistance;
import com.rastreadorespacial.domain.Position;
import com.rastreadorespacial.domain.RiskLevel;
import com.rastreadorespacial.domain.Satellite;
import com.rastreadorespacial.domain.SpaceObject;

import java.util.Locale;

final class MenuFormatters {
    private MenuFormatters() {}

    static String objeto(SpaceObject objeto) {
        return objeto(objeto, DistanceUnit.KILOMETERS);
    }

    static String objeto(SpaceObject objeto, DistanceUnit unidade) {
        if (objeto instanceof Asteroid asteroid) {
            return String.format(Locale.US, "Asteroide %s — %s",
                    asteroid.getNome(), formatarDistancia(asteroid.getDistanciaDePassagem(), unidade));
        }
        if (objeto instanceof Comet comet) {
            String nucleo = comet.getDiametroNucleoKm() == null ? "N/D"
                    : String.format(Locale.US, "%.1f", comet.getDiametroNucleoKm());
            return String.format("Cometa %s — periélio %s — núcleo %s km",
                    comet.getNome(), formatarDistancia(comet.getDistanciaPerielio(), unidade), nucleo);
        }
        if (objeto instanceof Satellite satellite) {
            String decaimento = satellite.getDecaimentoOrbitalDiario() == null ? "N/D"
                    : String.format(Locale.US, "%.3f", satellite.getDecaimentoOrbitalDiario());
            return String.format(Locale.US, "Satélite %s (NORAD %s) — decaimento %s km/dia",
                    satellite.getNome(), satellite.getId(), decaimento);
        }
        return objeto.getNome();
    }

    static String risco(SpaceObject objeto) {
        return risco(objeto, DistanceUnit.KILOMETERS);
    }

    static String risco(SpaceObject objeto, DistanceUnit unidade) {
        return String.format("[%-7s] %s", objeto.avaliarRisco().name(), objeto(objeto, unidade));
    }

    static String distancia(SpaceObject objeto) {
        return distancia(objeto, DistanceUnit.KILOMETERS);
    }

    static String distancia(SpaceObject objeto, DistanceUnit unidade) {
        if (objeto instanceof HasDistance hasDistance) {
            return hasDistance.getDistanciaDeReferencia()
                    .map(distance -> String.format(Locale.US, "%s — %s",
                            objeto.getNome(), formatarDistancia(distance, unidade)))
                    .orElse(objeto.getNome() + " — distância não aplicável");
        }
        return objeto.getNome() + " — distância não disponível";
    }

    static String posicao(SpaceObject objeto, Position position) {
        return posicao(objeto, position, DistanceUnit.KILOMETERS);
    }

    static String posicao(SpaceObject objeto, Position position, DistanceUnit unidade) {
        return String.format(Locale.US, "%s — lat %.2f°, lon %.2f°, altitude %s",
                objeto.getNome(), position.latitudeGraus(), position.longitudeGraus(),
                formatarDistancia(position.altitude(), unidade));
    }

    private static String formatarDistancia(Distance distancia, DistanceUnit unidade) {
        return switch (unidade) {
            case KILOMETERS -> String.format(Locale.US, "%.3f km", distancia.emQuilometros());
            case MILES -> String.format(Locale.US, "%.3f mi", distancia.emMilhas());
            case LUNAR_DISTANCES -> String.format(Locale.US, "%.3f LD", distancia.emDistanciasLunares());
        };
    }
}
