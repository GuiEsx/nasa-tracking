package com.rastreadorespacial.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa um satélite artificial ou detrito espacial rastreado na órbita terrestre (como os dados TLE do Celestrak).
 *
 * <p><strong>Identificador Canônico:</strong> O {@link #getIdCanonico()} corresponde ao
 * NORAD Catalog Number (Satellite Catalog Number / SCC#) mantido pelo US Space Command e distribuído pelo Celestrak,
 * sendo o identificador padronizado e imutável para rastreamento de satélites em órbita.</p>
 *
 * <p><strong>Critérios de Avaliação de Risco:</strong></p>
 * <ul>
 *   <li><strong>CRITICO:</strong> Taxa de decaimento orbital diário >= {@value #LIMIAR_DECAIMENTO_CRITICO} (reentrada atmosférica descontrolada iminente).</li>
 *   <li><strong>ALTO:</strong> Taxa de decaimento orbital diário >= {@value #LIMIAR_DECAIMENTO_ALTO} (perda de altitude acelerada).</li>
 *   <li><strong>MEDIO:</strong> Taxa de decaimento orbital diário >= {@value #LIMIAR_DECAIMENTO_MEDIO} (degradação orbital perceptível).</li>
 *   <li><strong>BAIXO:</strong> Taxa de decaimento < {@value #LIMIAR_DECAIMENTO_MEDIO}, nula ou desconhecida (órbita estável/controlada).</li>
 * </ul>
 *
 * <p><strong>Cálculo de Posição Orbital:</strong>
 * A implementação de {@link #getPosicaoAtual()} utiliza uma aproximação cinemática simplificada baseada nos
 * elementos orbitais médios e no timestamp atual (não substitui a propagação orbital numérica complexa SGP4/SDP4).</p>
 */
public final class Satellite implements SpaceObject, Locatable, HasDistance {

    // Limiares encapsulados internamente (não expostos publicamente)
    private static final double LIMIAR_DECAIMENTO_CRITICO = 0.01;
    private static final double LIMIAR_DECAIMENTO_ALTO = 0.001;
    private static final double LIMIAR_DECAIMENTO_MEDIO = 0.0001;

    private final String id;
    private final String nome;
    private final Double decaimentoOrbitalDiario; // null se desconhecido ou estável
    private final Double inclinacaoGraus;
    private final Double excentricidade;
    private final Double movimentoMedioRevDia;

    public Satellite(String id, String nome, Double decaimentoOrbitalDiario) {
        this(id, nome, decaimentoOrbitalDiario, 51.64, 0.0005, 15.5); // Parâmetros padrão estilo ISS / LEO
    }

    public Satellite(String id, String nome, Double decaimentoOrbitalDiario,
                     Double inclinacaoGraus, Double excentricidade, Double movimentoMedioRevDia) {
        this.id = Objects.requireNonNull(id, "O ID do satélite não pode ser nulo.");
        this.nome = Objects.requireNonNull(nome, "O nome do satélite não pode ser nulo.");
        this.decaimentoOrbitalDiario = decaimentoOrbitalDiario;
        this.inclinacaoGraus = inclinacaoGraus != null ? inclinacaoGraus : 51.64;
        this.excentricidade = excentricidade != null ? excentricidade : 0.0005;
        this.movimentoMedioRevDia = movimentoMedioRevDia != null ? movimentoMedioRevDia : 15.5;
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

    public Double getDecaimentoOrbitalDiario() {
        return decaimentoOrbitalDiario;
    }

    @Override
    public Optional<Distance> getDistanciaDeReferencia() {
        // Para satélites orbitais em LEO, a distância de aproximação à Terra não é uma aproximação astronômica
        return Optional.empty();
    }

    public Double getInclinacaoGraus() {
        return inclinacaoGraus;
    }

    public Double getExcentricidade() {
        return excentricidade;
    }

    public Double getMovimentoMedioRevDia() {
        return movimentoMedioRevDia;
    }

    @Override
    public RiskLevel avaliarRisco() {
        if (decaimentoOrbitalDiario == null) {
            return RiskLevel.BAIXO;
        }
        if (decaimentoOrbitalDiario >= LIMIAR_DECAIMENTO_CRITICO) {
            return RiskLevel.CRITICO;
        }
        if (decaimentoOrbitalDiario >= LIMIAR_DECAIMENTO_ALTO) {
            return RiskLevel.ALTO;
        }
        if (decaimentoOrbitalDiario >= LIMIAR_DECAIMENTO_MEDIO) {
            return RiskLevel.MEDIO;
        }
        return RiskLevel.BAIXO;
    }

    @Override
    public Position getPosicaoAtual() {
        Instant now = Instant.now();
        double epochSeconds = now.getEpochSecond() % 86400; // segundos do dia

        // Estimativa aproximada de latitude orbital baseada na inclinação
        double lat = Math.sin(epochSeconds * (movimentoMedioRevDia * 2 * Math.PI / 86400.0)) * inclinacaoGraus;
        lat = Math.max(-90.0, Math.min(90.0, lat));

        // Estimativa aproximada de longitude com rotação terrestre
        double lon = ((epochSeconds / 240.0) % 360.0) - 180.0;
        lon = Math.max(-180.0, Math.min(180.0, lon));

        // Altitude orbital aproximada típica (LEO ~420 km)
        double altKm = 420.0;
        if (movimentoMedioRevDia != null && movimentoMedioRevDia > 0) {
            altKm = Math.max(150.0, Math.min(36000.0, 6371.0 * (Math.pow(15.5 / movimentoMedioRevDia, 2.0 / 3.0) - 1.0) + 420.0));
        }

        return new Position(lat, lon, Distance.ofKilometers(altKm), now);
    }

    @Override
    public String toString() {
        return String.format("Satélite '%s' (ID: %s) - Decaimento Diário: %s - Risco: %s",
                nome, id,
                decaimentoOrbitalDiario != null ? decaimentoOrbitalDiario.toString() : "N/D",
                avaliarRisco());
    }
}
