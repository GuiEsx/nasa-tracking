package com.rastreadorespacial.menu;

import com.rastreadorespacial.domain.Asteroid;
import com.rastreadorespacial.domain.Comet;
import com.rastreadorespacial.domain.Distance;
import com.rastreadorespacial.domain.HasDistance;
import com.rastreadorespacial.domain.Position;
import com.rastreadorespacial.domain.RiskLevel;
import com.rastreadorespacial.domain.Satellite;
import com.rastreadorespacial.domain.SpaceObject;

import java.util.Locale;

final class MenuFormatters {
    private MenuFormatters() {}

    static String objeto(SpaceObject objeto) {
        if (objeto instanceof Asteroid asteroid) {
            return String.format(Locale.US, "Asteroide %s — %.1f distâncias lunares",
                    asteroid.getNome(), asteroid.getDistanciaDePassagem().emDistanciasLunares());
        }
        if (objeto instanceof Comet comet) {
            String nucleo = comet.getDiametroNucleoKm() == null ? "N/D"
                    : String.format(Locale.US, "%.1f", comet.getDiametroNucleoKm());
            return String.format("Cometa %s — núcleo %s km", comet.getNome(), nucleo);
        }
        if (objeto instanceof Satellite satellite) {
            String decaimento = satellite.getDecaimentoOrbitalDiario() == null ? "N/D"
                    : String.format(Locale.US, "%.2f", satellite.getDecaimentoOrbitalDiario());
            return String.format(Locale.US, "Satélite %s (NORAD %s) — decaimento %s km/dia",
                    satellite.getNome(), satellite.getId(), decaimento);
        }
        return objeto.getNome();
    }

    static String risco(SpaceObject objeto) {
        return String.format("[%-7s] %s", objeto.avaliarRisco().name(), objeto(objeto));
    }

    static String distancia(SpaceObject objeto) {
        if (objeto instanceof HasDistance hasDistance) {
            return hasDistance.getDistanciaDeReferencia()
                    .map(distance -> String.format(Locale.US, "%s — %.1f LD",
                            objeto.getNome(), distance.emDistanciasLunares()))
                    .orElse(objeto.getNome() + " — distância não aplicável");
        }
        return objeto.getNome() + " — distância não disponível";
    }

    static String posicao(SpaceObject objeto, Position position) {
        return String.format(Locale.US, "%s — lat %.2f°, lon %.2f°, altitude %.1f km",
                objeto.getNome(), position.latitudeGraus(), position.longitudeGraus(),
                position.altitude().emQuilometros());
    }
}
