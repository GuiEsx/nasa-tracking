package model;

import java.time.LocalDateTime;

public class Cometa extends ObjetoEspacial {
    private double tamanhoNucleoKm;

    public Cometa(String nome, Distancia distancia, double tamanhoNucleo) {
        this(nome, nome, distancia, tamanhoNucleo, LocalDateTime.now());
    }

    public Cometa(String nome, Distancia distancia, double tamanhoNucleo, LocalDateTime ultimoAvistamento) {
        this(nome, nome, distancia, tamanhoNucleo, ultimoAvistamento);
    }

    public Cometa(String id, String nome, Distancia distancia, double tamanhoNucleo, LocalDateTime ultimoAvistamento) {
        super(id, nome, distancia, "Cometa", ultimoAvistamento);
        this.tamanhoNucleoKm = tamanhoNucleo;
    }

    public double getTamanhoNucleoKm() {
        return tamanhoNucleoKm;
    }

    @Override
    public NivelRisco avaliarRisco() {
        if (tamanhoNucleoKm > 10.0 && distanciaAtual.emDistanciasLunares() < 2.0) {
            return NivelRisco.ALTO;
        }
        return NivelRisco.MEDIO;
    }

    @Override
    public double calcularTaxaMovimentoPorMinuto() {
        return distanciaAtual.emKilometros() * 1.0e-11;
    }

    @Override
    public String obterDescricaoOrbital() {
        return "trajetória cometária em deslocamento solar, com aproximação gradual ao periélio.";
    }
}
