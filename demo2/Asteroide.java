package demo2;

public class Asteroide extends ObjetoEspacial {
    private boolean ePerigosoPelaNasa;

    public Asteroide(String nome, Distancia distancia, boolean perigoso) {
        // A MUDANÇA ESTÁ AQUI: repassando "Asteroide" para o pai
        super(nome, distancia, "Asteroide"); 
        this.ePerigosoPelaNasa = perigoso;
    }

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
}