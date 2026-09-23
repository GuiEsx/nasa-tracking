package com.rastreadorespacial.risk;

import com.rastreadorespacial.domain.RiskLevel;
import com.rastreadorespacial.domain.SpaceObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utilitário polimórfico responsável pela classificação, ordenação por severidade
 * e agrupamento de objetos espaciais com base exclusivamente no contrato {@link SpaceObject}.
 *
 * <p><strong>Princípio Aberto/Fechado (OCP):</strong> Esta classe não possui nenhuma dependência ou
 * ramificação (como {@code instanceof} ou {@code switch}) sobre implementações concretas (como
 * {@code Asteroid}, {@code Comet} ou {@code Satellite}), permitindo que novos tipos de objetos espaciais
 * sejam adicionados sem qualquer necessidade de alteração neste código.</p>
 */
public final class RiskRanking {

    private static final Comparator<SpaceObject> COMPARADOR_PERIGO = Comparator
            .comparing(SpaceObject::avaliarRisco, Comparator.reverseOrder())
            .thenComparing(SpaceObject::getNome, String.CASE_INSENSITIVE_ORDER);

    private RiskRanking() {
        // Classe utilitária estática
    }

    /**
     * Ordena uma coleção de objetos espaciais do mais perigoso ({@link RiskLevel#CRITICO}) para o
     * menos perigoso ({@link RiskLevel#BAIXO}).
     *
     * <p><strong>Critério de Desempate:</strong> Em caso de empate no {@link RiskLevel}, os objetos são
     * ordenados alfabeticamente por seu nome ({@link SpaceObject#getNome()}) de forma insensível a maiúsculas/minúsculas.</p>
     *
     * @param objetos lista contendo objetos espaciais a serem ordenados
     * @return nova lista imutável ordenada por nível de perigo decrescente
     */
    public static List<SpaceObject> ordenarPorPerigo(List<SpaceObject> objetos) {
        if (objetos == null || objetos.isEmpty()) {
            return List.of();
        }

        return objetos.stream()
                .filter(Objects::nonNull)
                .sorted(COMPARADOR_PERIGO)
                .toList();
    }

    /**
     * Agrupa os objetos espaciais por seu respectivo nível de risco ({@link RiskLevel}).
     *
     * @param objetos lista contendo objetos espaciais
     * @return mapa associando cada {@link RiskLevel} à lista de objetos classificados naquele nível
     */
    public static Map<RiskLevel, List<SpaceObject>> agruparPorNivel(List<SpaceObject> objetos) {
        if (objetos == null || objetos.isEmpty()) {
            return new EnumMap<>(RiskLevel.class);
        }

        return objetos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        SpaceObject::avaliarRisco,
                        () -> new EnumMap<>(RiskLevel.class),
                        Collectors.toList()
                ));
    }
}

