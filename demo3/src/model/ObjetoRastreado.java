package model;

import java.util.Objects;

public class ObjetoRastreado {
    private final String nome;
    private final String tipo;
    private final NivelRisco risco;

    public ObjetoRastreado(String nome, String tipo, NivelRisco risco) {
        this.nome = (nome != null) ? nome.trim() : "";
        this.tipo = (tipo != null) ? tipo.trim() : "";
        this.risco = risco;
    }

    public String getNome() { 
        return nome; 
    }

    public String getTipo() { 
        return tipo; 
    }

    public NivelRisco getRisco() { 
        return risco; 
    }

    // RF9 [P]: Reconhecimento de duplicatas em sessões de rastreamento
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjetoRastreado that = (ObjetoRastreado) o;
        return Objects.equals(this.tipo.toUpperCase(), that.tipo.toUpperCase()) &&
               Objects.equals(this.nome.toUpperCase(), that.nome.toUpperCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tipo.toUpperCase(), this.nome.toUpperCase());
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Risco: %s", nome, tipo, risco);
    }
}
