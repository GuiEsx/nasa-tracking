package model;

import java.time.LocalDateTime;

public class Asteroide extends ObjetoEspacial {
    private boolean ePerigosoPelaNasa;

    public Asteroide(String nome, Distancia distancia, boolean perigoso) {
        this(nome, nome, distancia, perigoso, LocalDateTime.now());
    }

    public Asteroide(String nome, Distancia distancia, boolean perigoso, LocalDateTime ultimoAvistamento) {
        this(nome, nome, distancia, perigoso, ultimoAvistamento);
    }

    public Asteroide(String id, String nome, Distancia distancia, boolean perigoso, LocalDateTime ultimoAvistamento) {
        super(id, nome, distancia, "Asteroide", ultimoAvistamento);
        this.ePerigosoPelaNasa = perigoso;
    }

    public boolean isEPerigosoPelaNasa() {
        return ePerigosoPelaNasa;
    }

    @Override
    public NivelRisco avaliarRisco() {
        if (ePerigosoPelaNasa && distanciaAtual.emDistanciasLunares() < 1.0) {
            return NivelRisco.CRITICO;
        } else if (distanciaAtual.emDistanciasLunares() < 5.0) {
            return NivelRisco.ALTO;
        } else if (distanciaAtual.emDistanciasLunares() < 10.0) {
            return NivelRisco.MEDIO;
        }
        return NivelRisco.BAIXO;
    }

    @Override
    public double calcularTaxaMovimentoPorMinuto() {
        return distanciaAtual.emKilometros() * 1.0e-10;
    }

    @Override
    public String obterDescricaoOrbital() {
        return "órbita heliocêntrica em translação, com deslocamento progressivo em relação ao observador.";
    }
}
