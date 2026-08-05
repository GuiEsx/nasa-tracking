package demo2;

public class Satelite extends ObjetoEspacial {
    private double decaimentoOrbitalMetrosPorDia;

    public Satelite(String nome, Distancia altitude, double decaimento) {
        // A MUDANÇA ESTÁ AQUI: repassando "Satélite" para o pai
        super(nome, altitude, "Satélite"); 
        this.decaimentoOrbitalMetrosPorDia = decaimento;
    }

    public NivelRisco avaliarRisco() {
        if (decaimentoOrbitalMetrosPorDia > 1000) { 
            return NivelRisco.CRITICO;
        }
        return NivelRisco.BAIXO;
    }
}