package model;

import java.time.LocalDateTime;

public class PosicaoAtual {
    private final String descricao;
    private final Distancia distanciaAtual;
    private final LocalDateTime momentoReferencia;
    private final LocalDateTime momentoConsulta;

    public PosicaoAtual(String descricao, Distancia distanciaAtual, LocalDateTime momentoReferencia, LocalDateTime momentoConsulta) {
        this.descricao = descricao;
        this.distanciaAtual = distanciaAtual;
        this.momentoReferencia = momentoReferencia;
        this.momentoConsulta = momentoConsulta;
    }

    // Sobrecarga de compatibilidade para conversão a partir de km (RF1)
    public PosicaoAtual(String descricao, double distanciaAtualKm, LocalDateTime momentoReferencia, LocalDateTime momentoConsulta) {
        this(descricao, Distancia.deKilometros(distanciaAtualKm), momentoReferencia, momentoConsulta);
    }

    public String getDescricao() { 
        return descricao; 
    }

    // RF1: Retorna o Value Object seguro
    public Distancia getDistancia() {
        return distanciaAtual;
    }

    public double getDistanciaAtualKm() { 
        return distanciaAtual != null ? distanciaAtual.emKilometros() : 0.0; 
    }

    public LocalDateTime getMomentoReferencia() { 
        return momentoReferencia; 
    }

    public LocalDateTime getMomentoConsulta() { 
        return momentoConsulta; 
    }
}
