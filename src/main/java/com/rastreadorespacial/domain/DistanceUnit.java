package com.rastreadorespacial.domain;

/**
 * Representa as unidades de distância suportadas pelo sistema e seus respectivos fatores de conversão
 * para a unidade base padrão interna (Quilômetros).
 *
 * <p><strong>Referências astronômicas e de conversão:</strong></p>
 * <ul>
 *   <li>{@link #KILOMETERS}: Unidade base (fator 1.0).</li>
 *   <li>{@link #MILES}: Milha internacional padrão definida como exatamente 1.609344 km (acordo internacional de jardas e libras de 1959).</li>
 *   <li>{@link #LUNAR_DISTANCES}: 1 Distância Lunar (LD) é aproximadamente 384.400 km, correspondente ao semi-eixo maior da órbita lunar média (referência padrão NASA / IAU).</li>
 * </ul>
 */
public enum DistanceUnit {

    KILOMETERS(1.0),
    MILES(1.609344),
    LUNAR_DISTANCES(384_400.0);

    public static DistanceUnit fromChoice(int escolha) {
        return switch (escolha) {
            case 2 -> MILES;
            case 3 -> LUNAR_DISTANCES;
            default -> KILOMETERS;
        };
    }

    private final double fatorParaKm;

    DistanceUnit(double fatorParaKm) {
        this.fatorParaKm = fatorParaKm;
    }

    /**
     * Converte um valor desta unidade para quilômetros.
     *
     * @param valor valor na unidade atual
     * @return valor correspondente em quilômetros
     */
    public double toKilometers(double valor) {
        return valor * this.fatorParaKm;
    }

    /**
     * Converte um valor expresso em quilômetros para esta unidade.
     *
     * @param valorEmKm valor em quilômetros
     * @return valor correspondente nesta unidade
     */
    public double fromKilometers(double valorEmKm) {
        return valorEmKm / this.fatorParaKm;
    }

    /**
     * Retorna o fator multiplicador para converter esta unidade para quilômetros.
     *
     * @return fator de conversão para quilômetros
     */
    public double getFatorParaKm() {
        return fatorParaKm;
    }
}

