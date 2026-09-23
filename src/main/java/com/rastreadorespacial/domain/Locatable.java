package com.rastreadorespacial.domain;

/**
 * Interface de capacidade opcional para objetos espaciais que possuem mecanismo
 * de determinação ou estimativa de posição geográfica/espacial no instante atual.
 */
public interface Locatable {

    /**
     * Calcula e retorna a posição atual do objeto espacial.
     *
     * @return {@link Position} contendo as coordenadas espaciais, altitude e timestamp
     */
    Position getPosicaoAtual();
}

