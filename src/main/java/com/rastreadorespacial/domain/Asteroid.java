package com.rastreadorespacial.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa um asteroide rastreado (como os fornecidos pela API NASA NeoWs).
 *
 * <p><strong>Identificador Canônico:</strong> O {@link #getIdCanonico()} corresponde ao
 * {@code neo_reference_id} (ou SPK-ID) fornecido pelo banco de dados da NASA (JPL Small-Body Database),
 * sendo universal e imutável para o mesmo asteroide através de diferentes datas de feed.</p>
 *
 * <p><strong>Critérios de Avaliação de Risco:</strong></p>
 * <ul>
 *   <li><strong>CRITICO:</strong> Classificado como potencialmente perigoso pela NASA (PHA) e com distância de passagem inferior a {@value #LIMIAR_CRITICO_LD} LD (distância lunar).</li>
 *   <li><strong>ALTO:</strong> Classificado como potencialmente perigoso pela NASA (PHA) OU com passagem a menos de {@value #LIMIAR_ALTO_LD} LD.</li>
 *   <li><strong>MEDIO:</strong> Passagem próxima inferior a {@value #LIMIAR_MEDIO_LD} LD.</li>
 *   <li><strong>BAIXO:</strong> Passagem além de {@value #LIMIAR_MEDIO_LD} LD e sem alerta da NASA.</li>
 * </ul>
 *
 * <p><strong>Efeméride Aproximada:</strong>
 * A posição retornada por {@link #getPosicaoAtual()} representa uma aproximação de coordenadas celestes
 * para fins de visualização gráfica, com a altitude definida como a distância de passagem em relação à Terra.</p>
 */
public final class Asteroid implements SpaceObject, Locatable, HasDistance {

    // Limiares encapsulados internamente (não expostos publicamente)
    private static final double LIMIAR_CRITICO_LD = 1.0;
    private static final double LIMIAR_ALTO_LD = 5.0;
    private static final double LIMIAR_MEDIO_LD = 20.0;

    private static final Distance DISTANCIA_CRITICA = Distance.ofLunarDistances(LIMIAR_CRITICO_LD);
    private static final Distance DISTANCIA_ALTA = Distance.ofLunarDistances(LIMIAR_ALTO_LD);
    private static final Distance DISTANCIA_MEDIA = Distance.ofLunarDistances(LIMIAR_MEDIO_LD);

    private final String id;
    private final String nome;
    private final Distance distanciaDePassagem;
    private final boolean flagPericulosidadeNasa;

    public Asteroid(String id, String nome, Distance distanciaDePassagem, boolean flagPericulosidadeNasa) {
        this.id = Objects.requireNonNull(id, "O ID do asteroide não pode ser nulo.");
        this.nome = Objects.requireNonNull(nome, "O nome do asteroide não pode ser nulo.");
        this.distanciaDePassagem = Objects.requireNonNull(distanciaDePassagem, "A distância de passagem não pode ser nula.");
        this.flagPericulosidadeNasa = flagPericulosidadeNasa;
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

    public Distance getDistanciaDePassagem() {
        return distanciaDePassagem;
    }

    @Override
    public Optional<Distance> getDistanciaDeReferencia() {
        return Optional.of(distanciaDePassagem);
    }

    public boolean isFlagPericulosidadeNasa() {
        return flagPericulosidadeNasa;
    }

    @Override
    public RiskLevel avaliarRisco() {
        if (flagPericulosidadeNasa && distanciaDePassagem.isMenorQue(DISTANCIA_CRITICA)) {
            return RiskLevel.CRITICO;
        }
        if (flagPericulosidadeNasa || distanciaDePassagem.isMenorQue(DISTANCIA_ALTA)) {
            return RiskLevel.ALTO;
        }
        if (distanciaDePassagem.isMenorQue(DISTANCIA_MEDIA)) {
            return RiskLevel.MEDIO;
        }
        return RiskLevel.BAIXO;
    }

    @Override
    public Position getPosicaoAtual() {
        Instant now = Instant.now();
        long hash = Math.abs((long) id.hashCode());
        double declinacao = (hash % 120) - 60.0; // -60 a +60 graus
        double ascensaoReta = (hash % 360) - 180.0; // -180 a +180 graus

        return new Position(declinacao, ascensaoReta, distanciaDePassagem, now);
    }

    @Override
    public String toString() {
        return String.format("Asteroide '%s' (ID: %s) - Distância: %s - PHA NASA: %s - Risco: %s",
                nome, id, distanciaDePassagem, flagPericulosidadeNasa, avaliarRisco());
    }
}
