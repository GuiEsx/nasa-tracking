package com.rastreadorespacial.domain;

import com.rastreadorespacial.risk.RiskRanking;

import java.time.LocalDate;
import java.util.*;

/**
 * Representa um retrato (snapshot) imutável dos objetos espaciais rastreados em uma data específica,
 * fornecendo operações de agregação e estatísticas analíticas.
 */
public final class DailySnapshot {

    private final LocalDate data;
    private final List<SpaceObject> objetos;

    public DailySnapshot(LocalDate data, List<SpaceObject> objetos) {
        this.data = Objects.requireNonNull(data, "A data do snapshot não pode ser nula.");
        this.objetos = objetos != null ? List.copyOf(objetos) : List.of();
    }

    public LocalDate getData() {
        return data;
    }

    public List<SpaceObject> getObjetos() {
        return objetos;
    }

    /**
     * Retorna a quantidade total de objetos presentes neste snapshot.
     *
     * @return quantidade total de objetos
     */
    public int quantidadeDeObjetos() {
        return objetos.size();
    }

    /**
     * Retorna o objeto de maior nível de risco do snapshot (conforme ordenação de {@link RiskRanking}).
     * Em caso de empate no nível de risco, o critério de desempate alfabético de RiskRanking é aplicado.
     *
     * @return {@link Optional} com o objeto mais perigoso, ou vazio se não houver objetos
     */
    public Optional<SpaceObject> maisPerigoso() {
        if (objetos.isEmpty()) {
            return Optional.empty();
        }
        List<SpaceObject> ordenados = RiskRanking.ordenarPorPerigo(objetos);
        return Optional.of(ordenados.get(0));
    }

    /**
     * Retorna o objeto espacial mais próximo da Terra neste snapshot, considerando exclusivamente
     * aqueles que possuem uma distância de referência válida (via {@link HasDistance}).
     *
     * @return {@link Optional} com o objeto mais próximo, ou vazio se nenhum possuir distância aplicável
     */
    public Optional<SpaceObject> maisProximo() {
        return objetos.stream()
                .filter(HasDistance.class::isInstance)
                .map(HasDistance.class::cast)
                .filter(hd -> hd.getDistanciaDeReferencia().isPresent())
                .min(Comparator
                        .<HasDistance, Distance>comparing(hd -> hd.getDistanciaDeReferencia().get())
                        .thenComparing(hd -> ((SpaceObject) hd).getNome(), String.CASE_INSENSITIVE_ORDER)
                )
                .map(SpaceObject.class::cast);
    }

    /**
     * Retorna a contagem de objetos agrupados por cada categoria de {@link RiskLevel}.
     *
     * @return mapa com as contagens por nível de risco
     */
    public Map<RiskLevel, Long> contagemPorNivelDeRisco() {
        Map<RiskLevel, Long> contagens = new EnumMap<>(RiskLevel.class);
        for (RiskLevel level : RiskLevel.values()) {
            contagens.put(level, 0L);
        }

        Map<RiskLevel, List<SpaceObject>> agrupados = RiskRanking.agruparPorNivel(objetos);
        for (Map.Entry<RiskLevel, List<SpaceObject>> entry : agrupados.entrySet()) {
            contagens.put(entry.getKey(), (long) entry.getValue().size());
        }

        return Collections.unmodifiableMap(contagens);
    }
}

