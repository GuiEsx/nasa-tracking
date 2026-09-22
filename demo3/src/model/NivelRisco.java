package model;

public enum NivelRisco {
    BAIXO(1),
    MEDIO(2),
    ALTO(3),
    CRITICO(4);

    private final int prioridade;

    NivelRisco(int prioridade) {
        this.prioridade = prioridade;
    }

    public int getPrioridade() {
        return prioridade;
    }

    // RF3 & RF5: Comparação pura entre riscos categóricos
    public boolean isMaisPerigosoQue(NivelRisco outro) {
        if (outro == null) return true;
        return this.prioridade > outro.prioridade;
    }
}
