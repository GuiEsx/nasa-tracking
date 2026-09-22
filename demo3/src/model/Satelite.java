package model;

import java.time.LocalDateTime;

public class Satelite extends ObjetoEspacial {
    private double decaimentoOrbitalMetrosPorDia;

    public Satelite(String nome, Distancia altitude, double decaimento) {
        this(nome, nome, altitude, decaimento, LocalDateTime.now());
    }

    public Satelite(String nome, Distancia altitude, double decaimento, LocalDateTime ultimoAvistamento) {
        this(nome, nome, altitude, decaimento, ultimoAvistamento);
    }

    public Satelite(String id, String nome, Distancia altitude, double decaimento, LocalDateTime ultimoAvistamento) {
        super(id, nome, altitude, "Satélite", ultimoAvistamento);
        this.decaimentoOrbitalMetrosPorDia = decaimento;
    }

    public double getDecaimentoOrbitalMetrosPorDia() {
        return decaimentoOrbitalMetrosPorDia;
    }

    @Override
    public NivelRisco avaliarRisco() {
        if (decaimentoOrbitalMetrosPorDia > 1000) { 
            return NivelRisco.CRITICO;
        }
        return NivelRisco.BAIXO;
    }

    @Override
    public double calcularTaxaMovimentoPorMinuto() {
        return distanciaAtual.emKilometros() * 5.0e-10;
    }

    @Override
    public String obterDescricaoOrbital() {
        return "posição orbital em torno da Terra, com variação de fase por TLE aproximado.";
    }
}
