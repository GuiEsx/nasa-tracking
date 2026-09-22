package model;

public class Distancia {
    private final double metros;

    public Distancia(double metros) {
        if (metros < 0) {
            throw new IllegalArgumentException("A distância não pode ser negativa!");
        }
        this.metros = metros;
    }

    // RF1 [E]: Métodos de fábrica para garantir medições seguras na criação
    public static Distancia deMetros(double metros) {
        return new Distancia(metros);
    }

    public static Distancia deKilometros(double km) {
        return new Distancia(km * 1000.0);
    }

    public static Distancia deMilhas(double milhas) {
        return new Distancia(milhas * 1609.34);
    }

    public static Distancia deDistanciasLunares(double ld) {
        return new Distancia(ld * 384400000.0);
    }

    public double emMetros() {
        return this.metros;
    }

    public double emKilometros() {
        return this.metros / 1000.0;
    }

    public double emMilhas() {
        return this.metros / 1609.34;
    }

    public double emDistanciasLunares() {
        return this.metros / 384400000.0;
    }

    @Override
    public String toString() {
        return String.format("%.2f km (%.4f LD)", emKilometros(), emDistanciasLunares());
    }
}
