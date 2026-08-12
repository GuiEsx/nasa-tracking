package demo2;

public class Cometa extends ObjetoEspacial {
    private double tamanhoNucleoKm;

    public Cometa(String nome, Distancia distancia, double tamanhoNucleo) {
        // A MUDANÇA ESTÁ AQUI: repassando "Cometa" para o pai
        super(nome, distancia, "Cometa"); 
        this.tamanhoNucleoKm = tamanhoNucleo;
    }

    public NivelRisco avaliarRisco() {
        if (tamanhoNucleoKm > 10.0 && distanciaAtual.emDistanciasLunares() < 2.0) {
            return NivelRisco.ALTO;
        }
        return NivelRisco.MEDIO;
    }
}