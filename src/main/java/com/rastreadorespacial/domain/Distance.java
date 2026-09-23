package com.rastreadorespacial.domain;

import java.util.Locale;
import java.util.Objects;

/**
 * Value Object imutável que representa uma grandeza de distância astronômica ou física.
 *
 * <p><strong>Garantia de segurança de tipos:</strong>
 * A classe armazena o valor internamente em uma unidade canônica (quilômetros) e não expõe nenhum
 * getter genérico ou ambíguo. A única forma de ler um valor numérico é através dos métodos explícitos
 * {@link #emQuilometros()}, {@link #emMilhas()} e {@link #emDistanciasLunares()}, eliminando qualquer
 * possibilidade de interpretar milhas ou distâncias lunares como quilômetros por engano.</p>
 */
public final class Distance implements Comparable<Distance> {

    private static final double TOLERANCE_KM = 1e-6;
    private final double valorEmKm;

    private Distance(double valorEmKm) {
        if (Double.isNaN(valorEmKm) || Double.isInfinite(valorEmKm)) {
            throw new IllegalArgumentException("O valor de distância deve ser um número finito válido.");
        }
        if (valorEmKm < 0) {
            throw new IllegalArgumentException("O valor de distância não pode ser negativo.");
        }
        this.valorEmKm = valorEmKm;
    }

    /**
     * Cria uma instância de {@link Distance} a partir de um valor e de sua respectiva unidade de origem.
     *
     * @param valor valor numérico
     * @param unidadeDeOrigem unidade em que o valor foi medido
     * @return nova instância imutável de Distance
     */
    public static Distance of(double valor, DistanceUnit unidadeDeOrigem) {
        Objects.requireNonNull(unidadeDeOrigem, "A unidade de origem não pode ser nula.");
        return new Distance(unidadeDeOrigem.toKilometers(valor));
    }

    /**
     * Factory method de conveniência para criar uma distância em quilômetros.
     *
     * @param km valor em quilômetros
     * @return nova instância de Distance
     */
    public static Distance ofKilometers(double km) {
        return of(km, DistanceUnit.KILOMETERS);
    }

    /**
     * Factory method de conveniência para criar uma distância em milhas.
     *
     * @param miles valor em milhas
     * @return nova instância de Distance
     */
    public static Distance ofMiles(double miles) {
        return of(miles, DistanceUnit.MILES);
    }

    /**
     * Factory method de conveniência para criar uma distância em distâncias lunares (LD).
     *
     * @param ld valor em distâncias lunares
     * @return nova instância de Distance
     */
    public static Distance ofLunarDistances(double ld) {
        return of(ld, DistanceUnit.LUNAR_DISTANCES);
    }

    /**
     * Retorna o valor da distância convertido para quilômetros (km).
     *
     * @return valor em quilômetros
     */
    public double emQuilometros() {
        return DistanceUnit.KILOMETERS.fromKilometers(this.valorEmKm);
    }

    /**
     * Retorna o valor da distância convertido para milhas (mi).
     *
     * @return valor em milhas
     */
    public double emMilhas() {
        return DistanceUnit.MILES.fromKilometers(this.valorEmKm);
    }

    /**
     * Retorna o valor da distância convertido para distâncias lunares (LD).
     *
     * @return valor em distâncias lunares
     */
    public double emDistanciasLunares() {
        return DistanceUnit.LUNAR_DISTANCES.fromKilometers(this.valorEmKm);
    }

    /**
     * Compara se esta distância é estritamente menor que a outra fornecida.
     *
     * @param outra distância para comparação
     * @return true se esta distância for menor que a outra
     */
    public boolean isMenorQue(Distance outra) {
        Objects.requireNonNull(outra, "A distância de comparação não pode ser nula.");
        return (outra.valorEmKm - this.valorEmKm) > TOLERANCE_KM;
    }

    /**
     * Compara se esta distância é estritamente maior que a outra fornecida.
     *
     * @param outra distância para comparação
     * @return true se esta distância for maior que a outra
     */
    public boolean isMaiorQue(Distance outra) {
        Objects.requireNonNull(outra, "A distância de comparação não pode ser nula.");
        return (this.valorEmKm - outra.valorEmKm) > TOLERANCE_KM;
    }

    @Override
    public int compareTo(Distance outra) {
        Objects.requireNonNull(outra, "A distância de comparação não pode ser nula.");
        if (Math.abs(this.valorEmKm - outra.valorEmKm) <= TOLERANCE_KM) {
            return 0;
        }
        return Double.compare(this.valorEmKm, outra.valorEmKm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Distance distance = (Distance) o;
        return Math.abs(this.valorEmKm - distance.valorEmKm) <= TOLERANCE_KM;
    }

    @Override
    public int hashCode() {
        long rounded = Math.round(this.valorEmKm / TOLERANCE_KM);
        return Objects.hash(rounded);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%.2f km", this.valorEmKm);
    }
}

