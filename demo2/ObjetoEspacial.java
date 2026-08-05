package demo2;

public class ObjetoEspacial {
    protected String nome;
    protected Distancia distanciaAtual;
    protected String tipo; // O atributo que guarda o tipo

    public ObjetoEspacial(String nome, Distancia distanciaAtual, String tipo) {
        this.nome = nome;
        this.distanciaAtual = distanciaAtual;
        this.tipo = tipo;
    }

    public String getNome() { 
        return this.nome; 
    }
    
    public Distancia getDistancia() { 
        return this.distanciaAtual; 
    }
    
    // ESTE É O MÉTODO QUE O JAVA ESTÁ PROCURANDO:
    public String getTipo() { 
        return this.tipo; 
    } 
}