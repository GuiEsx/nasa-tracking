package com.rastreadorespacial.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa um cometa rastreado no sistema solar.
 *
 * <p><strong>Identificador Canônico:</strong> O {@link #getIdCanonico()} corresponde à designação
 * oficial IAU (International Astronomical Union) ou ao identificador oficial de corpos menores da NASA/JPL (ex: "1P", "C/2024-S1").</p>
 *
 * <p><strong>Critérios de Avaliação de Risco:</strong></p>
 * <ul>
 *   <li><strong>CRITICO:</strong> Distância de periélio extremamente próxima da Terra (< {@value #LIMIAR_PERIELIO_CRITICO_LD} LD) OU periélio < {@value #LIMIAR_PERIELIO_ALTO_LD} LD associado a um núcleo massivo (>= {@value #LIMIAR_NUCLEO_GRANDE_KM} km).</li>
 *   <li><strong>ALTO:</strong> Periélio < {@value #LIMIAR_PERIELIO_ALTO_LD} LD OU periélio < {@value #LIMIAR_PERIELIO_MEDIO_LD} LD com núcleo >= {@value #LIMIAR_NUCLEO_MEDIO_KM} km.</li>
 *   <li><strong>MEDIO:</strong> Periélio < {@value #LIMIAR_PERIELIO_MEDIO_LD} LD.</li>
 *   <li><strong>BAIXO:</strong> Periélio distante (>= {@value #LIMIAR_PERIELIO_MEDIO_LD} LD) ou cometa de pequenas proporções afastado.</li>
 * </ul>
 *
 * <p><strong>Efeméride Aproximada:</strong>
 * A posição retornada por {@link #getPosicaoAtual()} representa uma aproximação de coordenadas celestes
 * para fins de visualização no painel, adotando a distância de periélio como altitude/distância espacial de referência.</p>
 */
public final class Comet implements SpaceObject, Locatable, HasDistance {

    // Limiares encapsulados internamente (não expostos publicamente)
    private static final double LIMIAR_PERIELIO_CRITICO_LD = 2.0;
    private static final double LIMIAR_PERIELIO_ALTO_LD = 10.0;
    private static final double LIMIAR_PERIELIO_MEDIO_LD = 50.0;

    private static final double LIMIAR_NUCLEO_GRANDE_KM = 5.0;
    private static final double LIMIAR_NUCLEO_MEDIO_KM = 2.0;

    private static final Distance PERIELIO_CRITICO = Distance.ofLunarDistances(LIMIAR_PERIELIO_CRITICO_LD);
    private static final Distance PERIELIO_ALTO = Distance.ofLunarDistances(LIMIAR_PERIELIO_ALTO_LD);
    private static final Distance PERIELIO_MEDIO = Distance.ofLunarDistances(LIMIAR_PERIELIO_MEDIO_LD);

    private final String id;
    private final String nome;
    private final Distance distanciaPerielio;
    private final Double diametroNucleoKm; // null se desconhecido

    public Comet(String id, String nome, Distance distanciaPerielio, Double diametroNucleoKm) {
        this.id = Objects.requireNonNull(id, "O ID do cometa não pode ser nulo.");
        this.nome = Objects.requireNonNull(nome, "O nome do cometa não pode ser nulo.");
        this.distanciaPerielio = Objects.requireNonNull(distanciaPerielio, "A distância de periélio não pode ser nula.");
        this.diametroNucleoKm = diametroNucleoKm;
    }

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

    public Distance getDistanciaPerielio() {
        return distanciaPerielio;
    }

    @Override
    public Optional<Distance> getDistanciaDeReferencia() {
        return Optional.of(distanciaPerielio);
    }

    public Double getDiametroNucleoKm() {
        return diametroNucleoKm;
    }

    @Override
    public RiskLevel avaliarRisco() {
        boolean nucleoGrande = diametroNucleoKm != null && diametroNucleoKm >= LIMIAR_NUCLEO_GRANDE_KM;
        boolean nucleoMedio = diametroNucleoKm != null && diametroNucleoKm >= LIMIAR_NUCLEO_MEDIO_KM;

        if (distanciaPerielio.isMenorQue(PERIELIO_CRITICO) || (nucleoGrande && distanciaPerielio.isMenorQue(PERIELIO_ALTO))) {
            return RiskLevel.CRITICO;
        }
        if (distanciaPerielio.isMenorQue(PERIELIO_ALTO) || (nucleoMedio && distanciaPerielio.isMenorQue(PERIELIO_MEDIO))) {
            return RiskLevel.ALTO;
        }
        if (distanciaPerielio.isMenorQue(PERIELIO_MEDIO)) {
            return RiskLevel.MEDIO;
        }
        return RiskLevel.BAIXO;
    }

    @Override
    public Position getPosicaoAtual() {
        Instant now = Instant.now();
        long hash = Math.abs((long) id.hashCode());
        double declinacao = (hash % 160) - 80.0; // -80 a +80 graus
        double ascensaoReta = (hash % 360) - 180.0; // -180 a +180 graus

        return new Position(declinacao, ascensaoReta, distanciaPerielio, now);
    }

    @Override
    public String toString() {
        return String.format("Cometa '%s' (ID: %s) - Periélio: %s - Núcleo: %s km - Risco: %s",
                nome, id, distanciaPerielio,
                diametroNucleoKm != null ? String.format("%.1f", diametroNucleoKm) : "N/D",
                avaliarRisco());
    }
}
