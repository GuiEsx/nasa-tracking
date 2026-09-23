package com.rastreadorespacial.domain;

import java.util.Objects;

/**
 * Representa os níveis fechados de risco para objetos espaciais rastreados.
 *
 * <p><strong>Ordem de Severidade:</strong>
 * A ordem de declaração dos valores do enum define sua ordem natural de comparação ({@link Comparable}):
 * {@code BAIXO < MEDIO < ALTO < CRITICO}.
 * Essa ordem é estritamente crescente em termos de severidade e potencial de perigo.</p>
 */
public enum RiskLevel {

    BAIXO("Baixo risco — monitoramento de rotina"),
    MEDIO("Médio risco — acompanhamento periódico"),
    ALTO("Alto risco — aproximação significativa"),
    CRITICO("Crítico — atenção imediata requerida");

    private final String descricao;

    RiskLevel(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna uma descrição curta e legível do nível de risco para exibição em painéis e relatórios.
     *
     * @return descrição amigável
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Reconstrói de forma segura uma instância de {@link RiskLevel} a partir de uma String.
     *
     * @param valor texto com o nome do nível de risco (ex: "BAIXO", "MEDIO", "ALTO", "CRITICO")
     * @return a constante enum correspondente
     * @throws IllegalArgumentException caso o valor seja nulo, vazio ou não corresponda exatamente a nenhum dos quatro níveis
     */
    public static RiskLevel fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O valor de nível de risco não pode ser nulo ou vazio.");
        }

        String valorNormalizado = valor.trim().toUpperCase();
        for (RiskLevel level : values()) {
            if (level.name().equals(valorNormalizado)) {
                return level;
            }
        }

        throw new IllegalArgumentException(String.format(
                "Nível de risco inválido: '%s'. Valores aceitos são: BAIXO, MEDIO, ALTO, CRITICO.",
                valor
        ));
    }
}

