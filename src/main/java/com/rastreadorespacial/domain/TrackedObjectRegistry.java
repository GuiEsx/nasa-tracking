package com.rastreadorespacial.domain;

import java.util.*;

/**
 * Registro central de objetos espaciais rastreados, responsável por manter uma coleção unificada
 * e livre de duplicatas baseando-se no identificador canônico estável de cada objeto.
 *
 * <p><strong>Regra de Deduplicação:</strong>
 * A deduplicação é realizada através da chave {@link SpaceObject#getIdCanonico()}.
 * Ao adicionar um objeto cujo identificador canônico já esteja presente no registro, a versão mais recente
 * <em>sobrescreve</em> a versão anterior existente. Essa estratégia garante que parâmetros orbitais, distâncias
 * e medições mais recentes de feeds subsequentes sejam sempre preservados sem gerar duplicidade de contagem.</p>
 */
public class TrackedObjectRegistry {

    private final Map<String, SpaceObject> objetosPorIdCanonico;

    public TrackedObjectRegistry() {
        this.objetosPorIdCanonico = new LinkedHashMap<>();
    }

    /**
     * Adiciona ou atualiza um objeto espacial no registro.
     * Caso o objeto já exista (mesmo {@link SpaceObject#getIdCanonico()}), seus dados são atualizados.
     *
     * @param objeto objeto espacial a ser registrado
     */
    public void adicionar(SpaceObject objeto) {
        if (objeto == null || objeto.getIdCanonico() == null || objeto.getIdCanonico().isBlank()) {
            return;
        }
        objetosPorIdCanonico.put(objeto.getIdCanonico(), objeto);
    }

    /**
     * Adiciona ou atualiza uma coleção de objetos espaciais no registro.
     *
     * @param objetos coleção de objetos a serem adicionados
     */
    public void adicionarTodos(Collection<? extends SpaceObject> objetos) {
        if (objetos == null || objetos.isEmpty()) {
            return;
        }
        for (SpaceObject obj : objetos) {
            adicionar(obj);
        }
    }

    /**
     * Retorna a lista contendo todos os objetos espaciais únicos atualmente rastreados.
     *
     * @return lista imutável com os objetos rastreados
     */
    public List<SpaceObject> listarTodos() {
        return List.copyOf(objetosPorIdCanonico.values());
    }

    /**
     * Busca um objeto espacial pelo seu identificador canônico.
     *
     * @param idCanonico identificador canônico
     * @return Optional contendo o objeto se presente, ou Optional.empty()
     */
    public Optional<SpaceObject> buscarPorIdCanonico(String idCanonico) {
        if (idCanonico == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(objetosPorIdCanonico.get(idCanonico));
    }

    /**
     * Retorna o total de objetos espaciais únicos rastreados.
     *
     * @return quantidade de objetos únicos
     */
    public int quantidade() {
        return objetosPorIdCanonico.size();
    }

    /**
     * Remove todos os objetos do registro.
     */
    public void limpar() {
        objetosPorIdCanonico.clear();
    }
}

