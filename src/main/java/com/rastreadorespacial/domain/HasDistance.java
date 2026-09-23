package com.rastreadorespacial.domain;

import java.util.Optional;

/**
 * Interface de capacidade para objetos espaciais que possuem uma distância física/astronômica
 * de referência comparável em relação à Terra (ex: distância de passagem ou periélio).
 */
public interface HasDistance {

    /**
     * Retorna a distância de referência do objeto espacial em relação à Terra.
     *
     * @return {@link Optional} com a {@link Distance} do objeto, ou {@link Optional#empty()} se não aplicável
     */
    Optional<Distance> getDistanciaDeReferencia();
}

