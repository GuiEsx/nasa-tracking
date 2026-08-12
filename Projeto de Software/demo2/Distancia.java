package demo2;

public class Distancia {
    private double metros; 

    public Distancia(double metros) {
        if (metros < 0) {
            throw new IllegalArgumentException("A distância não pode ser negativa!");
        }
        this.metros = metros;
    }

    public double emKilometros() { return this.metros / 1000.0; }
    public double emMilhas() { return this.metros / 1609.34; }
    public double emDistanciasLunares() { return this.metros / 384400000.0; }
}
