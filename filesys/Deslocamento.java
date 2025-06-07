package filesys;

public class Deslocamento {
    private int valorMax;
    private int valor;


    public Deslocamento(int valor) {
        setValor(valor);
        setValorMax(Integer.MAX_VALUE);
    }


    public int getValorMax() {
        return valorMax;
    }


    public void setValorMax(int valorMax) {
        if(valorMax < 0)
            throw new IllegalArgumentException("Valor não pode ser menor que 0");
        else if(this.valor > valorMax)
            throw new IllegalArgumentException("Valor padrão não pode ser maior que o limite");

        this.valorMax = valorMax;
    }


    public int getValor() {
        return valor;
    }


    public void setValor(int valor) {
        if(valorMax < 0)
            throw new IllegalArgumentException("Valor não pode ser menor que 0");
        else if(this.valor > valorMax)
            throw new IllegalArgumentException("Valor padrão não pode ser maior que o limite: " + valorMax);
    }

    
}
