package com.rastreadorespacial.domain;

/**
 * Interface comum para todos os objetos espaciais rastreáveis pelo sistema.
 */
public interface SpaceObject {

    /**
     * Retorna o identificador do objeto espacial.
     *
     * @return identificador
     */
    String getId();

    /**
     * Retorna o identificador canônico e estável do objeto espacial no mundo real
     * (ex: neo_reference_id da NASA, NORAD Catalog Number, designação IAU).
     * <p>Este identificador independe da data do feed ou da consulta.</p>
     *
     * @return identificador canônico
     */
    String getIdCanonico();

    /**
     * Retorna o nome de designação do objeto espacial.
     *
     * @return nome do objeto
     */
    String getNome();

    /**
     * Avalia e retorna o nível de risco oferecido pelo objeto espacial.
     * <p>A lógica de cálculo e os limiares numéricos são estritamente encapsulados
     * dentro de cada implementação concreta de objeto espacial.</p>
     *
     * @return nível de risco como enum {@link RiskLevel}
     */
    RiskLevel avaliarRisco();
}
