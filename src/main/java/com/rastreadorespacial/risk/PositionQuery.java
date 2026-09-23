package com.rastreadorespacial.risk;

import com.rastreadorespacial.domain.Locatable;
import com.rastreadorespacial.domain.Position;
import com.rastreadorespacial.domain.SpaceObject;

import java.util.List;
import java.util.Objects;

/**
 * Utilitário responsável pela consulta polimórfica de posições de objetos espaciais
 * que possuem a capacidade opcional {@link Locatable}.
 */
public final class PositionQuery {

    private PositionQuery() {
        // Classe utilitária estática
    }

    /**
     * Extrai a lista de posições atuais a partir de uma coleção mista de objetos espaciais,
     * filtrando polimorficamente apenas aqueles que implementam {@link Locatable}.
     *
     * @param objetos lista de objetos espaciais
     * @return lista com as posições atuais dos objetos localizáveis
     */
    public static List<Position> posicoesAtuais(List<SpaceObject> objetos) {
        if (objetos == null || objetos.isEmpty()) {
            return List.of();
        }

        return objetos.stream()
                .filter(Objects::nonNull)
                .filter(Locatable.class::isInstance)
                .map(Locatable.class::cast)
                .map(Locatable::getPosicaoAtual)
                .toList();
    }
}

