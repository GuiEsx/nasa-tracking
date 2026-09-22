package model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;

public abstract class ObjetoEspacial implements Posicionavel {
    // RF5 [P]: Comparador polimórfico desacoplado - ordena por criticidade de risco (CRÍTICO -> BAIXO)
    // com desempate determinístico pela menor distância
    public static final Comparator<ObjetoEspacial> POR_RISCO_DECRESCENTE = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;
        int comp = o2.avaliarRisco().compareTo(o1.avaliarRisco());
        if (comp != 0) {
            return comp;
        }
        return Double.compare(o1.getDistancia().emKilometros(), o2.getDistancia().emKilometros());
    };
    protected String id;
    protected String nome;
    protected Distancia distanciaAtual;
    protected String tipo;
    protected LocalDateTime ultimoAvistamento;

    public ObjetoEspacial(String nome, Distancia distanciaAtual, String tipo) {
        this(nome, nome, distanciaAtual, tipo, LocalDateTime.now());
    }

    public ObjetoEspacial(String nome, Distancia distanciaAtual, String tipo, LocalDateTime ultimoAvistamento) {
        this(nome, nome, distanciaAtual, tipo, ultimoAvistamento);
    }

    public ObjetoEspacial(String id, String nome, Distancia distanciaAtual, String tipo, LocalDateTime ultimoAvistamento) {
        this.id = (id != null && !id.trim().isEmpty()) ? id.trim() : (nome != null ? nome.trim() : "");
        this.nome = nome;
        this.distanciaAtual = distanciaAtual;
        this.tipo = tipo;
        this.ultimoAvistamento = ultimoAvistamento;
    }

    public String getId() {
        return this.id;
    }

    public String getNome() { 
        return this.nome; 
    }
    
    public Distancia getDistancia() { 
        return this.distanciaAtual; 
    }
    
    public String getTipo() { 
        return this.tipo; 
    }

    public LocalDateTime getUltimoAvistamento() {
        return this.ultimoAvistamento;
    }

    public abstract NivelRisco avaliarRisco();

    public abstract double calcularTaxaMovimentoPorMinuto();

    public abstract String obterDescricaoOrbital();

    // RF6 [IF]: Implementação polimórfica da interface comum Posicionavel
    @Override
    public PosicaoAtual consultarPosicaoAgora() {
        return consultarPosicao(LocalDateTime.now());
    }

    @Override
    public PosicaoAtual consultarPosicao(LocalDateTime momentoConsulta) {
        LocalDateTime referencia = (this.ultimoAvistamento != null) ? this.ultimoAvistamento : momentoConsulta;
        double distanciaBaseKm = this.distanciaAtual.emKilometros();
        double minutos = Math.max(0, ChronoUnit.MINUTES.between(referencia, momentoConsulta));
        double deslocamentoKm = minutos * calcularTaxaMovimentoPorMinuto();
        double distanciaAtualKm = Math.max(0.0, distanciaBaseKm - deslocamentoKm);
        Distancia dist = Distancia.deKilometros(distanciaAtualKm);
        return new PosicaoAtual(obterDescricaoOrbital(), dist, referencia, momentoConsulta);
    }

    // RF9 [P]: Reconhecimento estrito de duplicatas (mesmo tipo e identificador único)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ObjetoEspacial)) return false;
        ObjetoEspacial that = (ObjetoEspacial) o;
        return Objects.equals(this.tipo, that.tipo) &&
               Objects.equals(this.id.toUpperCase(), that.id.toUpperCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tipo, this.id.toUpperCase());
    }

    @Override
    public String toString() {
        return String.format("%s [ID: %s, Nome: %s, Distância: %.2f km]", tipo, id, nome, distanciaAtual.emKilometros());
    }
}
